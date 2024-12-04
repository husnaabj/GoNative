package com.example.gonative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _userData = MutableLiveData<UserData?>()
    val userData: LiveData<UserData?> = _userData

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        //val currentUser = auth.currentUser
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
            fetchUserData()
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                    fetchUserData()
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signup(email: String, password: String, username: String, phonenumb: String) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || phonenumb.isEmpty()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserData(userId, email, username, phonenumb)
                    }
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }


    private fun saveUserData(userId: String, email: String, username: String, phonenumb: String) {
        val userMap = mapOf(
            "username" to username,
            "email" to email,
            "phone" to phonenumb // Save phone number
        )
        database.getReference("users").child(userId).setValue(userMap)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _authState.value = AuthState.Error("Failed to save user data")
                }
            }
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("users").child(userId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val data = task.result?.getValue(UserData::class.java)
                    _userData.value = data
                } else {
                    _userData.value = null
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        //_userData.value = null
    }

    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

}

data class UserData(
    val username: String? = null,
    val email: String? = null
)

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
