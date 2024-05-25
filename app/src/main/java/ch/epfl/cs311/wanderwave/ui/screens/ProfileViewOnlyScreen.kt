package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.profile.ClickableIcon
import ch.epfl.cs311.wanderwave.ui.components.profile.SongsListDisplay
import ch.epfl.cs311.wanderwave.ui.components.profile.VisitCard
import ch.epfl.cs311.wanderwave.ui.components.utils.LoadingScreen
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel

val mockSongLists = listOf(Track("3", "Track 3", "Artist 3"), Track("4", "Track 4", "Artist 4"))
// TODO: modify this, because the profile.songLists is not available yet

/**
 * This is the screen composable which can only show the profile of the user. It includes a visit
 * card and a list of songs. This screen is not modifiable.
 *
 * @param profile The profile to display.
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
@Composable
fun ProfileViewOnlyScreen(
    profileId: String,
    navigationActions: NavigationActions,
    viewModel: ProfileViewModel = hiltViewModel()
) {

  LaunchedEffect(profileId) { viewModel.getProfileByID(profileId, false) }

  val uiState = viewModel.uiState.collectAsState().value

  ClickableIcon(icon = Icons.Default.ArrowBack, onClick = { navigationActions?.goBack() })
  if (uiState.isLoading) {
    LoadingScreen()
  } else {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("profileScreen"),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Box(modifier = Modifier.fillMaxWidth()) {
            VisitCard(Modifier, uiState.profile!!)
            // to #127
          }

          SongsListDisplay(
              navigationActions = navigationActions,
              mockSongLists,
              {},
              onSelectTrack = { viewModel.selectTrack(it) },
              canAddSong = false,
          )
        }
  }
}
