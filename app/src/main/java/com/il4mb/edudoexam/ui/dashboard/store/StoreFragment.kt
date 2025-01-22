package com.il4mb.edudoexam.ui.dashboard.store

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.common.collect.ImmutableList
import com.google.gson.Gson
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
import com.il4mb.edudoexam.models.ProductItem
import com.il4mb.edudoexam.ui.dashboard.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


class StoreFragment : BaseFragment<FragmentStoreBinding>(FragmentStoreBinding::class.java),
    GenericListAdapter.ItemBindListener<ProductDetails, ViewItemPackageBinding> {

        class ProductDiff: DiffUtil.ItemCallback<ProductDetails>() {
            override fun areItemsTheSame(oldItem: ProductDetails, newItem: ProductDetails): Boolean {
                return oldItem.productId == newItem.productId
            }

            override fun areContentsTheSame(oldItem: ProductDetails, newItem: ProductDetails): Boolean {
                return oldItem == newItem
            }
        }

    private val listAdapter: GenericListAdapter<ProductDetails, ViewItemPackageBinding> by lazy {
        GenericListAdapter(
            viewBindingClass = ViewItemPackageBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ProductDiff()
        )
    }
    override var isBottomNavigationVisible = false
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val storeViewModel: StoreViewModel by activityViewModels()
    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(requireContext())
            .enablePendingPurchases()
            .setListener { p0, p1 ->
                if (p0.responseCode == BillingClient.BillingResponseCode.OK && p1 != null) {
                    for (purchase in p1) {
                        Log.d("PURCHASE", Gson().toJson(purchase))
                        // verifySubPurchase(purchase)
                    }
                }
            }
            .build()
    }

    fun establishConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play
                establishConnection()
            }
        })
    }

    fun showProducts() {

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("small")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("medium")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("large")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, prodDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && prodDetailsList.isNotEmpty()) {
                listAdapter.submitList(prodDetailsList)
            } else {
                // Handle errors
                println("Error querying product details: ${billingResult.debugMessage}")
            }
        }
    }


    fun launchPurchaseFlow(productDetails: ProductDetails) {
        // Ensure product details and offer token are available
        checkNotNull(productDetails.subscriptionOfferDetails)

        // Build product details parameters list
        val productDetailsParamsList =
            ImmutableList.of(
                ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(productDetails.subscriptionOfferDetails!![0].offerToken)
                    .build()
            )

        // Build billing flow parameters
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        // Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
    }

    fun verifySubPurchase(purchase: Purchase) {
        // Acknowledge the purchase
        val acknowledgePurchaseParams = AcknowledgePurchaseParams
            .newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Subscription activated, update UI or perform necessary actions
                Toast.makeText(
                    requireContext(),
                    "Subscription activated, Enjoy!",
                    Toast.LENGTH_SHORT
                ).show()
                //prefs.setPremium(1) // Set premium status to 1
                // startActivity(Intent(this, requireActivity()))
                //finish()
            }
        }

        // Log purchase information for reference
        Log.d("TAG", "Purchase Token: " + purchase.purchaseToken)
        Log.d("TAG", "Purchase Time: " + purchase.purchaseTime)
        Log.d("TAG", "Purchase OrderID: " + purchase.orderId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        establishConnection()

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
    override fun onViewBind(binding: ViewItemPackageBinding, item: ProductDetails, position: Int) {
        binding.apply {
            val price = item.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList?.first()?.formattedPrice
            labelTextView.text = item.title
            listProductBenefit.text = item.description
            priceValue.text = price
            if(item.productId == sharedViewModel.user.value?.currentPackage?.id) {
                container.apply {
                    strokeColor = requireContext().getColor(R.color.primary)
                    backgroundTintList = requireContext().getColorStateList(R.color.primary_variant)
                }
            } else {
                root.setOnClickListener {
                    launchPurchaseFlow(item)
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