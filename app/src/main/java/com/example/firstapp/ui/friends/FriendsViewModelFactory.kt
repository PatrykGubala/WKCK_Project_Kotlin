package com.example.firstapp.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendsViewModel(firestore, auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
