package com.veeps.app.feature.artist.ui

import android.os.Bundle
import android.text.Html
import android.view.KeyEvent
import android.view.View
import androidx.leanback.widget.BaseGridView
import androidx.recyclerview.widget.PagerSnapHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.veeps.app.R
import com.veeps.app.core.BaseFragment
import com.veeps.app.databinding.FragmentArtistDetailsScreenBinding
import com.veeps.app.feature.artist.viewModel.ArtistViewModel
import com.veeps.app.feature.contentRail.adapter.ContentRailsAdapter
import com.veeps.app.feature.contentRail.model.Entities
import com.veeps.app.feature.contentRail.model.RailData
import com.veeps.app.util.AppAction
import com.veeps.app.util.CardTypes
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.EntityTypes
import com.veeps.app.util.Image
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens
import com.veeps.app.widget.navigationMenu.NavigationItems
import io.noties.markwon.Markwon

class ArtistScreen : BaseFragment<ArtistViewModel, FragmentArtistDetailsScreenBinding>() {

	private var entity = ""
	private var entityId = ""
	private var entityScope = ""
	private var entities: ArrayList<Entities> = arrayListOf()
	var allEventsRail: ArrayList<RailData> = arrayListOf()
	private val action by lazy {
		object : AppAction {
			override fun focusDown() {
				if (!binding.description.text.isNullOrBlank()) {
					binding.description.requestFocus()
					if (!allEventsRail.none { it.entities.isNotEmpty() }) {
						binding.listing.visibility = View.GONE
					} else {
						binding.darkBackground.visibility = View.VISIBLE
						binding.carousel.visibility = View.GONE
					}
				}
			}

			override fun onAction() {
				Logger.print(
					"Action performed on ${
						this@ArtistScreen.javaClass.name.substringAfterLast(".")
					}"
				)
			}
		}
	}

	override fun getViewBinding(): FragmentArtistDetailsScreenBinding =
		FragmentArtistDetailsScreenBinding.inflate(layoutInflater)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.apply {
			artist = viewModel
			artistScreen = this@ArtistScreen
			lifecycleOwner = viewLifecycleOwner
			lifecycle.addObserver(viewModel)
			loader.visibility = View.VISIBLE
			darkBackground.visibility = View.GONE
			carousel.visibility = View.GONE
			listing.visibility = View.GONE
			description.visibility = View.GONE
			branding.requestFocus()
			entities = arrayListOf()
			allEventsRail = arrayListOf()
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
		entities = arrayListOf()
		fetchEntityUpcomingEvents()
		fetchEntityDetails()
	}

	private fun fetchEntityUpcomingEvents() {
		binding.branding.requestFocus()
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

				if (entities.isNotEmpty()) {
					val rail = RailData(
						name = getString(R.string.all_events_label),
						entities = entities,
						cardType = CardTypes.PORTRAIT,
						entitiesType = EntityTypes.EVENT
					)
					allEventsRail = arrayListOf()
					allEventsRail.add(rail)
				}
				if (allEventsRail.none { it.entities.isNotEmpty() }) {
					binding.listing.visibility = View.GONE
				} else {
					binding.listing.apply {
						setNumColumns(1)
						windowAlignment = BaseGridView.WINDOW_ALIGN_HIGH_EDGE
						windowAlignmentOffsetPercent = 0f
						isItemAlignmentOffsetWithPadding = true
						itemAlignmentOffsetPercent = 0f
						adapter = ContentRailsAdapter(
							rails = allEventsRail, helper, Screens.ARTIST, action
						)
						onFlingListener = PagerSnapHelper()
					}
					binding.listing.visibility = View.VISIBLE
				}
			}
		}
	}

	private fun fetchEntityDetails() {
		binding.branding.requestFocus()
		viewModel.fetchEntityDetails(entity, entityId)
			.observe(viewLifecycleOwner) { entityDetails ->
				fetch(
					entityDetails,
					isLoaderEnabled = true,
					canUserAccessScreen = false,
					shouldBeInBackground = false,
				) {
					entityDetails.response?.let { artistResponse ->
						artistResponse.data.let { artist ->
							artist.let {
								val posterImage = (artist?.landscapeUrl
									?: DEFAULT.EMPTY_STRING).replace(Image.DEFAULT, Image.HERO)
								val logoImage = (artist?.logoUrl ?: DEFAULT.EMPTY_STRING).replace(
									Image.DEFAULT, Image.LOGO
								)
								val title = artist?.name ?: DEFAULT.EMPTY_STRING
								val description = artist?.bio ?: DEFAULT.EMPTY_STRING

								Glide.with(binding.image.context).load(posterImage)
									.diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.image)

								Glide.with(binding.logo.context).load(logoImage)
									.diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.logo)

								binding.title.text = title.ifBlank { DEFAULT.EMPTY_STRING }
								context?.let {
									Markwon.create(it).setMarkdown(binding.description, description)
								} ?: run {
									binding.description.text =
										Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)
											.ifBlank { DEFAULT.EMPTY_STRING }
								}
								binding.description.visibility = View.VISIBLE
								binding.darkBackground.visibility = View.GONE
								binding.carousel.visibility = View.VISIBLE
								binding.title.requestFocus()
								binding.branding.isFocusable = false
								binding.branding.isFocusableInTouchMode = false
							}
						}
					}
				}
			}
	}

	private fun notifyAppEvents() {
		homeViewModel.translateCarouselToTop.observe(viewLifecycleOwner) { shouldTranslate ->
			if (shouldTranslate && viewModel.isVisible.value == true) {
				binding.darkBackground.visibility = View.VISIBLE
				binding.carousel.visibility = View.GONE
			}
		}
		homeViewModel.translateCarouselToBottom.observe(viewLifecycleOwner) { shouldTranslate ->
			if (shouldTranslate && viewModel.isVisible.value == true) {
				binding.carousel.visibility = View.VISIBLE
				binding.title.requestFocus()
				binding.darkBackground.visibility = View.GONE
			}
		}

		binding.title.setOnKeyListener { _, keyCode, keyEvent ->
			if (keyEvent.action == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (allEventsRail.none { it.entities.isNotEmpty() }) {
						if (!binding.description.text.isNullOrBlank()) {
							binding.darkBackground.visibility = View.VISIBLE
							binding.description.requestFocus()
							binding.carousel.visibility = View.GONE
						}
					} else {
						binding.darkBackground.visibility = View.VISIBLE
						binding.carousel.visibility = View.GONE
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
						if (!allEventsRail.none { it.entities.isNotEmpty() }) {
							binding.listing.visibility = View.VISIBLE
							binding.listing.requestFocus()
						} else {
							binding.carousel.visibility = View.VISIBLE
							binding.title.requestFocus()
							binding.darkBackground.visibility = View.GONE
						}
					} else {
						binding.scrollView.pageScroll(View.FOCUS_UP)
					}
				}
			}
			return@setOnKeyListener false
		}

		viewModel.isVisible.observeForever { isVisible ->
			if (isVisible) {
				helper.selectNavigationMenu(NavigationItems.NO_MENU)
				helper.completelyHideNavigationMenu()
			}
			Logger.print(
				"Visibility Changed to $isVisible On ${
					this@ArtistScreen.javaClass.name.substringAfterLast(".")
				}"
			)
		}
	}
}