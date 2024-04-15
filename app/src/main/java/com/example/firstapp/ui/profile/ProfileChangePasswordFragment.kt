package com.example.firstapp.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.firstapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ProfileChangePasswordFragment : Fragment() {
    private var savedNavBarColor: Int = 0
    private lateinit var bottomNavView: BottomNavigationView

    private lateinit var auth: FirebaseAuth

    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var currentPasswordLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_change_password, container, false)

        auth = FirebaseAuth.getInstance()

        newPasswordLayout = view.findViewById(R.id.textInputLayoutNewPassword)
        currentPasswordLayout = view.findViewById(R.id.textInputLayoutPassword)

        view.findViewById<Button>(R.id.buttonChangePassword).setOnClickListener {
            handleChangePassword()
        }
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        savedNavBarColor = requireActivity().window.navigationBarColor
        requireActivity().window.navigationBarColor = requireContext().getColor(R.color.black)
        bottomNavView = requireActivity().findViewById(R.id.bottomNavView) ?: return
        if (bottomNavView.visibility != View.GONE) {
            bottomNavView.visibility = View.GONE
        }
    }

    private fun handleChangePassword() {
        val newPassword = newPasswordLayout.editText?.text.toString().trim()
        val currentPassword = currentPasswordLayout.editText?.text.toString()

        if (newPassword.isEmpty() || currentPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Wypełnij pola tekstowe", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(requireContext(), "Hasło zostało zaktualizowane", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                                Log.d(TAG, "Password updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Nie udało się zaktualizować hasła", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "Password update failed", e)
                            }
                    } else {
                        Toast.makeText(requireContext(), "Dane są niepoprawne", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Reauthentication failed", reauthTask.exception)
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Użytkownik nie istnieje - NULL", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "User is null")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavView.visibility = View.VISIBLE
        requireActivity().window.navigationBarColor = savedNavBarColor
    }

    companion object {
        private const val TAG = "ProfileChangePasswordFragment"
    }
}
