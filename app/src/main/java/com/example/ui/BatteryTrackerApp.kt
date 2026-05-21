package com.example.ui

import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.BatteryRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

// Design colors for Professional Polish theme
val CosmicBg = Color(0xFFFDFBFF)         // Light visual canvas
val PanelBg = Color(0xFFFFFFFF)          // Crisp white primary cards
val PanelBorder = Color(0xFFE1E2E6)      // Modern soft borders
val NeonGreen = Color(0xFF2E7D32)        // Professional status green
val NeonBlue = Color(0xFF315DA8)         // Theme primary blue brand highlight & charging glow
val NeonAmber = Color(0xFFE65100)        // Alert warning orange
val NeonRed = Color(0xFFC62828)          // Critical status red
val TechGlow = Color(0xFF315DA8)         // Theme primary blue brand highlight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatteryTrackerApp(
    viewModel: BatteryViewModel = viewModel()
) {
    val liveState by viewModel.liveState.collectAsState()
    val history by viewModel.historyRecords.collectAsState()

    var showAdjusterDialog by remember { mutableStateOf(false) }

    // Dynamic coloring based on battery status and state
    val statusColor = when {
        liveState.status == BatteryManager.BATTERY_STATUS_CHARGING -> NeonBlue
        liveState.level <= 15 -> NeonRed
        liveState.level <= 40 -> NeonAmber
        else -> NeonGreen
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBg),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmicBg)
                .padding(innerPadding)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Outer restraint for tablet/expanded viewing
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Header Core Unit
                    HeaderCoreUnit(liveState = liveState, statusColor = statusColor)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Liquid Plasma Fluid Core indicator
                    LiquidPlasmaFluidCore(liveState = liveState, accentColor = statusColor)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Live Diagnostics (2x2 Grid)
                    LiveDiagnosticsPanelGrid(liveState = liveState)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Interactive Simulation Command Center
                    SimulationCommandCenter(
                        onSelectScenario = { scenario -> viewModel.simulateScenario(scenario) },
                        onOpenManualInjections = { showAdjusterDialog = true }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bézier Trend Graph Analytics
                    TrendAnalyzerChart(
                        history = history,
                        accentColor = statusColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Activity Logs stream database
                    ActivityLogsStreamUnit(
                        history = history,
                        onDeleteLog = { id -> viewModel.deleteRecord(id) },
                        onClearLogs = { viewModel.clearHistory() }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Adjuster / Custom injection dialog
            if (showAdjusterDialog) {
                ManualInjectionDialog(
                    onDismiss = { showAdjusterDialog = false },
                    onConfirm = { level, temp, volt, isCharging ->
                        viewModel.addManualMockRecord(level, temp, volt, isCharging)
                        showAdjusterDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderCoreUnit(liveState: LiveBatteryState, statusColor: Color) {
    // Elegant pulsing animation for active communication protocol status
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PanelBorder, RoundedCornerShape(12.dp))
            .background(PanelBg, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "BATTERY PROTOCOL",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF1B1B1F),
                letterSpacing = 1.sp
            )
            Text(
                text = "Diagnostics active in system core",
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF44474E)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .background(statusColor.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (liveState.status == BatteryManager.BATTERY_STATUS_CHARGING) "CHARGING" else "BATTERY",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
fun LiquidPlasmaFluidCore(liveState: LiveBatteryState, accentColor: Color) {
    // Custom wavy liquid math coordinates animation
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    val targetLevelFraction = liveState.level / 100f
    val animatedLevelFraction by animateFloatAsState(
        targetValue = targetLevelFraction,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "animateProgress"
    )

    val isCharging = liveState.status == BatteryManager.BATTERY_STATUS_CHARGING

    Box(
        modifier = Modifier
            .size(220.dp)
            .border(2.dp, PanelBorder, CircleShape)
            .padding(8.dp)
            .border(1.dp, PanelBorder.copy(alpha = 0.6f), CircleShape)
            .background(Color(0xFFD7E2FF).copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .testTag("circular_battery_gauge")
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw glowing background container ring
            drawArc(
                color = PanelBorder.copy(alpha = 0.6f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. Draw outer capacity trace indicator Ring
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.3f),
                        accentColor,
                        accentColor.copy(alpha = 0.9f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = animatedLevelFraction * 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Wavy interactive solid liquid fill representing current percentage
            val clipPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(4.dp.toPx(), 4.dp.toPx(), width - 4.dp.toPx(), height - 4.dp.toPx()))
            }

            clipPath(clipPath) {
                val wavePath = Path()
                // Wave properties proportional to current level and charging status
                val amplitude = if (isCharging) 12.dp.toPx() else 8.dp.toPx()
                val frequency = 0.04f
                val fillY = height - (animatedLevelFraction * height)

                wavePath.moveTo(0f, height)
                wavePath.lineTo(0f, fillY)

                for (x in 0..width.toInt()) {
                    val y = fillY + amplitude * sin(x * frequency + phase)
                    wavePath.lineTo(x.toFloat(), y)
                }

                wavePath.lineTo(width, height)
                wavePath.close()

                // Draw the plasma liquid layer
                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.75f),
                            accentColor.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )

                // Optional: Draw secondary slightly offset wave for realistic transparent aesthetic depth
                val secondWavePath = Path()
                secondWavePath.moveTo(0f, height)
                secondWavePath.lineTo(0f, fillY)

                for (x in 0..width.toInt()) {
                    val y = fillY + (amplitude * 0.7f) * sin(x * frequency * 0.8f + phase + Math.PI.toFloat() / 2f)
                    secondWavePath.lineTo(x.toFloat(), y)
                }
                secondWavePath.lineTo(width, height)
                secondWavePath.close()

                drawPath(
                    path = secondWavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.3f),
                            accentColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
            }
        }

        // 4. Center numeric values & stat labels
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val formattedLevel = remember(liveState.level) { "${liveState.level}%" }
            Text(
                text = formattedLevel,
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF001B3D)
            )

            val speedText = when {
                isCharging && liveState.plugged == BatteryManager.BATTERY_PLUGGED_AC -> "FAST AC GLOW"
                isCharging -> "USB PORT LINK"
                liveState.level <= 15 -> "CRITICAL RESERVE"
                liveState.level <= 40 -> "SYSTEM LOW"
                else -> "NOMINAL DISCHARGE"
            }

            Text(
                text = speedText,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Estimated status description
            Text(
                text = if (isCharging) "Power Source Connected" else "Operating on cell battery",
                fontSize = 9.sp,
                color = Color(0xFF44474E)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveDiagnosticsPanelGrid(liveState: LiveBatteryState) {
    // Maps technical flags to highly explanatory user-facing text and aesthetic warnings
    val sourceName = when (liveState.plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC CHARGER"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB PORT"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "WIRELESS CELL"
        else -> "NO INPUT"
    }

    val healthName = when (liveState.health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "HEALTHY (GOOD)"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEATING"
        BatteryManager.BATTERY_HEALTH_DEAD -> "REPLACE BATTERY"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OVER-VOLTAGE"
        BatteryManager.BATTERY_HEALTH_COLD -> "COLD FAILURE"
        else -> "UNSPECIFIED"
    }
    
    val healthColor = if (liveState.health == BatteryManager.BATTERY_HEALTH_GOOD) NeonGreen else NeonRed

    val tempFahrenheit = (liveState.temperature * 9 / 5) + 32
    val formatTempC = "%.1f°C".format(liveState.temperature)
    val formatTempF = "%.1f°F".format(tempFahrenheit)

    // Heat badge text
    val heatLevelText = when {
        liveState.temperature >= 40f -> "CRITICAL HEAT"
        liveState.temperature >= 35f -> "WARM STATE"
        else -> "COOL TEMP"
    }
    val heatBadgeColor = when {
        liveState.temperature >= 40f -> NeonRed
        liveState.temperature >= 35f -> NeonAmber
        else -> TechGlow
    }

    val voltageVolts = liveState.voltage / 1000f
    val formatVolt = "%.2f V".format(voltageVolts)

    // Structured 2x2 grid card using modern FlowRow in compose to avoid nesting sizes
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val cardWidthModifier = Modifier
            .weight(1f)
            .border(1.dp, PanelBorder, RoundedCornerShape(12.dp))

        // Card 1: Connection Potential Source
        DiagnosticStatItem(
            modifier = cardWidthModifier,
            title = "POWER INPUT",
            mainText = sourceName,
            subText = if (liveState.plugged > 0) "Current inflow active" else "Isolator cells engaged",
            accentColor = if (liveState.plugged > 0) NeonBlue else Color(0xFF74777F)
        )

        // Card 2: Core Health Rating
        DiagnosticStatItem(
            modifier = cardWidthModifier,
            title = "CELL HEALTH",
            mainText = healthName,
            subText = if (liveState.health == BatteryManager.BATTERY_HEALTH_GOOD) "Internal cells are stable" else "Check power supplies",
            accentColor = healthColor
        )

        // Card 3: Realtime Temperature Conversion
        DiagnosticStatItem(
            modifier = cardWidthModifier,
            title = "THERMAL INDEX",
            mainText = "$formatTempC / $formatTempF",
            subText = heatLevelText,
            accentColor = heatBadgeColor
        )

        // Card 4: Potential Electrical Voltage
        DiagnosticStatItem(
            modifier = cardWidthModifier,
            title = "VOLTAGE LEVEL",
            mainText = formatVolt,
            subText = "${liveState.voltage} mV raw potential",
            accentColor = TechGlow
        )
    }
}

@Composable
fun DiagnosticStatItem(
    modifier: Modifier = Modifier,
    title: String,
    mainText: String,
    subText: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PanelBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                title,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF44474E),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                mainText,
                fontSize = 18.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B1B1F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    subText,
                    fontSize = 11.sp,
                    color = Color(0xFF74777F),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun SimulationCommandCenter(
    onSelectScenario: (String) -> Unit,
    onOpenManualInjections: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PanelBorder, RoundedCornerShape(12.dp))
            .background(PanelBg, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SIMULATION CONTROL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF44474E),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Inject battery profiles into history DB",
                    fontSize = 11.sp,
                    color = Color(0xFF74777F)
                )
            }

            Button(
                onClick = onOpenManualInjections,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechGlow.copy(alpha = 0.12f),
                    contentColor = TechGlow
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechGlow.copy(alpha = 0.3f)),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("add_mock_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add custom data record",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Inflow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Row of pre-configured simulation datasets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScenarioChip(
                label = "Battery Drain",
                testTag = "simulation_drain_chip",
                color = NeonRed,
                onClick = { onSelectScenario("drain") },
                modifier = Modifier.weight(1f)
            )
            ScenarioChip(
                label = "Fast Charging",
                testTag = "simulation_charge_chip",
                color = NeonBlue,
                onClick = { onSelectScenario("charge") },
                modifier = Modifier.weight(1f)
            )
            ScenarioChip(
                label = "Daily Cycle",
                testTag = "simulation_cycle_chip",
                color = NeonGreen,
                onClick = { onSelectScenario("cycle") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ScenarioChip(
    label: String,
    testTag: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .border(1.dp, PanelBorder, RoundedCornerShape(8.dp))
            .background(Color(0xFFF3F3F7), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1F),
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun TrendAnalyzerChart(
    history: List<BatteryRecord>,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PanelBorder, RoundedCornerShape(12.dp))
            .background(PanelBg, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "TREND ANALYZER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF44474E),
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Historical trace data flow",
                fontSize = 11.sp,
                color = Color(0xFF74777F)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.dp, PanelBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F3).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "No historic metrics",
                        tint = Color(0xFF74777F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "History database empty.",
                        fontSize = 12.sp,
                        color = Color(0xFF1B1B1F),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Run simulation scenarios or keep app opened.",
                        fontSize = 10.sp,
                        color = Color(0xFF74777F),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Precise custom vector Bézier drawing inside a Canvas
            val cleanLogs = remember(history) {
                // Ensure chronological order for plotting
                history.sortedBy { it.timestamp }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(vertical = 8.dp)
            ) {
                val canvasW = size.width
                val canvasH = size.height
                val dataPointsCount = cleanLogs.size

                // Drawing guidelines grid (100%, 75%, 50%, 25%, 0%)
                val horizontalLinesCount = 4
                for (i in 0..horizontalLinesCount) {
                    val y = (canvasH / horizontalLinesCount) * i
                    drawLine(
                        color = PanelBorder.copy(alpha = 0.6f),
                        start = Offset(0f, y),
                        end = Offset(canvasW, y),
                        strokeWidth = 1f
                    )
                }

                if (dataPointsCount > 1) {
                    val path = Path()
                    val backgroundPath = Path()

                    val minTime = cleanLogs.first().timestamp
                    val maxTime = cleanLogs.last().timestamp
                    val timeSpan = maxTime - minTime

                    fun getCoordinates(index: Int, record: BatteryRecord): Offset {
                        // Spread coordinates over timeline dynamically or via standard stepped indexes
                        val x = if (timeSpan > 0) {
                            ((record.timestamp - minTime).toFloat() / timeSpan.toFloat()) * canvasW
                        } else {
                            (index.toFloat() / (dataPointsCount - 1).toFloat()) * canvasW
                        }
                        val y = canvasH - (record.level / 100f * canvasH)
                        return Offset(x, y)
                    }

                    // Compute whole set of Coordinates
                    val points = cleanLogs.mapIndexed { index, record -> getCoordinates(index, record) }

                    // Plot Bézier curve
                    path.moveTo(points.first().x, points.first().y)
                    backgroundPath.moveTo(points.first().x, canvasH)
                    backgroundPath.lineTo(points.first().x, points.first().y)

                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val controlPointX = (prev.x + curr.x) / 2f
                        path.cubicTo(
                            controlPointX, prev.y,
                            controlPointX, curr.y,
                            curr.x, curr.y
                        )
                        backgroundPath.cubicTo(
                            controlPointX, prev.y,
                            controlPointX, curr.y,
                            curr.x, curr.y
                        )
                    }

                    backgroundPath.lineTo(points.last().x, canvasH)
                    backgroundPath.close()

                    // Fill under curve Area with dynamic glowing neon brush gradient
                    drawPath(
                        path = backgroundPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.25f),
                                accentColor.copy(alpha = 0.02f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw Stroke line
                    drawPath(
                        path = path,
                        color = accentColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw individual data pulse points
                    points.forEach { point ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = accentColor,
                            radius = 2.dp.toPx(),
                            center = point
                        )
                    }
                } else if (dataPointsCount == 1) {
                    val point = Offset(canvasW / 2f, canvasH - (cleanLogs.first().level / 100f * canvasH))
                    drawCircle(
                        color = accentColor,
                        radius = 8.dp.toPx(),
                        center = point
                    )
                }
            }

            // Legend labels for graph timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatFullDate(cleanLogs.first().timestamp),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF74777F)
                )
                Text(
                    text = "History Trace",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF44474E),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatFullDate(cleanLogs.last().timestamp),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF74777F)
                )
            }
        }
    }
}

@Composable
fun ActivityLogsStreamUnit(
    history: List<BatteryRecord>,
    onDeleteLog: (Int) -> Unit,
    onClearLogs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PanelBorder, RoundedCornerShape(12.dp))
            .background(PanelBg, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACTIVITY STREAM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF44474E),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Chronological telemetry list",
                    fontSize = 11.sp,
                    color = Color(0xFF74777F)
                )
            }

            if (history.isNotEmpty()) {
                TextButton(
                    onClick = onClearLogs,
                    colors = ButtonDefaults.textButtonColors(contentColor = NeonRed),
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Wipe history database logs",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Wipe Log", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
            Text(
                text = "Stream empty. Power changes register automatically.",
                fontSize = 11.sp,
                color = Color(0xFF74777F),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        } else {
            // Draw limited chronlog list view without rendering 1000 items (capped at 50 for safety)
            val showLogs = history.take(55)

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                showLogs.forEach { r ->
                    StreamLogItem(
                        record = r,
                        onDelete = { onDeleteLog(r.id) }
                    )
                }

                if (history.size > 55) {
                    Text(
                        text = "Viewing past 55 records out of ${history.size}",
                        fontSize = 10.sp,
                        color = Color(0xFF74777F),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StreamLogItem(
    record: BatteryRecord,
    onDelete: () -> Unit
) {
    val isCharging = record.status == BatteryManager.BATTERY_STATUS_CHARGING
    val accentColor = if (isCharging) NeonBlue else {
        when {
            record.level <= 15 -> NeonRed
            record.level <= 40 -> NeonAmber
            else -> NeonGreen
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PanelBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .background(Color(0xFFFBFBFE), RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Bullet icon with mini battery level trace
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${record.level}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCharging) "Charging event" else "Discharge log",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1F)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isCharging) "AC/USB" else "CELL",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Temp: %.1f°C | Volt: %.2fV | Time: %s".format(
                        record.temperature,
                        record.voltage / 1000f,
                        formatTime(record.timestamp)
                    ),
                    fontSize = 10.sp,
                    color = Color(0xFF74777F)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete specific log entry",
            tint = Color(0xFF74777F),
            modifier = Modifier
                .size(24.dp)
                .clickable { onDelete() }
                .padding(4.dp)
        )
    }
}

@Composable
fun ManualInjectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (level: Int, temp: Float, volt: Int, isCharging: Boolean) -> Unit
) {
    var rawLevel by remember { mutableStateOf(50f) }
    var rawTemp by remember { mutableStateOf(28.5f) }
    var rawVoltString by remember { mutableStateOf("3960") }
    var rawIsCharging by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .border(2.dp, PanelBorder, RoundedCornerShape(16.dp))
                .background(PanelBg, RoundedCornerShape(16.dp)),
            color = PanelBg,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "MANUAL LOG INJECTOR",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1F),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Construct customized historic state points",
                    fontSize = 11.sp,
                    color = Color(0xFF74777F)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Selector: Level Slider
                Text(
                    text = "Capacity: ${rawLevel.toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF44474E)
                )
                Slider(
                    value = rawLevel,
                    onValueChange = { rawLevel = it },
                    valueRange = 1f..100f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = TechGlow,
                        inactiveTrackColor = PanelBorder,
                        thumbColor = TechGlow
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selector: Temp Slider
                Text(
                    text = "Temperature: %.1f°C".format(rawTemp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF44474E)
                )
                Slider(
                    value = rawTemp,
                    onValueChange = { rawTemp = it },
                    valueRange = 10f..65f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = TechGlow,
                        inactiveTrackColor = PanelBorder,
                        thumbColor = TechGlow
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selector: Voltage Potential Field
                Text(
                    text = "Electrical Potential (mV)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF44474E)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = rawVoltString,
                    onValueChange = { input -> if (input.all { it.isDigit() }) rawVoltString = input },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechGlow,
                        unfocusedBorderColor = PanelBorder,
                        focusedTextColor = Color(0xFF1B1B1F),
                        unfocusedTextColor = Color(0xFF1B1B1F)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Selector: Active Inline Charge State State Indicator switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Charge source active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44474E)
                    )
                    Switch(
                        checked = rawIsCharging,
                        onCheckedChange = { rawIsCharging = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonBlue,
                            checkedTrackColor = NeonBlue.copy(alpha = 0.4f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = PanelBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF74777F))
                    ) {
                        Text("CANCEL")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            val volt = rawVoltString.toIntOrNull() ?: 4000
                            onConfirm(rawLevel.toInt(), rawTemp, volt, rawIsCharging)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechGlow)
                    ) {
                        Text("INJECT", color = Color.White)
                    }
                }
            }
        }
    }
}

// Helpers
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatFullDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
