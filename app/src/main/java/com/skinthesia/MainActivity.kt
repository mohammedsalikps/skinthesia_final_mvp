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
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.SkinthesiaNavHost

class MainActivity : ComponentActivity() {

    private val rootViewModel: RootViewModel by viewModels {
        (application as SkinthesiaApp).container.viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        splashScreen.setKeepOnScreenCondition { rootViewModel.startDestination.value == null }

        val container = (application as SkinthesiaApp).container
        setContent {
            SkinthesiaTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    val startDestination by rootViewModel.startDestination.collectAsStateWithLifecycle()
                    startDestination?.let { destination ->
                        SkinthesiaNavHost(startDestination = destination)
                    }
                }
            }
        }
    }
}
