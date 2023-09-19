package com.veeps.app.feature.home.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.veeps.app.R
import com.veeps.app.core.BaseActivity
import com.veeps.app.databinding.ActivityHomeScreenBinding
import com.veeps.app.extension.goToPage
import com.veeps.app.feature.browse.ui.BrowseScreen
import com.veeps.app.feature.home.viewModel.HomeViewModel
import com.veeps.app.feature.search.ui.SearchScreen
import com.veeps.app.feature.shows.ui.ShowsScreen
import com.veeps.app.feature.user.ui.ProfileScreen
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppHelper
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens
import com.veeps.app.widget.navigationMenu.NavigationItem
import com.veeps.app.widget.navigationMenu.NavigationItems
import com.veeps.app.widget.navigationMenu.NavigationMenu
import kotlin.system.exitProcess


class HomeScreen : BaseActivity<HomeViewModel, ActivityHomeScreenBinding>(), NavigationItem, AppHelper {

	private var isNavigationMenuVisible = false
	override fun isSplashScreenRequired(): Boolean {
		return false
	}

	override fun getBackCallback(): OnBackPressedCallback {
		val backPressedCallback = object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				if (isNavigationMenuVisible) {
					AppConstants.lastKeyPressTime = System.currentTimeMillis()
					clearNavigationMenuUI()
				} else {
					if (supportFragmentManager.backStackEntryCount == 1) {
						if (binding.errorContainer.visibility == View.GONE) {
							showError(Screens.EXIT_APP, resources.getString(R.string.exit_app))
						}
					} else {
						supportFragmentManager.popBackStack()
					}
				}
				Logger.print(
					"Back Pressed on ${
						this@HomeScreen.localClassName.substringAfterLast(
							"."
						)
					}"
				)
			}
		}
		return backPressedCallback
	}

	override fun getViewBinding(): ActivityHomeScreenBinding =
		ActivityHomeScreenBinding.inflate(layoutInflater)

	override fun showError(tag: String, message: String) {
		viewModel.contentHasLoaded.postValue(true)
		viewModel.errorMessage.postValue(message)
		when (tag) {
			Screens.EXIT_APP -> {
				viewModel.errorPositiveLabel.postValue(getString(R.string.yes_label))
				viewModel.errorNegativeLabel.postValue(getString(R.string.no_label))
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

		binding.positive.nextFocusUpId = binding.positive.id
		binding.positive.nextFocusDownId = binding.positive.id
		binding.positive.nextFocusLeftId = binding.positive.id
		binding.positive.nextFocusRightId =
			if (binding.negative.isEnabled) binding.negative.id else binding.positive.id
		binding.positive.nextFocusForwardId =
			if (binding.negative.isEnabled) binding.negative.id else binding.positive.id

		binding.negative.nextFocusUpId = binding.negative.id
		binding.negative.nextFocusDownId = binding.negative.id
		binding.negative.nextFocusLeftId =
			if (binding.positive.isEnabled) binding.positive.id else binding.negative.id
		binding.negative.nextFocusRightId = binding.negative.id
		binding.negative.nextFocusForwardId =
			if (binding.positive.isEnabled) binding.positive.id else binding.negative.id

		binding.errorContainer.visibility = View.VISIBLE
		binding.errorContainer.apply {
			alpha = 0f
			visibility = View.VISIBLE
			animate().alpha(1f)
				.setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
				.setListener(null)
		}
		binding.positive.postDelayed({
			binding.positive.requestFocus()
		}, AppConstants.keyPressShortDelayTime)
	}

	fun onErrorPositive(tag: Any?) {
		when (tag) {
			Screens.EXIT_APP -> {
//				finish()
				finishAffinity()
				exitProcess(0)
			}

			Screens.BROWSE -> {
				binding.errorContainer.visibility = View.GONE
			}

			else -> {
				binding.errorContainer.visibility = View.GONE
			}
		}
	}

	fun onErrorNegative(tag: Any?) {
		when (tag) {
			Screens.EXIT_APP -> {
				binding.errorContainer.visibility = View.GONE
			}

			Screens.BROWSE -> {
				binding.errorContainer.visibility = View.GONE
			}

			else -> {
				binding.errorContainer.visibility = View.GONE
			}
		}
	}

	override fun onRendered(viewModel: HomeViewModel, binding: ActivityHomeScreenBinding) {
		binding.apply {
			home = viewModel
			homeScreen = this@HomeScreen
			lifecycleOwner = this@HomeScreen
			errorContainer.visibility = View.GONE
			loader.visibility = View.GONE
			navigationMenuBackground.visibility = View.GONE
		}
		notifyAppEvents()
		loadAppContent()
	}

	private fun loadAppContent() {
	}

	private fun notifyAppEvents() {
		binding.navigationMenu.setupDefaultNavigationMenu(NavigationItems.BROWSE_MENU)
		select(NavigationItems.BROWSE_MENU)
		setupFocusOnNavigationMenu()
		setupBlurView()
	}

	private fun setupFocusOnNavigationMenu() {
		binding.navigationMenu.setOnFocusChangeListener { view, hasFocus ->
			if (hasFocus) {
				binding.navigationMenu.onFocusChangeListener = null
				isNavigationMenuVisible = true
				showNavigationMenu(view)
			} else {
				isNavigationMenuVisible = false
				hideNavigationMenu(view)
			}
		}
	}

	private fun showNavigationMenu(view: View) {
		binding.navigationMenu.setupNavigationMenuExpandedUI(this)
		binding.navigationMenuContainer.setBlurEnabled(false)
		binding.navigationMenuBackground.visibility = View.VISIBLE
		binding.navigationMenu.animateView(
			view, ValueAnimator.ofInt(
				binding.navigationMenu.measuredWidth,
				resources.getDimensionPixelSize(R.dimen.expand_width)
			)
		)
	}

	private fun hideNavigationMenu(view: View) {
		binding.navigationMenu.setupNavigationMenuCollapsedUI()
		binding.navigationMenuContainer.setBlurEnabled(true)
		binding.navigationMenuBackground.visibility = View.GONE
		binding.navigationMenu.animateView(
			view, ValueAnimator.ofInt(
				binding.navigationMenu.measuredWidth,
				resources.getDimensionPixelSize(R.dimen.collapsed_width)
			)
		)
	}

	private fun setupBlurView() {
		binding.errorContainer.setupWith(binding.container).setBlurRadius(12.5f)
		binding.navigationMenuContainer.setupWith(binding.container).setBlurRadius(12.5f)
	}

	override fun select(selectedItem: Int) {

		lateinit var tag: String
		lateinit var fragment: Class<out Fragment>
		lateinit var arguments: Bundle
		var shouldReplace = false
		var shouldAddToBackStack = false
		when (binding.navigationMenu.getSelectedItem(selectedItem)) {
			NavigationItems.PROFILE -> {
				tag = NavigationItems.PROFILE
				arguments = bundleOf(AppConstants.TAG to tag)
				fragment = ProfileScreen::class.java
				shouldReplace = true
				shouldAddToBackStack = true
				binding.navigationMenu.setCurrentSelected(selectedItem)
			}

			NavigationItems.BROWSE -> {
				tag = NavigationItems.BROWSE
				arguments = bundleOf(AppConstants.TAG to tag)
				fragment = BrowseScreen::class.java
				shouldReplace = true
				shouldAddToBackStack = true
				binding.navigationMenu.setCurrentSelected(selectedItem)
			}

			NavigationItems.SEARCH -> {
				tag = NavigationItems.SEARCH
				arguments = bundleOf(AppConstants.TAG to tag)
				fragment = SearchScreen::class.java
				shouldReplace = true
				shouldAddToBackStack = true
				binding.navigationMenu.setCurrentSelected(selectedItem)
			}

			NavigationItems.MY_SHOWS -> {
				tag = NavigationItems.MY_SHOWS
				arguments = bundleOf(AppConstants.TAG to tag)
				fragment = ShowsScreen::class.java
				shouldReplace = true
				shouldAddToBackStack = true
				binding.navigationMenu.setCurrentSelected(selectedItem)
			}
		}
		isNavigationMenuVisible = false
		hideNavigationMenu(binding.navigationMenu)
		goToPage(supportFragmentManager, shouldReplace, fragment, arguments, tag, shouldAddToBackStack)
		setupFocusOnNavigationMenu()
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
		val currentTime = System.currentTimeMillis()
		return if (currentTime - AppConstants.lastKeyPressTime < AppConstants.keyPressShortDelayTime) {
			true
		} else {
			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && isNavigationMenuVisible && binding.errorContainer.visibility == View.GONE) {
				AppConstants.lastKeyPressTime = currentTime
				return clearNavigationMenuUI()
			} else {
				AppConstants.lastKeyPressTime = currentTime
				return keyCode == KeyEvent.KEYCODE_DPAD_LEFT && isNavigationMenuVisible && binding.errorContainer.visibility == View.GONE
			}
		}
	}

	private fun clearNavigationMenuUI(): Boolean {
		isNavigationMenuVisible = false
		hideNavigationMenu(binding.navigationMenu)
		setupFocusOnNavigationMenu()
		return true
	}

	override fun onDestroy() {
		binding.navigationMenu.onFocusChangeListener = null
		super.onDestroy()
	}

	override fun selectNavigationMenu(selectedItem: Int) {
		binding.navigationMenu.setCurrentSelected(selectedItem)
	}

	override fun showErrorOnScreen(tag: String, message: String) {
		showError(tag, message)
	}
}