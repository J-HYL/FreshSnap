package com.marujho.freshsnap.data.repository

import retrofit2.HttpException
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


    private suspend fun saveCachedRecipes(recipes: List<CachedRecipe>) {
        val collection = getCachedRecipesCollection() ?: return
        recipes.forEach { recipe ->
            val docRef = collection.document()
            docRef.set(recipe.copy(id = docRef.id)).await()
        }
    }

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
            You are a professional chef very strict with logical ingredient combinations, an expert in classic recipes, food safety, and flavor balance.
        
            OBJECTIVE:
            Create a coherent and realistic recipe MANDATORILY using the main ingredients provided by the user.
            
            MANDATORY MAIN INGREDIENTS:
            ${selectedIngredients.joinToString(", ")}
            
            AVAILABLE OPTIONAL INGREDIENTS:
            ${if (availableIngredients.isEmpty()) "None" else availableIngredients.joinToString(", ")}
            
            ABSOLUTE RULES (MANDATORY):
            
            1. TOTAL PRIORITY TO MAIN INGREDIENTS
            - The recipe MUST be built around the mandatory ingredients.
            - All main ingredients must be used logically.
            
            2. SMART USE OF OPTIONAL INGREDIENTS
            - Optional ingredients are NOT mandatory.
            - Use ONLY 0, 1, or 2 optional ingredients if they truly improve the recipe.
            - If they do not combine naturally with the main ingredients, IGNORE THEM completely.
            - DO NOT try to use up the whole pantry.
            - DO NOT force absurd or incoherent combinations.
            
            3. CULINARY VALIDATION AND SAFETY
            - Evaluate if the combination of main ingredients is:
              - culinarily coherent,
              - safe for consumption,
              - technically viable.
              - does not include combinations of sweet and savory or similar things that might be unpleasant to some
            - If the recipe is unpleasant, toxic, dangerous, or practically impossible to cook:
              - return "is_possible": false
              - clearly explain the reason in "message"
              - leave the rest of the fields empty or with minimum valid values.
            
            4. REALISTIC AND APPETIZING RECIPE
            - The recipe must sound professional, coherent, and plausible.
            - Think like a strict chef:
              - flavor balance,
              - textures,
              - correct techniques,
              - classic and coherent combinations
              - do not be creative with the recipes, limit yourself to things that everyone likes
            - Prioritize recipes that a person would actually want to cook and eat.
            
            5. INGREDIENT MANAGEMENT
            - "ingredientes_tengo":
              - include ALL mandatory ingredients,
              - include ONLY the optional ingredients you actually use.
            - "ingredientes_falta":
              - add necessary basic ingredients:
                - salt,
                - oil,
                - butter,
                - spices, etc.
              - also add logical extra ingredients to properly complete the dish.
            - Each ingredient must include:
              - "name"
              - "measure"
            
            6. INSTRUCTIONS
            - Explain the step-by-step preparation clearly and professionally.
            - Include approximate times if relevant.
            - DO NOT make unnecessary explanations outside the recipe.
            
            $languageRule
            
            8. RESPONSE FORMAT
            - Respond EXCLUSIVELY with valid JSON.
            - DO NOT use markdown.
            - DO NOT add text outside the JSON.
            - DO NOT add comments.
            
            MANDATORY JSON SCHEMA:
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
            
            EXTRA QUALITY RULES:
            - DO NOT invent impossible techniques.
            - DO NOT add optional ingredients just for the sake of it.
            - DO NOT repeat ingredients unnecessarily.
            - DO NOT generate absurd recipes.
            - If there is a classic or known recipe that fits with the ingredients, prioritize it.
            - The recipe must maximize flavor, coherence, and smart simplicity.
        """.trimIndent()

        val userPrompt = if (isEnglish) {
            "Generate the recipe strictly following the rules."
        } else {
            "Genera la receta siguiendo estrictamente las reglas. Escribe TODO en español."
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

            // Parseo y guardado
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

        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                429 -> "Servidor saturado (Límite de peticiones alcanzado). Por favor, espera un minuto y vuelve a intentarlo."
                404 -> "Servicio de Inteligencia Artificial no encontrado temporalmente."
                401, 403 -> "Error de autenticación con el servidor de la IA."
                in 500..599 -> "Los servidores de la IA están experimentando problemas. Inténtalo más tarde."
                else -> "Error de conexión con la IA (Código: ${e.code()})."
            }
            Result.failure(Exception(errorMsg))

        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error inesperado de red."))
        }
    }
}
