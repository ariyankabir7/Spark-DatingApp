package com.ariyan.spark.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ariyan.spark.R
import com.ariyan.spark.utils.NavDestination
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SplashFragment : Fragment(R.layout.fragment_splash) {
    private val vm: SplashViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // small delay for splash feel
        Handler(Looper.getMainLooper()).postDelayed({ vm.checkNavigation() }, 1500)
        vm.navigation.observe(viewLifecycleOwner) { dest ->
            when (dest) {
                NavDestination.AUTH -> findNavController().navigate(R.id.action_splash_to_auth)
                NavDestination.INTERESTS -> findNavController().navigate(R.id.action_splash_to_interests)
                NavDestination.HOME -> findNavController().navigate(R.id.action_splash_to_home)
                else -> { /* nothing */ }
            }
        }


    }

    private fun decideNext() {
        val nav = findNavController()
        val user = auth.currentUser

        // Define NavOptions to pop SplashFragment
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.splashFragment, true) // Assumes R.id.splashFragment is your splash fragment's ID in the nav graph
            .build()

        if (user == null) {
            nav.navigate(R.id.action_splash_to_auth, null, navOptions)
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    nav.navigate(R.id.action_splash_to_auth, null, navOptions)
                    return@addOnSuccessListener
                }
                val interests = doc.get("interests") as? List<*>
                if (interests.isNullOrEmpty()) {
                    nav.navigate(R.id.action_splash_to_interests, null, navOptions)
                } else {
                    nav.navigate(R.id.action_splash_to_home, null, navOptions)
                }
            }
            .addOnFailureListener {
                nav.navigate(R.id.action_splash_to_auth, null, navOptions)
            }
    }
}
