package com.github.damontecres.wholphin.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import com.github.damontecres.wholphin.R
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

/**
 * Full-screen Poob intro video shown once when the app is launched.
 *
 * Calls [onFinished] when the video ends, fails to play, or the safety timeout elapses.
 * Skipping via a key press is handled by the activity.
 */
@OptIn(UnstableApi::class)
@Composable
fun LaunchVideo(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentOnFinished by rememberUpdatedState(onFinished)
    val player =
        remember {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(
                    MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.poob_launch}"),
                )
                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = true
                prepare()
            }
        }

    DisposableEffect(player) {
        val listener =
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) currentOnFinished()
                }

                override fun onPlayerError(error: PlaybackException) {
                    Timber.w(error, "Launch video failed to play, skipping")
                    currentOnFinished()
                }
            }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    // Never let the intro block the app if playback stalls
    LaunchedEffect(Unit) {
        delay(30.seconds)
        currentOnFinished()
    }

    val presentationState = rememberPresentationState(player)
    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        // TextureView so the video composites correctly on top of the app content underneath
        PlayerSurface(
            player = player,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            modifier = Modifier.resizeWithContentScale(ContentScale.Fit, presentationState.videoSizeDp),
        )
    }
}
