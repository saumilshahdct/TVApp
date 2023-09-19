package com.veeps.app.feature.browse.ui

import android.os.Bundle
import android.view.View
import com.veeps.app.core.BaseFragment
import com.veeps.app.databinding.FragmentBrowseScreenBinding
import com.veeps.app.feature.browse.viewModel.BrowseViewModel
import com.veeps.app.util.Logger
import com.veeps.app.widget.navigationMenu.NavigationItems

class BrowseScreen : BaseFragment<BrowseViewModel, FragmentBrowseScreenBinding>() {
	override fun getViewBinding(): FragmentBrowseScreenBinding =
		FragmentBrowseScreenBinding.inflate(layoutInflater)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.apply {
			browse = viewModel
			browseScreen = this@BrowseScreen
			lifecycleOwner = this@BrowseScreen
			lifecycle.addObserver(viewModel)
			loader.visibility = View.GONE
			label.requestFocus()
		}
		notifyAppEvents()
		loadAppContent()
	}

	private fun loadAppContent() {

	}

	private fun notifyAppEvents() {
		viewModel.isVisible.observe(viewLifecycleOwner) { isVisible ->
			if (isVisible) helper.selectNavigationMenu(NavigationItems.BROWSE_MENU)
			Logger.print(
				"Visibility Changed to $isVisible On ${
					this@BrowseScreen.javaClass.name.substringAfterLast(
						"."
					)
				}"
			)
		}
	}
}