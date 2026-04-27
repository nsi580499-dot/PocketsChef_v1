package es.uc3m.android.pockets_chef_app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatSession(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

data class ChatMessage(
    val id: String = "",
    val role: String = "", // "user", "model", "system"
    val content: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val referencedRecipes: List<String> = emptyList()
)
