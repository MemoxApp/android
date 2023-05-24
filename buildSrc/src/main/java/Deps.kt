import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.androidXLifeCycle() = "androidx.lifecycle:lifecycle-runtime-ktx:2.4.0"

object Markdown{
    private val org = "org.commonmark"
    private val ver = "0.21.0"
    private val mark = { name:String -> "$org:$name:$ver" }
    val commonMark = mark("commonmark")
    val gfmTable = mark("commonmark-ext-gfm-tables")
    val taskList = mark("commonmark-ext-task-list-items")
    val strikethough = mark("commonmark-ext-gfm-strikethrough")
    val autolink = mark("commonmark-ext-autolink")
    val intelij = "org.jetbrains:markdown:0.4.1"
}

object Version {
    val versionCode = 1
    val versionName = "1.0"
}

object Utils{
    val androidUtils = "com.blankj:utilcodex:1.31.1"
    val jsoup = "org.jsoup:jsoup:1.16.1"
}

object Apollo {
    val apollo = "com.apollographql.apollo3:apollo-runtime:3.8.1"
}

object Accomppanist {
    val systemUiController = "com.google.accompanist:accompanist-systemuicontroller:0.31.1-alpha"
    val navigationAnimation = "com.google.accompanist:accompanist-navigation-animation:0.31.2-alpha"
}

object Moshi {
    val base = "com.squareup.moshi:moshi:1.14.0"
    val codegen = "com.squareup.moshi:moshi-kotlin-codegen:1.14.0"
}

object Coil {
    val base = "io.coil-kt:coil:2.3.0"
    val compose = "io.coil-kt:coil-compose:2.3.0"
}

object AndroidX {
    val core = "androidx.core:core-ktx:1.10.1"
}

object Lifecycle {
    private val lifecycle_version = "2.5.1"
    val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:2.6.1"
    val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
    val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version"
    val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
    val savedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version"

}

object Compose {
    val compilerVersion = "1.4.6"
    val base = "androidx.activity:activity-compose:1.7.1"
    fun DependencyHandler.composeBom() = platform("androidx.compose:compose-bom:2023.05.01")
    val ui = "androidx.compose.ui:ui"
    val graphics = "androidx.compose.ui:ui-graphics"
    val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
    val material3 = "androidx.compose.material3:material3"
    val material = "androidx.compose.material:material"
    val constraint = "androidx.constraintlayout:constraintlayout-compose:1.0.0-alpha08"
    private val nav_version = "2.5.3"
    val navigation = "androidx.navigation:navigation-compose:$nav_version"

    val junit = "androidx.compose.ui:ui-test-junit4"
    val uiTooling = "androidx.compose.ui:ui-tooling"
    val uiTestManifest = "androidx.compose.ui:ui-test-manifest"
}

object Google {
    val material = "com.google.android.material:material:1.9.0"
}

object Kotlin {
    val version = "1.8.20"
    val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
    val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
    val kspVersion = "1.8.20-1.0.11"
}

object MMKV {
    val version = "1.2.16"
//    val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:$version"
    val base = "com.tencent:mmkv:$version"
}


object Test {
    val junit = "junit:junit:4.13.2"
    val junitExt = "androidx.test.ext:junit:1.1.5"
    val expresso = "androidx.test.espresso:espresso-core:3.5.1"
}