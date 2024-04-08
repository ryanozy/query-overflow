package com.example.query_overflow.questionsScreens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.query_overflow.Post
import com.example.query_overflow.Screen
import com.example.query_overflow.firebaseHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun handleConfirmation(
    subject: String,
    description: String,
    tag: List<String>,
    uris: List<Uri>,
    module: String
) {
    val fileName: MutableList<String> = mutableListOf()
    for (uri in uris) {
        uri.lastPathSegment?.let { fileName.add(it) }
    }

    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("hh:mma dd/MM/uu")

    val post = Post(
        userID = firebaseHelper.getUserID(),
        module = module,
        subject = subject,
        dateTime = currentDateTime.format(formatter),
        description = description,
        tags = tag,
        comments = emptyList(),
        likes = emptyList(),
        attachmentsFileName = fileName
    )

    val key = firebaseHelper.uploadPost(post)
    if (key != null) {
        for (uri in uris) {
            firebaseHelper.uploadPhoto(uri, key)
        }
    }

}

@Composable
fun TagList(tags: List<String>, onTagRemoved: (String) -> Unit) {
    if (tags.isNotEmpty()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(tags) { tag ->
                TagItem(tag = tag, onTagRemoved = onTagRemoved)
            }
        }
    }
}

@Composable
private fun TagItem(tag: String, onTagRemoved: (String) -> Unit) {
    Box(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .background(Color.Black, RoundedCornerShape(20.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Text(
                text = tag,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontStyle = FontStyle.Italic,
            )
            IconButton(
                onClick = { onTagRemoved(tag) },
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Remove tag",
                    tint = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("Recycle")
@Composable
fun QuestionCreateScreen(
    navController: NavController = rememberNavController(),
) {
    // Declare variables used in this function
    val questionSubject = remember { mutableStateOf("") }
    val questionDescription = remember { mutableStateOf("") }
    val questionTag = remember { mutableStateOf("") }
    val questionTagList = remember { mutableStateOf(listOf<String>()) }
    val selectedUris = remember { mutableStateListOf<Uri>() }
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val itemList = listOf("INF2007 Mobile App Development", "CSC3105 Data Analytics", "CSC2106 IoT")
    var isExpanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(itemList[0]) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current

    // Handle Activity for file selected
    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    if (uri in selectedUris) {
                        // Pop up saying uri selected
                        Log.d("File", "File already selected")
                    } else {
                        selectedUris.add(uri)
                    }
                }
            } else {
                Log.d("File", "No file selected")
            }
        }

    // Function to show Toast
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun handleAddTag(){
        if (questionTag.value.isEmpty()) {
            showToast("Please enter a tag")
        } else if (questionTagList.value.size >= 3) {
            showToast("You can only add 3 tags")
        } else if (questionTagList.value.contains(questionTag.value)) {
            showToast("Tag already added")
        } else {
            questionTagList.value = questionTagList.value.toMutableList().apply { add(questionTag.value) }
        }
        questionTag.value = ""
    }

    // Confirm posting alert box
    if (showDialog.value) {
        AlertDialog(containerColor = Color(0xFFFFFFFF),
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    onClick = {
                        // Handle confirmation action
                        showDialog.value = false // Close Dialog
                        handleConfirmation(
                            questionSubject.value,
                            questionDescription.value,
                            questionTagList.value,
                            selectedUris,
                            selectedText
                        )
                        // Navigate back to dashboard
                        navController.navigate(Screen.Dashboard.route)
                    }) {
                    Text(text = "Yes")
                }
            }, dismissButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0F0F0)
                    ),
                    onClick = {
                        // Handle confirmation action
                        showDialog.value = false // Close dialog
                    }) {
                    Text(
                        text = "Cancel",
                        color = Color.Black
                    )
                }
            }, title = {
                Text(text = "Are You Sure?")
            }, text = {
                Text(text = "You are about to post a new question are you sure?")
            })

    }

    // Start of Create Question Screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back Arrow",
                modifier = Modifier
                    .padding(16.dp)
                    .size(30.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "New Question",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .padding(end = 50.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject
        Text(
            "Subject",
            style = MaterialTheme.typography.labelLarge,
        )

        Spacer(modifier = Modifier.height(0.dp))

        // Subject Field
        TextField(
            value = questionSubject.value, // Subject value
            onValueChange = { questionSubject.value = it },
            label = null,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, shape = RoundedCornerShape(10.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                focusedLabelColor = Color(0xFF000000),
                focusedTextColor = Color(0xFF000000),
                focusedIndicatorColor = Color(0x00000000),
                unfocusedContainerColor = Color.Transparent,
                unfocusedLabelColor = Color(0xFF000000),
                unfocusedIndicatorColor = Color(0x00000000),
                ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    localFocusManager.moveFocus(
                        FocusDirection.Next
                    )
                }
            )
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        // Tag Label
        Text(
            "Tag",
            style = MaterialTheme.typography.labelLarge,
        )

        // Listing the tags added
        TagList(
            tags = questionTagList.value,
            onTagRemoved = { removedTag ->
                questionTagList.value = questionTagList.value.filter { it != removedTag }
            }
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        // Tag Field + Add Tag Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            // Tag Field
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        1.dp,
                        Color.LightGray,
                        shape = RoundedCornerShape(10.dp)
                    ), // Added weight modifier
                value = questionTag.value, // Subject value
                onValueChange = {
                    questionTag.value = it
                },
                label = null,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    focusedLabelColor = Color(0xFF000000),
                    focusedTextColor = Color(0xFF000000),
                    focusedIndicatorColor = Color(0x00000000),
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedLabelColor = Color(0xFF000000),
                    unfocusedIndicatorColor = Color(0x00000000),
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        localFocusManager.moveFocus(
                            FocusDirection.Next
                        )
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp)) // Added Spacer for separation
            // Tag button
            Button(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(9.dp), // Added weight modifier
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF000000)
                ),
                shape = RoundedCornerShape(18.dp),
                onClick = {
                    // Get length of tag list
                    handleAddTag()
                },
            ) {
                Text(text = "Add tag")
            }
        }

        // Module Label
        Text(
            "Modules",
            style = MaterialTheme.typography.labelLarge,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 8.dp, 0.dp, 8.dp)
        ) {
            // Drop Down Menu
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = {
                    isExpanded = !isExpanded
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, shape = RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        focusedLabelColor = Color(0xFF000000),
                        focusedTextColor = Color(0xFF000000),
                        focusedIndicatorColor = Color(0x00000000),
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedLabelColor = Color(0xFF000000),
                        unfocusedIndicatorColor = Color(0x00000000),
                    )
                )

                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemList.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            onClick = {
                                selectedText = item
                                isExpanded = false
                                Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Text(
            "Description",
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = questionDescription.value, // Subject value
            onValueChange = { questionDescription.value = it },
            label = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .border(1.dp, Color.LightGray, shape = RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                focusedLabelColor = Color(0xFF000000),
                focusedTextColor = Color(0xFF000000),
                focusedIndicatorColor = Color(0x00000000),
                unfocusedContainerColor = Color.Transparent,
                unfocusedLabelColor = Color(0xFF000000),
                unfocusedIndicatorColor = Color(0x00000000),
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            )
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
                .width(30.dp)
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "Attachments",
                style = MaterialTheme.typography.labelLarge,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Attachment Button
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                onClick = {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*" // Set MIME type to filter the file types if needed
                    }
                    filePickerLauncher.launch(intent)

                }, modifier = Modifier.padding(start = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add, contentDescription = "Attach File"
                )
                Spacer(
                    modifier = Modifier
                        .width(8.dp)
                )
                Text(text = "Attach File")
            }
        }
        LazyRow {
            items(selectedUris.size) { index ->
                val uri = selectedUris[index]
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val imageBitmap = BitmapFactory.decodeStream(inputStream)
                Surface {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageBitmap),
                            contentDescription = "File Preview",
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp)
                                .padding(8.dp),
                            contentScale = ContentScale.Crop
                        )
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Delete",
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .clickable {
                                    selectedUris.remove(uri)
                                },
                            tint = Color.Gray,
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier
                .height(8.dp)
                .width(30.dp)
        )

        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            ),
            onClick = {
                if (questionSubject.value.isNotEmpty() && questionDescription.value.isNotEmpty() && questionTagList.value.isNotEmpty() && questionDescription.value.isNotEmpty()) {
                    showDialog.value = true
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT)
                        .show()
                }
            }, modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Submit")
        }
    }
}
