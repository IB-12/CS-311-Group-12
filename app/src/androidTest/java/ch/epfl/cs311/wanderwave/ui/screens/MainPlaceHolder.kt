package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class MainPlaceHolder(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MainPlaceHolder>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("mainPlaceHolderScreen") }) {
  val singOutButton = onNode { hasTestTag("signOutButton") }
}