package com.example.firstapp.ui.auth.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentSignInBinding
import com.example.firstapp.ui.BaseFragment
import com.example.firstapp.ui.auth.register.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : BaseFragment() {
    private lateinit var binding: FragmentSignInBinding
    private val fbAuth = FirebaseAuth.getInstance()
    private val LOG_DEBUG = "LOG_DEBUG"
    private val regVm by viewModels<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupLoginClick()

        binding.backButton.setOnClickListener {
            findNavController().popBackStack(R.id.startFragment, false)
        }
    }

    private fun setupLoginClick() {
        binding.signInButton.setOnClickListener {
            val email = binding.textInputLayoutEmail.editText?.text.toString()
            val pass = binding.textInputLayoutPassword.editText?.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Wpisz email oraz hasło", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fbAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { authRes ->
                    startApp()
                }
                .addOnFailureListener { exc ->
                    Toast.makeText(requireContext(), exc.message ?: "Logowanie się nie powiodło", Toast.LENGTH_SHORT).show()
                    Log.d(LOG_DEBUG, exc.message.toString())
                }
        }
    }
}
