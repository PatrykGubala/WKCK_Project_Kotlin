package com.example.firstapp.ui.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.databinding.FragmentFriendsBinding
import com.example.firstapp.ui.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private lateinit var friendsAdapter: FriendsAdapter
    private val friendsList = mutableListOf<User>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        userId?.let { fetchFriends(it) }

        return root
    }

    private fun setupRecyclerView() {
        friendsAdapter = FriendsAdapter(friendsList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsAdapter
        }
    }

    private fun fetchFriends(userId: String) {
        firestore.collection("Users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    val friends = user.friends
                    if (friends != null && friends.isNotEmpty()) {
                        for (friendId in friends) {
                            fetchUserData(friendId)
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
                    friendsList.add(user)
                    friendsAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsFragment", "Error fetching user data", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
