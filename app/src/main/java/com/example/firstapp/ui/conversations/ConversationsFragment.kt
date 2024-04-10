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
import com.google.firebase.Timestamp
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
            conversationRef.collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { messagesSnapshot, exception ->
                    if (exception != null) {
                        Log.e(TAG, "Error fetching messages", exception)
                        return@addSnapshotListener
                    }

                    messagesSnapshot?.let { snapshot ->

                        val conversationId = conversationRef.id
                        val participants = mutableListOf<String>()
                        conversationRef.get().addOnSuccessListener { conversationDocument ->
                            val participantsList = conversationDocument.get("participants") as? List<String>
                            val messageIds = conversationDocument.get("messageIds") as? List<String>
                            participantsList?.let { participants.addAll(it) }

                            val status = conversationDocument.getString("status")

                            val conversation = Conversation(
                                conversationId = conversationId,
                                status = status,
                                participants = participants,
                                messageIds = messageIds

                            )
                            Log.d(TAG, "CONV ID: $conversationId, $messageIds ")


                            val existingConversation = conversationList.find { it.conversationId == conversationId }
                            if (existingConversation == null) {
                                conversationList.add(conversation)
                            } else {
                                conversationList[conversationList.indexOf(existingConversation)] = conversation
                            }

                            fetchMessagesForConversation(conversation)
                            Log.d(TAG, "CONV ID: $conversationId, ${conversation.messages} ")

                            conversationsAdapter.notifyDataSetChanged()
                        }.addOnFailureListener { exception ->
                            Log.e(TAG, "Error fetching conversation details", exception)
                        }
                    }
                }
        }
    }

    private fun fetchMessagesForConversation(conversation: Conversation) {
        val messages = mutableListOf<Message>()

        conversation.messageIds?.forEach { messageId ->
            firestore.collection("Messages").document(messageId)
                .get()
                .addOnSuccessListener { messageDocument ->
                    val messageData = messageDocument.data
                    val message = Message(
                        message = messageData?.get("message") as? String,
                        messageId = messageDocument.id,
                        senderId = messageData?.get("senderId") as? String,
                        timestamp = messageData?.get("timestamp") as? Timestamp,
                        messageImageUrl = messageData?.get("messageImageUrl") as? String
                    )
                    message?.let {
                        messages.add(it)
                    }

                    if (messages.size == conversation.messageIds?.size) {
                        conversation.messages = messages.sortedBy { it.timestamp }
                        conversationsAdapter.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching message", exception)
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
