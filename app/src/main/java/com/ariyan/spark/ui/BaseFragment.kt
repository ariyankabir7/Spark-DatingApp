package com.ariyan.spark.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.ariyan.spark.R
import com.ariyan.spark.utils.SessionViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

/**
 * A base fragment that all other fragments in the main part of the app should extend.
 * It handles observing the user's session status and triggers a forced logout if necessary.
 */
abstract class BaseFragment : Fragment() {

    // Use activityViewModels to get a ViewModel instance scoped to the navigation graph
    protected val sessionViewModel: SessionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSession()
    }

    override fun onResume() {
        super.onResume()
        // Check the session status every time a fragment becomes visible
        sessionViewModel.checkSessionStatus()
    }

    private fun observeSession() {
        sessionViewModel.forceLogoutEvent.observe(viewLifecycleOwner) { message ->
            // First, sign out the user from the local Firebase instance
            FirebaseAuth.getInstance().signOut()

            // Show a message explaining why they were logged out
            view?.let {
                Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
            }

            // Navigate back to the authentication screen, clearing all previous screens
            // This uses the same robust navigation logic we implemented for the logout button
            requireActivity().findNavController(R.id.main_nav_host_fragment)
                .navigate(R.id.action_global_to_authFragment)
        }
    }
}
