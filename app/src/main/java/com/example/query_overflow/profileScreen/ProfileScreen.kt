package com.example.query_overflow.profileScreen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.query_overflow.Screen
import com.example.query_overflow.firebaseHelper
import java.io.File

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
) {
    // Using `= remember` and manually accessing `.value`
    var downloadedFile by remember { mutableStateOf(File("")) }
    val name = remember { mutableStateOf(firebaseHelper.getUserName()) }
    var selectedUri: Uri? by remember { mutableStateOf(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val context = LocalContext.current

    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    if (uri == selectedUri) {
                        Log.d("File", "File already selected")
                    } else {
                        selectedUri = uri
                        downloadedFile = File("")
                    }
                }
            } else {
                Log.d("File", "No file selected")
            }
        }

    if (!firebaseHelper.isAuth()) {
        navController.navigate(Screen.Login.route)
    }

    // Function to show Toast
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Function to handle Button Save
    fun handleSave() {
        val toastMsg: Boolean = firebaseHelper.updateUserInfo(name.value, selectedUri)
        keyboardController?.hide()
        val message = if (toastMsg) "Profile Change Success" else "Profile Change Failed"
        showToast(message)
    }

    // Function to handle Logout
    fun handleLogout() {
        firebaseHelper.signOut()
        navController.navigate(Screen.Login.route)
    }

    LaunchedEffect(Unit) {
        firebaseHelper.getProfilePhoto(firebaseHelper.getUserID()) { file ->
            downloadedFile = file
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        /** App Bar **/
        Row {
            // Back to dashboard button
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Dashboard",
                    modifier = Modifier
                        .size(35.dp)
                        .weight(0.3f)
                )
            }

            Spacer(Modifier.weight(0.3f))

            // Profile page heading
            Text(
                text = "Profile Page",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(30.dp))

        /** Image **/
        Box(
            contentAlignment = Alignment.BottomEnd,
        ) {
            // Check if there is a downloadFile is ready else show a Account Circle
            if (downloadedFile.toString() != "") {
                Image(
                    painter = rememberAsyncImagePainter(downloadedFile),
                    contentDescription = null, // Provide content description if needed
                    modifier = Modifier
                        .size(140.dp)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.Black, CircleShape),

                    contentScale = ContentScale.Crop,
                )
            } else if (selectedUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedUri),
                    contentDescription = null, // Provide content description if needed
                    modifier = Modifier
                        .size(140.dp)
                        .padding(5.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .border(1.dp, Color.Black, CircleShape)
                    .background(color = Color.Black, CircleShape)
                //.size(35.dp)
            ) {
                // Camera IconButton
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "image/*" // Set MIME type to filter the file types if needed
                        }
                        filePickerLauncher.launch(intent)
                    }, modifier = modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt, // Use the appropriate camera icon
                        contentDescription = "Change Profile Picture",
                        tint = Color.White,

                        )

                }
            }
        }

        // Spacing between text field and profile
        Spacer(Modifier.height(16.dp))

        // Name TextField
        OutlinedTextField(value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Name", fontSize = (15.sp)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                // Move focus to next TextField
                localFocusManager.moveFocus(
                    FocusDirection.Next
                )
            }))

        Spacer(Modifier.height(8.dp))

        /** Save button **/
        Button(
            onClick = {
                handleSave()
            }, colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            ), modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)

        ) {
            Spacer(modifier = Modifier.padding(5.dp))
            Text(text = "Save", fontSize = 16.sp)
        }

        Spacer(Modifier.height(10.dp))

        /** Logout button **/
        Button(
            onClick = {
                handleLogout()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(25.dp))

        ) {
            Spacer(modifier = Modifier.padding(5.dp))
            Text(text = "Logout", fontSize = 16.sp, color = Color.Black)

        }
    }
}