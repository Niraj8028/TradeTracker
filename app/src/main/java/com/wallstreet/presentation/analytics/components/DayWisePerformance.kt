package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.SuccessGreen
import kotlin.math.abs
import kotlin.math.roundToInt
import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.core.util.formatAmount

// Flat gray bar for zero/no-trade days — matches the "Sat" dash in the screenshot
private val NoTradeColor = Color(0xFF3A4555)

@Composable
fun DayWisePerformance(dayPerformance: DayPerformance) {
    if (dayPerformance.days.isEmpty()) return

    val bestDay = dayPerformance.days.maxByOrNull { it.pnl }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(dayPerformance) {
        modelProducer.runTransaction {
            columnSeries {
                // KEY FIX: always use abs(pnl) so bars NEVER go below the X axis.
                // A tiny sentinel (0.5f) keeps zero-trade days visible as a flat stub.
                series(
                    dayPerformance.days.map { day ->
                        if (day.pnl == 0.0) 0.5f else abs(day.pnl).toFloat()
                    }
                )
            }
        }
    }

    // Use a custom ColumnProvider to color each bar individually based on its PnL
    val columnProvider = remember(dayPerformance) {
        object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore
            ): LineComponent {
                val day = dayPerformance.days.getOrNull(entry.x.roundToInt())
                val color = when {
                    day == null || day.pnl == 0.0 -> NoTradeColor
                    day.pnl > 0 -> SuccessGreen
                    else -> DangerRed
                }
                return LineComponent(
                    fill = fill(color),
                    thicknessDp = 38f,
                    shape = CorneredShape.rounded(allPercent = 20),
                )
            }

            override fun getWidestSeriesColumn(
                seriesIndex: Int,
                extraStore: ExtraStore
            ): LineComponent {
                return LineComponent(
                    fill = fill(NoTradeColor),
                    thicknessDp = 38f,
                    shape = CorneredShape.rounded(allPercent = 20),
                )
            }
        }
    }

    // ── Card — same surface token as LongShortCard ───────────────────────────
    // colorScheme.background = DarkSurface (#161B22) in dark / LightBackground in light
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // ── Header ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Day-wise Performance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Your most profitable days",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Best day badge — BadgeStockBg / BadgeStockText from your theme
                if (bestDay != null && bestDay.pnl > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E3A8A))       // BadgeStockBg
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Best: ${bestDay.day.name.take(3).uppercase()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,           // BadgeStockText
                        )
                    }
                }
            }

            // ── PnL labels above bars ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                dayPerformance.days.forEach { day ->
                    val (labelText, labelColor) = when {
                        day.pnl == 0.0 -> "—" to MaterialTheme.colorScheme.onSurfaceVariant
                        day.pnl > 0 -> "+\$${formatAmount(day.pnl)}" to SuccessGreen
                        else -> "\$${formatAmount(day.pnl)}" to DangerRed
                    }
                    Text(
                        text = labelText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = labelColor,
                    )
                }
            }

            // ── Chart ────────────────────────────────────────────────────────
            // No startAxis: we show labels manually above.
            // No guideline/tick/line on bottom axis — clean look matching screenshot.
            CartesianChartHost(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider = columnProvider,
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = rememberTextComponent(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textSize = 12.sp,
                            padding = Insets(topDp = 8f),
                        ),
                        line = null,
                        tick = null,
                        guideline = null,
                        valueFormatter = CartesianValueFormatter { _, x, _ ->
                            dayPerformance.days
                                .getOrNull(x.roundToInt())
                                ?.day
                                ?.name
                                ?.take(3)
                                ?.replaceFirstChar { it.uppercase() }
                                ?: ""
                        },
                    ),
                ),
                modelProducer = modelProducer,
            )
        }
    }
}
