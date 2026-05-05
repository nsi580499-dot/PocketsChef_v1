package es.uc3m.android.pockets_chef_app.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import es.uc3m.android.pockets_chef_app.BuildConfig
import es.uc3m.android.pockets_chef_app.data.model.ChatMessage
import es.uc3m.android.pockets_chef_app.data.model.ChatSession
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
) {

    private val chatCollection = db.collection("chat_sessions")
    private val recipeCollection = db.collection("recipes")

    fun createGenerativeModel(user: User): GenerativeModel {
        val systemInstruction = """
            You are CookAI, an expert cooking assistant for Pockets Chef.
            Your goal is to help the user cook in a simple and fun way.

            User Information:
            - Name: ${user.displayName}
            - Level: ${user.cookingLevel}
            - Preferences: ${user.dietaryPreferences.joinToString()}
            - Bio: ${user.bio}

            Important rules:
            1. Always respond in English.
            2. Do NOT use Spanish under any circumstances.
            3. Do NOT start responses with greetings like "Hi" or "Hello".
            4. Be friendly and encouraging.
            5. Adapt explanations to the user's ${user.cookingLevel} level.
            6. Respect dietary preferences.
            7. You can use existing recipes from the Pockets Chef database when they are provided in the context.
            8. Do NOT claim that you saved, created, uploaded, published, or added a recipe to the app.
            9. If the user asks you to create a new recipe, only write the recipe as a chat answer. Do not say it has been saved.
            10. Keep answers clear and concise.
        """.trimIndent()

        return GenerativeModel(
            modelName = "gemini-3.1-flash-lite-preview",
            apiKey = apiKey,
            systemInstruction = content { text(systemInstruction) }
        )
    }

    private suspend fun retrieveRelevantContext(queryText: String): String {
        return try {
            val queryWords = queryText
                .lowercase()
                .split(" ", ",", ".", "?", "!", ":", ";", "\n")
                .map { it.trim() }
                .filter { it.length >= 3 }
                .toSet()

            val recipes = recipeCollection
                .whereEqualTo("isPublic", true)
                .limit(50)
                .get()
                .await()
                .toObjects(Recipe::class.java)

            val relevantRecipes = recipes.filter { recipe ->
                val searchableText = buildString {
                    append(recipe.title.lowercase())
                    append(" ")
                    append(recipe.description.lowercase())
                    append(" ")
                    append(recipe.category.lowercase())
                    append(" ")
                    append(recipe.ingredients.joinToString(" ") { it.name.lowercase() })
                    append(" ")
                    append(recipe.steps.joinToString(" ") { it.description.lowercase() })
                }

                queryWords.any { word -> searchableText.contains(word) }
            }.take(5)

            if (relevantRecipes.isEmpty()) {
                return "No matching recipes were found in the Pockets Chef database."
            }

            buildString {
                appendLine("Existing recipes from the Pockets Chef database that may be relevant:")
                relevantRecipes.forEach { recipe ->
                    appendLine()
                    appendLine("Recipe title: ${recipe.title}")
                    appendLine("Description: ${recipe.description}")
                    appendLine("Category: ${recipe.category}")
                    appendLine("Cooking time: ${recipe.duration} minutes")
                    appendLine("Servings: ${recipe.servings}")
                    appendLine("Ingredients: ${recipe.ingredients.joinToString { it.name }}")
                    appendLine("Steps: ${recipe.steps.joinToString(" | ") { it.description }}")
                }
            }

        } catch (e: Exception) {
            "Could not retrieve recipes from the database: ${e.message}"
        }
    }

    fun sendMessageStream(
        generativeModel: GenerativeModel,
        history: List<Content>,
        userMessage: String
    ): Flow<String> = flow {

        val context = retrieveRelevantContext(userMessage)

        val augmentedPrompt = """
            Database context:
            $context

            User request:
            $userMessage

            Answer using the database context when it is relevant.
            If a matching database recipe exists, mention its title.
            Do not save, create, upload, publish, or add recipes to the database.
        """.trimIndent()

        val chat = generativeModel.startChat(history)

        chat.sendMessageStream(augmentedPrompt).collect { response ->
            response.text?.let { emit(it) }
        }
    }

    suspend fun startNewChatSession(userId: String, title: String): Result<String> = try {
        val docRef = chatCollection.document()
        val session = ChatSession(
            id = docRef.id,
            userId = userId,
            title = title
        )
        docRef.set(session).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addMessageToSession(sessionId: String, message: ChatMessage): Result<String> = try {
        val messageRef = chatCollection.document(sessionId).collection("messages").document()
        val messageWithId = message.copy(id = messageRef.id)
        messageRef.set(messageWithId).await()

        chatCollection.document(sessionId)
            .update("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())
            .await()

        Result.success(messageRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getMessagesFlow(sessionId: String): Flow<List<ChatMessage>> {
        return chatCollection.document(sessionId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .dataObjects<ChatMessage>()
    }
}