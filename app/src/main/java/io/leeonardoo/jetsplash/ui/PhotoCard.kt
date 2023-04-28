package io.leeonardoo.jetsplash.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.leeonardoo.jetsplash.model.UnsplashPhoto

@Composable
fun PhotoCard(photo: UnsplashPhoto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            modifier = Modifier.height(140.dp),
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.urls.small)
                .crossfade(true)
                .build(),
            contentDescription = photo.altDescription,
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = photo.user.name,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = photo.user.username,
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}