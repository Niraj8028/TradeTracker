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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.wallstreet.domain.model.EquityCurveData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.DynamicShader


@Composable
fun EquityCurveChart(
    equityCurveData: EquityCurveData,
    modifier: Modifier = Modifier
    ) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(equityCurveData) {
        equityCurveData?.let { data ->
            modelProducer.runTransaction {
                lineSeries {
                    series(data.points.map { it.cumulativePnL })
                }
            }
        }
    }
    val isPositive = (equityCurveData?.totalPnL ?: 0.0) >= 0
    val lineColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
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
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                val totalPnL = equityCurveData?.totalPnL ?: 0.0
                val isPositive = totalPnL >= 0

                Box(
                    modifier = Modifier
                        .background(
                            color = lineColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isPositive)
                            "+$${String.format("%.0f", totalPnL)}"
                        else
                            "-$${String.format("%.0f", -totalPnL)}",
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
                        .height(250.dp)
                ) {
                    // See next section for chart implementation
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(
                                lineProvider = LineCartesianLayer.LineProvider.series(
                                    rememberLine(
                                        fill = remember {
                                            LineCartesianLayer.LineFill.single(
                                                fill(lineColor)
                                            )
                                        },
                                        areaFill = remember {
                                            LineCartesianLayer.AreaFill.single(
                                                fill = Fill(
                                                    shader = DynamicShader.verticalGradient(
                                                        colors = intArrayOf(
                                                            lineColor.copy(alpha = 0.4f).toArgb(),
                                                            lineColor.copy(alpha = 0.1f).toArgb(),
                                                            Color.Transparent.toArgb()
                                                        )
                                                    )
                                                )
                                            )
                                        },
                                        thickness = 1.dp,
                                        pointProvider = null
                                    )
                                )
                            ),
                            startAxis = rememberStartAxis(
                                label = rememberTextComponent(
                                    color = Color(0xFF9CA3AF),
                                    textSize = 10.sp,
                                    padding = Dimensions(4.0F)
                                ),
                                tick = null,
                                guideline = rememberLineComponent(
                                    color = Color(0xFF374151),
                                    thickness = 1.dp,
                                    shape = com.patrykandpatrick.vico.core.common.shape.Shape.Rectangle
                                )
                            ),
                            bottomAxis = rememberBottomAxis(
                                label = rememberTextComponent(
                                    color = Color(0xFF9CA3AF),
                                    textSize = 10.sp
                                ),
                                tick = null,
                                guideline = null,
                                valueFormatter = { value, _, _ ->
                                    val index = value.toInt()
                                    if (index in equityCurveData.points.indices) {
                                        val date = equityCurveData.points[index].date
                                        SimpleDateFormat(
                                            "d",
                                            Locale.getDefault()
                                        ).format(Date(date))
                                    } else ""
                                }
                            )
                        ),
                        modelProducer = modelProducer
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
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}