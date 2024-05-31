package com.example.firstapp.ui.friends.invite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentFriendsInviteBinding
import com.example.firstapp.ui.friends.FriendsViewModel
import com.example.firstapp.ui.friends.FriendsViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsInviteFragment : Fragment() {
    private var _binding: FragmentFriendsInviteBinding? = null
    private val binding get() = _binding!!

    private lateinit var friendsViewModel: FriendsViewModel
    private lateinit var viewModel: FriendsInviteViewModel
    private lateinit var friendsAdapter: FriendsInviteAdapter

    private var savedNavBarColor: Int = 0
    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFriendsInviteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        val friendsFactory = FriendsViewModelFactory(firestore, auth)
        friendsViewModel = ViewModelProvider(requireActivity(), friendsFactory).get(FriendsViewModel::class.java)

        val factory = FriendsInviteViewModelFactory(firestore, auth, friendsViewModel)
        viewModel = ViewModelProvider(this, factory).get(FriendsInviteViewModel::class.java)

        setupRecyclerView() // Moved setupRecyclerView here

        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
        viewModel.friends.observe(
            viewLifecycleOwner,
            Observer { friends ->
                friends?.let {
                    friendsAdapter.submitList(it)
                }
            },
        )

        binding.buttonSearchFriends.setOnClickListener {
            val searchTerm = binding.textInputEditTextUsername.text.toString()
            viewModel.searchFriends(searchTerm)
            Log.e("searchTerm", searchTerm)
        }

        savedNavBarColor = requireActivity().window.navigationBarColor
        requireActivity().window.navigationBarColor = requireContext().getColor(R.color.black)
        bottomNavView = requireActivity().findViewById(R.id.bottomNavView) ?: return
        if (bottomNavView.visibility != View.GONE) {
            bottomNavView.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        friendsAdapter = FriendsInviteAdapter(viewModel)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavView.visibility = View.VISIBLE
        requireActivity().window.navigationBarColor = savedNavBarColor
        _binding = null
    }
}
