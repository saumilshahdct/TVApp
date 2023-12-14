package com.veeps.app.feature.home.ui

import android.content.res.Resources
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.FulfillmentResult
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse
import com.veeps.app.R
import com.veeps.app.core.BaseActivity
import com.veeps.app.databinding.ActivityHomeScreenBinding
import com.veeps.app.extension.goToPage
import com.veeps.app.extension.goToScreen
import com.veeps.app.extension.isFireTV
import com.veeps.app.extension.showToast
import com.veeps.app.extension.transformWidth
import com.veeps.app.feature.artist.ui.ArtistScreen
import com.veeps.app.feature.browse.ui.BrowseScreen
import com.veeps.app.feature.event.ui.EventScreen
import com.veeps.app.feature.home.viewModel.HomeViewModel
import com.veeps.app.feature.intro.ui.IntroScreen
import com.veeps.app.feature.search.ui.SearchScreen
import com.veeps.app.feature.shows.ui.ShowsScreen
import com.veeps.app.feature.venue.ui.VenueScreen
import com.veeps.app.feature.video.ui.VideoPlayerScreen
import com.veeps.app.feature.waitingRoom.ui.WaitingRoomScreen
import com.veeps.app.util.APIConstants
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppHelper
import com.veeps.app.util.AppPreferences
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.IntValue
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens
import com.veeps.app.widget.navigationMenu.NavigationItem
import com.veeps.app.widget.navigationMenu.NavigationItems
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.system.exitProcess


class HomeScreen : BaseActivity<HomeViewModel, ActivityHomeScreenBinding>(), NavigationItem,
	AppHelper, PurchasingListener {

	private var currentUserId: String = DEFAULT.EMPTY_STRING
	private var currentMarketplace: String = DEFAULT.EMPTY_STRING

	private fun getBackCallback(): OnBackPressedCallback {
		val backPressedCallback = object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				handleBack()
			}
		}
		return backPressedCallback
	}

	private fun handleBack(): Boolean {
		Logger.print(
			"Back Pressed on ${
				this@HomeScreen.localClassName.substringAfterLast(".")
			}"
		)
		if (viewModel.isNavigationMenuVisible.value!!) {
			AppConstants.lastKeyPressTime = System.currentTimeMillis()
			return clearNavigationMenuUI()
		} else {
			return if (supportFragmentManager.backStackEntryCount == 1) {
				if (!binding.errorContainer.isVisible) {
					showError(Screens.EXIT_APP, resources.getString(R.string.exit_app_warning))
				}
				false
			} else {
				supportFragmentManager.popBackStack()
				true
			}
		}
	}

	override fun goBack() {
		handleBack()
	}

	override fun getViewBinding(): ActivityHomeScreenBinding =
		ActivityHomeScreenBinding.inflate(layoutInflater)

	override fun showError(tag: String, message: String, description: String) {
		viewModel.isErrorVisible.postValue(true)
		viewModel.playerShouldPause.postValue(true)
		viewModel.contentHasLoaded.postValue(true)
		viewModel.errorMessage.postValue(message)
		binding.errorDescription.text = description
		when (tag) {
			APIConstants.removeWatchListEvents -> {
				viewModel.errorPositiveLabel.postValue(getString(R.string.ok_label))
				viewModel.errorNegativeLabel.postValue(getString(R.string.cancel_label))
				viewModel.isErrorPositiveApplicable.postValue(true)
				viewModel.isErrorNegativeApplicable.postValue(true)
			}

			Screens.EXIT_APP -> {
				viewModel.errorPositiveLabel.postValue(getString(R.string.yes_label))
				viewModel.errorNegativeLabel.postValue(getString(R.string.no_label))
				viewModel.isErrorPositiveApplicable.postValue(true)
				viewModel.isErrorNegativeApplicable.postValue(true)
			}

			Screens.PROFILE -> {
				viewModel.errorPositiveLabel.postValue(getString(R.string.sign_out_label))
				viewModel.errorNegativeLabel.postValue(getString(R.string.cancel_label))
				viewModel.isErrorPositiveApplicable.postValue(true)
				viewModel.isErrorNegativeApplicable.postValue(true)
			}

			Screens.BROWSE -> {
				viewModel.errorPositiveLabel.postValue(getString(R.string.ok_label))
				viewModel.errorNegativeLabel.postValue(getString(R.string.cancel_label))
				viewModel.isErrorPositiveApplicable.postValue(true)
				viewModel.isErrorNegativeApplicable.postValue(false)
			}

			else -> {
				viewModel.errorPositiveLabel.postValue(getString(R.string.ok_label))
				viewModel.errorNegativeLabel.postValue(getString(R.string.cancel_label))
				viewModel.isErrorPositiveApplicable.postValue(true)
				viewModel.isErrorNegativeApplicable.postValue(false)
			}
		}
		binding.positive.tag = tag
		binding.negative.tag = tag

		binding.negative.nextFocusUpId = binding.negative.id
		binding.negative.nextFocusDownId =
			if (binding.positive.isEnabled) binding.positive.id else binding.negative.id
		binding.negative.nextFocusLeftId = binding.negative.id
		binding.negative.nextFocusRightId = binding.negative.id
		binding.negative.nextFocusForwardId =
			if (binding.positive.isEnabled) binding.positive.id else binding.negative.id

		binding.positive.nextFocusUpId =
			if (binding.negative.isEnabled) binding.negative.id else binding.positive.id
		binding.positive.nextFocusDownId = binding.positive.id
		binding.positive.nextFocusLeftId = binding.positive.id
		binding.positive.nextFocusRightId = binding.positive.id
		binding.positive.nextFocusForwardId =
			if (binding.negative.isEnabled) binding.negative.id else binding.positive.id

		binding.errorContainer.apply {
			alpha = 0f
			visibility = View.VISIBLE
			animate().alpha(1f)
				.setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
				.setListener(null)
		}
		if (viewModel.isErrorNegativeApplicable.value!!) {
			binding.negative.postDelayed({
				binding.negative.requestFocus()
			}, AppConstants.keyPressShortDelayTime)
		} else {
			binding.positive.postDelayed({
				binding.positive.requestFocus()
			}, AppConstants.keyPressShortDelayTime)
		}
	}

	fun onErrorPositive(tag: Any?) {
		when (tag) {
			APIConstants.removeWatchListEvents -> {
				binding.errorContainer.visibility = View.GONE
				addRemoveWatchListEvent(binding.errorDescription.text.toString())
			}

			Screens.EXIT_APP -> {
//				finish()
				finishAffinity()
				exitProcess(0)
			}

			Screens.PROFILE -> {
				AppPreferences.removeAuthenticatedUser()
				goToScreen<IntroScreen>(true, AppConstants.TAG to Screens.INTRO)
			}

			Screens.BROWSE -> {
				binding.errorContainer.visibility = View.GONE
			}

			else -> {
				binding.errorContainer.visibility = View.GONE
			}
		}.also {
			viewModel.isErrorVisible.postValue(false)
			if (tag?.equals(Screens.EXIT_APP) == false) viewModel.playerShouldPause.postValue(false)
		}
	}

	fun onErrorNegative(tag: Any?) {
		when (tag) {
			Screens.EXIT_APP -> {
				viewModel.playerShouldPause.postValue(false)
				binding.errorContainer.visibility = View.GONE
			}

			Screens.BROWSE -> {
				binding.errorContainer.visibility = View.GONE
			}

			Screens.PROFILE -> {
				if (supportFragmentManager.findFragmentById(R.id.fragment_container) is BrowseScreen) viewModel.playerShouldPause.postValue(
					false
				)
				binding.errorContainer.visibility = View.GONE
			}

			else -> {
				binding.errorContainer.visibility = View.GONE
			}
		}.also {
			viewModel.isErrorVisible.postValue(false)
			viewModel.playerShouldPause.postValue(false)
		}
	}

	override fun onRendered(viewModel: HomeViewModel, binding: ActivityHomeScreenBinding) {
		backPressedCallback = getBackCallback()
		onBackPressedDispatcher.addCallback(this@HomeScreen, backPressedCallback)
		binding.apply {
			home = viewModel
			homeScreen = this@HomeScreen
			lifecycleOwner = this@HomeScreen
			errorContainer.visibility = View.GONE
			loader.visibility = View.GONE
			navigationMenuBackground.visibility = View.GONE
		}
		if (isFireTV) {
			initiateIAP()
		}
		notifyAppEvents()
	}

	private fun notifyAppEvents() {
		fetchAllWatchListEvents()
		binding.navigationMenu.setupDefaultNavigationMenu(NavigationItems.BROWSE_MENU)
		setupPageChange(
			true,
			BrowseScreen::class.java,
			bundleOf(AppConstants.TAG to NavigationItems.BROWSE),
			NavigationItems.BROWSE,
			true
		)
		setupBlur()
	}

	override fun fetchAllWatchListEvents() {
		viewModel.fetchAllWatchListEvents().observe(this@HomeScreen) { allWatchListEvents ->
			fetch(
				allWatchListEvents,
				isLoaderEnabled = false,
				canUserAccessScreen = true,
				shouldBeInBackground = true,
			) {
				allWatchListEvents.response?.let { railData ->
					viewModel.watchlistIds = (railData.data.map { it.id ?: DEFAULT.EMPTY_STRING })
				}
			}
		}
	}

	override fun setupPageChange(
		shouldReplace: Boolean,
		fragment: Class<out Fragment>?,
		arguments: Bundle,
		tag: String,
		shouldAddToBackStack: Boolean,
	) {
		Thread {
			runOnUiThread {
				binding.navigationMenu.onFocusChangeListener = null
			}
			if (fragment != null) {
				goToPage(
					supportFragmentManager,
					shouldReplace,
					fragment,
					arguments,
					tag,
					shouldAddToBackStack
				)
				runOnUiThread {
					hideNavigationMenu(binding.navigationMenu)
				}
				Timer().schedule(IntValue.NUMBER_100.toLong()) {
					runOnUiThread {
						binding.fragmentContainer.requestFocus()
					}
				}
			}
			Timer().schedule(if (fragment != null) IntValue.NUMBER_100.toLong() else IntValue.NUMBER_100.toLong()) {
				runOnUiThread {
					setupFocusOnNavigationMenu()
				}
			}
		}.start()
	}

	override fun goToVideoPlayer(eventId: String) {
		Logger.printWithTag("saumil", "Moving to player -- $eventId")
		goToScreen<VideoPlayerScreen>(
			false, AppConstants.TAG to Screens.VIDEO, "eventId" to eventId
		)
	}

	override fun goToWaitingRoom(
		eventId: String,
		eventLogo: String,
		eventTitle: String,
		eventDoorOpensAt: String,
		eventStreamStartsAt: String
	) {
		goToScreen<WaitingRoomScreen>(
			false,
			AppConstants.TAG to Screens.WAITING_ROOM,
			"eventId" to eventId,
			"eventTitle" to eventTitle,
			"eventLogo" to eventLogo,
			"eventDoorOpensAt" to eventDoorOpensAt,
			"eventStreamStartsAt" to eventStreamStartsAt
		)
	}

	override fun removeEventFromWatchList(eventId: String) {
		showError(
			APIConstants.removeWatchListEvents,
			"Are you sure you want to remove it from My Shows?",
			eventId
		)
	}

	private fun addRemoveWatchListEvent(eventId: String) {
		if (eventId.isNotBlank()) {
			viewModel.addRemoveWatchListEvent(hashMapOf("event_id" to eventId), true)
				.observe(this@HomeScreen) { addRemoveWatchList ->
					fetch(
						addRemoveWatchList,
						isLoaderEnabled = false,
						canUserAccessScreen = true,
						shouldBeInBackground = true,
					) {
						val fragment =
							supportFragmentManager.findFragmentById(R.id.fragment_container)
						if (fragment is ShowsScreen) {
							viewModel.focusItem.postValue(true)
						}
					}
				}
		}
	}

	private fun setupFocusOnNavigationMenu() {
		Logger.printWithTag("saumil", "here in set up focus on navigation menu")
		binding.navigationMenu.setOnFocusChangeListener { view, hasFocus ->
			if (hasFocus) {
				binding.navigationMenu.onFocusChangeListener = null
				showNavigationMenu(view)
			} else {
				hideNavigationMenu(view)
			}
		}
	}

	private fun showNavigationMenu(navigationMenu: View) {
		navigationMenu.transformWidth(R.dimen.expanded_navigation_menu_width, false)
		Logger.printWithTag(
			"saumil", "here in show navigation menu - does completely hidden required - false"
		)
		viewModel.isNavigationMenuVisible.postValue(true)
	}

	private fun hideNavigationMenu(navigationMenu: View) {
		val screen = supportFragmentManager.findFragmentById(R.id.fragment_container)
		val doesCompletelyHiddenRequired: Boolean = when (screen?.tag) {
			Screens.ARTIST -> {
				true
			}

			Screens.VENUE -> {
				true
			}

			Screens.EVENT -> {
				true
			}

			else -> {
				false
			}
		}
		Logger.printWithTag(
			"saumil",
			"here in hide navigation menu - screen ${screen?.tag} and does completely hidden required - $doesCompletelyHiddenRequired"
		)
		navigationMenu.transformWidth(
			R.dimen.collapsed_navigation_menu_width, doesCompletelyHiddenRequired
		)
		viewModel.isNavigationMenuVisible.postValue(false)
	}

	private fun setupBlur() {
		binding.errorContainer.setupWith(binding.container).setBlurRadius(12.5f)
		binding.navigationMenuContainer.setupWith(binding.container).setBlurRadius(12.5f)
	}

	override fun select(selectedItem: Int) {
		val tag = binding.navigationMenu.getSelectedItem(selectedItem)
		val arguments = bundleOf(AppConstants.TAG to tag)
		val shouldReplace = true
		val shouldAddToBackStack = true
		val isNotCurrentlySelected = binding.navigationMenu.getCurrentSelected() != selectedItem
		var fragment: Class<out Fragment>? = null

		val screen = supportFragmentManager.findFragmentById(R.id.fragment_container)
//		if (screen is BrowseScreen) {
//			viewModel.translateCarouselToBottom.postValue(true)
//		}
		if (isNotCurrentlySelected) {
			when (tag) {
				NavigationItems.PROFILE -> {
					showError(Screens.PROFILE, resources.getString(R.string.sign_out_warning))
				}

				NavigationItems.BROWSE -> {
					fragment = BrowseScreen::class.java
				}

				NavigationItems.SEARCH -> {
					fragment = SearchScreen::class.java
				}

				NavigationItems.MY_SHOWS -> {
					fragment = ShowsScreen::class.java
				}
			}
			if (fragment != null) binding.navigationMenu.setCurrentSelected(selectedItem)
		} else {
			when (tag) {
				NavigationItems.PROFILE -> {
					showError(Screens.PROFILE, resources.getString(R.string.sign_out_warning))
				}
			}
		}
		setupPageChange(shouldReplace, fragment, arguments, tag, shouldAddToBackStack)
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
		Logger.printWithTag("saumil", "On key down in home")
		val currentTime = System.currentTimeMillis()
		return if (currentTime - AppConstants.lastKeyPressTime < AppConstants.keyPressLongDelayTime) {
			true
		} else {
			return if (keyCode == KeyEvent.KEYCODE_BACK) {
				AppConstants.lastKeyPressTime = currentTime
				handleBack()
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && viewModel.isNavigationMenuVisible.value!! && !binding.errorContainer.isVisible) {
				AppConstants.lastKeyPressTime = currentTime
				return clearNavigationMenuUI()
			} else {
//				AppConstants.lastKeyPressTime = currentTime
				return keyCode == KeyEvent.KEYCODE_DPAD_LEFT && viewModel.isNavigationMenuVisible.value!! && !binding.errorContainer.isVisible
			}
		}
	}

	private fun knowFocusedView() {
		Thread(Runnable {
			var oldId = -1
			while (true) {
				val newView: View? = this@HomeScreen.currentFocus
				if (newView != null && newView.id != oldId) {
					oldId = newView.id
					val idName: String = try {
						resources.getResourceEntryName(newView.id)
					} catch (e: Resources.NotFoundException) {
						newView.id.toString()
					}
					Logger.print("Focused Id: \t" + idName + "\tClass: \t" + newView.javaClass)
				}
				try {
					Thread.sleep(100)
				} catch (e: InterruptedException) {
					e.printStackTrace()
				}
			}
		}).start()
	}

	private fun clearNavigationMenuUI(): Boolean {
		hideNavigationMenu(binding.navigationMenu)
		setupFocusOnNavigationMenu()
		return true
	}

	override fun onDestroy() {
		binding.navigationMenu.onFocusChangeListener = null
		super.onDestroy()
	}

	override fun translateCarouselToTop(needsOnTop: Boolean) {
		val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
		if (fragment is BrowseScreen) {
			viewModel.translateCarouselToTop.postValue(needsOnTop)
		}
		if (fragment is ArtistScreen) {
			viewModel.translateCarouselToTop.postValue(needsOnTop)
		}

		if (fragment is VenueScreen) {
			viewModel.translateCarouselToTop.postValue(needsOnTop)
		}

		if (fragment is EventScreen) {
			viewModel.translateCarouselToTop.postValue(needsOnTop)
		}
	}

	override fun translateCarouselToBottom(needsOnBottom: Boolean) {
		val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
		if (fragment is BrowseScreen) {
			viewModel.translateCarouselToBottom.postValue(needsOnBottom)
		}
		if (fragment is ArtistScreen) {
			viewModel.translateCarouselToBottom.postValue(needsOnBottom)
		}

		if (fragment is VenueScreen) {
			viewModel.translateCarouselToBottom.postValue(needsOnBottom)
		}

		if (fragment is EventScreen) {
			viewModel.translateCarouselToBottom.postValue(needsOnBottom)
		}
	}

	override fun selectNavigationMenu(selectedItem: Int) {
		binding.navigationMenu.setCurrentSelected(selectedItem)
	}

	override fun showErrorOnScreen(tag: String, message: String) {
		showError(tag, message)
	}

	override fun focusItem() {
		val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
		if (fragment is SearchScreen) {
			viewModel.focusItem.postValue(true)
		}
		if (fragment is ArtistScreen) {
			viewModel.focusItem.postValue(true)
		}

		if (fragment is VenueScreen) {
			viewModel.focusItem.postValue(true)
		}

		if (fragment is EventScreen) {
			viewModel.focusItem.postValue(true)
		}
	}

	override fun showToastMessage(message: String) {
		showToast(message)
	}

	override fun showNavigationMenu() {
		Logger.printWithTag("saumil", "here in show navigation menu")
		if (binding.navigationMenu.onFocusChangeListener != null) {
			if (!viewModel.isNavigationMenuVisible.value!!) {
				binding.navigationMenu.onFocusChangeListener = null
				showNavigationMenu(binding.navigationMenu)
			} else {
				clearNavigationMenuUI()
			}
		}
	}

	override fun completelyHideNavigationMenu() {
		hideNavigationMenu(binding.navigationMenu)
	}

	private fun initiateIAP() {
		PurchasingService.registerListener(this@HomeScreen, this@HomeScreen)
		PurchasingService.enablePendingPurchases()
		PurchasingService.getUserData()
		PurchasingService.getPurchaseUpdates(true)
	}

	fun fetchProduct() {
		val productSkus = hashSetOf("parentSKU")
		PurchasingService.getProductData(productSkus)
	}

	fun purchaseProduct() {
		PurchasingService.purchase("parentSKU")
	}

	override fun onUserDataResponse(response: UserDataResponse?) {
		when (response?.requestStatus) {
			UserDataResponse.RequestStatus.SUCCESSFUL -> {
				currentUserId = response.userData.userId
				currentMarketplace = response.userData.marketplace
				Logger.printWithTag(
					"IAP", "Got User Data  -- $currentUserId -- $currentMarketplace"
				)
			}

			UserDataResponse.RequestStatus.FAILED, UserDataResponse.RequestStatus.NOT_SUPPORTED, null -> {
				Logger.printWithTag("IAP", "User Data Request error")
			}
		}
	}

	override fun onProductDataResponse(productDataResponse: ProductDataResponse?) {
		when (productDataResponse?.requestStatus) {
			ProductDataResponse.RequestStatus.SUCCESSFUL -> {
				Logger.printWithTag(
					"IAP",
					"Got Product Data - ${productDataResponse.productData} -- ${productDataResponse.unavailableSkus}"
				)
				val products = productDataResponse.productData
				for (key in products.keys) {
					val product = products[key]
					Logger.printWithTag(
						"IAP:",
						"Product: ${product!!.title} \n Type: ${product.productType}\n SKU: ${product.sku}\n Price: ${product.price}\n Description: ${product.description}\n"
					)
				}
				for (sku in productDataResponse.unavailableSkus) {
					Logger.printWithTag("IAP", "Unavailable SKU:$sku")
				}
			}

			ProductDataResponse.RequestStatus.FAILED -> Logger.printWithTag(
				"IAP", "Product response - FAILED"
			)

			else -> {
				Logger.printWithTag("IAP", "IAP Not supported")
			}
		}
	}

	override fun onPurchaseResponse(purchaseResponse: PurchaseResponse?) {
		when (purchaseResponse?.requestStatus) {
			PurchaseResponse.RequestStatus.SUCCESSFUL -> {
				Logger.printWithTag("IAP", "Got Purchases ${purchaseResponse.toJSON()}")
				viewModel.receiptId = purchaseResponse.receipt.receiptId
				viewModel.purchaseAction.postValue("PURCHASED")
				PurchasingService.notifyFulfillment(
					purchaseResponse.receipt.receiptId, FulfillmentResult.FULFILLED
				)
			}

			PurchaseResponse.RequestStatus.FAILED -> {
				viewModel.purchaseAction.postValue("FAILED")
				Logger.printWithTag("IAP", "Fetch Purchase Failed")
			}

			else -> {
				viewModel.purchaseAction.postValue("FAILED")
				Logger.printWithTag("IAP", "IAP Not supported")
			}
		}
	}

	override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse?) {
		when (response?.requestStatus) {
			PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
				Logger.printWithTag("IAP", "Product purchase update")
				for (receipt in response.receipts) {
					if (!receipt.isCanceled) {
						Logger.printWithTag("IAP", "Product purchased -- $receipt")
					}
				}
				if (response.hasMore()) {
					Logger.printWithTag("IAP", "Product purchase has more data")
					PurchasingService.getPurchaseUpdates(true)
				}
			}

			PurchaseUpdatesResponse.RequestStatus.FAILED -> {
				Logger.printWithTag("IAP", "Purchase Failed")
			}

			else -> {
				Logger.printWithTag("IAP", "IAP Not supported")
			}
		}
	}
}