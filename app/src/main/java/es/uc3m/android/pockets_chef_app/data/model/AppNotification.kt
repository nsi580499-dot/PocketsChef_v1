package es.uc3m.android.pockets_chef_app.data.model

import com.google.firebase.firestore.DocumentId

data class AppNotification(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val type: String = "",
    val read: Boolean = false,
    val actorUid: String? = null,
    val actorName: String? = null
)