package com.example.firstapp.ui.friends.invite


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.databinding.FragmentFriendsInviteBinding
class FriendsInviteFragment : Fragment() {

    private var _binding: FragmentFriendsInviteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsInviteViewModel by viewModels()

    private lateinit var friendsAdapter: FriendsInviteAdapter

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
        _binding = null
    }
}