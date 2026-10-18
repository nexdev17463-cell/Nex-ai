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
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val Night = Color(0xFF07070B)
private val Glass = Color(0xCC171018)
private val Cyan = Color(0xFFFF315A)
private val Violet = Color(0xFFFF087F)
private val Soft = Color(0xFFFFF4F7)
private val Muted = Color(0xFFB7A4AF)

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
    var backendUrl by remember { mutableStateOf(appContext.getSharedPreferences("nex", 0).getString("backend_url", "") ?: "") }
    var provider by remember { mutableStateOf("gemini") }
    var chatReply by remember { mutableStateOf("Connect your NEX backend in Settings to start real conversations.") }
    var status by remember { mutableStateOf("Ready when you are. Connect a secure AI backend for real answers.") }
    LaunchedEffect(Unit) { delay(1500); splash = false }
    MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, secondary = Violet, background = Night, surface = Glass, onPrimary = Night, onBackground = Soft, onSurface = Soft)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF260914), Night, Color(0xFF03050C)))).statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp)) {
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
                            "Chat" -> ChatScreen(
                                prompt = prompt,
                                onPrompt = { prompt = it },
                                status = chatReply,
                                thinking = thinking,
                                onSend = {
                                    val message = prompt.trim()
                                    if (message.isNotBlank()) {
                                        chatReply = if (
                                            backendUrl.isNotBlank() &&
                                            backendUrl.startsWith("https://")
                                        ) {
                                            "Backend connection is not implemented yet. We can connect it next."
                                        } else {
                                            "Open Settings and add your deployed HTTPS backend URL to connect AI."
                                        }
                                        prompt = ""
                                    }
                                }
                            )
                            "Studio", "Explore" -> StudioScreen()
                            else -> SettingsScreen(backendUrl, { value -> backendUrl = value; appContext.getSharedPreferences("nex", 0).edit().putString("backend_url", value.trimEnd('/')).apply() }, backgroundOn, { enabled ->
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
    Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Cyan, Violet))).border(1.dp, Color.White.copy(alpha = .35f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text("N", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text("NEX", color = Soft, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp); Spacer(Modifier.width(5.dp)); Text("AI", color = Cyan, fontSize = 22.sp, fontWeight = FontWeight.Black) }
            Text("YOUR PERSONAL AI SPACE", color = Muted, fontSize = 9.sp, letterSpacing = 1.5.sp)
        }
        Surface(color = Color(0xFF1B1723), shape = RoundedCornerShape(50), border = androidx.compose.foundation.BorderStroke(1.dp, Cyan.copy(alpha = .45f))) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(6.dp).background(Color(0xFF72E6A1), CircleShape)); Spacer(Modifier.width(6.dp)); Text("ONLINE", color = Soft, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SplashScreen() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        NexOrb(170.dp, true)
        Spacer(Modifier.height(28.dp))
        Text("NEX AI", color = Soft, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
        Text("YOUR PERSONAL AI SPACE", color = Cyan, fontSize = 10.sp, letterSpacing = 2.sp)
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
    val messages = remember {
        mutableStateListOf(
            ChatMessage(false, "Hey! I'm NEX, your personal AI space. What are we working on today?"),
            ChatMessage(false, "I can help brainstorm, explain concepts, write code, or shape an idea into a plan.")
        )
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("YOUR AI CHAT", color = Soft, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("A fresh space for every idea", color = Muted, fontSize = 11.sp)
            }
            Surface(color = Color(0xFF21101B), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Cyan.copy(alpha = .35f))) {
                Text("✦  SMART MODE", Modifier.padding(horizontal = 10.dp, vertical = 8.dp), color = Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(Modifier.size(32.dp).clip(RoundedCornerShape(11.dp)).background(Brush.linearGradient(listOf(Cyan, Violet))), contentAlignment = Alignment.Center) { Text("N", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp) }
                    Column(Modifier.weight(1f)) {
                        Text("NEX AI", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        messages.filter { !it.fromUser }.forEach { msg ->
                            Surface(color = Color(0xE8171722), shape = RoundedCornerShape(topStart = 4.dp, topEnd = 17.dp, bottomEnd = 17.dp, bottomStart = 17.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .07f))) {
                                Text(msg.text, Modifier.padding(13.dp), color = Soft, fontSize = 13.sp, lineHeight = 19.sp)
                            }
                            Spacer(Modifier.height(7.dp))
                        }
                    }
                }
            }
            items(messages.count { it.fromUser }) { index ->
                val userMessages = messages.filter { it.fromUser }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Surface(color = Brush.linearGradient(listOf(Cyan, Violet)).let { Color(0xFFB91D54) }, shape = RoundedCornerShape(topStart = 17.dp, topEnd = 4.dp, bottomEnd = 17.dp, bottomStart = 17.dp)) {
                        Text(userMessages[index].text, Modifier.widthIn(max = 270.dp).padding(horizontal = 14.dp, vertical = 11.dp), color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
            if (thinking) item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(28.dp).clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(Cyan, Violet))), contentAlignment = Alignment.Center) { Text("N", color = Color.White, fontWeight = FontWeight.Black) }
                    Surface(color = Glass, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Cyan.copy(alpha = .25f))) { Text("Preparing your request…", Modifier.padding(12.dp), color = Muted, fontSize = 12.sp) }
                }
            }
        }
        if (messages.count { it.fromUser } == 0) {
            Text("TRY ASKING", color = Muted, fontSize = 9.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("Explain a topic", "Write code", "Brainstorm").forEach { suggestion ->
                    Surface(modifier = Modifier.weight(1f).clickable { onPrompt(suggestion) }, color = Color(0xFF17131D), shape = RoundedCornerShape(13.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Cyan.copy(alpha = .25f))) {
                        Text(suggestion, Modifier.padding(horizontal = 6.dp, vertical = 10.dp), color = Soft, fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(Color(0xFF13131D)).border(1.dp, Brush.horizontalGradient(listOf(Cyan.copy(alpha = .8f), Violet.copy(alpha = .5f))), RoundedCornerShape(23.dp)).padding(start = 14.dp, end = 6.dp, top = 5.dp, bottom = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(value = prompt, onValueChange = onPrompt, modifier = Modifier.weight(1f), placeholder = { Text("Message NEX…", color = Muted, fontSize = 13.sp) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedTextColor = Soft, unfocusedTextColor = Soft, cursorColor = Cyan, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), maxLines = 4)
            IconButton(onClick = { if (prompt.isNotBlank()) { messages.add(ChatMessage(true, prompt.trim())); onSend() } }, modifier = Modifier.size(43.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Cyan, Violet)))) { Text("➤", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(5.dp))
        Text("AI can make mistakes • Check important information", Modifier.fillMaxWidth(), color = Muted.copy(alpha = .8f), fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

private data class ChatMessage(val fromUser: Boolean, val text: String)

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
private fun SettingsScreen(backendUrl: String, onBackendUrl: (String) -> Unit, backgroundOn: Boolean, onBackground: (Boolean) -> Unit, effects: Boolean, onEffects: (Boolean) -> Unit, glow: Boolean, onGlow: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("CONTROL CENTRE", color = Cyan, fontSize = 11.sp, letterSpacing = 2.sp)
        GlassCard {
            Text("Background assistant", color = Soft, fontWeight = FontWeight.SemiBold)
            Text("Keeps a visible Android notification while running. Android may stop background work; this is not an unrestricted always-on agent.", color = Muted, fontSize = 12.sp)
            Switch(checked = backgroundOn, onCheckedChange = onBackground)
        }
        SettingRow("Floating animations", "Animate the orb and interface", effects, onEffects)
        SettingRow("Neon glow", "Neon red and pink accents", glow, onGlow)
        GlassCard {
            Text("AI CONNECTION", color = Soft, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            Text("Backend HTTPS URL", color = Muted, fontSize = 12.sp)
            TextField(value = backendUrl, onValueChange = onBackendUrl, modifier = Modifier.fillMaxWidth(), placeholder = { Text("https://your-backend.example.com") }, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Soft, unfocusedTextColor = Soft, cursorColor = Cyan))
            Text("Never put provider API keys in the app. Store GEMINI_API_KEY and OPENAI_API_KEY as server environment secrets.", color = Color(0xFFFFD58B), fontSize = 12.sp)
        }
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
    val items = listOf("Home" to "⌂", "Explore" to "⌕", "Chat" to "◉", "Studio" to "✦", "Settings" to "⚙")
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp)).background(Color(0xF2111018)).border(1.dp, Color.White.copy(alpha = .09f), RoundedCornerShape(25.dp)).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        items.forEach { (name, icon) ->
            val selected = current == name
            Column(Modifier.weight(1f).clip(RoundedCornerShape(19.dp)).background(if (selected) Brush.verticalGradient(listOf(Color(0xFF5A172F), Color(0xFF28121E))) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))).then(if (selected) Modifier.border(1.dp, Cyan.copy(alpha = .55f), RoundedCornerShape(19.dp)) else Modifier).clickable { onSelect(name) }.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(icon, color = if (selected) Cyan else Muted, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(name, color = if (selected) Soft else Muted, fontSize = 9.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                if (selected) Spacer(Modifier.height(2.dp).width(14.dp).clip(CircleShape).background(Cyan))
            }
        }
    }
}
