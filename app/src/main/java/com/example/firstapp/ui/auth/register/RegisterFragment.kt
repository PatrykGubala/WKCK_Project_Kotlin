package com.example.firstapp.ui.auth.register

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentSignUpBinding
import com.example.firstapp.ui.BaseFragment
import com.google.android.material.snackbar.Snackbar


class RegisterFragment: BaseFragment() {

        private lateinit var binding: FragmentSignUpBinding
        private val REG_DEBUG = "REG_DEBUG"
        private val regVm by viewModels<RegisterViewModel>()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = FragmentSignUpBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            binding.backButton.setOnClickListener {
                findNavController().popBackStack(R.id.startFragment, false)
            }

        }






}