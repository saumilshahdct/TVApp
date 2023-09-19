package com.veeps.app.extension

import androidx.fragment.app.Fragment

fun Fragment.showToast(message: CharSequence) =
	activity?.showToast(message)