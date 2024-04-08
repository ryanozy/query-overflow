package com.example.query_overflow

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.query_overflow.chatbot.ChatbotRoomSelectionScreen
import com.example.query_overflow.chatbot.ChatbotRoomSelectionViewModel
import com.example.query_overflow.chatbot.ChatbotRoomSelectionViewModelFactory
import com.example.query_overflow.chatbot.ChatbotViewModel
import com.example.query_overflow.chatbot.ChatbotViewModelFactory
import com.example.query_overflow.chatbot.ChatbotViewScreen
import com.example.query_overflow.dashboard.AdminDashboardScreen
import com.example.query_overflow.dashboard.DashboardScreen
import com.example.query_overflow.dashboard.LeaderboardScreen
import com.example.query_overflow.login.AdminLoginScreen
import com.example.query_overflow.login.ChooseAccountTypeScreen
import com.example.query_overflow.login.CreateAccountScreen
import com.example.query_overflow.login.LoginScreen
import com.example.query_overflow.profileScreen.ProfileScreen
import com.example.query_overflow.questionsScreens.QuestionCreateScreen
import com.example.query_overflow.questionsScreens.QuestionEditScreen
import com.example.query_overflow.questionsScreens.QuestionViewScreen
import com.example.query_overflow.questionsScreens.SpecificQuestionViewScreen

sealed class Screen(val route: String){
    object ChooseAccountType: Screen(route = "choose_account_type")
    object Login: Screen(route = "login")
    object Dashboard: Screen(route = "dashboard")
    object QuestionView: Screen(route = "question_view")
    object SpecificQuestionView: Screen(route = "specific_question_view")
    object QuestionCreate: Screen(route = "question_create")
    object QuestionEdit: Screen(route = "question_edit")
    object ChatbotRoomSelection: Screen(route = "chatbotroom_selection")
    object ChatbotView: Screen(route = "chatbot_view")
    object Leaderboard: Screen(route = "leaderboard_view")
    object Profile: Screen(route = "profile_view")
    object AdminLogin: Screen(route = "admin_login")
    object Admin: Screen(route = "admin_view")
    object CreateAccount: Screen(route = "create_account")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModelFactory: ChatbotViewModelFactory,
    roomViewModelFactory: ChatbotRoomSelectionViewModelFactory
) {

        NavHost(
            navController = navController,
            startDestination = Screen.ChooseAccountType.route
        ){
            composable(Screen.ChooseAccountType.route){
                ChooseAccountTypeScreen(navController = navController)
            }
            composable(Screen.Login.route){
                LoginScreen(navController = navController)
            }
            composable(Screen.Dashboard.route){
                DashboardScreen(navController = navController)
            }
            composable(Screen.QuestionView.route+ "/{passedString}") { navBackStackEntry ->
                val passedString = navBackStackEntry.arguments?.getString("passedString")?.trim() ?: ""
                QuestionViewScreen(navController = navController, passedString = passedString)
            }
            composable(Screen.SpecificQuestionView.route+ "/{passedString}") { navBackStackEntry ->
                val passedString = navBackStackEntry.arguments?.getString("passedString") ?: ""
                SpecificQuestionViewScreen(
                    navController = navController,
                    passedString = passedString
                )
            }
            composable(Screen.QuestionCreate.route){
                QuestionCreateScreen(navController = navController)
            }
            composable(Screen.QuestionEdit.route+ "/{passedString}") { navBackStackEntry ->
                val passedString = navBackStackEntry.arguments?.getString("passedString") ?: ""
                QuestionEditScreen(navController = navController, passedString = passedString)
            }
            composable(Screen.ChatbotRoomSelection.route) {
                val viewModel: ChatbotRoomSelectionViewModel = viewModel(factory = roomViewModelFactory)
                ChatbotRoomSelectionScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.ChatbotView.route+ "/{passedInt}") { navBackStackEntry ->
                val viewModel: ChatbotViewModel = viewModel(factory = viewModelFactory)
                val passedString = navBackStackEntry.arguments?.getString("passedInt") ?: ""
                ChatbotViewScreen(navController = navController, viewModel = viewModel, passedString = passedString)
            }
            composable(Screen.Leaderboard.route){
                LeaderboardScreen(navController = navController)
            }
            composable(Screen.Profile.route){
                ProfileScreen(navController = navController)
            }
            composable(Screen.AdminLogin.route){
                AdminLoginScreen(navController = navController)
            }
            composable(Screen.Admin.route){
                AdminDashboardScreen(navController = navController)
            }
            composable(Screen.CreateAccount.route){
                CreateAccountScreen(navController = navController)
            }
        }
}