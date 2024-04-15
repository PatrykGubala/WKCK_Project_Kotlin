package com.example.firstapp.ui.conversations.single

import android.content.ContentValues.TAG
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.firstapp.R
import com.example.firstapp.ui.data.Message
import com.example.firstapp.ui.data.User
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SingleConversationAdapter : ListAdapter<Message, SingleConversationAdapter.MessageViewHolder>(DiffCallback) {
    private val firestore = FirebaseFirestore.getInstance()
    private var isFirstTimeLoading = true
    private var scrollToBottomListener: ScrollToBottomListener? = null

    fun setScrollToBottomListener(listener: ScrollToBottomListener) {
        scrollToBottomListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_list_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder,
        position: Int,
    ) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message)
        private val senderTextView: TextView = itemView.findViewById(R.id.username)
        private val senderCodeTextView: TextView = itemView.findViewById(R.id.usernameCode)
        private val timeAgoTextView: TextView = itemView.findViewById(R.id.timeAgo)
        private val userImageView: ImageView = itemView.findViewById(R.id.userImage)
        private val messageImage: ImageView = itemView.findViewById(R.id.imageMessage)

        fun bind(message: Message) {
            messageTextView.text = message.message

            timeAgoTextView.text = getRelativeTimeAgo(message.timestamp?.toDate()?.time ?: 0)
            message.senderId?.let { userId ->
                getUserDetails(userId) { user ->
                    user?.let { userDetails ->
                        Glide.with(itemView.context)
                            .load(userDetails.profileImageUrl).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(userImageView)
                        senderTextView.text = user.username
                        senderCodeTextView.text = "#${user.usernameCode}"
                    }
                }
            }

            if (message.messageImageUrl != null && message.messageImageUrl.isNotBlank()) {
                messageImage.visibility = View.VISIBLE

                Glide.with(itemView.context)
                    .load(message.messageImageUrl)
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .override(760, 760)
                    .listener(
                        object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                isFirstResource: Boolean,
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean,
                            ): Boolean {
                                if (isFirstTimeLoading) {
                                    scrollToBottomListener?.scrollToBottom()
                                    isFirstTimeLoading = false
                                }
                                return false
                            }
                        },
                    )
                    .into(messageImage)

                Log.d(TAG, "Message height: ${itemView.height}")
            } else {
                messageImage.visibility = View.GONE
            }
        }

        private fun getRelativeTimeAgo(timestamp: Long): String {
            val messageCalendar =
                Calendar.getInstance().apply {
                    timeInMillis = timestamp
                }
            val currentCalendar = Calendar.getInstance()

            return if (isSameDay(messageCalendar, currentCalendar)) {
                "Dziś o ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageCalendar.time)}"
            } else if (isYesterday(messageCalendar, currentCalendar)) {
                "Wczoraj o ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageCalendar.time)}"
            } else {
                SimpleDateFormat("dd/MM/yyyy 'o' HH:mm", Locale.getDefault()).format(messageCalendar.time)
            }
        }

        private fun isSameDay(
            cal1: Calendar,
            cal2: Calendar,
        ): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        private fun isYesterday(
            cal1: Calendar,
            cal2: Calendar,
        ): Boolean {
            cal2.add(Calendar.DAY_OF_YEAR, -1)
            return isSameDay(cal1, cal2)
        }
    }

    private fun getUserDetails(
        userId: String,
        callback: (User?) -> Unit,
    ) {
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
        private val DiffCallback =
            object : DiffUtil.ItemCallback<Message>() {
                override fun areItemsTheSame(
                    oldItem: Message,
                    newItem: Message,
                ): Boolean {
                    return oldItem.messageId == newItem.messageId
                }

                override fun areContentsTheSame(
                    oldItem: Message,
                    newItem: Message,
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
