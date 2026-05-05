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
    suspend fun getRecipeSuggestions(
        products: List<UserProduct>,
        redDays: Int,
        yellowDays: Int,
        forceRefresh: Boolean = false
    ): Result<List<CachedRecipe>> {
        return try {
            val classification = masticator.classify(products, redDays, yellowDays)
            Log.d(TAG, "classify: red=${classification.redIngredients} yellow=${classification.yellowIngredients} green=${classification.greenIngredients}")

            if (classification.redIngredients.isEmpty()) {
                Log.d(TAG, "No red ingredients, returning empty")
                return Result.success(emptyList())
            }

            val inventoryHash = masticator.computeInventoryHash(classification)

            // Capa 0: Cache Firestore
            if (!forceRefresh) {
                val cached = getCachedRecipes(inventoryHash)
                Log.d(TAG, "cache lookup: ${cached.size} recipes")
                if (cached.isNotEmpty()) {
                    return Result.success(cached)
                }
            }

            val recipes = mutableListOf<CachedRecipe>()

            // Capa 1: TheMealDB - buscar por primer ingrediente RED
            val mainIngredient = classification.redIngredients.first()
            Log.d(TAG, "Layer 1: searching MealDB for '$mainIngredient'")
            val mealDbResults = searchMealDb(mainIngredient)
            Log.d(TAG, "Layer 1 result: ${mealDbResults.size} meals")

            if (mealDbResults.isNotEmpty()) {
                // Capa 2: Groq Filter - elegir la mejor receta
                Log.d(TAG, "Layer 2: filtering with Groq")
                val filtered = filterWithGroq(mealDbResults, classification)
                Log.d(TAG, "Layer 2 result: ${filtered?.title ?: "null"}")
                if (filtered != null) {
                    recipes.add(
                        filtered.copy(
                            inventoryHash = inventoryHash,
                            generatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // Capa 3: Groq Generate - si MealDB no dio resultados o como receta adicional
            if (recipes.isEmpty()) {
                Log.d(TAG, "Layer 3: generating with Groq")
                val generated = generateWithGroq(classification)
                Log.d(TAG, "Layer 3 result: ${generated?.title ?: "null"}")
                if (generated != null) {
                    recipes.add(
                        generated.copy(
                            inventoryHash = inventoryHash,
                            generatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // Guardar en Firestore
            if (recipes.isNotEmpty()) {
                saveCachedRecipes(recipes)
                Result.success(recipes)
            } else {
                Log.e(TAG, "All layers failed: no recipes generated")
                Result.failure(
                    Exception(
                        "No se pudieron generar recetas. Comprueba tu conexion y la GROQ_API_KEY."
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getRecipeSuggestions exception", e)
            Result.failure(e)
        }
    }

    // region Capa 0: Cache Firestore

    private suspend fun getCachedRecipes(inventoryHash: String): List<CachedRecipe> {
        val collection = getCachedRecipesCollection() ?: return emptyList()
        val cutoff = System.currentTimeMillis() - CACHE_TTL_MS

        val snapshot = collection
            .whereEqualTo("inventoryHash", inventoryHash)
            .whereGreaterThan("generatedAt", cutoff)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(CachedRecipe::class.java)?.copy(id = doc.id)
        }
    }

    private suspend fun saveCachedRecipes(recipes: List<CachedRecipe>) {
        val collection = getCachedRecipesCollection() ?: return
        recipes.forEach { recipe ->
            val docRef = collection.document()
            docRef.set(recipe.copy(id = docRef.id)).await()
        }
    }

    // endregion

    // region Capa 1: TheMealDB

    private suspend fun searchMealDb(ingredient: String): List<MealDetailDto> {
        return try {
            val filterResponse = mealDbApi.searchByIngredient(ingredient)
            val meals = filterResponse.meals ?: return emptyList()

            // Obtener detalles de los primeros 5 resultados (limitar llamadas)
            meals.take(MAX_MEALDB_RESULTS).mapNotNull { summary ->
                try {
                    val lookupResponse = mealDbApi.getMealById(summary.id ?: return@mapNotNull null)
                    lookupResponse.meals?.firstOrNull()
                } catch (e: Exception) {
                    Log.w(TAG, "MealDB lookup failed for id=${summary.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "MealDB search failed for '$ingredient'", e)
            emptyList()
        }
    }

    // endregion

    // region Capa 2: Groq Filter

    private suspend fun filterWithGroq(
        meals: List<MealDetailDto>,
        classification: IngredientClassification
    ): CachedRecipe? {
        val compactIngredients = masticator.toCompactString(classification)

        // Construir resumen de recetas para Groq
        val mealsDescription = meals.joinToString("\n") { meal ->
            val ingredients = meal.getIngredientsList()
                .joinToString(",") { it.first }
            "[${meal.id}] ${meal.name}: $ingredients"
        }

        val userMessage = "Ingredients: $compactIngredients\nRecipes:\n$mealsDescription"

        val request = GroqRequestDto(
            messages = listOf(
                GroqMessageDto(role = "system", content = filterSystemPrompt),
                GroqMessageDto(role = "user", content = userMessage)
            ),
            temperature = 0.3f,
            maxTokens = 500
        )

        return try {
            val response = groqApi.chatCompletion(request)
            val jsonContent = response.choices.firstOrNull()?.message?.content
            Log.d(TAG, "Groq Filter raw content: ${jsonContent?.take(300)}")
            if (jsonContent == null) return null
            val parsed = parseGroqRecipeResponse(jsonContent) ?: return null

            // Encontrar la receta original de MealDB para obtener imagen e instrucciones
            val originalMeal = meals.find { it.id == parsed.recipeId }

            CachedRecipe(
                title = parsed.title.ifBlank { originalMeal?.name ?: "Receta" },
                imageUrl = originalMeal?.thumbnailUrl,
                instructions = originalMeal?.instructions ?: "",
                ingredientsOwned = CachedRecipe.fromIngredients(
                    parsed.ingredientesTengo.map { RecipeIngredient(it.name, it.measure) }
                ),
                ingredientsMissing = CachedRecipe.fromIngredients(
                    parsed.ingredientesFalta.map { RecipeIngredient(it.name, it.measure) }
                ),
                source = RecipeSource.GROQ_FILTERED.name
            )
        } catch (e: Exception) {
            Log.e(TAG, "filterWithGroq exception, using MealDB fallback", e)
            // Fallback: devolver primera receta de MealDB sin filtro Groq
            meals.firstOrNull()?.let { meal ->
                val allIngredients = meal.getIngredientsList()
                CachedRecipe(
                    title = meal.name ?: "Receta",
                    imageUrl = meal.thumbnailUrl,
                    instructions = meal.instructions ?: "",
                    ingredientsOwned = CachedRecipe.fromIngredients(
                        allIngredients.map { RecipeIngredient(it.first, it.second) }
                    ),
                    ingredientsMissing = emptyList(),
                    source = RecipeSource.MEALDB.name
                )
            }
        }
    }

    // endregion

    // region Capa 3: Groq Generate

    private suspend fun generateWithGroq(
        classification: IngredientClassification
    ): CachedRecipe? {
        val compactIngredients = masticator.toCompactString(classification)
        val userMessage = "Ingredients: $compactIngredients"

        val request = GroqRequestDto(
            messages = listOf(
                GroqMessageDto(role = "system", content = generateSystemPrompt),
                GroqMessageDto(role = "user", content = userMessage)
            ),
            temperature = 0.7f,
            maxTokens = 800
        )

        return try {
            val response = groqApi.chatCompletion(request)
            val jsonContent = response.choices.firstOrNull()?.message?.content
            Log.d(TAG, "Groq Generate raw content: ${jsonContent?.take(300)}")
            if (jsonContent == null) return null
            val parsed = parseGroqRecipeResponse(jsonContent) ?: return null

            CachedRecipe(
                title = parsed.title,
                instructions = parsed.instructions ?: "",
                ingredientsOwned = CachedRecipe.fromIngredients(
                    parsed.ingredientesTengo.map { RecipeIngredient(it.name, it.measure) }
                ),
                ingredientsMissing = CachedRecipe.fromIngredients(
                    parsed.ingredientesFalta.map { RecipeIngredient(it.name, it.measure) }
                ),
                source = RecipeSource.GROQ_GENERATED.name
            )
        } catch (e: Exception) {
            Log.e(TAG, "generateWithGroq exception", e)
            null
        }
    }

    // endregion

    // region JSON Parsing

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
}
