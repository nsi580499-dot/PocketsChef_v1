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
                        // Note: Greeting is constructed here but ideally passed as a resource ID to UI
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

    fun containsRecipe(content: String): Boolean {
        val lower = content.lowercase()
        return (lower.contains("ingredient") || lower.contains("ingrediente")) &&
                (lower.contains("step") || lower.contains("instruction") ||
                        lower.contains("preparation") || lower.contains("paso"))
    }

    private fun extractTitle(content: String): String {
        val lines = content.lines()
        for (line in lines) {
            val clean = line
                .removePrefix("# ").removePrefix("## ").removePrefix("### ")
                .removePrefix("**").removeSuffix("**")
                .trim()
            if (clean.isNotBlank() && clean.length < 60) return clean
        }
        return "CookAI Recipe"
    }

    fun saveRecipeFromMessage(content: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _isSavingRecipe.value = true
            val title = extractTitle(content)

            val ingredients = content.lines()
                .filter { line ->
                    val l = line.trim().lowercase()
                    (l.startsWith("-") || l.startsWith("•") || l.startsWith("*")) &&
                            !l.contains("step") && !l.contains("instruction")
                }
                .map { line ->
                    val clean = line.trim()
                        .removePrefix("-").removePrefix("•").removePrefix("*").trim()
                    Ingredient(name = clean, amount = "")
                }
                .filter { it.name.isNotBlank() }
                .take(20)

            val steps = content.lines()
                .mapIndexedNotNull { index, line ->
                    val l = line.trim()
                    val isStep = l.matches(Regex("^\\d+\\..*")) ||
                            l.lowercase().startsWith("step")
                    if (isStep) {
                        val desc = l.replaceFirst(Regex("^\\d+\\.\\s*"), "")
                            .replaceFirst(Regex("(?i)^step\\s*\\d*:?\\s*"), "")
                            .trim()
                        if (desc.isNotBlank()) RecipeStep(order = index + 1, description = desc)
                        else null
                    } else null
                }
                .take(15)

            val recipe = Recipe(
                title = title,
                description = content.take(200),
                duration = "30 min",
                servings = 2,
                category = "Main",
                ingredients = ingredients.ifEmpty {
                    listOf(Ingredient(name = "See instructions", amount = ""))
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
            _isSavingRecipe.value = false
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
