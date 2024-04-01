package com.example.firstapp.ui.profile.requests

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentProfileFriendsRequestsBinding
import com.example.firstapp.ui.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsRequestsFragment : Fragment() {

    private var _binding: FragmentProfileFriendsRequestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var friendsRequestsAdapter: FriendsRequestsAdapter
    private val friendsRequestsList = mutableListOf<User>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileFriendsRequestsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        userId?.let { fetchFriends(it) }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack(R.id.profileFragment, false)
        }
        return root
    }

    private fun setupRecyclerView() {
        friendsRequestsAdapter =
            FriendsRequestsAdapter(friendsRequestsList, this::acceptFriendRequest)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsRequestsAdapter
        }
    }

    private fun fetchFriends(userId: String) {
        firestore.collection("Users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    val friendsRequests = user.friendsRequests
                    if (!friendsRequests.isNullOrEmpty()) {
                        for (friendRequestId in friendsRequests) {
                            fetchUserData(friendRequestId)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsFragment", "Error fetching friends", exception)
            }
    }

    private fun fetchUserData(userId: String) {
        firestore.collection("Users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    friendsRequestsList.add(user)
                    friendsRequestsAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsRequestsFragment", "Error fetching user data", exception)
            }
    }

    private fun acceptFriendRequest(user: User) {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUser != null) {
            val currentUserRef =
                FirebaseFirestore.getInstance().collection("Users").document(currentUser)
            currentUserRef.get().addOnSuccessListener { documentSnapshot ->
                val currentUser = documentSnapshot.toObject(User::class.java)
                currentUser?.let {
                    val updatedFriendRequests = currentUser.friendsRequests?.toMutableList()
                    updatedFriendRequests?.remove(user.userId)
                    currentUserRef.update("friendsRequests", updatedFriendRequests)
                        .addOnSuccessListener {
                            Log.d(TAG, "Friend request removed successfully from current user")
                            friendsRequestsList.remove(user)
                            friendsRequestsAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error removing friend request from current user", e)
                        }

                    val updatedFriends = currentUser.friends?.toMutableList()
                    updatedFriends?.add(user.userId!!) // Add friend
                    currentUserRef.update("friends", updatedFriends)
                        .addOnSuccessListener {
                            Log.d(TAG, "Friend added successfully to current user's friends list")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error adding friend to current user's friends list", e)
                        }
                }
            }?.addOnFailureListener { e ->
                Log.e(TAG, "Error fetching current user data", e)
            }

            val userRef =
                FirebaseFirestore.getInstance().collection("Users").document(user.userId!!)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                val friendData = documentSnapshot.toObject(User::class.java)
                val updatedFriendRequests = friendData?.friendsRequests?.toMutableList()
                updatedFriendRequests?.remove(currentUser)
                userRef.update("friendsRequests", updatedFriendRequests)
                    .addOnSuccessListener {
                        Log.d(TAG, "Friend request removed successfully from friend")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error removing friend request from friend", e)
                    }
            }?.addOnFailureListener { e ->
                Log.e(TAG, "Error fetching friend data", e)
            }
        } else {
            Log.e(TAG, "Current user is null")
        }
    }
}