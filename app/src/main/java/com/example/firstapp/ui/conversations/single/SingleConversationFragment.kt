package com.example.firstapp.ui.conversations.single

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentConversationsSoloBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.example.firstapp.ui.data.User
import com.example.firstapp.ui.data.Message
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage

class SingleConversationFragment : Fragment() {
    private var savedNavBarColor: Int = 0
    private var _binding: FragmentConversationsSoloBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SingleConversationViewModel by viewModels()
    private lateinit var messageAdapter: SingleConversationAdapter
    private lateinit var bottomNavView: BottomNavigationView
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            viewModel.loadMessages(it)
            loadFriendData(it)
        }
        view.findViewById<ImageButton>(R.id.imageButtonPlus).setOnClickListener {
            selectImageFromGallery()
        }
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        val sendMessageButton: ImageButton = view.findViewById(R.id.imageButtonSendMessage)
        sendMessageButton.setOnClickListener {
            val messageText: String = binding.textEditTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val currentTime = Timestamp.now()
                val messageId = firestore.collection("Messages").document().id

                val message = Message(
                    messageId,
                    messageText,
                    auth.currentUser?.uid ?: "",
                    currentTime
                )

                conversationId?.let { it1 -> viewModel.sendMessage(message, it1) }
                binding.textEditTextMessage.setText("")
            }
        }

        viewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            messages?.let {
                messageAdapter.submitList(it)
                binding.recyclerView.post {
                    binding.recyclerView.scrollToPosition(it.size - 1)
                }
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

    private fun loadFriendData(conversationId: String) {
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
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data
            val messageId = "123"
            imageUri?.let { uploadImageToFirebaseStorage(it, messageId) }
        }
    }
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }
    private fun uploadImageToFirebaseStorage(imageUri: Uri, messageId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/$userId/messages/$messageId.png")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { uploadTask ->
                uploadTask.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error getting download URL", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error uploading image", e)
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
