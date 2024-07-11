import io.sentry.android.gradle.extensions.InstrumentationFeature
import io.sentry.android.gradle.instrumentation.logcat.LogcatLevel
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties

val mainVersionCode = 4
val mainVersionName = ".4.0"
val releaseLabel = "release"
val debugLabel = "debug"

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("kotlin-kapt")
	id("io.sentry.android.gradle") version "4.1.1"
	id("com.google.devtools.ksp")
	id("com.google.gms.google-services")
}

android {
	val keyStore = Properties()
	keyStore.load(FileInputStream(file("$rootDir/keystore/keystore.config")))

	signingConfigs {
		getByName(debugLabel) {
			storeFile = file("$rootDir/keystore/veeps")
			storePassword = keyStore["password"].toString()
			keyPassword = keyStore["password"].toString()
			keyAlias = keyStore["alias"].toString()
		}
		create(releaseLabel) {
			storeFile = file("$rootDir/keystore/veeps")
			storePassword = keyStore["password"].toString()
			keyPassword = keyStore["password"].toString()
			keyAlias = keyStore["alias"].toString()
		}
	}
	namespace = "com.veeps.app"
	compileSdk = 34

	defaultConfig {
		applicationId = "com.veeps.app"
		minSdk = 24
		targetSdk = 34
		versionCode = mainVersionCode
		versionName = "1"
		signingConfig = signingConfigs.getByName(releaseLabel)
		testFunctionalTest = true
		testHandleProfiling = true
	}

	buildTypes {
		getByName(releaseLabel) {
			isMinifyEnabled = false
			versionNameSuffix = mainVersionName
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
			)
			signingConfig = signingConfigs.getByName(releaseLabel)
		}
		getByName(debugLabel) {
			/*applicationIdSuffix = ".$debugLabel"*/
			versionNameSuffix = ".$mainVersionName.$debugLabel" + LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern(".MMdd"))
			signingConfig = signingConfigs.getByName(debugLabel)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
	dependenciesInfo {
		includeInApk = false
		includeInBundle = false
	}
	buildFeatures {
		viewBinding = true
		dataBinding = true
		buildConfig = true
	}
	flavorDimensions += listOf("environment")
	productFlavors {
		create("stage") {
			dimension = "environment"
			versionCode = mainVersionCode
			signingConfig = signingConfigs.getByName(debugLabel)
			buildConfigField("Boolean", "isProduction", "false")
		}
		create("production") {
			dimension = "environment"
			versionCode = mainVersionCode
			signingConfig = signingConfigs.getByName(releaseLabel)
			buildConfigField("Boolean", "isProduction", "true")
		}
	}
}

dependencies {

	/* Amazon IAP */
	implementation(libs.amazon.appstore)

	/* Android Core */
	implementation(libs.core)
	implementation(libs.leanback)
	implementation(libs.leanback.grid)
	implementation(libs.palette)
	implementation(libs.kotlinx.coroutines.android)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.activity)
	implementation(libs.material)
	implementation(libs.fragment)
	implementation(libs.constraintlayout)
	implementation(libs.lifecycle.viewmodel)
	implementation(libs.lifecycle.livedata)
	implementation(libs.lifecycle.common.java8)
	implementation(libs.androidx.work.runtime)
	implementation(libs.dpadrecyclerview)
	implementation(libs.androidx.ui.unit.android)

	/* Splash Screen implementation(libs.androidx.core.splashscreen) */

	/* Calligraphy for Fonts */
	implementation(libs.calligraphy3)
	implementation(libs.viewpump) /* Keep library version "2.0.3" due to initialization issue on latest version "2.1.1" */

	/* Retrofit */
	implementation(libs.gson)
	implementation(libs.retrofit)
	implementation(libs.converter.scalars)
	implementation(libs.converter.gson)
	implementation(libs.okhttp)
	implementation(libs.logging.interceptor)

	/* Joda Time */
	implementation(libs.android.joda)

	/* ExoPlayer */
	implementation(libs.androidx.media3.exoplayer)
	implementation(libs.androidx.media3.exoplayer.dash)
	implementation(libs.androidx.media3.exoplayer.hls)
	implementation(libs.androidx.media3.datasource.okhttp)
	implementation(libs.androidx.media3.ui.leanback)
	implementation(libs.androidx.media3.ui)
	implementation(libs.androidx.media3.extractor)
	implementation(libs.androidx.media3.transformer)

	/* Blur */
	implementation(libs.blurView)

	/* PubNub */
	implementation(libs.pubnub)

	/* Lottie Animation */
	implementation(libs.lottie)

	/* Markdown Parser */
	implementation(libs.markwon.core)

	implementation(platform(libs.firebase.bom))
	implementation(libs.firebase.analytics)

	/* Glide */
	implementation(libs.glide)
	ksp(libs.glide.ksp)
	implementation(libs.glide.recyclerview.integration)

	/* JWT */
	implementation(libs.jjwt.api)
	runtimeOnly(libs.jjwt.impl)
	runtimeOnly(libs.jjwt.orgjson)

}

sentry {
	org.set("veeps")
	projectName.set("firetv")
	includeProguardMapping.set(true)
	autoUploadProguardMapping.set(true)
	authToken.set("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")
	includeSourceContext.set(true)
	tracingInstrumentation {
		enabled.set(true)
		features.set(
			setOf(
				InstrumentationFeature.DATABASE,
				InstrumentationFeature.FILE_IO,
				InstrumentationFeature.OKHTTP,
				InstrumentationFeature.COMPOSE
			)
		)
		logcat {
			enabled.set(true)
			minLevel.set(LogcatLevel.VERBOSE)
		}
	}
}
