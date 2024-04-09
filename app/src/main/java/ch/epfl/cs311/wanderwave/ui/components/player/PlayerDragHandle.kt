package ch.epfl.cs311.wanderwave.ui.components.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.components.animated.ColorRange
import ch.epfl.cs311.wanderwave.ui.components.animated.FlexibleRectangle
import ch.epfl.cs311.wanderwave.ui.components.animated.RectangleConstraints
import ch.epfl.cs311.wanderwave.ui.theme.orange
import ch.epfl.cs311.wanderwave.ui.theme.spotify_green

@Composable
fun PlayerDragHandle() {
  BoxWithConstraints {
    val boxWidth = this.maxWidth
    val mainRectangleConstraints =
        RectangleConstraints(
            startWidth = 12f, startHeight = 12f, endWidth = boxWidth.value - 20f, endHeight = 3f)
    val trailingRectangleConstraints =
        RectangleConstraints(
            startWidth = 0f, startHeight = 1f, endWidth = boxWidth.value, endHeight = 3f)
    val trailingColorRange =
        ColorRange(startColor = Color.White, endColor = MaterialTheme.colorScheme.background)
    Column(
        modifier =
            Modifier.height(63.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = .7f)),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.Start) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
                painter = painterResource(id = R.drawable.play_icon),
                contentDescription = "Play icon",
                modifier = Modifier.width(12.dp),
                tint = MaterialTheme.colorScheme.primary)
            FlexibleRectangle(
                rectangleConstraints = mainRectangleConstraints,
                colorRange =
                    ColorRange(
                        startColor = spotify_green, endColor = MaterialTheme.colorScheme.primary),
                durationMillis = 5000,
                easing = LinearOutSlowInEasing)
            Icon(
                painter = painterResource(id = R.drawable.wanderwave_icon),
                contentDescription = "Primary Wanderwave icon",
                modifier = Modifier.width(9.dp),
                tint = MaterialTheme.colorScheme.primary)
          }
          FlexibleRectangle(
              rectangleConstraints = trailingRectangleConstraints,
              colorRange = trailingColorRange,
              durationMillis = 10000,
              easing = LinearEasing,
              repeatMode = RepeatMode.Restart)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.stop_icon),
                contentDescription = "Stop icon",
                modifier = Modifier.width(12.dp),
                tint = orange)
            FlexibleRectangle(
                rectangleConstraints = mainRectangleConstraints,
                colorRange = ColorRange(startColor = spotify_green, endColor = orange),
                durationMillis = 4000,
                easing = LinearOutSlowInEasing)
            Icon(
                painter = painterResource(id = R.drawable.wanderwave_icon),
                contentDescription = "Orange Wanderwave icon",
                modifier = Modifier.width(9.dp),
                tint = orange)
          }
          FlexibleRectangle(
              rectangleConstraints = trailingRectangleConstraints,
              colorRange = trailingColorRange,
              durationMillis = 8000,
              easing = LinearEasing,
              repeatMode = RepeatMode.Restart)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.record_icon),
                contentDescription = "Record icon",
                modifier = Modifier.width(12.dp),
                tint = Color.Red)
            FlexibleRectangle(
                rectangleConstraints = mainRectangleConstraints,
                colorRange = ColorRange(startColor = spotify_green, endColor = Color.Red),
                durationMillis = 3000,
                easing = LinearOutSlowInEasing)
            Icon(
                painter = painterResource(id = R.drawable.wanderwave_icon),
                contentDescription = "Red Wanderwave icon",
                modifier = Modifier.width(9.dp),
                tint = Color.Red)
          }
          FlexibleRectangle(
              rectangleConstraints = trailingRectangleConstraints,
              colorRange = trailingColorRange,
              durationMillis = 6000,
              easing = LinearEasing,
              repeatMode = RepeatMode.Restart)
        }
  }
}
