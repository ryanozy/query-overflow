package com.example.query_overflow.chatbot

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.query_overflow.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun ChatbotRoomSelectionScreen(
    navController: NavController,
    viewModel: ChatbotRoomSelectionViewModel = viewModel()
) {

    val list by viewModel.groupFlow.collectAsState()
    val ttsEnabled by viewModel.textToSpeechEnabled.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Messages List
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, // Align items vertically centered
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Arrow Back Icon",
                modifier = Modifier
                    .padding(16.dp)
                    .size(30.dp)
                    .clickable(
                        onClick = {
                            navController.navigate(Screen.Dashboard.route)
                        }
                    )
            )


            Text(
                "Chatbot",
                style = MaterialTheme.typography.titleLarge,
                //color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
            )

            Text(
                "Text to Speech",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold
            )

            Switch(
                checked = ttsEnabled,
                onCheckedChange = {
                    viewModel.saveTTSPreference(it)
                }
            )

        }

        LazyColumn {
            items(list.size) { index ->
                ChatbotRoomSelectionItem(
                    id = list[index].id,
                    roomName = list[index].roomName,
                    onClick = {
                        navController.navigate(Screen.ChatbotView.route + "/${list[index].id}")
                    }
                )
                Spacer(modifier = Modifier.padding(8.dp))
            }
            if (list.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No rooms available",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                modifier = Modifier.padding(16.dp),
                //.shadow(20.dp, RoundedCornerShape(20.dp), ambientColor = Color.Gray)
                //.border(2.dp, Color(0xFF000000), RoundedCornerShape(20.dp)),
                containerColor = Color(0xFF000000),
                onClick = {
                    // Create a new room
                    val scope = CoroutineScope(Job() + Dispatchers.Main)
                    scope.launch {
                        viewModel.createRoom()
                    }
                },
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Question",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatbotRoomSelectionItem(
    id: Int,
    roomName: String,
    onClick: () -> Unit,
    viewModel: ChatbotRoomSelectionViewModel = viewModel()
) {

    val editRoomName = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            Icons.Rounded.ChatBubble,
            contentDescription = "Chat Room",
            modifier = Modifier
                .padding(16.dp)
                .size(30.dp)
        )
        Text(
            text = "$roomName (id: $id)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        if (editRoomName.value) {
            // Edit Room Name
            EditRoomNameDialog(
                onDismiss = { editRoomName.value = false },
                onConfirm = { newRoomName ->
                    val scope = CoroutineScope(Job() + Dispatchers.Main)
                    scope.launch {
                        viewModel.editRoomName(id, newRoomName)
                    }
                    editRoomName.value = false
                }
            )
        }
        Spacer(modifier = Modifier.weight(0.6f))
        Icon(
            Icons.Rounded.Edit,
            contentDescription = "Edit Room",
            modifier = Modifier
                .padding(16.dp, 16.dp, 2.dp, 16.dp)
                .size(30.dp)
                .clickable(onClick = {
                    //navController.navigate(Screen.ChatbotView.route + "/${item}")
                    editRoomName.value = true
                })
        )
        Icon(
            Icons.Rounded.Delete,
            contentDescription = "Delete Room",
            modifier = Modifier
                .padding(2.dp, 16.dp, 16.dp, 16.dp)
                .size(30.dp)
                .clickable(onClick = {

                    viewModel.deleteRoom(id)
                    //navController.navigate(Screen.ChatbotView.route + "/${item}")
                })
        )
    }
}

@Composable
fun EditRoomNameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val newRoomName = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Room Name") },
        text = {
            TextField(
                value = newRoomName.value,
                onValueChange = { newRoomName.value = it },
                label = { Text("New Room Name") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onConfirm(newRoomName.value)
                        onDismiss()
                    }
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(newRoomName.value)
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

