package com.example.query_overflow.dashboard

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.example.query_overflow.R
import com.example.query_overflow.firebaseHelper
import java.io.File
import kotlin.math.abs

@Composable
fun YourPositionCard(user: User, position: Int) {

    var downloadedFile by remember { mutableStateOf(File("")) }

    LaunchedEffect(Unit) {
        firebaseHelper.getProfilePhoto(firebaseHelper.getUserID()) { file ->
            downloadedFile = file
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        color = if (position == 1) {
            Color(0xFFFFD700)
        } else if (position == 2) {
            Color(0xFFC0C0C0)
        } else if (position == 3) {
            Color(0xFFCD7F32)
        } else {
            Color(0xFFDCDCDC)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (position == 1) {
                Image(
                    painter = painterResource(id = R.drawable.trophy_icon),
                    contentDescription = "Champion",
                    modifier = Modifier
                        .size(30.dp)
                        .padding(5.dp)
                )
            } else {
                Text(
                    text = "$position",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            if (downloadedFile.toString() != "") {
                Image(
                    painter = rememberAsyncImagePainter(downloadedFile),
                    contentDescription = null, // Provide content description if needed
                    modifier = Modifier
                        .size(60.dp)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Black, CircleShape),

                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(5.dp)
                        .clip(CircleShape),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "You",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(5.dp)
            )
            Text(
                text = "${user.points} points",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}

@Composable
fun LeaderboardScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
) {
    // Sample list of modules
    val modules = listOf("INF2007 Mobile App Development", "CSC3105 Data Analytics", "CSC2106 IoT")
    var expandedModule = remember { mutableStateOf(false) } // Dropdown menu state
    var selectedModule =
        remember { mutableStateOf("INF2007 Mobile App Development") } // Selected module
    var expandedSort = remember { mutableStateOf(false) } // Dropdown menu state
    var selectedSort = remember { mutableStateOf("Select an option") } // Selected module
    // State for storing leaderboard data
    var leaderboardData by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    // Call getLeaderboardData when the selected module changes
    LaunchedEffect(selectedModule) {
        firebaseHelper.getLeaderboardData(selectedModule.value) { data ->
            leaderboardData = data
        }

        Log.d("LeaderboardScreen", "Leaderboard data: $leaderboardData")
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically // Align items vertically center
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

            // Heading
            Text(
                text = "Leaderboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .wrapContentSize(Alignment.Center) // This centers the text inside its container
                    .padding(vertical = 35.dp)

            )
        }

        //Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Module: ",
                modifier = Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.bodyMedium
            )
            // Dropdown menu button
            Row {
                Text(
                    text = selectedModule.value,
                    modifier = Modifier
                        .padding(end = 30.dp)
                        .padding(vertical = 8.dp)
                        .clickable { expandedModule.value = !expandedModule.value },
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown",
                    modifier = Modifier.clickable { expandedModule.value = !expandedModule.value }
                )
                DropdownMenu(
                    expanded = expandedModule.value,
                    onDismissRequest = { expandedModule.value = false },
                ) {
                    for (module in modules) {
                        DropdownMenuItem(
                            text = { Text(text = module) },
                            onClick = {
                                selectedModule.value = module
                                expandedModule.value = false
                                firebaseHelper.getLeaderboardData(selectedModule.value) { data ->
                                    leaderboardData = data
                                }
                            }
                        )
                    }
                }
            }
        }


        Column(modifier = Modifier.padding(8.dp)) {

            // Loop through the leaderboard data
            // Drop all users with 0 points
            for (user in leaderboardData) {
                if (user.second == 0) {
                    leaderboardData = leaderboardData.dropWhile { it.second == 0 }
                }
            }

            // Assuming the first three items in the list are the top users
            val topThreeUsers = leaderboardData.take(3)
            val restOfUsers = leaderboardData.drop(3)

            TopThreeUsers(topThreeUsers.map {
                User(
                    it.first,
                    it.second
                )
            }) // Composable for the top three users

            Divider(Modifier.padding(vertical = 8.dp))
            // Card to display your position

            leaderboardData.find { it.first == firebaseHelper.getUserName() }?.let {
                val user = User(it.first, it.second)
                val index = leaderboardData.indexOf(it)
                YourPositionCard(user, index + 1)
            }


            if (leaderboardData.isEmpty()) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                // Remaining user
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(restOfUsers.size) { index ->
                        // Adjust index based on the number of top users
                        val (name, points) = restOfUsers[index]
                        if (points > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 4}", // Position is now from the user data
                                    modifier = Modifier.padding(horizontal = 40.dp)
                                )
                                Text(
                                    text = name,
                                    modifier = Modifier.padding(horizontal = 40.dp)
                                )
                                Text(
                                    text = "$points points",
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun TopThreeUsers(topUsers: List<User>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        topUsers.forEachIndexed { index, user ->
            TopUserCard(index + 1, user)
        }
    }
}

@Composable
fun TopUserCard(position: Int, user: User) {

    var downloadedFile by remember { mutableStateOf(File("")) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        firebaseHelper.downloadProfilePhotoFromUserName(user.name) { file ->
            downloadedFile = file
            isLoading = false
        }
    }

    val barHeight = 200 // This should be your maximum bar height
    val baseHeight = 100 // This is the minimum height that every bar will have
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Image(
            imageVector = Icons.Default.Person,
            contentDescription = "${user.name}'s profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )


        Text(text = user.name, fontWeight = FontWeight.Bold)
        Text(text = "${user.points} points")
        Box(
            contentAlignment = Alignment.BottomStart, // Align the content to the bottom of the box
            modifier = Modifier
                .height(barHeight.dp)
                .padding(4.dp)
        ) {
            Card(
                colors = when (position) {
                    1 -> CardColors(
                        //Color(0xFFDCDCDC),
                        Color(0xFFFFD700),
                        Color.Black,
                        //Color(0xFFFFD700),
                        Color(0xFFFFD700),
                        Color.Black,
                    ) // Gold
                    2 -> CardColors(
                        //Color(0xFFDCDCDC),
                        Color(0xFFC0C0C0),
                        Color.Black,
                        //Color(0xFFDCDCDC),
                        Color(0xFFC0C0C0),
                        Color.Black,
                    ) // Silver
                    else -> CardColors(
                        //Color(0xFFDCDCDC),
                        Color(0xFFCD7F32),
                        Color.Black,
                        //Color(0xFFDCDCDC),
                        Color(0xFFCD7F32),
                        Color.Black,
                    ) // Bronze
                },
                modifier = Modifier
                    .width(80.dp)
                    .height((baseHeight + abs(position - 4) * 25).dp) // Calculate the height based on position
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()

                ) {
                    if (position == 1) {
                        Image(
                            painterResource(id = R.drawable.trophy_icon),
                            contentDescription = "Champion",
                        )
                    } else {
                        /*Spacer(modifier = Modifier
                        .weight(1f))*/ // Pushes the text to the bottom
                        Text(text = "${position}", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (baseHeight / 2).dp) // Center the avatar and name just above the card
            ) {
            }
        }
    }
}


// Data class to hold user data
data class User(
    val name: String,
    val points: Int,
)