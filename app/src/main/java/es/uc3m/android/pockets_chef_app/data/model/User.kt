package es.uc3m.android.pockets_chef_app.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val cookingLevel: String = "Beginner",
    val dietaryPreferences: List<String> = emptyList(),
    val favoriteCuisine: String = "",
    val myRecipeIds: List<String> = emptyList(),
    val followersCount: Int = 0,
    val followingCount: Int = 0
)
