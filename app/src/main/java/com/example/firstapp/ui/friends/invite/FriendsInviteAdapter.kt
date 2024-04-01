package com.example.firstapp.ui.friends.invite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.ui.data.User
import com.google.firebase.auth.FirebaseAuth

class FriendsInviteAdapter(private val viewModel: FriendsInviteViewModel) : RecyclerView.Adapter<FriendsInviteAdapter.FriendsInviteViewHolder>() {

    private var friendsList = emptyList<User>()

    fun submitList(list: List<User>) {
        friendsList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsInviteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friends_invite_list_item, parent, false)
        return FriendsInviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendsInviteViewHolder, position: Int) {
        val friend = friendsList[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    inner class FriendsInviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username)
        private val usernameCodeTextView: TextView = itemView.findViewById(R.id.usernameCode)
        private val userImageView: ImageView = itemView.findViewById(R.id.userImage)
        private val sendRequestButton: ImageView = itemView.findViewById(R.id.imageButtonSendRequest)

        init {
            sendRequestButton.setOnClickListener {
                val friend = friendsList[adapterPosition]
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                userId?.let {
                    friend.userId?.let { it1 -> viewModel.sendFriendRequest(it, it1) }
                }
            }
        }

        fun bind(user: User) {
            usernameTextView.text = user.username
            usernameCodeTextView.text = user.usernameCode
            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .into(userImageView)
        }
    }
}