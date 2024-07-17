package com.veeps.app.feature.subscription.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.FulfillmentResult
import com.veeps.app.R
import com.veeps.app.core.BaseFragment
import com.veeps.app.databinding.SubscriptionScreenBinding
import com.veeps.app.extension.isFireTV
import com.veeps.app.feature.subscription.adapter.ProductsAdapter
import com.veeps.app.feature.subscription.adapter.ProductsDescriptionAdapter
import com.veeps.app.feature.subscription.viewModel.SubscriptionModel
import com.veeps.app.util.APIConstants
import com.veeps.app.util.AppAction
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppPreferences
import com.veeps.app.util.IntValue.NUMBER_15000
import com.veeps.app.util.Logger


class SubscriptionScreen : BaseFragment<SubscriptionModel, SubscriptionScreenBinding>() {
    private val action by lazy {
        object : AppAction {
            override fun onAction() {
                Logger.print(
                    "Action performed on ${
                        this@SubscriptionScreen.javaClass.name.substringAfterLast(".")
                    }"
                )
            }
        }
    }

    override fun getViewBinding(): SubscriptionScreenBinding =
        SubscriptionScreenBinding.inflate(layoutInflater)

    override fun onDestroyView() {
        viewModelStore.clear()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            subscription = viewModel
            subscriptionScreen = this@SubscriptionScreen
            loader.visibility = View.VISIBLE
            lifecycleOwner = viewLifecycleOwner
            lifecycle.addObserver(viewModel)
        }
        notifyAppEvents()
        loadAppContent()
    }

    private fun loadAppContent() {
        if (homeViewModel.productsList.isNotEmpty()) {
            viewModel.getPlanList.postValue(homeViewModel.productsList)
        }
    }

    private fun notifyAppEvents() {
        viewModel.getPlanList.observe(viewLifecycleOwner) { planList ->
            val adapter = ProductsAdapter(context, planList, helper)
            binding.planListing.adapter = adapter
            binding.planListing.requestFocus()
        }
        viewModel.planDetailListing.observe(viewLifecycleOwner) { planDetail ->
            val adapter = ProductsDescriptionAdapter(planDetail)
            binding.planDetailListing.adapter = adapter
            binding.loader.visibility = View.GONE
        }
        homeViewModel.purchaseAction.observe(viewLifecycleOwner) {
            if (it.isNullOrBlank()) {
                binding.paymentLoader.visibility = View.GONE
            } else {
                when (it) {
                    "PURCHASED" -> {
                        if (homeViewModel.isSubscription) {
                            binding.paymentLoader.visibility = View.VISIBLE
                            subscriptionMapping()
                        }
                    }

                    "FAILED" -> {
                        if (homeViewModel.isSubscription) {
                            homeViewModel.purchaseAction.postValue(null)
                            helper.showErrorOnScreen(
                                APIConstants.generateNewOrder, getString(R.string.payment_failed)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fetchUserDetails() {

        viewModel.fetchUserDetails().observe(viewLifecycleOwner) { userDetails ->
            fetch(
                userDetails,
                isLoaderEnabled = false,
                canUserAccessScreen = false,
                shouldBeInBackground = false
            ) {
                /* TODO: Handle 401 Unauthorized 422 Unprocessable Entity */
                userDetails.response?.let { userData ->
                    userData.data?.let { user ->
                        if (user.subscriptionStatus == "none") {
                            Handler(Looper.getMainLooper()).postDelayed({
                                fetchUserDetails()
                            }, NUMBER_15000.toLong())
                        } else {
                            homeViewModel.purchaseAction.postValue(null)
                            binding.paymentLoader.visibility = View.GONE
                            if (context?.isFireTV == true) {
                                PurchasingService.notifyFulfillment(
                                    homeViewModel.receiptId, FulfillmentResult.FULFILLED
                                )
                            }
                            AppPreferences.set(
                                AppConstants.userSubscriptionStatus, user.subscriptionStatus
                            )
                            helper.goBack()
                        }
                    } ?: helper.showErrorOnScreen(
                        userDetails.tag, getString(R.string.unknown_error)
                    )
                } ?: helper.showErrorOnScreen(userDetails.tag, getString(R.string.unknown_error))
            }
        }
    }

    private fun subscriptionMapping() {
        viewModel.subscriptionMapping(
            hashMapOf("partner_info" to hashMapOf("receipt_id" to homeViewModel.receiptId))
        ).observe(viewLifecycleOwner) { subscriptionMapping ->
            fetch(
                subscriptionMapping,
                isLoaderEnabled = false,
                canUserAccessScreen = false,
                shouldBeInBackground = true
            ) {
                Handler(Looper.getMainLooper()).post {
                    fetchUserDetails()
                }
            }
        }
    }
}