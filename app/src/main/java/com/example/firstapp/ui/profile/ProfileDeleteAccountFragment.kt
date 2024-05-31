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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ProfileDeleteAccountFragment : Fragment() {
    private var savedNavBarColor: Int = 0
    private lateinit var bottomNavView: BottomNavigationView

    private lateinit var auth: FirebaseAuth

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_delete_account, container, false)

        auth = FirebaseAuth.getInstance()

        emailLayout = view.findViewById(R.id.textInputLayoutEmail)
        passwordLayout = view.findViewById(R.id.textInputLayoutPassword)

        view.findViewById<Button>(R.id.buttonDeleteAccount).setOnClickListener {
            showAffirmationDialog()
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

    private fun showAffirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Potwierdzenie")
            .setMessage("Czy na pewno chcesz usunąć swoje konto? Tej operacji nie można cofnąć.")
            .setNegativeButton("Anuluj") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Usuń konto") { dialog, _ ->
                handleDeleteAccount()
                dialog.dismiss()
            }
            .show()
    }

    private fun handleDeleteAccount() {
        val email = emailLayout.editText?.text.toString().trim()
        val password = passwordLayout.editText?.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Wypełnij pola tekstowe", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.delete()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Konto zostało usunięte", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "Account deleted successfully")
                                findNavController().popBackStack(R.id.profileFragment, false)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Nie udało się usunąć konta", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "Account deletion failed", e)
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
        private const val TAG = "ProfileDeleteAccountFragment"
    }
}
