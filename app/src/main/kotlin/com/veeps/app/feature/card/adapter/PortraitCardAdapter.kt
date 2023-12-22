package com.veeps.app.feature.card.adapter

import android.content.Context
import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
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
import com.veeps.app.databinding.RowCardPortraitBinding
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

class PortraitCardAdapter(private val action: AppAction) :
	RecyclerView.Adapter<PortraitCardAdapter.ViewHolder>() {

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
			RowCardPortraitBinding.inflate(
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
			holder.binding.container.setCardBackgroundColor(
				if (hasFocus) context.getColor(R.color.white) else context.getColor(
					android.R.color.transparent
				)
			)
		}

		holder.binding.foreground.visibility =
			if (entities[position].isOfType(EventTypes.EXPIRED)) View.VISIBLE else View.GONE

		if (entitiesType == EntityTypes.ARTIST || entitiesType == EntityTypes.VENUE) {
			val image = entities[position].portraitUrl ?: DEFAULT.EMPTY_STRING
			val title = entities[position].name ?: DEFAULT.EMPTY_STRING
			holder.binding.title.visibility = View.GONE
			holder.binding.logoLabel.text = title
			holder.binding.logo.visibility = View.GONE

				val newResource = image.replace(Image.DEFAULT, Image.CARD)
				Glide.with(holder.binding.thumbnail.context).load(newResource)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.transition(DrawableTransitionOptions.withCrossFade())
					.transform(CenterCrop(), RoundedCorners(IntValue.NUMBER_10))
					.error(R.drawable.rounded_card_background_black).into(holder.binding.thumbnail)
				Logger.printMessage("Card Image url with optimized ($newResource) is requested to load.")

			holder.binding.dateContainer.visibility = View.GONE
		} else {
			when (val badgeStatus: String = AppUtil.getBadgeStatus(
				entities[position], isContinueWatching, screen == Screens.SHOWS
			)) {
				BadgeStatus.LIVE -> {
					holder.binding.liveBadgeContainer.visibility = View.VISIBLE
					holder.binding.dateContainer.visibility = View.GONE
				}

				BadgeStatus.NOTHING,
				-> {
					holder.binding.liveBadgeContainer.visibility = View.GONE
					holder.binding.dateContainer.visibility = View.GONE
				}

				else -> {
					holder.binding.liveBadgeContainer.visibility = View.GONE
					holder.binding.date.text = badgeStatus
					holder.binding.dateContainer.visibility =
						if (badgeStatus.isBlank()) View.GONE else View.VISIBLE
				}
			}

			if (isContinueWatching) {
				holder.binding.continueWatchingProgress.visibility = View.VISIBLE
				val stats = userStats.filter { it.eventId == entities[position].eventId }
				if (stats.size == 1 && ((stats[0].cursor / stats[0].duration) * 100) < 95) {
					holder.binding.continueWatchingProgress.progress = stats[0].cursor.roundToInt()
					holder.binding.continueWatchingProgress.max = stats[0].duration.roundToInt()
				}
			} else {
				holder.binding.continueWatchingProgress.visibility = View.GONE
			}
			var image: String
			entities[position].presentation.let {
				image = it.portraitUrl ?: DEFAULT.EMPTY_STRING
			}
			val title = entities[position].eventName
			val artistTitle =
				if (entities[position].lineup.isNotEmpty()) entities[position].lineup[0].name else ""
			entities[position].presentation.let {
				if (!it.badgeBgColor.isNullOrBlank()) holder.binding.badgeContainer.background.colorFilter =
					BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
						Color.parseColor(it.badgeBgColor), BlendModeCompat.SRC_OVER
					)

				if (!it.badgeFgColor.isNullOrBlank()) holder.binding.badge.setTextColor(
					Color.parseColor(
						it.badgeFgColor
					)
				)

				if (it.badgeLabel.isNullOrBlank()) {
					holder.binding.badgeContainer.visibility = View.GONE
				} else {
					val firstLetter = it.badgeLabel!!.substring(0, 1).uppercase(Locale.ROOT)
					val labelWOFirstLetter = it.badgeLabel!!.substring(1)
					val label = "$firstLetter$labelWOFirstLetter"
					holder.binding.badge.text = label
				}
			}
			holder.binding.title.text = title
			holder.binding.logoLabel.text = artistTitle
			holder.binding.logo.visibility = View.GONE

				val newResource = image.replace(Image.DEFAULT, Image.CARD)
				Glide.with(holder.binding.thumbnail.context).load(newResource)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.transition(DrawableTransitionOptions.withCrossFade())
					.transform(CenterCrop(), RoundedCorners(IntValue.NUMBER_10))
					.error(R.drawable.rounded_card_background_black).into(holder.binding.thumbnail)
				Logger.printMessage("Card Image url with optimized ($newResource) is requested to load.")

			holder.binding.dateContainer.setupWith(holder.binding.container).setBlurRadius(12.5f)
			holder.binding.dateContainer.outlineProvider = ViewOutlineProvider.BACKGROUND
			holder.binding.dateContainer.clipToOutline = true
		}
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

	inner class ViewHolder(val binding: RowCardPortraitBinding) :
		RecyclerView.ViewHolder(binding.root), DpadViewHolder {
		override fun onViewHolderSelected() {
			super.onViewHolderSelected()
			Logger.printWithTag("BrowseNew", "inside portrait view holder selected")
		}

		override fun onViewHolderSelectedAndAligned() {
			super.onViewHolderSelectedAndAligned()
			Logger.printWithTag("BrowseNew", "inside portrait view holder selected and aligned")
		}

		override fun onViewHolderDeselected() {
			super.onViewHolderDeselected()
			Logger.printWithTag("BrowseNew", "inside portrait view holder de-selected")
		}
	}
}