package com.example.firstapp.ui.conversations

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.ui.data.Conversation
import com.example.firstapp.ui.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ConversationsAdapter(private val conversations: List<Conversation>) :
    RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.username)
        private val messageText: TextView = itemView.findViewById(R.id.message)
        private val timeAgo: TextView = itemView.findViewById(R.id.timeAgo)
        private val conversationImage: ImageView = itemView.findViewById(R.id.userImage)

        init {
            itemView.setOnClickListener {
                val action = ConversationsFragmentDirections.actionMessagesFragmentToSingleConversationFragment(conversationId = conversations[adapterPosition].conversationId ?: "")
                it.findNavController().navigate(action)
            }
        }

        fun bind(conversation: Conversation) {
            Log.d("Costam", conversation.messageIds.toString())

            conversation.messages?.let { messages ->
                val lastMessage = messages.lastOrNull()
                messageText.text = lastMessage?.message
                timeAgo.text = getRelativeTimeAgo(lastMessage?.timestamp?.toDate()?.time ?: 0)
            }

            if (conversation.status == "solo" && conversation.participants != null) {
                val otherParticipantId = conversation.participants.find { it != currentUserId }
                otherParticipantId?.let { userId ->
                    getUserDetails(userId) { user ->
                        user?.let { userDetails ->
                            username.text = userDetails.username
                            Glide.with(itemView.context)
                                .load(userDetails.profileImageUrl)
                                .into(conversationImage)
                        }
                    }
                }
            } else {
                username.text = "Group Chat"
            }
        }

        private fun getRelativeTimeAgo(timeInMillis: Long): String {
            return DateUtils.getRelativeTimeSpanString(
                timeInMillis,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.conversation_item, parent, false)
        return ConversationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val currentConversation = conversations[position]
        holder.bind(currentConversation)
    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    private fun getUserDetails(userId: String, callback: (User?) -> Unit) {
        firestore.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                callback(user)
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }
}
