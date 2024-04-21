package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.Called
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackListScreenTest : TestCase() {

  private val testDispatcher = TestCoroutineDispatcher()

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK lateinit var trackConnection: TrackConnection

  @RelaxedMockK lateinit var mockShowMessage: (String) -> Unit

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testDispatcher.cleanupTestCoroutines()
  }

  private fun setupViewModel(result: Boolean) {
    every { mockSpotifyController.playTrack(any()) } returns flowOf(result)

    val viewModel = TrackListViewModel(mockSpotifyController, trackConnection)

    composeTestRule.setContent { TrackListScreen(mockShowMessage, viewModel) }
  }

  @Test
  fun trackListScreenIsDisplayed() =
      testDispatcher.runBlockingTest {
        setupViewModel(true)
        onComposeScreen<TrackListScreen>(composeTestRule) {
          assertIsDisplayed()

          trackButton {
            assertIsDisplayed()
            assert(hasClickAction())
          }
        }
      }

  @Test
  fun tappingTrackPlaysIt() =
      testDispatcher.runBlockingTest {
        setupViewModel(true)
        onComposeScreen<TrackListScreen>(composeTestRule) {
          trackButton {
            assertIsDisplayed()
            performClick()
          }
          advanceUntilIdle()
          verify { mockSpotifyController.playTrack(any()) }
          coVerify { mockShowMessage wasNot Called }
        }
      }

  @Test
  fun failedToPlayTrackDisplaysMessage() =
      testDispatcher.runBlockingTest {
        setupViewModel(false)
        onComposeScreen<TrackListScreen>(composeTestRule) {
          trackButton {
            assertIsDisplayed()
            performClick()
          }
          advanceUntilIdle()
          verify { mockSpotifyController.playTrack(any()) }
        }
      }
}
