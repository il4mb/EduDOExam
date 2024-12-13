package com.il4mb.edudoexam.ui.dashboard.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.api.AuthInterceptor
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.components.dialog.DialogBottom
import com.il4mb.edudoexam.components.Utils
import com.il4mb.edudoexam.components.Utils.Companion.asAbsoluteFile
import com.il4mb.edudoexam.databinding.FragmentProfileBinding
import com.il4mb.edudoexam.ui.dashboard.SharedViewModel
import com.il4mb.edudoexam.ui.welcome.WelcomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::class.java),
    ViewTreeObserver.OnGlobalLayoutListener {

    private val profileAppbarLayout: ProfileAppbarLayout by lazy {
        ProfileAppbarLayout(requireContext())
    }
    private var photoUri: Uri? = null
        set(value) {
            field = value
            profileAppbarLayout.setProfileImageUri(value)
            validateForm()
        }

    private val priceFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    private val viewModel : SharedViewModel by activityViewModels()
    private var currentName = ""
    private var currentGender = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveCycleObserve()
        setupUI()
    }

    private fun setupUI() {

        profileAppbarLayout.editButton.setOnClickListener { pickImageBooth() }

        binding.apply {
            root.viewTreeObserver.addOnGlobalLayoutListener(this@ProfileFragment)
            inputName.onTextChanged {
                validateForm()
            }
            genderRadio.onGenderChanged {
                validateForm()
            }
            saveButton.setOnClickListener {
                submitUpdateProfile()
            }
            seeAnotherPackages.setOnClickListener {
                findNavController().navigate(R.id.action_nav_profile_to_nav_store_showcase)
            }
        }

        lifecycleScope.launch {
            delay(400)
            getParentActivity().apply {
                hideNavBottom()
                addMenu(R.drawable.baseline_directions_walk_24, getColor(R.color.danger)) {
                    DialogBottom.Builder(requireActivity()).apply {
                        color = getColor(R.color.danger)
                        title = getString(R.string.do_you_really_want_to_leave)
                        message = getString(R.string.please_confirm_that_you_really_want_to_leave)
                        acceptText = getString(R.string.ya_sure)
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
    }

    @SuppressLint("SetTextI18n")
    private fun liveCycleObserve() {
        viewModel.apply {
            user.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    binding.apply {
                        currentName = it?.name.toString()
                        currentGender = it?.gender?: 1
                        inputName.text = it?.name.toString()
                        inputEmail.text = it?.email.toString()
                        genderRadio.gender = it?.gender?: 1
                        profileAppbarLayout.apply {
                            setUserIdText(it?.id.toString())
                            it?.let {
                                if(photoUri != null) {
                                    setProfileImageUri(photoUri)
                                } else {
                                    if(it.photo == null) {
                                        if (it.gender == 0) {
                                            setProfileImageResource(R.drawable.woman)
                                        } else {
                                            setProfileImageResource(R.drawable.man)
                                        }
                                    } else {
                                        setProfileImageUri(it.photo)
                                    }
                                }
                            }
                        }
                        remainingQuota.text = getString(R.string.remaining_quota_d, it?.quota ?: 0)
                        currentPackageLayout.apply {
                            val currentPackage = it?.currentPackage
                            if(currentPackage == null) {
                                root.visibility = View.GONE
                            } else {
                                labelTextView.text = currentPackage.label
                                maxParticipantValue.text = currentPackage.maxParticipant.toString()
                                maxQuestionValue.text = currentPackage.maxQuestion.toString()
                                freeQuotaValue.text = currentPackage.freeQuota.toString()
                                priceValue.text = priceFormat.format(currentPackage.price) + "/" + getString(R.string.month)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun submitUpdateProfile() {

        Utils.hideKeyboard(requireActivity())

        val fullName = binding.inputName.text
        val gender   = binding.genderRadio.gender

        viewModel.updateProfile(
            activity = requireActivity(),
            newName = fullName,
            newGender = gender,
            newPhoto = photoUri?.asAbsoluteFile(requireContext()),
            onErrorCallback = {
                lifecycleScope.launch {
                    delay(400)
                    DialogBottom.Builder(requireActivity()).apply {
                        title = getString(R.string.something_went_wrong)
                        message = it.message
                        isCancelActionButtonVisible = false
                    }.show()
                }
            }
        )
    }

    private fun formIsChanged() : Boolean {
        return  binding.inputName.text.length > 3 && (
                    binding.inputName.text != currentName ||
                            binding.genderRadio.gender != currentGender || photoUri != null)
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

    override fun onImageResult(result: Boolean, uri: Uri) {
        if(result) {
            cropImage(requireContext(), uri, 1, 1) {
                photoUri = it
                validateForm()
            }
        }
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