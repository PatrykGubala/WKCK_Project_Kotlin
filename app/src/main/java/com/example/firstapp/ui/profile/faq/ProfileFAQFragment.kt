package com.example.firstapp.ui.profile.faq

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentProfileFaqBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileFAQFragment : Fragment() {
    private var savedNavBarColor: Int = 0
    private lateinit var bottomNavView: BottomNavigationView

    private lateinit var binding: FragmentProfileFaqBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedNavBarColor = requireActivity().window.navigationBarColor
        requireActivity().window.navigationBarColor = requireContext().getColor(R.color.black)
        bottomNavView = requireActivity().findViewById(R.id.bottomNavView) ?: return
        if (bottomNavView.visibility != View.GONE) {
            bottomNavView.visibility = View.GONE
        }
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
        setupUI()
    }

    private fun setupUI() {
        setupExpandCollapseButton(binding.expandCollapseButtonTopic1, binding.expandedTextView)
        setupExpandCollapseButton(binding.expandCollapseButtonTopic2, binding.expandedTextView2)
    }

    private fun setupExpandCollapseButton(button: ImageButton, textView: TextView) {
        button.setOnClickListener {
            toggleExpansion(textView)
            updateExpandCollapseButtonIcon(button, textView.visibility == View.VISIBLE)
        }
    }

    private fun toggleExpansion(textView: TextView) {
        textView.visibility = if (textView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun updateExpandCollapseButtonIcon(button: ImageButton, isExpanded: Boolean) {
        button.isSelected = isExpanded
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavView.visibility = View.VISIBLE
        requireActivity().window.navigationBarColor = savedNavBarColor
    }
}
