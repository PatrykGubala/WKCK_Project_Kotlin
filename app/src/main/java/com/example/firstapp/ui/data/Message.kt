package com.example.firstapp.ui.data

import com.google.firebase.Timestamp

data class Message(
    val message: String? = null,
    val senderId: String? = null,
    val timestamp: Timestamp? = null
)