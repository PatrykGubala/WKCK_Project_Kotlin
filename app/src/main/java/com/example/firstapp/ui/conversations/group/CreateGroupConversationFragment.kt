package com.example.firstapp.ui.conversations.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentConversationsCreateNewGroupBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class CreateGroupConversationFragment : Fragment() {
    private var _binding: FragmentConversationsCreateNewGroupBinding? = null
    private var savedNavBarColor: Int = 0
    private lateinit var bottomNavView: BottomNavigationView
    private val binding get() = _binding!!
    private val viewModel: CreateGroupConversationViewModel by viewModels()

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
        if (bottomNavView.visibility != View.GONE) {
            bottomNavView.visibility = View.GONE
        }
        binding.signInButton.setOnClickListener {
            val groupName = binding.textInputLayoutEmail.editText?.text.toString().trim()
            val friendsList = listOf("friend1", "friend2")

            viewModel.createGroupConversation(groupName, friendsList)
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.groupCreated.observe(viewLifecycleOwner) { success ->
            if (success) {
            } else {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavView.visibility = View.VISIBLE
        requireActivity().window.navigationBarColor = savedNavBarColor
    }
}
