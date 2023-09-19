package com.veeps.app.feature.shows.ui

import android.os.Bundle
import android.view.View
import com.veeps.app.core.BaseFragment
import com.veeps.app.databinding.FragmentShowsScreenBinding
import com.veeps.app.feature.shows.viewModel.ShowsViewModel
import com.veeps.app.util.Logger
import com.veeps.app.widget.navigationMenu.NavigationItems

class ShowsScreen : BaseFragment<ShowsViewModel, FragmentShowsScreenBinding>() {
	override fun getViewBinding(): FragmentShowsScreenBinding =
		FragmentShowsScreenBinding.inflate(layoutInflater)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.apply {
			shows = viewModel
			showsScreen = this@ShowsScreen
			lifecycleOwner = this@ShowsScreen
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
			if (isVisible) helper.selectNavigationMenu(NavigationItems.MY_SHOWS_MENU)
			Logger.print(
				"Visibility Changed to $isVisible On ${
					this@ShowsScreen.javaClass.name.substringAfterLast(
						"."
					)
				}"
			)
		}
	}
}