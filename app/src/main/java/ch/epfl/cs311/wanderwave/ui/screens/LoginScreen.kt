package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.ui.components.login.LoginScreenHeader
import ch.epfl.cs311.wanderwave.ui.components.login.SignInButton
import ch.epfl.cs311.wanderwave.ui.components.login.WelcomeTitle
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.navigation.TOP_LEVEL_DESTINATIONS
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun LoginScreen(navigationActions: NavigationActions, profileViewModel: ProfileViewModel = hiltViewModel()) {
  Column(modifier = Modifier.testTag("loginScreen")) {
    LoginScreenHeader(modifier = Modifier.weight(1.5f))
    WelcomeTitle(modifier = Modifier.weight(4f))
    SignInButton(modifier = Modifier.weight(1f)) {
      // TODO : fetch the profile from the spotify API
      var profile =
          Profile(
              "John",
              "Doe",
              description = "I am a wanderer",
              numberOfLikes = 0,
              isPublic = true,
              spotifyUid = "123",
              firebaseUid = "123",
              profilePictureUri = null)
      profileViewModel.fetchProfile(profile)
      navigationActions.navigateTo(TOP_LEVEL_DESTINATIONS.first { it.route == Route.MAIN })
    }
  }
}
