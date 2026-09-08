package com.wallstreet.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.wallstreet.domain.model.EquityCurveData
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.EquityPoint
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.ui.theme.LocalCurrencySymbol
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun EquityCurveChart(
    equityCurveData: EquityCurveData,
    selectedPeriod: TimePeriod,
    modifier: Modifier = Modifier
) {
    fun reduceEquityPoints(
        points: List<EquityPoint>,
        maxPoints: Int = 10
    ): List<EquityPoint> {
        if (points.size <= maxPoints) return points
        val step = points.size.toFloat() / maxPoints
        val sampled = (0 until maxPoints).map { i ->
            points[(i * step).toInt().coerceAtMost(points.lastIndex)]
        }
        // Add last point only if not already included
        return if (sampled.last().date == points.last().date) {
            sampled
        } else {
            sampled + points.last()
        }
    }

    val reducedPoints = remember(equityCurveData, selectedPeriod) {
        reduceEquityPoints(equityCurveData.points, 9)
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(equityCurveData) {
        equityCurveData.let { data ->
            modelProducer.runTransaction {
                lineSeries {
                    series(reducedPoints.map { it.cumulativePnL })
                }
            }
        }
    }


    val isPositive = equityCurveData.totalPnL >= 0
    val lineColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val guidelineColor = MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Equity Curve",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                val totalPnL = equityCurveData.totalPnL

                Box(
                    modifier = Modifier
                        .background(
                            color = lineColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = totalPnL.formatPnl(LocalCurrencySymbol.current),
                        color = lineColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (equityCurveData.points.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .padding(bottom = 8.dp)
                ) {
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(
                                lineProvider = LineCartesianLayer.LineProvider.series(
                                    LineCartesianLayer.rememberLine(
                                        fill = LineCartesianLayer.LineFill.single(
                                            fill(lineColor)
                                        ),
                                        areaFill = LineCartesianLayer.AreaFill.single(
                                            fill(
                                                ShaderProvider.verticalGradient(
                                                    arrayOf(
                                                        lineColor.copy(alpha = 0.4f),
                                                        lineColor.copy(alpha = 0.1f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                        ),
                                        stroke = LineCartesianLayer.LineStroke.continuous(
                                            thickness = 1.dp
                                        ),
                                        pointConnector = remember { LineCartesianLayer.PointConnector.cubic(curvature = 0.4f) },
                                        pointProvider = null
                                    )
                                ),
                            ),
                            startAxis = VerticalAxis.rememberStart(
                                label = rememberTextComponent(
                                    color = labelColor,
                                    textSize = 10.sp,
                                    padding = Insets(4.0f)
                                ),
                                tick = null,
                                guideline = rememberLineComponent(
                                    fill = fill(guidelineColor),
                                    thickness = 1.dp,
                                    shape = Shape.Rectangle
                                ),
                            ),
                            bottomAxis = HorizontalAxis.rememberBottom(
                                label = rememberTextComponent(
                                    color = labelColor,
                                    textSize = 10.sp,
                                    padding = Insets(topDp = 2f, bottomDp = 2f)
                                ),
                                tick = null,
                                guideline = null,
                                valueFormatter = CartesianValueFormatter { _, value, _ ->
                                    val index = value.toInt()
                                    if (index in reducedPoints.indices) {
                                        val date = reducedPoints[index].date
                                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = date }
                                        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)

                                        // Show "MMM d" for 1st of month or first/last point, otherwise just "d"
                                        val format = if (day == 1 || index == reducedPoints.lastIndex) {
                                            "dMMM "
                                        } else {
                                            "d"
                                        }
                                        SimpleDateFormat(format, Locale.getDefault()).format(Date(date))
                                    } else value.toInt().toString()
                                },
                            )
                        ),
                        modelProducer = modelProducer,
                        zoomState = rememberVicoZoomState(zoomEnabled = false ),
                        modifier = Modifier.padding(bottom = 0.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
