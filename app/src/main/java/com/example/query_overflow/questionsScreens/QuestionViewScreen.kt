package com.example.query_overflow.questionsScreens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.query_overflow.Comments
import com.example.query_overflow.Post
import com.example.query_overflow.R
import com.example.query_overflow.Screen
import com.example.query_overflow.firebaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun QuestionViewScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    passedString: String = ""
) {

    val searchInput = remember { mutableStateOf(passedString) }

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (passedString.isNotEmpty()) {
            firebaseHelper.searchPosts(passedString) { searchResults ->
                // Update the posts list with search results
                posts = searchResults
            }
        } else {
            // If search query is empty, reset to display all posts
            firebaseHelper.fetchAllPostFromFirebase { updatedPosts ->
                posts = updatedPosts.reversed()
            }
        }
    }

    if (!firebaseHelper.isAuth()) {
        navController.navigate(Screen.Login.route)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically // Align items vertically centered
        ) {
            Image(painter = painterResource(id = R.drawable.backarrow),
                contentDescription = "Back Arrow",
                modifier = Modifier
                    .padding(10.dp)
                    .size(30.dp)
                    .clickable {
                        if (firebaseHelper.isAdminAuth()) {
                            navController.navigate(Screen.Admin.route)
                        } else {
                            navController.navigate(Screen.Dashboard.route)
                        }
                    }
            )

            Text(
                "Questions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        //Spacer(modifier = Modifier.padding(5.dp))

        // Search Field
        Row {
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
                    .fillMaxWidth(1f)
                    .padding(start = 3.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchInput.value.isNotEmpty()) {
                            firebaseHelper.searchPosts(searchInput.value) { searchResults ->
                                // Update the posts list with search results
                                posts = searchResults
                            }
                        } else {
                            // If search query is empty, reset to display all posts
                            firebaseHelper.fetchAllPostFromFirebase { updatedPosts ->
                                posts = updatedPosts
                            }
                        }
                    }
                )
            )
        }

        Spacer(modifier = Modifier.padding(5.dp))

        LazyColumn(modifier = Modifier.testTag("LazyColumn")) {
            items(items = posts, key = { post -> post.key }) { post ->
                QuestionSection(post = post, navController = navController)
            }
        }
    }
}


@Composable
fun QuestionSection(
    post: Post,
    navController: NavController = rememberNavController()
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isCommentMenu by remember { mutableStateOf(false) }
    val userComments = remember { mutableStateOf("") }
    var isLiked by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var isUserPost by remember { mutableStateOf(false) }
    var isPostEdited by remember { mutableStateOf(false) }

    LaunchedEffect(post) {
        firebaseHelper.isLikedByUser(post.key) { isLike ->
            isLiked = isLike
        }

        firebaseHelper.getUserNameByID(post.userID) { name ->
            userName = name ?: "Anonymous"
        }

        firebaseHelper.isPostEdited(post.key) { isEdited ->
            isPostEdited = isEdited
        }

        isUserPost = if (firebaseHelper.getUserID() == post.userID) {
            true
        } else firebaseHelper.isAdminAuth()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, MaterialTheme.shapes.medium)
            .clickable {
                navController.navigate("${Screen.SpecificQuestionView.route}/${post.key}")
            }
            .border(1.dp, Color.Gray, MaterialTheme.shapes.medium),
        color = Color.White,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = post.subject,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (isUserPost) {
                    Surface(
                        modifier = Modifier
                            .clickable {
                                navController.navigate("${Screen.QuestionEdit.route}/${post.key}")
                            },
                    ) {
                        Icon(
                            Icons.Rounded.EditNote,
                            contentDescription = "Edit",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                if (isUserPost) {
                    Surface(
                        modifier = Modifier
                            .clickable {
                                val scope = CoroutineScope(Job() + Dispatchers.Main)
                                scope.launch {
                                    firebaseHelper.deletePost(post.key)
                                }
                            },
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Surface(color = Color.White,
                modifier = Modifier
                    .animateContentSize()
                    .clickable { isExpanded = !isExpanded }) {
                Column {
                    Text(
                        text = post.description,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                    )
                    /* // If not expanded, show "View More" text
                    if (!isExpanded) {
                        Text(
                            text = "View More",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )*/

                    // TIFFANY TESTING
                    // If not expanded, show "View More" text
                    if (!isExpanded && post.description.length > 125) {
                        Text(
                            text = "View More",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    // If expanded, show "View Less" text
                    if (isExpanded && post.description.length > 125 ) {
                        Text(
                            text = "View Less",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(1.dp)
            ) {
                // Display downloaded images
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                for (tag in post.tags) {
                    Text(
                        text = tag,
                        modifier = Modifier
                            .background(
                                colorResource(id = R.color.black), RoundedCornerShape(10.dp)
                            )
                            .padding(5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Surface(modifier = Modifier
                    .clickable { isCommentMenu = !isCommentMenu }
                    .weight(1f), // Add weight modifier
                    color = Color.Transparent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Reply,
                            contentDescription = "Reply",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "${post.comments.size} Replies",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier
                    .clickable {
                        isLiked = !isLiked
                        firebaseHelper.updatePostLike(post.key)
                    }
                    .weight(1f) // Add weight modifier
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Add like icon
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = "Like",
                            modifier = Modifier
                                .size(24.dp),
                            tint = if (isLiked) Color.Red else Color.Gray,
                        )
                        Text(
                            text = "${post.likes.size} Likes",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
                Text(
                    text = "Attachments: ${post.attachmentsFileName.size}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(5.dp)
                )

                // Empty Surface
                Surface(modifier = Modifier.weight(0.5f)) {}
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(
                    text = "Posted by $userName on ${post.dateTime}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(3.dp)
                )
            }

            if (isPostEdited) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(
                        text = "Edited on ${post.lastEdited}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(3.dp)
                    )
                }
            }

            if (isCommentMenu) {
                Row(modifier = Modifier.padding(8.dp)) {
                    TextField(
                        modifier = Modifier.weight(1f), // Added weight modifier
                        value = userComments.value,
                        onValueChange = { userComments.value = it },
                        label = { Text("Enter Comment") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F0F0),
                            focusedLabelColor = Color(0xFF000000),
                            focusedTextColor = Color(0xFF000000),
                            focusedIndicatorColor = Color(0x00000000),
                            unfocusedContainerColor = Color(0xFFE1E1E1),
                            unfocusedLabelColor = Color(0xFF000000),
                            unfocusedIndicatorColor = Color(0x00000000),
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Added Spacer for separation
                    Button(
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(9.dp), // Added weight modifier
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007AFF)
                        ),
                        onClick = {
                            val currentDateTime = LocalDateTime.now()
                            val formatter = DateTimeFormatter.ofPattern("hh:mma dd/MM/uu")

                            val comments = Comments(
                                userID = firebaseHelper.getUserID(),
                                dateTime = currentDateTime.format(formatter),
                                comment = userComments.value
                            )

                            firebaseHelper.uploadComment(post.key, comments)
                            // Trigger Refresh
                            userComments.value = ""
                            isCommentMenu = !isCommentMenu
                        },
                    ) {
                        Text(text = "Post")
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}