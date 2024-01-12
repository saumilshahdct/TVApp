package com.veeps.app.feature.contentRail.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.veeps.app.R
import com.veeps.app.databinding.RowContentNewRailBinding
import com.veeps.app.feature.card.adapter.CircularCardAdapter
import com.veeps.app.feature.card.adapter.PortraitCardAdapter
import com.veeps.app.feature.contentRail.model.RailData
import com.veeps.app.util.AppAction
import com.veeps.app.util.AppHelper
import com.veeps.app.util.CardTypes
import com.veeps.app.util.EntityTypes
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens


class ContentRailsNewAdapter(
	private var rails: ArrayList<RailData>,
	private val helper: AppHelper,
	private val screen: String,
	private val action: AppAction,
) : RecyclerView.Adapter<ContentRailsNewAdapter.ViewHolder>() {

	lateinit var context: Context
	override fun onCreateViewHolder(
		parent: ViewGroup, viewType: Int,
	): ViewHolder {
		context = parent.context
		return ViewHolder(
			RowContentNewRailBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
		)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.binding.title.text = rails[position].name
		if (screen != Screens.SEARCH) {
			val params = holder.binding.title.layoutParams as ConstraintLayout.LayoutParams
			params.marginStart = context.resources.getDimensionPixelSize(R.dimen.dp75)
			params.marginEnd = context.resources.getDimensionPixelSize(R.dimen.dp75)
			holder.binding.title.layoutParams = params
		}
		holder.binding.listing.apply {
//			setLoopDirection(DpadLoopDirection.MAX)
			setFocusOutAllowed(throughFront = true, throughBack = true)
			setFocusOutSideAllowed(throughFront = true, throughBack = true)
//			setParentAlignment(
//				ParentAlignment(
//					edge = ParentAlignment.Edge.NONE,
//					offset = 0,
//					fraction = 0f,
//					preferKeylineOverEdge = false
//				)
//			)
//			setChildAlignment(
//				ChildAlignment(
//					offset = 0, fraction = 0f, includePadding = true
//				)
//			)
//			addOnViewHolderSelectedListener(object : OnViewHolderSelectedListener {
//				override fun onViewHolderSelected(
//					parent: RecyclerView,
//					child: RecyclerView.ViewHolder?,
//					position: Int,
//					subPosition: Int
//				) {
//					Logger.printWithTag(
//						"BrowseNew", "rails view holder selected - $position -- $subPosition"
//					)
//				}
//
//				override fun onViewHolderSelectedAndAligned(
//					parent: RecyclerView,
//					child: RecyclerView.ViewHolder?,
//					position: Int,
//					subPosition: Int
//				) {
//					Logger.printWithTag(
//						"BrowseNew", "rails view holder selected and aligned - $position -- $subPosition"
//					)
//				}
//			})
			if ((rails[position].entitiesType == EntityTypes.ARTIST || rails[position].entitiesType == EntityTypes.VENUE) && rails[position].cardType == CardTypes.CIRCLE) {
				val cardAdapter = CircularCardAdapter(action)
				cardAdapter.setEntities(rails[position].entities)
				rails[position].cardType?.let { cardAdapter.setCardType(it) }
				rails[position].entitiesType?.let { cardAdapter.setEntityType(it) }
				cardAdapter.setHelper(helper)
				cardAdapter.setAdapterPosition(position)
				cardAdapter.setScreen(screen)
				cardAdapter.setContinueWatching(rails[position].isContinueWatching)
				cardAdapter.setWatchList(rails[position].isWatchList)
				cardAdapter.setExpired(rails[position].isExpired)
				cardAdapter.setUserStats(rails[position].userStats)
				adapter = cardAdapter
			} else {
				val cardAdapter = PortraitCardAdapter(action)
				cardAdapter.setEntities(rails[position].entities)
				rails[position].cardType?.let { cardAdapter.setCardType(it) }
				rails[position].entitiesType?.let { cardAdapter.setEntityType(it) }
				cardAdapter.setHelper(helper)
				cardAdapter.setAdapterPosition(position)
				cardAdapter.setScreen(screen)
				cardAdapter.setContinueWatching(rails[position].isContinueWatching)
				cardAdapter.setWatchList(rails[position].isWatchList)
				cardAdapter.setExpired(rails[position].isExpired)
				cardAdapter.setUserStats(rails[position].userStats)
				adapter = cardAdapter
			}
		}
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	override fun getItemCount() = rails.size

	fun setRails(rails: ArrayList<RailData>) {
		this.rails = rails
		notifyDataSetChanged()
	}

	inner class ViewHolder(val binding: RowContentNewRailBinding) :
		RecyclerView.ViewHolder(binding.root), DpadViewHolder {
		override fun onViewHolderSelected() {
			super.onViewHolderSelected()
			Logger.printWithTag("BrowseNew", "inside rail view holder selected")
		}

		override fun onViewHolderSelectedAndAligned() {
			super.onViewHolderSelectedAndAligned()
			Logger.printWithTag("BrowseNew", "inside rail view holder selected and aligned")
		}

		override fun onViewHolderDeselected() {
			super.onViewHolderDeselected()
			Logger.printWithTag("BrowseNew", "inside rail view holder de-selected")
		}
	}
}