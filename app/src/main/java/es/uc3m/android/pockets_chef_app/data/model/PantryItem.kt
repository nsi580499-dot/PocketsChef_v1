package es.uc3m.android.pockets_chef_app.data.model

data class PantryItem(
    val id: Int = 0,
    val name: String,
    val quantity: String,
    val unit: String,
    val category: String,
    val expiryDate: Long,
    val isExpiringSoon: Boolean = false
)
