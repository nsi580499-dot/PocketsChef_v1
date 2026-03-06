package es.uc3m.android.pockets_chef_app.data.model

data class Recipe(
    val id: Int = 0,
    val title: String,
    val description: String,
    val duration: String,
    val servings: Int,
    val category: String,
    val ingredients: String,
    val isFavorite: Boolean = false
)
