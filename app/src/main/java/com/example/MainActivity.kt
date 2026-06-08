package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.ThesisSlide
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Apply deep premium academic dark theme directly
            AcademicTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    MainAdvisorScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// --- Dynamic Academic Dark Slate Theme ---
@Composable
fun AcademicTheme(content: @Composable () -> Unit) {
    val darkColors = darkColorScheme(
        primary = Color(0xFF00ADB5), // Cyan Accent
        secondary = Color(0xFF2C3E50), // Navy/Slate
        tertiary = Color(0xFFE74C3C), // Alert Red
        background = Color(0xFF121417), // Deep charcoal black
        surface = Color(0xFF1E2229), // Rich slate dark surface
        onBackground = Color(0xFFEEF2F6),
        onSurface = Color(0xFFDCDFE4)
    )

    MaterialTheme(
        colorScheme = darkColors,
        content = content
    )
}

enum class ActiveTab(val titleAr: String, val icon: ImageVector) {
    SIMULATOR("محاكاة المخاطر", Icons.Default.Place),
    CHAT("المساعد الذكي", Icons.Default.Person),
    WORKBOOK("هيكل الأطروحة", Icons.Default.List),
    GUIDE("الدليل التشريعي", Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAdvisorScreen(
    modifier: Modifier = Modifier,
    viewModel: ThesisViewModel = viewModel(factory = ThesisViewModelFactory(LocalContext.current))
) {
    var currentTab by remember { mutableStateOf(ActiveTab.SIMULATOR) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Top Bar: Kingdom of Morocco Academic Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            shape = RoundedCornerShape(0.0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Details
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "المملكة المغربية • جامعة الدار البيضاء",
                        color = Color(0xFF00ADB5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "مرشد إدارة الكوارث الطبيعية والأطروحات",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        textAlign = TextAlign.End
                    )
                }

                // Moroccan State Badge Logo Mockup
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF059669).copy(alpha = 0.2f), CircleShape)
                        .border(1.5.dp, Color(0xFF34D399), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DRR",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Main Screen Interface Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentTab) {
                ActiveTab.SIMULATOR -> RiskSimulatorView(viewModel = viewModel)
                ActiveTab.CHAT -> AdvisorChatView(viewModel = viewModel)
                ActiveTab.WORKBOOK -> ThesisWorkbookView(viewModel = viewModel)
                ActiveTab.GUIDE -> LegislativeGuideView()
            }
        }

        // Standard Navigation Bottom Bar
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.testTag("app_navigation_bar")
        ) {
            ActiveTab.values().forEach { tab ->
                val selected = currentTab == tab
                NavigationBarItem(
                    selected = selected,
                    onClick = { currentTab = tab },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.titleAr,
                            tint = if (selected) Color(0xFF00ADB5) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    label = {
                        Text(
                            text = tab.titleAr,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color(0xFF00ADB5) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFF00ADB5).copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}

// ============================================
// 1. TACTICAL RISK SIMULATOR VIEW (TAB 1)
// ============================================
@Composable
fun RiskSimulatorView(viewModel: ThesisViewModel) {
    val prefecture by viewModel.selectedPrefecture.collectAsStateWithLifecycle()
    val threatLevel by viewModel.threatLevel.collectAsStateWithLifecycle()
    val resilienceLevel by viewModel.resilienceLevel.collectAsStateWithLifecycle()
    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Region Selector
        item {
            Text(
                text = "1. حدد عمالة الدراسة الترابية للمقاطعة:",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF00ADB5)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Prefecture.values().forEach { pref ->
                    val isSelected = prefecture == pref
                    Button(
                        onClick = { viewModel.selectPrefecture(pref) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_${pref.name.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF00ADB5) else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = pref.labelAr, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Section: Interactive Map Custom Canvas Drawing
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D24)),
                border = BorderStroke(1.dp, Color(0xFF00ADB5).copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Title Indicator inside the Card
                    Text(
                        text = "رصد طوبوغرافي ديناميكي وعلاقة الارتفاع بالتهديد",
                        color = Color(0xFFEEEEEE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    )

                    // Draw the custom topological simulator onto the Canvas
                    DynamicSimulatorCanvas(
                        prefecture = prefecture,
                        threatLevel = threatLevel,
                        resilience = resilienceLevel
                    )
                }
            }
        }

        // Section: Tactical Controls (Sliders)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val slider1Label = when (prefecture) {
                        Prefecture.MOHAMMEDIA -> "منسوب تدفق واد المالح (Oued El Maleh Flow)"
                        Prefecture.AIN_SEBAA -> "شدة العواصف وتدفق الأمطار الحضرية"
                        Prefecture.SIDI_BERNOUSSI -> "شدة واهتزاز موج البحر وتآكل الساحل"
                    }

                    val slider2Label = when (prefecture) {
                        Prefecture.MOHAMMEDIA -> "تجهيزات المدن الإسفنجية (Sponge Zones / ABH)"
                        Prefecture.AIN_SEBAA -> "صرف السيول ومواقف الامتصاص الإشراقية"
                        Prefecture.SIDI_BERNOUSSI -> "مكافحة التعرية وحماية شاطئ البرنوصي"
                    }

                    // Threat Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE74C3C), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = slider1Label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEEEEEE))
                    }
                    Slider(
                        value = threatLevel,
                        onValueChange = { viewModel.setThreatLevel(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFE74C3C),
                            activeTrackColor = Color(0xFFE74C3C).copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("threat_slider")
                    )

                    // Resilience Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = slider2Label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEEEEEE))
                    }
                    Slider(
                        value = resilienceLevel,
                        onValueChange = { viewModel.setResilienceLevel(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF34D399),
                            activeTrackColor = Color(0xFF34D399).copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("resilience_slider")
                    )
                }
            }
        }

        // Section: Realtime Intelligent Legal Warnings (Under 15m rules, ABH, SDAU, FLCN)
        item {
            Text(
                text = "2. الفحوصات التشريعية والمطابقة القانونية المباشرة:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF00ADB5)
            )
        }

        if (activeAlerts.isEmpty()) {
            item {
                Text(
                    text = "جميع العوابط والبيانات في نطاق آمن ومستمر.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        } else {
            items(activeAlerts) { alert ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("alert_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            alert.contains("🔴") -> Color(0xFFE74C3C).copy(alpha = 0.15f)
                            alert.contains("⚠️") -> Color(0xFFF39C12).copy(alpha = 0.15f)
                            alert.contains("🟢") -> Color(0xFF2ECC71).copy(alpha = 0.15f)
                            else -> Color(0xFF2980B9).copy(alpha = 0.15f)
                        }
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = when {
                            alert.contains("🔴") -> Color(0xFFE74C3C).copy(alpha = 0.4f)
                            alert.contains("⚠️") -> Color(0xFFF39C12).copy(alpha = 0.4f)
                            alert.contains("🟢") -> Color(0xFF2ECC71).copy(alpha = 0.4f)
                            else -> Color(0xFF2980B9).copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Text(
                        text = alert,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

// --- Dynamic Simulator Graphic Engine Using Compose Canvas ---
@Composable
fun DynamicSimulatorCanvas(
    prefecture: Prefecture,
    threatLevel: Float,
    resilience: Float
) {
    // Pulse animation for hazard signals
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .testTag("simulator_canvas")
    ) {
        val width = size.width
        val height = size.height

        when (prefecture) {
            Prefecture.MOHAMMEDIA -> {
                // Drawing Mohammedia: Valley with Oued El Maleh river
                // 1. Draw topography (hills sloping to a central low-lying valley)
                val topoPath = Path().apply {
                    moveTo(0f, height * 0.4f)
                    quadraticTo(width * 0.25f, height * 0.95f, width * 0.5f, height * 0.95f)
                    quadraticTo(width * 0.75f, height * 0.95f, width * 1f, height * 0.35f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = topoPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2F3640), Color(0xFF1E272E))
                    )
                )

                // 2. Draw Oued El Maleh water body (height controlled by threatLevel and mitigated by sponge absorption)
                val baseWaterHeight = height * 0.95f
                val rise = (threatLevel * (height * 0.45f)) - (resilience * (height * 0.2f))
                val waveHeight = baseWaterHeight - rise.coerceAtLeast(0f)

                val waterPath = Path().apply {
                    moveTo(width * 0.25f, baseWaterHeight)
                    quadraticTo(width * 0.5f, waveHeight, width * 0.75f, baseWaterHeight)
                    lineTo(width * 0.75f, height)
                    lineTo(width * 0.25f, height)
                    close()
                }
                drawPath(
                    path = waterPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3498DB), Color(0xFF2980B9).copy(alpha = 0.8f))
                    )
                )

                // 3. Draw Legal Threshold Reference (15m height limit)
                val thresholdY = height * 0.72f
                drawLine(
                    color = Color(0xFFE74C3C).copy(alpha = 0.6f),
                    start = Offset(0f, thresholdY),
                    end = Offset(width, thresholdY),
                    strokeWidth = 3f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                )

                // Draw Text on Canvas
                // Wait: writing text on Canvas requires NativePaint, so we will draw graphical markers.
                // Draw 15M marker flag
                drawCircle(color = Color(0xFFE74C3C), radius = 6f, center = Offset(30f, thresholdY))

                // 4. Draw housing in the risk zone (under 15m line)
                val houseX = width * 0.35f
                val houseY = height * 0.82f
                val hSize = 25f

                // House body
                drawRect(
                    color = if (waveHeight < houseY) Color(0xFFE74C3C).copy(alpha = pulseAlpha) else Color(0xFFF1C40F),
                    topLeft = Offset(houseX, houseY),
                    size = Size(hSize, hSize)
                )
                // Roof
                val roofPath = Path().apply {
                    moveTo(houseX - 5f, houseY)
                    lineTo(houseX + hSize / 2f, houseY - 12f)
                    lineTo(houseX + hSize + 5f, houseY)
                    close()
                }
                drawPath(path = roofPath, color = Color(0xFFD35400))

                // 5. Draw Sponge retaining reservoirs if resilience exists
                if (resilience > 0.3f) {
                    drawRoundRect(
                        color = Color(0xFF2ECC71).copy(alpha = 0.8f),
                        topLeft = Offset(width * 0.15f, height * 0.65f),
                        size = Size(50f, 16f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = Color(0xFF2ECC71).copy(alpha = 0.8f),
                        topLeft = Offset(width * 0.75f, height * 0.65f),
                        size = Size(50f, 16f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                }
            }
            Prefecture.AIN_SEBAA -> {
                // Drawing Ain Sebaâ Highly Urbanized block with coastal proximity
                // Slanted city ground sloping down to the coastal front
                val groundPath = Path().apply {
                    moveTo(0f, height * 0.75f)
                    lineTo(width * 0.8f, height * 0.85f)
                    lineTo(width, height * 0.95f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = groundPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF57606F), Color(0xFF2F3542))
                    )
                )

                // 15m line
                val thresholdY = height * 0.80f
                drawLine(
                    color = Color(0xFFE74C3C).copy(alpha = 0.6f),
                    start = Offset(0f, thresholdY),
                    end = Offset(width, thresholdY),
                    strokeWidth = 3f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                )

                // Rainstorms lines drawn based on Threat level
                if (threatLevel > 0.1f) {
                    val numDrops = (threatLevel * 20).toInt()
                    for (i in 0..numDrops) {
                        val rx = (i * (width / numDrops))
                        val ry = (i * 17) % (height * 0.5f)
                        drawLine(
                            color = Color(0xFF74B9FF).copy(alpha = 0.6f),
                            start = Offset(rx, ry),
                            end = Offset(rx - 8f, ry + 25f),
                            strokeWidth = 2f
                        )
                    }
                }

                // Cumulative urban water logging (reduces with sponge elements)
                val loggedHeight = (threatLevel * 30f) - (resilience * 25f)
                if (loggedHeight > 2f) {
                    drawRect(
                        color = Color(0xFF41A2E6).copy(alpha = 0.6f),
                        topLeft = Offset(0f, height - loggedHeight),
                        size = Size(width, loggedHeight)
                    )
                }

                // Draw high-density skyscrapers / logistic sheds in Ain Sebaa
                for (xOffset in listOf(0.1f, 0.3f, 0.5f, 0.7f)) {
                    val wx = width * xOffset
                    val wy = height * (0.75f + (xOffset * 0.05f)) - 50f
                    // Shed/Building
                    drawRect(
                        color = if (loggedHeight > 5f && xOffset > 0.5f) Color(0xFFE74C3C).copy(alpha = pulseAlpha) else Color(0xFF95A5A6),
                        topLeft = Offset(wx, wy),
                        size = Size(35f, 50f)
                    )
                    // Windows
                    drawRect(color = Color(0xFFF1C40F), topLeft = Offset(wx + 5f, wy + 10f), size = Size(8f, 10f))
                    drawRect(color = Color(0xFFF1C40F), topLeft = Offset(wx + 20f, wy + 10f), size = Size(8f, 10f))
                }

                // If green sponge elements active, draw green circles representing absorption parks
                if (resilience > 0.3f) {
                    for (xp in listOf(0.25f, 0.62f)) {
                        drawCircle(
                            color = Color(0xFF2ECC71).copy(alpha = 0.8f),
                            radius = 12f * (resilience * 1.5f),
                            center = Offset(width * xp, height * 0.73f)
                        )
                    }
                }
            }
            Prefecture.SIDI_BERNOUSSI -> {
                // Sidi Bernoussi: Sandy Coastal Line with High Erosion threat
                // 1. Draw Sandy beach sloping down to the sea on the right
                val beachPath = Path().apply {
                    moveTo(0f, height * 0.55f)
                    quadraticTo(width * 0.4f, height * 0.65f, width * 0.7f, height * 0.85f)
                    lineTo(width, height * 0.95f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = beachPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE9C46A), Color(0xFFE76F51))
                    )
                )

                // 2. Coastal Erosion Wave action (Threat increases, waves eat the sandbox)
                val seaBaseX = width * 0.6f - (threatLevel * (width * 0.25f)) + (resilience * (width * 0.2f))
                val seaPath = Path().apply {
                    moveTo(seaBaseX, height * 0.7f)
                    quadraticTo(width * 0.8f, height * 0.75f, width, height * 0.65f)
                    lineTo(width, height)
                    lineTo(seaBaseX, height)
                    close()
                }
                drawPath(
                    path = seaPath,
                    color = Color(0xFF2A9D8F).copy(alpha = 0.85f)
                )

                // Wave peak circles
                val crestX = seaBaseX + 10f
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = 10f + (threatLevel * 10f),
                    center = Offset(crestX, height * 0.78f)
                )

                // Prohibited border marker below 15m (which is close to the seaside)
                val thresholdY = height * 0.68f
                drawLine(
                    color = Color(0xFFE74C3C).copy(alpha = 0.6f),
                    start = Offset(0f, thresholdY),
                    end = Offset(width, thresholdY),
                    strokeWidth = 3f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                )

                // Danger symbol if sea reaches far
                if (seaBaseX < width * 0.45f) {
                    drawRect(
                        color = Color(0xFFC0392B).copy(alpha = pulseAlpha),
                        topLeft = Offset(width * 0.15f, height * 0.38f),
                        size = Size(40f, 24f)
                    )
                }

                // Eco-buffer structures (dunes / breakwaters)
                if (resilience > 0.4f) {
                    for (x in listOf(0.55f, 0.65f)) {
                        drawRoundRect(
                            color = Color(0xFF7F8C8D),
                            topLeft = Offset(width * x, height * 0.71f),
                            size = Size(14f, 25f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }
    }
}


// ============================================
// 2. INTELLIGENT AI ACADEMIC ADVISOR (TAB 2)
// ============================================
@Composable
fun AdvisorChatView(viewModel: ThesisViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var studentInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Recommended Academic Prompt Tags
    val defaultPrompts = listOf(
        "أثر سنداي 1 على مناطق تحت 15 متر",
        "مسؤولية وكالة الحوض المائي في واد المالح",
        "أكاديمياً: حظر البناء المادة 12.90",
        "تصميم إسفنجي Sponge Cities للحي المحمدي",
        "تمويل FLCN لمشاريع الساحل"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Chat History Frame
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatHistory) { msg ->
                val isAdvisor = msg.sender == Sender.ADVISOR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAdvisor) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag(if (isAdvisor) "advisor_msg" else "student_msg"),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isAdvisor) 2.dp else 16.dp,
                            bottomEnd = if (isAdvisor) 16.dp else 2.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAdvisor) Color(0xFF1E2229) else Color(0xFF00ADB5).copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isAdvisor) Color(0xFF1E2229) else Color(0xFF00ADB5)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header label helper
                            Text(
                                text = if (isAdvisor) "👨‍🏫 المستشار الأكاديمي (الخبير القانوني):" else "🎓 الباحث طالب الماستر:",
                                fontWeight = FontWeight.Bold,
                                color = if (isAdvisor) Color(0xFF00ADB5) else Color(0xFF34D399),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Text Body
                            Text(
                                text = msg.text,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.End
                            )

                            // Quick Action Tools inside Advisor's answers (Save to Database Outline list)
                            if (isAdvisor && msg.text.length > 50) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Save Button
                                    Button(
                                        onClick = {
                                            viewModel.saveSlideToThesis(
                                                title = "مستخرج من الاستشارة الأكاديمية",
                                                content = msg.text.take(300) + "...",
                                                category = "عام"
                                            )
                                            Toast.makeText(context, "تم حفظ المادة في هيكل الأطروحة بنجاح!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("حفظ بالأطروحة", fontSize = 10.sp, color = Color.White)
                                    }

                                    // Copy Button
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(msg.text))
                                            Toast.makeText(context, "تم الحفظ في حافظة النصوص!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("نسخ النص", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Spinner while loading response from Gemini developer API
            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2229))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp).testTag("chat_loading_spinner"),
                                    color = Color(0xFF00ADB5),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("المستشار الأكاديمي يراجع القوانين الوطنية والمخططات...", fontSize = 11.sp, color = Color(0xFFB2B2B2))
                            }
                        }
                    }
                }
            }
        }

        // Auto Scroll to Bottom on message receipt
        LaunchedEffect(chatHistory.size, isChatLoading) {
            if (chatHistory.isNotEmpty()) {
                listState.animateScrollToItem(chatHistory.size - 1)
            }
        }

        // Quick Tag Suggestion Panel
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(defaultPrompts) { prompt ->
                Card(
                    modifier = Modifier
                        .clickable { studentInput = prompt }
                        .testTag("tag_chip"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50)),
                    border = BorderStroke(1.dp, Color(0xFF00ADB5).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        color = Color(0xFFEEEEEE),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Input Messaging Area
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Send Action Button
            IconButton(
                onClick = {
                    if (studentInput.isNotBlank()) {
                        viewModel.sendMessage(studentInput)
                        studentInput = ""
                    }
                },
                enabled = !isChatLoading,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isChatLoading) Color.Gray else Color(0xFF00ADB5),
                        CircleShape
                    )
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "ارسال",
                    tint = Color.White
                )
            }

            // Input Field
            TextField(
                value = studentInput,
                onValueChange = { studentInput = it },
                placeholder = { Text("اطرح سؤالك لدراسة الحالة (مثال: قانون التعمير سيدي البرنوصي)...", fontSize = 12.sp, textAlign = TextAlign.End) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("chat_input_field"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (studentInput.isNotBlank()) {
                        viewModel.sendMessage(studentInput)
                        studentInput = ""
                    }
                })
            )

            // Clear Button
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "تصفية", tint = Color.Red)
            }
        }
    }
}


// ============================================
// 3. THESIS WORKBOOK SLIDES PERSISTENCE (TAB 3)
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThesisWorkbookView(viewModel: ThesisViewModel) {
    val savedSlides by viewModel.savedSlides.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedSlideForEdit by remember { mutableStateOf<ThesisSlide?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("عام") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toolbar notes control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isAddDialogOpen = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ADB5)),
                modifier = Modifier.testTag("add_slide_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة مستند/شريحة", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { viewModel.preInstallStandardSlides() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF34D399)),
                modifier = Modifier.testTag("preinstall_slides_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF34D399))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تنزيل الأطروحة النموذجية", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "مخطط الأطروحة والشرائح المحفوظة (${savedSlides.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFFEEEEEE)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (savedSlides.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد أي شرائح أو مستخلصات محفوظة حاليا.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اضغط على زر (تنزيل الأطروحة النموذجية) لبدء العمل تلقائياً.",
                        color = Color.Gray.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedSlides) { slide ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSlideForEdit = slide }
                            .testTag("slide_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Tag
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF00ADB5).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(text = slide.category, color = Color(0xFF00ADB5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                // Delete slide Action
                                IconButton(
                                    onClick = {
                                        viewModel.deleteSlide(slide.id)
                                        Toast.makeText(context, "تم حذف المادة بنجاح", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp).testTag("delete_slide_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Slide Title
                            Text(
                                text = slide.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Slide Content Preview
                            Text(
                                text = slide.content,
                                color = Color(0xFFB2B2B2),
                                fontSize = 12.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { selectedSlideForEdit = slide },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp).testTag("edit_slide_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تعديل كامل", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog 1: Add New Slide
    if (isAddDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAddDialogOpen = false },
            title = { Text("إدراج محتوى جديد بالأطروحة", fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("عنوان الشريحة / الفقرة") },
                        modifier = Modifier.fillMaxWidth().testTag("add_slide_title_input")
                    )

                    TextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("المحتوى المنهجي / التوصيات") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("add_slide_content_input")
                    )

                    TextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("التصنيف (سنداي، مياه، تعمير)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_slide_category_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.saveSlideToThesis(newTitle, newContent, newCategory)
                            newTitle = ""
                            newContent = ""
                            newCategory = "عام"
                            isAddDialogOpen = false
                            Toast.makeText(context, "تم حفظ الشريحة بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ADB5)),
                    modifier = Modifier.testTag("confirm_add_slide_btn")
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddDialogOpen = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Dialog 2: Edit Existing Slide
    selectedSlideForEdit?.let { slide ->
        var editTitle by remember { mutableStateOf(slide.title) }
        var editContent by remember { mutableStateOf(slide.content) }
        var editCategory by remember { mutableStateOf(slide.category) }

        AlertDialog(
            onDismissRequest = { selectedSlideForEdit = null },
            title = { Text("تحيين وتدقيق نص الشريحة", fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("عنوان الشريحة") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_slide_title_input")
                    )

                    TextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("النص الأكاديمي") },
                        modifier = Modifier.fillMaxWidth().height(140.dp).testTag("edit_slide_content_input")
                    )

                    TextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        label = { Text("التصنيف") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_slide_category_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSlide(slide.copy(title = editTitle, content = editContent, category = editCategory))
                        selectedSlideForEdit = null
                        Toast.makeText(context, "تم تحديث الشريحة الأكاديمية بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ADB5)),
                    modifier = Modifier.testTag("confirm_edit_slide_btn")
                ) {
                    Text("حفظ التغييرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSlideForEdit = null }) {
                    Text("إغلاق")
                }
            }
        )
    }
}


// ============================================
// 4. MOROCCAN LEGISLATIVE REFERENCE GUIDE (TAB 4)
// ============================================
@Composable
fun LegislativeGuideView() {
    val legalDesk = remember {
        listOf(
            LegalItem(
                subject = "إطار سنداي الدولي 2015-2030 (Sendai Framework)",
                details = """
                    • الأولوية 1 (فهم مخاطر الكوارث): تلزم الدول بإنشاء خرائط الارتفاعات الرقمية والمخاطر الهيدرولوجية. يجب ربط خرائط فيضانات واد المالح لتحديد المناطق الأكثر عرضة للضرر.
                    • الأولوية 4 (تعزيز التأهب لمواجهة الكوارث): تنمية وتفعيل نهج "البناء بشكل أفضل - Build Back Better". توظف تقنيات "المدن الإسفنجية" لإلقاء مياه الأمطار في التربة عوض طمرها بالاسفلت والخرسانة المسلحة.
                """.trimIndent(),
                color = Color(0xFF00ADB5)
            ),
            LegalItem(
                subject = "قانون الماء 36.15 والكورات المائية (Loi de l'Eau)",
                details = """
                    • المادة 115 والسيولة: تناط بـ "وكالة الحوض المائي لأم الربيع" (ABH-OER) سلطة تحديد الحدود الطبيعية للفيضان، وتصميم المنشآت الوقائية، ودراسة تدفق واد المالح لضمان عدم غمر مساكن عمالة المحمدية.
                    • تدبير الملك العام المائي: يمنع قطعيًا في مجرى الماء أي نشاط من شأنه إعاقة جريانه الطبيعي لمنع المخاطر المركبة والسيول العنيفة.
                """.trimIndent(),
                color = Color(0xFF3498DB)
            ),
            LegalItem(
                subject = "قانون التعمير 12.90 والتهيئة المعمارية (Loi de l'Urbanisme)",
                details = """
                    • المخطط التوجيهي SDAU: يعتبر المخطط التوجيهي للتهيئة الحضرية مرجع حاسم في تحديد الأراضي غير القابلة للبناء. يمنع الترخيص بإنشاء مرافق سكنية أو واجهات لوجستية في الخطوط الكنتورية الطبوغرافية الضحلة (تحت 15 مترًا عن سطح البحر).
                    • ضوابط البناء الصارمة: فرض أحزمة الأمان الإسفنجية (Eco-buffers) على كامل الشريط الساحلي لعين السبع وسيدي البرنوصي للوقاية من التعرية وتآكل الشاطئ.
                """.trimIndent(),
                color = Color(0xFFF1C40F)
            ),
            LegalItem(
                subject = "الاستراتيجية الوطنية لتدبير المحاطر 2020-2030 (National Strategy)",
                details = """
                    • خطط التنزيل الجهوي لجهة الدار البيضاء-سطات: تهدف لتأمين حواضر الدار البيضاء ومقاطعاتها من فيضان مجاري المياه، من خلال تبني مصفوفة الصيانة الإسفنجية وزيادة مرونة الأحياء أمام التغيرات المناخية.
                    • الانتقال نحو المقاربة الاستباقية: استبدال الحلول الاسمنتية الكلاسيكية ببدائل "مبنية على الطبيعة" ومشاريع Sponge City لتجميع وارتشاح فائض السيول.
                """.trimIndent(),
                color = Color(0xFF9B59B6)
            ),
            LegalItem(
                subject = "صندوق مكافحة آثار الكوارث الطبيعية FLCN",
                details = """
                    • الآلية الوطنية المرنة للتمويل: صندوق FLCN يمثل الرافعة الداعمة لمشاريع الحماية الوقائية قبل حدوث الفيضانات. يمول بنسب تفضيلية تصل إلى 60% لإقامة شبكات الصرف المرنة والمدن الإسفنجية في عمالات الدار البيضاء.
                    • الاستدامة التشغيلية: الدعم مشروط بإدراج دراسات طبوغرافية لتقييم الارتفاع وحماية المواقع الحضرية الضعيفة وتفعيل الاستعداد الأمثل.
                """.trimIndent(),
                color = Color(0xFF2ECC71)
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "المرجعيات القانونية المنهجية والمخططات الوطنية للبحث:",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF00ADB5)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "استعن بهذه النصوص الرسمية لربط تحليلاتك بخرائط الارتفاع وواد المالح في أروقة مناقشة الماستر.",
                fontSize = 11.sp,
                color = Color(0xFFB2B2B2)
            )
        }

        items(legalDesk) { item ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("guide_item_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, item.color.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header label
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(item.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.subject,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = item.details,
                        color = Color(0xFFDCDFE4),
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

data class LegalItem(
    val subject: String,
    val details: String,
    val color: Color
)
