package com.example.firstapp.ui.messages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.firstapp.databinding.FragmentMessagesBinding

class MessageFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
                ViewModelProvider(this).get(MessageViewModel::class.java)

        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var sharedPreferences =
            requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}