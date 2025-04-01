plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.echoenglish_mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.echoenglish_mobile"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    viewBinding {
        enable = true
    }
    dataBinding {
        enable = true
    }
}

dependencies {
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ViewModel: Quản lý dữ liệu và vòng đời UI, giúp lưu trạng thái khi xoay màn hình
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")

    // LiveData: Giúp UI tự động cập nhật khi dữ liệu thay đổi, tránh memory leaks
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")

    // Data Binding: Cho phép liên kết dữ liệu trực tiếp từ ViewModel vào XML
    implementation("androidx.databinding:databinding-runtime:8.1.0")

    // Retrofit: Thư viện giúp gọi API dễ dàng, hỗ trợ RESTful API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Converter Gson: Chuyển đổi JSON thành Object Java và ngược lại
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp Logging Interceptor: Ghi log request & response khi gọi API, giúp debug dễ dàng
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.0")

    // Glide: Thư viện load ảnh tối ưu, hỗ trợ caching và tải ảnh từ URL
    implementation("com.github.bumptech.glide:glide:4.14.2")

    // Glide Compiler: Dùng để sinh code cho Glide, giúp tối ưu hiệu suất load ảnh
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")
}