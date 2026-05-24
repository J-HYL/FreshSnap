package com.marujho.freshsnap.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.marujho.freshsnap.data.domain.IngredientMasticator
import com.marujho.freshsnap.data.model.CachedRecipe
import com.marujho.freshsnap.data.model.IngredientClassification
import com.marujho.freshsnap.data.model.RecipeIngredient
import com.marujho.freshsnap.data.model.RecipeSource
import com.marujho.freshsnap.data.model.UserProduct
import com.marujho.freshsnap.data.remote.api.GroqApi
import com.marujho.freshsnap.data.remote.api.TheMealDbApi
import com.marujho.freshsnap.data.remote.dto.groq.GroqMessageDto
import com.marujho.freshsnap.data.remote.dto.groq.GroqRecipeResponseDto
import com.marujho.freshsnap.data.remote.dto.groq.GroqRequestDto
import com.marujho.freshsnap.data.remote.dto.groq.GroqResponseFormatDto
import com.marujho.freshsnap.data.remote.dto.mealdb.MealDetailDto
import com.squareup.moshi.Moshi
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val mealDbApi: TheMealDbApi,
    private val groqApi: GroqApi,
    private val masticator: IngredientMasticator,
    private val moshi: Moshi
) {
    private fun getCachedRecipesCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("cached_recipes")
    }

    // region System Prompts

    private val filterSystemPrompt = """
You are a strict culinary expert. Given user ingredients classified by freshness (R=expiring soon; G=fresh, available) and a list of MealDB recipes, pick the BEST recipe that uses some 'R' ingredients. 
CRITICAL: The chosen recipe MUST make absolute culinary sense. 
Respond ONLY with valid JSON matching this schema:
{"recipe_id":"string","title":"string","ingredientes_tengo":[{"name":"string","measure":"string"}],"ingredientes_falta":[{"name":"string","measure":"string"}],"reason":"string"}
Rules: Be concise. Do not invent ingredients that are not in the original recipe. The final text output must be in Spanish.
    """.trimIndent()

    private val generateSystemPrompt = """
You are a strict, Michelin-star chef. You will be given user ingredients classified by freshness (R=expiring soon, high priority; G=fresh, available). 
Create ONE realistic, delicious recipe that makes absolute culinary sense. 
CRITICAL RULES: 
1. NEVER mix incompatible ingredients just to use them up. Do NOT create disgusting or weird combinations (e.g., mixing dairy with isotonic drinks, or fish with chocolate).
2. It is STRICTLY FORBIDDEN to use all 'R' ingredients if they do not belong in the same flavor profile. If they clash, pick just ONE or TWO 'R' ingredients and build a normal, tasty dish around them. Discard the rest.
Respond ONLY with valid JSON matching this schema:
{"title":"string","instructions":"string","ingredientes_tengo":[{"name":"string","measure":"string"}],"ingredientes_falta":[{"name":"string","measure":"string"}],"category":"string","area":"string"}
Formatting Rules:
1. 'instructions' must contain clear, numbered steps.
2. Put the R and G ingredients you decided to use in 'ingredientes_tengo'. Exclude any provided ingredients that ruin the dish.
3. Put only strictly necessary, logical extra ingredients to make the dish work in 'ingredientes_falta'.
4. The response language must be Spanish.
    """.trimIndent()

    // endregion

    /**
     * Punto de entrada principal. Orquesta las 3 capas:
     * 1. Cache Firestore (si inventoryHash coincide y < 24h)
     * 2. TheMealDB + Groq Filter
     * 3. Groq Generate (si MealDB no tiene resultados)
     */

    private suspend fun saveCachedRecipes(recipes: List<CachedRecipe>) {
        val collection = getCachedRecipesCollection() ?: return
        recipes.forEach { recipe ->
            val docRef = collection.document()
            docRef.set(recipe.copy(id = docRef.id)).await()
        }
    }

    // endregion

    // region Capa 1: TheMealDB


    private fun parseGroqRecipeResponse(json: String): GroqRecipeResponseDto? {
        return try {
            val adapter = moshi.adapter(GroqRecipeResponseDto::class.java)
            adapter.fromJson(json)
        } catch (e: Exception) {
            Log.e(TAG, "parseGroqRecipeResponse failed: ${json.take(200)}", e)
            null
        }
    }

    // endregion

    companion object {
        private const val TAG = "RecipeRepository"
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24 horas
        private const val MAX_MEALDB_RESULTS = 5
    }

    suspend fun generateCustomRecipe(
        selectedIngredients: List<String>,
        availableIngredients: List<String>,
        languageCode: String
    ): Result<CachedRecipe> {
        val isEnglish = languageCode.lowercase().startsWith("en")

        val languageRule = if (isEnglish) {
            """
            7. LANGUAGE / IDIOMA (CRITICAL)
            - YOU MUST WRITE THE ENTIRE RESPONSE IN ENGLISH.
            - All recipe titles, instructions, and ingredients MUST be translated to English.
            - DO NOT output any Spanish text in the final JSON values.
            """.trimIndent()
        } else {
            """
            7. IDIOMA
            - Toda la respuesta debe estar en ESPAÑOL.
            """.trimIndent()
        }

        val systemPrompt = """
            Eres un chef profesional con múltiples estrellas Michelin, experto en creatividad culinaria, seguridad alimentaria y equilibrio de sabores.

            OBJETIVO:
            Crear una receta deliciosa, coherente y realista utilizando OBLIGATORIAMENTE los ingredientes principales proporcionados por el usuario.
            
            INGREDIENTES PRINCIPALES OBLIGATORIOS:
            ${selectedIngredients.joinToString(", ")}
            
            INGREDIENTES DISPONIBLES OPCIONALES:
            ${if (availableIngredients.isEmpty()) "Ninguno" else availableIngredients.joinToString(", ")}
            
            REGLAS ABSOLUTAS (OBLIGATORIAS):
            
            1. PRIORIDAD TOTAL A LOS INGREDIENTES PRINCIPALES
            - La receta DEBE construirse alrededor de los ingredientes obligatorios.
            - Todos los ingredientes principales deben utilizarse de forma lógica y relevante dentro del plato.
            
            2. USO INTELIGENTE DE LOS INGREDIENTES OPCIONALES
            - Los ingredientes opcionales NO son obligatorios.
            - Usa SOLO 0, 1, 2 o 3 ingredientes opcionales si realmente mejoran la receta.
            - Si no combinan de forma natural con los ingredientes principales, IGNÓRALOS completamente.
            - NO intentes gastar toda la despensa.
            - NO fuerces combinaciones absurdas o incoherentes.
            
            3. VALIDACIÓN CULINARIA Y SEGURIDAD
            - Evalúa si la combinación de ingredientes principales es:
              - culinariamente coherente,
              - segura para el consumo,
              - técnicamente viable.
            - Si la receta es desagradable, tóxica, peligrosa o prácticamente imposible de cocinar:
              - devuelve "is_possible": false
              - explica claramente el motivo en "message"
              - deja el resto de campos vacíos o con valores mínimos válidos.
            
            4. RECETA REALISTA Y APETECIBLE
            - La receta debe sonar profesional, sabrosa y plausible.
            - Evita recetas genéricas o sin personalidad.
            - Piensa como un chef Michelin:
              - equilibrio de sabores,
              - texturas,
              - técnicas correctas,
              - presentación atractiva.
            - Prioriza recetas que una persona realmente querría cocinar y comer.
            
            5. GESTIÓN DE INGREDIENTES
            - "ingredientes_tengo":
              - incluye TODOS los ingredientes obligatorios,
              - incluye SOLO los ingredientes opcionales que realmente uses.
            - "ingredientes_falta":
              - añade ingredientes básicos necesarios:
                - sal,
                - aceite,
                - pimienta,
                - especias,
                - mantequilla,
                - ajo,
                - cebolla, etc.
              - añade también ingredientes extra lógicos y mínimos necesarios para completar bien el plato.
            - Cada ingrediente debe incluir:
              - "name"
              - "measure"
            
            6. INSTRUCCIONES
            - Explica la preparación paso a paso de forma clara y profesional.
            - Incluye tiempos aproximados si es relevante.
            - NO hagas explicaciones innecesarias fuera de la receta.
            
            $languageRule
            
            8. FORMATO DE RESPUESTA
            - Responde EXCLUSIVAMENTE con JSON válido.
            - NO uses markdown.
            - NO añadas texto fuera del JSON.
            - NO añadas comentarios.
            
            ESQUEMA JSON OBLIGATORIO:
            {
              "is_possible": boolean,
              "message": "string",
              "title": "string",
              "instructions": "string",
              "ingredientes_tengo": [
                {
                  "name": "string",
                  "measure": "string"
                }
              ],
              "ingredientes_falta": [
                {
                  "name": "string",
                  "measure": "string"
                }
              ]
            }
            
            REGLAS EXTRA DE CALIDAD:
            - NO inventes técnicas imposibles.
            - NO añadas ingredientes opcionales porque sí.
            - NO repitas ingredientes innecesariamente.
            - NO generes recetas infantiles o absurdas.
            - Si existe una receta clásica o conocida que encaje con los ingredientes, priorízala.
            - La receta debe maximizar sabor, coherencia y simplicidad inteligente.
        """.trimIndent()

        val userPrompt = if (isEnglish) {
            "Generate the recipe strictly following the rules. Write EVERYTHING in English."
        } else {
            "Genera la receta siguiendo estrictamente las reglas."
        }

        val request = GroqRequestDto(
            messages = listOf(
                GroqMessageDto(role = "system", content = systemPrompt),
                GroqMessageDto(role = "user", content = userPrompt)
            ),
            temperature = 0.2f,
            maxTokens = 1000
        )

        return try {
            val response = groqApi.chatCompletion(request)
            val jsonContent = response.choices.firstOrNull()?.message?.content ?: throw Exception("Respuesta vacía de la IA")

            val parsed = parseGroqRecipeResponse(jsonContent) ?: throw Exception("Error al procesar el formato de la receta")

            if (parsed.isPossible == false) {
                return Result.failure(Exception(parsed.message ?: "Combinación de ingredientes inviable."))
            }

            val recipe = CachedRecipe(
                title = parsed.title ?: "Receta Sorpresa",
                instructions = parsed.instructions ?: "",
                ingredientsOwned = CachedRecipe.fromIngredients(
                    parsed.ingredientesTengo?.map { RecipeIngredient(it.name, it.measure) } ?: emptyList()
                ),
                ingredientsMissing = CachedRecipe.fromIngredients(
                    parsed.ingredientesFalta?.map { RecipeIngredient(it.name, it.measure) } ?: emptyList()
                ),
                source = RecipeSource.GROQ_GENERATED.name,
                generatedAt = System.currentTimeMillis()
            )

            saveCachedRecipes(listOf(recipe))

            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
