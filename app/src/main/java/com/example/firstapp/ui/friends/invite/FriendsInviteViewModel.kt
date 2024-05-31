package com.example.firstapp.ui.friends.invite

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firstapp.ui.data.User
import com.example.firstapp.ui.friends.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FriendsInviteViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val friendsViewModel: FriendsViewModel,
) : ViewModel() {
    private val _friends = MutableLiveData<List<User>?>()
    val friends: LiveData<List<User>?> = _friends

    private val userId = auth.currentUser?.uid

    init {
        loadFriends()
    }

    fun sendFriendRequest(
        userId: String,
        friendUserId: String,
    ) {
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
        _friends.value = null

        userId?.let { uid ->
            val username = searchTerm.substringBefore("#", "")
            val usernameCode = searchTerm.substringAfter("#", "")

            var searchQuery: Query = firestore.collection("Users")

            if (username.isNotBlank()) {
                searchQuery = searchQuery.whereEqualTo("username", username)
            }

            if (usernameCode.isNotBlank()) {
                searchQuery =
                    searchQuery.whereGreaterThanOrEqualTo("usernameCode", usernameCode)
                        .whereLessThanOrEqualTo("usernameCode", usernameCode + "\uf8ff")
            }

            searchQuery.get()
                .addOnSuccessListener { querySnapshot ->
                    val friendsList = mutableListOf<User>()
                    val currentUserFriends = friendsViewModel.friends.value.orEmpty()

                    for (document in querySnapshot.documents) {
                        val friend = document.toObject(User::class.java)
                        if (friend?.userId != userId &&
                            !currentUserFriends.any { it.userId == friend?.userId } &&
                            !friendAlreadyExists(friend?.userId)
                        ) {
                            friend?.let {
                                friendsList.add(it)
                            }
                        }
                    }
                    Log.d("FriendsInviteViewModel", "Filtered friends list: $friendsList")
                    _friends.value = friendsList
                }
                .addOnFailureListener { e ->
                    Log.e("FriendsInviteViewModel", "Error searching friends", e)
                }
        }
    }

    private fun loadFriends() {
        friendsViewModel.friends.observeForever { friendsList ->
            friendsList?.let {
                _friends.value = friendsList
            }
        }
        friendsViewModel.fetchFriends()
    }

    private fun friendAlreadyExists(friendId: String?): Boolean {
        return _friends.value?.any { it.userId == friendId } ?: false
    }
}
