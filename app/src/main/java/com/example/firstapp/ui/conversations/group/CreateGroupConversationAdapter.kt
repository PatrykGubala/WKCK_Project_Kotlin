package com.example.firstapp.ui.conversations.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.ui.data.User

class CreateGroupConversationAdapter(
    private var friends: List<User>,
    private val selectedFriends: MutableSet<User>,
) : RecyclerView.Adapter<CreateGroupConversationAdapter.FriendViewHolder>() {
    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val friendName: TextView = view.findViewById(R.id.username)
        val friendCheckbox: CheckBox = view.findViewById(R.id.checkBoxSelectFriend)
        val friendAvatar: ImageView = view.findViewById(R.id.userImage)
        val friendCode: TextView = view.findViewById(R.id.usernameCode)

        init {
            friendCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedFriends.add(friends[adapterPosition])
                } else {
                    selectedFriends.remove(friends[adapterPosition])
                }
            }
        }

        fun bind(friend: User) {
            friendName.text = friend.username
            friendCode.text = friend.usernameCode
            Glide.with(friendAvatar.context).load(friend.profileImageUrl).into(friendAvatar)
            friendCheckbox.isChecked = selectedFriends.contains(friend)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friends_create_group_list_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FriendViewHolder,
        position: Int,
    ) {
        holder.bind(friends[position])
    }

    override fun getItemCount() = friends.size

    fun updateFriends(newFriends: List<User>) {
        friends = newFriends
        notifyDataSetChanged()
    }
}
