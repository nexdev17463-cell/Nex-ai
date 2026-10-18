package com.nexdev.nexai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

private val Night = Color(0xFF070B18)
private val Glass = Color(0xCC111C35)
private val Cyan = Color(0xFF63E8FF)
private val Violet = Color(0xFF9472FF)
private val Soft = Color(0xFFF2F6FF)
private val Muted = Color(0xFF91A4C6)

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent { NexApp(applicationContext) }
    }
}

@Composable
private fun NexApp(appContext: android.content.Context) {
    var splash by remember { mutableStateOf(true) }
    var tab by remember { mutableStateOf("Home") }
    var thinking by remember { mutableStateOf(false) }
    var backgroundOn by remember { mutableStateOf(false) }
    var effects by remember { mutableStateOf(true) }
    var glow by remember { mutableStateOf(true) }
    var prompt by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready when you are. Connect a secure AI backend for real answers.") }
    LaunchedEffect(Unit) { delay(1500); splash = false }
    MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, secondary = Violet, background = Night, surface = Glass, onPrimary = Night, onBackground = Soft, onSurface = Soft)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF142447), Night, Color(0xFF03050C)))).statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp)) {
            if (splash) {
                SplashScreen()
            } else {
                Column(Modifier.fillMaxSize()) {
                    Header()
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when (tab) {
                            "Home" -> HomeScreen(thinking, status, effects, glow) {
                                tab = "Chat"; thinking = true
                                status = "Task preview started. Real actions need a configured AI backend."
                            }
                            "Chat" -> ChatScreen(prompt, { prompt = it }, status, thinking) {
                                if (prompt.isNotBlank()) {
                                    status = "Demo only: request captured in the interface. Configure a secure backend for AI replies."
                                    thinking = true; prompt = ""
                                }
                            }
                            "Studio" -> StudioScreen()
                            else -> SettingsScreen(backgroundOn, { enabled ->
                                backgroundOn = enabled
                                val intent = Intent(appContext, NexBackgroundService::class.java)
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= 26) appContext.startForegroundService(intent) else appContext.startService(intent)
                                } else appContext.stopService(intent)
                            }, effects, { effects = it }, glow, { glow = it })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    FloatingNav(tab) { tab = it }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(17.dp)).background(Brush.linearGradient(listOf(Cyan, Violet))), contentAlignment = Alignment.Center) {
            Text("N", color = Night, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("NEX AI", color = Soft, fontSize = 23.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("PERSONAL AI COMPANION", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp)
        }
        Surface(color = Color(0x3325DFA0), shape = RoundedCornerShape(50)) {
            Text("● READY", Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Color(0xFF68F3B0), fontSize = 10.sp)
        }
    }
}

@Composable
private fun SplashScreen() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        NexOrb(170.dp, true)
        Spacer(Modifier.height(28.dp))
        Text("NEX AI", color = Soft, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
        Text("A MORE PERSONAL DIGITAL SPACE", color = Cyan, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(28.dp))
        LinearProgressIndicator(Modifier.fillMaxWidth(.48f).height(3.dp).clip(CircleShape), color = Cyan, trackColor = Glass)
        Spacer(Modifier.height(10.dp))
        Text("INITIALIZING INTERFACE", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
    }
}

@Composable
private fun HomeScreen(thinking: Boolean, status: String, effects: Boolean, glow: Boolean, onStart: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard {
                Text("YOUR SPACE. YOUR AI.", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(10.dp))
                Text("Hey there.", color = Soft, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                Text("What would you like to get done?", color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { NexOrb(160.dp, effects && glow) }
                Spacer(Modifier.height(10.dp))
                Text(if (thinking) "◉ PROCESSING PREVIEW" else "● SYSTEM STANDBY", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(status, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(17.dp)) { Text("✦  START A TASK", fontWeight = FontWeight.Bold) }
            }
        }
        item { Text("QUICK LAUNCH", color = Cyan, fontSize = 11.sp, letterSpacing = 2.sp) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { QuickCard("✧", "Ask AI", Modifier.weight(1f)); QuickCard("◉", "Voice", Modifier.weight(1f)); QuickCard("⌘", "Focus", Modifier.weight(1f)) } }
        item { GlassCard { Text("DESIGNED TO FEEL ALIVE", color = Soft, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(6.dp)); Text("Ambient glow • animated orb • floating navigation • responsive status states", color = Muted, fontSize = 12.sp) } }
    }
}

@Composable
private fun ChatScreen(prompt: String, onPrompt: (String) -> Unit, status: String, thinking: Boolean, onSend: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text("CONVERSATION", color = Cyan, fontSize = 11.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        GlassCard(Modifier.weight(1f)) {
            NexOrb(92.dp, thinking)
            Spacer(Modifier.height(12.dp))
            Text(if (thinking) "Thinking animation" else "NEX is ready", color = Soft, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(status, color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Text("Demo interface only — a secure AI backend is needed for real responses.", color = Color(0xFFFFD58B), fontSize = 11.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Glass).border(1.dp, Cyan.copy(alpha = .25f), RoundedCornerShape(22.dp)).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(value = prompt, onValueChange = onPrompt, modifier = Modifier.weight(1f), placeholder = { Text("Ask NEX anything…", color = Muted) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Soft, unfocusedTextColor = Soft, cursorColor = Cyan), maxLines = 3)
            IconButton(onClick = onSend) { Text("➤", color = Cyan, fontSize = 22.sp) }
        }
    }
}

@Composable
private fun StudioScreen() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("NEX STUDIO", color = Cyan, fontSize = 11.sp, letterSpacing = 2.sp)
        StudioCard("3D AMBIENCE", "Animated orb with layered neon gradients and pulse motion.")
        StudioCard("THINKING STATES", "Idle, processing and response states are visually distinct.")
        StudioCard("FLOATING GLASS", "Translucent panels, soft borders, rounded controls and glow.")
        StudioCard("TASK VISIBILITY", "Show progress and status before and after real actions are connected.")
    }
}

@Composable
private fun SettingsScreen(backgroundOn: Boolean, onBackground: (Boolean) -> Unit, effects: Boolean, onEffects: (Boolean) -> Unit, glow: Boolean, onGlow: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("CONTROL CENTRE", color = Cyan, fontSize = 11.sp, letterSpacing = 2.sp)
        GlassCard {
            Text("Background assistant", color = Soft, fontWeight = FontWeight.SemiBold)
            Text("Keeps a visible Android notification while running. Android may stop background work; this is not an unrestricted always-on agent.", color = Muted, fontSize = 12.sp)
            Switch(checked = backgroundOn, onCheckedChange = onBackground)
        }
        SettingRow("Floating animations", "Animate the orb and interface", effects, onEffects)
        SettingRow("Neon glow", "Cyan and violet accents", glow, onGlow)
        GlassCard { Text("AI CONNECTION", color = Soft, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(5.dp)); Text("No API key is stored in this app. Add a trusted backend before enabling Gemini or other cloud AI.", color = Color(0xFFFFD58B), fontSize = 12.sp) }
        GlassCard { Text("PRIVACY & CONTROL", color = Soft, fontWeight = FontWeight.SemiBold); Text("NEX does not silently control other apps. Cross-app actions need Android-approved permissions and clear confirmation.", color = Muted, fontSize = 12.sp) }
    }
}

@Composable
private fun SettingRow(title: String, detail: String, checked: Boolean, change: (Boolean) -> Unit) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title, color = Soft, fontWeight = FontWeight.SemiBold); Text(detail, color = Muted, fontSize = 12.sp) }
            Switch(checked = checked, onCheckedChange = change)
        }
    }
}

@Composable
private fun QuickCard(icon: String, title: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Glass).border(1.dp, Cyan.copy(alpha = .18f), RoundedCornerShape(18.dp)).padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, color = Cyan, fontSize = 25.sp); Text(title, color = Soft, fontSize = 11.sp)
    }
}

@Composable
private fun StudioCard(title: String, detail: String) {
    GlassCard { Text(title, color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); Text(detail, color = Muted, fontSize = 13.sp) }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp))
        .background(Brush.linearGradient(listOf(Color(0xD91B2B4B), Color(0xB80C1429))))
        .border(1.dp, Brush.linearGradient(listOf(Cyan.copy(alpha = .30f), Violet.copy(alpha = .23f))), RoundedCornerShape(25.dp))
        .padding(17.dp), content = content)
}

@Composable
private fun NexOrb(size: androidx.compose.ui.unit.Dp, animated: Boolean) {
    val t = rememberInfiniteTransition(label = "orb")
    val pulse by t.animateFloat(.94f, 1.04f, infiniteRepeatable(tween(1450, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val rotation by t.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "rotation")
    Box(Modifier.size(size).scale(if (animated) pulse else 1f), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().padding(2.dp).border(1.dp, Cyan.copy(alpha = .28f), CircleShape))
        Box(Modifier.fillMaxSize(.82f).background(Brush.sweepGradient(listOf(Cyan, Violet, Color(0xFF1D7CFF), Cyan)), CircleShape).padding(5.dp).background(Night, CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxSize(.8f).rotate(rotation).border(2.dp, Brush.sweepGradient(listOf(Cyan, Violet, Color.Transparent, Cyan)), CircleShape))
            Text("N", color = Soft, fontSize = (size.value * .32f).sp, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
private fun FloatingNav(current: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Brush.horizontalGradient(listOf(Color(0xEE14213D), Color(0xEE10182D)))).border(1.dp, Cyan.copy(alpha = .24f), RoundedCornerShape(28.dp)).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("Home" to "⌂", "Chat" to "◌", "Studio" to "✧", "Settings" to "⚙").forEach { (name, icon) ->
            Column(Modifier.weight(1f).clip(RoundedCornerShape(21.dp)).background(if (current == name) Cyan.copy(alpha = .13f) else Color.Transparent).clickable { onSelect(name) }.padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(icon, color = if (current == name) Cyan else Muted, fontSize = 20.sp)
                Text(name, color = if (current == name) Soft else Muted, fontSize = 9.sp)
            }
        }
    }
}
