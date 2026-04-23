package es.uc3m.android.pockets_chef_app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import es.uc3m.android.pockets_chef_app.data.model.ChatMessage
import es.uc3m.android.pockets_chef_app.data.model.ChatSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ChatRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val chatCollection = db.collection("chat_sessions")

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
        
        // Update the session's updatedAt timestamp
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
