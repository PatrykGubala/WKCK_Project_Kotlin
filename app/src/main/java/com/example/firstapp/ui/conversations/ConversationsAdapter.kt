package com.example.firstapp.ui.conversations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.R
import com.example.firstapp.ui.data.Conversation

class ConversationsAdapter(private val conversations: List<Conversation>) :
    RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.username)
        val messageText: TextView = itemView.findViewById(R.id.message)

        fun bind(conversation: Conversation) {
            username.text = conversation.lastMessageSender
            messageText.text = conversation.lastMessage
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.conversation_item, parent, false)
        return ConversationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val currentConversation = conversations[position]
        holder.username.text = currentConversation.lastMessageSender
        holder.messageText.text = currentConversation.lastMessage
    }

    override fun getItemCount(): Int {
        return conversations.size
    }


}
