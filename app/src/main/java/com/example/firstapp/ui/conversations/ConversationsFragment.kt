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
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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

        conversationsRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val conversationRefs = documentSnapshot.get("conversations") as? List<DocumentReference>
                    conversationRefs?.let {
                        fetchConversationsByRefs(it)
                    }
                } else {
                    Log.d(TAG, "Document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching user document", exception)
            }
    }
    private fun fetchConversationsByRefs(conversationRefs: List<DocumentReference>) {
        val conversations = mutableListOf<Conversation>()

        val tasks = conversationRefs.map { conversationRef ->
            conversationRef.get().addOnSuccessListener { conversationSnapshot ->
                val participants = conversationSnapshot.get("participants") as? List<String>
                val messages = mutableListOf<Message>()

                val messageRefs = conversationSnapshot.get("messages") as? List<DocumentReference>
                messageRefs?.let { messageRefs ->
                    val messageTasks = messageRefs.map { messageRef ->
                        messageRef.get().addOnSuccessListener { messageSnapshot ->
                            val message = messageSnapshot.toObject(Message::class.java)
                            message?.let {
                                messages.add(message)
                            }
                        }.addOnFailureListener { exception ->
                            Log.e(TAG, "Error fetching message", exception)
                        }
                    }
                    Tasks.whenAllSuccess<DocumentSnapshot>(messageTasks).addOnCompleteListener { messageTask ->
                        if (messageTask.isSuccessful) {
                            val lastMessage = messages.lastOrNull()?.message
                            val lastMessageSender = messages.lastOrNull()?.senderId

                            val conversation = Conversation(
                                participants = participants,
                                messages = messages,
                                lastMessage = lastMessage,
                                lastMessageSender = lastMessageSender
                            )
                            conversations.add(conversation)
                            Log.d(TAG, "Conversations !!!!!!!!!!! fetching messages: $conversations")

                            updateConversations(conversations)
                        } else {
                            Log.e(TAG, "Error fetching messages: ${messageTask.exception}")
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching conversation", exception)
            }
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "Error fetching conversations: ${task.exception}")
            }
        }
    }
    private fun updateConversations(conversations: List<Conversation>) {
        conversationList.clear()
        conversationList.addAll(conversations)
        conversationsAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MessageFragment"
    }
}
