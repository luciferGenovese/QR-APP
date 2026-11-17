package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.example.ui.screens.HomeScreen
import com.example.ui.MainViewModel
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.CodeRepository
import androidx.room.Room
import android.content.Context

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
    @Test
    fun testHomeScreen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val repo = CodeRepository(db.codeDao())
        val prefs = context.getSharedPreferences("test", Context.MODE_PRIVATE)
        val viewModel = MainViewModel(context, repo, prefs)
        
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToCreate = {},
                onNavigateToEdit = {},
                onNavigateToBarcode = {},
                onNavigateToEditBarcode = {},
                onNavigateToScan = {},
                onNavigateToTransfer = {}
            )
        }
        
        // Wait for idle to catch any crashes during composition
        composeTestRule.waitForIdle()
    }
}
