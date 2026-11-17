package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class MainActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMainActivity() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        composeTestRule.waitForIdle()
    }
}
