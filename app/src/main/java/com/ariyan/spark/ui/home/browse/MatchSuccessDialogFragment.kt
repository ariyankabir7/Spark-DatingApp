package com.ariyan.spark.ui.home.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.ariyan.spark.databinding.DialogMatchBinding
import com.ariyan.spark.model.User
import com.bumptech.glide.Glide
import com.ariyan.spark.R

class MatchSuccessDialogFragment(
    private val currentUser: User,
    private val matchedUser: User,
    private val onStartChat: (User) -> Unit
) : DialogFragment() {

    private var _binding: DialogMatchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // Make the dialog's background transparent to show the custom rounded background
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvSubtitle.text = "You and ${matchedUser.name} have liked each other."

        Glide.with(this)
            .load(currentUser.photoUrl)
            .placeholder(R.drawable.ic_person)
            .circleCrop()
            .into(binding.ivCurrentUser)

        Glide.with(this)
            .load(matchedUser.photoUrl)
            .placeholder(R.drawable.ic_person)
            .circleCrop()
            .into(binding.ivMatchedUser)

        binding.btnStartChat.setOnClickListener {
            onStartChat(matchedUser)
            dismiss()
        }

        binding.btnKeepSwiping.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

