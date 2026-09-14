package com.skinthesia

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.SkinthesiaNavHost

class MainActivity : ComponentActivity() {

    private val container: AppContainer by lazy { (application as SkinthesiaApp).container }

    private val rootViewModel: RootViewModel by viewModels {
        viewModelFactory { initializer { RootViewModel(container.profiles, container.startAssessment) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        splashScreen.setKeepOnScreenCondition { rootViewModel.startDestination.value == null }
        setContent {
            SkinthesiaTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    val start by rootViewModel.startDestination.collectAsStateWithLifecycle()
                    start?.let { SkinthesiaNavHost(startDestination = it) }
                }
            }
        }
    }
}
