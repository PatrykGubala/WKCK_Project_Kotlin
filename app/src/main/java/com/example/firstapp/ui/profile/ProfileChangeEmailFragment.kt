package com.example.firstapp.ui.profile

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.firstapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ProfileChangeEmailFragment : Fragment() {
    private var savedNavBarColor: Int = 0
    private lateinit var bottomNavView: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private lateinit var currentEmailLayout: TextInputLayout
    private lateinit var newEmailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_change_email, container, false)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        currentEmailLayout = view.findViewById(R.id.textInputLayoutEmail)
        newEmailLayout = view.findViewById(R.id.textInputLayoutNewEmail)
        passwordLayout = view.findViewById(R.id.textInputLayoutPassword)

        view.findViewById<Button>(R.id.changeEmailButton).setOnClickListener {
            handleChangeEmail()
        }

        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        savedNavBarColor = requireActivity().window.navigationBarColor
        requireActivity().window.navigationBarColor = requireContext().getColor(R.color.black)
        bottomNavView = requireActivity().findViewById(R.id.bottomNavView) ?: return
        if (bottomNavView.visibility != View.GONE) {
            bottomNavView.visibility = View.GONE
        }
    }

        private fun handleChangeEmail() {
        val currentEmail = currentEmailLayout.editText?.text.toString().trim()
        val newEmail = newEmailLayout.editText?.text.toString().trim()
        val password = passwordLayout.editText?.text.toString()

        if (currentEmail.isEmpty() || newEmail.isEmpty() || password.isEmpty()) {
            return
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val credential = EmailAuthProvider.getCredential(currentEmail, password)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.verifyBeforeUpdateEmail(newEmail)
                            .addOnCompleteListener { updateEmailTask ->
                                if (updateEmailTask.isSuccessful) {
                                    Log.d(TAG, "Verification email sent successfully")
                                } else {
                                    Log.e(TAG, "Verification email sending failed", updateEmailTask.exception)
                                }
                            }
                    } else {
                        Log.e(TAG, "Reauthentication failed", reauthTask.exception)
                    }
                }
        } else {
            Log.e(TAG, "User is null")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavView.visibility = View.VISIBLE
        requireActivity().window.navigationBarColor = savedNavBarColor
    }

}