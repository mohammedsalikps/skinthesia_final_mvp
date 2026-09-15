package com.skinthesia.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.utils.BitmapDecoder
import com.skinthesia.domain.model.ImageSource

/**
 * Renders any [ImageSource]. Bundled resources draw immediately; local files are
 * decoded off the main thread and cross-fade in; remote images show a calm
 * placeholder until a network image loader arrives with the content backend.
 */
@Composable
fun SkinthesiaImage(
    source: ImageSource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    maxDimension: Int = 1600,
    colorFilter: ColorFilter? = null,
) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    when (source) {
        is ImageSource.Resource -> Image(
            painter = painterResource(source.resId),
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            colorFilter = colorFilter,
        )

        is ImageSource.LocalFile -> {
            val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = source.path) {
                value = BitmapDecoder.decode(source.path, maxDimension)?.asImageBitmap()
            }
            Box(modifier = modifier.background(colors.surfaceMuted)) {
                Crossfade(
                    targetState = bitmap,
                    animationSpec = tween(motion.duration(motion.slow)),
                    label = "localImage",
                ) { loaded ->
                    if (loaded != null) {
                        Image(
                            bitmap = loaded,
                            contentDescription = contentDescription,
                            modifier = Modifier.fillMaxSize(),
                            alignment = alignment,
                            contentScale = contentScale,
                            colorFilter = colorFilter,
                        )
                    } else {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }

        is ImageSource.Remote -> Box(modifier = modifier.background(colors.surfaceMuted))
    }
}
