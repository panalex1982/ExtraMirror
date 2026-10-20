package com.bue.extramirror.ui.mirror

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextGeometricTransform
import com.bue.extramirror.data.SpeedometerMode
import com.bue.extramirror.trip.MotionState
import com.bue.extramirror.util.MeasureUnit
import com.bue.extramirror.util.Units
import kotlin.math.cos
import kotlin.math.sin

@Immutable
data class SpeedometerReading(
    val speedKmh: Float = 0f,
    val distanceMeters: Float = 0f,
    val altitudeMeters: Double? = null,
    val standingMillis: Long = 0L,
    val movingMillis: Long = 0L,
    val motion: MotionState = MotionState.ENGINE_OFF,
    val engineOn: Boolean = false,
)

/** Localized unit strings, resolved by the caller so this file stays resource-free. */
@Immutable
data class SpeedometerLabels(
    val kmh: String = "Km/h",
    val mph: String = "mph",
    val km: String = "km",
    val miles: String = "mi.",
    val meters: String = "m",
    val feet: String = "ft",
)

/**
 * Compose port of the legacy `AnalogSpeedMeterView`. All geometry is relative to the size of
 * the (square) canvas, using the same proportions as the original Canvas code.
 */
@Composable
fun Speedometer(
    reading: SpeedometerReading,
    unit: MeasureUnit,
    mode: SpeedometerMode,
    contentDescription: String,
    modifier: Modifier = Modifier,
    labels: SpeedometerLabels = SpeedometerLabels(),
) {
    val textMeasurer = rememberTextMeasurer(cacheSize = 64)
    Canvas(
        modifier
            .aspectRatio(1f)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val gauge = Gauge(size.minDimension, reading, unit, labels, textMeasurer)
        when (mode) {
            SpeedometerMode.ANALOG -> drawAnalog(gauge)
            SpeedometerMode.DIGITAL -> drawDigital(gauge)
        }
    }
}

private class Gauge(
    val canvasSize: Float,
    val reading: SpeedometerReading,
    val unit: MeasureUnit,
    val labels: SpeedometerLabels,
    val textMeasurer: TextMeasurer,
) {
    val radius = canvasSize / 2 - canvasSize / 30
    val center = Offset(canvasSize / 2, canvasSize / 2)
    val metric = unit == MeasureUnit.METRIC
    val moving = reading.motion == MotionState.MOVING

    /** Red while the engine is off, green while the trip is running (legacy behaviour). */
    val speedColor = if (reading.engineOn) Color.Green else Color.Red

    val speedText = Units.displaySpeed(reading.speedKmh, unit).toString()
    val speedUnit = if (metric) labels.kmh else labels.mph
    val distanceText = Units.formatDistance(reading.distanceMeters, unit)
    val distanceUnit = if (metric) labels.km else labels.miles
    val altitudeText = "${reading.altitudeMeters?.let { Units.formatAltitude(it, unit) } ?: "—"} " +
        if (metric) labels.meters else labels.feet
    val standingText = Units.formatDuration(reading.standingMillis)
    val movingText = Units.formatDuration(reading.movingMillis)

    fun polar(distance: Float, angleDegrees: Float): Offset {
        val radians = Math.toRadians(angleDegrees.toDouble())
        return Offset(
            center.x + distance * cos(radians).toFloat(),
            center.y + distance * sin(radians).toFloat(),
        )
    }
}

private enum class Anchor { Start, Center, End }

private val ArcBlue = Color(0x500066CC)
private val ScaleMajor = Color(0xFFFFB266)
private val ScaleMinor = Color(0xFFFFCC99)
private val DistanceBox = Color(0x50FFB266)
private val DistanceText = Color(0xFF994C00)
private val AltitudeGray = Color(0xFFCCCCCC)
private val TrafficLightBody = Color(0xFF9E9E9E)
private val TrafficLightGreen = Color(0xFF43A047)

private fun DrawScope.drawAnalog(g: Gauge) {
    val c = g.canvasSize
    val r = g.radius
    val inset = c / 30

    // Outer arc and inner circle.
    drawArc(
        color = ArcBlue,
        startAngle = 150f,
        sweepAngle = 260f,
        useCenter = false,
        topLeft = Offset(inset, inset),
        size = Size(c - 2 * inset, c - 2 * inset),
        style = Stroke(width = r / 10),
    )
    drawCircle(ArcBlue, radius = r / 2, center = g.center, style = Stroke(width = r / 150))

    drawLabel(g, g.speedUnit, g.polar(r - 2 * r / 5, -90f), r / 15, ScaleMajor)
    drawScale(g)

    // Needle: one degree per km/h, 0 at 150°, 260 km/h at the end of the arc.
    val needleAngle = 150f + g.reading.speedKmh.coerceIn(0f, 260f)
    drawLine(
        color = Color.Red,
        start = g.polar(r / 2, needleAngle),
        end = g.polar(r, needleAngle),
        strokeWidth = r / 50,
        cap = StrokeCap.Round,
    )

    // Digital read-out, altitude and trip timers inside the inner circle.
    val cx = g.center.x
    val cy = g.center.y
    drawLabel(g, g.speedText, Offset(cx, cy - r / 10), r / 2, g.speedColor)
    drawLabel(g, g.altitudeText, Offset(cx, cy + r / 20), r / 8, AltitudeGray)
    drawTrafficLight(Offset(cx - r / 3.3f, cy + r / 9), height = r * 0.3f, moving = g.moving)
    drawLabel(g, g.standingText, Offset(cx + r / 14, cy + r / 5), r / 8, Color.Red)
    drawLabel(g, g.movingText, Offset(cx + r / 14, cy + r / 3), r / 8, Color.Green)

    // Odometer box.
    val left = cx - r / 1.5f
    val right = cx + r / 3
    val top = cy + r / 2 + r / 20
    val bottom = cy + r - r / 20
    drawRoundRect(
        color = DistanceBox,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        cornerRadius = CornerRadius(r / 20),
    )
    drawLabel(g, g.distanceText, Offset((left + right) / 2, (top + bottom) / 1.9f), r / 4, DistanceText)
}

/** Ticks every 5 units, numbers every 20 km/h or 10 mph, placed on the km/h angle scale. */
private fun DrawScope.drawScale(g: Gauge) {
    val r = g.radius
    val max = if (g.metric) 260 else 160
    val labelEvery = if (g.metric) 20 else 10
    // Where the legacy code nudged the numbers so they don't collide with the arc.
    val (first, second, third) = if (g.metric) Triple(40, 140, 220) else Triple(20, 70, 110)
    val majorStroke = r / 45
    val minorStroke = r / 110

    for (value in 0..max step 5) {
        val angle = 150f + if (g.metric) value.toFloat() else Units.milesToKm(value.toFloat())
        if (value % 10 == 0) {
            drawLine(ScaleMajor, g.polar(r + r / 20, angle), g.polar(r - r / 20, angle), majorStroke)
        } else {
            drawLine(ScaleMinor, g.polar(r + r / 20, angle), g.polar(r - r / 30, angle), minorStroke)
        }
        if (value % labelEvery == 0) {
            val textSize = if (value % (2 * labelEvery) == 0) r / 6 else r / 9
            val (distance, nudge) = when {
                value < first -> r - 2 * r / 10 to -2f
                value <= second -> r - 2 * r / 10 to 0f
                value < third -> r - 2 * r / 8 to 2f
                else -> r - 2 * r / 10 to 4f
            }
            drawLabel(g, value.toString(), g.polar(distance, angle + nudge), textSize, ScaleMajor, skewX = -0.25f)
        }
    }
}

private fun DrawScope.drawDigital(g: Gauge) {
    val c = g.canvasSize
    val r = g.radius
    val mono = FontFamily.Monospace
    val gap = c / 50

    // Big speed, bottom right, with its unit next to it.
    val speedRight = c - c / 7
    val speedSize = if (g.speedText.length < 3) r * 1.5f else r
    drawLabel(
        g, g.speedText, Offset(speedRight, c - c / 10), speedSize, g.speedColor, Anchor.End, mono,
        maxWidth = speedRight - gap,
    )
    drawLabel(
        g, g.speedUnit, Offset(speedRight + gap / 2, c - c / 10), r / 8, g.speedColor, Anchor.Start,
        maxWidth = c - speedRight - gap,
    )

    // Distance, top right. Kept clear of the altitude / timer column on the left, which the
    // legacy layout overlapped once the distance had four or more characters.
    val distanceRight = c - c / 10
    drawLabel(
        g, g.distanceText, Offset(distanceRight, c / 5), r / 2, g.speedColor, Anchor.End, mono,
        maxWidth = distanceRight - c * 0.48f,
    )
    drawLabel(g, g.distanceUnit, Offset(distanceRight, c / 5), r / 10, g.speedColor, Anchor.Start)

    // Altitude and trip timers, top left.
    drawLabel(g, g.altitudeText, Offset(c / 5, c / 15), r / 6, AltitudeGray, fontFamily = mono, maxWidth = c * 0.36f)
    drawTrafficLight(Offset(c / 2.5f, c / 10), height = r * 0.3f, moving = g.moving)
    drawLabel(g, g.standingText, Offset(c / 5, c / 7), r / 6, Color.Red, fontFamily = mono, maxWidth = c * 0.36f)
    drawLabel(g, g.movingText, Offset(c / 5, c / 4.5f), r / 6, Color.Green, fontFamily = mono, maxWidth = c * 0.36f)
}

/** Drawn instead of the legacy 15x39 px bitmaps so it stays sharp at any size. */
private fun DrawScope.drawTrafficLight(topLeft: Offset, height: Float, moving: Boolean) {
    val width = height * 15f / 39f
    val bodyHeight = height * 0.78f
    drawRoundRect(
        color = TrafficLightBody,
        topLeft = topLeft,
        size = Size(width, bodyHeight),
        cornerRadius = CornerRadius(width * 0.2f),
    )
    val poleWidth = width * 0.22f
    drawRect(
        color = TrafficLightBody,
        topLeft = Offset(topLeft.x + (width - poleWidth) / 2, topLeft.y + bodyHeight),
        size = Size(poleWidth, height - bodyHeight),
    )
    val lampSpacing = bodyHeight / 3
    for (lamp in 0..2) {
        val color = when {
            lamp == 0 && !moving -> Color.Red
            lamp == 2 && moving -> TrafficLightGreen
            else -> Color.White
        }
        drawCircle(
            color = color,
            radius = width * 0.32f,
            center = Offset(topLeft.x + width / 2, topLeft.y + lampSpacing * (lamp + 0.5f)),
        )
    }
}

/**
 * Draws single-line text the way `android.graphics.Canvas.drawText` did: [position] is on the
 * baseline, horizontally anchored by [anchor]. [sizePx] is in pixels so the text scales with
 * the gauge rather than with the system font size; text wider than [maxWidth] is shrunk to fit.
 */
private fun DrawScope.drawLabel(
    g: Gauge,
    text: String,
    position: Offset,
    sizePx: Float,
    color: Color,
    anchor: Anchor = Anchor.Center,
    fontFamily: FontFamily? = null,
    skewX: Float = 0f,
    maxWidth: Float = Float.POSITIVE_INFINITY,
) {
    fun measure(size: Float) = g.textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = size.toSp(),
            fontFamily = fontFamily,
            textGeometricTransform = TextGeometricTransform(skewX = skewX),
        ),
        softWrap = false,
        maxLines = 1,
    )
    var layout = measure(sizePx)
    if (layout.size.width > maxWidth && maxWidth > 0f) {
        layout = measure(sizePx * maxWidth / layout.size.width)
    }
    val left = when (anchor) {
        Anchor.Start -> position.x
        Anchor.Center -> position.x - layout.size.width / 2f
        Anchor.End -> position.x - layout.size.width
    }
    drawText(layout, topLeft = Offset(left, position.y - layout.firstBaseline))
}
