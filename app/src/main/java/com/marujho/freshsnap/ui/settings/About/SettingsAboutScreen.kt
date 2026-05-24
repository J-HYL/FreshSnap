package com.marujho.freshsnap.ui.settings.About

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.marujho.freshsnap.R

private data class Attribution(
    val titleRes: Int,
    val descriptionRes: Int,
    val licenseRes: Int?,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAboutScreen() {
    val uriHandler = LocalUriHandler.current

    val externalApis = listOf(
        Attribution(
            titleRes = R.string.about_openfoodfacts,
            descriptionRes = R.string.about_openfoodfacts_desc,
            licenseRes = R.string.license_badge_odbl,
            url = "https://world.openfoodfacts.org/"
        ),
        Attribution(
            titleRes = R.string.about_themealdb,
            descriptionRes = R.string.about_themealdb_desc,
            licenseRes = null,
            url = "https://www.themealdb.com/"
        )
    )

    val libraries = listOf(
        Attribution(
            titleRes = R.string.about_retrofit,
            descriptionRes = R.string.about_retrofit_desc,
            licenseRes = R.string.license_badge_apache,
            url = "https://square.github.io/retrofit/"
        ),
        Attribution(
            titleRes = R.string.about_moshi,
            descriptionRes = R.string.about_moshi_desc,
            licenseRes = R.string.license_badge_apache,
            url = "https://github.com/square/moshi"
        ),
        Attribution(
            titleRes = R.string.about_coil,
            descriptionRes = R.string.about_coil_desc,
            licenseRes = R.string.license_badge_apache,
            url = "https://coil-kt.github.io/coil/"
        ),
        Attribution(
            titleRes = R.string.about_mlkit,
            descriptionRes = R.string.about_mlkit_desc,
            licenseRes = R.string.license_badge_apache,
            url = "https://developers.google.com/ml-kit"
        ),
        Attribution(
            titleRes = R.string.about_jetpack,
            descriptionRes = R.string.about_jetpack_desc,
            licenseRes = R.string.license_badge_apache,
            url = "https://developer.android.com/jetpack"
        ),
        Attribution(
            titleRes = R.string.about_coroutines,
            descriptionRes = R.string.about_coroutines_desc,
            licenseRes = R.string.license_badge_apache,
            url = "https://kotlinlang.org/docs/coroutines-overview.html"
        )
    )

    val resources = listOf(
        Attribution(
            titleRes = R.string.about_material_symbols,
            descriptionRes = R.string.about_material_symbols_desc,
            licenseRes = R.string.license_badge_apache,
            url = "https://fonts.google.com/icons"
        ),
        Attribution(
            titleRes = R.string.about_assistant,
            descriptionRes = R.string.about_assistant_desc,
            licenseRes = R.string.license_badge_ofl,
            url = "https://fonts.google.com/specimen/Assistant"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_about)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            AttributionSection(
                title = stringResource(R.string.about_section_apis),
                note = null,
                items = externalApis,
                onOpenUrl = { uriHandler.openUri(it) }
            )

            AttributionSection(
                title = stringResource(R.string.about_section_libraries),
                note = stringResource(R.string.about_section_libraries_note),
                items = libraries,
                onOpenUrl = { uriHandler.openUri(it) }
            )

            AttributionSection(
                title = stringResource(R.string.about_section_resources),
                note = null,
                items = resources,
                onOpenUrl = { uriHandler.openUri(it) }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.about_section_license),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ProjectLicenseCard(
                onOpenUrl = { uriHandler.openUri("https://www.apache.org/licenses/LICENSE-2.0") }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Creadores",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            CreatorCard(name = "Rubén", githubUsername = "Ruben-am", onOpenUrl = { uriHandler.openUri(it) })
            CreatorCard(name = "Marcos", githubUsername = "Marcoshervas", onOpenUrl = { uriHandler.openUri(it) })
            CreatorCard(name = "Jhon", githubUsername = "J-HYL", onOpenUrl = { uriHandler.openUri(it) })

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun AttributionSection(
    title: String,
    note: String?,
    items: List<Attribution>,
    onOpenUrl: (String) -> Unit
) {
    Spacer(Modifier.height(8.dp))

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    if (note != null) {
        Text(
            text = note,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    items.forEach { item ->
        AttributionCard(
            title = stringResource(item.titleRes),
            description = stringResource(item.descriptionRes),
            license = item.licenseRes?.let { stringResource(it) },
            onClick = { onOpenUrl(item.url) }
        )
    }

    Spacer(Modifier.height(8.dp))
}

@Composable
private fun AttributionCard(
    title: String,
    description: String,
    license: String?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (license != null) {
                    Spacer(Modifier.height(8.dp))
                    LicenseBadge(license)
                }
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.about_open_link),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LicenseBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ProjectLicenseCard(onOpenUrl: () -> Unit) {
    Card(
        onClick = onOpenUrl,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.about_license_apache),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.about_copyright),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.about_open_link),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CreatorCard(name: String, githubUsername: String, onOpenUrl: (String) -> Unit) {
    Card(
        onClick = { onOpenUrl("https://github.com/$githubUsername") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://github.com/$githubUsername.png",
                contentDescription = "Foto de $name",
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = "@$githubUsername", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}