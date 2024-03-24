package com.example.firstapp.ui.profile


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentProfileBinding
import com.example.firstapp.ui.BaseFragment
import com.example.firstapp.ui.auth.FirebaseAuthManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : BaseFragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fetchUserData()

        binding.buttonLogout.setOnClickListener {
            logout()
        }
        binding.imageButtonStatus.setOnClickListener {
            showChangeStatusBottomSheet()
        }
        return root
    }

    private fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "User ID: $userId")

        val userDocRef = userId?.let {
            FirebaseFirestore.getInstance().collection("Users").document(it)
        }

        userDocRef?.get()
            ?.addOnSuccessListener { document ->
                if (document != null) {
                    val email = document.getString("email")
                    val username = document.getString("username")
                    val usernameCode = document.getString("usernameCode")

                    binding.textViewEmail.text = email
                    binding.textViewUsername.text = username
                    binding.textViewUsernameCode.text = "#$usernameCode"

                    val photoUrl = document.getString("profilePhoto")
                    if (photoUrl != null) {
                        Log.d(TAG, photoUrl)
                    }

                    photoUrl?.let { loadProfileImageWithGlide(it) }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            ?.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }
    private fun showChangeStatusBottomSheet() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile_status_image_change, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val radioGreen = dialogView.findViewById<RadioButton>(R.id.radioGreen)
        val radioRed = dialogView.findViewById<RadioButton>(R.id.radioRed)
        val radioYellow = dialogView.findViewById<RadioButton>(R.id.radioYellow)

        radioGreen.setOnClickListener {
            updateProfileStatus("Dostępny")
            dialog.dismiss()
        }

        radioRed.setOnClickListener {
            updateProfileStatus("Zaraz wracam")
            dialog.dismiss()
        }

        radioYellow.setOnClickListener {
            updateProfileStatus("Nie przeszkadzać")
            dialog.dismiss()
        }

        dialog.show()


    }

    private fun updateProfileStatus(status: String) {
        Log.d(TAG, "Updating profile status to: $status")
    }

    private fun loadProfileImageWithGlide(photoUrl: String) {
        Glide.with(this)
            .load(photoUrl)
            .into(binding.imageViewProfileAvatar)
    }
    private fun logout() {
        FirebaseAuthManager.logoutUser()
        logoutApp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}
