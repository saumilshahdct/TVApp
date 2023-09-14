package com.veeps.app.data.network

import android.util.Base64
import com.google.gson.GsonBuilder
import com.veeps.app.BuildConfig
import com.veeps.app.data.common.BaseResponseGeneric
import com.veeps.app.extension.isAppConnected
import com.veeps.app.feature.signIn.model.PollingData
import com.veeps.app.feature.signIn.model.SignInData
import com.veeps.app.feature.user.model.UserData
import com.veeps.app.util.APIConstants
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppPreferences
import com.veeps.app.util.Logger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

object APIUtil {

	private var startNs: Long = System.nanoTime()

	val service: APIService by lazy {
		val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
		logging.level =
			if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

		val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
			if (isAppConnected()) {
				startNs = System.nanoTime()
				var request = chain.request()

				val builder: Request.Builder = request.newBuilder().header(
					"Authorization", "Bearer " + AppPreferences.get(
						AppConstants.authenticatedUserToken, "AuthenticatedUserToken"
					)
				)
				request = builder.build()
				chain.proceed(request)
			} else {
				throw NoConnectivityException()
			}
		}.addInterceptor { chain ->
			val response = chain.proceed(chain.request())
			var isBodyEncoded: Boolean
			var bodyString: String
			response.body.let {
				bodyString = response.body.string()
				isBodyEncoded =
					bodyString.split("\\.".toRegex()).size > 2 && !bodyString.startsWith("{") && !bodyString.endsWith(
						"}"
					)
				if (isBodyEncoded) {
					val jsonObject = JSONObject(
						String(
							Base64.decode(
								bodyString.split("\\.".toRegex())[1], Base64.URL_SAFE
							), StandardCharsets.UTF_8
						)
					)
					bodyString = jsonObject.toString()
				}
			}

			val newResponse: okhttp3.Response.Builder = response.newBuilder()
			newResponse.body(bodyString.toResponseBody(if (isBodyEncoded) "application/json".toMediaTypeOrNull() else response.body.contentType()))

			val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
			val tookS = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startNs)
			Logger.printAPILogs(chain, response, bodyString, tookMs, tookS)
			newResponse.build()
		}.addInterceptor(logging).connectTimeout(1, TimeUnit.MINUTES)
			.readTimeout(1, TimeUnit.MINUTES).writeTimeout(1, TimeUnit.MINUTES).build()

		Retrofit.Builder().baseUrl(APIConstants.BASE_URL)
			.addConverterFactory(ScalarsConverterFactory.create())
			.addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
			.client(httpClient).build().create(APIService::class.java)
	}

	interface APIService {
		@POST(APIConstants.fetchAuthenticationDetails)
		suspend fun fetchAuthenticationDetails(@Query("client_id") clientId: String): Response<SignInData>

		@POST(APIConstants.authenticationPolling)
		@FormUrlEncoded
		suspend fun authenticationPolling(
			@Field("grant_type") grantType: String,
			@Field("device_code") deviceCode: String,
			@Field("client_id") clientId: String,
			@Field("client_secret") clientSecret: String
		): Response<PollingData>

		@GET(APIConstants.fetchUserDetails)
		suspend fun fetchUserDetails(): Response<BaseResponseGeneric<UserData>>

	}
}