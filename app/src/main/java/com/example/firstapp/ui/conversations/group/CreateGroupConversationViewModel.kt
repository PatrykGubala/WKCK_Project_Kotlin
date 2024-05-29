package com.example.firstapp.ui.conversations.group

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firstapp.ui.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateGroupConversationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _groupCreated = MutableLiveData<Boolean>()
    val groupCreated: LiveData<Boolean> get() = _groupCreated

    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>> get() = _friends

    private val _conversationImageUrl = MutableLiveData<String>()
    val conversationImageUrl: LiveData<String> get() = _conversationImageUrl

    init {
        loadFriends()
    }

    fun createGroupConversation(
        groupName: String,
        friendsList: List<String?>,
        conversationImageUrl: String,
    ) {
        val currentUser = auth.currentUser ?: return

        val participants = friendsList + currentUser.uid

        val conversationData =
            hashMapOf(
                "name" to groupName,
                "participants" to friendsList + currentUser.uid,
                "conversationImageUrl" to conversationImageUrl,
                "messageIds" to listOf<String>(),
                "status" to "group",
            )

        firestore.collection("Conversations").add(conversationData).addOnSuccessListener { documentReference ->
            val conversationId = documentReference.id
            _groupCreated.postValue(true)
            updateUsersConversations(participants, conversationId)
        }.addOnFailureListener {
            _groupCreated.postValue(false)
        }
    }

    fun setConversationImageUrl(url: String) {
        _conversationImageUrl.postValue(url)
    }

    private fun updateUsersConversations(
        participants: List<String?>,
        conversationId: String,
    ) {
        val conversationRef = firestore.document("Conversations/$conversationId")

        participants.forEach { userId ->
            userId?.let {
                val userDocRef = firestore.collection("Users").document(it)
                userDocRef.update("conversations", FieldValue.arrayUnion(conversationRef))
                    .addOnSuccessListener {
                        Log.d("UpdateUserConversations", "User $userId updated with conversation reference $conversationId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UpdateUserConversations", "Error updating user $userId with conversation reference $conversationId", e)
                    }
            }
        }
    }

    private fun loadFriends() {
        val currentUser = auth.currentUser ?: return

        firestore.collection("Users")
            .whereArrayContains("friends", currentUser.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val friendsList = querySnapshot.documents.mapNotNull { it.toObject(User::class.java) }
                if (friendsList.isEmpty()) {
                    Log.d("CreateGroupConversationVM", "No friends found")
                } else {
                    Log.d("CreateGroupConversationVM", "Friends loaded: ${friendsList.size}")
                }
                _friends.postValue(friendsList)
            }
            .addOnFailureListener { exception ->
                Log.e("CreateGroupConversationVM", "Error loading friends", exception)
                _friends.postValue(emptyList())
            }
    }
}
