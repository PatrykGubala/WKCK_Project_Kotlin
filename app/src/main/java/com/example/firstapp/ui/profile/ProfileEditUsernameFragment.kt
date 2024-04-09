package com.example.firstapp.ui.profile

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentProfileEditUsernameBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileEditUsernameFragment : Fragment() {
    private lateinit var binding: FragmentProfileEditUsernameBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var savedNavBarColor: Int = 0
    private lateinit var bottomNavView: BottomNavigationView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileEditUsernameBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.apply {
            binding.changeUsernameButton.setOnClickListener {
                val newUsername = binding.textInputEditTextNewUsername.text.toString().trim()
                val newUsernameCode = binding.textInputEditTextNewUsernameCode.text.toString().trim()

                if (newUsername.isNotEmpty() && newUsernameCode.isNotEmpty()) {
                    updateUserProfile(newUsername, newUsernameCode)
                }
            }

            binding.textInputEditTextNewUsername.addTextChangedListener { editable ->
                val newUsername = editable?.toString()?.trim() ?: ""
                generateUniqueUsernameCode(newUsername) { uniqueCode ->
                    binding.textInputEditTextNewUsernameCode.post {
                        binding.textInputEditTextNewUsernameCode.setText(uniqueCode)
                    }
                }
            }

            backButton.setOnClickListener {
                findNavController().popBackStack(R.id.profileFragment, false)
            }
        }

        return view
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
    }
    private fun updateUserProfile(newUsername: String, newUsernameCode: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("Users").document(userId)

            val updates = hashMapOf(
                "username" to newUsername,
                "usernameCode" to newUsernameCode
            )

            userDocRef
                .update(updates as Map<String, Any>)
                .addOnSuccessListener {
                    findNavController().popBackStack(R.id.profileFragment, false)

                }
                .addOnFailureListener { e ->
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavView.visibility = View.VISIBLE
        requireActivity().window.navigationBarColor = savedNavBarColor

    }
}
