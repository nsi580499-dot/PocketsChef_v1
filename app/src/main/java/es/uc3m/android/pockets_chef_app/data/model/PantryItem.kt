package es.uc3m.android.pockets_chef_app.data.model

import com.google.firebase.firestore.PropertyName

data class PantryItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
    val category: String = "",
    val expiryDate: Long = 0L,
    @get:PropertyName("isExpiringSoon")
    @set:PropertyName("isExpiringSoon")
    var isExpiringSoon: Boolean = false
)
