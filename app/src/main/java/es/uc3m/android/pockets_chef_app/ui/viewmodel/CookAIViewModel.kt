package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.ChatMessage
import es.uc3m.android.pockets_chef_app.data.repository.ChatRepository
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CookAIViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow<CookAIUiState>(CookAIUiState.Initial)
    val uiState = _uiState.asStateFlow()

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
                    // Mensaje de bienvenida inicial
                    if (messages.isEmpty()) {
                        messages.add(ChatMessage(role = "model", content = "¡Hola ${user.displayName}! Soy CookAI. ¿En qué puedo ayudarte hoy?"))
                    }
                } else {
                    _uiState.value = CookAIUiState.Error("No se pudo cargar el perfil del usuario")
                }
            }.onFailure {
                _uiState.value = CookAIUiState.Error(it.message ?: "Error desconocido")
            }
        }
    }

    fun sendMessage(text: String) {
        val model = generativeModel ?: return
        if (text.isBlank()) return

        // 1. Añadir mensaje del usuario a la UI
        messages.add(ChatMessage(role = "user", content = text))
        
        // 2. Preparar respuesta de la IA (vacía al inicio para el streaming)
        val aiMessageIndex = messages.size
        messages.add(ChatMessage(role = "model", content = ""))

        viewModelScope.launch {
            try {
                // Convertir historial a formato Gemini
                val history = messages.dropLast(1).map { msg ->
                    content(msg.role) { text(msg.content) }
                }

                var fullResponse = ""
                chatRepository.sendMessageStream(model, history, text).collect { chunk ->
                    fullResponse += chunk
                    messages[aiMessageIndex] = messages[aiMessageIndex].copy(content = fullResponse)
                }
            } catch (e: Exception) {
                messages[aiMessageIndex] = messages[aiMessageIndex].copy(content = "Lo siento, hubo un error: ${e.message}")
            }
        }
    }
}

sealed class CookAIUiState {
    object Initial : CookAIUiState()
    object Loading : CookAIUiState()
    object Ready : CookAIUiState()
    data class Error(val message: String) : CookAIUiState()
}
