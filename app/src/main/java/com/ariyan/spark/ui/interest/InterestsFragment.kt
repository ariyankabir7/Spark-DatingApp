package com.ariyan.spark.ui.interest

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ariyan.spark.R
import com.ariyan.spark.databinding.FragmentInterestsBinding
import com.ariyan.spark.utils.LoadingUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InterestsFragment : Fragment(R.layout.fragment_interests) {
    private var _binding: FragmentInterestsBinding? = null
    private val binding get() = _binding!!
    private val categories = listOf("Sports","Entertainment","Bollywood","Music","Travel","Food","Art","Technology")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInterestsBinding.bind(view)

        val adapter = InterestsAdapter(categories) { selectedSet ->
            // optional: enable button based on selection
            binding.btnSaveInterests.isEnabled = selectedSet.isNotEmpty()
            binding.btnSaveInterests.tag = selectedSet
        }
        binding.rvInterests.adapter = adapter
        // Use a simple FlexboxLayoutManager or FlowLayoutManager if added; fallback to standard GridLayout

        binding.btnSaveInterests.setOnClickListener {
            val selected = binding.btnSaveInterests.tag as? Set<String> ?: emptySet()
            if (selected.isEmpty()) { Toast.makeText(requireContext(), "Pick at least one", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            LoadingUtils.show(childFragmentManager)
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            Firebase.firestore.collection("users").document(uid).update("interests", selected.toList())
                .addOnSuccessListener {
                    LoadingUtils.hide()
                    findNavController().navigate(R.id.action_interests_to_home)
                }.addOnFailureListener {  e ->
                    LoadingUtils.hide()
                    Toast.makeText(requireContext(), "Save failed: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
