package com.example.dhvani.ui.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoSignPlayer(
    assetPath: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    repeat: Boolean = true
) {
    val context = LocalContext.current
    val exoPlayer = remember { PlayerPool.acquire(context) }

    LaunchedEffect(exoPlayer, autoPlay, repeat) {
        exoPlayer.playWhenReady = autoPlay
        exoPlayer.repeatMode = if (repeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    LaunchedEffect(assetPath) {
        val uri = if (assetPath.startsWith("http")) {
            Uri.parse(assetPath)
        } else {
            Uri.parse("file:///android_asset/$assetPath")
        }
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            PlayerPool.release(exoPlayer)
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(24.dp))) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
