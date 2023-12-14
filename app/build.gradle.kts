import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("kotlin-kapt")
	id("io.sentry.android.gradle") version "3.11.1"
	id("com.google.devtools.ksp")
}

android {
	signingConfigs {
		getByName("debug") {
			storeFile = file("/Users/saumilshah/Projects/dcafe/Veeps/keystore/veeps")
			storePassword = "b8d01c2c1db72d2ab195c06694daa8a7"
			keyPassword = "b8d01c2c1db72d2ab195c06694daa8a7"
			keyAlias = "Veeps"
		}
		create("release") {
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
		versionCode = 1
		versionName = "1.0"
		signingConfig = signingConfigs.getByName("release")
		testFunctionalTest = true
		testHandleProfiling = true
	}

	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
			versionNameSuffix = ".0"
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
			)
			signingConfig = signingConfigs.getByName("release")
		}
		getByName("debug") {
			applicationIdSuffix = ".debug"
			versionNameSuffix =
				".debug" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(".MMdd"))
			signingConfig = signingConfigs.getByName("debug")
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
			versionCode = 1
			signingConfig = signingConfigs.getByName("debug")
			buildConfigField("Boolean", "isProduction", "false")
		}
		create("production") {
			dimension = "environment"
			versionCode = 1
			signingConfig = signingConfigs.getByName("release")
			buildConfigField("Boolean", "isProduction", "true")
		}
	}
}

dependencies {

	/* Amazon IAP */
	implementation("com.amazon.device:amazon-appstore-sdk:3.0.4")

	/* Android Core */
	implementation("androidx.core:core-ktx:1.12.0")
	implementation("androidx.leanback:leanback:1.0.0")
	implementation("androidx.leanback:leanback-grid:1.0.0-alpha03")
	implementation("androidx.palette:palette-ktx:1.0.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
	implementation("androidx.activity:activity-ktx:1.8.1")
	implementation("com.google.android.material:material:1.10.0")
	implementation("androidx.fragment:fragment-ktx:1.6.2")
	implementation("androidx.constraintlayout:constraintlayout:2.1.4")
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
	implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
	implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2")
	implementation("androidx.work:work-runtime-ktx:2.9.0")

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
	implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.7")
	implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.7")

	/* Glide */
	implementation("com.github.bumptech.glide:glide:4.16.0")
	ksp("com.github.bumptech.glide:ksp:4.16.0")
	implementation("com.github.bumptech.glide:recyclerview-integration:4.14.2") {
		// Excludes the support library because it's already included by Glide.
		isTransitive = false
	}

	/* JWT */
//	implementation("com.auth0:java-jwt:4.4.0")
	implementation("io.jsonwebtoken:jjwt-api:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-orgjson:0.12.3") {
		exclude(group = "org.json", module = "json") // Excludes the support library because it's already included by Android natively.
	}

	/* Joda Time */
	implementation("net.danlew:android.joda:2.12.5")

	/* ExoPlayer */
	implementation("androidx.media3:media3-exoplayer:1.2.0")
	implementation("androidx.media3:media3-exoplayer-dash:1.2.0")
	implementation("androidx.media3:media3-exoplayer-hls:1.2.0")
	implementation("androidx.media3:media3-datasource-okhttp:1.2.0")
	implementation("androidx.media3:media3-ui-leanback:1.2.0")
	implementation("androidx.media3:media3-ui:1.2.0")
	implementation("androidx.media3:media3-extractor:1.2.0")
	implementation("androidx.media3:media3-transformer:1.2.0")

	/* Blur */
	implementation("com.github.Dimezis:BlurView:version-2.0.3")

	/* PubNub */
	implementation("com.pubnub:pubnub-kotlin:7.7.4")

	/* Lottie Animation */
	implementation("com.airbnb.android:lottie:6.1.0")

	/* Markdown Parser */
	implementation ("io.noties.markwon:core:4.6.2")
}