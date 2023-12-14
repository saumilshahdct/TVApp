package com.veeps.app.util

import com.veeps.app.feature.contentRail.model.Entities

interface AppAction {
	fun onAction(entity: Entities) = run {
		Logger.print(
			"Action performed but not implemented"
		)
	}

	fun onAction() = run {
		Logger.print(
			"Action performed but not implemented"
		)
	}
}