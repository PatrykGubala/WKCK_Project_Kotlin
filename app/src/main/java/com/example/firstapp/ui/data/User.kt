package com.example.firstapp.ui.data

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName

data class User(
                var profileImageUrl: String? = null,
                var userId: String? = null,
                val email: String? = null,
                val username: String? = null,
                val usernameCode: String? = null,
                val status: String? = null,
                val image: String? = null,
                val friends: List<String>? = null,
                val conversations: List<DocumentReference>? = null
)