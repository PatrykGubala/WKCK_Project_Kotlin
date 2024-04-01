package com.example.firstapp.ui.conversations

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.databinding.FragmentConversationsBinding
import com.example.firstapp.ui.data.Conversation
import com.example.firstapp.ui.data.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class ConversationsFragment : Fragment() {

    private var _binding: FragmentConversationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var conversationsAdapter: ConversationsAdapter
    private val conversationList = mutableListOf<Conversation>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        userId?.let { fetchConversations(it) }

        return root
    }

    private fun setupRecyclerView() {
        conversationsAdapter = ConversationsAdapter(conversationList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = conversationsAdapter
        }
    }

    private fun fetchConversations(userId: String) {
        val conversationsRef = firestore.collection("Users").document(userId)

        conversationsRef.addSnapshotListener { documentSnapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Error fetching conversations", exception)
                return@addSnapshotListener
            }

            documentSnapshot?.let { snapshot ->
                if (snapshot.exists()) {
                    val conversationRefs = snapshot.get("conversations") as? List<DocumentReference>
                    conversationRefs?.let {
                        fetchConversationsByRefs(it)
                    }
                } else {
                    Log.d(TAG, "Document does not exist")
                }
            }
        }
    }

    private fun fetchConversationsByRefs(conversationRefs: List<DocumentReference>) {
        conversationRefs.forEach { conversationRef ->
            conversationRef.addSnapshotListener { conversationSnapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error fetching conversation", exception)
                    return@addSnapshotListener
                }

                conversationSnapshot?.let {
                    val conversationId = conversationSnapshot.id
                    val participants = it.get("participants") as? List<String>
                    val messages = mutableListOf<Message>()
                    val status = it.getString("status")
                    val lastMessageTimestamp = it.getTimestamp("lastMessageTimestamp")

                    val messageRefs = it.get("messages") as? List<DocumentReference>
                    messageRefs?.let { messageRefs ->
                        messageRefs.forEach { messageRef ->
                            messageRef.get().addOnSuccessListener { messageSnapshot ->
                                val message = messageSnapshot.toObject(Message::class.java)
                                message?.let {
                                    messages.add(message)
                                }

                                val lastMessage = messages.lastOrNull()?.message
                                val lastMessageSender = messages.lastOrNull()?.senderId

                                val conversation = Conversation(
                                    conversationId = conversationId,
                                    status = status,
                                    participants = participants,
                                    messages = messages,
                                    lastMessage = lastMessage,
                                    lastMessageSender = lastMessageSender,
                                    lastMessageTime = lastMessageTimestamp
                                )

                                val existingConversation = conversationList.find { it.conversationId == conversationId }
                                if (existingConversation == null) {
                                    conversationList.add(conversation)
                                } else {
                                    conversationList[conversationList.indexOf(existingConversation)] = conversation
                                }

                                conversationsAdapter.notifyDataSetChanged()
                            }.addOnFailureListener { exception ->
                                Log.e(TAG, "Error fetching message", exception)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ConversationsFragment"
    }
}
