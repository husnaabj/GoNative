package com.example.gonative

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.gonative.AuthState
import com.example.gonative.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.database


@Composable
fun Profile(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    val authState = authViewModel.authState.observeAsState()
    val userId = authViewModel.getCurrentUser()?.uid // Fetch current user ID
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phonenumber by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId != null) {
            // Fetch user data from Firebase
            val database = Firebase.database
            val userRef = database.getReference("users").child(userId)

            userRef.get().addOnSuccessListener { snapshot ->
                username = snapshot.child("username").getValue(String::class.java) ?: ""
                email = snapshot.child("email").getValue(String::class.java) ?: ""
                phonenumber = snapshot.child("phone").getValue(String::class.java) ?: ""

            }.addOnFailureListener {
                Toast.makeText(context, "Error fetching data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(authState.value){
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.profile), // Replace with your image resource
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = {},
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Makes the field uneditable
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Makes the field uneditable
            )

            Spacer(modifier = Modifier.height(8.dp))

            // phone number Field
            OutlinedTextField(
                value = phonenumber,
                onValueChange = {},
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Makes the field uneditable
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Out Button
            Button(
                onClick = { /* Sign out logic */ authViewModel.signout()},
                //colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8E6E6)),
                /*modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)*/
            ) {
                Text(text = "Sign Out", color = Color.Black, fontSize = 16.sp)
            }
        }
    }
}
