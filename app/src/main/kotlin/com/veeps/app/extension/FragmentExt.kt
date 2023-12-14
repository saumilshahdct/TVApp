package com.veeps.app.extension

import androidx.fragment.app.Fragment

fun Fragment.showToast(message: Any) = activity?.showToast(message.toString())