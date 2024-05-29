package com.example.firstapp.ui.friends

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firstapp.ui.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FriendsViewModel : ViewModel() {
    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>> = _friends

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private val _searchQuery = MutableLiveData<String>("")
    private val _filteredFriends =
        MediatorLiveData<List<User>>().apply {
            addSource(_friends) { filterFriends() }
            addSource(_searchQuery) { filterFriends() }
        }
    private var userSnapshotListener: ListenerRegistration? = null
    val filteredFriends: LiveData<List<User>> = _filteredFriends

    private fun filterFriends() {
        val query = _searchQuery.value ?: ""
        _filteredFriends.value =
            _friends.value?.filter {
                it.username!!.contains(query, ignoreCase = true) || it.usernameCode!!.contains(query, ignoreCase = true)
            }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun fetchFriends() {
        userId?.let { uid ->
            userSnapshotListener =
                firestore.collection("Users").document(uid)
                    .addSnapshotListener { documentSnapshot, exception ->
                        if (exception != null) {
                            Log.e("FriendsViewModel", "Error fetching friends", exception)
                            return@addSnapshotListener
                        }

                        documentSnapshot?.let { snapshot ->
                            val user = snapshot.toObject(User::class.java)
                            user?.let {
                                val friends = user.friends
                                val friendsList = mutableListOf<User>()

                                friends?.let { friendIds ->
                                    val pendingFriendsCount = friendIds.size
                                    var fetchedFriendsCount = 0

                                    for (friendId in friendIds) {
                                        firestore.collection("Users").document(friendId)
                                            .get()
                                            .addOnSuccessListener { friendSnapshot ->
                                                val friend = friendSnapshot.toObject(User::class.java)
                                                friend?.let {
                                                    friendsList.add(it)
                                                }
                                                fetchedFriendsCount++
                                                if (fetchedFriendsCount == pendingFriendsCount) {
                                                    _friends.value = friendsList
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("FriendsViewModel", "Error fetching friend data", e)
                                                fetchedFriendsCount++
                                                if (fetchedFriendsCount == pendingFriendsCount) {
                                                    _friends.value = friendsList
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userSnapshotListener?.remove()
    }
}
