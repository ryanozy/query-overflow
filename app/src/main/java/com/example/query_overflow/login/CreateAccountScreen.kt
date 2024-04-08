package com.example.query_overflow.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.query_overflow.Screen
import com.example.query_overflow.firebaseHelper


@Composable
fun CreateAccountScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showPassword = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf("") }
    val localFocusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Heading
        Text(
            text = "Account Creation",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = modifier.padding(vertical = 100.dp)
        )

        // Email
        TextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier
                .padding(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
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

        // Password
        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
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

        // Login Button
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF),
            ),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.8f),
            // Disabled the button only when the fill is filled in then enable it
            enabled = email.value.isNotEmpty() && password.value.isNotEmpty(),
            onClick = {
                firebaseHelper.createUser(email.value, password.value,
                    {
                        navController.navigate(Screen.Dashboard.route)
                    },
                    {
                        errorMsg.value = it
                    }
                )
            },
        ) {
            Text(text = "Create Account")
        }

        // Back to Account Type Selection
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Blue
            ),
            modifier = modifier
                .padding(horizontal = 35.dp) // To make the forget Password align with the text field
                .align(Alignment.CenterHorizontally),
            onClick = {
                navController.navigate(Screen.ChooseAccountType.route)
            }
        ) {
            Text(
                text = "Back to Account Type Selection"
            )
        }

        if (errorMsg.value.isNotEmpty()){
            //TODO change to login fail but dont say why (Security reasons)
            Text(
                text = errorMsg.value,
                modifier = modifier
                    .padding(horizontal = 35.dp)
                    .align(Alignment.CenterHorizontally),
            )
        }
    }
}