package com.ariyan.spark.ui.home.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.ariyan.spark.R
import com.ariyan.spark.databinding.FragmentProfileBinding
import com.ariyan.spark.model.User
import com.ariyan.spark.ui.BaseFragment
import com.ariyan.spark.utils.LoadingUtils

class ProfileFragment : BaseFragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val vm: ProfileViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var isEditMode = false

    // Activity result launcher for picking an image from the gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.profileImageView.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
        vm.loadUserProfile()
    }

    private fun setupClickListeners() {
        binding.fabEdit.setOnClickListener {
            toggleEditMode()
        }

        binding.profileImageView.setOnClickListener {
            if (isEditMode) {
                pickImageLauncher.launch("image/*")
            }
        }

        binding.btnLogout.setOnClickListener {
            vm.logout()
            // and clearing the back stack correctly.
            requireActivity().findNavController(R.id.main_nav_host_fragment)
                .navigate(R.id.action_global_to_authFragment)
        }
    }

    private fun observeViewModel() {
        vm.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) LoadingUtils.show(childFragmentManager)
            else LoadingUtils.hide()
        }

        vm.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }

        vm.user.observe(viewLifecycleOwner) { user ->
            user?.let { populateUi(it) }
        }

        vm.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                toggleEditMode() // Exit edit mode on success
            }
        }
    }

    private fun populateUi(user: User) {
        binding.etName.setText(user.name)
        binding.etAge.setText(user.age.toString())

        when (user.gender.lowercase()) {
            "male" -> binding.rbMale.isChecked = true
            "female" -> binding.rbFemale.isChecked = true
            "other" -> binding.rbOther.isChecked = true
        }

        if (user.photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.profileImageView)
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode

        // Enable/disable input fields
        binding.etName.isEnabled = isEditMode
        binding.etAge.isEnabled = isEditMode
        binding.genderGroup.findViewById<RadioButton>(R.id.rbMale).isEnabled = isEditMode
        binding.genderGroup.findViewById<RadioButton>(R.id.rbFemale).isEnabled = isEditMode
        binding.genderGroup.findViewById<RadioButton>(R.id.rbOther).isEnabled = isEditMode

        // Change FAB icon and show/hide save button
        if (isEditMode) {
            binding.fabEdit.setImageResource(R.drawable.ic_done)
            binding.fabEdit.setOnClickListener {
                saveProfileChanges()
            }
        } else {
            binding.fabEdit.setImageResource(R.drawable.ic_edit)
            binding.btnSaveChanges.visibility = View.GONE

            // Optional: Reload data to discard any non-saved changes
            // vm.loadUserProfile()
        }
    }

    private fun saveProfileChanges() {
        val name = binding.etName.text.toString().trim()
        val age = binding.etAge.text.toString().toIntOrNull()

        val selectedGenderId = binding.genderGroup.checkedRadioButtonId
        if (name.isEmpty() || age == null || selectedGenderId == -1) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        val gender = binding.root.findViewById<RadioButton>(selectedGenderId).text.toString()

        vm.updateUserProfile(name, age, gender, selectedImageUri)
        binding.fabEdit.setOnClickListener {
            toggleEditMode()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
