package com.example.query_overflow.dashboard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.query_overflow.Post
import com.example.query_overflow.R
import com.example.query_overflow.Screen
import com.example.query_overflow.firebaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AdminDashboardScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
) {

    val searchInput = remember { mutableStateOf("") }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var showMenu by remember {mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firebaseHelper.fetchAllPostFromFirebase { updatedPosts ->
            posts = updatedPosts
        }
    }

    if (!firebaseHelper.isAuth()) {
        navController.navigate(Screen.Login.route)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Row for Profile Button, Dashboard, Achievement Button
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {

            // Dashboard Heading
            Text(
                text = "Admin Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = modifier
                    .padding(top = 15.dp)
            )

            // More button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopEnd)
            )
            {
                IconButton(
                    onClick = { showMenu = !showMenu},

                    ){
                    Icon(
                        // painter = painterResource(id = R.drawable.baseline_density_medium_24),
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        modifier = modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .padding(top = 20.dp),
                    )
                }
                Surface (
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ){
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        offset = DpOffset(x=23.dp,44.dp)
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.Black)},
                            text = { Text(text = "Profile") },
                            onClick = { navController.navigate(Screen.Profile.route) }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(painter = painterResource(id = R.drawable.trophy_icon),
                                    contentDescription = "Achievement",
                                    tint = Color.Black)},
                            text = { Text(text = "Leaderboard") },
                            onClick = { navController.navigate(Screen.Leaderboard.route) }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                                    contentDescription = "Achievement",
                                    tint = Color.Black)},
                            text = { Text(text = "Logout") },
                            onClick = {
                                firebaseHelper.signOut()
                                navController.navigate(Screen.Login.route) }
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // Search Field
            TextField(
                value = searchInput.value,
                onValueChange = { searchInput.value = it },
                label = { Text("Search") },
                // Search Icon
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    focusedLabelColor = Color(0xFF000000),
                    focusedTextColor = Color(0xFF000000),
                    focusedIndicatorColor = Color(0x00000000),
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedLabelColor = Color(0xFF000000),
                    unfocusedIndicatorColor = Color(0x00000000),
                    unfocusedPlaceholderColor = Color(0xFF000000)
                ),
                modifier = modifier
                    .padding(vertical = 15.dp)
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchInput.value.isNotEmpty()) {
                            navController.navigate("${Screen.QuestionView.route}/${searchInput.value}")
                        } else {
                            navController.navigate("${Screen.QuestionView.route}/ ")
                        }
                    }
                )
            )

            // Space between the Chatbot button and the two buttons
            Spacer(modifier = modifier.height(5.dp))

            // Row for New Questions and Top Questions
            Row(
                modifier = modifier.fillMaxWidth(0.825f)
            ) {
                // Padding between the two button
                Spacer(modifier = modifier.width(10.dp). weight(0.5f))

                // Top Question Button
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(100.dp)
                        .weight(0.8f),
                    //.shadow(20.dp, RoundedCornerShape(20.dp), ambientColor = Color.Gray)
                    //.border(2.dp, Color(0xFF000000), RoundedCornerShape(20.dp)),
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black,
                    onClick = {
                        navController.navigate("${Screen.QuestionView.route}/ ")
                    },
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Top Questions",
                            tint = Color.Black,
                            modifier = modifier.size(50.dp)
                        )
                        Text(
                            text = "Top Questions",
                            style = TextStyle(fontSize = 13.sp)
                        )
                    }
                }
                Spacer(modifier = modifier.width(10.dp). weight(0.5f))
            }

            // Space between the two buttons and Recently Posted Questions
            Spacer(modifier = modifier.height(24.dp))

            // Button to the full list of questions
            Row(
                modifier = modifier
                    .fillMaxWidth(0.85f)
                    .padding(start = 10.dp, bottom = 2.dp)
            ) {
                Text(
                    text = "Recent Posts by the Community: ",
                    //style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    modifier = modifier.padding(bottom = 15.dp)
                )
            }

            Box(
                modifier = modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Box for the questions
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (posts.isNotEmpty()) {
                            LazyColumn {
                                items(
                                    items = posts.reversed(),
                                    key = { post -> post.key }) { post ->
                                    AdminPostItem(post)
                                }
                            }
                        } else {
                            Text(
                                text = "No questions to see here...", color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End),
            containerColor = Color(0xFF000000),
            onClick = {
                navController.navigate(Screen.QuestionCreate.route)
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


@Composable
private fun AdminPostItem(post: Post) {

    var userName by remember { mutableStateOf("") }
    var isLiked by remember { mutableStateOf(false) }
    var isUserPost by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firebaseHelper.getUserNameByID(post.userID) { name ->
            userName = name ?: "Anonymous"
        }
        firebaseHelper.isLikedByUser(post.key) { isLike ->
            isLiked = isLike
        }
        isUserPost = post.userID == firebaseHelper.getUserID()
    }

    Surface(
        modifier = Modifier
            .padding(5.dp)
            .shadow(5.dp, RoundedCornerShape(10.dp), ambientColor = Color.White)
            .border(2.dp, Color(0xFF000000), RoundedCornerShape(10.dp)),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = post.subject, style = TextStyle(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = "Tags: ${post.tags}", style = TextStyle(fontStyle = FontStyle.Italic))
            Spacer(modifier = Modifier.height(5.dp))
            // Display Post Age
            Row {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Access Time",
                    tint = Color.Gray,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${formatDateTime(post.dateTime)} by $userName",
                    style = TextStyle(fontStyle = FontStyle.Italic)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Likes: ${post.likes.size} Replies: ${post.comments.size}",
                style = TextStyle(fontStyle = FontStyle.Italic)
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            IconButton(
                onClick = {
                    isLiked = !isLiked
                    firebaseHelper.updatePostLike(post.key)
                },
                modifier = Modifier
                    .padding(5.dp)
                    .size(30.dp)
            ) {
                Icon(
                    Icons.Rounded.Favorite,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray,
                )
            }
            IconButton(
                onClick = {
                    val scope = CoroutineScope(Job() + Dispatchers.Main)
                    scope.launch {
                        firebaseHelper.deletePost(post.key)
                    }
                },
                modifier = Modifier
                    .padding(5.dp)
                    .size(30.dp)
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray,
                )
            }
        }
    }
}

private fun formatDateTime(dateTimeString: String): String {
    val postDateTime =
        LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("hh:mma dd/MM/uu"))
    val currentTime = LocalDateTime.now()

    val duration = Duration.between(postDateTime, currentTime)

    return when {
        duration.toDays() > 0 -> "${duration.toDays()} days ago"
        duration.toHours() > 0 -> "${duration.toHours()} hours ago"
        duration.toMinutes() > 0 -> "${duration.toMinutes()} minutes ago"
        else -> "Just now"
    }
}

@Preview
@Composable
fun AdminDashboardScreenPreview() {
    AdminDashboardScreen()
}
