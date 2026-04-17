package es.uc3m.android.pockets_chef_app.data.model

data class PantryItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
    val category: String = "",
    val expiryDate: Long = 0L,
    val isExpiringSoon: Boolean = false
)
