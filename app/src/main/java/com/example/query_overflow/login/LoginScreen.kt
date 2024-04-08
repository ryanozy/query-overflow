package com.example.query_overflow.login

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.query_overflow.Screen
import com.example.query_overflow.firebaseHelper

@Composable
fun LoginScreen(
    navController: NavController = rememberNavController(),
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showPassword = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf("") }
    val localFocusManager = LocalFocusManager.current
    val isButtonEnabled = email.value.isNotEmpty() && password.value.isNotEmpty()

    if (firebaseHelper.isAuth()) {
        navController.navigate(Screen.Dashboard.route)
    }

    // sparkle icon
    Column(
        modifier = Modifier
            .padding(end = 30.dp, top = 100.dp),
        horizontalAlignment = AbsoluteAlignment.Right
    ) {
        Icon(
            Icons.Filled.AutoAwesome,
            contentDescription = "Sparkle",
            tint = Color.Black,
            modifier = Modifier
                .size(38.dp)
            //.padding(50.dp)
        )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(bottom = 10.dp)
    ) {
        // Heading
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 40.dp, bottom = 30.dp)
        ) {
            Text(
                text = "Welcome to \n" +
                        "Query-Overflow",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(1f)
            )
        }

        // Email
        TextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email", fontSize = (15.sp)) },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .border(
                    1.dp, Color.LightGray,
                    shape = RoundedCornerShape(10.dp)
                )
                .testTag("Email_Field"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                focusedLabelColor = Color(0xFF000000),
                focusedTextColor = Color(0xFF000000),
                focusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                unfocusedLabelColor = Color(0xFF000000),
                unfocusedIndicatorColor = Color.Transparent,
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email"
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    localFocusManager.moveFocus(
                        FocusDirection.Next
                    )
                }
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Password
        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password", fontSize = 15.sp) },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .border(
                    1.dp, Color.LightGray,
                    shape = RoundedCornerShape(10.dp)
                ).testTag("Password_Field"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                focusedLabelColor = Color(0xFF000000),
                focusedTextColor = Color(0xFF000000),
                focusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                unfocusedLabelColor = Color(0xFF000000),
                unfocusedIndicatorColor = Color.Transparent,
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password"
                )
            },
            visualTransformation = if (showPassword.value) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                if (showPassword.value) {
                    IconButton(onClick = { showPassword.value = false }) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = "Hide Password"
                        )
                    }
                } else {
                    IconButton(onClick = { showPassword.value = true }) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = "Show Password"
                        )

                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Login Button
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isButtonEnabled) Color.Black else Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            enabled = isButtonEnabled,
            onClick = {
                firebaseHelper.signIn(email.value, password.value,
                    onSuccess = {
                        navController.navigate(Screen.Dashboard.route)
                    },
                    onError = { errorMessage ->
                        errorMsg.value = errorMessage
                    })
            },
        ) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Back to Account Type Selection
        Text(
            text = "Back to Account Type Selection",
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(top = 15.dp)
                .clickable {
                    navController.navigate(Screen.ChooseAccountType.route)
                }
        )

        if (errorMsg.value.isNotEmpty()) {
            Text(text = "Error")
        }
    }
}
