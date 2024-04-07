package com.example.firstapp.ui.conversations.single

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.ui.data.Message
import com.example.firstapp.ui.data.User
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SingleConversationAdapter : ListAdapter<Message, SingleConversationAdapter.MessageViewHolder>(DiffCallback) {
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_list_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message)
        private val senderTextView: TextView = itemView.findViewById(R.id.username)
        private val senderCodeTextView: TextView = itemView.findViewById(R.id.usernameCode)
        private val timeAgoTextView: TextView = itemView.findViewById(R.id.timeAgo)
        private val userImageView: ImageView = itemView.findViewById(R.id.userImage)

        fun bind(message: Message) {
            messageTextView.text = message.message

            timeAgoTextView.text = getRelativeTimeAgo(message.timestamp?.toDate()?.time ?: 0)
            message.senderId?.let { userId ->
                getUserDetails(userId) { user ->
                    user?.let { userDetails ->
                        Glide.with(itemView.context)
                            .load(userDetails.profileImageUrl)
                            .into(userImageView)
                        senderTextView.text = user.username
                        senderCodeTextView.text = "#${user.usernameCode}"
                    }
                }
            }
        }

        private fun getRelativeTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                days == 0L -> "Dziś o ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))}"
                days == 1L -> "Wczoraj o ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))}"
                else -> SimpleDateFormat("dd/MM/yyyy 'o' HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

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

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem.messageId == newItem.messageId
            }

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }
        }
    }
}
