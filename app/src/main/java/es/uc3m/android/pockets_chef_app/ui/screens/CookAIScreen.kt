package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@Composable
fun CookAIScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    val greeting = stringResource(R.string.cookai_greeting)
    val aiResponse = stringResource(R.string.cookai_ai_response)

    val messages = remember {
        mutableStateListOf(
            ChatMessage(greeting, false)
        )
    }

    CookAIScreenContent(
        messages = messages,
        inputText = inputText,
        onInputTextChange = { inputText = it },
        onSendMessage = {
            if (inputText.isNotBlank()) {
                messages.add(ChatMessage(inputText, true))
                inputText = ""
                // Fake AI response
                messages.add(ChatMessage(aiResponse, false))
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message)
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
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onSendMessage,
                        enabled = inputText.isNotBlank(),
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
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val containerColor = if (message.isUser) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer
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
                bottomStart = if (message.isUser) 16.dp else 0.dp,
                bottomEnd = if (message.isUser) 0.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
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
                ChatMessage("Hello! I'm CookAI. How can I help you today?", false),
                ChatMessage("I want to make a carbonara.", true),
                ChatMessage("Great choice! Do you have eggs and guanciale?", false)
            ),
            inputText = "Yes, I do.",
            onInputTextChange = {},
            onSendMessage = {},
            onBackClick = {}
        )
    }
}