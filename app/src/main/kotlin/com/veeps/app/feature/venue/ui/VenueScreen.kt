package com.veeps.app.feature.venue.ui

import android.os.Bundle
import android.text.Html
import android.view.KeyEvent
import android.view.View
import androidx.leanback.widget.BaseGridView
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SimpleItemAnimator
import com.veeps.app.R
import com.veeps.app.core.BaseFragment
import com.veeps.app.databinding.FragmentVenueDetailsScreenBinding
import com.veeps.app.extension.loadImage
import com.veeps.app.feature.contentRail.adapter.ContentRailsAdapter
import com.veeps.app.feature.contentRail.model.Entities
import com.veeps.app.feature.contentRail.model.RailData
import com.veeps.app.feature.venue.viewModel.VenueViewModel
import com.veeps.app.util.AppAction
import com.veeps.app.util.CardTypes
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.EntityTypes
import com.veeps.app.util.ImageTags
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens
import com.veeps.app.widget.navigationMenu.NavigationItems
import io.noties.markwon.Markwon

class VenueScreen : BaseFragment<VenueViewModel, FragmentVenueDetailsScreenBinding>() {

	private var entity = ""
	private var entityId = ""
	private var entityScope = ""
	private var entities: ArrayList<Entities> = arrayListOf()
	private val action by lazy {
		object : AppAction {
			override fun onAction() {
				Logger.print(
					"Action performed on ${
						this@VenueScreen.javaClass.name.substringAfterLast(".")
					}"
				)
			}
		}
	}

	override fun getViewBinding(): FragmentVenueDetailsScreenBinding =
		FragmentVenueDetailsScreenBinding.inflate(layoutInflater)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.apply {
			venue = viewModel
			venueScreen = this@VenueScreen
			lifecycleOwner = viewLifecycleOwner
			lifecycle.addObserver(viewModel)
			loader.visibility = View.GONE
			image.tag = ""
			title.requestFocus()
			entities.toMutableList().clear()
			viewModel.allEventsRail.postValue(arrayListOf())
		}
		notifyAppEvents()
		loadAppContent()
	}

	private fun loadAppContent() {
		helper.completelyHideNavigationMenu()
		if (arguments != null) {
			entity = requireArguments().getString("entity").toString()
			entityId = requireArguments().getString("entityId").toString()
			entityScope = "$entity.$entityId"
			entity = entity.plus("s")
		}
		fetchEntityUpcomingEvents()
		fetchEntityDetails()
	}

	private fun fetchEntityUpcomingEvents() {
		viewModel.fetchEntityUpcomingEvents(entityScope)
			.observe(viewLifecycleOwner) { entityDetails ->
				fetch(
					entityDetails,
					isLoaderEnabled = false,
					canUserAccessScreen = true,
					shouldBeInBackground = true,
				) {
					entityDetails.response?.let { railData ->
						if (railData.data.isNotEmpty()) {
							entities.addAll(railData.data)
						}
					}
					fetchEntityOnDemandEvents()
				}
			}
	}

	private fun fetchEntityOnDemandEvents() {
		viewModel.fetchEntityOnDemandEvents(entityScope)
			.observe(viewLifecycleOwner) { entityDetails ->
				fetch(
					entityDetails,
					isLoaderEnabled = false,
					canUserAccessScreen = true,
					shouldBeInBackground = true,
				) {
					entityDetails.response?.let { railData ->
						if (railData.data.isNotEmpty()) {
							entities.addAll(railData.data)
						}
					}
					fetchEntityPastEvents()
				}
			}
	}

	private fun fetchEntityPastEvents() {
		viewModel.fetchEntityPastEvents(entityScope).observe(viewLifecycleOwner) { entityDetails ->
			fetch(
				entityDetails,
				isLoaderEnabled = false,
				canUserAccessScreen = true,
				shouldBeInBackground = true,
			) {
				entityDetails.response?.let { railData ->
					if (railData.data.isNotEmpty()) {
						entities.addAll(railData.data)
					}
				}
				val rails: ArrayList<RailData> = arrayListOf()
				if (entities.isNotEmpty()) {
					val allEventsRail = RailData(
						name = getString(R.string.all_events_label),
						entities = entities,
						cardType = CardTypes.PORTRAIT,
						entitiesType = EntityTypes.EVENT
					)
					rails.add(allEventsRail)
				}
				viewModel.allEventsRail.postValue(rails)
			}
		}
	}

	private fun fetchEntityDetails() {
		viewModel.fetchEntityDetails(entity, entityId)
			.observe(viewLifecycleOwner) { entityDetails ->
				fetch(
					entityDetails,
					isLoaderEnabled = true,
					canUserAccessScreen = true,
					shouldBeInBackground = true,
				) {
					entityDetails.response?.let { venueResponse ->
						venueResponse.data.let { venue ->
							venue.let {
								val posterImage = venue?.landscapeUrl ?: DEFAULT.EMPTY_STRING
								val logoImage = venue?.logoUrl ?: DEFAULT.EMPTY_STRING
								val title = venue?.name ?: DEFAULT.EMPTY_STRING
								val description = venue?.bio ?: DEFAULT.EMPTY_STRING
								binding.image.loadImage(posterImage, ImageTags.HERO)
								binding.logo.loadImage(logoImage, ImageTags.LOGO)
								binding.title.text = title.ifBlank { DEFAULT.EMPTY_STRING }
								context?.let {
									Markwon.create(it).setMarkdown(binding.description, description)
								} ?: run {
									binding.description.text =
										Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)
											.ifBlank { DEFAULT.EMPTY_STRING }
								}
							}
						}
					}
				}
			}
	}

	private fun notifyAppEvents() {
		homeViewModel.focusItem.observe(viewLifecycleOwner) { hasFocus ->
			if (hasFocus && !binding.description.text.isNullOrBlank()) {
				binding.description.requestFocus()
				if (viewModel.allEventsRail.value?.none { it.entities.isNotEmpty() } == false) {
					binding.listing.visibility = View.GONE
				} else {
					binding.image.visibility = View.GONE
					binding.logo.visibility = View.GONE
					binding.title.visibility = View.GONE
				}
			}
		}
		homeViewModel.translateCarouselToTop.observe(viewLifecycleOwner) { shouldTranslate ->
			if (shouldTranslate && viewModel.isVisible.value == true) {
				binding.image.visibility = View.GONE
				binding.logo.visibility = View.GONE
				binding.title.visibility = View.GONE
			}
		}
		homeViewModel.translateCarouselToBottom.observe(viewLifecycleOwner) { shouldTranslate ->
			if (shouldTranslate && viewModel.isVisible.value == true) {
				binding.image.visibility = View.VISIBLE
				binding.logo.visibility = View.VISIBLE
				binding.title.visibility = View.VISIBLE
				binding.title.requestFocus()
			}
		}

		binding.title.setOnKeyListener { _, keyCode, keyEvent ->
			if (keyEvent.action == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (viewModel.allEventsRail.value?.none { it.entities.isNotEmpty() } == true) {
						if (!binding.description.text.isNullOrBlank()) {
							binding.description.requestFocus()
							binding.image.visibility = View.GONE
							binding.logo.visibility = View.GONE
							binding.title.visibility = View.GONE
						}
					} else {
						binding.image.visibility = View.GONE
						binding.logo.visibility = View.GONE
						binding.title.visibility = View.GONE
					}
				}
			}
			return@setOnKeyListener false
		}

		binding.description.setOnKeyListener { _, keyCode, keyEvent ->
			if (keyEvent.action == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					binding.scrollView.pageScroll(View.FOCUS_DOWN)
				}
				if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
					if (binding.scrollView.scrollY == 0) {
						if (viewModel.allEventsRail.value?.none { it.entities.isNotEmpty() } == false) {
							binding.listing.visibility = View.VISIBLE
							binding.listing.requestFocus()
						} else {
							binding.image.visibility = View.VISIBLE
							binding.logo.visibility = View.VISIBLE
							binding.title.visibility = View.VISIBLE
							binding.title.requestFocus()
						}
					} else {
						binding.scrollView.pageScroll(View.FOCUS_UP)
					}
				}
			}
			return@setOnKeyListener false
		}

		viewModel.allEventsRail.observe(viewLifecycleOwner) { rails ->
			if (rails.none { it.entities.isNotEmpty() }) {
				binding.listing.visibility = View.GONE
			} else {
				binding.listing.apply {
					itemAnimator = null
					setNumColumns(1)
					setHasFixedSize(true)
					windowAlignment = BaseGridView.WINDOW_ALIGN_HIGH_EDGE
					windowAlignmentOffsetPercent = 0f
					isItemAlignmentOffsetWithPadding = true
					itemAlignmentOffsetPercent = 0f
					adapter = ContentRailsAdapter(rails = rails, helper, Screens.VENUE, action)
					onFlingListener = PagerSnapHelper()
				}
				binding.listing.visibility = View.VISIBLE
			}
		}

		viewModel.isVisible.observeForever  { isVisible ->
			if (isVisible) {
				helper.selectNavigationMenu(NavigationItems.NO_MENU)
				helper.completelyHideNavigationMenu()
			}
			Logger.print(
				"Visibility Changed to $isVisible On ${
					this@VenueScreen.javaClass.name.substringAfterLast(".")
				}"
			)
		}
	}
}