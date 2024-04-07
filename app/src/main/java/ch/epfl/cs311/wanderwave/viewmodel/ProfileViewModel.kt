package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.data.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

  private val _profile =
      MutableStateFlow(
          Profile(
              firstName = "My FirstName",
              lastName = "My LastName",
              description = "My Description",
              numberOfLikes = 0,
              isPublic = true,
              spotifyUid = "My Spotify UID",
              firebaseUid = "My Firebase UID",
              profilePictureUri = null))
  val profile: StateFlow<Profile> = _profile

  private val _isInEditMode = MutableStateFlow(false)
  val isInEditMode: StateFlow<Boolean> = _isInEditMode

  private val _isInPublicMode = MutableStateFlow(false)
  val isInPublicMode: StateFlow<Boolean> = _isInPublicMode

  val profileConnection = ProfileConnection()

  fun updateProfile(updatedProfile: Profile) {
    _profile.value = updatedProfile
  }

  fun togglePublicMode() {
    _isInPublicMode.value = !_isInPublicMode.value
  }

  fun fetchProfile(profile: Profile) {
    // TODO : fetch profile from Spotify
    // _profile.value = spotifyConnection.getProfile()....
    // Fetch profile from Firestore if it doesn't exist, create it

    profileConnection.isUidExisting(profile.spotifyUid) { isExisting, fetchedProfile ->
      if (isExisting) {

        _profile.value = fetchedProfile
        // update profile on the local database
        viewModelScope.launch { repository.insert(fetchedProfile!!) }
      } else {
        val newProfile = profile
        profileConnection.addItem(newProfile)
        viewModelScope.launch { repository.insert(fetchedProfile!!) }
        _profile.value = newProfile
      }
    }
  }

  fun fetchProfile(profile: Profile) {
    // TODO : fetch profile from Spotify
    // _profile.value = spotifyConnection.getProfile()....
    // Fetch profile from Firestore if it doesn't exist, create it

    profileConnection.isUidExisting(profile.spotifyUid) { isExisting, fetchedProfile ->
      if (isExisting) {

        _profile.value = fetchedProfile
        // update profile on the local database
        viewModelScope.launch { repository.insert(fetchedProfile!!) }
      } else {
        val newProfile = profile
        profileConnection.addItem(newProfile)
        viewModelScope.launch { repository.insert(fetchedProfile!!) }
        _profile.value = newProfile
      }
    }
  }
}
