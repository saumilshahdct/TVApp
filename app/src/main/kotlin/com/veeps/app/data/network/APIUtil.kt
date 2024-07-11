package com.veeps.app.data.network

import com.google.gson.GsonBuilder
import com.veeps.app.BuildConfig
import com.veeps.app.data.common.BaseResponseGeneric
import com.veeps.app.extension.isAppConnected
import com.veeps.app.feature.artist.model.ArtistResponse
import com.veeps.app.feature.contentRail.model.Entities
import com.veeps.app.feature.contentRail.model.ProductsResponse
import com.veeps.app.feature.contentRail.model.RailData
import com.veeps.app.feature.contentRail.model.RailResponse
import com.veeps.app.feature.contentRail.model.UserStatsResponse
import com.veeps.app.feature.event.model.OrderGeneration
import com.veeps.app.feature.event.model.Reservation
import com.veeps.app.feature.search.model.SearchResponse
import com.veeps.app.feature.signIn.model.PollingData
import com.veeps.app.feature.signIn.model.SignInData
import com.veeps.app.feature.user.model.UserData
import com.veeps.app.feature.video.model.Companion
import com.veeps.app.feature.video.model.StoryBoardImages
import com.veeps.app.util.APIConstants
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppPreferences
import com.veeps.app.util.Logger
import io.sentry.HttpStatusCodeRange
import io.sentry.okhttp.SentryOkHttpEventListener
import io.sentry.okhttp.SentryOkHttpInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

object APIUtil {

	private var startNs: Long = System.nanoTime()

	val service: APIService by lazy {
		val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
		logging.level =
			if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

		val httpClient = OkHttpClient.Builder().addInterceptor(
			SentryOkHttpInterceptor(
				captureFailedRequests = true, failedRequestStatusCodes = listOf(
					HttpStatusCodeRange(400, 599)
				)
			)
		).eventListener(SentryOkHttpEventListener()).addInterceptor { chain ->
			if (isAppConnected()) {
				startNs = System.nanoTime()
				var request = chain.request()
				val originalHttpUrl = request.url
				val headerToken =
					if (originalHttpUrl.encodedPath == APIConstants.fetchUserStats || originalHttpUrl.encodedPath == APIConstants.addStats) AppPreferences.get(
						AppConstants.generatedJWT,
						"GeneratedJWT"
					) else AppPreferences.get(
						AppConstants.authenticatedUserToken,
						"AuthenticatedUserToken"
					)
				val builder: Request.Builder =
					request.newBuilder().header("Authorization", "Bearer $headerToken")
				request = builder.build()
				if (originalHttpUrl.encodedPath == APIConstants.fetchUserStats || originalHttpUrl.encodedPath == APIConstants.addStats) AppPreferences.remove(
					AppConstants.generatedJWT
				)
				chain.proceed(request)
			} else {
				throw NoConnectivityException()
			}
		}.addInterceptor { chain ->
			val response = chain.proceed(chain.request())
			var bodyString: String
			response.body.let {
				bodyString = response.body.string()
				val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
				val tookS = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startNs)
				Logger.printAPILogs(chain, response, bodyString, tookMs, tookS)
			}
			val newResponse: okhttp3.Response.Builder = response.newBuilder()
			newResponse.body(bodyString.toResponseBody(response.body.contentType()))
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
			@Field("client_secret") clientSecret: String,
		): Response<PollingData>

		@GET(APIConstants.fetchUserDetails)
		suspend fun fetchUserDetails(): Response<BaseResponseGeneric<UserData>>

		@GET(APIConstants.fetchBrowseRails)
		suspend fun fetchBrowseRails(): Response<RailResponse>

		@GET(APIConstants.fetchOnDemandRails)
		suspend fun fetchOnDemandRails(): Response<RailResponse>

		@GET(APIConstants.fetchFeaturedContent)
		suspend fun fetchFeaturedContent(): Response<RailResponse>

		@GET(APIConstants.fetchContinueWatchingRail)
		suspend fun fetchContinueWatchingRail(): Response<RailData>

		@GET
		suspend fun fetchUserStats(
			@Url userStatsAPIURL: String, @Query("e") eventIds: String
		): Response<UserStatsResponse>

		@GET(APIConstants.fetchUpcomingEvents)
		suspend fun fetchUpcomingEvents(): Response<RailData>

		@GET(APIConstants.fetchSearchResult)
		suspend fun fetchSearchResult(@Query("q") search: String): Response<SearchResponse>

		@GET(APIConstants.fetchEntityDetails)
		suspend fun fetchEntityDetails(
			@Path("ENTITY") entity: String,
			@Path("ENTITY_ID") entityId: String,
		): Response<ArtistResponse>

		@GET(APIConstants.fetchEntityUpcomingEvents)
		suspend fun fetchEntityUpcomingEvents(@Query("scope") entityScope: String): Response<RailData>

		@GET(APIConstants.fetchEntityOnDemandEvents)
		suspend fun fetchEntityOnDemandEvents(@Query("scope") entityScope: String): Response<RailData>

		@GET(APIConstants.fetchEntityPastEvents)
		suspend fun fetchEntityPastEvents(@Query("scope") entityScope: String): Response<RailData>

		@GET(APIConstants.fetchAllPurchasedEvents)
		suspend fun fetchAllPurchasedEvents(): Response<RailData>

		@GET(APIConstants.watchListEvents)
		suspend fun fetchAllWatchListEvents(): Response<RailData>

		@POST(APIConstants.watchListEvents)
		suspend fun addWatchListEvent(@Body hashMap: HashMap<String, Any>): Response<BaseResponseGeneric<Any>>

		@HTTP(method = "DELETE", path = APIConstants.removeWatchListEvents, hasBody = true)
		suspend fun removeWatchListEvent(@Body hashMap: HashMap<String, Any>): Response<BaseResponseGeneric<Any>>

		@GET(APIConstants.fetchEventStreamDetails)
		suspend fun fetchEventStreamDetails(@Path("EVENT_ID") entity: String): Response<BaseResponseGeneric<Entities>>

		@GET(APIConstants.fetchEventDetails)
		suspend fun fetchEventDetails(@Path("EVENT_ID") entity: String): Response<BaseResponseGeneric<Entities>>

		@GET(APIConstants.fetchEventProductDetails)
		suspend fun fetchEventProductDetails(@Path("EVENT_ID") entity: String): Response<ProductsResponse>

		@POST(APIConstants.claimFreeTicketForEvent)
		suspend fun claimFreeTicketForEvent(@Path("EVENT_ID") entity: String): Response<BaseResponseGeneric<Any>>

		@DELETE(APIConstants.clearAllReservations)
		suspend fun clearAllReservations(): Response<BaseResponseGeneric<Any>>

		@POST(APIConstants.setNewReservation)
		suspend fun setNewReservation(@Body hashMap: HashMap<String, Any>): Response<BaseResponseGeneric<Reservation>>

		@GET(APIConstants.generateNewOrder)
		suspend fun generateNewOrder(): Response<BaseResponseGeneric<OrderGeneration>>

		@POST(APIConstants.createOrder)
		suspend fun createOrder(@Body hashMap: HashMap<String, Any>): Response<BaseResponseGeneric<Any>>

		@POST(APIConstants.fetchCompanions)
		suspend fun fetchCompanions(@Body hashMap: HashMap<String, Any>): Response<BaseResponseGeneric<Companion>>

		@GET
		suspend fun fetchStoryBoard(@Url storyBoardURL: String): Response<StoryBoardImages>

		@GET
		suspend fun addStats(
			@Url addStatsAPIURL: String,
			@Query("cur") currentTime: String,
			@Query("pld") duration: String,
			@Query("plv") playerVersion: String,
			@Query("dvm") deviceModel: String,
			@Query("dvv") deviceVendor: String,
			@Query("pls") playbackStreamType: String,
			@Query("p") platform: String,
			@Query("s") userType: String
		): Response<BaseResponseGeneric<Any>>

		@GET(APIConstants.fetchRecommendedEvent)
		suspend fun fetchRecommendedEvent(@Query("scope") scope: String): Response<RailResponse>
	}
}