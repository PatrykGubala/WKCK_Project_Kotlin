package com.example.firstapp.ui.data

import com.google.firebase.firestore.DocumentReference



data class Conversation(
    val participants: List<String>? = null,
    val messages: List<Message>? = null,
    val lastMessage: String? = null,
    val lastMessageSender: String?
) {
    constructor() : this(ArrayList(), ArrayList(), null, null)
}