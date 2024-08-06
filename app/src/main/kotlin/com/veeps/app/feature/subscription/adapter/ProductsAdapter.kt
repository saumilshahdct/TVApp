package com.veeps.app.feature.subscription.adapter

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazon.device.iap.PurchasingService
import com.veeps.app.R
import com.veeps.app.databinding.ProductItemBinding
import com.veeps.app.extension.isFireTV
import com.veeps.app.feature.contentRail.model.Products
import com.veeps.app.util.AppHelper
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.Screens


class ProductsAdapter(
    private var context: Context?,
    private val planList: ArrayList<Products>,
    val helper: AppHelper,
) :
    RecyclerView.Adapter<ProductsAdapter.PlanViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int,
    ): ProductsAdapter.PlanViewHolder {
        return PlanViewHolder(
            ProductItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.binding.planTitle.text = planList[position].name
        holder.binding.planPrice.text = planList[position].price
        holder.binding.planContainer.setOnClickListener{
            if (context?.isFireTV == true) {
                try {
                    PurchasingService.purchase(planList[position].id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                helper.showErrorOnScreen(Screens.SUBSCRIPTION, context?.getString(R.string.feature_not_available).toString())
            }

        }
        holder.binding.planContainer.setOnFocusChangeListener { view, hasFocus ->
            holder.binding.unselectLabel.visibility = if (hasFocus) View.GONE else View.VISIBLE
            holder.binding.selectLabel.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
        holder.binding.planContainer.setOnKeyListener { _, keyCode, keyEvent ->
            keyEvent.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP || (position != DEFAULT.EMPTY_INT && (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)))
        }

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return planList.size
    }
    inner class PlanViewHolder(val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root)

}