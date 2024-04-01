package com.example.firstapp.ui.friends.invite

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firstapp.ui.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FriendsInviteViewModel : ViewModel() {

    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>> = _friends

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    fun sendFriendRequest(userId: String, friendUserId: String) {
        firestore.collection("Users").document(friendUserId)
            .update("friendsRequests", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                Log.d("FriendsInviteViewModel", "Friend request sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FriendsInviteViewModel", "Error sending friend request", e)
            }
    }
    fun searchFriends(searchTerm: String) {
        userId?.let { uid ->
            val username = searchTerm.substringBefore("#", "")
            val usernameCode = searchTerm.substringAfter("#", "")

            var searchQuery: Query = firestore.collection("Users")

            if (username.isNotBlank()) {
                searchQuery = searchQuery.whereEqualTo("username", username)
            }

            if (usernameCode.isNotBlank()) {
                searchQuery = searchQuery.whereGreaterThanOrEqualTo("usernameCode", usernameCode)
                    .whereLessThanOrEqualTo("usernameCode", usernameCode + "\uf8ff")
            }

            searchQuery.get()
                .addOnSuccessListener { querySnapshot ->
                    val friendsList = mutableListOf<User>()
                    for (document in querySnapshot.documents) {
                        val friend = document.toObject(User::class.java)
                        if (friend?.userId != userId) {
                            friend?.let {
                                friendsList.add(it)
                            }
                        }
                    }
                    _friends.value = friendsList
                }
                .addOnFailureListener { e ->
                    Log.e("FriendsInviteViewModel", "Error searching friends", e)
                }
        }
    }
}
