package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.protocol.types.ListItem
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ProfileViewModelTest {

  lateinit var viewModel: ProfileViewModel
  val testDispatcher = TestCoroutineDispatcher()
  @get:Rule val mockkRule = MockKRule(this)
  @RelaxedMockK private lateinit var profileRepositoryImpl: ProfileRepositoryImpl

  @RelaxedMockK private lateinit var spotifyController: SpotifyController

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    viewModel = ProfileViewModel(profileRepositoryImpl, spotifyController)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain() // Reset the main dispatcher to the original one
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  fun testAddTrackToList() = runBlockingTest {
    // Define a new track
    val newTrack = Track("Some Track ID", "Track Title", "Artist Name")

    // Ensure song lists are initially empty
    assertTrue(viewModel.songLists.value.isEmpty())

    // Call createSpecificSongList to initialize a list
    viewModel.createSpecificSongList("TOP_SONGS")

    // Add track to "TOP SONGS"
    viewModel.addTrackToList("TOP SONGS", newTrack)

    // Get the updated song list
    val songLists = viewModel.songLists.value
    assertFalse("Song list should not be empty after adding a track", songLists.isEmpty())

    // Check if the track was added correctly
    val songsInList = songLists.find { it.name == "TOP SONGS" }?.tracks ?: emptyList()
    assertTrue("Song list should contain the newly added track", songsInList.contains(newTrack))
  }

  @Test
  fun retrieveTopTrack_doesNotAddTrackToTopSongs_whenTrackHasNoChildren() = runBlockingTest {
    val track = ListItem("id", "", null, "", "", false, false)
    every { spotifyController.getTrack() } returns flowOf(track)

    viewModel.retrieveTopTrack()

    val topSongs = viewModel.songLists.value.find { it.name == "TOP SONGS" }
    assertTrue(topSongs?.tracks?.isEmpty() ?: true)
  }

  @Test
  fun retrieveTopTrack_doesNotAddTrackToTopSongs_whenTrackHasChildren() = runBlockingTest {
    val track = ListItem("id", "", null, "", "", false, true)
    every { spotifyController.getTrack() } returns flowOf(track)

    viewModel.retrieveTopTrack()

    val topSongs = viewModel.songLists.value.find { it.name == "TOP SONGS" }
    assertTrue(topSongs?.tracks?.isEmpty() ?: true)
  }

  @Test
  fun retrieveTopTrack_doesNotAddTrackToTopSongs_whenTrackIdIsEmpty() = runBlockingTest {
    val track = ListItem("", "", null, "", "", false, false)
    every { spotifyController.getTrack() } returns flowOf(track)

    viewModel.retrieveTopTrack()

    val topSongs = viewModel.songLists.value.find { it.name == "TOP SONGS" }
    assertTrue(topSongs?.tracks?.isEmpty() ?: true)
  }

  @Test
  fun retrieveTrack_doesNotAddTrackToSongs_whenTrackHasNoChildren() = runBlockingTest {
    val track = ListItem("id", "", null, "", "", false, false)
    every { spotifyController.getTrack() } returns flowOf(track)

    viewModel.retrieveTracks()

    val topSongs = viewModel.songLists.value.find { it.name == "TOP SONGS" }
    assertTrue(topSongs?.tracks?.isEmpty() ?: true)
  }

  @Test
  fun retrieveTrack_doesNotAddTrackSongs_whenTrackHasChildren() = runBlockingTest {
    val track = ListItem("id", "", null, "", "", false, true)
    every { spotifyController.getTrack() } returns flowOf(track)

    viewModel.retrieveTracks()

    val topSongs = viewModel.songLists.value.find { it.name == "TOP SONGS" }
    assertTrue(topSongs?.tracks?.isEmpty() ?: true)
  }

  @Test
  fun retrieveTrack_doesNotAddTrackToSongs_whenTrackIdIsEmpty() = runBlockingTest {
    val track = ListItem("", "", null, "", "", false, false)
    every { spotifyController.getTrack() } returns flowOf(track)

    viewModel.retrieveTracks()

    val topSongs = viewModel.songLists.value.find { it.name == "TOP SONGS" }
    assertTrue(topSongs?.tracks?.isEmpty() ?: true)
  }

  @Test
  fun retrieveTrackTest() = runBlockingTest {
    val track = ListItem("bbbb", "bbbb", null, "bbbb", "bbbb", false, true)
    val track2 = ListItem("aaaa", "aaaaa", null, "aaaaa", "aaaaa", false, true)
    every { spotifyController.getAllElementFromSpotify() } returns flowOf(listOf(track))
    every { spotifyController.getAllChildren(track) } returns
        flowOf(listOf(track2)) // No empty list first

    viewModel.createSpecificSongList("TOP_SONGS")
    viewModel.retrieveTracks()
    advanceUntilIdle()
    var songLists = viewModel.songLists.value
    assertFalse("Song list should not be empty after adding a track", songLists.isEmpty())

    songLists = viewModel.songLists.value
    // Check if the track was added correctly
    val songsInList = songLists.find { it.name == "TOP SONGS" }?.tracks ?: emptyList()
    Log.d("Temp", songsInList.toString())
  }

  @Test
  fun testGetAllChildrenFlow() = runBlockingTest {
    val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)
    every { spotifyController.getAllChildren(expectedListItem) } returns
        flowOf(listOf(expectedListItem))

    val result = spotifyController.getAllChildren(expectedListItem)
    assertEquals(expectedListItem, result.first().get(0)) // Check if the first item is as expected
  }

  @Test
  fun testRetrieveSubsection() = runBlockingTest {
    val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)
    every { spotifyController.getAllElementFromSpotify() } returns flowOf(listOf(expectedListItem))

    viewModel.retrieveAndAddSubsection()

    advanceUntilIdle() // Ensure all coroutines are completed

    // val result = viewModel.spotifySubsectionList.first()  // Safely access the first item
    val flow = viewModel.spotifySubsectionList
    val result = flow.timeout(2.seconds).catch {}.firstOrNull()
    Log.d("restut", result.toString())
    // assertEquals(expectedListItem, result?.get(0))
  }

  @Test
  fun testRetrieveChild() = runBlockingTest {
    val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)
    every {
      spotifyController.getAllChildren(ListItem("id", "title", null, "subtitle", "", false, true))
    } returns flowOf(listOf(expectedListItem))

    viewModel.retrieveChild(expectedListItem)
    val flow = viewModel.childrenList

    val result = flow.timeout(2.seconds).catch {}.firstOrNull()
    assertEquals(expectedListItem, result?.get(0))
  }
}
