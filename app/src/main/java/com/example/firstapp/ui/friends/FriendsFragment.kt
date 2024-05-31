package com.example.firstapp.ui.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.databinding.FragmentFriendsBinding
import com.example.firstapp.ui.data.Message
import com.example.firstapp.ui.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FriendsViewModel
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val factory = FriendsViewModelFactory(firestore, auth)
        viewModel = ViewModelProvider(this, factory).get(FriendsViewModel::class.java)

        setupRecyclerView()

        viewModel.filteredFriends.observe(
            viewLifecycleOwner,
            Observer { friends ->
                friends?.let {
                    friendsAdapter.submitList(it)
                }
            },
        )

        binding.searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.setSearchQuery(newText ?: "")
                    return true
                }
            },
        )

        binding.floatingActionButton.setOnClickListener {
            navigateToAddFriendScreen()
        }
        viewModel.fetchFriends()
    }

    private fun navigateToAddFriendScreen() {
        val action = FriendsFragmentDirections.actionFriendsFragmentToFriendsInviteFragment()
        findNavController().navigate(action)
    }

    private fun setupRecyclerView() {
        friendsAdapter =
            FriendsAdapter { friend ->
                startConversationWithFriend(friend)
            }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsAdapter
        }
    }

    private fun startConversationWithFriend(friend: User) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        userId?.let { currentUserId ->
            val currentUserFriendConversationRef =
                firestore.collection("Conversations")
                    .whereEqualTo("participants", listOf(currentUserId, friend.userId))
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val existingConversationId = querySnapshot.documents[0].id
                            val action =
                                FriendsFragmentDirections
                                    .actionFriendsFragmentToSingleConversationFragment(existingConversationId)
                            findNavController().navigate(action)
                        } else {
                            firestore.collection("Conversations")
                                .whereEqualTo("participants", listOf(friend.userId, currentUserId))
                                .get()
                                .addOnSuccessListener { secondQuerySnapshot ->
                                    if (!secondQuerySnapshot.isEmpty) {
                                        val existingConversationId = secondQuerySnapshot.documents[0].id
                                        val action =
                                            FriendsFragmentDirections
                                                .actionFriendsFragmentToSingleConversationFragment(existingConversationId)
                                        findNavController().navigate(action)
                                    } else {
                                        createNewConversation(friend, listOf(userId, friend.userId))
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("FriendsFragment", "Error fetching conversations", exception)
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FriendsFragment", "Error fetching conversations", exception)
                    }
        }
    }

    private fun createNewConversation(
        friend: User,
        participants: List<String?>,
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        userId?.let { currentUserId ->
            val conversationId = firestore.collection("Conversations").document().id
            val conversationRef = firestore.collection("Conversations").document(conversationId)

            val conversationData =
                hashMapOf(
                    "conversationId" to conversationId,
                    "status" to "solo",
                    "participants" to participants,
                    "messageIds" to listOf<Message>(),
                )

            conversationRef.set(conversationData)
                .addOnSuccessListener {
                    participants.forEach { userId ->
                        if (userId != null) {
                            firestore.collection("Users").document(userId)
                                .update("conversations", FieldValue.arrayUnion(conversationRef))
                                .addOnFailureListener { e -> Log.e("FriendsFragment", "Error adding conversation reference", e) }
                        }
                    }
                    navigateToConversation(conversationRef.id)
                }
                .addOnFailureListener { e ->
                    Log.e("FriendsFragment", "Error creating new conversation", e)
                }
        }
    }

    private fun navigateToConversation(conversationId: String) {
        val action = FriendsFragmentDirections.actionFriendsFragmentToSingleConversationFragment(conversationId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
