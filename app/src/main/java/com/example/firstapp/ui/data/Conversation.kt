package com.example.firstapp.ui.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference



data class Conversation(
    val conversationId: String? = null,
    val status: String? = "solo",
    val participants: List<String>? = null,
    val messages: List<Message>? = null,
    val lastMessage: String? = null,
    val lastMessageSender: String?,
    val lastMessageTime: Timestamp?
) {
    constructor() : this(null,null, ArrayList(), ArrayList(), null, null, null)
}