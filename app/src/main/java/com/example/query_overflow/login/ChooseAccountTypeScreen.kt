package com.example.query_overflow.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.query_overflow.R
import com.example.query_overflow.Screen
import com.example.query_overflow.ui.theme.QueryOverflowTheme

@Composable
fun ChooseAccountTypeScreen(modifier: Modifier = Modifier,
                            navController: NavController = rememberNavController(),
)
{

    Column (
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.star), // Assuming 'star' is the resource name
            contentDescription = "Star Image",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 130.dp)
                .height(280.dp)

        )

        // Login Text
        Text(
            text = "Query-Overflow",
            style = MaterialTheme.typography.headlineMedium,
            //modifier = modifier.padding(vertical = 160.dp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 30.dp)
                .height(50.dp)
        )

        // Student Button
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF000000)
            ),
            modifier = Modifier
                .padding(8.dp)
                .padding(top = 10.dp)
                .height(50.dp)
                .fillMaxWidth(0.8f),
            //.shadow(4.dp, RoundedCornerShape(22.dp), ambientColor = Color.Gray),



            onClick = {
                // Redirect to Login Page
                navController.navigate(Screen.Login.route)
            },

            ) {
            Text(text = "Student",
                style = TextStyle(fontSize = 16.sp)
            )
        }

        // Administrator Button
        OutlinedButton(
            onClick = {
                // Redirect to Admin Login Page
                navController.navigate(Screen.AdminLogin.route)
            },

            border = BorderStroke(1.dp, Color.Black),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.Black,
                containerColor = Color.White,
            ),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.8f)
                .height(50.dp)
            //.shadow(4.dp, RoundedCornerShape(22.dp), ambientColor = Color.LightGray),



        ) {
            Text(text = "Administrator",
                style = TextStyle(fontSize = 16.sp)
            )
        }

        // Register Button
        /*        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF)
            ),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.8f)
                .shadow(4.dp, RoundedCornerShape(22.dp), ambientColor = Color.Gray),

            onClick = {
                // Redirect to Create Account Page
                navController.navigate(Screen.CreateAccount.route)
            },

            ) {
            Text(text = "Register")
        }*/

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Black)) {
                    append("New User? ")
                }
                // Applying blue color and underline to "Signup"
                withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                    append("Signup")
                }
            },
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(0.8f)
                .wrapContentSize(Alignment.Center)
                .clickable {
                    // Redirect to Create Account Page
                    navController.navigate(Screen.CreateAccount.route)
                },
            fontSize = 14.sp
        )
    }
}

@Preview
@Composable
fun ChooseAccountTypeScreenPreview() {
    QueryOverflowTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ChooseAccountTypeScreen()
        }
    }
}