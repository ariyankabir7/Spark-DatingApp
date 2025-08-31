package com.ariyan.spark.ui.home.browse

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import com.ariyan.spark.R
import com.ariyan.spark.databinding.FragmentBrowseBinding
import com.ariyan.spark.utils.LoadingUtils

class BrowseFragment : Fragment(R.layout.fragment_browse) {

    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!
    private val vm: BrowseViewModel by viewModels()
    private lateinit var adapter: UserCardAdapter
    private lateinit var layoutManager: CardStackLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBrowseBinding.bind(view)

        setupCardStackView()
        observeViewModel()
        vm.loadUsers()
    }

    override fun onResume() {
        super.onResume()
        vm.updateUserPresence()
    }

    private fun setupCardStackView() {
        adapter = UserCardAdapter()
        layoutManager = CardStackLayoutManager(requireContext(), object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {}
            override fun onCardSwiped(direction: Direction?) {
                val pos = layoutManager.topPosition - 1
                val swipedUser = vm.otherUsers.value?.getOrNull(pos) ?: return

                val action = when (direction) {
                    Direction.Right -> "like"
                    Direction.Left -> "pass"
                    else -> null
                }
                action?.let {
                    // First, save the swipe action to the backend.
                    vm.saveSwipe(swipedUser.uid, it)

                    // THE FIX: Immediately remove the user from the local list after any swipe.
                    // This ensures that when the last card is swiped, the list becomes empty
                    // and triggers the observer to show the "no profiles" view instantly.
                    vm.removeUserFromStack(swipedUser.uid)

                    val feedback = if (it == "like") "Liked ${swipedUser.name}" else "Passed on ${swipedUser.name}"
                    Snackbar.make(binding.root, feedback, Snackbar.LENGTH_SHORT).show()
                }
            }
            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View?, position: Int) {}
            override fun onCardDisappeared(view: View?, position: Int) {}
        })

        binding.cardStackView.layoutManager = layoutManager
        binding.cardStackView.adapter = adapter
        adapter.attachRecyclerView(binding.cardStackView)
    }

    private fun observeViewModel() {
        vm.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                LoadingUtils.show(childFragmentManager)
                binding.cardStackView.visibility = View.GONE
                binding.emptyStateContainer.visibility = View.GONE
            } else {
                LoadingUtils.hide()
            }
        }

        vm.otherUsers.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) {
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.cardStackView.visibility = View.GONE
            } else {
                binding.emptyStateContainer.visibility = View.GONE
                binding.cardStackView.visibility = View.VISIBLE
            }
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            err?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }

        vm.mutualMatch.observe(viewLifecycleOwner) { matchedUser ->
            val currentUser = vm.currentUser.value
            if (matchedUser != null && currentUser != null) {
                // THE FIX: The removal logic has been moved to onCardSwiped.
                // This observer's only job now is to show the success dialog.

                val dialog = MatchSuccessDialogFragment(currentUser, matchedUser) { user ->
                    val bundle = bundleOf(
                        "otherUserId" to user.uid,
                        "otherUserName" to user.name
                    )
                    findNavController().navigate(R.id.chatFragment, bundle)
                }
                dialog.show(childFragmentManager, "MatchSuccessDialog")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

