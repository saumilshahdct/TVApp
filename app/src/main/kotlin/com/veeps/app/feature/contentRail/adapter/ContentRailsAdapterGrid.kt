package com.veeps.app.feature.contentRail.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.leanback.widget.BaseGridView
import androidx.recyclerview.widget.RecyclerView
import com.veeps.app.R
import com.veeps.app.databinding.RowContentRailGridBinding
import com.veeps.app.feature.card.adapter.CardAdapterGrid
import com.veeps.app.feature.contentRail.model.RailData
import com.veeps.app.util.AppAction
import com.veeps.app.util.AppHelper
import com.veeps.app.util.Screens

class ContentRailsAdapterGrid(
	private val rails: ArrayList<RailData>,
	private val helper: AppHelper,
	private val screen: String,
	private val action: AppAction,
) : RecyclerView.Adapter<ContentRailsAdapterGrid.ViewHolder>() {

	lateinit var context: Context
	override fun onCreateViewHolder(
		parent: ViewGroup, viewType: Int,
	): ViewHolder {
		context = parent.context
		val binding =
			RowContentRailGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		binding.root.layoutParams.height = parent.height
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.binding.title.text = rails[position].name
		holder.binding.listing.apply {
			itemAnimator = null
			setNumColumns(
				if (screen == Screens.SEARCH && rails[position].name == context.getString(
						R.string.artists_label
					)
				) 4 else 3
			)
			windowAlignment = BaseGridView.WINDOW_ALIGN_HIGH_EDGE
			windowAlignmentOffsetPercent = 0f
			isItemAlignmentOffsetWithPadding = true
			itemAlignmentOffsetPercent = 0f

			val cardAdapterGrid = CardAdapterGrid(action)
			cardAdapterGrid.setEntities(rails[position].entities)
			rails[position].cardType?.let { cardAdapterGrid.setCardType(it) }
			rails[position].entitiesType?.let { cardAdapterGrid.setEntityType(it) }
			cardAdapterGrid.setHelper(helper)
			cardAdapterGrid.setAdapterPosition(position)
			cardAdapterGrid.setScreen(screen)
			cardAdapterGrid.setContinueWatching(rails[position].isContinueWatching)
			cardAdapterGrid.setWatchList(rails[position].isWatchList)
			cardAdapterGrid.setExpired(rails[position].isExpired)
			cardAdapterGrid.setUserStats(rails[position].userStats)

			adapter = cardAdapterGrid
		}
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	override fun getItemCount() = rails.size

	inner class ViewHolder(val binding: RowContentRailGridBinding) :
		RecyclerView.ViewHolder(binding.root)

}