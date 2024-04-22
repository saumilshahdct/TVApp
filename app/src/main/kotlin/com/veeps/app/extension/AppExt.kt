package com.veeps.app.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.compose.ui.unit.Dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import com.veeps.app.R
import com.veeps.app.application.Veeps
import com.veeps.app.feature.contentRail.model.Entities
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppPreferences
import com.veeps.app.util.AppUtil
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.DateTimeCompareDifference
import com.veeps.app.util.EventTypes
import com.veeps.app.util.Logger
import kotlin.math.round
import kotlin.math.roundToInt

val Context.isFreshInstall
	get() = with(packageManager.getPackageInfo(packageName, 0)) {
		firstInstallTime == lastUpdateTime
	}

val Context.isFireTV
	get() = packageManager.hasSystemFeature("amazon.hardware.fire_tv")

fun isAppConnected(): Boolean {
	var isOnline = false
	try {
		val manager =
			Veeps.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val capabilities =
			manager.getNetworkCapabilities(manager.activeNetwork) // need ACCESS_NETWORK_STATE permission
		isOnline =
			capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
	} catch (e: java.lang.Exception) {
		e.printStackTrace()
	}
	return isOnline
}

fun Double.round(decimals: Int): Double {
	var multiplier = 1.0
	repeat(decimals) { multiplier *= 10 }
	return round(this * multiplier) / multiplier
}

fun goToPage(
	manager: FragmentManager,
	shouldReplace: Boolean,
	fragment: Class<out Fragment>,
	arguments: Bundle,
	tag: String,
	shouldAddToBackStack: Boolean,
) {
	manager.commit {
//		setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
		if (shouldReplace) {
			replace(R.id.fragment_container, fragment, arguments, tag)
		} else {
			add(R.id.fragment_container, fragment, arguments, tag)
		}
		if (shouldAddToBackStack) addToBackStack(tag)
		Logger.print(
			"Page is requested to load -- ${
				fragment.javaClass.name.substringAfterLast(".")
			}"
		)
	}
}

fun <T> ArrayList<T>.filterFor(filters: ArrayList<(T) -> Boolean>) = run {
	val filteredList = filter { listItem -> filters.any { filter -> filter(listItem) } }
	return@run if (filteredList.isEmpty()) arrayListOf<T>() else ArrayList<T>(filteredList)
}

fun Entities.isOfType(eventType: String) = when (eventType) {
	EventTypes.LIVE -> {
		((status ?: DEFAULT.EMPTY_STRING).contains(EventTypes.LIVE, true) && (watchStatus
			?: DEFAULT.EMPTY_STRING).contains("watchable", true))
	}

	EventTypes.UPCOMING -> {
		((status ?: DEFAULT.EMPTY_STRING).contains(EventTypes.LIVE, true) && !(watchStatus
			?: DEFAULT.EMPTY_STRING).contains(
			"watchable", true
		) && !(watchStatus ?: DEFAULT.EMPTY_STRING).contains("expired", true)) || ((status
			?: DEFAULT.EMPTY_STRING).contains(
			EventTypes.UPCOMING, true
		)) || ((status ?: DEFAULT.EMPTY_STRING).contains(
			EventTypes.ON_DEMAND, true
		) && (watchUntil
			?: DEFAULT.EMPTY_STRING).isNotBlank() && AppUtil.compare(watchUntil) == DateTimeCompareDifference.GREATER_THAN && !(watchStatus
			?: DEFAULT.EMPTY_STRING).contains(
			"watchable_ondemand", true
		)) || ((status ?: DEFAULT.EMPTY_STRING).contains(EventTypes.ENDED, true) && !(watchStatus
			?: DEFAULT.EMPTY_STRING).contains("expired", true))
	}

	EventTypes.ON_DEMAND -> {
		((status ?: DEFAULT.EMPTY_STRING).contains(EventTypes.ON_DEMAND, true) && (watchUntil
			?: DEFAULT.EMPTY_STRING).isBlank()) || ((status ?: DEFAULT.EMPTY_STRING).contains(
			EventTypes.ON_DEMAND, true
		) && (watchUntil
			?: DEFAULT.EMPTY_STRING).isNotBlank() && AppUtil.compare(watchUntil) == DateTimeCompareDifference.GREATER_THAN && (watchStatus
			?: DEFAULT.EMPTY_STRING).contains(
			"watchable_ondemand", true
		))
	}

	else -> {
		((status ?: DEFAULT.EMPTY_STRING).contains(EventTypes.LIVE, true) && (watchStatus
			?: DEFAULT.EMPTY_STRING).contains(
			"expired", true
		)) || ((status ?: DEFAULT.EMPTY_STRING).contains(
			EventTypes.ON_DEMAND, true
		) && (watchUntil
			?: DEFAULT.EMPTY_STRING).isNotBlank() && AppUtil.compare(watchUntil) != DateTimeCompareDifference.GREATER_THAN) || ((status
			?: DEFAULT.EMPTY_STRING).contains(
			EventTypes.ENDED, true
		) && (watchStatus ?: DEFAULT.EMPTY_STRING).contains("expired", true))
	}
}

fun View.dpToPx(dimension: Dp): Int {
	return (resources.displayMetrics.density * dimension.value).roundToInt()
}

fun Int?.isGreaterThan(other: Int?) = this != null && other != null && this > other

fun Int?.convertToMilli() = if (this != null && this.isGreaterThan(0)) this * 1000 else 0

fun getAnalytics() = Firebase.analytics

fun logAnalyticsEvent(eventName: String, params: Bundle) =
	getAnalytics().logEvent(eventName, params)

fun logScreenViewEvent(screenName: String, screenClassName: String) =
	getAnalytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
		param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
		param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClassName)
	}

fun setUserIdForAnalytics() = getAnalytics().setUserId(AppPreferences.get(AppConstants.userID, ""))
