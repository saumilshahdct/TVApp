package com.veeps.app.feature.card.adapter

import android.content.Context
import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.veeps.app.R
import com.veeps.app.databinding.RowCardBinding
import com.veeps.app.databinding.RowCardCirclularBinding
import com.veeps.app.extension.dpToPx
import com.veeps.app.extension.isOfType
import com.veeps.app.feature.artist.ui.ArtistScreen
import com.veeps.app.feature.contentRail.model.Entities
import com.veeps.app.feature.contentRail.model.UserStats
import com.veeps.app.feature.event.ui.EventScreen
import com.veeps.app.feature.venue.ui.VenueScreen
import com.veeps.app.util.AppAction
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppHelper
import com.veeps.app.util.AppUtil
import com.veeps.app.util.BadgeStatus
import com.veeps.app.util.CardTypes
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.DateTimeCompareDifference
import com.veeps.app.util.EntityTypes
import com.veeps.app.util.EventTypes
import com.veeps.app.util.Image
import com.veeps.app.util.IntValue
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

class CircularCardAdapter(private val action: AppAction) :
	RecyclerView.Adapter<CircularCardAdapter.ViewHolder>() {

	private lateinit var context: Context
	private var entities: ArrayList<Entities> = arrayListOf()
	private var cardType: String? = CardTypes.PORTRAIT
	private var entitiesType: String? = EntityTypes.EVENT
	private lateinit var helper: AppHelper
	private var adapterPosition: Int = 0
	private var screen: String = Screens.BROWSE
	private var isContinueWatching: Boolean = false
	private var isWatchList: Boolean = false
	private var isExpired: Boolean = false
	private var userStats: ArrayList<UserStats> = arrayListOf()
	override fun onCreateViewHolder(
		parent: ViewGroup, viewType: Int,
	): ViewHolder {
		context = parent.context
		return ViewHolder(
			RowCardCirclularBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
		)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.binding.container.setOnClickListener {
			if (entitiesType == EntityTypes.ARTIST) {
				action.onArtist(entities[position])
			} else if (entitiesType == EntityTypes.VENUE) {
				action.onVenue(entities[position])
			} else if (screen == Screens.SHOWS) {
				if (isExpired) {
					action.onEvent(entities[position])
				} else {
					if (!entities[position].eventStreamStartsAt.isNullOrBlank() && AppUtil.compare(
							entities[position].eventStreamStartsAt
						) == DateTimeCompareDifference.GREATER_THAN
					) {
						action.onAction(entities[position])
					} else {
						helper.goToVideoPlayer(
							entities[position].eventId ?: entities[position].id
							?: DEFAULT.EMPTY_STRING
						)
					}
				}
			} else if (isContinueWatching) {
				if (!entities[position].eventStreamStartsAt.isNullOrBlank() && AppUtil.compare(
						entities[position].eventStreamStartsAt
					) == DateTimeCompareDifference.GREATER_THAN
				) {
					action.onAction(entities[position])
				} else {
					helper.goToVideoPlayer(
						entities[position].eventId ?: entities[position].id ?: DEFAULT.EMPTY_STRING
					)
				}
			} else {
				action.onEvent(entities[position])
			}
		}

		if (isWatchList) holder.binding.container.setOnLongClickListener {
			helper.removeEventFromWatchList(entities[position].id ?: DEFAULT.EMPTY_STRING)
			true
		}

		holder.binding.container.setOnFocusChangeListener { _, hasFocus ->
			holder.binding.thumbnailContainer.setImageResource(if (hasFocus) R.drawable.rounded_card_image_background_focused else (if (entitiesType == EntityTypes.ARTIST) R.drawable.rounded_card_image_background_white_10 else R.drawable.rounded_card_image_background_transparent))
		}

		holder.binding.follow.setOnFocusChangeListener { _, hasFocus ->
			holder.binding.followLabel.setTextColor(context.getColor(if (hasFocus) R.color.black else R.color.white))
			holder.binding.followIcon.setImageResource(if (hasFocus) R.drawable.add_black else R.drawable.add_white)
		}

		val image = entities[position].landscapeUrl ?: DEFAULT.EMPTY_STRING
		val title = entities[position].name ?: DEFAULT.EMPTY_STRING
		holder.binding.title.text = title
		if (entitiesType == EntityTypes.ARTIST) holder.binding.thumbnailContainer.setImageResource(
			R.drawable.rounded_card_image_background_white_10
		)

			Logger.printWithTag("BrowseNew", "width -- ${holder.binding.thumbnail.measuredWidth}")
			val newResource = image.replace(Image.DEFAULT, Image.CIRCLE)
			Glide.with(holder.binding.thumbnail.context).load(newResource)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.transition(DrawableTransitionOptions.withCrossFade()).transform(
					CenterCrop(), RoundedCorners(holder.binding.thumbnail.dpToPx(72.dp))
				).error(R.drawable.rounded_card_background_white_10).into(holder.binding.thumbnail)
			Logger.printMessage("Artist or Venue Image url with optimized ($newResource) is requested to load.")

//				holder.binding.container.nextFocusDownId = R.id.artist_venue_follow
		holder.binding.follow.visibility = View.GONE
		holder.binding.container.visibility = View.VISIBLE
		holder.binding.container.visibility = View.GONE
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	override fun getItemCount() = entities.size

	fun setEntities(entities: ArrayList<Entities>) {
		this.entities = entities
	}

	fun setCardType(cardType: String) {
		this.cardType = cardType
	}

	fun setEntityType(entitiesType: String) {
		this.entitiesType = entitiesType
	}

	fun setHelper(helper: AppHelper) {
		this.helper = helper
	}

	fun setAdapterPosition(adapterPosition: Int) {
		this.adapterPosition = adapterPosition
	}

	fun setScreen(screen: String) {
		this.screen = screen
	}

	fun setContinueWatching(isContinueWatching: Boolean) {
		this.isContinueWatching = isContinueWatching
	}

	fun setWatchList(isWatchList: Boolean) {
		this.isWatchList = isWatchList
	}

	fun setExpired(isExpired: Boolean) {
		this.isExpired = isExpired
	}

	fun setUserStats(userStats: ArrayList<UserStats>) {
		this.userStats = userStats
	}

	inner class ViewHolder(val binding: RowCardCirclularBinding) :
		RecyclerView.ViewHolder(binding.root), DpadViewHolder {
		override fun onViewHolderSelected() {
			super.onViewHolderSelected()
			Logger.printWithTag("BrowseNew", "inside circular view holder selected")
		}

		override fun onViewHolderSelectedAndAligned() {
			super.onViewHolderSelectedAndAligned()
			Logger.printWithTag("BrowseNew", "inside circular view holder selected and aligned")
		}

		override fun onViewHolderDeselected() {
			super.onViewHolderDeselected()
			Logger.printWithTag("BrowseNew", "inside circular view holder de-selected")
		}
	}
}