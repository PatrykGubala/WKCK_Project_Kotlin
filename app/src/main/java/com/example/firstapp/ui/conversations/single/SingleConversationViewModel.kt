package com.example.firstapp.ui.conversations.single

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firstapp.ui.data.Message
import com.google.firebase.firestore.FirebaseFirestore

class SingleConversationViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    fun setMessages(messages: List<Message>) {
        _messages.value = messages
    }
    fun loadMessages(messageIds: List<String>) {
        _loading.value = true
        val loadedMessages = mutableListOf<Message>()

        messageIds.forEach { messageId ->
            firestore.collection("Messages").document(messageId)
                .get()
                .addOnSuccessListener { messageDocument ->
                    val message = messageDocument.toObject(Message::class.java)
                    message?.let {
                        loadedMessages.add(it)
                        _messages.value = loadedMessages
                    }
                }
                .addOnFailureListener { exception ->
                }
                .addOnCompleteListener {
                    _loading.value = false
                }
        }
    }
}
