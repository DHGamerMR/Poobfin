package com.github.damontecres.wholphin.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.CardBorder
import androidx.tv.material3.CardDefaults
import com.github.damontecres.wholphin.preferences.AppThemeColors
import com.github.damontecres.wholphin.ui.theme.LocalTheme

/** Colors from the POOB title card */
object PoobBrand {
    val Red = Color(0xFFE2483A)
    val Teal = Color(0xFF2BB3A3)
    val Gold = Color(0xFFF2C230)
    val Cream = Color(0xFFFFF3DC)
    val Brown = Color(0xFF3B1A0E)
}

/** One ring of a frame. [offset] is how far outside (+) or inside (-) the shape's edge the ring's center sits. */
data class FrameRing(
    val offset: Dp,
    val width: Dp,
    val color: Color,
)

/** The "sunburst double" frame: teal band, red outer line, gold star */
val SunburstRings =
    listOf(
        FrameRing(offset = 4.dp, width = 4.dp, color = PoobBrand.Teal),
        FrameRing(offset = 9.5.dp, width = 3.dp, color = PoobBrand.Red),
    )

@Composable
fun isPoobTheme(): Boolean = LocalTheme.current == AppThemeColors.POOB

/**
 * Card border for the current theme: on the Poob theme the built-in focus outline is removed
 * because [poobCardFocusFrame] draws the frame instead.
 */
@Composable
fun themedCardBorder(): CardBorder =
    if (isPoobTheme()) {
        CardDefaults.border(focusedBorder = Border.None)
    } else {
        CardDefaults.border()
    }

/**
 * Draws the sunburst frame around a TV Card while it is focused (Poob theme only).
 * Apply to the Card's modifier, together with [themedCardBorder].
 *
 * @param shape the card's shape
 * @param focusedScale the card's focused scale, so the frame grows with the card
 */
@Composable
fun Modifier.poobCardFocusFrame(
    interactionSource: InteractionSource?,
    shape: Shape = CardShape,
    focusedScale: Float = 1.1f, // tv-material3 CardDefaults focused scale
): Modifier {
    if (interactionSource == null || !isPoobTheme()) return this
    val focused by interactionSource.collectIsFocusedAsState()
    val progress by animateFloatAsState(if (focused) 1f else 0f, tween(150), label = "poobFrame")
    if (progress == 0f) return this
    return drawWithContent {
        drawContent()
        val s = 1f + (focusedScale - 1f) * progress
        scale(s) {
            drawFrame(shape, SunburstRings, star = true, alpha = progress)
        }
    }
}

/** Draws [rings] (and optionally the gold star) around [shape] at this draw scope's size */
fun DrawScope.drawFrame(
    shape: Shape,
    rings: List<FrameRing>,
    star: Boolean,
    alpha: Float = 1f,
) {
    val outline = shape.createOutline(size, layoutDirection, this)
    val base: RoundRect =
        when (outline) {
            is Outline.Rounded -> outline.roundRect
            is Outline.Rectangle -> RoundRect(outline.rect)
            is Outline.Generic -> RoundRect(outline.path.getBounds())
        }
    val baseRadius = base.topLeftCornerRadius.x
    rings.forEach { ring ->
        val d = ring.offset.toPx()
        val r = (baseRadius + d).coerceAtLeast(0f)
        val rect =
            RoundRect(
                left = base.left - d,
                top = base.top - d,
                right = base.right + d,
                bottom = base.bottom + d,
                cornerRadius = CornerRadius(r, r),
            )
        drawPath(
            Path().apply { addRoundRect(rect) },
            color = ring.color,
            alpha = alpha,
            style = Stroke(width = ring.width.toPx()),
        )
    }
    if (star && rings.isNotEmpty()) {
        // Sit the star on the outer ring's top-right corner
        val d = rings.maxOf { it.offset }.toPx()
        val cornerInset = (baseRadius + d).coerceAtLeast(0f) * 0.29f
        val center = Offset(base.right + d - cornerInset, base.top - d + cornerInset)
        drawStar(center, radius = 11.dp.toPx(), color = PoobBrand.Gold, alpha = alpha)
    }
}

/** Four-point sparkle star like the one on the POOB title card */
fun DrawScope.drawStar(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float = 1f,
) {
    val inner = radius * 0.28f
    val path =
        Path().apply {
            moveTo(center.x, center.y - radius)
            lineTo(center.x + inner, center.y - inner)
            lineTo(center.x + radius, center.y)
            lineTo(center.x + inner, center.y + inner)
            lineTo(center.x, center.y + radius)
            lineTo(center.x - inner, center.y + inner)
            lineTo(center.x - radius, center.y)
            lineTo(center.x - inner, center.y - inner)
            close()
        }
    drawPath(path, color = color, alpha = alpha)
}

/** Default TV Card shape */
val CardShape = RoundedCornerShape(8.dp)

/** Sidebar background on the Poob theme: the pale cream used for focused sidebar items */
@Composable
fun poobSidebarColor(): Color = androidx.tv.material3.MaterialTheme.colorScheme.inverseSurface

/** The Poob orange (the old selected-item highlight), as an opaque color */
@Composable
fun poobOrange(): Color =
    androidx.tv.material3.MaterialTheme.colorScheme.border
        .compositeOver(androidx.tv.material3.MaterialTheme.colorScheme.background)

/**
 * Sidebar item styling on the Poob theme, drawn inside the item's bounds so it doesn't overlap
 * neighbouring items:
 * - selected: a teal pill with a cream outline and a gold star
 * - focused: an orange outline (on top of the teal pill if the item is also selected)
 */
@Composable
fun Modifier.poobNavSelectedFrame(
    selected: Boolean,
    focused: Boolean,
): Modifier {
    if ((!selected && !focused) || !isPoobTheme()) return this
    val orange = poobOrange()
    return drawWithContent {
        val radius = CornerRadius(size.height / 2, size.height / 2)
        fun outline(
            color: Color,
            width: Float,
        ) = drawRoundRect(
            color = color,
            topLeft = Offset(width / 2, width / 2),
            size = androidx.compose.ui.geometry.Size(size.width - width, size.height - width),
            cornerRadius = CornerRadius(radius.x - width / 2, radius.y - width / 2),
            style = Stroke(width = width),
        )
        if (selected) {
            drawRoundRect(color = PoobBrand.Teal, cornerRadius = radius)
            if (!focused) outline(PoobBrand.Cream, 2.dp.toPx())
        }
        drawContent()
        if (focused) outline(orange, 3.dp.toPx())
        if (selected) {
            drawStar(
                center = Offset(size.width - size.height * 0.22f, size.height * 0.22f),
                radius = 6.dp.toPx(),
                color = PoobBrand.Gold,
            )
        }
    }
}
