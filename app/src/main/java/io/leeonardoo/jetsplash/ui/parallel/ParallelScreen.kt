package io.leeonardoo.jetsplash.ui.parallel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import io.leeonardoo.jetsplash.api.NetworkError
import io.leeonardoo.jetsplash.api.rememberErrorDescription
import io.leeonardoo.jetsplash.plus
import io.leeonardoo.jetsplash.ui.PhotoCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ParallelScreen(
    viewModel: ParallelViewModel = koinViewModel()
) {

    val error by viewModel.error.collectAsStateWithLifecycle()

    val errorDescription = error?.rememberErrorDescription()

    if (errorDescription != null) {
        AlertDialog(
            onDismissRequest = { viewModel.getRandomPhotos() },
            title = { Text(text = "Error") },
            text = { Text(text = errorDescription) },
            confirmButton = {
                Button(
                    onClick = { viewModel.getRandomPhotos() },
                    content = { Text(text = "Retry") }
                )
            }
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Parallel")
                },
                scrollBehavior = scrollBehavior
            )
        },
        content = {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(170.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = it + PaddingValues(16.dp)
            ) {

                items(items = viewModel.photos, key = { item -> item.id }) { photo ->
                    PhotoCard(photo = photo)
                }
            }
        }
    )
}