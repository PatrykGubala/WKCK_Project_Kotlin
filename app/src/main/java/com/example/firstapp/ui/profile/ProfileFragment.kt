package com.example.firstapp.ui.profile


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentProfileBinding
import com.example.firstapp.ui.BaseFragment
import com.example.firstapp.ui.auth.FirebaseAuthManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : BaseFragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userId: String
    private lateinit var userDocRef: DocumentReference

    private val userSnapshotListener = FirebaseFirestore.getInstance()
        .collection("Users")
        .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
        .addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.w(TAG, "Listen failed", exception)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val email = snapshot.getString("email")
                val username = snapshot.getString("username")
                val usernameCode = snapshot.getString("usernameCode")

                binding.textViewEmail.text = email
                binding.textViewUsername.text = username
                binding.textViewUsernameCode.text = "#$usernameCode"

                val photoUrl = snapshot.getString("profileImageUrl")
                if (photoUrl != null) {
                    Log.d(TAG, photoUrl)
                    loadProfileImageWithGlide(photoUrl)
                }
                val status = snapshot.getString("status")
                updateStatusImageButton(status)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }
    private fun updateStatusImageButton(status: String?) {
        status?.let {
            when (it) {
                "Dostępny" -> binding.imageButtonStatus.setImageResource(R.drawable.chrome_green)
                "Zaraz wracam" -> binding.imageButtonStatus.setImageResource(R.drawable.chrome_yellow)
                "Nie przeszkadzać" -> binding.imageButtonStatus.setImageResource(R.drawable.chrome_red)
                else -> {

                }
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        userDocRef = FirebaseFirestore.getInstance().collection("Users").document(userId)

        binding.imageButtonStatus.setOnClickListener {
            showChangeStatusBottomSheet()
        }
        binding.imageButtonEditProfile.setOnClickListener {
            showEditProfileBottomSheet()
        }

        binding.buttonLogout.setOnClickListener {
            logout()
        }
    }
    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun dismissBottomSheetDialog() {
        val dialog = (binding.imageButtonStatus.tag as? BottomSheetDialog)
        dialog?.dismiss()
    }
    private fun showEditProfileBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_profile_edit, null)

        val buttonEditProfileNickname = view.findViewById<Button>(R.id.buttonEditProfileNickname)
        val buttonEditProfilePicture = view.findViewById<Button>(R.id.buttonEditProfilePicture)

        buttonEditProfileNickname.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        buttonEditProfilePicture.setOnClickListener {
            bottomSheetDialog.dismiss()
            selectImageFromGallery()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()


    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data
            imageUri?.let { uploadImageToFirebaseStorage(it) }
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/$userId/profilePhoto.png")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { uploadTask ->
                uploadTask.storage.downloadUrl.addOnSuccessListener { uri ->
                    val profileImageUrl = uri.toString()
                    userDocRef.update("profileImageUrl", profileImageUrl)
                        .addOnSuccessListener {
                            Log.d(TAG, "Profile image URL updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating profile image URL", e)
                        }

                    Log.d(TAG, "Image uploaded successfully")
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error getting download URL", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error uploading image", e)
            }
    }
    private fun showChangeStatusBottomSheet() {
        userDocRef.get()
            .addOnSuccessListener { document ->
                val currentStatus = document.getString("status")
                currentStatus?.let {
                    val bottomSheetDialog = BottomSheetDialog(requireContext())
                    val view = layoutInflater.inflate(R.layout.bottomsheet_profile_status_change, null)

                    val radioGroupStatus = view.findViewById<RadioGroup>(R.id.radioGroupStatus)

                    when (it) {
                        "Dostępny" -> view.findViewById<RadioButton>(R.id.radioGreen).isChecked = true
                        "Zaraz wracam" -> view.findViewById<RadioButton>(R.id.radioYellow).isChecked = true
                        "Nie przeszkadzać" -> view.findViewById<RadioButton>(R.id.radioRed).isChecked = true
                    }

                    radioGroupStatus.setOnCheckedChangeListener { _, checkedId ->
                        val status = when (checkedId) {
                            R.id.radioGreen -> "Dostępny"
                            R.id.radioYellow -> "Zaraz wracam"
                            R.id.radioRed -> "Nie przeszkadzać"
                            else -> ""
                        }
                        updateProfileStatus(status)
                        bottomSheetDialog.dismiss()
                    }

                    bottomSheetDialog.setContentView(view)
                    bottomSheetDialog.show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching current status", e)
            }
    }

    private fun updateProfileStatus(status: String) {
        Log.d(TAG, "Updating profile status to: $status")
        userDocRef.update("status", status)
            .addOnSuccessListener {
                Log.d(TAG, "Profile status updated successfully")
                dismissBottomSheetDialog()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating profile status", e)
            }
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
        userSnapshotListener.remove()
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}
