package com.ariyan.spark.ui.home.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ariyan.spark.databinding.FragmentMatchBinding
import com.ariyan.spark.ui.BaseFragment

class MatchFragment : BaseFragment() {

    private var _binding: FragmentMatchBinding? = null
    private val binding get() = _binding!!

    // Use the real-time MatchViewModel
    private val vm: MatchViewModel by viewModels()
    private lateinit var adapter: MatchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToRefresh()
        observeViewModel()

        // We no longer need to call vm.loadMatches() here,
        // because the ViewModel's `init` block starts the real-time listener automatically.
    }

    private fun setupRecyclerView() {
        adapter = MatchAdapter { user ->
            // Navigate to the chat screen when a match is clicked
            val action =
                MatchFragmentDirections.actionMatchFragmentToChatFragment(user.uid, user.name)
            findNavController().navigate(action)
        }
        binding.recyclerViewMatches.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMatches.adapter = adapter
    }

    private fun setupSwipeToRefresh() {
        // Even though the list is real-time, we can provide this for user feedback
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Since the list updates automatically, we just hide the indicator.
            // The ViewModel's isLoading will also do this, but this provides instant feedback.
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        // Observe the loading state to show/hide the swipe refresh indicator.
        // This is primarily for the initial data load.
        vm.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        // Observe the real-time list of matches from the ViewModel
        vm.matches.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            // Show the empty state view if the list is empty, otherwise hide it
            binding.emptyStateContainer.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe for any potential errors
        vm.error.observe(viewLifecycleOwner) { err ->
            err?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

