package com.github.damontecres.wholphin.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.github.damontecres.wholphin.R
import com.github.damontecres.wholphin.ui.util.LocalClock
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Displays the [LocalClock] in the upper right corner of the parent [androidx.compose.foundation.layout.Box]
 *
 * @param decorated on the Poob theme, show the clock as the starburst badge instead of plain text
 */
@Composable
fun BoxScope.TimeDisplay(
    modifier: Modifier = Modifier,
    decorated: Boolean = true,
) {
    val timeString by LocalClock.current.timeString
    if (decorated && isPoobTheme()) {
        PoobClockBadge(
            time = timeString,
            modifier =
                modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 36.dp),
        )
    } else {
        Text(
            text = timeString,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            modifier =
                modifier
                    .align(Alignment.TopEnd)
                    .padding(vertical = 16.dp, horizontal = 24.dp),
        )
    }
}

val LuckiestGuy = FontFamily(Font(R.font.luckiest_guy))

private object ClockBadgeColors {
    val Plate = Color(0xFF138B84)
    val Orange = Color(0xFFF58220)
    val Star = Color(0xFFFFD84D)
    val Ink = Color(0xFF111111)
    val Shadow = Color.Black
}

/** Removes a trailing AM/PM so the badge shows just the numbers */
private fun stripAmPm(time: String): String = time.replace(Regex("""\s*[AaPp]\.?\s?[Mm]\.?$"""), "").trim()

/**
 * The Poob clock: a slightly tilted teal plate with an orange rim and black drop shadow, two yellow
 * starbursts on opposite corners, and chunky black numerals outlined in orange.
 */
@Composable
fun PoobClockBadge(
    time: String,
    modifier: Modifier = Modifier,
) {
    val outlineWidth = with(LocalDensity.current) { 5.dp.toPx() }
    val baseStyle = TextStyle(fontFamily = LuckiestGuy, fontSize = 30.sp)
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .rotate(-3f)
                .drawWithContent {
                    drawPlate()
                    drawContent()
                    drawStars()
                }.padding(start = 26.dp, end = 26.dp, top = 12.dp, bottom = 6.dp),
    ) {
        val text = stripAmPm(time)
        // Orange outline underneath, black fill on top
        Text(
            text = text,
            style = baseStyle.copy(drawStyle = Stroke(width = outlineWidth, join = StrokeJoin.Round)),
            color = ClockBadgeColors.Orange,
        )
        Text(text = text, style = baseStyle.copy(drawStyle = Fill), color = ClockBadgeColors.Ink)
    }
}

/** Slightly irregular four-sided plate, a little taller on the right like a tilted sign */
private fun DrawScope.platePath(): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        moveTo(0f, h * 0.10f)
        lineTo(w, 0f)
        lineTo(w * 0.995f, h)
        lineTo(w * 0.015f, h * 0.94f)
        close()
    }
}

private fun DrawScope.drawPlate() {
    val path = platePath()
    val corners = PathEffect.cornerPathEffect(12.dp.toPx())
    val shadow = 6.dp.toPx()
    translate(left = -shadow, top = shadow) {
        drawPath(path, ClockBadgeColors.Shadow, style = Fill)
        drawPath(path, ClockBadgeColors.Shadow, style = Stroke(width = 4.dp.toPx(), pathEffect = corners))
    }
    drawPath(path, ClockBadgeColors.Plate, style = Fill)
    drawPath(
        path,
        ClockBadgeColors.Orange,
        style = Stroke(width = 4.dp.toPx(), join = StrokeJoin.Round, pathEffect = corners),
    )
}

private fun DrawScope.drawStars() {
    val w = size.width
    val h = size.height
    drawStarburst(Offset(w * 0.06f, h * 0.12f), outer = 22.dp.toPx(), rotation = -0.3f)
    drawStarburst(Offset(w * 0.95f, h * 0.92f), outer = 19.dp.toPx(), rotation = 0.2f)
}

/** Eight-point starburst with a black drop shadow */
private fun DrawScope.drawStarburst(
    center: Offset,
    outer: Float,
    rotation: Float,
) {
    val inner = outer * 0.28f
    val path =
        Path().apply {
            for (i in 0 until 16) {
                val angle = rotation + i * PI.toFloat() / 8
                val r = if (i % 2 == 0) outer else inner
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
    val shadow = 4.dp.toPx()
    translate(left = -shadow, top = shadow) { drawPath(path, ClockBadgeColors.Shadow) }
    drawPath(path, ClockBadgeColors.Star)
}
