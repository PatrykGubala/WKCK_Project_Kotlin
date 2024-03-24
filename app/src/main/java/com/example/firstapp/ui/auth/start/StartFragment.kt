package com.example.firstapp.ui.auth.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.firstapp.databinding.FragmentStartBinding
import com.example.firstapp.ui.BaseFragment



class StartFragment : BaseFragment() {

    private lateinit var binding: FragmentStartBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setupLoginClick()
            setupRegistrationClick()
        }
    }

    private fun setupRegistrationClick() {

        binding.signUpButton.setOnClickListener {
            findNavController().navigate(StartFragmentDirections.actionStartFragmentToRegistrationFragment())
        }
    }

    private fun setupLoginClick() {
        binding.signInButton.setOnClickListener {
            findNavController().navigate(StartFragmentDirections.actionStartFragmentToLoginFragment())
        }
    }
}
