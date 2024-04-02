package com.example.firstapp.ui.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.databinding.FragmentFriendsBinding
import com.example.firstapp.ui.data.Message
import com.example.firstapp.ui.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsViewModel by viewModels()

    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.friends.observe(viewLifecycleOwner, Observer { friends ->
            friends?.let {
                friendsAdapter.submitList(it)
            }
        })
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
        friendsAdapter = FriendsAdapter { friend ->
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
            val currentUserFriendConversationRef = firestore.collection("Conversations")
                .whereEqualTo("participants", listOf(currentUserId, friend.userId))

            currentUserFriendConversationRef.get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val existingConversationId = querySnapshot.documents[0].id
                    } else {
                        createNewConversation(friend)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FriendsFragment", "Error checking conversation existence", exception)
                }
        }
    }

    private fun createNewConversation(friend: User) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        userId?.let { currentUserId ->
            val conversationId = firestore.collection("Conversations").document().id
            val conversationRef = firestore.collection("Conversations").document(conversationId)

            val conversationData = hashMapOf(
                "status" to "solo",
                "participants" to listOf(currentUserId, friend.userId),
                "messages" to listOf<Message>(),
                "lastMessage" to null,
                "lastMessageSender" to null,
                "lastMessageTimestamp" to null
            )

            conversationRef.set(conversationData)
                .addOnSuccessListener {
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}