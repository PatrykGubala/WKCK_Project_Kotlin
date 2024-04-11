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
import androidx.core.view.doOnLayout
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
    private lateinit var bottomNavView: BottomNavigationView

    private var _binding: FragmentConversationsSoloBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SingleConversationViewModel by viewModels()
    private lateinit var messageAdapter: SingleConversationAdapter
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var isImageSelected: Boolean = false
    private var selectedImageUri: Uri? = null

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
            if (isImageSelected) {
                selectedImageUri?.let { uri ->
                    sendMessageWithImage(uri, conversationId ?: "")
                    isImageSelected= false
                    selectedImageUri = null
                }
            } else {
                val messageText: String = binding.textEditTextMessage.text.toString().trim()
                if (messageText.isNotEmpty()) {
                    val currentTime = Timestamp.now()
                    val messageId = firestore.collection("Messages").document().id

                    val message = Message(
                        messageId = messageId,
                        message = messageText,
                        senderId = auth.currentUser?.uid ?: "",
                        timestamp = currentTime,
                        messageImageUrl = null
                    )

                    conversationId?.let { it1 -> viewModel.sendMessage(message, it1) }
                    binding.textEditTextMessage.setText("")
                }
            }
        }
        viewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            messages?.let {
                messageAdapter.submitList(it)
                if (it.isNotEmpty()) {
                    binding.recyclerView.doOnLayout {
                        val totalHeight = calculateTotalItemsHeight()
                        binding.recyclerView.smoothScrollBy(0, totalHeight)
                    }
                }
            }
        })



        val textEditTextMessage: TextInputEditText = binding.textEditTextMessage

        textEditTextMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                messageAdapter?.let { adapter ->
                    if (adapter.itemCount > 0) {
                        binding.recyclerView.post {
                            binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                        }
                    }
                }
            }
        }
    }

    private fun calculateTotalItemsHeight(): Int {
        var totalHeight = 0
        for (i in 0 until messageAdapter.itemCount) {
            val itemView = binding.recyclerView.layoutManager?.findViewByPosition(i)
            itemView?.let {
                totalHeight += it.height
            }
        }
        return totalHeight
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
            data?.data?.let { uri ->
                selectedImageUri = uri
                isImageSelected = true
                updateImageButtonState()
            }
        }
    }
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun updateImageButtonState() {
        val imageButton: ImageButton = view?.findViewById(R.id.imageButtonPlus) ?: return
        if (isImageSelected) {
            imageButton.isSelected = true
            imageButton.setOnClickListener {
                isImageSelected = false
                selectedImageUri = null
                updateImageButtonState()
            }
        } else {
            imageButton.isSelected = false
            imageButton.setOnClickListener {
                selectImageFromGallery()
            }
        }
    }

    private fun sendMessageWithImage(imageUri: Uri, conversationId: String) {
        val messageId = firestore.collection("Messages").document().id

        uploadImageToFirebaseStorage(imageUri, messageId) { imageUrl ->
            val currentTime = Timestamp.now()
            val message = Message(
                message = "" ,
                messageId,
                senderId = auth.currentUser?.uid ?: "",
                timestamp = currentTime,
                messageImageUrl = imageUrl
            )
            updateImageButtonState()
            viewModel.sendMessage(message, conversationId)
        }
    }
    private fun uploadImageToFirebaseStorage(imageUri: Uri, messageId: String, onComplete: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/$userId/messages/$messageId.png")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { uploadTask ->
                uploadTask.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    Log.d(TAG, "Image uploaded successfully. URL: $imageUrl")
                    onComplete(imageUrl)
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error getting download URL", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error uploading image", e)
            }
    }


    private fun updateFirestoreWithImageUrl(messageId: String, imageUrl: String) {
        val messageRef = firestore.collection("Messages").document(messageId)
        messageRef.update("messageImageUrl", imageUrl)
            .addOnSuccessListener {
                Log.d(TAG, "Image URL updated in Firestore")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating image URL in Firestore", exception)
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
