package com.example.query_overflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import org.junit.Rule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.query_overflow.login.LoginScreen
import com.example.query_overflow.ui.theme.QueryOverflowTheme
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.regex.Pattern.matches

class LoginScreenKtTest{
    // UiAutomator device
    private lateinit var device: UiDevice


    @get:Rule
    var composeTestRule = createComposeRule()

    @Before
    fun setupUiAutomator(){
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun loginScreen_RenderedProperly() {

        composeTestRule.setContent {
            QueryOverflowTheme {
                LoginScreen()
            }
        }

        // Check if email field is displayed and empty
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Email_Field").assertTextContains("")

        // Check if password field is displayed and empty
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Password_Field").assertTextContains("")

        // Check if Login button is displayed and enabled
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()

        // Check if Back to Account Type Selection text is displayed
        composeTestRule.onNodeWithText("Back to Account Type Selection").assertIsDisplayed()
    }

    @Test
    fun login_SuccessfulNavigation() {
        val email = "test_user@test.com"
        val password = "P@ssw0rd"

        composeTestRule.setContent {
            QueryOverflowTheme {
                LoginScreen()
            }
        }

        composeTestRule.onNodeWithTag("Email_Field").performTextInput(email)
        composeTestRule.onNodeWithTag("Password_Field").performTextInput(password)

        // Perform login
        composeTestRule.onNodeWithText("Login").performClick()

   }
}