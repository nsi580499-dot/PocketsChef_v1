package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.data.model.ChatMessage
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.viewmodel.CookAIUiState
import es.uc3m.android.pockets_chef_app.ui.viewmodel.CookAIViewModel
import kotlinx.coroutines.launch

@Composable
fun CookAIScreen(
    navController: NavController,
    viewModel: CookAIViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when messages change
    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    CookAIScreenContent(
        messages = viewModel.messages,
        inputText = inputText,
        uiState = uiState,
        listState = listState,
        onInputTextChange = { inputText = it },
        onSendMessage = {
            if (inputText.isNotBlank()) {
                viewModel.sendMessage(inputText)
                inputText = ""
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookAIScreenContent(
    messages: List<ChatMessage>,
    inputText: String,
    uiState: CookAIUiState,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    onInputTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(bottomEnd = 32.dp, bottomStart = 32.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.cookai),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = stringResource(R.string.ready_text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cookai_back_desc),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when (uiState) {
                    is CookAIUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is CookAIUiState.Error -> {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(messages) { message ->
                                ChatBubble(message)
                            }
                        }
                    }
                }
            }

            // Input area
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputTextChange,
                        placeholder = { Text(stringResource(R.string.cookai_placeholder)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        enabled = uiState is CookAIUiState.Ready
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onSendMessage,
                        enabled = inputText.isNotBlank() && uiState is CookAIUiState.Ready,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.cookai_send_desc)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            color = containerColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 0.dp,
                bottomEnd = if (isUser) 0.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 15.sp,
                color = contentColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CookAIScreenPreview() {
    PocketsChefTheme {
        CookAIScreenContent(
            messages = listOf(
                ChatMessage(role = "model", content = "¡Hola! Soy CookAI. ¿En qué puedo ayudarte hoy?"),
                ChatMessage(role = "user", content = "Quiero hacer una carbonara."),
                ChatMessage(role = "model", content = "¡Excelente elección! ¿Tienes huevos y guanciale?")
            ),
            inputText = "Sí, los tengo.",
            uiState = CookAIUiState.Ready,
            onInputTextChange = {},
            onSendMessage = {},
            onBackClick = {}
        )
    }
}
