package com.capstone.edudoexam.ui.dashboard.profile

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.capstone.edudoexam.R
import com.capstone.edudoexam.api.AuthInterceptor
import com.capstone.edudoexam.api.payloads.UpdateProfile
import com.capstone.edudoexam.components.ui.BaseFragment
import com.capstone.edudoexam.components.dialog.DialogBottom
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.Utils
import com.capstone.edudoexam.databinding.FragmentProfileBinding
import com.capstone.edudoexam.ui.welcome.WelcomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::class.java),
    ViewTreeObserver.OnGlobalLayoutListener {

    private val profileAppbarLayout: ProfileAppbarLayout by lazy {
        ProfileAppbarLayout(requireContext())
    }
    private val viewModel : ProfileViewModel by viewModels()

    private var currentName = ""
    private var currentGender = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.apply {
            response.observe(viewLifecycleOwner) { response ->
                lifecycleScope.launch {
                    delay(600)
                    setLoading(false)
                    binding.apply {
                        currentName = response.user?.name.toString()
                        currentGender = response.user?.gender?: 1
                        inputName.text = response.user?.name.toString()
                        inputEmail.text = response.user?.email.toString()
                        genderRadio.gender = response.user?.gender?: 1
                        profileAppbarLayout.setUserIdText(response.user?.id.toString())
                    }
                }
            }
        }

        fetchProfile()

        lifecycleScope.launch {
            delay(400)
            getParentActivity().apply {
                hideNavBottom()
                addMenu(R.drawable.baseline_directions_walk_24, getColor(R.color.danger)) {
                    DialogBottom.Builder(requireActivity()).apply {
                        color = getColor(R.color.danger)
                        title = "Do you really want to leave?"
                        message = "Please confirm that you really want to leave?"
                        acceptText = "Ya sure"
                        acceptHandler = {
                            AuthInterceptor.clearToken(requireActivity())
                            startActivity(Intent(requireActivity(), WelcomeActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                            true
                        }
                    }.show()
                }
            }
        }

        binding.apply {
            root.viewTreeObserver.addOnGlobalLayoutListener(this@ProfileFragment)
            inputName.onTextChanged {
                validateForm()
            }
            genderRadio.onGenderChanged {
                validateForm()
            }
            saveButton.setOnClickListener {
                updateProfile()
            }
        }
    }

    private fun updateProfile() {

        Utils.hideKeyboard(requireActivity())
        setLoading(true)

        val fullName = binding.inputName.text
        val gender   = binding.genderRadio.gender

        viewModel.withProfile(requireActivity())
            .onSuccess {
                lifecycleScope.launch {
                    delay(600)
                    setLoading(false)
                    Snackbar.with(binding.root).show("Update Successfull", Snackbar.LENGTH_LONG)
                    currentName   = binding.inputName.text
                    currentGender = binding.genderRadio.gender
                    validateForm() // hide save button
                }
            }
            .onError {
                lifecycleScope.launch {
                    delay(600)
                    setLoading(false)
                    Snackbar.with(binding.root).show(
                        "Something went wrong",
                        it.message,
                        Snackbar.LENGTH_LONG
                    )
                }
            }
            .fetch { it.updateProfile(UpdateProfile(fullName, gender)) }
    }

    private fun fetchProfile() {
        viewModel.withProfile(requireActivity())
            .onError {
                lifecycleScope.launch {
                    delay(600)
                    setLoading(false)
                    Snackbar.with(binding.root)
                        .show(
                            "Something went wrong",
                            it.message,
                            Snackbar.LENGTH_LONG
                        )
                }
            }
            .fetch { it.getProfile() }
    }

    private fun formIsChanged() : Boolean {
        return binding.inputName.text.length > 3 && (
                    binding.inputName.text != currentName ||
                            binding.genderRadio.gender != currentGender)
    }

    private fun validateForm() {
        binding.saveButton.apply {
            val currentTranslationY = translationY
            if (formIsChanged()) {
                if(!isVisible) {
                    visibility = View.VISIBLE
                    alpha = 0f
                    translationY = currentTranslationY + 10f
                    animate()
                        .alpha(1f)
                        .translationY(currentTranslationY)
                        .setDuration(300)
                        .start()
                }
            } else {
                if(isVisible) {
                    animate()
                        .alpha(0f)
                        .translationY(currentTranslationY + 10f)
                        .setDuration(300)
                        .withEndAction{
                            visibility = View.GONE
                        }
                        .start()
                }
            }
        }
    }

    override fun onAppbarContentView(): View {
        return profileAppbarLayout
    }

    override fun onDestroyView() {
        try {
            binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
        } catch (_: Throwable) {
            // silent is prefect
        }
        super.onDestroyView()
    }

    override fun onGlobalLayout() {
        try {
            binding.apply {
                val rect = Rect()
                root.getWindowVisibleDisplayFrame(rect)
                val screenHeight = binding.root.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) {
                    binding.saveButton.translationY = -keypadHeight.toFloat()
                } else {
                    binding.saveButton.translationY = 0f
                }
            }
        } catch (_: Throwable) {
            // silent is perfect
        }
    }

}