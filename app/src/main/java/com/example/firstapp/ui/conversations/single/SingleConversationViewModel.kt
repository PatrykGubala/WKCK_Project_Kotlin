package com.example.firstapp.ui.conversations.single

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firstapp.ui.data.Message
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SingleConversationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun loadMessages(conversationId: String) {
        _loading.value = true

        firestore.collection("Conversations").document(conversationId)
            .addSnapshotListener { conversationDoc, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting conversation document", error)
                    _loading.value = false
                    return@addSnapshotListener
                }

                val messageIds = conversationDoc?.get("messageIds") as? List<String> ?: emptyList()

                if (messageIds.isNullOrEmpty()) {
                    _loading.value = false
                    return@addSnapshotListener
                }

                val batchSize = 30
                val numBatches = (messageIds.size + batchSize - 1) / batchSize

                val messagesList = mutableListOf<Message>()

                _loading.postValue(true)

                for (i in 0 until numBatches) {
                    val startIndex = i * batchSize
                    val endIndex = minOf((i + 1) * batchSize, messageIds.size)

                    val batchIds = messageIds.subList(startIndex, endIndex)

                    firestore.collection("Messages")
                        .whereIn(FieldPath.documentId(), batchIds)
                        .orderBy("timestamp")
                        .get()
                        .addOnSuccessListener { messagesSnapshot ->
                            for (doc in messagesSnapshot.documents) {
                                val message = doc.toObject(Message::class.java)
                                message?.let {
                                    messagesList.add(it)
                                }
                            }

                            if (i == numBatches - 1) {
                                _messages.postValue(messagesList)
                                _loading.postValue(false)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error fetching messages", e)
                            _loading.postValue(false)
                        }
                }
            }
    }

    fun sendMessage(
        message: Message,
        conversationId: String,
    ) {
        val messageRef = firestore.collection("Messages").document()
        messageRef.set(message)
            .addOnSuccessListener {
                val updatedMessages = _messages.value?.toMutableList() ?: mutableListOf()
                updatedMessages.add(message)
                _messages.value = updatedMessages

                firestore.collection("Conversations").document(conversationId)
                    .update("messageIds", FieldValue.arrayUnion(messageRef.id))
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error updating conversation", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error sending message", exception)
            }
    }

    companion object {
        private const val TAG = "SingleConversationViewModel"
    }
}
