package com.example.firstapp.ui.profile.requests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.ui.data.User

class FriendsRequestsAdapter(private val friendsRequestsList: List<User>, private val acceptFriendRequest: (User) -> Unit) :
    RecyclerView.Adapter<FriendsRequestsAdapter.FriendsRequestsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsRequestsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friends_request_list_item, parent, false)
        return FriendsRequestsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendsRequestsViewHolder, position: Int) {
        val friend = friendsRequestsList[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int {
        return friendsRequestsList.size
    }

    inner class FriendsRequestsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username)
        private val usernameCodeTextView: TextView = itemView.findViewById(R.id.usernameCode)
        private val userImageView: ImageView = itemView.findViewById(R.id.userImage)
        private val acceptRequestButton: ImageButton = itemView.findViewById(R.id.imageButtonAcceptRequest)

        fun bind(user: User) {
            usernameTextView.text = user.username
            usernameCodeTextView.text = user.usernameCode
            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .into(userImageView)

            acceptRequestButton.setOnClickListener {
                acceptFriendRequest(user)
            }
        }
    }
}
