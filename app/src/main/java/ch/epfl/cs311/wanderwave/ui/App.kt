package ch.epfl.cs311.wanderwave.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.localDb.LocalProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import ch.epfl.cs311.wanderwave.ui.components.AppBottomBar
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.LaunchScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.MainPlaceHolder
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.ui.theme.WanderwaveTheme
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel

@Composable
fun App(navController: NavHostController) {
  WanderwaveTheme {
    Surface(
        modifier = Modifier.fillMaxSize().testTag("appScreen"),
        color = MaterialTheme.colorScheme.background) {
          AppScaffold(navController)
        }
  }
}

@Composable
fun AppScaffold(navController: NavHostController) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val navActions = NavigationActions(navController)
  val appDatabase = AppDatabase.getInstance()
  val profileRepositoryImpl = ProfileRepositoryImpl(LocalProfileRepository(AppDatabase.))
  val profileViewModel = ProfileViewModel(profileRepositoryImpl)


  Scaffold(bottomBar = { AppBottomBar(navActions = navActions, currentRoute = currentRoute) }) {
      innerPadding ->
    NavHost(
        navController = navController,
        startDestination = Route.LAUNCH,
        modifier = Modifier.padding(innerPadding)) {
          composable(Route.LAUNCH) { LaunchScreen(navActions) }
          composable(Route.LOGIN) { LoginScreen(navActions, profileViewModel) }
          composable(Route.MAIN) { MainPlaceHolder(navActions) }
          composable(Route.TRACK_LIST) { TrackListScreen() }
          composable(Route.PROFILE_SCREEN) { ProfileScreen(navActions, profileViewModel) }
        }
  }
}
