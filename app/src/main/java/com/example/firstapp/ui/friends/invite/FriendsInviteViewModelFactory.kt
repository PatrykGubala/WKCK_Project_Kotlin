package com.example.firstapp.ui.friends.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.firstapp.ui.friends.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsInviteViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val friendsViewModel: FriendsViewModel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendsInviteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendsInviteViewModel(firestore, auth, friendsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
