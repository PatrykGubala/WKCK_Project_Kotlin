package com.example.firstapp.ui.friends.invite


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.R
import com.example.firstapp.databinding.FragmentFriendsInviteBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class FriendsInviteFragment : Fragment() {

    private var _binding: FragmentFriendsInviteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsInviteViewModel by viewModels()

    private lateinit var friendsAdapter: FriendsInviteAdapter

    private var savedNavBarColor: Int = 0
    private lateinit var bottomNavView: BottomNavigationView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsInviteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
        viewModel.friends.observe(viewLifecycleOwner, Observer { friends ->
            friends?.let {
                friendsAdapter.submitList(it)
            }
        })

        binding.buttonSearchFriends.setOnClickListener {
            val searchTerm = binding.textInputEditTextUsername.text.toString()
            viewModel.searchFriends(searchTerm)
            Log.e("searchTerm",searchTerm)
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