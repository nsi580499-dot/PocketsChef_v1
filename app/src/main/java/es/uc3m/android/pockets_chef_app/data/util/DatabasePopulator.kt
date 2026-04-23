package es.uc3m.android.pockets_chef_app.data.util

import es.uc3m.android.pockets_chef_app.data.model.Ingredient
import es.uc3m.android.pockets_chef_app.data.model.PantryItem
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.RecipeStep
import es.uc3m.android.pockets_chef_app.data.model.User
import es.uc3m.android.pockets_chef_app.data.repository.PantryRepository
import es.uc3m.android.pockets_chef_app.data.repository.RecipeRepository
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabasePopulator(
    private val recipeRepository: RecipeRepository = RecipeRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val pantryRepository: PantryRepository = PantryRepository()
) {
    suspend fun populateMockData(currentUserId: String) = withContext(Dispatchers.IO) {
        // 1. Create some users (Other Chefs + Example User)
        val mockUsers = listOf(
            User(uid = "chef_julia", displayName = "Julia Child", cookingLevel = "Pro Chef", bio = "Mastering the art of French cooking."),
            User(uid = "chef_ramsay", displayName = "Gordon R.", cookingLevel = "Pro Chef", bio = "Where is the lamb sauce?!"),
            User(uid = "chef_jamie", displayName = "Jamie O.", cookingLevel = "Intermediate", bio = "Keeping it simple and fresh."),
            User(uid = "chef_martha", displayName = "Martha S.", cookingLevel = "Pro Chef", bio = "Good things happen in the kitchen."),
            User(uid = currentUserId, displayName = "Example Chef", email = "example@pocketschef.com", cookingLevel = "Beginner", bio = "I am here to learn how to cook!")
        )

        mockUsers.forEach { userRepository.createUserProfile(it) }

        // 2. Create some recipes for these users
        val mockRecipes = listOf(
            Recipe(
                title = "French Omelette",
                description = "A classic, smooth and buttery omelette.",
                duration = "5 min",
                servings = 1,
                category = "Breakfast",
                authorId = "chef_julia",
                authorName = "Julia Child",
                ingredients = listOf(
                    Ingredient("Eggs", "3 large"),
                    Ingredient("Butter", "1 tbsp"),
                    Ingredient("Chives", "1 tsp")
                ),
                steps = listOf(
                    RecipeStep(1, "Beat the eggs until combined but not foamy."),
                    RecipeStep(2, "Melt butter in a skillet over medium heat."),
                    RecipeStep(3, "Pour in eggs and stir constantly until curds form."),
                    RecipeStep(4, "Roll the omelette and serve immediately.")
                )
            ),
            Recipe(
                title = "Beef Wellington",
                description = "Gordon's signature luxury dish.",
                duration = "2 hours",
                servings = 6,
                category = "Main",
                authorId = "chef_ramsay",
                authorName = "Gordon R.",
                ingredients = listOf(
                    Ingredient("Beef Fillet", "1kg"),
                    Ingredient("Puff Pastry", "500g"),
                    Ingredient("Mushrooms", "250g"),
                    Ingredient("Prosciutto", "8 slices")
                ),
                steps = listOf(
                    RecipeStep(1, "Sear the beef on all sides."),
                    RecipeStep(2, "Wrap beef in mushroom duxelles and prosciutto."),
                    RecipeStep(3, "Encase in puff pastry."),
                    RecipeStep(4, "Bake until golden and internal temp is 52°C.")
                )
            ),
            Recipe(
                title = "Quick Pasta",
                description = "Fresh pasta in under 15 minutes.",
                duration = "15 min",
                servings = 2,
                category = "Main",
                authorId = "chef_jamie",
                authorName = "Jamie O.",
                ingredients = listOf(
                    Ingredient("Spaghetti", "200g"),
                    Ingredient("Garlic", "2 cloves"),
                    Ingredient("Olive Oil", "3 tbsp"),
                    Ingredient("Chili", "1 small")
                ),
                steps = listOf(
                    RecipeStep(1, "Cook pasta in salted water."),
                    RecipeStep(2, "Sauté garlic and chili in oil."),
                    RecipeStep(3, "Toss pasta with the oil and a splash of cooking water.")
                )
            ),
            Recipe(
                title = "Classic Avocado Toast",
                description = "Healthy and trendy breakfast.",
                duration = "10 min",
                servings = 1,
                category = "Breakfast",
                authorId = currentUserId,
                authorName = "Example Chef",
                ingredients = listOf(
                    Ingredient("Bread", "2 slices"),
                    Ingredient("Avocado", "1 ripe"),
                    Ingredient("Lemon", "half"),
                    Ingredient("Red Pepper Flakes", "a pinch")
                ),
                steps = listOf(
                    RecipeStep(1, "Toast the bread slices."),
                    RecipeStep(2, "Mash the avocado with lemon juice and salt."),
                    RecipeStep(3, "Spread on toast and sprinkle with red pepper flakes.")
                )
            )
        )

        mockRecipes.forEach { recipeRepository.createRecipe(it) }

        // 3. Populate Pantry for the current User
        val mockPantry = listOf(
            PantryItem(name = "Milk", quantity = "1", unit = "L", category = "Dairy", expiryDate = System.currentTimeMillis() + 86400000 * 2, isExpiringSoon = true),
            PantryItem(name = "Eggs", quantity = "12", unit = "units", category = "Dairy", expiryDate = System.currentTimeMillis() + 86400000 * 7),
            PantryItem(name = "Pasta", quantity = "500", unit = "g", category = "Grains", expiryDate = System.currentTimeMillis() + 86400000 * 30),
            PantryItem(name = "Tomato Sauce", quantity = "1", unit = "jar", category = "Condiments", expiryDate = System.currentTimeMillis() + 86400000 * 60),
            PantryItem(name = "Chicken", quantity = "2", unit = "breasts", category = "Meat", expiryDate = System.currentTimeMillis() + 86400000 * 1, isExpiringSoon = true)
        )

        mockPantry.forEach { pantryRepository.addItem(currentUserId, it) }
    }
}
