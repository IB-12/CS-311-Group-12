package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.map.WanderwaveGoogleMap
import ch.epfl.cs311.wanderwave.ui.components.map.WanderwaveMapMarker
import ch.epfl.cs311.wanderwave.ui.theme.WanderwaveTheme
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun BeaconScreen(
    navigationActions: NavigationActions,
    viewModel: BeaconViewModel = hiltViewModel()
) {
  // id value remebered for the text field
  // Here is the id of a good beacon for testing : UAn8OUadgrUOKYagf8a2
  val uiState = viewModel.uiState.collectAsState().value
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (!uiState.isLoading) {
      BeaconScreen(beacon = uiState.beacon!!)
    } else {
      Text("Beacon not found")
    }
  }
}

@Composable
@Preview(showBackground = true)
private fun BeaconScreenPreview() {
  val previewBeacon =
      Beacon(
          id = "a",
          location = Location(latitude = 46.519962, longitude = 6.633597, name = "EPFL"),
          tracks =
              listOf(
                  Track("a", "Never gonna give you up", "Rick Astley"),
                  Track("b", "Take on me", "A-ha"),
                  Track("c", "Africa", "Toto"),
              ),
      )
  WanderwaveTheme { BeaconScreen(previewBeacon) }
}

@Composable
private fun BeaconScreen(beacon: Beacon) {
  Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        BeaconInformation(beacon.location)
        SongList(beacon)
      }
}

@Composable
fun BeaconInformation(location: Location) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = "Beacon", style = MaterialTheme.typography.displayLarge)
    if (location.name.isNotBlank()) {
      Text("at ${location.name}", style = MaterialTheme.typography.titleMedium)
    }
    // TODO: Maybe add location tracking here too?
    WanderwaveGoogleMap(
      cameraPositionState = CameraPositionState(
        CameraPosition(LatLng(location.latitude, location.longitude), 15f, 0f, 0f)
      ),
      locationSource = null,
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(4f / 3)
        .padding(4.dp)
        .clip(RoundedCornerShape(8.dp)),
      controlsEnabled = false,
    ) {
      WanderwaveMapMarker(location.toLatLng(), location.name, "Beacon location")
    }
  }
}

@Composable
fun SongList(beacon: Beacon) {
  HorizontalDivider()
  Text(text = "Tracks", style = MaterialTheme.typography.displayMedium)
  LazyColumn {
    items(beacon.tracks) { TrackItem(it) }
  }
}

@Composable
internal fun TrackItem(track: Track) {
  Card(
    colors = CardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      CardDefaults.cardColors().contentColor,
      CardDefaults.cardColors().disabledContainerColor,
      CardDefaults.cardColors().disabledContentColor
    ),
    modifier = Modifier
      .height(80.dp)
      .fillMaxWidth()
      .padding(4.dp)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(1f), contentAlignment = Alignment.Center) {
        Image(imageVector = Icons.Default.PlayArrow, contentDescription = "Album Cover",
          modifier = Modifier.fillMaxSize(.8f),
        )
      }
      Column(modifier = Modifier.padding(8.dp)) {
        Text(
          text = track.title,
          color = MaterialTheme.colorScheme.onSurface,
          style = MaterialTheme.typography.titleMedium
        )
        Text(
          text = track.artist,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }
}