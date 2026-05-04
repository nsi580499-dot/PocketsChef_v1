package es.uc3m.android.pockets_chef_app.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Ingredient(
    val name: String = "",
    val amount: String = ""
)

data class RecipeStep(
    val order: Int = 0,
    val description: String = ""
)

data class Recipe(
    val id: String = "",
    val title: String = "",
    val source: String = "", //this one is for testing
    val description: String = "",
    val duration: String = "",
    val servings: Int = 0,
    val imageUrl: String = "",
    val category: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    @get:PropertyName("isFavorite")
    @set:PropertyName("isFavorite")
    var isFavorite: Boolean = false, // Local-only or temporarily mapped
    val authorId: String = "",
    val authorName: String = "",
    @get:PropertyName("isPublic")
    @set:PropertyName("isPublic")
    var isPublic: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null
)

val source: String = "" //this one is for testing