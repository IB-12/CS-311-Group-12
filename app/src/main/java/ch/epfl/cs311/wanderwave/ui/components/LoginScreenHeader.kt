package ch.epfl.cs311.wanderwave.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R

@Composable
fun LoginScreenHeader(modifier: Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(id = R.string.header_title), style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}