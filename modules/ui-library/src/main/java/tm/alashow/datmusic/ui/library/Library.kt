/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.base.util.extensions.Callback
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.LibraryItems
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.ui.library.items.LibraryItemRow
import tm.alashow.datmusic.ui.library.playlists.PlaylistRow
import tm.alashow.domain.models.Success
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen
import tm.alashow.ui.components.AppTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.FullScreenLoading
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.theme.AppTheme

@Composable
fun Library() {
    Library(viewModel = hiltViewModel())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Library(
    viewModel: LibraryViewModel,
    navigator: Navigator = LocalNavigator.current
) {
    val listState = rememberLazyListState()
    val asyncLibraryItems by rememberFlowWithLifecycle(viewModel.libraryItems)

    Scaffold(
        topBar = {
            LibraryTopBar(
                onCreatePlaylist = {
                    navigator.navigate(LeafScreen.CreatePlaylist().createRoute())
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddings ->
        when (val items = asyncLibraryItems) {
            is Success -> {
                LazyColumn(
                    contentPadding = paddings,
                    state = listState
                ) {
                    libraryList(
                        items = items(),
                        onDelete = viewModel::deletePlaylist,
                        onDownload = viewModel::downloadPlaylist,
                    )
                }
            }
            else -> FullScreenLoading()
        }
    }
}

@Composable
private fun LibraryTopBar(onCreatePlaylist: Callback = {}) {
    AppTopBar(
        title = stringResource(R.string.library_title),
        actions = {
            IconButton(
                onClick = onCreatePlaylist,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.playlist_create),
                    modifier = Modifier.size(AppTheme.specs.iconSize)
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.libraryList(
    items: LibraryItems = listOf(),
    onDelete: (PlaylistId) -> Unit,
    onDownload: (PlaylistId) -> Unit,
) {
    val downloadsEmpty = items.isEmpty()
    if (downloadsEmpty) {
        item {
            EmptyErrorBox(
                message = stringResource(R.string.library_empty),
                retryVisible = false,
                modifier = Modifier.fillParentMaxHeight()
            )
        }
    }

    items(items, key = { it.getIdentifier() }) {
        val modifier = Modifier.animateItemPlacement()
        when (it) {
            is Playlist -> PlaylistRow(
                playlist = it,
                onDelete = { onDelete(it.id) },
                onDownload = { onDownload(it.id) },
                modifier = modifier
            )
            is Album -> LibraryItemRow(libraryItem = it, typeRes = R.string.albums_detail_title, modifier)
            else -> LibraryItemRow(libraryItem = it, typeRes = R.string.unknown, modifier)
        }
    }
}
