package com.example.firstapp.ui.conversations.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateGroupConversationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _groupCreated = MutableLiveData<Boolean>()
    val groupCreated: LiveData<Boolean> get() = _groupCreated

    fun createGroupConversation(
        groupName: String,
        friendsList: List<String>,
    ) {
        val currentUser = auth.currentUser ?: return

        val conversationData =
            hashMapOf(
                "name" to groupName,
                "participants" to friendsList + currentUser.uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "createdBy" to currentUser.uid,
                "status" to "group",
            )

        firestore.collection("Conversations")
            .add(conversationData)
            .addOnSuccessListener {
                _groupCreated.postValue(true)
            }
            .addOnFailureListener {
                _groupCreated.postValue(false)
            }
    }
}
