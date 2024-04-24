import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.viewmodel.LoopMode
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrackListViewModelTest {

  private lateinit var viewModel: TrackListViewModel

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK private lateinit var repository: TrackRepositoryImpl

  private lateinit var track: Track

  @Before
  fun setup() {
    val connectResult = SpotifyController.ConnectResult.SUCCESS
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)

    repository = mockk()

    track = Track("1cNf5WAYWuQwGoJyfsHcEF", "Across The Stars", "John Williams")

    val track1 = Track("6ImuyUQYhJKEKFtlrstHCD", "Main Title", "John Williams")
    val track2 = Track("0HLQFjnwq0FHpNVxormx60", "The Nightingale", "Percival Schuttenbach")
    val track3 = Track("2NZhNbfb1rD1aRj3hZaoqk", "The Imperial Suite", "Michael Giacchino")
    val track4 = Track("5EWPGh7jbTNO2wakv8LjUI", "Free Bird", "Lynyrd Skynyrd")
    val track5 = Track("4rTlPsga6T8yiHGOvZAPhJ", "Godzilla", "Eminem")

    val trackList =
        listOf(
            track,
            track1,
            track2,
            track3,
            track4,
            track5,
        )

    every { repository.getAll() } returns flowOf(trackList)

    viewModel = TrackListViewModel(repository, mockSpotifyController)

    runBlocking { viewModel.uiState.first { !it.loading } }
  }

  @Test
  fun trackIsProperlySelected() = run {
    viewModel.selectTrack(track)
    assertEquals(track.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun songPlaysProperly() = run {
    assertFalse(viewModel.uiState.value.isPlaying)

    viewModel.selectTrack(track)
    viewModel.play()

    assertTrue(viewModel.uiState.value.isPlaying)
    assertEquals(track.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun songPausesProperly() = run {
    viewModel.selectTrack(track)
    viewModel.play()
    assertTrue(viewModel.uiState.value.isPlaying)

    viewModel.pause()
    assertFalse(viewModel.uiState.value.isPlaying)
    assertEquals(track.id, viewModel.uiState.value.pausedTrack?.id)
  }

  @Test
  fun songResumesProperly() = run {
    viewModel.selectTrack(track)
    viewModel.play()
    viewModel.pause()
    assertFalse(viewModel.uiState.value.isPlaying)

    viewModel.play()
    assertTrue(viewModel.uiState.value.isPlaying)
    assertEquals(track.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun songDoesntPlayWhenNull() = run {
    viewModel.play()
    assertFalse(viewModel.uiState.value.isPlaying)
  }

  @Test
  fun skipForwardWorksProperly() = run {
    assert(viewModel.uiState.value.tracks.isNotEmpty())

    val firstTrack = viewModel.uiState.value.tracks[0]
    val secondTrack = viewModel.uiState.value.tracks[1]

    viewModel.selectTrack(firstTrack)
    assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)

    viewModel.skipForward()
    assertEquals(secondTrack.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun skipForwardAtEndWorksProperly() = run {
    assert(viewModel.uiState.value.tracks.isNotEmpty())

    val firstTrack = viewModel.uiState.value.tracks[0]
    val lastTrack = viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1]

    viewModel.toggleLoop()
    viewModel.selectTrack(lastTrack)
    viewModel.play()
    assertTrue(viewModel.uiState.value.isPlaying)
    assertEquals(lastTrack.id, viewModel.uiState.value.selectedTrack?.id)

    viewModel.skipForward()
    assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun skipBackwardWorksProperly() = run {
    assert(viewModel.uiState.value.tracks.isNotEmpty())

    val firstTrack = viewModel.uiState.value.tracks[1]
    val secondTrack = viewModel.uiState.value.tracks[0]

    viewModel.selectTrack(firstTrack)
    assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)

    viewModel.skipBackward()
    assertEquals(secondTrack.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun skipBackwardAtBeginningWorksProperly() = run {
    assert(viewModel.uiState.value.tracks.isNotEmpty())

    val firstTrack = viewModel.uiState.value.tracks[0]
    val lastTrack = viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1]

    viewModel.toggleLoop()
    viewModel.selectTrack(firstTrack)
    viewModel.play()
    assertTrue(viewModel.uiState.value.isPlaying)
    assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)

    viewModel.skipBackward()
    assertEquals(lastTrack.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun playTrackWhenControllerReturnsFalse() = run {
    every { mockSpotifyController.playTrack(track) } returns flowOf(false)
    viewModel.selectTrack(track)
    viewModel.play()
  }

  @Test
  fun collapseTrackList() = run {
    viewModel.collapse()
    assertFalse(viewModel.uiState.value.expanded)
  }

  @Test
  fun testToggleShuffle() = run {
    viewModel.toggleShuffle()
    assertTrue(viewModel.uiState.value.isShuffled)
    viewModel.toggleShuffle()
    assertFalse(viewModel.uiState.value.isShuffled)
  }

  @Test
  fun testIfQueueHasBeenShuffled() = run {
    assertEquals(viewModel.uiState.value.tracks, viewModel.uiState.value.queue)
    viewModel.toggleShuffle()
    assertNotEquals(viewModel.uiState.value.tracks, viewModel.uiState.value.queue)
  }

  @Test
  fun testIfQueueHasBeenUnshuffled() = run {
    assertEquals(viewModel.uiState.value.tracks, viewModel.uiState.value.queue)
    viewModel.toggleShuffle()
    viewModel.toggleShuffle()
    assertEquals(viewModel.uiState.value.tracks, viewModel.uiState.value.queue)
  }

  @Test
  fun testSkipForwardWhenLooping() = run {
    viewModel.toggleLoop()
    viewModel.selectTrack(viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1])
    viewModel.skipForward()
    assertEquals(viewModel.uiState.value.tracks[0], viewModel.uiState.value.selectedTrack)
  }

  @Test
  fun testSkipForwardWhenNotLooping() = run {
    viewModel.selectTrack(viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1])
    viewModel.skipForward()
    assertNull(viewModel.uiState.value.selectedTrack)
  }

  @Test
  fun testSkipBackwardWhenLooping() = run {
    viewModel.toggleLoop()
    viewModel.selectTrack(viewModel.uiState.value.tracks[0])
    viewModel.skipBackward()
    assertEquals(
        viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1],
        viewModel.uiState.value.selectedTrack)
  }

  @Test
  fun testSkipBackwardWhenNotLooping() = run {
    viewModel.selectTrack(viewModel.uiState.value.tracks[0])
    viewModel.skipBackward()
    assertNull(viewModel.uiState.value.selectedTrack)
  }

  @Test
  fun testLoopToggle() {
    assertEquals(LoopMode.NONE, viewModel.uiState.value.loopMode)
    viewModel.toggleLoop()
    assertEquals(LoopMode.ALL, viewModel.uiState.value.loopMode)
    viewModel.toggleLoop()
    assertEquals(LoopMode.ONE, viewModel.uiState.value.loopMode)
    viewModel.toggleLoop()
    assertEquals(LoopMode.NONE, viewModel.uiState.value.loopMode)
  }

  @Test
  fun testSetLoop() {
    viewModel.setLoop(LoopMode.ALL)
    assertEquals(LoopMode.ALL, viewModel.uiState.value.loopMode)
    viewModel.setLoop(LoopMode.ONE)
    assertEquals(LoopMode.ONE, viewModel.uiState.value.loopMode)
    viewModel.setLoop(LoopMode.NONE)
    assertEquals(LoopMode.NONE, viewModel.uiState.value.loopMode)
  }
}
