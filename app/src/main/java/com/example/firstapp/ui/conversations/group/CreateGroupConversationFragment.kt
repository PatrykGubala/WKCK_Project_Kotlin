package com.example.firstapp.ui.conversations.group

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentConversationsCreateNewGroupBinding
import com.example.firstapp.ui.data.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CreateGroupConversationFragment : Fragment() {
    private var _binding: FragmentConversationsCreateNewGroupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateGroupConversationViewModel by viewModels()
    private lateinit var createGroupConversationAdapter: CreateGroupConversationAdapter
    private lateinit var bottomNavView: BottomNavigationView
    private var savedNavBarColor: Int = 0
    private val selectedFriends = mutableSetOf<User>()

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
                    uploadImageToFirebaseStorage(it)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentConversationsCreateNewGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        savedNavBarColor = requireActivity().window.navigationBarColor
        requireActivity().window.navigationBarColor = requireContext().getColor(R.color.black)
        bottomNavView = requireActivity().findViewById(R.id.bottomNavView) ?: return
        bottomNavView.visibility = View.GONE

        setupRecyclerView()

        binding.createGroupButton.setOnClickListener {
            val groupName = binding.textInputLayoutGroupName.editText?.text.toString().trim()
            val friendsList = selectedFriends.map { it.userId }
            val conversationImageUrl = viewModel.conversationImageUrl.value ?: ""

            viewModel.createGroupConversation(groupName, friendsList, conversationImageUrl)
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.groupCreated.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Group created successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to create group", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.friends.observe(viewLifecycleOwner) { friends ->
            if (friends.isNotEmpty()) {
                createGroupConversationAdapter.updateFriends(friends)
            } else {
                Toast.makeText(context, "No friends loaded", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imageButtonEditProfile.setOnClickListener {
            selectImageFromGallery()
        }

        viewModel.conversationImageUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this).load(url).into(binding.conversationImageAvatar)
        }
    }

    private fun setupRecyclerView() {
        createGroupConversationAdapter = CreateGroupConversationAdapter(listOf(), selectedFriends)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = createGroupConversationAdapter
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/conversations/${UUID.randomUUID()}/groupImage.png")

        imageRef.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val conversationImageUrl = uri.toString()
                viewModel.setConversationImageUrl(conversationImageUrl)
                Glide.with(this).load(conversationImageUrl).into(binding.conversationImageAvatar)
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Upload failed: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavView.visibility = View.VISIBLE
        requireActivity().window.navigationBarColor = savedNavBarColor
        _binding = null
    }
}
