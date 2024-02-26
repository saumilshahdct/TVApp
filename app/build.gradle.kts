import io.sentry.android.gradle.extensions.InstrumentationFeature
import io.sentry.android.gradle.instrumentation.logcat.LogcatLevel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val mainVersionCode = 4
val mainVersionName = ".3"
val releaseLabel = "release"
val debugLabel = "debug"

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("kotlin-kapt")
	id("io.sentry.android.gradle") version "4.1.1"
	id("com.google.devtools.ksp")
}

android {
	signingConfigs {
		getByName(debugLabel) {
			storeFile = file("/Users/saumilshah/Projects/dcafe/Veeps/keystore/veeps")
			storePassword = "b8d01c2c1db72d2ab195c06694daa8a7"
			keyPassword = "b8d01c2c1db72d2ab195c06694daa8a7"
			keyAlias = "Veeps"
		}
		create(releaseLabel) {
			storeFile = file("/Users/saumilshah/Projects/dcafe/Veeps/keystore/veeps")
			storePassword = "b8d01c2c1db72d2ab195c06694daa8a7"
			keyPassword = "b8d01c2c1db72d2ab195c06694daa8a7"
			keyAlias = "Veeps"
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
			applicationIdSuffix = ".$debugLabel"
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
	implementation("com.amazon.device:amazon-appstore-sdk:3.0.4")

	/* Android Core */
	implementation("androidx.core:core-ktx:1.12.0")
	implementation("androidx.leanback:leanback:1.2.0-alpha04")
	implementation("androidx.leanback:leanback-grid:1.0.0-alpha03")
	implementation("androidx.palette:palette-ktx:1.0.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
	implementation("androidx.activity:activity-ktx:1.8.2")
	implementation("com.google.android.material:material:1.11.0")
	implementation("androidx.fragment:fragment-ktx:1.6.2")
	implementation("androidx.constraintlayout:constraintlayout:2.1.4")
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
	implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
	implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0")
	implementation("androidx.work:work-runtime-ktx:2.9.0")
	implementation("com.rubensousa.dpadrecyclerview:dpadrecyclerview:1.2.0-alpha03")

	/* Splash Screen */
//	implementation("androidx.core:core-splashscreen:1.0.1")

	/* Calligraphy for Fonts */
	implementation("io.github.inflationx:calligraphy3:3.1.1")
	//noinspection GradleDependency
	implementation("io.github.inflationx:viewpump:2.0.3") /* Keep library version "2.0.3" due to initialization issue on latest version "2.1.1" */

	/* Retrofit */
	implementation("com.google.code.gson:gson:2.10.1")
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
	implementation("com.squareup.retrofit2:converter-gson:2.9.0")
	implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.12")
	implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.12")

	/* Glide */
	implementation("com.github.bumptech.glide:glide:4.16.0")
	implementation("androidx.compose.ui:ui-unit-android:1.6.2")
	ksp("com.github.bumptech.glide:ksp:4.16.0")
	implementation("com.github.bumptech.glide:recyclerview-integration:4.16.0") {
		// Excludes the support library because it's already included by Glide.
		isTransitive = false
	}

	/* JWT */
//	implementation("com.auth0:java-jwt:4.4.0")
	implementation("io.jsonwebtoken:jjwt-api:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-orgjson:0.12.3") {
		exclude(
			group = "org.json", module = "json"
		) // Excludes the support library because it's already included by Android natively.
	}

	/* Joda Time */
	implementation("net.danlew:android.joda:2.12.7")

	val exoVersion = "1.2.1"    /* ExoPlayer */
	implementation("androidx.media3:media3-exoplayer:$exoVersion")
	implementation("androidx.media3:media3-exoplayer-dash:$exoVersion")
	implementation("androidx.media3:media3-exoplayer-hls:$exoVersion")
	implementation("androidx.media3:media3-datasource-okhttp:$exoVersion")
	implementation("androidx.media3:media3-ui-leanback:$exoVersion")
	implementation("androidx.media3:media3-ui:$exoVersion")
	implementation("androidx.media3:media3-extractor:$exoVersion")
	implementation("androidx.media3:media3-transformer:$exoVersion")

	/* Blur */
	implementation("com.github.Dimezis:BlurView:version-2.0.3")

	/* PubNub */
	implementation("com.pubnub:pubnub-kotlin:7.8.0")

	/* Lottie Animation */
	implementation("com.airbnb.android:lottie:6.3.0")

	/* Markdown Parser */
	implementation("io.noties.markwon:core:4.6.2")
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
