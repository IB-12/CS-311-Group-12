package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BeaconViewModel @Inject constructor(private val beaconRepository: BeaconRepository) :
    ViewModel() {
  private val id = "UAn8OUadgrUOKYagf8a2"

  private var _uiState = MutableStateFlow(UIState())

  val uiState: StateFlow<UIState> = _uiState

  init {
    getBeaconById(id)
  }

  fun getBeaconById(id: String) {
    viewModelScope.launch {
      beaconRepository.getItem(id).collect { fetchedBeacon ->
        _uiState.value = UIState(beacon = fetchedBeacon, isLoading = false)
      }
    }
  }

  data class UIState(
      val beacon: Beacon? = null,
      val isLoading: Boolean = true,
      val error: String? = null
  )
}
