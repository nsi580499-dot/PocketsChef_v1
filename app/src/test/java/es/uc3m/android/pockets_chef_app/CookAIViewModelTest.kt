package es.uc3m.android.pockets_chef_app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CookAIViewModelTest {

    // Copy of the logic from CookAIViewModel — no Firebase needed
    private fun containsRecipe(content: String): Boolean {
        val lower = content.lowercase()
        return (lower.contains("ingredient") || lower.contains("ingrediente")) &&
                (lower.contains("step") || lower.contains("instruction") ||
                        lower.contains("preparation") || lower.contains("paso"))
    }

    @Test
    fun containsRecipe_returnsTrue_whenMessageHasIngredientsAndSteps() {
        val message = """
            Ingredients:
            - 200g pasta
            - 2 eggs
            Steps:
            1. Boil the pasta
            2. Mix with eggs
        """.trimIndent()

        assertTrue(containsRecipe(message))
    }

    @Test
    fun containsRecipe_returnsFalse_forPlainMessage() {
        val message = "Hi! How can I help you today?"

        assertFalse(containsRecipe(message))
    }

    @Test
    fun containsRecipe_returnsFalse_whenOnlyIngredientsPresent() {
        val message = "You will need some ingredients like eggs and milk."

        assertFalse(containsRecipe(message))
    }
}