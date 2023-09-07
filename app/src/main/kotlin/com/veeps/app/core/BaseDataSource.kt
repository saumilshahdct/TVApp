package com.veeps.app.core

import android.text.Html
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.veeps.app.R
import com.veeps.app.application.Veeps
import com.veeps.app.data.common.BaseResponseGeneric
import com.veeps.app.data.network.NoConnectivityException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

abstract class BaseDataSource {

	protected suspend fun <T> getResult(
		tag: String, call: suspend () -> Response<BaseResponseGeneric<T>>
	): Resource<BaseResponseGeneric<T>> {
		try {
			val response = call()
			when (response.isSuccessful) {
				true -> {
					when (response.code()) {
						200 -> {
							response.body()?.let {
								when (tag) {
									else -> {
										return if (response.body()!!.success) {
											Resource.success(tag, response.body()!!)
										} else {
											if (response.body()!!.message.isEmpty()) {
												Resource.error(
													tag,
													Veeps.appContext.getString(R.string.unknown_error)
												)
											} else {
												Resource.error(tag, response.body()!!.message)
											}
										}
									}
								}
							} ?: return Resource.error(
								tag, Veeps.appContext.getString(R.string.unknown_error)
							)
						}

						else -> {
							response.body()?.let {
								when (tag) {
									else -> {
										return if (response.body()!!.success) {
											Resource.success(tag, response.body()!!)
										} else {
											if (response.body()!!.message.isEmpty()) {
												Resource.error(
													tag,
													Veeps.appContext.getString(R.string.unknown_error)
												)
											} else {
												Resource.error(tag, response.body()!!.message)
											}
										}
									}
								}
							} ?: return Resource.error(
								tag, Veeps.appContext.getString(R.string.unknown_error)
							)
						}
					}
				}

				else -> {
					when (response.code()) {
						400 -> {
							var error: String = Veeps.appContext.getString(R.string.unknown_error)
							response.errorBody()?.let { errorBody ->
								val errorObject = JSONObject(errorBody.string())
								if (errorObject.has("message")) {
									error = Html.fromHtml(
										errorObject.getString("message"),
										HtmlCompat.FROM_HTML_MODE_LEGACY
									).toString()
								}
							}
							return Resource.error(tag, error)
						}

						else -> {
							var error: String = Veeps.appContext.getString(R.string.unknown_error)
							response.errorBody()?.let { errorBody ->
								val errorObject = JSONObject(errorBody.string())
								if (errorObject.has("message")) {
									error = Html.fromHtml(
										errorObject.getString("message"),
										HtmlCompat.FROM_HTML_MODE_LEGACY
									).toString()
								}
							}
							return Resource.error(tag, error)
						}
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
			return when (e) {
				is NoConnectivityException -> {
					Resource.error(tag, Veeps.appContext.getString(R.string.no_connectivity_error))
				}

				is IOException -> {
					Resource.error(tag, Veeps.appContext.getString(R.string.no_connectivity_error))
				}

				else -> {
					Resource.error(tag, e.message ?: e.toString())
				}
			}
		}
	}

	fun <T> performFlowOperation(
		tag: String, networkCall: suspend () -> Resource<T>
	): Flow<Resource<T?>> = flow {
		try {
			emit(Resource.loading(tag, response = null))
			val responseStatus = networkCall.invoke()
			emit(responseStatus)
		} catch (e: Exception) {
			emit(Resource.error(tag, e.message ?: e.toString()))
		}
	}.flowOn(Dispatchers.Main)

	fun <T> performOperation(
		tag: String, networkCall: suspend () -> Resource<T>
	): LiveData<Resource<T?>> = liveData(Dispatchers.IO) {
		try {
			emit(Resource.loading(tag, response = null))
			val responseStatus = networkCall.invoke()
			emit(responseStatus)
		} catch (e: Exception) {
			emit(Resource.error<T>(tag, e.message ?: e.toString()))
		}
	}

	data class Resource<out T>(
		val callStatus: CallStatus, val response: T?, val message: String?, val tag: String
	) {
		enum class CallStatus {
			SUCCESS, ERROR, LOADING
		}

		companion object {
			fun <T> success(tag: String, response: T): Resource<T> {
				return Resource(CallStatus.SUCCESS, response, null, tag)
			}

			fun <T> error(tag: String, message: String?): Resource<T> {
				return Resource(CallStatus.ERROR, null, message, tag)
			}

			fun <T> loading(tag: String, response: T? = null): Resource<T> {
				return Resource(CallStatus.LOADING, response, null, tag)
			}
		}
	}
}
