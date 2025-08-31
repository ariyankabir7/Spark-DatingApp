package com.ariyan.spark.ui.auth

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ariyan.spark.R
import com.ariyan.spark.databinding.FragmentAuthBinding
import com.ariyan.spark.utils.LoadingUtils

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val vm: AuthViewModel by viewModels()
    private var isLoginMode = false
    private var selectedPhotoUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedPhotoUri = it
                binding.profileImageView.setImageURI(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.profileImageView.setOnClickListener { pickImageLauncher.launch("image/*") }
        updateUiForLogin() // Start in login mode
        binding.btnToggleAuth.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) updateUiForLogin() else updateUiForSignUp()
        }
    }

    private fun observeViewModel() {
        vm.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) LoadingUtils.show(childFragmentManager) else LoadingUtils.hide()
        }

        vm.authResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthResult.Success -> {
                    val destination = if (result.hasInterests) R.id.action_auth_to_home else R.id.action_auth_to_interests
                    findNavController().navigate(destination)
                }
                is AuthResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateUiForLogin() {
        binding.apply {
            signUpFieldsContainer.visibility = View.GONE
            tvTitle.text = "Welcome Back!"
            tvSubtitle.text = "Login to continue"
            btnPrimaryAction.text = "Login"
            tvToggleQuestion.text = "Don't have an account?"
            btnToggleAuth.text = "Sign Up"
            btnPrimaryAction.setOnClickListener { handleLogin() }
        }
    }

    private fun updateUiForSignUp() {
        binding.apply {
            signUpFieldsContainer.visibility = View.VISIBLE
            tvTitle.text = "Create Account"
            tvSubtitle.text = "Let's get you started!"
            btnPrimaryAction.text = "Sign Up"
            tvToggleQuestion.text = "Already have an account?"
            btnToggleAuth.text = "Login"
            btnPrimaryAction.setOnClickListener { handleSignUp() }
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (validateLogin(email, password)) {
            vm.signIn(email, password)
        }
    }

    private fun handleSignUp() {
        val name = binding.etName.text.toString().trim()
        val ageStr = binding.etAge.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val gender = when {
            binding.rbMale.isChecked -> "male"
            binding.rbFemale.isChecked -> "female"
            else -> "other"
        }
        val age = ageStr.toIntOrNull()

        if (validateSignUp(name, age, email, password)) {
            vm.signUp(email, password, name, age!!, gender, selectedPhotoUri)
        }
    }

    private fun validateLogin(email: String, pass: String): Boolean {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            return false
        }
        if (pass.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        return true
    }

    private fun validateSignUp(name: String, age: Int?, email: String, pass: String): Boolean {
        if (!validateLogin(email, pass)) return false

        binding.tilName.error = null
        binding.tilAge.error = null

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            return false
        }
        if (age == null) {
            binding.tilAge.error = "Invalid age format"
            return false
        }
        if (age < 18) {
            binding.tilAge.error = "You must be 18 or older"
            return false
        }
        if (selectedPhotoUri == null) {
            Toast.makeText(context, "Profile picture is required", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
