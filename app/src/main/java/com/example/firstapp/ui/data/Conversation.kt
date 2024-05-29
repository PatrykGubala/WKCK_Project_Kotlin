package com.example.firstapp.ui.data
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class Conversation(
    val conversationId: String? = null,
    val name: String? = null,
    val conversationImageUrl: String? = null,
    val status: String? = null,
    val participants: List<String>? = null,
    val messageIds: List<String>? = null,
    var messages: List<Message>? = null,
) {
    constructor() : this(null, null, null, null, ArrayList(), ArrayList())

    suspend fun populateMessagesFromIds() {
        if (messageIds.isNullOrEmpty()) {
            messages = emptyList()
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val messageDocs = mutableListOf<Message>()

        withContext(Dispatchers.IO) {
            for (messageId in messageIds) {
                try {
                    val messageDoc = firestore.collection("Messages").document(messageId).get().await()
                    val message = messageDoc.toObject(Message::class.java)
                    message?.let { messageDocs.add(it) }
                } catch (e: Exception) {
                    println("Error fetching message with ID: $messageId - ${e.message}")
                }
            }
        }

        messages = messageDocs
    }
}
