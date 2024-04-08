package com.example.query_overflow

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FirebaseHelperTest{
    private lateinit var firebaseHelperTesting: FirebaseHelper


    @Before
    fun setUp() {
        // Initialize FirebaseHelper with a mock Firebase database
        runOnUiThread{
            firebaseHelperTesting = FirebaseHelper()
            firebaseHelperTesting.signOut()
        }
    }

    @After
    fun tearDown(){
        runOnUiThread{
            firebaseHelperTesting.signOut()
            firebaseHelperTesting = FirebaseHelper()
        }
    }

    @Test
    fun signIn() {
        val email = "test_user@test.com"
        val password = "P@ssw0rd"

        firebaseHelperTesting.signIn(email, password, onSuccess = {assert(true)}, onError = {assert(false)})

        assert(firebaseHelperTesting.isAuth())
    }

    @Test
    fun getUserName() {
        val expectedUserName = "John Loh"

        // Assuming getUserName() returns a String
        val userName = firebaseHelperTesting.getUserName()

        assertEquals(expectedUserName, userName)
        
    }

    @Test
    fun getUserID() {
        val expectedUID = "2g8NEGDDBTbf1SuohdwemzuV18W2"
        val uID = firebaseHelperTesting.getUserID()

        assertEquals(expectedUID, uID)

    }

}