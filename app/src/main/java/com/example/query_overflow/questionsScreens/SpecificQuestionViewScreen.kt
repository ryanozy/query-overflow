package com.example.query_overflow.questionsScreens

import android.util.Log
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.query_overflow.Comments
import com.example.query_overflow.Post
import com.example.query_overflow.R
import com.example.query_overflow.Replies
import com.example.query_overflow.Screen
import com.example.query_overflow.firebaseHelper
import com.example.query_overflow.ui.theme.QueryOverflowTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

@Composable
fun ReplySection(
    postKey: String,
    comment: Comments,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val userReply = remember { mutableStateOf("") }
    var isCommentMenu by remember { mutableStateOf(false) }
    var isCommentLiked by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var isUserComment by remember { mutableStateOf(false) }
    var isCommentEdit by remember { mutableStateOf(false) }
    var isReplyEdit by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postKey) {
        firebaseHelper.isCommentLikedByUser(postKey, comment.key) { isLike ->
            isCommentLiked = isLike
        }
        firebaseHelper.getUserNameByID(comment.userID) { name ->
            userName = name ?: "Anonymous"
        }
        isUserComment = if (firebaseHelper.getUserID() == comment.userID) {
            true
        } else firebaseHelper.isAdminAuth()
    }

    Surface(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .shadow(4.dp, MaterialTheme.shapes.medium)
            .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
            .clickable { isExpanded = !isExpanded },
        color = Color.White,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Surface(
                color = Color.White,
                modifier = Modifier
                    .animateContentSize()
            ) {
                Column {
                    if (!isCommentEdit) {
                        Text(
                            text = comment.comment,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Row(modifier = Modifier.padding(8.dp)) {
                            TextField(
                                modifier = Modifier.weight(1f), // Added weight modifier
                                value = commentText,
                                onValueChange = { commentText = it },
                                label = { Text("Edit Comment") },
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
                                    firebaseHelper.editComment(postKey, comment.key, commentText)
                                    isCommentEdit = !isCommentEdit
                                    commentText = ""
                                },
                            ) {
                                Text(text = "Edit")
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Surface(modifier = Modifier
                    .clickable { isCommentMenu = !isCommentMenu }
                    .weight(1f) // Add weight modifier
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Reply,
                            contentDescription = "Reply",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${comment.replies.size} Replies",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier
                    .clickable {
                        isCommentLiked = !isCommentLiked
                        firebaseHelper.updateCommentLike(postKey, comment.key)
                    }
                    .weight(1f) // Add weight modifier
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Add like icon
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = "Like",
                            modifier = Modifier.size(24.dp),
                            tint = if (isCommentLiked) Color.Red else Color.Black,
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${comment.likes.size} Likes",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }

                // Add Empty spacing between icons
                Surface(modifier = Modifier.weight(1.5f)) {}
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(
                    text = "Posted by $userName (${formatDateTime(comment.dateTime)})",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(5.dp)
                )

                if (isUserComment) {
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        modifier = Modifier
                            .clickable {
                                commentText = comment.comment
                                isCommentEdit = !isCommentEdit
                            },
                    ) {
                        Icon(
                            Icons.Rounded.EditNote,
                            contentDescription = "Edit",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .clickable {
                                val scope = CoroutineScope(Job() + Dispatchers.Main)
                                scope.launch {
                                    firebaseHelper.deleteComment(postKey, comment.key)
                                }
                            },
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (comment.lastEdited != "") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(
                        text = "Last Edited: ${formatDateTime(comment.lastEdited)}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }

            if (isCommentMenu) {
                Row(modifier = Modifier.padding(8.dp)) {
                    TextField(
                        modifier = Modifier.weight(1f), // Added weight modifier
                        value = userReply.value,
                        onValueChange = { userReply.value = it },
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

                            val replies = Replies(
                                userID = firebaseHelper.getUserID(),
                                dateTime = currentDateTime.format(formatter),
                                comment = userReply.value,
                            )
                            firebaseHelper.addReplies(postKey, comment.key, replies)
                            userReply.value = ""
                            isCommentMenu = !isCommentMenu
                        },
                    ) {
                        Text(text = "Post")
                    }
                }
            }
            if (isExpanded && comment.replies.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp) // Adjust padding as needed
                    ) {
                        comment.replies.forEach { reply ->
                            var isReplyLiked by remember { mutableStateOf(false) }
                            var replyUserName by remember { mutableStateOf("") }
                            var isUserReply by remember { mutableStateOf(false) }

                            firebaseHelper.isReplyLikedByUser(
                                postKey,
                                comment.key,
                                reply.key
                            ) { isLike ->
                                isReplyLiked = isLike
                            }

                            firebaseHelper.getUserNameByID(reply.userID) { name ->
                                replyUserName = name ?: "Anonymous"
                            }

                            isUserReply = if (firebaseHelper.getUserID() == reply.userID) {
                                true
                            } else firebaseHelper.isAdminAuth()

                            Surface(
                                modifier = Modifier
                                    .padding(5.dp)
                                    .shadow(
                                        5.dp,
                                        RoundedCornerShape(22.dp),
                                        ambientColor = Color.White
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = reply.comment,
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            isReplyLiked = !isReplyLiked
                                            firebaseHelper.updateReplyLike(
                                                postKey,
                                                comment.key,
                                                reply.key
                                            )
                                        }
                                    ) {
                                        // Add like icon
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Like",
                                            modifier = Modifier.size(24.dp),
                                            tint = if (isReplyLiked) Color.Red else Color.Gray,
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "${reply.likes.size} Likes",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(5.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row {
                                        Text(
                                            text = "Posted by $replyUserName on ${
                                                formatDateTime(
                                                    reply.dateTime
                                                )
                                            }",
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (isUserReply) {
                                            Surface(
                                                modifier = Modifier
                                                    .clickable {
                                                        isReplyEdit = !isReplyEdit
                                                        commentText = reply.comment
                                                    },
                                            ) {
                                                Icon(
                                                    Icons.Rounded.EditNote,
                                                    contentDescription = "Edit",
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Surface(
                                                modifier = Modifier
                                                    .clickable {
                                                        val scope =
                                                            CoroutineScope(Job() + Dispatchers.Main)
                                                        scope.launch {
                                                            firebaseHelper.deleteReply(
                                                                postKey,
                                                                comment.key,
                                                                reply.key
                                                            )
                                                        }
                                                    },
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (reply.lastEdited != "") {
                                        Text(
                                            text = "Last Edited: ${formatDateTime(reply.lastEdited)}",
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            if (isReplyEdit) {
                                Row(modifier = Modifier.padding(8.dp)) {
                                    TextField(
                                        modifier = Modifier.weight(1f), // Added weight modifier
                                        value = commentText,
                                        onValueChange = { commentText = it },
                                        label = { Text("Edit Comment") },
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
                                            firebaseHelper.editReply(
                                                postKey,
                                                comment.key,
                                                reply.key,
                                                commentText
                                            )
                                            isReplyEdit = !isReplyEdit
                                            commentText = ""
                                        },
                                    ) {
                                        Text(text = "Edit")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(5.dp))
                        }
                    }
                }

            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
}


@Composable
fun SpecificQuestionSection(
    modifier: Modifier = Modifier,
    post: Post,
) {
    var downloadedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var isCommentMenu by remember { mutableStateOf(false) }
    val userComments = remember { mutableStateOf("") }
    var isPostLiked by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var isUserPost by remember { mutableStateOf(false) }
    var isPostEdited by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    isLoading = post.attachmentsFileName.isNotEmpty() && downloadedFiles.isEmpty()

    LaunchedEffect(post) {
        firebaseHelper.getPhoto(post.key, post.attachmentsFileName) { files ->
            downloadedFiles = files
            isLoading = false
            Log.d("SpecificQuestionSection", "Downloaded Files: $files")
        }
        firebaseHelper.isLikedByUser(post.key) { isLike ->
            isPostLiked = isLike
        }
        firebaseHelper.getUserNameByID(post.userID) { name ->
            userName = name ?: "Anonymous"
        }
        firebaseHelper.isPostEdited(post.key) { isEdited ->
            isPostEdited = isEdited
        }
        isUserPost = firebaseHelper.getUserID() == post.userID || firebaseHelper.isAdminAuth()
    }

    if (showImageDialog) {
        ImageDialog(
            imageFile = selectedImage,
            onDismiss = { showImageDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, MaterialTheme.shapes.medium)
                    .border(1.dp, Color.Gray, MaterialTheme.shapes.medium),
                color = Color.White,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {

                    Surface(
                        color = Color.White,
                        modifier = Modifier
                            .animateContentSize()
                    ) {
                        Column {
                            Text(
                                text = post.description,
                                modifier = Modifier
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        if (!isLoading) {
                            LazyRow {
                                items(downloadedFiles) { file ->
                                    Image(
                                        painter = rememberAsyncImagePainter(file),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable {
                                                selectedImage = file
                                                showImageDialog = true
                                            }
                                    )
                                }
                            }
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(all = 8.dp)
                            )
                        }
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
                                        colorResource(id = R.color.black),
                                        RoundedCornerShape(10.dp)
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
                            // tiffany added to take away the background colour of Replies
                            color = Color.Transparent
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Reply,
                                    contentDescription = "Reply",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${post.comments.size} Replies",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clickable {
                                    isPostLiked = !isPostLiked
                                    firebaseHelper.updatePostLike(post.key)
                                }
                                .weight(1f), // Add weight modifier
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // Add like icon
                                Icon(
                                    Icons.Rounded.Favorite,
                                    contentDescription = "Like",
                                    modifier = Modifier
                                        .size(24.dp),
                                    tint = if (isPostLiked) Color.Red else Color.Black,
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${post.likes.size} Likes",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(5.dp)
                                )
                            }
                        }

                        // Empty Surface
                        Surface(modifier = Modifier.weight(1.5f)) {}
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
//                        Icon(
//                            Icons.Rounded.AccessTime,
//                            contentDescription = "Time",
//                            modifier = Modifier.size(24.dp)
//                        )
                        Text(
                            text = "Posted by $userName on ${post.dateTime}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(5.dp)
                        )
                    }

                    if (isPostEdited) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
//                            Icon(
//                                Icons.Rounded.EditNote,
//                                contentDescription = "Edit",
//                                modifier = Modifier.size(24.dp)
//                            )
                            Text(
                                text = "Edited on ${post.lastEdited}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(5.dp)
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
        }

        item {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 20.dp, 0.dp, 20.dp)
            )
        }

        items(post.comments) { comment ->
            ReplySection(postKey = post.key, comment = comment)
        }

        if (post.comments.isEmpty()) {
            item {
                Text(text = "No Reply Yet")
            }
        }
    }
}


@Composable
fun ImageDialog(imageFile: File?, onDismiss: () -> Unit) {

    if (imageFile != null) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageFile),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                )
            }
        }
    }

}

@Composable
fun SpecificQuestionViewScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    passedString: String
) {
    var selectedPost by remember { mutableStateOf(Post()) }
    var showMenu by remember { mutableStateOf(false) }
    var isUserPost by remember { mutableStateOf(false) }

    LaunchedEffect(passedString) {
        firebaseHelper.getSpecificLivePost(passedString) { post ->
            selectedPost = post
            isUserPost = firebaseHelper.getUserID() == selectedPost.userID || firebaseHelper.isAdminAuth()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically, // Align items vertically centered
        ) {
            Image(
                painter = painterResource(id = R.drawable.backarrow),
                contentDescription = "Back Arrow",
                modifier = Modifier
                    .padding(16.dp)
                    .size(30.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )

            Text(
                selectedPost.subject,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            // More button
            if (isUserPost) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopEnd)
                        .padding(top = 5.dp)
                )
                {
                    IconButton(
                        onClick = { showMenu = !showMenu },

                        ) {
                        Icon(
                            // painter = painterResource(id = R.drawable.baseline_density_medium_24),
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            modifier = modifier
                                .size(30.dp)
                                .clip(CircleShape)
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ) {
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            offset = DpOffset(x = 23.dp, 44.dp)
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = "Edit Question",
                                        tint = Color.Black
                                    )
                                },
                                text = { Text(text = "Edit Post") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("${Screen.QuestionEdit.route}/${selectedPost.key}")
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete Question",
                                        tint = Color.Red
                                    )
                                },
                                text = { Text(text = "Delete") },
                                onClick = {
                                    showMenu = false
                                    val scope = CoroutineScope(Job() + Dispatchers.Main)
                                    scope.launch {
                                        firebaseHelper.deletePost(selectedPost.key)
                                    }
                                    navController.navigate("${Screen.QuestionView.route}/ ")
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.padding(5.dp))
        SpecificQuestionSection(
            post = selectedPost,
        )
    }
}


@Preview
@Composable
fun SpecificQuestionViewScreenPreview() {
    QueryOverflowTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SpecificQuestionViewScreen(passedString = "Test Key")
        }
    }
}