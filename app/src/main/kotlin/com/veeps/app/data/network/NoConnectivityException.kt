package com.veeps.app.data.network

import com.veeps.app.R
import com.veeps.app.application.Veeps
import java.io.IOException

class NoConnectivityException : IOException() {
	override val message: String
		get() = Veeps.appContext.getString(R.string.no_connectivity_error)
}