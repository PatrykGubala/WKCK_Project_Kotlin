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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsRequestsFragment : Fragment() {
    private var _binding: FragmentProfileFriendsRequestsBinding? = null
    private val binding get() = _binding!!

    private var savedNavBarColor: Int = 0
    private lateinit var bottomNavView: BottomNavigationView

    private lateinit var friendsRequestsAdapter: FriendsRequestsAdapter
    private val friendsRequestsList = mutableListOf<User>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        savedNavBarColor = requireActivity().window.navigationBarColor
        requireActivity().window.navigationBarColor = requireContext().getColor(R.color.black)
        bottomNavView = requireActivity().findViewById(R.id.bottomNavView) ?: return
        if (bottomNavView.visibility != View.GONE) {
            bottomNavView.visibility = View.GONE
        }
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
            val currentUserRef = firestore.collection("Users").document(currentUser)
            val friendRef = firestore.collection("Users").document(user.userId!!)

            firestore.runTransaction { transaction ->
                val currentUserDoc = transaction.get(currentUserRef)
                val friendDoc = transaction.get(friendRef)

                val currentUserFriends = (currentUserDoc["friends"] as? MutableList<String>) ?: mutableListOf()
                val friendFriends = (friendDoc["friends"] as? MutableList<String>) ?: mutableListOf()

                if (!currentUserFriends.contains(user.userId)) {
                    currentUserFriends.add(user.userId!!)
                }
                if (!friendFriends.contains(currentUser)) {
                    friendFriends.add(currentUser)
                }

                transaction.update(currentUserRef, "friends", currentUserFriends)
                transaction.update(friendRef, "friends", friendFriends)

                val currentUserFriendRequests = (currentUserDoc["friendsRequests"] as? MutableList<String>) ?: mutableListOf()
                currentUserFriendRequests.remove(user.userId)
                transaction.update(currentUserRef, "friendsRequests", currentUserFriendRequests)

                val friendFriendRequests = (friendDoc["friendsRequests"] as? MutableList<String>) ?: mutableListOf()
                friendFriendRequests.remove(currentUser)
                transaction.update(friendRef, "friendsRequests", friendFriendRequests)

                null
            }.addOnSuccessListener {
                Log.d(TAG, "Transaction success: both friends lists and friend requests updated")
                friendsRequestsList.remove(user)
                friendsRequestsAdapter.notifyDataSetChanged()
            }.addOnFailureListener { e ->
                Log.e(TAG, "Transaction failure: ", e)
            }
        } else {
            Log.e(TAG, "Current user is null")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavView.visibility = View.VISIBLE
        requireActivity().window.navigationBarColor = savedNavBarColor
    }
}
