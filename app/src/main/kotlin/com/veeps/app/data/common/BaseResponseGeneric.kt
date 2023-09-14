package com.veeps.app.data.common

open class BaseResponseGeneric<T> {
	var status: T? = null
	var data: T? = null
	var errors: T? = null
}