package com.example.dhvani.ui.components

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import java.util.LinkedList

/**
 * A simple pool for ExoPlayer instances to avoid high overhead of creating/releasing 
 * players frequently in lists or grids.
 */
@UnstableApi
object PlayerPool {
    private val pool = LinkedList<ExoPlayer>()
    private const val MAX_POOL_SIZE = 3

    fun acquire(context: Context): ExoPlayer {
        return if (pool.isNotEmpty()) {
            pool.removeFirst()
        } else {
            ExoPlayer.Builder(context).build().apply {
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                repeatMode = Player.REPEAT_MODE_ALL
                playWhenReady = true
            }
        }
    }

    fun release(player: ExoPlayer) {
        player.stop()
        player.clearMediaItems()
        if (pool.size < MAX_POOL_SIZE) {
            pool.addLast(player)
        } else {
            player.release()
        }
    }
}
