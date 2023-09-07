package com.veeps.app.data.common

open class BaseResponseGeneric<T> {
	var message: String = ""
	var success: Boolean = false
	var data: T? = null
}