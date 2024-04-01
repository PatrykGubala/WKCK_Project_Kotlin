package com.example.firstapp.ui.auth.register

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentSignUpBinding
import com.example.firstapp.ui.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterFragment : BaseFragment() {

    private lateinit var binding: FragmentSignUpBinding
    private val TAG = "RegisterFragment"
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        binding.textInputEditTextLayoutUsername.addTextChangedListener { editable ->
            val newUsername = editable?.toString()?.trim() ?: ""
            generateUniqueUsernameCode(newUsername) { uniqueCode ->
                binding.textInputEditTextGeneratedUsernameCode.post {
                    binding.textInputEditTextGeneratedUsernameCode.setText(uniqueCode)
                }
            }
        }
        binding.backButton.setOnClickListener {
            findNavController().popBackStack(R.id.startFragment, false)
        }

        binding.signUpButton.setOnClickListener {
            val email = binding.textInputEditTextLayoutEmail.text.toString()
            val password = binding.textInputEditTextLayoutPassword.text.toString()
            val repeatedPassword = binding.textInputEditTextLayoutRepeatPassword.text.toString()
            val username = binding.textInputEditTextLayoutUsername.text.toString()


            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != repeatedPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            checkEmailUnique(email) { isUnique ->
                if (!isUnique) {
                    Toast.makeText(requireContext(), "Email is already in use", Toast.LENGTH_SHORT)
                        .show()
                    return@checkEmailUnique
                }
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val user = hashMapOf(
                                "userId" to userId,
                                "email" to email,
                                "username" to username,
                                "usernameCode" to binding.textInputEditTextGeneratedUsernameCode.text.toString(),
                                "friends" to emptyList<String>(),
                                "friendsRequests" to emptyList<String>(),
                                "profileImageUrl" to "",
                                "status" to "Dostępny",
                                "conversations" to emptyList<DocumentReference>()
                            )
                            db.collection("Users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    Log.d(TAG, "User details saved in Firestore")
                                    Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnSuccessListener { authRes ->
                                            startApp()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding user details to Firestore", e)
                                    Toast.makeText(requireContext(), "Failed to create account", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmailAndPassword:failure", task.exception)
                        Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }

        }
        return binding.root
    }

    private fun checkEmailUnique(email: String, callback: (Boolean) -> Unit) {
        db.collection("Users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                callback(querySnapshot.isEmpty)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking email uniqueness", e)
                callback(true)
            }
    }
    private fun generateUniqueUsernameCode(username: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("Users")

        lifecycleScope.launch(Dispatchers.IO) {
            var usernameCode = "0000"
            var isUnique = false

            while (!isUnique && usernameCode != "10000") {
                val querySnapshot = usersRef
                    .whereEqualTo("username", username)
                    .whereEqualTo("usernameCode", usernameCode)
                    .limit(1)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    isUnique = true
                    callback(usernameCode)
                } else {
                    val codeInt = usernameCode.toInt()
                    usernameCode = String.format("%04d", codeInt + 1)
                }
            }
        }
    }

}
