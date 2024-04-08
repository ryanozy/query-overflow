package com.example.query_overflow.questionsScreens

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.query_overflow.Comments
import com.example.query_overflow.Post
import com.example.query_overflow.Screen
import com.example.query_overflow.firebaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

fun handleUpdateConfirmation(
    subject: String,
    description: String,
    datetime: String,
    tag: List<String>,
    uris: List<Uri>,
    module: String,
    postKey: String,
    comments: List<Comments>,
    likes: List<String>,
    moduleChange: Boolean,
    attachmentsFileName: List<String>,
    deletedAttachments: List<String>
) {
    val fileName: MutableList<String> = mutableListOf()
    for (file in attachmentsFileName) {
        fileName.add(file)
    }
    for (uri in uris) {
        uri.lastPathSegment?.let { fileName.add(it) }
    }

    val post = Post(
        userID = firebaseHelper.getUserID(),
        module = module,
        subject = subject,
        dateTime = datetime,
        description = description,
        tags = tag,
        comments = comments,
        likes = likes,
        attachmentsFileName = fileName
    )

    val scope = CoroutineScope(Job() + Dispatchers.Main)
    scope.launch {
        val key = firebaseHelper.updatePost(post, postKey, moduleChange, deletedAttachments)
        if (key != null) {
            for (uri in uris) {
                firebaseHelper.uploadPhoto(uri, key)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    passedString: String
) {
    var originalModule by remember { mutableStateOf("") }
    var selectedPost by remember { mutableStateOf(Post()) }
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
    var deletedAttachments by remember { mutableStateOf<List<String>>(emptyList()) }
    var downloadedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(passedString) {
        firebaseHelper.getSpecificLivePost(passedString) { post ->
            selectedPost = post
            questionSubject.value = selectedPost.subject
            questionDescription.value = selectedPost.description
            questionTagList.value = selectedPost.tags
            originalModule = selectedPost.module

            if (selectedPost.attachmentsFileName.isEmpty()) {
                isLoading = false
            }

            firebaseHelper.getPhoto(post.key, post.attachmentsFileName) { files ->
                // Update the state with the downloaded files
                downloadedFiles = files
                isLoading = false
            }

            for (item in itemList) {
                if (item == selectedPost.module) {
                    selectedText = item
                }
            }
        }
    }

    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    if (uri in selectedUris) {
                        // TO DO
                        // Pop up saying uri selected
                        Log.d("File", "File alr selected")
                    } else {
                        selectedUris.add(uri)
                        Log.d("File", "File added $uri")
                    }

                }
            } else {
                Log.d("File", "No file selected")
            }
        }

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
                        showDialog.value = false
                        // You can call your function here if needed
                        handleUpdateConfirmation(
                            questionSubject.value,
                            questionDescription.value,
                            selectedPost.dateTime,
                            questionTagList.value,
                            selectedUris,
                            selectedText,
                            selectedPost.key,
                            selectedPost.comments,
                            selectedPost.likes,
                            originalModule == selectedText,
                            selectedPost.attachmentsFileName,
                            deletedAttachments
                        )
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
                        showDialog.value = false
                    }) {
                    Text(
                        text = "Cancel",
                        color = Color.Black
                    )
                }
            }, title = {
                Text(text = "Are You Sure?")
            }, text = {
                Text(text = "You are about to edit a subject are you sure?")
            })

    }


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
                "Edit Question",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .padding(end = 50.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }


        Text(
            "Subject",
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Question Fields
        TextField(
            value = questionSubject.value, // Subject value
            onValueChange = { questionSubject.value = it },
            label = null,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Tag",
            style = MaterialTheme.typography.labelLarge,
        )

        TagList(
            tags = questionTagList.value,
            onTagRemoved = { removedTag ->
                questionTagList.value = questionTagList.value.filter { it != removedTag }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                modifier = Modifier.weight(1f)
                    .border(1.dp, Color.LightGray, shape = RoundedCornerShape(10.dp)),
                 // Added weight modifier
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
            Button(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(9.dp), // Added weight modifier
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                onClick = {
                    // Get length of tag list
                    if (questionTag.value.isEmpty()) {
                        Toast.makeText(context, "Please enter a tag", Toast.LENGTH_SHORT).show()
                    } else if (questionTagList.value.size > 2) {
                        Toast.makeText(context, "You can only add 3 tags", Toast.LENGTH_SHORT).show()
                        questionTag.value = ""
                    } else if (questionTagList.value.contains(questionTag.value)) {
                        Toast.makeText(context, "Tag already added", Toast.LENGTH_SHORT).show()
                        questionTag.value = ""
                    } else {
                        questionTagList.value = questionTagList.value.toMutableList().apply { add(questionTag.value) }
                        questionTag.value = ""
                    }
                },
            ) {
                Text(text = "Add tag")
            }
        }
        Text(
            "Modules",
            style = MaterialTheme.typography.labelLarge,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 8.dp, 0.dp, 8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = {
                    isExpanded = !isExpanded
                },
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, Color.LightGray, shape = RoundedCornerShape(10.dp)),
                ) {
                TextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(8.dp))

        Row (horizontalArrangement = Arrangement.Center
            , verticalAlignment = Alignment.CenterVertically){

            Text(
                "Attachments",
                style = MaterialTheme.typography.labelLarge,
            )

            Spacer(modifier = modifier.width(10.dp))
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

                }, //modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Add
                    , contentDescription = "Attach File")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Attach File")
            }
        }

            Spacer(modifier = Modifier
                .height(8.dp)
                .width(30.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(all = 8.dp)
                )
            } else {
                LazyRow {
                    items(downloadedFiles.size) { index ->
                        val file = downloadedFiles[index]
                        val imageBitmap = BitmapFactory.decodeFile(file.path)
                        Surface {
                            Box{
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
                                            //firebaseHelper.removePhoto(selectedPost.key, file.name)
                                            Log.d("File", "File deleted ${file.name}")
                                            downloadedFiles = downloadedFiles.filter { it != file }
                                            for (attachment in selectedPost.attachmentsFileName) {
                                                if (attachment
                                                        .split("/")
                                                        .last()
                                                        .split(".")
                                                        .first() == file.name
                                                        .split(".")
                                                        .first()
                                                ) {
                                                    selectedPost.attachmentsFileName =
                                                        selectedPost.attachmentsFileName.filter { it != attachment }
                                                    deletedAttachments = deletedAttachments
                                                        .toMutableList()
                                                        .apply { add(attachment) }
                                                }
                                            }
                                            Log.d("File", "${selectedPost.attachmentsFileName}")
                                        },
                                    tint = Color.Gray,
                                )
                            }
                        }
                    }
                    items(selectedUris.size) { index ->
                        val uri = selectedUris[index]
                        val contentResolver = context.contentResolver
                        val inputStream = contentResolver.openInputStream(uri)
                        val imageBitmap = BitmapFactory.decodeStream(inputStream)
                        Surface {
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
                                    .align(alignment = Alignment.End)
                                    .clickable {
                                        // Remove the URI from the list
                                        selectedUris.remove(uri)
                                    },
                                tint = Color.Gray,
                            )
                        }
                    }
                }
            }

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
