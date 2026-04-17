package es.uc3m.android.pockets_chef_app.data.model

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val age: Int = 18,
    val description: String = "",
    val level: String = "Beginner",
    val diet: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val favoriteRecipes: List<String> = emptyList(),
    val pantryItemIds: List<String> = emptyList(),
    val photoUrl: String = "",
    val favoriteCuisine: String = "",
    val createdAt: Long = 0L
)