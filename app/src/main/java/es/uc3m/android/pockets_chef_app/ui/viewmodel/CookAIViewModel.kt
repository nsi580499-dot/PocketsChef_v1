package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.ChatMessage
import es.uc3m.android.pockets_chef_app.data.model.Ingredient
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.RecipeStep
import es.uc3m.android.pockets_chef_app.data.repository.ChatRepository
import es.uc3m.android.pockets_chef_app.data.repository.RecipeRepository
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CookAIViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val recipeRepository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<CookAIUiState>(CookAIUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _recipeSaved = MutableStateFlow(false)
    val recipeSaved = _recipeSaved.asStateFlow()

    private val _isSavingRecipe = MutableStateFlow(false)
    val isSavingRecipe = _isSavingRecipe.asStateFlow()

    val messages = mutableStateListOf<ChatMessage>()

    private var generativeModel: GenerativeModel? = null

    init {
        initializeCookAI()
    }

    private fun initializeCookAI() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.value = CookAIUiState.Loading

            userRepository.getUserProfile(uid).onSuccess { user ->
                if (user != null) {
                    generativeModel = chatRepository.createGenerativeModel(user)
                    _uiState.value = CookAIUiState.Ready

                    if (messages.isEmpty()) {
                        messages.add(
                            ChatMessage(
                                role = "model",
                                content = "Hi ${user.displayName}! I'm CookAI. What can I help you with today?"
                            )
                        )
                    }
                } else {
                    _uiState.value = CookAIUiState.Error("Failed to load user profile")
                }
            }.onFailure {
                _uiState.value = CookAIUiState.Error(it.message ?: "Unknown error")
            }
        }
    }

    fun sendMessage(text: String) {
        val model = generativeModel ?: return
        if (text.isBlank()) return

        messages.add(ChatMessage(role = "user", content = text))
        val aiMessageIndex = messages.size
        messages.add(ChatMessage(role = "model", content = ""))

        viewModelScope.launch {
            try {
                val history = messages.dropLast(1).map { msg ->
                    content(msg.role) { text(msg.content) }
                }

                var fullResponse = ""

                chatRepository.sendMessageStream(model, history, text).collect { chunk ->
                    fullResponse += chunk
                    messages[aiMessageIndex] =
                        messages[aiMessageIndex].copy(content = fullResponse)
                }

            } catch (e: Exception) {
                messages[aiMessageIndex] =
                    messages[aiMessageIndex].copy(
                        content = "Sorry, something went wrong: ${e.message}"
                    )
            }
        }
    }

    // Detect if a message contains a recipe
    fun containsRecipe(content: String): Boolean {
        val lower = content.lowercase()
        return (lower.contains("ingredient") || lower.contains("ingrediente")) &&
                (lower.contains("step") || lower.contains("instruction") ||
                        lower.contains("preparation") || lower.contains("paso"))
    }

    // Parse the AI message directly — no second Gemini call needed
    fun saveRecipeFromMessage(content: String) {
        val uid = auth.currentUser?.uid ?: return
        _isSavingRecipe.value = true

        viewModelScope.launch {
            try {
                val lines = content.lines()

                // --- Extract title ---
                var title = "CookAI Recipe"
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("**") && trimmed.endsWith("**")) {
                        title = trimmed.removePrefix("**").removeSuffix("**").trim()
                        break
                    }
                }

                // --- Extract ingredients ---
                val ingredients = mutableListOf<Ingredient>()
                var inIngredients = false
                var inSteps = false

                for (line in lines) {
                    val lower = line.lowercase().trim()

                    when {
                        lower.startsWith("ingredient") -> {
                            inIngredients = true
                            inSteps = false
                        }
                        lower.startsWith("step") || lower.startsWith("instruction") ||
                                lower.startsWith("preparation") || lower.matches(Regex("^\\d+\\..*")) -> {
                            inSteps = true
                            inIngredients = false
                        }
                        inIngredients && line.trim().isNotBlank() -> {
                            val clean = line.trim()
                                .removePrefix("-").removePrefix("•")
                                .removePrefix("*").trim()
                            if (clean.isNotBlank()) {
                                // Try to split name and amount
                                // e.g. "2 Ahi tuna loins (sashimi grade)"
                                // amount = first word(s) that look like quantity
                                val amountRegex = Regex("^([\\d/½¼¾]+\\s*(?:cup|tbsp|tsp|g|kg|ml|l|oz|lb|units?)?s?\\.?)")
                                val amountMatch = amountRegex.find(clean)
                                val amount = amountMatch?.value?.trim() ?: ""
                                val name = if (amount.isNotBlank()) {
                                    clean.removePrefix(amount).trim()
                                } else clean

                                ingredients.add(Ingredient(name = name, amount = amount))
                            }
                        }
                    }
                }

                // --- Extract steps ---
                val steps = mutableListOf<RecipeStep>()
                var stepOrder = 1
                var inStepsSection = false

                for (line in lines) {
                    val trimmed = line.trim()
                    val lower = trimmed.lowercase()

                    // Detect "Steps:" header
                    if (lower == "steps:" || lower == "steps" ||
                        lower == "instructions:" || lower == "instructions") {
                        inStepsSection = true
                        continue
                    }

                    // Stop if we hit another section
                    if (inStepsSection && (lower == "ingredients:" || lower == "ingredients")) {
                        inStepsSection = false
                        continue
                    }

                    if (inStepsSection && trimmed.matches(Regex("^\\d+\\..*"))) {
                        val description = trimmed
                            .replaceFirst(Regex("^\\d+\\.\\s*"), "")
                            .trim()
                        if (description.isNotBlank()) {
                            steps.add(RecipeStep(order = stepOrder++, description = description))
                        }
                    }
                }

                val recipe = Recipe(
                    title = title,
                    description = content.lines()
                        .firstOrNull { it.trim().isNotBlank() && it.trim() != title }
                        ?.trim()?.take(200) ?: "",
                    duration = "30 min",
                    servings = 2,
                    category = "Main",
                    ingredients = ingredients.ifEmpty {
                        listOf(Ingredient(name = "See full instructions", amount = ""))
                    },
                    steps = steps.ifEmpty {
                        listOf(RecipeStep(order = 1, description = content.take(500)))
                    },
                    authorId = uid,
                    authorName = "CookAI",
                    isPublic = false,
                    source = "cookai"
                )

                val result = recipeRepository.createRecipeForUser(recipe, uid)
                if (result.isSuccess) {
                    _recipeSaved.value = true
                }

            } catch (e: Exception) {
                android.util.Log.e("CookAI", "Parse failed: ${e.message}")
                // Fallback
                val recipe = Recipe(
                    title = "CookAI Recipe",
                    description = content.take(200),
                    duration = "30 min",
                    servings = 2,
                    category = "Main",
                    ingredients = listOf(Ingredient("See full instructions", "")),
                    steps = listOf(RecipeStep(1, content.take(500))),
                    authorId = uid,
                    authorName = "CookAI",
                    isPublic = false,
                    source = "cookai"
                )
                recipeRepository.createRecipeForUser(recipe, uid)
                _recipeSaved.value = true
            } finally {
                _isSavingRecipe.value = false
            }
        }
    }

    fun clearRecipeSaved() {
        _recipeSaved.value = false
    }
}

sealed class CookAIUiState {
    object Initial : CookAIUiState()
    object Loading : CookAIUiState()
    object Ready : CookAIUiState()
    data class Error(val message: String) : CookAIUiState()
}