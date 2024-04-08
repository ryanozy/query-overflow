package com.example.query_overflow

import android.app.Activity
import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Define Data Class
data class Post(
    var key: String = "",
    var userID: String = "",
    var dateTime: String = "",
    var subject: String = "",
    var module: String = "",
    var description: String = "",
    var tags: List<String> = emptyList(),
    var comments: List<Comments> = emptyList(),
    var likes: List<String> = emptyList(),
    var attachmentsFileName: List<String> = emptyList(),
    var lastEdited: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "key" to key,
            "userID" to userID,
            "dateTime" to dateTime,
            "subject" to subject,
            "module" to module,
            "description" to description,
            "tags" to tags,
            "comments" to comments,
            "likes" to likes,
            "attachmentsFileName" to attachmentsFileName,
            "lastEdited" to lastEdited
        )
    }
}

data class Comments(
    var userID: String = "",
    var dateTime: String = "",
    var comment: String = "",
    var likes: List<String> = emptyList(),
    var replies: List<Replies> = emptyList(),
    var key: String = "",
    var lastEdited: String = ""
)

data class Replies(
    var userID: String = "",
    var dateTime: String = "",
    var comment: String = "",
    var likes: List<String> = emptyList(),
    var key: String = "",
    var lastEdited: String = ""
)

// Define FirebaseHelper Class
class FirebaseHelper : Activity() {
    // Firebase Authentication
    private var auth: FirebaseAuth = Firebase.auth

    // Firebase Storage
    private lateinit var storage: FirebaseStorage

    // Firebase Realtime Database
    private val database =
        Firebase.database("https://inf2007-team46-2024-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val myRef = database.getReference("Subjects")

    // Getters //
    /**
     * Retrieves photos associated with a specific key from Firebase Storage.
     * @param postKey (str): Post Unique Identifier key
     * @param files (List<File>): A list of filenames corresponding to the photos to be retrieved.
     * @param onSuccess Callback function to be executed upon successful retrieval of photos,
     * providing the list of downloaded files as a parameter.
     */
    fun getPhoto(postKey: String, files: List<String>, onSuccess: (List<File>) -> Unit) {
        val postFiles = mutableListOf<File>()
        val storageRef = Firebase.storage("gs://inf2007-team46-2024.appspot.com").reference

        // Use a counter to keep track of how many files have been downloaded
        var counter = 0

        // Iterate through each file to download
        for (file in files) {
            val imageRef = storageRef.child("user_posts_image").child(postKey).child(file)
            val localFile = File.createTempFile("${file.substringAfterLast('/')}-", "-jpg")
            Log.d("Download", "Downloading file: $file")

            imageRef.getFile(localFile)
                .addOnSuccessListener {
                    // Add the downloaded file to the list
                    postFiles.add(localFile)

                    // Increment the counter
                    counter++

                    // If all files have been downloaded, call the onSuccess callback with the list of files
                    if (counter == files.size) {
                        onSuccess(postFiles)
                    }
                }
                .addOnFailureListener {
                    Log.d("Download", "Fail")
                }
        }
    }

    /**
     * Retrieves a specific post from Firebase Realtime Database.
     * @param userID (str): Post Unique Identifier key
     * @param onSuccess Callback function to be executed upon successful retrieval of a Specific Post.
     */
    fun getProfilePhoto(userID: String, onSuccess: (File) -> Unit) {
        val storageRef = Firebase.storage("gs://inf2007-team46-2024.appspot.com").reference

        // Download the profile image
        val imageRef = storageRef.child("profile_image").child(userID).child("profile.jpg")
        val localFile = File.createTempFile("profile-", "-jpg")
        Log.d("Download", "Downloading profile image")

        imageRef.getFile(localFile)
            .addOnSuccessListener {
                // Add the downloaded file to the list
                onSuccess(localFile)

            }
            .addOnFailureListener {
                Log.d("Download", "Fail")
            }
    }

    private fun uploadProfilePhoto(uri: Uri) {
        // Attempt to upload photo first
        storage = Firebase.storage("gs://inf2007-team46-2024.appspot.com")
        val storageRef = storage.reference

        val name = uri.lastPathSegment
        val ref = storageRef.child("profile_image/${getUserID()}/profile.jpg")

        // if the user has a profile picture, delete it
        ref.delete()
        val uploadTask = ref.putFile(uri)

        uploadTask.addOnFailureListener { exception ->
            Log.d("Firebase File Upload", "Failed to upload file $name: $exception")
        }.addOnSuccessListener { taskSnapshot ->
            Log.d(
                "Firebase File Upload",
                "File $name uploaded successfully. Metadata: ${taskSnapshot.metadata}"
            )
        }
    }

    /**
     * @return Returns the user's current displayName from the Database
     */
    fun getUserName(): String {
        return auth.currentUser?.displayName ?: ""
    }

    fun getUserNameByID(userID: String, callback: (String?) -> Unit) {
        val userRef = myRef.child("Users").child(userID).child("userName")
        userRef.get().addOnSuccessListener { dataSnapshot ->
            val userName = dataSnapshot.getValue(String::class.java)
            callback(userName)
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error getting user name", exception)
            callback(null)
        }
    }

    fun downloadProfilePhotoFromUserName(userName: String, onSuccess: (File) -> Unit) {
        val userRef = myRef.child("Users")
        userRef.get().addOnSuccessListener { dataSnapshot ->
            val users = dataSnapshot.children
            users.forEach { user ->
                if (userName == user.child("userName").toString()) {
                    val userID = user.key.toString()
                    val storageRef =
                        Firebase.storage("gs://inf2007-team46-2024.appspot.com").reference

                    // Download the profile image
                    val imageRef =
                        storageRef.child("profile_image").child(userID).child("profile.jpg")
                    val localFile = File.createTempFile("profile-", "-jpg")

                    imageRef.getFile(localFile)
                        .addOnSuccessListener {
                            // Add the downloaded file to the list
                            onSuccess(localFile)
                        }
                        .addOnFailureListener {
                            Log.d("Download", "Fail")
                        }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error getting users", exception)
        }
    }

    /**
     * @return Returns the user's current uid from the Database
     */
    fun getUserID(): String {
        return auth.currentUser?.uid ?: ""
    }

    /**
     * Retrieves a specific post live associated with a specific key from Firebase Realtime Database.
     * @param postKey (str): Post Unique Identifier key
     * @param onSuccess Callback function to be executed upon successful retrieval of a Specific Post.
     */
    fun getSpecificLivePost(postKey: String, onSuccess: (Post) -> Unit) {
        myRef.child("user-posts").child(postKey).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val getPost: Post? = dataSnapshot.getValue(Post::class.java)
                Log.d("Test", getPost.toString())
                if (getPost != null) {
                    onSuccess(getPost)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    /**
     * Retrieves a all post from Firebase Realtime Database.
     * @param onDataChanged Callback function to be executed upon successful retrieval of a list of Posts.
     */
    fun fetchAllPostFromFirebase(onDataChanged: (List<Post>) -> Unit) {
        myRef.child("user-posts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (userSnapshot in dataSnapshot.children) {
                    val post = userSnapshot.getValue(Post::class.java)
                    post?.let {
                        posts.add(it)
                    }
                }
                onDataChanged(posts)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    fun searchPosts(searchText: String, onDataChanged: (List<Post>) -> Unit) {
        myRef.child("user-posts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (userSnapshot in dataSnapshot.children) {
                    val post = userSnapshot.getValue(Post::class.java)
                    post?.let {
                        if (it.subject.contains(
                                searchText,
                                ignoreCase = true
                            ) || it.description.contains(searchText, ignoreCase = true)
                        ) {
                            posts.add(it)
                        }
                    }
                }
                onDataChanged(posts)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }


    // File and Post Uploading functions //
    /**
     * A function that is use to upload post into the Firebase Realtime Database
     * @param post (Post Class): Post Class to upload to database
     * @return Returns a Post Unique Identifier key
     */
    fun uploadPost(post: Post): String? {

        val key = myRef.push().key ?: run {
            Log.w("Set Value", "Couldn't get push key for posts")
            return null
        }

        post.key = key

        val userID = post.userID

        if (userID == "") {
            Log.d("Set Value", "Couldn't get user")
            return null
        }

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("hh:mma dd/MM/uu")
        post.dateTime = currentDateTime.format(formatter)

        val postValues = post.toMap()
        val childUpdates = hashMapOf<String, Any>(
            "/user-posts/$key" to postValues,
        )

        myRef.updateChildren(childUpdates)
            .addOnCompleteListener {
                Log.d("Set Value", "Data added successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Set Value", "Error adding data", e)
            }

        return post.key
    }

    /**
     * A function that is use to update post into the Firebase Realtime Database
     * @param post (Post Class): Post Class to upload to database
     * @param key (str): Post Unique Identifier key
     * @return Returns a Post Unique Identifier key
     */
    suspend fun updatePost(
        post: Post,
        key: String,
        moduleChange: Boolean,
        deletedFileName: List<String>
    ): String = coroutineScope {
        if (moduleChange) {
            updatePostInDatabase(post, key)
        } else {
            val oldModuleSnapshot =
                myRef.child("user-posts").child(key).child("module").get().await()
            val oldModule = oldModuleSnapshot.getValue(String::class.java).toString()

            removeLikeCounts(oldModule, key)
            updatePostInDatabase(post, key)

            val newModuleSnapshot =
                myRef.child("user-posts").child(key).child("module").get().await()
            val newModule = newModuleSnapshot.getValue(String::class.java).toString()
            addLikeCounts(newModule, key)
        }

        // Remove Files from Firebase Storage
        val storageRef = Firebase.storage("gs://inf2007-team46-2024.appspot.com").reference
        val postRef =
            storageRef.child("user_posts_image").child(key).child("primary:DCIM").child("Camera")
        // Iterate through postRef images and remove the ones that are not in the downloadedFiles
        postRef.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { imageRef ->
                for (fileName in deletedFileName) {
                    val name = fileName.split("/").last()
                    if (imageRef.name == name) {
                        imageRef.delete()
                    }
                }
            }
        }.addOnFailureListener {
            Log.d("Firebase File Upload", "Failed to delete file: $it")
        }

        return@coroutineScope key

    }

    private suspend fun removeLikeCounts(module: String, key: String) {
        removePostLikeCount(module, key)
        removeCommentLikeCount(module, key)
        removeReplyLikeCount(module, key)
    }

    private suspend fun addLikeCounts(module: String, key: String) {
        addPostLikeCount(module, key)
        addCommentLikeCount(module, key)
        addReplyLikeCount(module, key)
    }

    private suspend fun updatePostInDatabase(post: Post, key: String) {
        post.key = key
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("hh:mma dd/MM/uu")
        post.lastEdited = currentDateTime.format(formatter)
        val postValues = post.toMap()

        try {
            myRef.child("user-posts").child(key).updateChildren(postValues).await()
            Log.d("Set Value", "Data updated successfully")
        } catch (e: Exception) {
            Log.w("Set Value", "Error updating data", e)
        }
    }

    fun editComment(postKey: String, commentKey: String, newComment: String) {
        getCommentIndex(postKey, commentKey) { commentIndex ->
            if (commentIndex != -1) {
                val commentRef = myRef.child("user-posts").child(postKey).child("comments")
                commentRef.get().addOnSuccessListener { dataSnapshot ->
                    val existingComments =
                        dataSnapshot.getValue(object : GenericTypeIndicator<List<Comments>>() {})

                    val updatedComments = existingComments?.toMutableList() ?: mutableListOf()
                    updatedComments[commentIndex].comment = newComment
                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("hh:mma dd/MM/uu")
                    updatedComments[commentIndex].lastEdited = currentDateTime.format(formatter)

                    commentRef.setValue(updatedComments)
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting existing comments", exception)
                }
            } else {
                Log.e("Firebase", "Comment not found")
            }
        }
    }

    fun editReply(postKey: String, commentKey: String, replyKey: String, newReply: String) {
        getCommentIndex(postKey, commentKey) { commentIndex ->
            if (commentIndex != -1) {
                val replyRef = myRef.child("user-posts").child(postKey).child("comments")
                    .child(commentIndex.toString()).child("replies")
                replyRef.get().addOnSuccessListener { dataSnapshot ->
                    val existingReplies =
                        dataSnapshot.getValue(object : GenericTypeIndicator<List<Replies>>() {})

                    existingReplies?.forEachIndexed { index, reply ->
                        if (reply.key == replyKey) {
                            val updatedReplies = existingReplies.toMutableList()
                            updatedReplies[index].comment = newReply
                            val currentDateTime = LocalDateTime.now()
                            val formatter = DateTimeFormatter.ofPattern("hh:mma dd/MM/uu")
                            updatedReplies[index].lastEdited = currentDateTime.format(formatter)

                            replyRef.setValue(updatedReplies)
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting existing comments", exception)
                }
            } else {
                Log.e("Firebase", "Comment not found")
            }
        }
    }

    private suspend fun removePostLikeCount(module: String, key: String) {
        val postSnapshot = myRef.child("user-posts").child(key).child("likes").get().await()
        val existingLikes = postSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
        val userSnapshot = myRef.child("user-posts").child(key).child("userID").get().await()
        val userID = userSnapshot.getValue(String::class.java).toString()
        val likeCount = existingLikes?.size ?: 0
        val likeCountRef = myRef.child("Users").child(userID).child("likeCount").child(module)

        val existingLikeCountSnapshot = likeCountRef.get().await()
        val existingLikeCount = existingLikeCountSnapshot.getValue(Int::class.java)
        val updatedLikeCount = existingLikeCount?.minus(likeCount) ?: 0
        likeCountRef.setValue(updatedLikeCount)
    }

    private suspend fun removeCommentLikeCount(module: String, key: String) {
        val commentSnapshot = myRef.child("user-posts").child(key).child("comments").get().await()
        val existingComments =
            commentSnapshot.getValue(object : GenericTypeIndicator<List<Comments>>() {})
        existingComments?.forEach { comment ->
            val likeCount = comment.likes.size
            val likeCountRef = myRef.child("Users").child(comment.userID).child("likeCount")
                .child(module)
            val existingLikeCountSnapshot = likeCountRef.get().await()
            val existingLikeCount = existingLikeCountSnapshot.getValue(Int::class.java)
            val updatedLikeCount = existingLikeCount?.minus(likeCount) ?: 0
            likeCountRef.setValue(updatedLikeCount)
        }
    }

    private suspend fun removeReplyLikeCount(module: String, key: String) {
        val commentSnapshot = myRef.child("user-posts").child(key).child("comments").get().await()
        val existingComments =
            commentSnapshot.getValue(object : GenericTypeIndicator<List<Comments>>() {})
        existingComments?.forEach { comment ->
            // Reply is indexed
            val replySnapshot = myRef.child("user-posts").child(key).child("comments")
                .child(existingComments.indexOf(comment).toString()).child("replies").get().await()
            val existingReplies =
                replySnapshot.getValue(object : GenericTypeIndicator<List<Replies>>() {})
            existingReplies?.forEach { reply ->
                val likeCount = reply.likes.size
                val likeCountRef = myRef.child("Users").child(reply.userID).child("likeCount")
                    .child(module)
                val existingLikeCountSnapshot = likeCountRef.get().await()
                val existingLikeCount = existingLikeCountSnapshot.getValue(Int::class.java)
                val updatedLikeCount = existingLikeCount?.minus(likeCount) ?: 0
                likeCountRef.setValue(updatedLikeCount)
            }
        }
    }

    private suspend fun addPostLikeCount(module: String, key: String) {
        val postSnapshot = myRef.child("user-posts").child(key).child("likes").get().await()
        val existingLikes = postSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
        val userSnapshot = myRef.child("user-posts").child(key).child("userID").get().await()
        val userID = userSnapshot.getValue(String::class.java).toString()
        val likeCount = existingLikes?.size ?: 0
        val likeCountRef = myRef.child("Users").child(userID).child("likeCount").child(module)

        val existingLikeCountSnapshot = likeCountRef.get().await()
        val existingLikeCount = existingLikeCountSnapshot.getValue(Int::class.java)
        val updatedLikeCount = existingLikeCount?.plus(likeCount) ?: likeCount
        likeCountRef.setValue(updatedLikeCount)
    }

    private suspend fun addCommentLikeCount(module: String, key: String) {
        val commentSnapshot = myRef.child("user-posts").child(key).child("comments").get().await()
        val existingComments =
            commentSnapshot.getValue(object : GenericTypeIndicator<List<Comments>>() {})
        existingComments?.forEach { comment ->
            val likeCount = comment.likes.size
            val likeCountRef = myRef.child("Users").child(comment.userID).child("likeCount")
                .child(module)
            val existingLikeCountSnapshot = likeCountRef.get().await()
            val existingLikeCount = existingLikeCountSnapshot.getValue(Int::class.java)
            val updatedLikeCount = existingLikeCount?.plus(likeCount) ?: likeCount
            likeCountRef.setValue(updatedLikeCount)
        }
    }

    private suspend fun addReplyLikeCount(module: String, key: String) {
        val commentSnapshot = myRef.child("user-posts").child(key).child("comments").get().await()
        val existingComments =
            commentSnapshot.getValue(object : GenericTypeIndicator<List<Comments>>() {})
        existingComments?.forEach { comment ->
            // Reply is indexed
            val replySnapshot = myRef.child("user-posts").child(key).child("comments")
                .child(existingComments.indexOf(comment).toString()).child("replies").get().await()
            val existingReplies =
                replySnapshot.getValue(object : GenericTypeIndicator<List<Replies>>() {})
            existingReplies?.forEach { reply ->
                val likeCount = reply.likes.size
                val likeCountRef = myRef.child("Users").child(reply.userID).child("likeCount")
                    .child(module)
                val existingLikeCountSnapshot = likeCountRef.get().await()
                val existingLikeCount = existingLikeCountSnapshot.getValue(Int::class.java)
                val updatedLikeCount = existingLikeCount?.plus(likeCount) ?: likeCount
                likeCountRef.setValue(updatedLikeCount)
            }
        }
    }

    suspend fun deletePost(key: String) {
        // Remove likes from user likeCount
        val postSnapshot = myRef.child("user-posts").child(key).get().await()
        val post = postSnapshot.getValue(Post::class.java)
        val module = post?.module ?: ""
        removeLikeCounts(module, key)

        // Remove post from database
        myRef.child("user-posts").child(key).removeValue().await()
    }

    fun isPostEdited(postKey: String, onSuccess: (Boolean) -> Unit) {
        val postRef = myRef.child("user-posts").child(postKey)
        postRef.get().addOnSuccessListener { dataSnapshot ->
            // If lastEdited is not null, the post has been edited
            val lastEdited = dataSnapshot.child("lastEdited").getValue(String::class.java)
            onSuccess(lastEdited != "")
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error getting post", exception)
        }
    }

    suspend fun deleteComment(postKey: String, commentKey: String) {
        // Get module of the post
        val postSnapshot = myRef.child("user-posts").child(postKey).get().await()
        val post = postSnapshot.getValue(Post::class.java)
        val module = post?.module ?: ""
        // Remove likes from user likeCount
        val commentSnapshot =
            myRef.child("user-posts").child(postKey).child("comments").get().await()
        val existingComments =
            commentSnapshot.getValue(object : GenericTypeIndicator<List<Comments>>() {})
        existingComments?.forEach { comment ->
            if (comment.key == commentKey) {
                // Iterate through replies and remove likes from user likeCount
                comment.replies.forEach { reply ->
                    val likeCount = reply.likes.size
                    val likeCountRef = myRef.child("Users").child(reply.userID).child("likeCount")
                        .child(module)
                    val existingLikeCountSnapshot = likeCountRef.get().await()
                    val existingLikeCount = existingLikeCountSnapshot.getValue(Int::class.java)
                    val updatedLikeCount = existingLikeCount?.minus(likeCount) ?: 0
                    likeCountRef.setValue(updatedLikeCount)
                }
                val likeCount = comment.likes.size
                val likeCountRef = myRef.child("Users").child(comment.userID).child("likeCount")
                    .child(module)
                val existingLikeCountSnapshot = likeCountRef.get().await()
                val existingLikeCount = existingLikeCountSnapshot.getValue(Int::class.java)
                val updatedLikeCount = existingLikeCount?.minus(likeCount) ?: 0
                likeCountRef.setValue(updatedLikeCount)
            }
        }


        // Remove comment from database
        getCommentIndex(postKey, commentKey) { commentIndex ->
            if (commentIndex != -1) {
                val commentRef = myRef.child("user-posts").child(postKey).child("comments")
                commentRef.get().addOnSuccessListener {
                    val existComments =
                        it.getValue(object : GenericTypeIndicator<List<Comments>>() {})

                    val updatedComments = existComments?.toMutableList() ?: mutableListOf()
                    updatedComments.removeAt(commentIndex)

                    commentRef.setValue(updatedComments)
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting existing comments", exception)
                }
            } else {
                Log.e("Firebase", "Comment not found")
            }
        }
    }

    suspend fun deleteReply(postKey: String, commentKey: String, replyKey: String) {
        // Get module of the post
        val postSnapshot = myRef.child("user-posts").child(postKey).get().await()
        val post = postSnapshot.getValue(Post::class.java)
        val module = post?.module ?: ""
        // Remove likes from user likeCount
        val commentSnapshot =
            myRef.child("user-posts").child(postKey).child("comments").get().await()
        val existingComments =
            commentSnapshot.getValue(object : GenericTypeIndicator<List<Comments>>() {})
        existingComments?.forEach { comment ->
            if (comment.key == commentKey) {
                // Find the reply and remove likes from user likeCount
                comment.replies.forEach { reply ->
                    if (reply.key == replyKey) {
                        val likeCount = reply.likes.size
                        val likeCountRef =
                            myRef.child("Users").child(reply.userID).child("likeCount")
                                .child(module)
                        val existingLikeCountSnapshot = likeCountRef.get().await()
                        val existingLikeCount = existingLikeCountSnapshot.getValue(Int::class.java)
                        val updatedLikeCount = existingLikeCount?.minus(likeCount) ?: 0
                        likeCountRef.setValue(updatedLikeCount)
                    }
                }
            }
        }

        // Remove reply from database
        getCommentIndex(postKey, commentKey) { commentIndex ->
            if (commentIndex != -1) {
                val replyRef = myRef.child("user-posts").child(postKey).child("comments")
                    .child(commentIndex.toString()).child("replies")
                replyRef.get().addOnSuccessListener { dataSnapshot ->
                    val existingReplies =
                        dataSnapshot.getValue(object : GenericTypeIndicator<List<Replies>>() {})

                    existingReplies?.forEachIndexed { index, reply ->
                        if (reply.key == replyKey) {
                            val updatedReplies = existingReplies.toMutableList()
                            updatedReplies.removeAt(index)

                            replyRef.setValue(updatedReplies)
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting existing comments", exception)
                }
            } else {
                Log.e("Firebase", "Comment not found")
            }
        }
    }

    /**
     * This function is used to add comments to a specific Post using its postKey as reference
     * @param postKey (str): Post Unique Identifier key
     * @param comment (Comments Class): Comment to upload to database
     */
    fun uploadComment(postKey: String, comment: Comments) {

        val key = myRef.push().key ?: run {
            Log.w("Set Value", "Couldn't get push key for posts")
            return
        }

        val commentRef = myRef.child("user-posts").child(postKey).child("comments")

        comment.key = key

        // Fetch existing comments
        commentRef.get().addOnSuccessListener { dataSnapshot ->
            val existingComments =
                dataSnapshot.getValue(object : GenericTypeIndicator<List<Comments>>() {})

            // If there are existing comments, add the new comment to the list
            val updatedComments = existingComments?.toMutableList() ?: mutableListOf()
            updatedComments.add(comment)

            // Update the entire list of comments
            commentRef.setValue(updatedComments)
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error getting existing comments", exception)

        }
    }

    /**
     * A function that is use to upload post into the Firebase Storage
     * @param uri (Uri): Uri that of the file user would like to upload
     * @param postKey (str): Post Unique Identifier key
     */
    fun uploadPhoto(uri: Uri, postKey: String) {
        // Attempt to upload photo first
        storage = Firebase.storage("gs://inf2007-team46-2024.appspot.com")
        val storageRef = storage.reference

        val name = uri.lastPathSegment
        val ref = storageRef.child("user_posts_image/$postKey/$name")
        val uploadTask = ref.putFile(uri)

        uploadTask.addOnFailureListener { exception ->
            Log.d("Firebase File Upload", "Failed to upload file $name: $exception")
        }.addOnSuccessListener { taskSnapshot ->
            Log.d(
                "Firebase File Upload",
                "File $name uploaded successfully. Metadata: ${taskSnapshot.metadata}"
            )
        }
    }

    // Authentication Functions //
    /**
     * Sign in the user using the provided email and password.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param onSuccess Callback function to be executed upon successful sign-in.
     * @param onError Callback function to be executed if an error occurs during sign-in,
     * with the error message passed as a parameter.
     */
    fun signIn(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    onSuccess()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    onError(task.exception?.message ?: "Authentication failed.")
                }
            }
        // [END sign_in_with_email]
    }

    fun adminSignIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    if (user?.uid == "Sa60iAQsf6gdjqw8qEaisHkti8B3") {
                        onSuccess()
                    } else {
                        onError("Authentication failed.")
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    onError(task.exception?.message ?: "Authentication failed.")
                }
            }
        // [END sign_in_with_email]
    }

    fun createUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    onSuccess()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    onError(task.exception?.message ?: "Authentication failed.")
                }
            }
    }

    fun signOut() {
        Firebase.auth.signOut()
    }

    // Writing to Database //
    /**
     * This function is use to update user name and email
     * @param userName (str): Username in String
     * @param selectedUri (Uri): Uri to file
     */
    fun updateUserInfo(userName: String, selectedUri: Uri?): Boolean {
        val user = Firebase.auth.currentUser

        val profileUpdates = userProfileChangeRequest {
            displayName = userName
        }

        myRef.child("Users").child(user!!.uid).child("userName").setValue(userName)

        if (selectedUri != null) {
            uploadProfilePhoto(selectedUri)
        }

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                }
            }

        return true
    }


    private fun getCommentIndex(postKey: String, commentKey: String, callback: (Int) -> Unit) {
        var indexOfComment = -1 // Initialize to -1 to indicate not found
        val commentRef = myRef.child("user-posts").child(postKey).child("comments")
        commentRef.get().addOnSuccessListener { dataSnapshot ->
            val existingComments =
                dataSnapshot.getValue(object : GenericTypeIndicator<List<Comments>>() {})

            existingComments?.forEachIndexed { index, comment ->
                if (comment.key == commentKey) {
                    indexOfComment = index
                    return@forEachIndexed
                }
            }
            callback(indexOfComment)
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error getting comments", exception)
            callback(-1) // Invoke callback with -1 to indicate failure
        }
    }

    fun addReplies(postKey: String, commentKey: String, reply: Replies) {
        val key = myRef.push().key ?: run {
            Log.w("Set Value", "Couldn't get push key for posts")
            return
        }

        getCommentIndex(postKey, commentKey) { commentIndex ->
            if (commentIndex != -1) {
                val repliesRef = myRef.child("user-posts").child(postKey).child("comments")
                    .child(commentIndex.toString()).child("replies")

                reply.key = key

                repliesRef.get().addOnSuccessListener { dataSnapshot ->
                    val existingReplies =
                        dataSnapshot.getValue(object : GenericTypeIndicator<List<Replies>>() {})

                    val updatedReplies = existingReplies?.toMutableList() ?: mutableListOf()
                    updatedReplies.add(reply)

                    repliesRef.setValue(updatedReplies)

                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting existing comments", exception)

                }
            } else {
                Log.e("Firebase", "Comment not found")
            }
        }
    }

    /**
     * This function toggles the like status of the given post based on the user's previous interaction.
     * If the user has previously liked the post, it will unlike the post, and vice versa
     * @param postKey (str): The identifier of the post being interacted with.
     */
    fun updatePostLike(postKey: String) {
        val userID = getUserID()
        val likesRef = myRef.child("user-posts").child(postKey).child("likes")
        likesRef.get().addOnSuccessListener { dataSnapshot ->
            val existingLikes =
                dataSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})

            // If there are existing comments, add the new comment to the list
            val updatedLikes = existingLikes?.toMutableList() ?: mutableListOf()
            if (userID in updatedLikes) {
                // Remove user name from the list
                updatedLikes.remove(userID)
                removeLikeCountFromUser(listOf(postKey), "post")
                // Update the entire list of likes
                likesRef.setValue(updatedLikes)
            } else {
                // Add user name that like to list
                updatedLikes.add(userID)
                addLikeCountToUser(listOf(postKey), "post")
                // Update the entire list of likes
                likesRef.setValue(updatedLikes)
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error getting existing comments", exception)
        }
    }

    fun updateCommentLike(postKey: String, commentKey: String) {
        val userID = getUserID()
        getCommentIndex(postKey, commentKey) { commentIndex ->
            if (commentIndex != -1) {
                val likeRef = myRef.child("user-posts").child(postKey).child("comments")
                    .child(commentIndex.toString()).child("likes")
                likeRef.get().addOnSuccessListener { dataSnapshot ->
                    val existingLikes =
                        dataSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})

                    // If there are existing comments, add the new comment to the list
                    val updatedLikes = existingLikes?.toMutableList() ?: mutableListOf()
                    if (userID in updatedLikes) {
                        // Remove user name from the list
                        updatedLikes.remove(userID)
                        removeLikeCountFromUser(listOf(postKey, commentKey), "comment")
                        // Update the entire list of likes
                        likeRef.setValue(updatedLikes)
                    } else {
                        // Add user name that like to list
                        updatedLikes.add(userID)
                        addLikeCountToUser(listOf(postKey, commentKey), "comment")
                        // Update the entire list of likes
                        likeRef.setValue(updatedLikes)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting existing comments", exception)
                }
            } else {
                Log.e("Firebase", "Comment not found")
            }
        }
    }

    fun updateReplyLike(postKey: String, commentKey: String, replyKey: String) {
        val userID = getUserID()
        getCommentIndex(postKey, commentKey) { commentIndex ->
            if (commentIndex != -1) {
                val replyRef = myRef.child("user-posts").child(postKey).child("comments")
                    .child(commentIndex.toString()).child("replies")
                replyRef.get().addOnSuccessListener { dataSnapshot ->
                    val existingReplies =
                        dataSnapshot.getValue(object : GenericTypeIndicator<List<Replies>>() {})

                    existingReplies?.forEachIndexed { index, reply ->
                        if (reply.key == replyKey) {
                            val likeRef = myRef.child("user-posts").child(postKey).child("comments")
                                .child(commentIndex.toString()).child("replies")
                                .child(index.toString()).child("likes")
                            likeRef.get().addOnSuccessListener { dataSnapshot ->
                                val existingLikes =
                                    dataSnapshot.getValue(object :
                                        GenericTypeIndicator<List<String>>() {})

                                // If there are existing comments, add the new comment to the list
                                val updatedLikes = existingLikes?.toMutableList() ?: mutableListOf()
                                if (userID in updatedLikes) {
                                    // Remove user name from the list
                                    updatedLikes.remove(userID)
                                    removeLikeCountFromUser(
                                        listOf(postKey, commentKey, replyKey),
                                        "reply"
                                    )
                                    // Update the entire list of likes
                                    likeRef.setValue(updatedLikes)
                                } else {
                                    // Add user name that like to list
                                    updatedLikes.add(userID)
                                    addLikeCountToUser(
                                        listOf(postKey, commentKey, replyKey),
                                        "reply"
                                    )
                                    // Update the entire list of likes
                                    likeRef.setValue(updatedLikes)
                                }
                            }.addOnFailureListener { exception ->
                                Log.e("Firebase", "Error getting existing comments", exception)
                            }
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting existing comments", exception)
                }
            } else {
                Log.e("Firebase", "Comment not found")
            }
        }
    }

    private fun addLikeCountToUser(keys: List<String>, type: String) {
        var module: String
        val postRef = myRef.child("user-posts").child(keys[0])
        postRef.get().addOnSuccessListener { dataSnapshot ->
            module = dataSnapshot.child("module").getValue(String::class.java).toString()
            when (type) {
                "post" -> {
                    val likedPostRef = myRef.child("user-posts").child(keys[0])
                    likedPostRef.get().addOnSuccessListener {
                        val userID = it.child("userID").getValue(String::class.java)
                        val likeCountRef =
                            myRef.child("Users").child(userID.toString()).child("likeCount")
                                .child(module)
                        likeCountRef.get().addOnSuccessListener { dataSnapshot ->
                            val existingLikeCount = dataSnapshot.getValue(Int::class.java)
                            val updatedLikeCount = existingLikeCount?.plus(1) ?: 1
                            likeCountRef.setValue(updatedLikeCount)
                        }.addOnFailureListener { exception ->
                            Log.e("Firebase", "Error getting like count", exception)
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("Firebase", "Error getting post", exception)
                    }
                }

                "comment" -> {
                    getCommentIndex(keys[0], keys[1]) { commentIndex ->
                        if (commentIndex != -1) {
                            val commentRef =
                                myRef.child("user-posts").child(keys[0]).child("comments")
                                    .child(commentIndex.toString())
                            commentRef.get().addOnSuccessListener { dataSnapshot ->
                                val userID =
                                    dataSnapshot.child("userID").getValue(String::class.java)
                                val likeCountRef =
                                    myRef.child("Users").child(userID.toString()).child("likeCount")
                                        .child(module)
                                likeCountRef.get().addOnSuccessListener {
                                    val existingLikeCount = it.getValue(Int::class.java)
                                    val updatedLikeCount = existingLikeCount?.plus(1) ?: 1
                                    likeCountRef.setValue(updatedLikeCount)
                                }.addOnFailureListener { exception ->
                                    Log.e("Firebase", "Error getting like count", exception)
                                }
                            }.addOnFailureListener { exception ->
                                Log.e("Firebase", "Error getting post", exception)
                            }
                        } else {
                            Log.e("Firebase", "Comment not found")
                        }
                    }
                }

                "reply" -> {
                    getCommentIndex(keys[0], keys[1]) { commentIndex ->
                        if (commentIndex != -1) {
                            val replyRef =
                                myRef.child("user-posts").child(keys[0]).child("comments")
                                    .child(commentIndex.toString()).child("replies")
                            replyRef.get().addOnSuccessListener { dataSnapshot ->
                                val existingReplies =
                                    dataSnapshot.getValue(object :
                                        GenericTypeIndicator<List<Replies>>() {})

                                existingReplies?.forEachIndexed { _, reply ->
                                    if (reply.key == keys[2]) {
                                        val userID = reply.userID
                                        val likeCountRef =
                                            myRef.child("Users").child(userID).child("likeCount")
                                                .child(module)
                                        likeCountRef.get().addOnSuccessListener { dataSnapshot ->
                                            val existingLikeCount =
                                                dataSnapshot.getValue(Int::class.java)
                                            val updatedLikeCount = existingLikeCount?.plus(1) ?: 1
                                            likeCountRef.setValue(updatedLikeCount)
                                        }.addOnFailureListener { exception ->
                                            Log.e("Firebase", "Error getting like count", exception)
                                        }
                                    }
                                }
                            }.addOnFailureListener { exception ->
                                Log.e("Firebase", "Error getting post", exception)
                            }
                        } else {
                            Log.e("Firebase", "Comment not found")
                        }
                    }
                }
            }
        }
    }

    private fun removeLikeCountFromUser(keys: List<String>, type: String) {
        var module: String
        val postRef = myRef.child("user-posts").child(keys[0])
        postRef.get().addOnSuccessListener { dataSnapshot ->
            module = dataSnapshot.child("module").getValue(String::class.java).toString()
            when (type) {
                "post" -> {
                    val likedPostRef = myRef.child("user-posts").child(keys[0])
                    likedPostRef.get().addOnSuccessListener {
                        val userID = it.child("userID").getValue(String::class.java)
                        val likeCountRef =
                            myRef.child("Users").child(userID.toString()).child("likeCount")
                                .child(module)
                        likeCountRef.get().addOnSuccessListener {dataSnapshot ->
                            val existingLikeCount = dataSnapshot.getValue(Int::class.java)
                            val updatedLikeCount = existingLikeCount?.minus(1) ?: 0
                            likeCountRef.setValue(updatedLikeCount)
                        }.addOnFailureListener { exception ->
                            Log.e("Firebase", "Error getting like count", exception)
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("Firebase", "Error getting post", exception)
                    }
                }

                "comment" -> {
                    getCommentIndex(keys[0], keys[1]) { commentIndex ->
                        if (commentIndex != -1) {
                            val commentRef =
                                myRef.child("user-posts").child(keys[0]).child("comments")
                                    .child(commentIndex.toString())
                            commentRef.get().addOnSuccessListener { dataSnapshot ->
                                val userID =
                                    dataSnapshot.child("userID").getValue(String::class.java)
                                val likeCountRef =
                                    myRef.child("Users").child(userID.toString()).child("likeCount")
                                        .child(module)
                                likeCountRef.get().addOnSuccessListener {
                                    val existingLikeCount = it.getValue(Int::class.java)
                                    val updatedLikeCount = existingLikeCount?.minus(1) ?: 0
                                    likeCountRef.setValue(updatedLikeCount)
                                }.addOnFailureListener { exception ->
                                    Log.e("Firebase", "Error getting like count", exception)
                                }
                            }.addOnFailureListener { exception ->
                                Log.e("Firebase", "Error getting post", exception)
                            }
                        } else {
                            Log.e("Firebase", "Comment not found")
                        }
                    }
                }

                "reply" -> {
                    getCommentIndex(keys[0], keys[1]) { commentIndex ->
                        if (commentIndex != -1) {
                            val replyRef =
                                myRef.child("user-posts").child(keys[0]).child("comments")
                                    .child(commentIndex.toString()).child("replies")
                            replyRef.get().addOnSuccessListener { dataSnapshot ->
                                val existingReplies =
                                    dataSnapshot.getValue(object :
                                        GenericTypeIndicator<List<Replies>>() {})

                                existingReplies?.forEachIndexed { _, reply ->
                                    if (reply.key == keys[2]) {
                                        val userID = reply.userID
                                        val likeCountRef =
                                            myRef.child("Users").child(userID).child("likeCount")
                                                .child(module)
                                        likeCountRef.get().addOnSuccessListener { dataSnapshot ->
                                            val existingLikeCount =
                                                dataSnapshot.getValue(Int::class.java)
                                            val updatedLikeCount = existingLikeCount?.minus(1) ?: 0
                                            likeCountRef.setValue(updatedLikeCount)
                                        }.addOnFailureListener { exception ->
                                            Log.e("Firebase", "Error getting like count", exception)
                                        }
                                    }
                                }
                            }.addOnFailureListener { exception ->
                                Log.e("Firebase", "Error getting post", exception)
                            }
                        } else {
                            Log.e("Firebase", "Comment not found")
                        }
                    }
                }
            }
        }
    }


    // Checking Functions //
    /**
     * This function is use to check if the current logged in user has liked the post
     * @param postKey (str): Post Unique Identifier key
     * @return result of the user has liked the post in Boolean
     */
    fun isLikedByUser(postKey: String, onSuccess: (Boolean) -> Unit) {
        val userID = getUserID()
        val likesRef = myRef.child("user-posts").child(postKey).child("likes")
        var isLiked: Boolean

        // Retrieve the list of likes for the postKey
        likesRef.get().addOnSuccessListener { dataSnapshot ->
            val existingLikes =
                dataSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})

            // Check if the username is present in the list of likes
            isLiked = existingLikes?.contains(userID) ?: false

            onSuccess(isLiked)
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error getting existing likes", exception)
        }

    }

    fun isCommentLikedByUser(
        postKey: String,
        commentKey: String,
        onSuccess: (Boolean) -> Unit
    ) {
        val userID = getUserID()
        getCommentIndex(postKey, commentKey) { commentIndex ->
            if (commentIndex != -1) {
                val likeRef = myRef.child("user-posts").child(postKey).child("comments")
                    .child(commentIndex.toString()).child("likes")
                var isLiked: Boolean
                likeRef.get().addOnSuccessListener { dataSnapshot ->
                    val existingLikes =
                        dataSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                    isLiked = existingLikes?.contains(userID) ?: false
                    onSuccess(isLiked)
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting existing comments", exception)
                }

            } else {
                Log.e("Firebase", "Comment not found")
            }
        }


    }

    fun isReplyLikedByUser(
        postKey: String,
        commentKey: String,
        replyKey: String,
        onSuccess: (Boolean) -> Unit
    ) {
        val userID = getUserID()
        getCommentIndex(postKey, commentKey) { commentIndex ->
            if (commentIndex != -1) {
                val replyRef = myRef.child("user-posts").child(postKey).child("comments")
                    .child(commentIndex.toString()).child("replies")
                replyRef.get().addOnSuccessListener { dataSnapshot ->
                    val existingReplies =
                        dataSnapshot.getValue(object : GenericTypeIndicator<List<Replies>>() {})

                    existingReplies?.forEachIndexed { index, reply ->
                        if (reply.key == replyKey) {
                            val likeRef = myRef.child("user-posts").child(postKey).child("comments")
                                .child(commentIndex.toString()).child("replies")
                                .child(index.toString()).child("likes")
                            var isLiked: Boolean
                            likeRef.get().addOnSuccessListener { dataSnapshot ->
                                val existingLikes =
                                    dataSnapshot.getValue(object :
                                        GenericTypeIndicator<List<String>>() {})
                                isLiked = existingLikes?.contains(userID) ?: false
                                onSuccess(isLiked)
                            }.addOnFailureListener { exception ->
                                Log.e("Firebase", "Error getting existing comments", exception)
                            }
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting existing comments", exception)
                }
            } else {
                Log.e("Firebase", "Comment not found")
            }
        }
    }

    fun getLeaderboardData(module: String, onDataChanged: (List<Pair<String, Int>>) -> Unit) {
        val userRef = myRef.child("Users")
        // Get all users' like count for the specified module
        userRef.get().addOnSuccessListener { dataSnapshot ->
            val users = mutableListOf<Pair<String, Int>>()
            for (userSnapshot in dataSnapshot.children) {
                val userID = userSnapshot.key
                val likeCount =
                    userSnapshot.child("likeCount").child(module).getValue(Int::class.java)
                val userName = userSnapshot.child("userName").getValue(String::class.java)
                if (userID != null && likeCount != null) {
                    users.add(Pair(userName ?: "", likeCount))
                }
            }
            users.sortByDescending { it.second }
            Log.d("Firebase", "Leaderboard data: $users")
            onDataChanged(users)
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error getting user like count", exception)
        }
    }

    /**
     * This function is use to check if the user has authenticated
     * @return result of whether the user is login
     */
    fun isAuth(): Boolean {
        val currentUser = auth.currentUser
        return currentUser != null
    }

    fun isAdminAuth(): Boolean {
        val currentUser = auth.currentUser
        return currentUser?.uid == "Sa60iAQsf6gdjqw8qEaisHkti8B3"
    }

}
