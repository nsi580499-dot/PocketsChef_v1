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
    private val usersCollection = db.collection("users")

    // 1. Inicialización con Contexto (System Instruction)
    fun createGenerativeModel(user: User): GenerativeModel {
        val systemInstruction = """
            You are CookAI, an expert cooking assistant for Pockets Chef.
            Your goal is to help the user cook in a simple and fun way.
            
            User Information:
            - Name: ${user.displayName}
            - Level: ${user.cookingLevel}
            - Preferences: ${user.dietaryPreferences.joinToString()}
            - Bio: ${user.bio}
            
            Instructions:
            1. Be friendly and encouraging.
            2. Adapt your explanations to the user's ${user.cookingLevel} level.
            3. Take their dietary preferences into account.
            4. If the context includes recipes from the database, use them to provide accurate answers.
            5. Be concise in your answers.
            """.trimIndent()

        return GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
            systemInstruction = content { text(systemInstruction) }
        )
    }

    // 2. Retrieval: Simulación de búsqueda en Firestore
    private suspend fun retrieveRelevantContext(queryText: String): String {
        return try {
            // Simulación simple: buscamos recetas que coincidan con palabras clave del mensaje
            val recipes = recipeCollection
                .whereArrayContainsAny("category", queryText.split(" ")) // Ejemplo de búsqueda
                .limit(3)
                .get()
                .await()
                .toObjects(Recipe::class.java)

            if (recipes.isEmpty()) return "No se encontraron recetas específicas relacionadas."

            val context = StringBuilder("Aquí hay información de recetas relevantes en Pockets Chef:\n")
            recipes.forEach { recipe ->
                context.append("- ${recipe.title}: ${recipe.description}. Ingredientes: ${recipe.ingredients.joinToString { it.name }}\n")
            }
            context.toString()
        } catch (e: Exception) {
            "Error recuperando contexto: ${e.message}"
        }
    }

    // 3. Lógica del Chat RAG con Streaming
    fun sendMessageStream(
        generativeModel: GenerativeModel,
        history: List<Content>,
        userMessage: String
    ): Flow<String> = flow {
        // A. Retrieval
        val context = retrieveRelevantContext(userMessage)
        
        // B. Augmentation: Combinar contexto + pregunta
        val augmentedPrompt = """
            Contexto de la base de datos:
            $context
            
            Pregunta del usuario:
            $userMessage
        """.trimIndent()

        // C. Generation (Streaming)
        val chat = generativeModel.startChat(history)
        chat.sendMessageStream(augmentedPrompt).collect { response ->
            response.text?.let { emit(it) }
        }
    }

    // Firebase methods for session management
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
        chatCollection.document(sessionId).update("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp()).await()
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
