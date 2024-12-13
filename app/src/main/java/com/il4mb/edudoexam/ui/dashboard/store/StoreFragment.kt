package com.il4mb.edudoexam.ui.dashboard.store

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.api.BuyPayload
import com.il4mb.edudoexam.components.GenericListAdapter
import com.il4mb.edudoexam.components.dialog.DialogBottom
import com.il4mb.edudoexam.components.dialog.InfoDialog
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.components.ui.UiHelper
import com.il4mb.edudoexam.databinding.FragmentStoreBinding
import com.il4mb.edudoexam.databinding.ViewItemPackageBinding
import com.il4mb.edudoexam.models.AccountPackage
import com.il4mb.edudoexam.ui.dashboard.SharedViewModel
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class StoreFragment : BaseFragment<FragmentStoreBinding>(FragmentStoreBinding::class.java),
    GenericListAdapter.ItemBindListener<AccountPackage, ViewItemPackageBinding> {

    private val listAdapter: GenericListAdapter<AccountPackage, ViewItemPackageBinding> by lazy {
        GenericListAdapter(
            viewBindingClass = ViewItemPackageBinding::class.java,
            onItemBindCallback = this,
            diffCallback = AccountPackage.DiffCallback()
        )
    }
    override var isBottomNavigationVisible = false
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val storeViewModel: StoreViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.packageRecycle.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        liveCycleObserve()

        lifecycleScope.launch {
            delay(400)
            storeViewModel.fetchPriceList(
                activity = requireActivity(),
                onSuccess = {  },
                onError = {
                    showInfo(getString(R.string.something_went_wrong), it.message)
                })
        }

        binding.apply {
            getPriceButton.setOnClickListener {
                if(inputQuota.text.isEmpty()) {
                    showInfo(getString(R.string.quota_must_be_filled))
                } else
                if(inputQuota.text.toInt() <= 0) {
                    showInfo(getString(R.string.quota_must_be_greater_than_zero))
                } else {
                    showPriceDialog(inputQuota.text.toInt())
                }
            }
            userCard.apply {
                actionButton.visibility = View.GONE
                root.apply {
                    background = ColorDrawable()
                }
            }
        }
    }

    private fun showInfo(message: String) {
        InfoDialog(requireActivity())
            .setMessage(message)
            .show()
    }

    private fun showInfo(title: String, message: String) {
        InfoDialog(requireActivity())
            .setTitle(title)
            .setMessage(message)
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun liveCycleObserve() {
        storeViewModel.priceList.observe(viewLifecycleOwner) {
            Log.d("PACKAGES", Gson().toJson(it))
            listAdapter.submitList(it.packages)
        }
        sharedViewModel.user.observe(viewLifecycleOwner) {
            binding.apply {
                it?.let { user ->
                    userCard.apply {
                        userName.text = user.name
                        userEmail.text = user.email
                        UiHelper.setupUserImage(requireContext(), userPhoto, user)
                    }
                    currentPackageLabel.text = HtmlCompat.fromHtml(
                        getString(
                            R.string.your_current_package_b_b,
                            user.currentPackage?.label ?: "N/A"
                        ),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )

                    if(user.quota <= 0) {
                        quotaLimitReached.visibility = View.VISIBLE
                    }
                }
                quotaRemaining.text = getString(R.string.d_quota_remaining).format(it?.quota)
                currentPackageLayout.root.visibility = View.GONE
            }
        }
    }

    private val priceFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    @SuppressLint("SetTextI18n")
    override fun onViewBind(binding: ViewItemPackageBinding, item: AccountPackage, position: Int) {
        binding.apply {
            labelTextView.text = item.label
            maxParticipantValue.text = item.maxParticipant.toString()
            maxQuestionValue.text = item.maxQuestion.toString()
            freeQuotaValue.text = "+${item.freeQuota}"
            priceValue.text = priceFormat.format(item.price) + "/" + getString(R.string.month)

            if(item.id == sharedViewModel.user.value?.currentPackage?.id) {
                container.apply {
                    strokeColor = requireContext().getColor(R.color.primary)
                    backgroundTintList = requireContext().getColorStateList(R.color.primary_variant)
                }
            } else {
                root.setOnClickListener {
                    showPurchaseDialog(item)
                }
            }
        }
    }

    private fun showPurchaseDialog(item: AccountPackage) {
        DialogBottom.Builder(requireActivity())
            .apply {
                title = getString(R.string.buy_package)
                message = HtmlCompat.fromHtml(
                    getString(
                        R.string.are_you_sure_you_want_to_buy_this_package_price_will_be_b_b,
                        priceFormat.format(item.price)
                    ),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                dismissText = getString(R.string.cancel)
                dismissHandler = { true }
                acceptHandler = {
                    sendPurchaseRequest(item)
                    true
                }
                acceptText = getString(R.string.buy)
            }.show()
    }

    @SuppressLint("StringFormatMatches")
    private fun showPriceDialog(quota: Int) {
        storeViewModel.priceList.value?.pricing?.quota?.let { priceQuota ->
            val price = priceQuota * quota
            DialogBottom.Builder(requireActivity())
                .apply {
                    title = getString(R.string.price_detail)
                    message =
                        getString(R.string.quota_will_cost_you, quota, priceFormat.format(price))
                    dismissText = getString(R.string.cancel)
                    dismissHandler = { true }
                    acceptText = getString(R.string.cancel)
                    acceptHandler = {
                        sendPurchaseRequest(null, quota)
                        true
                    }
                }.show()
        } ?: showInfo(getString(R.string.something_went_wrong))
    }

    private fun sendPurchaseRequest(item: AccountPackage?, quota: Int = 0) {
        sharedViewModel.user.value?.let { user ->
            storeViewModel.buyPackage(
                activity = requireActivity(),
                userId = user.id,
                buyPayload = BuyPayload(
                    item?.id,
                    quota
                ),
                onSuccess = {
                    showSuccessDialog(
                        "Purchase Success",
                        "You have successfully purchased this package"
                    )
                    sharedViewModel.fetchUser(requireActivity())
                },
                onError = {
                    showErrorDialog(
                        title = "Failed to buy package",
                        message = it.message,
                        textAction = "Retry",
                        handler = { sendPurchaseRequest(item, quota) }
                    )
                }
            )
        } ?: showInfo("Missing user id")
    }

    private fun showSuccessDialog(title: String, message: String) {

        sharedViewModel.fetchUser(requireActivity())

        DialogBottom.Builder(requireActivity()).apply {
            this.title = title
            this.message = message
            acceptText = getString(R.string.go_back)
            acceptHandler = {
                findNavController().popBackStack()
                true
            }
        }.show()
    }

    private fun showErrorDialog(title: String, message: String, textAction: String?, handler: (() -> Unit) = {}) {
        DialogBottom.Builder(requireActivity()).apply {
            this.title = title
            this.message = message
            acceptText = textAction ?: ""
            acceptHandler = {
                handler()
                true
            }
            if(textAction.isNullOrEmpty()) {
                isAcceptActionButtonVisible = false
            }
        }.show()
    }
}