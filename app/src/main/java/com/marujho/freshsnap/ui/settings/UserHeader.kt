package com.marujho.freshsnap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marujho.freshsnap.ui.settings.Account.SettingsAccountViewModel

@Composable
fun UserHeader(
    viewModel: SettingsAccountViewModel = viewModel()
) {

    val name by viewModel.userName.collectAsStateWithLifecycle()
    val age by viewModel.userAge.collectAsStateWithLifecycle()
    val gender by viewModel.userGender.collectAsStateWithLifecycle()

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "$gender · $age años",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )
                .padding(8.dp),
            tint = Color.White
        )
    }
}
