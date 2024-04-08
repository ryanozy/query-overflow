package com.example.query_overflow.chatbot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.query_overflow.Screen
import kotlinx.coroutines.launch

@Composable
fun ChatBubbleItem(
    chatMessage: ChatMessage,
    selectedLanguage: String,
    viewModel: ChatbotViewModel = viewModel()
) {

    val context = LocalContext.current
    val isModelMessage = chatMessage.participant == Participant.MODEL ||
            chatMessage.participant == Participant.ERROR

    val backgroundColor = when (chatMessage.participant) {
        Participant.MODEL -> MaterialTheme.colorScheme.primaryContainer
        Participant.USER -> MaterialTheme.colorScheme.tertiaryContainer
        Participant.ERROR -> MaterialTheme.colorScheme.errorContainer
    }

    val bubbleShape = if (isModelMessage) {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    }

    val horizontalAlignment = if (isModelMessage) {
        Alignment.Start
    } else {
        Alignment.End
    }

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = chatMessage.participant.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row {
            if (chatMessage.isPending) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(all = 8.dp)
                )
            }
            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = bubbleShape,
                    modifier = Modifier.widthIn(0.dp, maxWidth * 0.9f)
                ) {
                    Row {
                        Text(
                            text = chatMessage.text,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Row {
                        Icon(
                            Icons.Filled.Translate,
                            contentDescription = "Translate",
                            modifier = Modifier
                                .padding(16.dp, 16.dp, 2.dp, 16.dp)
                                .size(24.dp)
                                .clickable {
                                    // Translate the message
                                    if (selectedLanguage == "EN") {
                                        viewModel.translateText(chatMessage.text, "en") { translatedText ->
                                            chatMessage.text = translatedText
                                        }
                                    } else {
                                        viewModel.translateText(chatMessage.text, "zh") { translatedText ->
                                            chatMessage.text = translatedText
                                        }
                                    }
                                }
                        )
                        Icon(
                            Icons.Filled.CopyAll,
                            contentDescription = "Copy",
                            modifier = Modifier
                                .padding(2.dp, 16.dp, 2.dp, 16.dp)
                                .size(24.dp)
                                .clickable {
                                    // Copy the message
                                    viewModel.copyTextToClipboard(chatMessage.text, context)
                                }
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            modifier = Modifier
                                .padding(2.dp, 16.dp, 16.dp, 16.dp)
                                .size(24.dp)
                                .clickable {
                                    // Speak the message
                                    viewModel.speakText(chatMessage.text, context, selectedLanguage)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    resetScroll: () -> Unit = {},
    viewModel: ChatbotViewModel = viewModel()
) {
    var userMessage by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = userMessage,
                label = null,
                onValueChange = { userMessage = it },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (userMessage.isNotBlank()) {
                            onSendMessage(userMessage)
                            userMessage = ""
                            resetScroll()
                            keyboardController?.hide()
                        }
                    }),
                shape = RoundedCornerShape(25.dp),
                placeholder = { Text(text = "Ask Me Anything") },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.85f)
            )
            IconButton(
                onClick = {
                    // Voice Input
                    viewModel.startVoiceInput(context) { voiceInputText ->
                        userMessage = voiceInputText
                    }
                },
                modifier = Modifier
                    .padding(start = 2.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.15f)
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    modifier = Modifier
                )
            }
            IconButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        onSendMessage(userMessage)
                        userMessage = ""
                        resetScroll()
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .padding(start = 2.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.15f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    modifier = Modifier
                )
            }
        }
    }
}
@Composable
fun ChatbotViewScreen(
    navController: NavController = rememberNavController(),
    viewModel: ChatbotViewModel = viewModel(),
    passedString: String
) {

    val chatUiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val chatRoomName = remember { mutableStateOf("") }
    val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
    var selectedText by rememberSaveable { mutableStateOf("EN") }
    val context = LocalContext.current
    val ttsStatus by viewModel.textToSpeechEnabled.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.loadChatHistory(passedString.toInt())
        coroutineScope.launch {
            chatRoomName.value = viewModel.getChatRoomName(passedString.toInt())
            // Do something with the roomName
        }
    }

    Scaffold(
        bottomBar = {
            MessageInput(
                onSendMessage = { inputText ->
                    viewModel.sendMessage(inputText, passedString.toInt(), context, ttsStatus)
                },
                resetScroll = {
                    coroutineScope.launch {
                        listState.scrollToItem(0)
                    }
                },
                viewModel = viewModel
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Messages List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically, // Align items vertically centered
                horizontalArrangement = Arrangement.SpaceBetween // Space between items
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Arrow Back Icon",
                    modifier = Modifier
                        .padding(16.dp)
                        .size(30.dp)
                        .clickable(
                            onClick = {
                                navController.navigate(Screen.ChatbotRoomSelection.route)
                            }
                        )
                )


                Text(
                    chatRoomName.value,
                    style = MaterialTheme.typography.titleMedium,
                    //color = Color.White,
                    modifier = Modifier
                        .padding(8.dp, 16.dp)
                )

                Button(
                    onClick = {
                        selectedText = if (selectedText == "EN") {
                            "ZH"
                        } else {
                            "EN"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000)
                    ),
                ) {
                    Icon(
                        Icons.Rounded.Language,
                        contentDescription = "Language",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 8.dp)
                    )
                    Text(text = selectedText)
                }
            }

            BoxWithConstraints {
                LazyColumn(
                    reverseLayout = true,
                    state = listState
                ) {
                    items(chatUiState.messages.reversed()) { message ->
                        ChatBubbleItem(message, selectedText, viewModel)
                    }
                }

                if (visibleItemsInfo.isNotEmpty() && visibleItemsInfo.first().index > 0) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.scrollToItem(0)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = Color(0xFF000000),
                    ) {
                        Icon(
                            Icons.Rounded.Download,
                            contentDescription = "Scroll to bottom",
                            modifier = Modifier
                                .size(24.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
