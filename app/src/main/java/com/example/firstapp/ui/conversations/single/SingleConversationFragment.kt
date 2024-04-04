package com.example.firstapp.ui.conversations.single

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentConversationsSoloBinding
import com.example.firstapp.ui.data.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.example.firstapp.ui.data.Conversation
import com.example.firstapp.ui.data.Message
import com.google.android.material.textfield.TextInputEditText

class SingleConversationFragment : Fragment() {
    private var savedNavBarColor: Int = 0

    private var _binding: FragmentConversationsSoloBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SingleConversationViewModel by viewModels()

    private lateinit var messageAdapter: SingleConversationAdapter

    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversationsSoloBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedNavBarColor = requireActivity().window.navigationBarColor

        if (Build.VERSION.SDK_INT >= 21) {
            requireActivity().window.navigationBarColor = requireContext().getColor(R.color.black)
        }
        bottomNavView = requireActivity().findViewById(R.id.bottomNavView) ?: return
        if (bottomNavView.visibility != View.GONE) {
            bottomNavView.visibility = View.GONE
        }

        val conversationId = requireArguments().getString("conversationId")

        conversationId?.let {
            loadMessages(it)
            loadFriendData(it)
        }
        viewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            messages?.let {
                Log.d(TAG, "Messages: $messages")
                messageAdapter.submitList(it)
            }
        })
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            messages?.let {
                messageAdapter.submitList(it)
            }
        })

        val textEditTextMessage: TextInputEditText = binding.textEditTextMessage

        textEditTextMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.recyclerView.post {
                    binding.recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }

    }

    private fun setupRecyclerView() {
        messageAdapter = SingleConversationAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
        }
    }

    private fun loadMessages(conversationId: String) {
        val firestore = FirebaseFirestore.getInstance()

        val messages = mutableListOf<Message>()

        firestore.collection("Conversations").document(conversationId)
            .get()
            .addOnSuccessListener { conversationDocument ->
                val conversation = conversationDocument.toObject(Conversation::class.java)
                conversation?.let {
                    it.messageIds?.forEach { messageId ->
                        firestore.collection("Messages").document(messageId)
                            .get()
                            .addOnSuccessListener { messageDocument ->
                                val message = messageDocument.toObject(Message::class.java)
                                message?.let {
                                    messages.add(it)
                                }

                                if (messages.size == conversation.messageIds?.size) {
                                    val sortedMessages = messages.sortedBy { it.timestamp }

                                    viewModel.setMessages(sortedMessages)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Error fetching message", exception)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching conversation", exception)
            }
    }


    private fun loadFriendData(conversationId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        firestore.collection("Conversations").document(conversationId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val participants = documentSnapshot.get("participants") as? List<String>
                val friendUserId = participants?.find { it != auth.currentUser?.uid }

                friendUserId?.let { userId ->
                    firestore.collection("Users").document(userId)
                        .get()
                        .addOnSuccessListener { userDocument ->
                            val friend = userDocument.toObject(User::class.java)
                            friend?.let { user ->
                                binding.textViewFriendsUsername.text = user.username
                                binding.textViewFriendsUsernameCode.text = "#${user.usernameCode}"
                                Glide.with(requireContext())
                                    .load(user.profileImageUrl)
                                    .into(binding.imageViewFriendsImage)
                            }
                        }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavView.visibility = View.VISIBLE
        requireActivity().window.navigationBarColor = savedNavBarColor

        _binding = null
    }

    companion object {
        private const val TAG = "SingleConversationFragment"
    }
}
