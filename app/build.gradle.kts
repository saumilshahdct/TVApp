import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("kotlin-kapt")
	id("io.sentry.android.gradle") version "3.11.1"
//    id("com.google.devtools.ksp")
}

android {
	signingConfigs {
		getByName("debug") {
			storeFile = file("/Users/saumilshah/Projects/Veeps/keystore/veeps")
			storePassword = "b8d01c2c1db72d2ab195c06694daa8a7"
			keyPassword = "b8d01c2c1db72d2ab195c06694daa8a7"
			keyAlias = "Veeps"
		}
		create("release") {
			storeFile = file("/Users/saumilshah/Projects/Veeps/keystore/veeps")
			storePassword = "b8d01c2c1db72d2ab195c06694daa8a7"
			keyPassword = "b8d01c2c1db72d2ab195c06694daa8a7"
			keyAlias = "Veeps"
		}
	}
	namespace = "com.veeps.app"
	compileSdk = 33

	defaultConfig {
		applicationId = "com.veeps.app"
		minSdk = 24
		targetSdk = 33
		versionCode = 1
		versionName = "1.0.0"
		signingConfig = signingConfigs.getByName("release")
		testFunctionalTest = true
		testHandleProfiling = true
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
			)
			signingConfig = signingConfigs.getByName("release")
		}
		getByName("debug") {
			applicationIdSuffix = ".debug"
			versionNameSuffix =
				".debug" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(".HHmm.yyMMdd"))
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
	}
}

dependencies {

	/* DEFAULT */
	implementation("androidx.core:core-ktx:1.10.1")
	implementation("androidx.leanback:leanback:1.0.0")
	implementation("androidx.palette:palette-ktx:1.0.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
	implementation("com.google.android.material:material:1.9.0")
	implementation("androidx.fragment:fragment-ktx:1.6.1")
	implementation("androidx.constraintlayout:constraintlayout:2.1.4")
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
	implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
	implementation("androidx.lifecycle:lifecycle-common-java8:2.6.1")

	/* Splash Screen */
	implementation("androidx.core:core-splashscreen:1.0.1")

	/* Calligraphy for Fonts */
	implementation("io.github.inflationx:calligraphy3:3.1.1")
	implementation("io.github.inflationx:viewpump:2.0.3")//keep to lower version "2.0.3" due to init method issue on latest version "2.1.1"

	/* Retrofit */
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
	implementation("com.squareup.retrofit2:converter-gson:2.9.0")
	implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.7")
	implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.7")

	/* Glide */
	implementation("com.github.bumptech.glide:glide:4.16.0")
	//noinspection KaptUsageInsteadOfKsp
	kapt("com.github.bumptech.glide:compiler:4.13.2")

	/* ExoPlayer */
	implementation("androidx.media3:media3-exoplayer:1.1.1")
	implementation("androidx.media3:media3-exoplayer-dash:1.1.1")
	implementation("androidx.media3:media3-ui:1.1.1")

	/* Blur */
	implementation("com.github.Dimezis:BlurView:version-2.0.3")

	/* PubNub */
	implementation("com.pubnub:pubnub-kotlin:7.5.0")
}