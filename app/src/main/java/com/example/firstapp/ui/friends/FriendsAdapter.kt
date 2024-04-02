package com.example.firstapp.ui.friends

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
import com.example.firstapp.ui.data.User

class FriendsAdapter(private val onClickListener: (User) -> Unit) : ListAdapter<User, FriendsAdapter.FriendsViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friends_list_item, parent, false)
        return FriendsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val friend = getItem(position)
        holder.bind(friend)
    }

    inner class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username)
        private val usernameCodeTextView: TextView = itemView.findViewById(R.id.usernameCode)
        private val userImageView: ImageView = itemView.findViewById(R.id.userImage)

        fun bind(user: User) {
            usernameTextView.text = user.username
            usernameCodeTextView.text = user.usernameCode
            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .into(userImageView)

            itemView.setOnClickListener {
                onClickListener.invoke(user)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}
