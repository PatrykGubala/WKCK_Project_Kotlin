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

class SingleConversationAdapter : ListAdapter<Message, SingleConversationAdapter.MessageViewHolder>(DiffCallback) {
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_list_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message)
        private val senderTextView: TextView = itemView.findViewById(R.id.username)
        private val timeAgoTextView: TextView = itemView.findViewById(R.id.timeAgo)
        private val userImageView: ImageView = itemView.findViewById(R.id.userImage)

        fun bind(message: Message) {
            messageTextView.text = message.message
            senderTextView.text = message.senderId
            timeAgoTextView.text = getRelativeTimeAgo(message.timestamp?.toDate()?.time ?: 0)
            message.senderId?.let { userId ->
                getUserDetails(userId) { user ->
                   user?.let { userDetails ->
                       Glide.with(itemView.context)
                            .load(userDetails.profileImageUrl)
                           .into(userImageView)

                    }
                }
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
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }
        }
    }
}
