package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel
class TrackListViewModel
@Inject
constructor(
    private val repository: TrackRepositoryImpl,
    private val spotifyController: SpotifyController
) : ViewModel() {

  private val _uiState = MutableStateFlow(UiState(loading = true))
  val uiState: StateFlow<UiState> = _uiState

  init {
    observeTracks()
  }

  private fun observeTracks() {
    CoroutineScope(Dispatchers.IO).launch {
      repository.getAll().collect { tracks ->
        _uiState.value = UiState(tracks = tracks, loading = false)
      }
    }
  }

  /**
   * Plays the given track using the SpotifyController.
   *
   * @param track The track to play.
   */
  private fun playTrack(track: Track) {
    CoroutineScope(Dispatchers.IO).launch {
      val success = spotifyController.playTrack(track).firstOrNull()
      if (success == null || !success) {
        _uiState.value = _uiState.value.copy(message = "Failed to play track")
      }
    }
  }

  /** Resumes the currently paused track using the SpotifyController. */
  private fun resumeTrack() {
    CoroutineScope(Dispatchers.IO).launch {
      val success = spotifyController.resumeTrack().firstOrNull()
      if (success == null || !success) {
        _uiState.value = _uiState.value.copy(message = "Failed to resume track")
      }
    }
  }

  /** Pauses the currently playing track using the SpotifyController. */
  private fun pauseTrack() {
    CoroutineScope(Dispatchers.IO).launch {
      val success = spotifyController.pauseTrack().firstOrNull()
      if (success == null || !success) {
        _uiState.value = _uiState.value.copy(message = "Failed to pause track")
      }
    }
  }

  /**
   * Selects the given track and updates the UI state accordingly.
   *
   * @param track The track to select.
   */
  fun selectTrack(track: Track) {
    _uiState.value = _uiState.value.copy(selectedTrack = track)
    _uiState.value = _uiState.value.copy(pausedTrack = null)
    if (_uiState.value.isPlaying) playTrack(track)
  }

  fun collapse() {
    _uiState.value = _uiState.value.copy(expanded = false)
  }

  fun expand() {
    _uiState.value = _uiState.value.copy(expanded = true)
  }

  /**
   * Plays the selected track if it's not already playing or resumes the paused track if it's the
   * same as the selected track. If no track is selected, it updates the UI state with an
   * appropriate message.
   */
  fun play() {
    if (_uiState.value.selectedTrack != null && !_uiState.value.isPlaying) {

      if (_uiState.value.pausedTrack == _uiState.value.selectedTrack) {
        resumeTrack()
      } else {
        playTrack(_uiState.value.selectedTrack!!)
      }

      _uiState.value = _uiState.value.copy(isPlaying = true)
    } else {
      if (!_uiState.value.isPlaying) {
        _uiState.value = _uiState.value.copy(message = "No track selected")
      } else {
        _uiState.value = _uiState.value.copy(message = "Track already playing")
      }
    }
  }

  /**
   * Pauses the currently playing track and updates the UI state accordingly. If no track is
   * playing, it updates the UI state with an appropriate message.
   */
  fun pause() {
    if (_uiState.value.isPlaying) {
      pauseTrack()
      _uiState.value =
          _uiState.value.copy(
              isPlaying = false, currentMillis = 1000, pausedTrack = _uiState.value.selectedTrack)
    } else {
      _uiState.value = _uiState.value.copy(message = "No track playing")
    }
  }

  /**
   * Skips to the next or previous track in the list.
   *
   * @param dir The direction to skip in. 1 for next, -1 for previous.
   */
  private fun skip(dir: Int) {
    if (_uiState.value.selectedTrack != null && (dir == 1 || dir == -1)) {
      _uiState.value.tracks.indexOf(_uiState.value.selectedTrack).let { it: Int ->
        var next = it + dir
        if (uiState.value.isLooping) {
          next = Math.floorMod((it + dir), _uiState.value.tracks.size)
        }
        if (next >= 0 && next < _uiState.value.tracks.size) {
          selectTrack(_uiState.value.tracks[next])
        } else {
          pause()
          _uiState.value = _uiState.value.copy(selectedTrack = null)
        }
      }
    }
  }

  /** Skips to the next track in the list. */
  fun skipForward() {
    skip(1)
  }

  /** Skips to the previous track in the list. */
  fun skipBackward() {
    skip(-1)
  }

  /** Toggles the looping state of the player. */
  fun toggleLoop() {
    setLoop(!_uiState.value.isLooping)
  }

  /** Sets the looping state of the player. */
  fun setLoop(isLooping: Boolean) {
    _uiState.value = _uiState.value.copy(isLooping = isLooping)
  }

  data class UiState(
      val tracks: List<Track> = listOf(),
      val loading: Boolean = false,
      val message: String? = null,
      val selectedTrack: Track? = null,
      val pausedTrack: Track? = null,
      val isPlaying: Boolean = false,
      val isLooping: Boolean = false,
      val currentMillis: Int = 0,
      val expanded: Boolean = false,
      val progress: Float = 0f
  )
}
