package com.skinthesia

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.components.ScoreRing
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.feature.onboarding.WelcomeScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** On-device UI checks for the first impression and two accessibility contracts. */
@RunWith(AndroidJUnit4::class)
class SkinthesiaUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val container: AppContainer
        get() = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as SkinthesiaApp).container

    @Test
    fun welcomeShowsThePromiseAndStartsTheJourney() {
        var started = false
        compose.setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                SkinthesiaTheme { WelcomeScreen(onStart = { started = true }) }
            }
        }
        compose.onNodeWithText("healthier skin", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Start My Journey").assertIsDisplayed().performClick()
        assertTrue("Start My Journey should begin onboarding", started)
    }

    @Test
    fun skinPrintRingAnnouncesItsScoreAndBand() {
        compose.setContent {
            SkinthesiaTheme { ScoreRing(score = 68, label = "Good", animate = false) }
        }
        compose.onNodeWithContentDescription("SkinPrint 68 out of 100, Good").assertIsDisplayed()
    }

    @Test
    fun disabledPrimaryButtonIsExposedAsDisabled() {
        compose.setContent {
            SkinthesiaTheme { SkinthesiaPrimaryButton(text = "Continue", onClick = {}, enabled = false) }
        }
        compose.onNodeWithText("Continue").assertIsNotEnabled()
    }
}
