package ch.epfl.cs311.wanderwave.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.protocol.types.ListItem
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
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
  @RelaxedMockK private lateinit var profileRepository: ProfileConnection

  @RelaxedMockK private lateinit var spotifyController: SpotifyController

  @RelaxedMockK private lateinit var authenticationController: AuthenticationController

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    viewModel = ProfileViewModel(profileRepository, spotifyController, authenticationController)
  }

  @After
  fun tearDown() {
    try {
      testDispatcher.cleanupTestCoroutines()
    } finally {
      Dispatchers.resetMain() // Always reset the dispatcher
    }
  }

  @After
  fun clearMocks() {
    clearAllMocks() // Clear all MockK mocks
  }

  //  override fun getTracksFromPlaylist(
  //    playlistId: String,
  //    playlist: MutableStateFlow<List<ListItem>>
  //  ) {
  //    viewModelScope.launch {
  //      getTracksFromSpotifyPlaylist(playlistId, playlist, spotifyController, viewModelScope)
  //    }
  //  }

  @Test
  fun testGetTracksFromPlaylist() = runBlockingTest {
    val playlistId = "Some Playlist ID"

    // Call getTracksFromPlaylist to initialize the playlist
    viewModel.getTracksFromPlaylist(playlistId)
  }

  @Test
  fun testChangeChosenSongs() = runBlockingTest {
    // Ensure the initial value is true
    assertTrue(viewModel.isTopSongsListVisible.value)

    // Change the value
    viewModel.changeChosenSongs()

    // Ensure the value is now false
    assertFalse(viewModel.isTopSongsListVisible.value)
  }

  @Test
  fun testAddTrackToList() = runBlockingTest {
    // Define a new track
    val newTrack = Track("Some Track ID", "Track Title", "Artist Name")
    val expectedTrack = Track("spotify:track:Some Track ID", "Track Title", "Artist Name")

    // Ensure song lists are initially empty
    assertTrue(viewModel.songLists.value.isEmpty())

    // Call createSpecificSongList to initialize a list
    viewModel.createSpecificSongList(ListType.TOP_SONGS)

    // Add track to "TOP SONGS"
    viewModel.addTrackToList(ListType.TOP_SONGS, newTrack)

    // Get the updated song list
    val songLists = viewModel.songLists.value
    assertFalse("Song list should not be empty after adding a track", songLists.isEmpty())

    // Check if the track was added correctly
    val songsInList = songLists.find { it.name == ListType.TOP_SONGS }?.tracks ?: emptyList()
    assertTrue(
        "Song list should contain the newly added track", songsInList.contains(expectedTrack))
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
  fun testRetrieveSubsectionAndChildrenFlow() = runBlockingTest {
    val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)
    every { spotifyController.getAllElementFromSpotify() } returns flowOf(listOf(expectedListItem))
    every {
      spotifyController.getAllChildren(ListItem("id", "title", null, "subtitle", "", false, true))
    } returns flowOf(listOf(expectedListItem))
    viewModel.retrieveAndAddSubsection()
    viewModel.retrieveChild(expectedListItem)
    advanceUntilIdle() // Ensure all coroutines are completed

    // val result = viewModel.spotifySubsectionList.first()  // Safely access the first item
    val flow = viewModel.spotifySubsectionList
    val flow2 = viewModel.childrenPlaylistTrackList
    val result = flow.timeout(2.seconds).catch {}.firstOrNull()
    val result2 = flow2.timeout(2.seconds).catch {}.firstOrNull()

    assertEquals(expectedListItem, result?.get(0))
    assertEquals(expectedListItem, result2?.get(0))
  }

  @Test
  fun testRetrieveTracksFromSpotify() = runBlocking {
    val listItem = ListItem("id", "title", null, "subtitle", "", false, true)
    val expectedListItem = ListItem("spotify:track:id", "title", null, "subtitle", "", false, true)
    every { spotifyController.getAllElementFromSpotify() } returns flowOf(listOf(listItem))
    every {
      spotifyController.getAllChildren(ListItem("id", "title", null, "subtitle", "", false, true))
    } returns flowOf(listOf(listItem))
    viewModel.createSpecificSongList(ListType.TOP_SONGS)
    viewModel.retrieveTracksFromSpotify()

    val flow = viewModel.songLists
    val result = flow.timeout(2.seconds).catch {}.firstOrNull()

    assertEquals(expectedListItem.id, result?.get(0)?.tracks?.get(0)?.id)
  }

  @Test
  fun testSelectTrack() = runTest {
    val track = Track("id", "title", "artist")
    viewModel.createSpecificSongList(ListType.TOP_SONGS)
    viewModel.selectTrack(track, ListType.TOP_SONGS.name)
    verify { spotifyController.playTrackList(any(), any(), any(), any()) }

    viewModel.selectTrack(track, "fake name")
    verify { spotifyController.playTrack(track) }
  }

  @Test
  fun testGetProfileByID() = runBlocking {
    // Arrange
    val testId = "testId"
    val testProfile =
        Profile(
            firstName = "Test",
            lastName = "User",
            description = "Test Description",
            numberOfLikes = 0,
            isPublic = true,
            spotifyUid = "Test Spotify UID",
            firebaseUid = "Test Firebase UID",
            profilePictureUri = null)
    val testFlow = flowOf(Result.success(testProfile))

    every { profileRepository.getItem(testId) } returns testFlow

    // Act
    viewModel.getProfileByID(testId)

    // Assert
    assertEquals(testProfile, viewModel.profile.value)
    assertEquals(
        ProfileViewModel.UIState(profile = testProfile, isLoading = false), viewModel.uiState.value)

    // failure case
    val testFlowError = flowOf(Result.failure<Profile>(Exception("Test Exception")))
    every { profileRepository.getItem(testId) } returns testFlowError

    viewModel.getProfileByID(testId)
    assertEquals(
        ProfileViewModel.UIState(profile = null, isLoading = false, error = "Test Exception"),
        viewModel.uiState.value)
  }

  @Test
  fun testCreateProfile() = runBlockingTest {
    every { profileRepository.isUidExisting("firebaseUid", any()) } answers
        {
          val callback = arg<(Boolean, Profile?) -> Unit>(1)
          callback(false, null)
        }

    viewModel.getProfileByID("firebaseUid", true)

    verify { profileRepository.addItemWithId(any()) }

    viewModel.getProfileByID("firebaseUid", false)
  }
}
