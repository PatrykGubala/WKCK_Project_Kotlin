package com.example.firstapp.ui.data


data class Conversation(
    val conversationId: String? = null,
    val status: String? = "solo",
    val participants: List<String>? = null,
    val messageIds: List<String>? = null,
    var messages: List<Message>? = null,
) {
    constructor() : this(null,null, ArrayList(), ArrayList())
}