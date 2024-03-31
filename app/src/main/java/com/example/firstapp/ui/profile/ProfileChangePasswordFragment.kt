package com.example.firstapp.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.firstapp.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ProfileChangePasswordFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var currentPasswordLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_change_password, container, false)

        auth = FirebaseAuth.getInstance()

        newPasswordLayout = view.findViewById(R.id.textInputLayoutNewPassword)
        currentPasswordLayout = view.findViewById(R.id.textInputLayoutPassword)

        view.findViewById<Button>(R.id.buttonChangePassword).setOnClickListener {
            handleChangePassword()
        }

        return view
    }

    private fun handleChangePassword() {
        val newPassword = newPasswordLayout.editText?.text.toString().trim()
        val currentPassword = currentPasswordLayout.editText?.text.toString()

        if (newPassword.isEmpty() || currentPassword.isEmpty()) {
            return
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnSuccessListener {
                                Log.d(TAG, "Password updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Password update failed", e)
                            }
                    } else {
                        Log.e(TAG, "Reauthentication failed", reauthTask.exception)
                    }
                }
        } else {
            Log.e(TAG, "User is null")
        }
    }

    companion object {
        private const val TAG = "ProfileChangePasswordFragment"
    }
}