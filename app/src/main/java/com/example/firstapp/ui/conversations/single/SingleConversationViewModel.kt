package com.example.firstapp.ui.conversations.single
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firstapp.ui.data.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class SingleConversationViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    fun loadMessages(conversationId: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("Conversations").document(conversationId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val conversationData = documentSnapshot.data
                val messages = conversationData?.get("messages") as? List<HashMap<String, Any>>

                val messageList = mutableListOf<Message>()
                messages?.forEach { messageData ->
                    val message = Message(
                        messageData["message"] as? String,
                        messageData["senderId"] as? String,
                        messageData["timestamp"] as? Timestamp
                    )
                    messageList.add(message)
                }
                _messages.value = messageList
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }
}