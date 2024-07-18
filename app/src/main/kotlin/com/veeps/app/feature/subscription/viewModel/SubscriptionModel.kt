package com.veeps.app.feature.subscription.viewModel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.veeps.app.data.common.APIRepository
import com.veeps.app.feature.contentRail.model.Products
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.SubscriptionPlanDetails
import com.veeps.app.util.SubscriptionPlanSKUs

class SubscriptionModel : ViewModel(), DefaultLifecycleObserver {
    var errorMessage = MutableLiveData(DEFAULT.EMPTY_STRING)
    var errorPositiveLabel = MutableLiveData(DEFAULT.EMPTY_STRING)
    var errorNegativeLabel = MutableLiveData(DEFAULT.EMPTY_STRING)
    var isErrorPositiveApplicable = MutableLiveData(false)
    var isErrorNegativeApplicable = MutableLiveData(false)
    var getPlanList = MutableLiveData(ArrayList<Products>().apply {
        add(
            Products(
                id = SubscriptionPlanSKUs.MONTHLY_SUBSCRIPTION,
                name = SubscriptionPlanDetails.MONTHLY_PLAN_NAME,
                price = SubscriptionPlanDetails.MONTHLY_PLAN_PRICE
            )
        )
        add(
            Products(
                id = SubscriptionPlanSKUs.YEARLY_SUBSCRIPTION,
                name = SubscriptionPlanDetails.YEARLY_PLAN_NAME,
                price = SubscriptionPlanDetails.YEARLY_PLAN_PRICE
            )
        )
    })
    var planDetailListing = MutableLiveData(ArrayList<String>().apply {
        add(SubscriptionPlanDetails.PLAN_BENEFIT_ONE)
        add(SubscriptionPlanDetails.PLAN_BENEFIT_TWO)
        add(SubscriptionPlanDetails.PLAN_BENEFIT_THREE)
        add(SubscriptionPlanDetails.PLAN_BENEFIT_FOURE)
    })

    fun fetchUserDetails() = APIRepository().fetchUserDetails()
    fun subscriptionMapping(subscriptionMappingRequest: HashMap<String, Any>) =
        APIRepository().subscriptionMapping(subscriptionMappingRequest)

}