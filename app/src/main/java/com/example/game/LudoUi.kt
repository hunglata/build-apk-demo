package com.example.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Color darkening extension helper for 3D faces
fun Color.darken(factor: Float): Color {
    return Color(
        red = (this.red * (1f - factor)).coerceIn(0f, 1f),
        green = (this.green * (1f - factor)).coerceIn(0f, 1f),
        blue = (this.blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}

// Avatars lists
val AvatarEmojis = listOf("🧁", "🍓", "🍩", "🐼", "🦊", "🐯", "🦁")

@Composable
fun rememberAssetImagePainter(assetPath: String): Painter? {
    val context = LocalContext.current
    return remember(assetPath) {
        try {
            context.assets.open(assetPath).use { stream ->
                val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                bitmap?.let {
                    androidx.compose.ui.graphics.painter.BitmapPainter(it.asImageBitmap())
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun PlayerAvatar(
    color: PlayerColor,
    avatarId: Int,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    customEmoji: String? = null
) {
    val context = LocalContext.current
    var painter by remember(color) { mutableStateOf<Painter?>(null) }
    
    LaunchedEffect(color) {
        try {
            val possiblePaths = listOf(
                "IMG/${color.name.lowercase()}.png",
                "IMG/${color.name.lowercase()}.jpg",
                "IMG/${color.name.lowercase()}.jpeg",
                "IMG/${color.name.lowercase()}.webp",
                "img/${color.name.lowercase()}.png",
                "img/${color.name.lowercase()}.jpg",
                "img/${color.name.lowercase()}.jpeg",
                "img/${color.name.lowercase()}.webp",
                "${color.name.lowercase()}.png",
                "${color.name.lowercase()}.jpg",
                "${color.name.lowercase()}.jpeg",
                "${color.name.lowercase()}.webp",
                "IMG/${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.png",
                "img/${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.png",
                "${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.png",
                "IMG/${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.jpg",
                "img/${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.jpg",
                "${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.jpg"
            )
            
            var foundPath: String? = null
            
            // Try matching via dynamic file list first (checking "img" then "IMG" directories)
            var list = emptyArray<String>()
            var dirPrefix = "img"
            try {
                list = context.assets.list("img") ?: emptyArray()
            } catch (e: Exception) {
                // ignore
            }
            if (list.isEmpty()) {
                try {
                    list = context.assets.list("IMG") ?: emptyArray()
                    dirPrefix = "IMG"
                } catch (e: Exception) {
                    // ignore
                }
            }
            
            if (list.isNotEmpty()) {
                val match = list.firstOrNull { filename ->
                    val lower = filename.lowercase()
                    lower.contains(color.name.lowercase()) || (
                        lower.startsWith(when(color) {
                            PlayerColor.GREEN -> "1"
                            PlayerColor.RED -> "2"
                            PlayerColor.BLUE -> "3"
                            PlayerColor.YELLOW -> "4"
                        })
                    )
                }
                if (match != null) {
                    foundPath = "$dirPrefix/$match"
                }
            }
            
            if (foundPath == null) {
                // Try matching root list
                val rootList = context.assets.list("") ?: emptyArray()
                if (rootList.isNotEmpty()) {
                    val match = rootList.firstOrNull { filename ->
                        val lower = filename.lowercase()
                        lower.contains(color.name.lowercase()) || (
                            lower.startsWith(when(color) {
                                PlayerColor.GREEN -> "1"
                                PlayerColor.RED -> "2"
                                PlayerColor.BLUE -> "3"
                                PlayerColor.YELLOW -> "4"
                            })
                        )
                    }
                    if (match != null) {
                        foundPath = match
                    }
                }
            }
            
            if (foundPath == null) {
                for (path in possiblePaths) {
                    try {
                        context.assets.open(path).close()
                        foundPath = path
                        break
                    } catch (e: Exception) {
                        // ignore and try next
                    }
                }
            }
            
            if (foundPath != null) {
                context.assets.open(foundPath).use { stream ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        painter = androidx.compose.ui.graphics.painter.BitmapPainter(
                            bitmap.asImageBitmap()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    if (painter != null) {
        Image(
            painter = painter!!,
            contentDescription = "Avatar ${color.name}",
            modifier = modifier.size(size).clip(shape),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        // Fallback to emoji
        val emojiStr = when (color) {
            PlayerColor.GREEN -> "🐴"
            PlayerColor.BLUE -> "🐂"
            PlayerColor.YELLOW -> "🐥"
            PlayerColor.RED -> customEmoji ?: "🦈"
        }
        val fallbackEmoji = if (avatarId >= 0) {
            if (color == PlayerColor.RED) customEmoji ?: "🦈"
            else AvatarEmojis[avatarId % AvatarEmojis.size]
        } else {
            emojiStr
        }
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Text(fallbackEmoji, fontSize = fontSize)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LudoGameApp(viewModel: LudoViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Screen-shake offset when bump occurs
    var shakeOffset by remember { mutableStateOf(Offset.Zero) }

    val characterBitmaps = remember { mutableStateMapOf<String, ImageBitmap>() }
    LaunchedEffect(Unit) {
        for (i in 1..18) {
            val paths = listOf(
                "IMG/Character/Character$i.png",
                "IMG/Character/character$i.png",
                "IMG/Character/Character$i.PNG",
                "IMG/Character$i.png",
                "IMG/character$i.png",
                "img/Character$i.png",
                "img/character$i.png",
                "IMG/$i.png",
                "img/$i.png",
                "$i.png"
            )
            var loaded = false
            for (path in paths) {
                try {
                    context.assets.open(path).use { stream ->
                        val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                        if (bitmap != null) {
                            characterBitmaps["char$i"] = bitmap.asImageBitmap()
                            loaded = true
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
                if (loaded) break
            }
        }
    }

    // Start background music loop if ON
    LaunchedEffect(state.isMusicOn, state.status) {
        if (state.isMusicOn) {
            LudoSoundSynth.isMusicEnabled = true
            LudoSoundSynth.startMusic(state.status == GameStateStatus.MAIN_MENU || state.status == GameStateStatus.MATCHMAKING)
        } else {
            LudoSoundSynth.stopMusic()
        }
    }

    // Capture bumps for screenshake animation
    LaunchedEffect(state.pawns) {
        val anyBumping = state.pawns.any { it.isBumping }
        if (anyBumping) {
            // Trigger 5-frame shake
            launch {
                try {
                    for (i in 1..8) {
                        shakeOffset = Offset(
                            (kotlin.random.Random.nextFloat() - 0.5f) * 20f,
                            (kotlin.random.Random.nextFloat() - 0.5f) * 20f
                        )
                        delay(40)
                    }
                } finally {
                    shakeOffset = Offset.Zero
                }
            }
        } else {
            shakeOffset = Offset.Zero
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFCE4EC) // Warm pastel strawberry pink background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shakeOffset.x.dp, y = shakeOffset.y.dp)
        ) {
            when {
                state.mode == GameMode.TEAM_LOBBY && state.status == GameStateStatus.MAIN_MENU -> {
                    GhepDoiLobbyScreen(
                        state = state,
                        viewModel = viewModel,
                        characterBitmaps = characterBitmaps,
                        onExit = { viewModel.leaveTeamRoom() }
                    )
                }
                state.status == GameStateStatus.MAIN_MENU -> {
                    MainMenuScreen(
                        state = state,
                        viewModel = viewModel,
                        characterBitmaps = characterBitmaps,
                        onStartGame = { mode -> viewModel.startNewGame(mode, context) },
                        onToggleMusic = { viewModel.toggleMusic() },
                        onToggleSfx = { viewModel.toggleSfx() },
                        onSetLanguage = { lang -> viewModel.setLanguage(lang) }
                    )
                }
                state.status == GameStateStatus.MATCHMAKING -> {
                    MatchmakingScreen(
                        state = state,
                        characterBitmaps = characterBitmaps,
                        onCancel = { viewModel.exitToMainMenu() }
                    )
                }
                else -> {
                    // Actual In-Game screen with 2.5D Isometric Board
                    GamePlayScreen(
                        state = state,
                        viewModel = viewModel,
                        characterBitmaps = characterBitmaps,
                        onExit = { viewModel.exitToMainMenu() }
                    )
                }
            }

            if (state.showTeamLobbyDialog) {
                TeamLobbyOptionDialog(
                    state = state,
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeTeamLobbyOptions() }
                )
            }

            if (state.showInventoryDialogInLobby) {
                InventoryDialogInLobby(
                    state = state,
                    characterBitmaps = characterBitmaps,
                    onEquipSkin = { skinId: String, skinIcon: String -> viewModel.equipSkinInLobby(skinId, skinIcon) },
                    onDismiss = { viewModel.closeInventoryInLobby() }
                )
            }
        }
    }
}

fun isArenaOpen(): Boolean {
    val calendar = java.util.Calendar.getInstance()
    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    if (dayOfWeek == java.util.Calendar.SUNDAY) return true
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    return hour in 20..21 // 20:00 to 21:59 (20h-22h)
}

fun isNetworkAvailable(context: android.content.Context): Boolean {
    val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return false
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun MainMenuScreen(
    state: GameState,
    viewModel: LudoViewModel,
    characterBitmaps: Map<String, ImageBitmap>,
    onStartGame: (GameMode) -> Unit,
    onToggleMusic: () -> Unit,
    onToggleSfx: () -> Unit,
    onSetLanguage: (String) -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }
    val isVi = state.language == "vi"
    val context = LocalContext.current

    var showRewardsDialog by remember { mutableStateOf(false) }
    var showInventoryDialog by remember { mutableStateOf(false) }
    var showArenaClosedDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showWeeklyWheelDialog by remember { mutableStateOf(false) }
    var showOfflineSelectPlayersDialog by remember { mutableStateOf(false) }
    var showWithFriendsNamesDialog by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var adminPasswordInput by remember { mutableStateOf("") }
    var friendName1 by remember { mutableStateOf("") }
    var friendName2 by remember { mutableStateOf("") }
    var friendName3 by remember { mutableStateOf("") }
    var friendName4 by remember { mutableStateOf("") }

    val homePainter = rememberAssetImagePainter("IMG/home.jpg") ?: rememberAssetImagePainter("IMG/background.png")
    val avataPainter = rememberAssetImagePainter("IMG/avata.png")
    val tienPainter = rememberAssetImagePainter("IMG/tien.png")
    val tencongtrinhPainter = rememberAssetImagePainter("IMG/tencongtrinh.png")
    val iconPainter = rememberAssetImagePainter("IMG/icon.png")
    val nhanquaPainter = rememberAssetImagePainter("IMG/nhanqua.png") ?: iconPainter
    val vongquayPainter = rememberAssetImagePainter("IMG/vongquay.png") ?: iconPainter
    val caidatPainter = rememberAssetImagePainter("IMG/caidat.png") ?: iconPainter
    val huongdanPainter = rememberAssetImagePainter("IMG/huongdan.png") ?: iconPainter
    val cuahangPainter = rememberAssetImagePainter("IMG/cuahang.png") ?: iconPainter
    val choicungbanPainter = rememberAssetImagePainter("IMG/choicungban.png") ?: rememberAssetImagePainter("IMG/banbe.png")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF81D4FA))
    ) {
        // 1. Full Landscape Background Image (home.jpg)
        if (homePainter != null) {
            androidx.compose.foundation.Image(
                painter = homePainter,
                contentDescription = "Home Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.FillBounds
            )
        }

        // 2. Interactive Map Building Buttons overlay
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().zIndex(5f)
        ) {
            val screenW = maxWidth
            val screenH = maxHeight

            // Building Hitbox 1: Chơi Online (Blue Roof House - Left)
            Box(
                modifier = Modifier
                    .offset(x = screenW * 0.05f, y = screenH * 0.35f)
                    .size(width = screenW * 0.23f, height = screenH * 0.45f)
                    .background(Color(0x01000000))
                    .clickable {
                        LudoSoundSynth.playClick()
                        if (!isNetworkAvailable(context)) {
                            android.widget.Toast.makeText(
                                context,
                                if (isVi) "Không có kết nối Internet! Vui lòng kiểm tra mạng để vào chơi trực tuyến."
                                else "No Internet connection! Please check network to play online.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else if (state.userGold < 2000) {
                            android.widget.Toast.makeText(
                                context,
                                if (isVi) "Không đủ 2,000 Kẹo 🍭 để tham gia chơi Online! (Bạn hiện có ${state.userGold} 🍭)"
                                else "Not enough 2,000 Candy 🍭 to play Online! (You have ${state.userGold} 🍭)",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            onStartGame(GameMode.ONLINE)
                        }
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Pill Label with tencongtrinh.png (80% opacity)
                Box(
                    modifier = Modifier
                        .offset(y = (-12).dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .background(Color(0xCCFFFDF0), RoundedCornerShape(14.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (tencongtrinhPainter != null) {
                        androidx.compose.foundation.Image(
                            painter = tencongtrinhPainter,
                            contentDescription = "Tên công trình",
                            modifier = Modifier.matchParentSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                            alpha = 0.8f
                        )
                    }
                    Text(
                        text = if (isVi) "Chơi Online" else "Online Play",
                        color = Color(0xFF2C1E11),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Building Hitbox 2: ĐẤU TRƯỜNG (Red Castle - Center Left)
            Box(
                modifier = Modifier
                    .offset(x = screenW * 0.26f, y = screenH * 0.20f)
                    .size(width = screenW * 0.23f, height = screenH * 0.58f)
                    .background(Color(0x01000000))
                    .clickable {
                        LudoSoundSynth.playClick()
                        if (!isNetworkAvailable(context)) {
                            android.widget.Toast.makeText(
                                context,
                                if (isVi) "Không có kết nối Internet! Vui lòng kiểm tra mạng để vào Đấu Trường."
                                else "No Internet connection! Please check network to enter Arena.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else if (!isArenaOpen()) {
                            showArenaClosedDialog = true
                        } else {
                            onStartGame(GameMode.ARENA)
                        }
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Pill Label (80% opacity)
                Box(
                    modifier = Modifier
                        .offset(y = (-12).dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .background(Color(0xCCFFFDF0), RoundedCornerShape(14.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (tencongtrinhPainter != null) {
                        androidx.compose.foundation.Image(
                            painter = tencongtrinhPainter,
                            contentDescription = "Tên công trình",
                            modifier = Modifier.matchParentSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                            alpha = 0.8f
                        )
                    }
                    Text(
                        text = if (isVi) "ĐẤU TRƯỜNG" else "ARENA",
                        color = Color(0xFF2C1E11),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Building Hitbox 3: GHÉP ĐỘI (Statue - Center Right)
            Box(
                modifier = Modifier
                    .offset(x = screenW * 0.48f, y = screenH * 0.28f)
                    .size(width = screenW * 0.21f, height = screenH * 0.50f)
                    .background(Color(0x01000000))
                    .clickable {
                        LudoSoundSynth.playClick()
                        if (!isNetworkAvailable(context)) {
                            android.widget.Toast.makeText(
                                context,
                                if (isVi) "Không có kết nối Internet! Vui lòng kiểm tra mạng để ghép đội trực tuyến."
                                else "No Internet connection! Please check network to join team lobby.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            viewModel.openTeamLobbyOptions()
                        }
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Pill Label (80% opacity)
                Box(
                    modifier = Modifier
                        .offset(y = (-12).dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .background(Color(0xCCFFFDF0), RoundedCornerShape(14.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (tencongtrinhPainter != null) {
                        androidx.compose.foundation.Image(
                            painter = tencongtrinhPainter,
                            contentDescription = "Tên công trình",
                            modifier = Modifier.matchParentSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                            alpha = 0.8f
                        )
                    }
                    Text(
                        text = if (isVi) "GHÉP ĐỘI" else "TEAM LOBBY",
                        color = Color(0xFF2C1E11),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Building Hitbox 4: Chơi Offline (Red Roof Shop - Right)
            Box(
                modifier = Modifier
                    .offset(x = screenW * 0.70f, y = screenH * 0.28f)
                    .size(width = screenW * 0.24f, height = screenH * 0.50f)
                    .zIndex(10f)
                    .background(Color(0x01000000))
                    .clickable {
                        LudoSoundSynth.playClick()
                        onStartGame(GameMode.OFFLINE)
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Pill Label (80% opacity - synchronized with other 3 buildings)
                Box(
                    modifier = Modifier
                        .offset(y = (-12).dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .background(Color(0xCCFFFDF0), RoundedCornerShape(14.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (tencongtrinhPainter != null) {
                        androidx.compose.foundation.Image(
                            painter = tencongtrinhPainter,
                            contentDescription = "Tên công trình",
                            modifier = Modifier.matchParentSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                            alpha = 0.8f
                        )
                    }
                    Text(
                        text = if (isVi) "Chơi Offline" else "Play Offline",
                        color = Color(0xFF2C1E11),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // 3. TOP HEADER HUD ROW (Profile, Currency, Top Right Action Buttons)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .zIndex(100f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile & Balances Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Profile & Name Block (Avatar & Name Frame)
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color(0xFFFFFBEA),
                    border = BorderStroke(2.5.dp, Color(0xFFFFD54F)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .height(52.dp)
                        .clickable {
                            LudoSoundSynth.playClick()
                            showRenameDialog = true
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        // Avatar Circle Box with Gold Ring
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(2.dp, CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFFF8E1), Color(0xFFFFE082))
                                    ),
                                    CircleShape
                                )
                                .border(2.dp, Color(0xFFFFB300), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val bitmap = characterBitmaps[state.selectedCharacter]
                            if (bitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(34.dp)
                                )
                            } else {
                                Text("🧁", fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        // Player Name
                        val displayName = if (state.playerName.isNotBlank()) state.playerName else (if (isVi) "Người chơi 1" else "Player 1")
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color(0xFF3E2723),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("✏️", fontSize = 12.sp)
                    }
                }

                // Currency Bar (uses tien.png)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFFBEA), RoundedCornerShape(20.dp))
                        .border(2.dp, Color(0xFFE6D8B8), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (tienPainter != null) {
                        androidx.compose.foundation.Image(
                            painter = tienPainter,
                            contentDescription = "Lollipops",
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text("🍭", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%,d", state.userGold),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF3E2723)
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .height(16.dp)
                            .background(Color(0xFFD0C0A0))
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    if (tienPainter != null) {
                        androidx.compose.foundation.Image(
                            painter = tienPainter,
                            contentDescription = "Diamonds",
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text("💎", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%,d", state.userDiamonds),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF3E2723)
                    )
                }
            }

            // Top-right action group (Nhận quà, Vòng quay, Cài đặt)
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Nhận quà
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showRewardsDialog = true }
                ) {
                    Box(
                        modifier = Modifier.wrapContentSize(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFFBEA), RoundedCornerShape(16.dp))
                                .border(2.dp, Color(0xFFE6D8B8), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (nhanquaPainter != null) {
                                androidx.compose.foundation.Image(
                                    painter = nhanquaPainter,
                                    contentDescription = "Nhận quà",
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                Text("🎁", fontSize = 24.sp)
                            }
                        }

                        if (viewModel.canClaimDailyReward()) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 6.dp, y = (-6).dp)
                                    .zIndex(100f)
                                    .size(20.dp)
                                    .background(Color(0xFFFF1744), CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("!", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isVi) "Nhận quà" else "Rewards",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF2C1E11),
                                offset = Offset(1.5f, 1.5f),
                                blurRadius = 3f
                            )
                        )
                    )
                }

                // 2. Vòng quay
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showWeeklyWheelDialog = true }
                ) {
                    Box(
                        modifier = Modifier.wrapContentSize(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFFBEA), RoundedCornerShape(16.dp))
                                .border(2.dp, Color(0xFFE6D8B8), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (vongquayPainter != null) {
                                androidx.compose.foundation.Image(
                                    painter = vongquayPainter,
                                    contentDescription = "Vòng quay",
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                Text("🎡", fontSize = 24.sp)
                            }
                        }

                        if (viewModel.canSpinWeeklyWheel()) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 6.dp, y = (-6).dp)
                                    .zIndex(100f)
                                    .size(20.dp)
                                    .background(Color(0xFFFF1744), CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("!", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isVi) "Vòng quay" else "Wheel",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF2C1E11),
                                offset = Offset(1.5f, 1.5f),
                                blurRadius = 3f
                            )
                        )
                    )
                }

                // 3. Cài đặt
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showSettings = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFFBEA), RoundedCornerShape(16.dp))
                            .border(2.dp, Color(0xFFE6D8B8), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (caidatPainter != null) {
                            androidx.compose.foundation.Image(
                                painter = caidatPainter,
                                contentDescription = "Cài đặt",
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF5D4037),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isVi) "Cài đặt" else "Settings",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF2C1E11),
                                offset = Offset(1.5f, 1.5f),
                                blurRadius = 3f
                            )
                        )
                    )
                }
            }
        }

        // 4. BOTTOM RIGHT NAVIGATION (Cùng Bạn Bè, Hướng Dẫn, Cửa Hàng)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 12.dp)
                .zIndex(50f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chơi Cùng Bạn Bè
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    LudoSoundSynth.playClick()
                    showOfflineSelectPlayersDialog = true
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFFBEA), RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFFE6D8B8), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (choicungbanPainter != null) {
                        androidx.compose.foundation.Image(
                            painter = choicungbanPainter,
                            contentDescription = "Cùng Bạn Bè",
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Text("👨‍👩‍👧‍👦", fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isVi) "Cùng Bạn Bè" else "With Friends",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF2C1E11),
                            offset = Offset(1.5f, 1.5f),
                            blurRadius = 3f
                        )
                    )
                )
            }

            // Hướng Dẫn
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { showTutorial = true }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFFBEA), RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFFE6D8B8), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (huongdanPainter != null) {
                        androidx.compose.foundation.Image(
                            painter = huongdanPainter,
                            contentDescription = "Hướng Dẫn",
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Text("📖", fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isVi) "Hướng Dẫn" else "Tutorial",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF2C1E11),
                            offset = Offset(1.5f, 1.5f),
                            blurRadius = 3f
                        )
                    )
                )
            }

            // Cửa Hàng
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { showInventoryDialog = true }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFFBEA), RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFFE6D8B8), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (cuahangPainter != null) {
                        androidx.compose.foundation.Image(
                            painter = cuahangPainter,
                            contentDescription = "Cửa Hàng",
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Text("🏪", fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isVi) "Cửa Hàng" else "Shop",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF2C1E11),
                            offset = Offset(1.5f, 1.5f),
                            blurRadius = 3f
                        )
                    )
                )
            }
        }

        // Subtle Admin Button on Bottom-Left Corner (Faint and hard to notice)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 12.dp)
                .zIndex(50f)
                .background(Color(0x0D000000), RoundedCornerShape(6.dp))
                .border(0.5.dp, Color(0x12FFFFFF), RoundedCornerShape(6.dp))
                .clickable {
                    LudoSoundSynth.playClick()
                    adminPasswordInput = ""
                    showAdminDialog = true
                }
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Admin",
                color = Color.White.copy(alpha = 0.12f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // Admin Buff Password Dialog
    if (showAdminDialog) {
        Dialog(onDismissRequest = { 
            showAdminDialog = false
            adminPasswordInput = ""
        }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3E2723)),
                border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "🔐 VIP Admin Mode",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isVi) "Nhập mật khẩu Admin để nhận +10k Kẹo & +500 Kim cương test game:" 
                               else "Enter Admin Password to get +10k Lollipops & +500 Diamonds:",
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = { adminPasswordInput = it },
                        label = { Text("Mật khẩu / Password", color = Color(0xFFD7CCC8)) },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFD54F),
                            unfocusedBorderColor = Color(0xFF8D6E63),
                            focusedContainerColor = Color(0xFF2C1E11),
                            unfocusedContainerColor = Color(0xFF2C1E11)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                showAdminDialog = false
                                adminPasswordInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                        ) {
                            Text(if (isVi) "Hủy" else "Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                if (adminPasswordInput == "2804") {
                                    viewModel.buffAdminCurrency(10000, 500)
                                    android.widget.Toast.makeText(
                                        context,
                                        if (isVi) "🎉 Mở khóa Admin thành công! +10,000 🍭 Kẹo & +500 💎 Kim cương"
                                        else "🎉 Admin unlocked! +10,000 🍭 Lollipops & +500 💎 Diamonds",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    showAdminDialog = false
                                    adminPasswordInput = ""
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        if (isVi) "❌ Mật khẩu Admin không đúng!" else "❌ Incorrect Admin password!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text(if (isVi) "Xác Nhận" else "Confirm", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Weekly Lucky Wheel Dialog
    if (showWeeklyWheelDialog) {
        val canSpin = viewModel.canSpinWeeklyWheel()
        var isSpinning by remember { mutableStateOf(false) }
        var resultText by remember { mutableStateOf<String?>(null) }
        val rotationAnim = remember { Animatable(0f) }
        val coroutineScope = rememberCoroutineScope()

        Dialog(onDismissRequest = { if (!isSpinning) showWeeklyWheelDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0xCCFFFFFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                border = androidx.compose.foundation.BorderStroke(3.5.dp, Color(0xFFD81B60))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎡 " + (if (isVi) "VÒNG QUAY MAY MẮN MỖI TUẦN" else "WEEKLY LUCKY WHEEL"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD81B60),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isVi) "Quay 1 lần/tuần - Nhận Kim cương, Kẹo mút & Nhân vật VIP!" else "Spin 1x/week - Win Diamonds, Lollipops & VIP Characters!",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    // The Wheel Visual
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Wheel Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(rotationZ = rotationAnim.value)
                        ) {
                            val slices = 8
                            val sweepAngle = 360f / slices
                            val sliceColors = listOf(
                                Color(0xFFFF8A80), Color(0xFF81C784), Color(0xFFFFD54F), Color(0xFF64B5F6),
                                Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4DD0E1), Color(0xFFAED581)
                            )
                            val sliceLabels = listOf(
                                "🍭 300", "💎 10", "❌ TRỐNG", "🎭 NV 2-3⭐",
                                "🍭 500", "💎 20", "❌ TRỐNG", "🎭 VIP"
                            )

                            val r = size.width / 2f
                            for (i in 0 until slices) {
                                drawArc(
                                    color = sliceColors[i],
                                    startAngle = i * sweepAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true
                                )
                                drawArc(
                                    color = Color.White.copy(alpha = 0.5f),
                                    startAngle = i * sweepAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    style = Stroke(width = 3f)
                                )
                            }

                            // Outer ring
                            drawCircle(Color(0xFFFFD54F), radius = r, style = Stroke(width = 8f))
                            drawCircle(Color(0xFF8D6E63), radius = r, style = Stroke(width = 2.5f))

                            // Slice labels
                            sliceLabels.forEachIndexed { i, label ->
                                val midAngle = Math.toRadians((i * sweepAngle + sweepAngle / 2).toDouble())
                                val textR = r * 0.65f
                                val tx = (r + textR * Math.cos(midAngle)).toFloat()
                                val ty = (r + textR * Math.sin(midAngle)).toFloat()

                                drawIntoCanvas { canvas ->
                                    val paint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 26f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                        isAntiAlias = true
                                        setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                                    }
                                    val textOffset = (paint.descent() + paint.ascent()) / 2f
                                    canvas.nativeCanvas.drawText(label, tx, ty - textOffset, paint)
                                }
                            }
                        }

                        // Top Pointer Arrow 🎯
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-8).dp)
                        ) {
                            Text("🔽", fontSize = 28.sp)
                        }

                        // Center Hub
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(4.dp, CircleShape)
                                .background(Color(0xFFFFD54F), CircleShape)
                                .border(2.5.dp, Color(0xFF8D6E63), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎁", fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (resultText != null) {
                        Text(
                            text = resultText!!,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD81B60),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(Color(0xFFFFF0F5), RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color(0xFFFF8A80), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Button(
                        onClick = {
                            if (canSpin && !isSpinning) {
                                isSpinning = true
                                resultText = null
                                coroutineScope.launch {
                                    val targetSlice = kotlin.random.Random.nextInt(0, 8)
                                    val sweep = 360f / 8
                                    val sliceCenter = targetSlice * sweep + sweep / 2f
                                    val targetAngle = 360f * 5 + (270f - sliceCenter)
                                    
                                    rotationAnim.snapTo(rotationAnim.value % 360f)
                                    rotationAnim.animateTo(
                                        targetValue = rotationAnim.value + targetAngle,
                                        animationSpec = tween(durationMillis = 3200, easing = FastOutSlowInEasing)
                                    )
                                    LudoSoundSynth.playGoalCelebration()
                                    val msg = viewModel.spinWeeklyWheel(targetSlice)
                                    resultText = msg
                                    isSpinning = false
                                }
                            }
                        },
                        enabled = canSpin && !isSpinning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canSpin) Color(0xFFD81B60) else Color.LightGray
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (isSpinning) {
                                if (isVi) "ĐANG QUAY..." else "SPINNING..."
                            } else if (canSpin) {
                                if (isVi) "🚀 QUAY MAY MẮN NGAY!" else "🚀 SPIN NOW!"
                            } else {
                                if (isVi) "ĐÃ QUAY TUẦN NÀY (CHỜ TUẦN SAU)" else "SPUN THIS WEEK"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    androidx.compose.material3.TextButton(
                        onClick = { showWeeklyWheelDialog = false },
                        enabled = !isSpinning
                    ) {
                        Text(if (isVi) "Đóng" else "Close", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Special Sweet reward dialog
    if (showRewardsDialog) {
        val canClaim = viewModel.canClaimDailyReward()
        Dialog(onDismissRequest = { showRewardsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xCCFFFFFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎁 " + (if (isVi) "Nhận Quà Hàng Ngày" else "Daily Reward"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD81B60)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (canClaim) {
                            if (isVi) "Chúc mừng! Bạn nhận được quà điểm danh hàng ngày:\n\n🍭 +200 Kẹo mút ngọt ngào!"
                            else "Congratulations! Daily check-in reward:\n\n🍭 +200 Sweet Lollipops!"
                        } else {
                            if (isVi) "Bạn đã nhận quà hôm nay rồi!\n\nThời gian nhận quà sẽ reset sau 00:00 hàng ngày. Hãy quay lại vào ngày mai để tiếp tục nhận quà nhé! 💖"
                            else "You have already claimed your daily reward today!\n\nThe claim timer resets at 00:00 daily. Please come back tomorrow for more sweet treats! 💖"
                        },
                        fontSize = 15.sp,
                        color = Color(0xFF5D4037),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (canClaim) {
                                val success = viewModel.claimDailyReward()
                                if (success) {
                                    LudoSoundSynth.playGoalCelebration()
                                    android.widget.Toast.makeText(
                                        context,
                                        if (isVi) "Nhận quà thành công! +200 Kẹo mút 🍭"
                                        else "Reward claimed! +200 Sweet Lollipops 🍭",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            showRewardsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canClaim) Color(0xFFD81B60) else Color.LightGray,
                            contentColor = Color.White
                        ),
                        enabled = canClaim,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (canClaim) {
                                if (isVi) "NHẬN QUÀ" else "CLAIM REWARD"
                            } else {
                                if (isVi) "ĐÃ NHẬN HÔM NAY" else "CLAIMED TODAY"
                            }
                        )
                    }
                    if (!canClaim) {
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.TextButton(
                            onClick = { showRewardsDialog = false }
                        ) {
                            Text(if (isVi) "Đóng" else "Close", color = Color(0xFFD81B60))
                        }
                    }
                }
            }
        }
    }

    if (showArenaClosedDialog) {
        Dialog(onDismissRequest = { showArenaClosedDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xCCFFFFFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆", fontSize = 64.sp)
                    Text(
                        text = if (isVi) "Đấu Trường Ludo" else "Ludo Arena",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD81B60),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isVi) {
                            "Đấu trường chỉ mở cửa tranh tài vào lúc 20:00 - 22:00 hàng ngày (Riêng Chủ Nhật mở cả ngày)!\n\nỞ đây, bạn sẽ đấu với các đối thủ thông minh và có cơ hội kiếm được rất nhiều Kẹo mút vàng 🍭 khi chiến thắng về nhất hoặc về nhì!\n\nHiện tại đấu trường đang đóng cửa."
                        } else {
                            "The Arena is only open for competition from 20:00 to 22:00 daily (Open all day on Sunday)!\n\nHere, you will play against highly intelligent bots and earn lots of Golden Lollipops 🍭 when finishing 1st or 2nd place!\n\nCurrently, the Arena is closed."
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF5D4037),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Prominent ADMIN BYPASS button
                    Button(
                        onClick = {
                            showArenaClosedDialog = false
                            onStartGame(GameMode.ARENA)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isVi) "🛠️ CHƠI THỬ NGHIỆM (ADMIN BYPASS)" else "🛠️ PLAY TEST MODE (ADMIN BYPASS)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    androidx.compose.material3.OutlinedButton(
                        onClick = { showArenaClosedDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isVi) "QUAY LẠI SAU" else "COME BACK LATER", color = Color.Gray)
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        Dialog(onDismissRequest = { showRenameDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFFFDF0),
                border = BorderStroke(3.dp, Color(0xFFFFB300)),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                var tempName by remember { mutableStateOf(state.playerName.ifBlank { if (isVi) "Người chơi 1" else "Player 1" }) }
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✏️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isVi) "ĐỔI TÊN NGƯỜI CHƠI" else "CHANGE PLAYER NAME",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF5D4037)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { if (it.length <= 16) tempName = it },
                        label = { Text(if (isVi) "Tên hiển thị" else "Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB300),
                            unfocusedBorderColor = Color(0xFFBCAAA4),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = { showRenameDialog = false }
                        ) {
                            Text(
                                if (isVi) "HỦY" else "CANCEL",
                                color = Color(0xFF8D6E63),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                val trimmed = tempName.trim()
                                if (trimmed.isNotBlank()) {
                                    viewModel.updatePlayerName(trimmed)
                                    showRenameDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                if (isVi) "LƯU" else "SAVE",
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }

    // Special Sweet Shop/Cửa Hàng full-screen overlay
    if (showInventoryDialog) {
        Dialog(
            onDismissRequest = { showInventoryDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            val dialogView = androidx.compose.ui.platform.LocalView.current
            androidx.compose.runtime.SideEffect {
                val window = (dialogView.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
                if (window != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        window.attributes.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                    window.setLayout(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
            InventoryShopScreen(
                state = state,
                viewModel = viewModel,
                characterBitmaps = characterBitmaps,
                onClose = { showInventoryDialog = false }
            )
        }
    }

    // Settings popup Dialog
    if (showSettings) {
        var langExpanded by remember { mutableStateOf(false) }
        Dialog(onDismissRequest = { showSettings = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isVi) "Cài đặt Game" else "Game Settings",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // 1. Music toggle (BGM)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🎵", fontSize = 18.sp)
                            Text(
                                text = if (isVi) "Nhạc Nền (BGM)" else "Background Music",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                        }
                        Switch(
                            checked = state.isMusicOn,
                            onCheckedChange = { onToggleMusic() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFE91E63),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }

                    // 2. Sound Effects toggle (SFX)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🔊", fontSize = 18.sp)
                            Text(
                                text = if (isVi) "Hiệu Ứng Âm Thanh" else "Sound Effects",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                        }
                        Switch(
                            checked = state.isSfxOn,
                            onCheckedChange = { onToggleSfx() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFE91E63),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }

                    // 3. Language selection (Dropdown picker like in screenshot)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🌐", fontSize = 18.sp)
                            Text(
                                text = if (isVi) "Ngôn Ngữ" else "Language",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                        }

                        Box {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFCFD8DC)),
                                color = Color.White,
                                modifier = Modifier.clickable { langExpanded = !langExpanded }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (state.language == "vi") "🇻🇳 Tiếng Việt" else "🇬🇧 English",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF37474F)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFF78909C),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = langExpanded,
                                onDismissRequest = { langExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🇻🇳 Tiếng Việt", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        onSetLanguage("vi")
                                        langExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🇬🇧 English", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        onSetLanguage("en")
                                        langExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Close Button (Pill shaped magenta button)
                    Button(
                        onClick = { showSettings = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (isVi) "ĐÓNG" else "CLOSE",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Tutorial dialog
    if (showTutorial) {
        Dialog(onDismissRequest = { showTutorial = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xCCFFFFFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isVi) "Cách Chơi Sweety Ludo" else "How To Play",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val tutText = if (isVi) {
                        "1. **Đổ xúc xắc**: Nhấn vào khối chocolate ở góc phải khi đến lượt.\n" +
                        "2. **Xuất quân**: Đổ được số 6 để đưa quân từ Căn Cứ Bánh Kem ra vạch xuất phát.\n" +
                        "3. **Đá quân**: Di chuyển trùng ô với đối thủ để đá bay họ về căn cứ (Trừ ô Hoa An Toàn 🌸).\n" +
                        "4. **Tạo khối chắn**: 2 quân cùng màu đứng chung ô sẽ tạo khối chắn đối thủ không thể đá.\n" +
                        "5. **Thêm lượt**: Nhận thêm lượt đổ khi đổ được 6, đá đối thủ hoặc đưa quân về đích thành công (Tối đa 3 lần đổ 6 sẽ mất lượt).\n" +
                        "6. **Chiến thắng**: Đưa toàn bộ 4 quân cờ nhảy lên đỉnh tháp bánh ở trung tâm bằng điểm đổ chính xác!"
                    } else {
                        "1. **Roll Dice**: Tap the chocolate dice block on bottom right when it is your turn.\n" +
                        "2. **Deployment**: Roll a 6 to deploy a cute bird from your high Cage to the track.\n" +
                        "3. **Bump (Kill)**: Land on an opponent's tile to bump them back to base (except on safe Green Flowers 🌸).\n" +
                        "4. **Blocks**: 2 same-colored birds on a tile form a double-block that cannot be bumped.\n" +
                        "5. **Bonus Rolls**: Get a bonus roll by rolling a 6, bumping an opponent, or reaching Home. Rolling a 6 thrice voids turn.\n" +
                        "6. **Goal**: Land all 4 pawns at the center cake top with exact rolls to win!"
                    }

                    Text(
                        text = tutText,
                        fontSize = 14.sp,
                        color = Color(0xFF5D4037),
                        lineHeight = 20.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showTutorial = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isVi) "ĐÃ HIỂU" else "GOT IT")
                    }
                }
            }
        }
    }

    if (showWithFriendsNamesDialog) {
        Dialog(onDismissRequest = { showWithFriendsNamesDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xCCFFFFFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                border = BorderStroke(3.dp, Color(0xFF8D6E63))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isVi) "👥 CHẾ ĐỘ TỰ DO - NHẬP TÊN NGƯỜI CHƠI" else "👥 FREE MODE - ENTER PLAYER NAMES",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD81B60),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isVi) "Điền tên người chơi. Ô để trống sẽ tự động thay bằng Bot 🤖" else "Fill names for human players. Blank slots will be Bots 🤖",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp, top = 2.dp)
                    )

                    val defaultP1 = if (state.playerName.isNotEmpty()) state.playerName else (if (isVi) "Bạn" else "You")

                    // Name 1 (Red)
                    androidx.compose.material3.OutlinedTextField(
                        value = friendName1,
                        onValueChange = { friendName1 = it },
                        label = { Text(if (isVi) "Người chơi 1 (Đỏ 🔴)" else "Player 1 (Red 🔴)", fontSize = 12.sp) },
                        placeholder = { Text(defaultP1) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Name 2 (Green)
                    androidx.compose.material3.OutlinedTextField(
                        value = friendName2,
                        onValueChange = { friendName2 = it },
                        label = { Text(if (isVi) "Người chơi 2 (Lá 🟢)" else "Player 2 (Green 🟢)", fontSize = 12.sp) },
                        placeholder = { Text("Player 2") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Name 3 (Yellow)
                    androidx.compose.material3.OutlinedTextField(
                        value = friendName3,
                        onValueChange = { friendName3 = it },
                        label = { Text(if (isVi) "Người chơi 3 (Vàng 🟡)" else "Player 3 (Yellow 🟡)", fontSize = 12.sp) },
                        placeholder = { Text("Player 3") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Name 4 (Blue)
                    androidx.compose.material3.OutlinedTextField(
                        value = friendName4,
                        onValueChange = { friendName4 = it },
                        label = { Text(if (isVi) "Người chơi 4 (Dương 🔵)" else "Player 4 (Blue 🔵)", fontSize = 12.sp) },
                        placeholder = { Text("Player 4") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent Start Game Button
                    Button(
                        onClick = {
                            val p1 = if (friendName1.isNotBlank()) friendName1 else defaultP1
                            val namesList = listOf(p1, friendName2.trim(), friendName3.trim(), friendName4.trim())

                            showWithFriendsNamesDialog = false
                            showOfflineSelectPlayersDialog = false
                            viewModel.startNewGame(GameMode.WITH_FRIENDS, context, namesList, humanCount = 4, noBots = false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (isVi) "🚀 BẮT ĐẦU VÀO GAME NGAY 🚀" else "🚀 START GAME NOW 🚀",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.material3.TextButton(
                        onClick = { showWithFriendsNamesDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isVi) "HỦY" else "CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showOfflineSelectPlayersDialog) {
        PlayWithFriendsScreen(
            state = state,
            viewModel = viewModel,
            onBack = { showOfflineSelectPlayersDialog = false },
            onOpenAddressBook = { showWithFriendsNamesDialog = true },
            onOpenSettings = { showSettings = true }
        )
    }
}

@Composable
fun PlayWithFriendsScreen(
    state: GameState,
    viewModel: LudoViewModel,
    onBack: () -> Unit,
    onOpenAddressBook: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val isVi = state.language == "vi"
    val context = LocalContext.current
    val homePainter = rememberAssetImagePainter("IMG/home.jpg") ?: rememberAssetImagePainter("IMG/background.png")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(200f)
            .background(Color.Black)
    ) {
        // 1. Background Image
        if (homePainter != null) {
            androidx.compose.foundation.Image(
                painter = homePainter,
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                alpha = 0.85f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ================= 1. HEADER ROW =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Yellow Circle with Arrow)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                            ),
                            shape = CircleShape
                        )
                        .border(2.5.dp, Color(0xFFFFF59D), CircleShape)
                        .clickable {
                            LudoSoundSynth.playClick()
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Center Title Board (Wooden Signboard)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFA0522D), Color(0xFF5C2C16))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(2.5.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                            .padding(horizontal = 28.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌸", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVi) "CHƠI CÙNG BẠN BÈ" else "PLAY WITH FRIENDS",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color(0xFF200F05),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🌸", fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isVi) "◆  Chơi offline trên cùng 1 máy  ◆" else "◆  Local offline play on same device  ◆",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                offset = Offset(1.5f, 1.5f),
                                blurRadius = 3f
                            )
                        )
                    )
                }

                // Spacer on top right for symmetry (Contacts button removed)
                Spacer(modifier = Modifier.width(46.dp))
            }

            // ================= 2. 4 HORIZONTAL SLOT CARDS =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slot 1: Đấu 2 Người (Không có Bot)
                PlayModeBannerSlotCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    title = if (isVi) "Đấu 2 Người" else "2 Players",
                    subtitle = if (isVi) "Không có Bot" else "No Bots",
                    badgeIcon = "👥",
                    badgeBgColor = Color(0xFF80DEEA),
                    bgGradient = listOf(Color(0xFF03A9F4), Color(0xFF0277BD)),
                    borderColor = Color(0xFF81D4FA),
                    artIcons = "👦 👧",
                    onClick = {
                        LudoSoundSynth.playClick()
                        viewModel.startNewGame(GameMode.WITH_FRIENDS, context, humanCount = 2, noBots = true)
                        onBack()
                    }
                )

                // Slot 2: Đấu 3 Người (Không có Bot)
                PlayModeBannerSlotCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    title = if (isVi) "Đấu 3 Người" else "3 Players",
                    subtitle = if (isVi) "Không có Bot" else "No Bots",
                    badgeIcon = "👨‍👧‍👦",
                    badgeBgColor = Color(0xFFFFE082),
                    bgGradient = listOf(Color(0xFFFFB74D), Color(0xFFF57C00)),
                    borderColor = Color(0xFFFFE082),
                    artIcons = "👧 👦 👧",
                    onClick = {
                        LudoSoundSynth.playClick()
                        viewModel.startNewGame(GameMode.WITH_FRIENDS, context, humanCount = 3, noBots = true)
                        onBack()
                    }
                )

                // Slot 3: Đấu 4 Người (Nhóm 4 người)
                PlayModeBannerSlotCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    title = if (isVi) "Đấu 4 Người" else "4 Players",
                    subtitle = if (isVi) "Nhóm 4 người" else "4 Humans",
                    badgeIcon = "👨‍👩‍👧‍👦",
                    badgeBgColor = Color(0xFFFFCDD2),
                    bgGradient = listOf(Color(0xFFEF5350), Color(0xFFC62828)),
                    borderColor = Color(0xFFFFCDD2),
                    artIcons = "👦 👧 👦 👧",
                    onClick = {
                        LudoSoundSynth.playClick()
                        viewModel.startNewGame(GameMode.WITH_FRIENDS, context, humanCount = 4, noBots = true)
                        onBack()
                    }
                )

                // Slot 4: Chế độ tự do (Tùy chọn số lượng Bot)
                PlayModeBannerSlotCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    title = if (isVi) "Chế độ tự do" else "Custom Free",
                    subtitle = if (isVi) "Tùy chọn số Bot" else "Custom Bots",
                    badgeIcon = "🎮",
                    badgeBgColor = Color(0xFFE1BEE7),
                    bgGradient = listOf(Color(0xFFAB47BC), Color(0xFF6A1B9A)),
                    borderColor = Color(0xFFE1BEE7),
                    artIcons = "🎲 🤖",
                    onClick = {
                        LudoSoundSynth.playClick()
                        onOpenAddressBook()
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun PlayModeBannerSlotCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    badgeIcon: String,
    badgeBgColor: Color,
    bgGradient: List<Color>,
    borderColor: Color,
    artIcons: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        border = BorderStroke(2.5.dp, borderColor),
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(bgGradient))
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top badge circle
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(6.dp, CircleShape)
                        .background(badgeBgColor, CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badgeIcon, fontSize = 22.sp)
                }

                // Middle Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.6f),
                                offset = Offset(1.5f, 1.5f),
                                blurRadius = 3f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Bottom Art Icons & Arrow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = artIcons,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "❯",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun PlayModeBannerCard(
    title: String,
    subtitle: String,
    badgeIcon: String,
    badgeBgColor: Color,
    bgGradient: List<Color>,
    borderColor: Color,
    artIcons: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = BorderStroke(2.5.dp, borderColor),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.horizontalGradient(bgGradient))
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Badge & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Badge circle
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(4.dp, CircleShape)
                            .background(badgeBgColor, CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(badgeIcon, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(1.5f, 1.5f),
                                    blurRadius = 3f
                                )
                            )
                        )
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right side: Art Icons & Arrow
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = artIcons,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "❯",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    desc: String,
    icon: String,
    bgColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable { onClick() }
            .testTag("mode_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF4E342E)
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun FooterButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color(0xFFD81B60))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color(0xFF4E342E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun MatchmakingScreen(
    state: GameState,
    characterBitmaps: Map<String, ImageBitmap> = emptyMap(),
    onCancel: () -> Unit
) {
    val isVi = state.language == "vi"
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .rotate(rotation)
                .border(6.dp, Color(0xFFFFB74D), CircleShape)
                .background(Color(0xFF0F172A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val defaultBm = characterBitmaps.values.firstOrNull()
            if (defaultBm != null) {
                Image(
                    bitmap = defaultBm,
                    contentDescription = "Bóng đen nhân vật",
                    modifier = Modifier
                        .size(85.dp)
                        .rotate(-rotation),
                    colorFilter = ColorFilter.tint(Color(0xFF475569), BlendMode.SrcIn)
                )
            } else {
                Text(
                    text = "👤",
                    fontSize = 58.sp,
                    modifier = Modifier.rotate(-rotation),
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = if (isVi) "Đang Tìm Đối Thủ Trực Tuyến..." else "Finding Online Opponents...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F)
        )
        Text(
            text = if (isVi) "Đang tìm kiếm đối thủ xứng tầm..." else "Searching for worthy opponents...",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isVi) "HỦY TÌM KIẾM" else "CANCEL SEARCH", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GamePlayScreen(
    state: GameState,
    viewModel: LudoViewModel,
    characterBitmaps: Map<String, ImageBitmap>,
    onExit: () -> Unit
) {
    val isVi = state.language == "vi"
    val activePlayer = state.players.getOrNull(state.activePlayerIndex) ?: return

    val isMultiplayer = state.mode == GameMode.TEAM_LOBBY || state.mode == GameMode.ARENA || state.activeRoom != null
    val mySlotColor = slotToColor(state.myPlayerId)
    val isMyTurn = if (isMultiplayer) (activePlayer.color == mySlotColor) else (!activePlayer.isBot)

    val context = LocalContext.current
    var backgroundPlayBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(Unit) {
        val paths = listOf("IMG/background_play.png", "img/background_play.png", "background_play.png")
        for (path in paths) {
            try {
                context.assets.open(path).use { stream ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        backgroundPlayBitmap = bitmap.asImageBitmap()
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
            if (backgroundPlayBitmap != null) break
        }
    }

    // Prevent screen timeout during gameplay
    val currentView = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(currentView) {
        currentView.keepScreenOn = true
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) {
                ctx.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                break
            }
            ctx = ctx.baseContext
        }
        onDispose {
            currentView.keepScreenOn = true
            var c = context
            while (c is android.content.ContextWrapper) {
                if (c is android.app.Activity) {
                    c.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    break
                }
                c = c.baseContext
            }
        }
    }

    // Periodically trigger random pawn emotes during gameplay
    LaunchedEffect(Unit) {
        while (true) {
            delay(12000)
            viewModel.triggerRandomPawnEmote()
        }
    }

    // Board shake effect when pawn deploys from base
    val shakeAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(state.boardShakeTrigger) {
        if (state.boardShakeTrigger > 0L) {
            try {
                shakeAnim.snapTo(0f)
                shakeAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(350, easing = LinearEasing)
                )
            } finally {
                shakeAnim.snapTo(0f)
            }
        } else {
            shakeAnim.snapTo(0f)
        }
    }
    val shakeX = if (shakeAnim.value > 0f) {
        (kotlin.math.sin(shakeAnim.value * Math.PI.toFloat() * 8f) * 12.dp.value * (1f - shakeAnim.value))
    } else 0f
    val shakeY = if (shakeAnim.value > 0f) {
        (kotlin.math.cos(shakeAnim.value * Math.PI.toFloat() * 8f) * 12.dp.value * (1f - shakeAnim.value))
    } else 0f

    // Camera zoom intro state animation!
    var boardZoom by remember { mutableStateOf(0.4f) }
    var showGameplaySettings by remember { mutableStateOf(false) }
    var showChatEmoteDialog by remember { mutableStateOf(false) }
    var renameTargetPlayer by remember { mutableStateOf<LudoPlayer?>(null) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            boardZoom = value
        }
    }

    // Glow shader pulse around dice block for active player's turn to roll
    val dicePulseTransition = rememberInfiniteTransition(label = "dicePulse")
    val diceGlowScale by dicePulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF261912)) // Deep Warm Chocolate Cocoa Backdrop for ultimate character contrast
    ) {
        if (backgroundPlayBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = backgroundPlayBitmap!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            // BEAUTIFUL DESSERT BACKDROP DECORATIONS
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Wafer Pillars in the background (as dark brown columns)
                // Left wafer column holding red lollipop
                drawRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(40f.dp.toPx(), h - 350f.dp.toPx()),
                    size = Size(24f.dp.toPx(), 350f.dp.toPx())
                )
                // Left chocolate dripping top
                drawCircle(
                    color = Color(0xFF3E2723),
                    radius = 16f.dp.toPx(),
                    center = Offset(52f.dp.toPx(), h - 350f.dp.toPx())
                )

                // Right wafer column holding green lollipop
                drawRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(w - 70f.dp.toPx(), h - 450f.dp.toPx()),
                    size = Size(20f.dp.toPx(), 450f.dp.toPx())
                )
                // Right chocolate dripping top
                drawCircle(
                    color = Color(0xFF3E2723),
                    radius = 14f.dp.toPx(),
                    center = Offset(w - 60f.dp.toPx(), h - 450f.dp.toPx())
                )

                // Draw spiral red/white lollipop on the left
                val leftLollipopCenter = Offset(52f.dp.toPx(), h - 360f.dp.toPx())
                val leftLollipopRadius = 50f.dp.toPx()
                drawCircle(Color.White, radius = leftLollipopRadius, center = leftLollipopCenter)
                // Draw spiral red lines
                for (i in 0..5) {
                    drawArc(
                        color = Color(0xFFE57373),
                        startAngle = (i * 60).toFloat(),
                        sweepAngle = 30f,
                        useCenter = true,
                        topLeft = Offset(leftLollipopCenter.x - leftLollipopRadius, leftLollipopCenter.y - leftLollipopRadius),
                        size = Size(leftLollipopRadius * 2, leftLollipopRadius * 2)
                    )
                }
                // Inner white swirl overlay
                drawCircle(Color.White, radius = leftLollipopRadius * 0.4f, center = leftLollipopCenter)
                drawCircle(Color(0xFFE57373), radius = leftLollipopRadius * 0.2f, center = leftLollipopCenter)

                // Draw spiral green/white lollipop on the top-right
                val rightLollipopCenter = Offset(w - 60f.dp.toPx(), h - 460f.dp.toPx())
                val rightLollipopRadius = 45f.dp.toPx()
                drawCircle(Color.White, radius = rightLollipopRadius, center = rightLollipopCenter)
                // Draw spiral green lines
                for (i in 0..5) {
                    drawArc(
                        color = Color(0xFF81C784),
                        startAngle = (i * 60 + 30).toFloat(),
                        sweepAngle = 30f,
                        useCenter = true,
                        topLeft = Offset(rightLollipopCenter.x - rightLollipopRadius, rightLollipopCenter.y - rightLollipopRadius),
                        size = Size(rightLollipopRadius * 2, rightLollipopRadius * 2)
                    )
                }
                // Inner white swirl overlay
                drawCircle(Color.White, radius = rightLollipopRadius * 0.4f, center = rightLollipopCenter)
                drawCircle(Color(0xFF81C784), radius = rightLollipopRadius * 0.2f, center = rightLollipopCenter)

                // Floating star cookies!
                drawPastelStarCookie(Offset(80f.dp.toPx(), h - 220f.dp.toPx()), 12f.dp.toPx())
                drawPastelStarCookie(Offset(100f.dp.toPx(), 60f.dp.toPx()), 15f.dp.toPx())
                drawPastelStarCookie(Offset(w - 50f.dp.toPx(), 180f.dp.toPx()), 14f.dp.toPx())
                drawPastelStarCookie(Offset(w - 140f.dp.toPx(), h - 180f.dp.toPx()), 18f.dp.toPx())
            }
        }

        // CENTER: ISOMETRIC BOARD AREA
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 96.dp, vertical = 6.dp) // More vertical space to avoid clipping of bases
                .graphicsLayer {
                    translationX = shakeX
                    translationY = shakeY
                },
            contentAlignment = Alignment.Center
        ) {
            // Rendering 2.5D Isometric Canvas
            LudoIsometricBoardCanvas(
                state = state,
                zoom = boardZoom,
                characterBitmaps = characterBitmaps,
                onPawnClicked = { pawnId ->
                    if (state.status == GameStateStatus.WAITING_FOR_MOVE && isMyTurn) {
                        viewModel.movePawn(pawnId)
                    }
                }
            )
        }

        // ==========================================
        // LANDSCAPE HUD OVERLAYS (MATCHING REFERENCE MOCKUP)
        // ==========================================

        // 1. TOP-LEFT CORNER: Green Player Card ("Suzanna" style with dinosaur mascot)
        state.players.find { it.color == PlayerColor.GREEN }?.let { player ->
            val finishedCount = state.pawns.count { it.color == PlayerColor.GREEN && it.stepCount == 56 }
            CutePlayerCard(
                player = player,
                isActive = activePlayer.color == PlayerColor.GREEN,
                timeLeft = state.turnTimeLeft,
                align = Alignment.TopStart,
                isGreenMascotStyle = true,
                finishedPawnsCount = finishedCount,
                characterBitmaps = characterBitmaps,
                onClick = { viewModel.showProfileStats(player) }
            )
        }

        // 2. BOTTOM-LEFT CORNER: Red Player Card (Strawberry Red style)
        state.players.find { it.color == PlayerColor.RED }?.let { player ->
            val finishedCount = state.pawns.count { it.color == PlayerColor.RED && it.stepCount == 56 }
            CutePlayerCard(
                player = player,
                isActive = activePlayer.color == PlayerColor.RED,
                timeLeft = state.turnTimeLeft,
                align = Alignment.BottomStart,
                finishedPawnsCount = finishedCount,
                characterBitmaps = characterBitmaps,
                onClick = { viewModel.showProfileStats(player) }
            )
        }

        // 3. TOP-RIGHT CORNER: Yellow Player Card (Custard Yellow style)
        state.players.find { it.color == PlayerColor.YELLOW }?.let { player ->
            val finishedCount = state.pawns.count { it.color == PlayerColor.YELLOW && it.stepCount == 56 }
            CutePlayerCard(
                player = player,
                isActive = activePlayer.color == PlayerColor.YELLOW,
                timeLeft = state.turnTimeLeft,
                align = Alignment.TopEnd,
                finishedPawnsCount = finishedCount,
                characterBitmaps = characterBitmaps,
                onClick = { viewModel.showProfileStats(player) }
            )
        }

        // 4. BOTTOM-RIGHT CORNER: Blue Player Card ("Guest5132" style with chicken mascot and badge)
        state.players.find { it.color == PlayerColor.BLUE }?.let { player ->
            val finishedCount = state.pawns.count { it.color == PlayerColor.BLUE && it.stepCount == 56 }
            CutePlayerCard(
                player = player,
                isActive = activePlayer.color == PlayerColor.BLUE,
                timeLeft = state.turnTimeLeft,
                align = Alignment.BottomEnd,
                isBlueGuestStyle = true,
                finishedPawnsCount = finishedCount,
                characterBitmaps = characterBitmaps,
                onClick = { viewModel.showProfileStats(player) }
            )
        }

        // 5. LEFT EDGE (MIDDLE): Cute wooden scalloped Back Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, top = 40.dp) // Offset down from green player
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier
                    .size(54.dp)
                    .shadow(6.dp, CircleShape)
                    .background(Color(0xFFE2C488), CircleShape) // Biscuit color
                    .border(3.dp, Color(0xFF8D6E63), CircleShape) // Chocolate border
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Exit", tint = Color(0xFF5D4037), modifier = Modifier.size(26.dp))
            }
        }

        // 6. RIGHT EDGE (TOP): Gear Settings Icon ⚙️
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 98.dp) // Positioned comfortably below the Yellow player card without overlapping!
        ) {
            IconButton(
                onClick = { showGameplaySettings = true }, // Opens actual Settings dialog
                modifier = Modifier
                    .size(46.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xFFFFD93D), CircleShape) // Scallop yellow
                    .border(2.5.dp, Color(0xFFEBC02D), CircleShape)
            ) {
                Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = Color(0xFF795548), modifier = Modifier.size(24.dp))
            }
        }

        // 7. RIGHT EDGE (MIDDLE): Beautiful Glowing Orange Scalloped Dice Badge with horizontal glowing line
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glowing yellow horizontal bar across the dice as in mockup
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(24.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFFFFEB3B).copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
            )

            // Pulse aura on active turn
            if (state.status == GameStateStatus.WAITING_FOR_ROLL && isMyTurn) {
                Box(
                    modifier = Modifier
                        .size((90 * diceGlowScale).dp)
                        .background(Color(0xFFFFEB3B).copy(alpha = 0.35f), CircleShape)
                )
            }

            // Outer Scalloped Orange Badge
            Box(
                modifier = Modifier
                    .size(86.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.width / 2f
                    drawCircle(Color(0xFFE67E22), radius = r) // Outer orange scallop
                    drawCircle(Color(0xFFF39C12), radius = r * 0.9f) // Mid orange
                    drawCircle(Color(0xFFFFEB3B).copy(alpha = 0.25f), radius = r * 0.8f) // Inner light
                }

                val activeDiceSkin = state.trialDice ?: state.selectedDice
                val (diceBodyBg, diceBorderColor, diceDotColor) = when (activeDiceSkin) {
                    "dice2" -> Triple(Color(0xFF4E2A1E), Color(0xFF8D6E63), Color(0xFFFFD54F))
                    "dice3" -> Triple(Color(0xFFFF80AB), Color(0xFFF50057), Color.White)
                    "dice4" -> Triple(Color(0xFF1A237E), Color(0xFF00E5FF), Color(0xFF00E5FF))
                    "dice5" -> Triple(Color(0xFFFFD700), Color(0xFFFF8F00), Color(0xFFD50000))
                    else -> Triple(Color(0xFFFFFDF0), Color(0xFFE2C488), Color(0xFF261204))
                }

                // Interactive Custom Dice Cookie
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(4.dp, RoundedCornerShape(10.dp))
                        .background(diceBodyBg, RoundedCornerShape(10.dp))
                        .border(2.5.dp, diceBorderColor, RoundedCornerShape(10.dp))
                        .clickable {
                            if (state.status == GameStateStatus.WAITING_FOR_ROLL && isMyTurn) {
                                viewModel.rollDice()
                            }
                        }
                        .testTag("dice_roll_block"),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isDiceRolling) {
                        CircularProgressIndicator(color = diceBorderColor, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                    } else {
                        DrawDiceFace(value = state.diceValue, dotColor = diceDotColor)
                    }
                }
            }
        }

        // 8. RIGHT EDGE (BOTTOM): Beautiful Chat & Emote Trigger Button
        IconButton(
            onClick = { showChatEmoteDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp) // Placed nicely above the Blue Card
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .background(Color(0xFF8D6E63), CircleShape)
                .border(2.dp, Color(0xFF5D4037), CircleShape)
        ) {
            Text("💬", fontSize = 24.sp)
        }

        // CỜ VINA & ARENA SPECIAL POWER-UP SKILL CARDS PANEL (SHIELD, REROLL, ROCKET)
        if (state.mode == GameMode.CO_VINA || state.mode == GameMode.ARENA) {
            val humanPlayerColor = PlayerColor.RED
            val shieldLeft = state.shieldCharges[humanPlayerColor] ?: 0
            val rerollLeft = state.rerollCharges[humanPlayerColor] ?: 0
            val rocketLeft = state.rocketCharges[humanPlayerColor] ?: 0
            val isHumanTurn = activePlayer.color == humanPlayerColor && !activePlayer.isBot

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 72.dp)
            ) {
                // 1. Shield Skill Card 🛡️
                val isShieldActive = state.shieldActivePlayers.contains(humanPlayerColor)
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(
                            if (isShieldActive) Color(0xFF4CAF50) else Color(0xFF1E88E5),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                        .clickable(enabled = shieldLeft > 0 && !isShieldActive) {
                            viewModel.useShieldSkill()
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (isShieldActive) "🛡️ Dùng" else "🛡️ Khiên", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("x$shieldLeft", fontSize = 10.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
                    }
                }

                // 2. Reroll Skill Card 🎲
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(
                            if (isHumanTurn && rerollLeft > 0) Color(0xFFFF9800) else Color.Gray,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                        .clickable(enabled = isHumanTurn && rerollLeft > 0) {
                            viewModel.useRerollSkill()
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🎲 Đổi Vận", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("x$rerollLeft", fontSize = 10.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
                    }
                }

                // 3. Rocket Skill Card 🚀
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(
                            if (isHumanTurn && rocketLeft > 0) Color(0xFFE91E63) else Color.Gray,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                        .clickable(enabled = isHumanTurn && rocketLeft > 0) {
                            viewModel.useRocketSkill()
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🚀 Rocket", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("x$rocketLeft", fontSize = 10.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // SUPER BUMP COMBO OVERLAY BANNER 🔥
        if (state.showSuperBumpBanner) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.dismissSuperBumpBanner() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6D00)),
                    modifier = Modifier
                        .padding(32.dp)
                        .border(3.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🔥 SUPER BUMP COMBO! 🔥", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Black)
                        Text(state.superBumpText, fontSize = 16.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Button(
                            onClick = { viewModel.dismissSuperBumpBanner() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000))
                        ) {
                            Text("NHẬN THƯỞNG 🍭", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }



        // 9. BOTTOM CENTER: Cute Dialog Speech Bubble displaying latest logs
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .width(320.dp)
                .height(44.dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFFF5D1D8), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.logs.lastOrNull() ?: (if (isVi) "Chào mừng bạn đến với Thế giới Bánh Ngọt!" else "Welcome to the Dessert World!"),
                fontSize = 11.sp,
                color = Color(0xFF4E342E),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }

        // 10. ACTIVE TURN POPUP BANNER ("NEXT TURN" / "LƯỢT TIẾP THEO")
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = state.bannerText.isNotEmpty() && state.status == GameStateStatus.WAITING_FOR_ROLL,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFD93D), RoundedCornerShape(16.dp))
                        .border(3.dp, Color(0xFFEBC02D), RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isVi) "LƯỢT TIẾP THEO" else "NEXT TURN",
                            color = Color(0xFF795548),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = state.bannerText,
                            color = Color(0xFF3E2723), // Dark cocoa chocolate for solid, high-contrast readability
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(
                                    color = Color.White.copy(alpha = 0.5f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 1f
                                )
                            )
                        )
                    }
                }
            }
        }

        // EMOTE OVERLAYS floating on top
        state.activeEmotes.forEach { activeEmote ->
            FloatingEmoteOverlay(activeEmote = activeEmote)
        }

        // PROFILE VIEW POPUP WITH STATS AND EMOTE GRID
        state.showProfileStatsPlayer?.let { profilePlayer ->
            Dialog(onDismissRequest = { viewModel.showProfileStats(null) }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xCCFFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xCCFFE0B2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                drawRect(
                                    color = Color(0xCCFFF3E0),
                                    size = size
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(profilePlayer.color.baseColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bitmap = characterBitmaps[profilePlayer.characterSkin]
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = profilePlayer.name,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        PlayerAvatar(
                                            color = profilePlayer.color,
                                            avatarId = profilePlayer.avatarId,
                                            size = 50.dp,
                                            fontSize = 32.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = profilePlayer.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4E342E)
                                    )
                                }
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        // Player Profile Metrics
                        val isHumanProfile = (!profilePlayer.isBot || profilePlayer.color == PlayerColor.RED)
                        val totalG = if (isHumanProfile) state.userTotalGames else profilePlayer.totalGames
                        val wonG = if (isHumanProfile) state.userWonGames else profilePlayer.wins
                        val calcRate = if (totalG > 0) (wonG * 100 / totalG) else 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MetricItem(label = if (isVi) "Tổng số trận" else "Games Played", value = totalG.toString())
                            MetricItem(label = if (isVi) "Số trận thắng" else "Wins", value = wonG.toString())
                            MetricItem(label = if (isVi) "Tỉ lệ thắng" else "Win Rate", value = "$calcRate%")
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        // EMOTE INTERACTIVE GRID UNDERNEATH METRICS
                        Text(
                            text = if (isVi) "Ném Biểu Cảm Cho Đối Thủ:" else "Throw Emote at Opponent:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8D6E63),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            EmoteType.values().forEach { emo ->
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .shadow(1.dp, CircleShape)
                                        .background(Color(0xFFFFF1F1), CircleShape)
                                        .clickable {
                                            viewModel.throwEmote(emo, profilePlayer.color)
                                            viewModel.showProfileStats(null)
                                        }
                                        .testTag("emote_${emo.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emo.symbol, fontSize = 24.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.showProfileStats(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isVi) "ĐÓNG" else "CLOSE")
                        }
                    }
                }
            }
        }

// (renameTargetPlayer removed)

        // CUSTOM EMOTE & QUICK CHAT SELECTION DIALOG (CONSOLIDATED & SMART DESIGN)
        if (showChatEmoteDialog) {
            Dialog(onDismissRequest = { showChatEmoteDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFD81B60))
                ) {
                    var customChatText by remember { mutableStateOf("") }

                    val dialogScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .fillMaxWidth()
                            .heightIn(max = 290.dp)
                            .verticalScroll(dialogScrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header with Close Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "💬 BIỂU CẢM & TRÒ CHUYỆN" else "💬 EMOTES & CHAT",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD81B60)
                            )
                            IconButton(
                                onClick = { showChatEmoteDialog = false },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFF5F5F5), CircleShape)
                            ) {
                                Text("❌", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Emotes Section (Single elegant row of all 6 interactive emojis with background glow)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "🎭 BIỂU CẢM NHANH:" else "🎭 QUICK EMOTE:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8D6E63),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val emotes = EmoteType.values()
                            val emoteColors = mapOf(
                                EmoteType.LAUGH to Pair(Color(0xFFFFF9C4), Color(0xFFFBC02D)), // Light yellow, yellow border
                                EmoteType.ANGRY to Pair(Color(0xFFFFCDD2), Color(0xFFE53935)), // Light red, red border
                                EmoteType.CRY to Pair(Color(0xFFE1F5FE), Color(0xFF1E88E5)),   // Light blue, blue border
                                EmoteType.LOVE to Pair(Color(0xFFFCE4EC), Color(0xFFD81B60)),  // Light pink, pink border
                                EmoteType.SLEEPY to Pair(Color(0xFFEDE7F6), Color(0xFF8E24AA)),// Light violet, purple border
                                EmoteType.APPLE to Pair(Color(0xFFE8F5E9), Color(0xFF43A047))  // Light green, green border
                            )

                            emotes.forEach { emo ->
                                val colors = emoteColors[emo] ?: Pair(Color(0xFFFFF1F1), Color(0xFFD81B60))
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.throwEmote(emo)
                                            showChatEmoteDialog = false
                                        }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(3.dp, CircleShape)
                                            .background(colors.first, CircleShape)
                                            .border(1.5.dp, colors.second, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emo.symbol, fontSize = 26.sp)
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                        // 2. Custom Input Bar (The Smart Field with Send action and instant feedback)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.OutlinedTextField(
                                value = customChatText,
                                onValueChange = { customChatText = it },
                                placeholder = {
                                    Text(
                                        text = if (isVi) "Nhập tin nhắn tùy chỉnh..." else "Type custom message...",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (customChatText.isNotEmpty()) {
                                        IconButton(onClick = { customChatText = "" }) {
                                            Text("❌", fontSize = 10.sp)
                                        }
                                    }
                                },
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD81B60),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (customChatText.isNotBlank()) {
                                        viewModel.throwEmoteWithChat(emote = EmoteType.LOVE, chatText = customChatText)
                                        customChatText = ""
                                        showChatEmoteDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text(
                                    text = if (isVi) "Gửi 🚀" else "Send 🚀",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                        // 3. Quick Chats List (Much taller, organized list of beautifully colored speech bubble cards)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "💬 MẪU CHAT NHANH (BẤM ĐỂ GỬI):" else "💬 QUICK CHAT TEMPLATES (TAP TO SEND):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8D6E63),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val chatCategories = listOf(
                            Triple(
                                if (isVi) "Vui vẻ 😄" else "Joy 😄",
                                EmoteType.LAUGH,
                                listOf(
                                    if (isVi) "Ha ha, quá tuyệt vời! 🎉" else "Haha, awesome! 🎉",
                                    if (isVi) "Đỉnh quá đi! 😎" else "So cool! 😎",
                                    if (isVi) "May mắn ghê nha! 🍀" else "So lucky! 🍀"
                                )
                            ),
                            Triple(
                                if (isVi) "Buồn bã 😭" else "Sad 😭",
                                EmoteType.CRY,
                                listOf(
                                    if (isVi) "Huhu, xui quá đi mất! 😭" else "Huhu, so unlucky! 😭",
                                    if (isVi) "Sao tớ đen thế nhỉ? 😢" else "Why am I so unfortunate? 😢",
                                    if (isVi) "Không công bằng tí nào! 💔" else "This is not fair! 💔"
                                )
                            ),
                            Triple(
                                if (isVi) "Tức giận 😡" else "Angry 😡",
                                EmoteType.ANGRY,
                                listOf(
                                    if (isVi) "Chờ đấy, tớ sẽ phục thù! 😡" else "Wait, I will get revenge! 😡",
                                    if (isVi) "Đừng có đùa với tớ! 😤" else "Don't play with me! 😤",
                                    if (isVi) "Chơi thế mà chơi à! 👿" else "Is that how you play?! 👿"
                                )
                            ),
                            Triple(
                                if (isVi) "Hài hước 🤣" else "Funny 🤣",
                                EmoteType.LAUGH,
                                listOf(
                                    if (isVi) "Chạy đi đâu con sâu! 🐛" else "Where are you running, little bug! 🐛",
                                    if (isVi) "Lêu lêu, bắt tớ đi nè! 😜" else "Teehee, catch me if you can! 😜",
                                    if (isVi) "Một bước lên mây luôn! 🚀" else "One step to the sky! 🚀"
                                )
                            )
                        )

                        // Styling for quick chat category bubbles
                        val categoryStyles = mapOf(
                            "Vui vẻ 😄" to Pair(Color(0xFFFFFDE7), Color(0xFFFFF59D)), // Yellow
                            "Joy 😄" to Pair(Color(0xFFFFFDE7), Color(0xFFFFF59D)),
                            "Buồn bã 😭" to Pair(Color(0xFFE3F2FD), Color(0xFF90CAF9)), // Blue
                            "Sad 😭" to Pair(Color(0xFFE3F2FD), Color(0xFF90CAF9)),
                            "Tức giận 😡" to Pair(Color(0xFFFFEBEE), Color(0xFFEF9A9A)), // Red
                            "Angry 😡" to Pair(Color(0xFFFFEBEE), Color(0xFFEF9A9A)),
                            "Hài hước 🤣" to Pair(Color(0xFFE8F5E9), Color(0xFFA5D6A7)), // Green
                            "Funny 🤣" to Pair(Color(0xFFE8F5E9), Color(0xFFA5D6A7))
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chatCategories.forEach { category ->
                                val catTitle = category.first
                                val catEmote = category.second
                                val phrases = category.third

                                val bubbleColors = categoryStyles[catTitle] ?: Pair(Color(0xFFF9F9F9), Color(0xFFE0E0E0))

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = catTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD81B60),
                                        modifier = Modifier.padding(start = 2.dp, bottom = 4.dp, top = 2.dp)
                                    )
                                    phrases.forEach { phrase ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                                .shadow(1.dp, RoundedCornerShape(10.dp))
                                                .background(bubbleColors.first, RoundedCornerShape(10.dp))
                                                .border(1.dp, bubbleColors.second, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    viewModel.throwEmoteWithChat(emote = catEmote, chatText = phrase)
                                                    showChatEmoteDialog = false
                                                }
                                                .padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = catEmote.symbol,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = phrase,
                                                fontSize = 13.sp,
                                                color = Color(0xFF4E342E),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showChatEmoteDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text(
                                text = if (isVi) "ĐÓNG CHAT" else "CLOSE CHAT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // GAMEPLAY SETTINGS DIALOG (BGM & SFX TOGGLES)
        if (showGameplaySettings) {
            Dialog(onDismissRequest = { showGameplaySettings = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isVi) "Cài Đặt Game" else "Settings",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4E342E)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Music toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "Nhạc Nền (BGM)" else "Background Music",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                            Switch(
                                checked = state.isMusicOn,
                                onCheckedChange = { viewModel.toggleMusic() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD81B60))
                            )
                        }

                        // Music Volume Slider
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = if (isVi) "Âm lượng nhạc: ${(state.musicVolume * 100).toInt()}%" else "Music Vol: ${(state.musicVolume * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8D6E63)
                            )
                            Slider(
                                value = state.musicVolume,
                                onValueChange = { viewModel.setMusicVolume(it) },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFD81B60),
                                    activeTrackColor = Color(0xFFD81B60)
                                )
                            )
                        }

                        // SFX toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "Âm Thanh Hiệu Ứng" else "Sound Effects (SFX)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                            Switch(
                                checked = state.isSfxOn,
                                onCheckedChange = { viewModel.toggleSfx() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD81B60))
                            )
                        }

                        // SFX Volume Slider
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = if (isVi) "Âm lượng hiệu ứng: ${(state.sfxVolume * 100).toInt()}%" else "SFX Vol: ${(state.sfxVolume * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8D6E63)
                            )
                            Slider(
                                value = state.sfxVolume,
                                onValueChange = { viewModel.setSfxVolume(it) },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFD81B60),
                                    activeTrackColor = Color(0xFFD81B60)
                                )
                            )
                        }

                        // Language toggler
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "Ngôn Ngữ" else "Language",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF5D4037)
                            )
                            Row {
                                Button(
                                    onClick = { viewModel.setLanguage("vi") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isVi) Color(0xFFD81B60) else Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text("VI", color = Color.White)
                                }
                                Button(
                                    onClick = { viewModel.setLanguage("en") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isVi) Color(0xFFD81B60) else Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("EN", color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showGameplaySettings = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isVi) "ĐÓNG" else "CLOSE")
                        }
                    }
                }
            }
        }

        // MATCH COMPLETED WIN DIALOG
        if (state.status == GameStateStatus.MATCH_ENDED) {
            Dialog(onDismissRequest = { viewModel.exitToMainMenu() }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👑", fontSize = 64.sp)
                        Text(
                            text = state.bannerText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD81B60),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.matchRewardText.isNotEmpty()) state.matchRewardText else (if (isVi) "Phần thưởng của bạn: +500 Vàng 🍭" else "Reward: +500 Gold 🍭"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.exitToMainMenu() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isVi) "QUAY LẠI SẢNH CHỜ" else "BACK TO MAIN MENU")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerHudItem(
    player: LudoPlayer,
    activePlayer: LudoPlayer,
    timeLeft: Int,
    onClick: () -> Unit
) {
    val isActive = activePlayer.color == player.color
    val baseColor = player.color.sweetBaseColor()
    val accentColor = player.color.sweetAccentColor()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(if (isActive) 8.dp else 3.dp, RoundedCornerShape(16.dp))
            .background(accentColor, RoundedCornerShape(16.dp))
            .border(
                width = if (isActive) 3.dp else 2.dp,
                color = baseColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                CircularProgressIndicator(
                    progress = { timeLeft / 30f },
                    modifier = Modifier.fillMaxSize(),
                    color = baseColor,
                    strokeWidth = 3.dp
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.5.dp, baseColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                PlayerAvatar(
                    color = player.color,
                    avatarId = player.avatarId,
                    size = 28.dp,
                    fontSize = 18.sp,
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = player.name,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = baseColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CutePlayerCard(
    player: LudoPlayer,
    isActive: Boolean,
    timeLeft: Int,
    align: Alignment,
    isGreenMascotStyle: Boolean = false,
    isBlueGuestStyle: Boolean = false,
    finishedPawnsCount: Int = 0,
    characterBitmaps: Map<String, ImageBitmap> = emptyMap(),
    onClick: () -> Unit
) {
    // High-contrast rich dark sweet-fudge backgrounds for perfect white text contrast!
    val cardBg = when (player.color) {
        PlayerColor.RED -> Color(0xFF6B1D20) // Rich dark cherry/strawberry
        PlayerColor.GREEN -> Color(0xFF1E4620) // Rich dark matcha green
        PlayerColor.YELLOW -> Color(0xFF5F4504) // Rich dark honey/caramel
        PlayerColor.BLUE -> Color(0xFF13325B) // Rich dark blueberry syrup
    }
    val cardBorder = when (player.color) {
        PlayerColor.RED -> Color(0xFFFF8A80)
        PlayerColor.GREEN -> Color(0xFF81C784)
        PlayerColor.YELLOW -> Color(0xFFFFD54F)
        PlayerColor.BLUE -> Color(0xFF64B5F6)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = align
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .shadow(if (isActive) 10.dp else 3.dp, RoundedCornerShape(20.dp))
                .background(cardBg, RoundedCornerShape(20.dp))
                .border(
                    width = if (isActive) 3.5.dp else 2.5.dp,
                    color = if (isActive) Color.White else cardBorder,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "avatarPulse")
            val avatarScale by if (isActive) {
                infiniteTransition.animateFloat(
                    initialValue = 0.96f,
                    targetValue = 1.14f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "avatarScale"
                )
            } else {
                remember { androidx.compose.runtime.mutableStateOf(1.0f) }
            }

            val glowAlpha by if (isActive) {
                infiniteTransition.animateFloat(
                    initialValue = 0.25f,
                    targetValue = 0.75f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowAlpha"
                )
            } else {
                remember { androidx.compose.runtime.mutableStateOf(0f) }
            }

            // Square avatar box
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    // Pulsating glowing halo matching the player's primary border color!
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = avatarScale * 1.25f
                                scaleY = avatarScale * 1.25f
                                alpha = glowAlpha
                            }
                            .size(38.dp)
                            .background(cardBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    )
                }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = avatarScale
                            scaleY = avatarScale
                        }
                        .size(42.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(
                            if (isActive) 3.dp else 2.dp,
                            if (isActive) Color.White else cardBorder,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Circular active turn indicator wrapping around the avatar
                    if (isActive) {
                        val maxTime = if (timeLeft <= 5) 5f else 15f
                        CircularProgressIndicator(
                            progress = { (timeLeft / maxTime).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxSize().padding(2.dp),
                            color = cardBorder,
                            strokeWidth = 3.dp
                        )
                    }

                    // Load custom character images with safe fallback to emojis
                    val bitmap = characterBitmaps[player.characterSkin]
                    if (bitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap,
                            contentDescription = player.name,
                            modifier = Modifier.size(34.dp)
                        )
                    } else {
                        PlayerAvatar(
                            color = player.color,
                            avatarId = -1, // Use color-specific themed default if custom image fails
                            size = 36.dp,
                            fontSize = 24.sp,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // White Player Name
            val displayNameShown = if (player.name.isNotBlank()) player.name else "Bạn"
            Text(
                text = displayNameShown,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.padding(end = 4.dp),
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(1.5f, 1.5f),
                        blurRadius = 1f
                    )
                )
            )

            // Dynamic badge showing exact count of pawns that reached home for this player
            if (finishedPawnsCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFFFD93D), CircleShape)
                        .border(1.2.dp, Color(0xFFEBC02D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$finishedPawnsCount",
                        color = Color(0xFF795548),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun DrawDiceFace(value: Int, dotColor: Color = Color(0xFF261204)) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val dotRadius = size.width * 0.11f
        val offset = size.width * 0.24f

        // Helper to draw a high-end 3D glossy spherical dot
        fun drawGlossyDot(x: Float, y: Float) {
            // 1. Drop shadow under the dot
            drawCircle(
                color = Color(0x3D000000),
                radius = dotRadius * 1.15f,
                center = Offset(x + 1f, y + 1.5f)
            )
            // 2. Spherical core
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(x, y)
            )
            // 3. 3D Volume inner lighting
            drawCircle(
                color = Color.White.copy(alpha = 0.25f),
                radius = dotRadius * 0.8f,
                center = Offset(x + dotRadius * 0.1f, y + dotRadius * 0.1f)
            )
            // 4. Strong glossy highlight reflection (white spot on top-left)
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = dotRadius * 0.28f,
                center = Offset(x - dotRadius * 0.35f, y - dotRadius * 0.35f)
            )
        }

        when (value) {
            1 -> drawGlossyDot(cx, cy)
            2 -> {
                drawGlossyDot(cx - offset, cy - offset)
                drawGlossyDot(cx + offset, cy + offset)
            }
            3 -> {
                drawGlossyDot(cx - offset, cy - offset)
                drawGlossyDot(cx, cy)
                drawGlossyDot(cx + offset, cy + offset)
            }
            4 -> {
                drawGlossyDot(cx - offset, cy - offset)
                drawGlossyDot(cx + offset, cy - offset)
                drawGlossyDot(cx - offset, cy + offset)
                drawGlossyDot(cx + offset, cy + offset)
            }
            5 -> {
                drawGlossyDot(cx - offset, cy - offset)
                drawGlossyDot(cx + offset, cy - offset)
                drawGlossyDot(cx, cy)
                drawGlossyDot(cx - offset, cy + offset)
                drawGlossyDot(cx + offset, cy + offset)
            }
            6 -> {
                drawGlossyDot(cx - offset, cy - offset)
                drawGlossyDot(cx + offset, cy - offset)
                drawGlossyDot(cx - offset, cy)
                drawGlossyDot(cx + offset, cy)
                drawGlossyDot(cx - offset, cy + offset)
                drawGlossyDot(cx + offset, cy + offset)
            }
        }
    }
}

@Composable
fun FloatingEmoteOverlay(activeEmote: ActiveEmote) {
    val align = when (activeEmote.playerColor) {
        PlayerColor.GREEN -> Alignment.TopStart
        PlayerColor.RED -> Alignment.BottomStart
        PlayerColor.YELLOW -> Alignment.TopEnd
        PlayerColor.BLUE -> Alignment.BottomEnd
    }

    val offsetX = when (activeEmote.playerColor) {
        PlayerColor.GREEN -> 160.dp
        PlayerColor.RED -> 160.dp
        PlayerColor.YELLOW -> (-160).dp
        PlayerColor.BLUE -> (-160).dp
    }
    
    val offsetY = when (activeEmote.playerColor) {
        PlayerColor.GREEN -> 24.dp
        PlayerColor.RED -> (-24).dp
        PlayerColor.YELLOW -> 24.dp
        PlayerColor.BLUE -> (-24).dp
    }

    val animatedY = remember { Animatable(0f) }
    val animatedAlpha = remember { Animatable(1.0f) }

    LaunchedEffect(Unit) {
        launch {
            animatedY.animateTo(
                targetValue = -40f,
                animationSpec = tween(2500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(1500)
            animatedAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(1000)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = align
    ) {
        Box(
            modifier = Modifier
                .offset(x = offsetX, y = offsetY + animatedY.value.dp)
                .graphicsLayer(alpha = animatedAlpha.value)
                .shadow(6.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(2.5.dp, activeEmote.playerColor.baseColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(activeEmote.emote.symbol, fontSize = 28.sp)
                if (!activeEmote.chatText.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeEmote.chatText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4E342E),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFD81B60))
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

// ==========================================
// CANVASES & MATH FOR 2.5D ISOMETRIC GAME BOARD
// ==========================================

fun getIsometricCoords(
    x: Float,
    y: Float,
    z: Float,
    canvasWidth: Float,
    canvasHeight: Float,
    zoom: Float
): Offset {
    val centerX = canvasWidth / 2f
    val centerY = canvasHeight / 2f
    
    // Constraint board size by height in landscape mode to prevent clipping at the top/bottom
    val boardDim = minOf(canvasWidth, canvasHeight * 1.55f)
    val tileWidth = (boardDim / 27.5f) * zoom
    val tileHeight = tileWidth * 0.58f

    // Center coordinates at index (7, 7)
    val cx = x - 7.0f
    val cy = y - 7.0f

    // Isometric math projection:
    // Screen X = center + (cx - cy) * tileWidth
    // Screen Y = center + (cx + cy) * tileHeight - z * heightOffset
    val screenX = centerX + (cx - cy) * tileWidth
    val screenY = centerY + (cx + cy) * tileHeight - z * tileHeight * 1.3f

    return Offset(screenX, screenY)
}

@Composable
fun LudoIsometricBoardCanvas(
    state: GameState,
    zoom: Float,
    characterBitmaps: Map<String, ImageBitmap>,
    onPawnClicked: (Int) -> Unit
) {
    val pawns = state.pawns
    val activePlayerIndex = state.activePlayerIndex
    val activePlayer = state.players.getOrNull(activePlayerIndex)

    val infiniteTransition = rememberInfiniteTransition(label = "pawnArrowBob")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    val idleTransition = rememberInfiniteTransition(label = "pawnIdle")
    val idleBreathScaleY by idleTransition.animateFloat(
        initialValue = 1.00f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleBreathScaleY"
    )
    val idleFloatZ by idleTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleFloatZ"
    )

    // Interactive clicking overlay detection
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(pawns, state.status, state.activePlayerIndex, state.myPlayerId) {
                    detectTapGestures { offset ->
                        // Detect if a pawn of active player was clicked
                        if (state.status == GameStateStatus.WAITING_FOR_MOVE && activePlayer != null) {
                            var clickedPawnId: Int? = null
                            var minDistance = 35000f // max touch radius in pixels sq (~187px radius)

                            pawns.filter { it.color == activePlayer.color }.forEach { p ->
                                val coord = getPawnVisualCoords(p)
                                if (coord != null) {
                                    val scrCoord = getIsometricCoords(coord.first, coord.second, coord.third, width, height, zoom)
                                    // Character body stands upright above feet (scrCoord.y)
                                    val charCenter = Offset(scrCoord.x, scrCoord.y - 45f * zoom)
                                    val dist = (offset - charCenter).getDistanceSquared()
                                    if (dist < minDistance) {
                                        minDistance = dist
                                        clickedPawnId = p.id
                                    }
                                }
                             }

                            clickedPawnId?.let { id ->
                                onPawnClicked(id)
                            }
                        }
                    }
                }
        ) {
            // 1. DRAW GIANT CAKE BOARD LAYERS (BACKGROUND 3D STRUCTURE)
            drawGiantCakeBoard(width, height, zoom)

            // 2. DRAW TRACK TILES AND CENTRAL GOAL PEDESTAL IN ISOMETRIC Z-ORDER
            drawTrackTilesAndCages(pawns, width, height, zoom, state)

            // 3. DRAW THE PAWNS (CUTE HOPPING RETRO CHARACTERS WITH IDLE ANIMATION AND DUST VFX)
            drawCutePawns(pawns, state, width, height, zoom, characterBitmaps, bobOffset, idleBreathScaleY, idleFloatZ)
        }
    }
}

private fun getPawnVisualCoords(pawn: Pawn): Triple<Float, Float, Float>? {
    val color = pawn.color
    if (pawn.stepCount == -1) {
        val basePos = LudoBoardConfig.basePawnPositions[color]?.getOrNull(pawn.id) ?: return null
        return Triple(basePos.first, basePos.second, 0.4f)
    }

    if (pawn.stepCount == 56) {
        val c = LudoBoardConfig.centerHomes[color] ?: return null
        return Triple(c.first.toFloat(), c.second.toFloat(), 1.0f)
    }

    if (pawn.stepCount in 0..50) {
        val startIndex = LudoBoardConfig.playerStartTrackIndex[color] ?: 0
        val idx = (startIndex + pawn.stepCount) % 52
        val c = LudoBoardConfig.outerTrack[idx]
        return Triple(c.first.toFloat(), c.second.toFloat(), 0.12f)
    }

    if (pawn.stepCount in 51..55) {
        val c = LudoBoardConfig.homePaths[color]?.getOrNull(pawn.stepCount - 51) ?: return null
        return Triple(c.first.toFloat(), c.second.toFloat(), 0.12f)
    }

    return null
}

// ==========================================
// COLOR AND SHAPE HELPERS FOR DESSERT THEME
// ==========================================

fun PlayerColor.sweetBaseColor(): Color = when (this) {
    PlayerColor.RED -> Color(0xFFE53935) // High-contrast richer cherry red
    PlayerColor.GREEN -> Color(0xFF2E7D32) // High-contrast richer matcha green
    PlayerColor.YELLOW -> Color(0xFFFBC02D) // High-contrast richer custard honey
    PlayerColor.BLUE -> Color(0xFF1565C0) // High-contrast richer blueberry jam
}

fun PlayerColor.sweetAccentColor(): Color = when (this) {
    PlayerColor.RED -> Color(0xFFFFCDD2) // Soft pink cream
    PlayerColor.GREEN -> Color(0xFFC8E6C9) // Soft matcha cream
    PlayerColor.YELLOW -> Color(0xFFFFF9C4) // Soft vanilla cream
    PlayerColor.BLUE -> Color(0xFFBBDEFB) // Soft blueberry cream
}

fun DrawScope.drawPastelStarCookie(center: Offset, radius: Float) {
    val path = Path()
    for (i in 0..9) {
        val r = if (i % 2 == 0) radius else radius * 0.45f
        val angle = i * 36 - 90
        val x = center.x + r * cos(angle * Math.PI / 180.0).toFloat()
        val y = center.y + r * sin(angle * Math.PI / 180.0).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    drawPath(path, SolidColor(Color(0xFFFFF0B5)))
    drawPath(path, SolidColor(Color(0xFFEBC02D)), style = Stroke(width = 2.5f.dp.toPx()))
}

fun DrawScope.drawCakeBasePedestal(
    centerX: Float,
    centerY: Float,
    w: Float,
    h: Float,
    color: PlayerColor
) {
    val baseColor = color.sweetBaseColor()
    val accentColor = color.sweetAccentColor()

    // Layer 1: Bottom Chocolate/Wafer Biscuit layer
    drawIsometricSlab(
        centerX = centerX,
        centerY = centerY + 14f,
        w = w,
        h = h,
        thickness = 10f,
        topBrush = SolidColor(Color(0xFF8D6E63)),
        leftBrush = SolidColor(Color(0xFF5D4037)),
        rightBrush = SolidColor(Color(0xFF4E342E))
    )

    // Layer 2: Dripping white cream layer
    drawIsometricSlab(
        centerX = centerX,
        centerY = centerY + 6f,
        w = w * 0.96f,
        h = h * 0.96f,
        thickness = 8f,
        topBrush = SolidColor(Color.White),
        leftBrush = SolidColor(Color(0xFFEEEEEE)),
        rightBrush = SolidColor(Color(0xFFDDDDDD))
    )
    
    // Draw 3D dripping circular drops on the left/right front sides
    val leftDropCount = 4
    for (i in 0..leftDropCount) {
        val t = i.toFloat() / leftDropCount
        val p3 = Offset(centerX - w * 0.48f, centerY + 6f)
        val p2 = Offset(centerX, centerY + h * 0.48f + 6f)
        val dx = p3.x + (p2.x - p3.x) * t
        val dy = p3.y + (p2.y - p3.y) * t + 8f
        drawCircle(Color(0xFFEEEEEE), radius = w * 0.05f, center = Offset(dx, dy))
    }
    val rightDropCount = 4
    for (i in 0..rightDropCount) {
        val t = i.toFloat() / rightDropCount
        val p2 = Offset(centerX, centerY + h * 0.48f + 6f)
        val p1 = Offset(centerX + w * 0.48f, centerY + 6f)
        val dx = p2.x + (p1.x - p2.x) * t
        val dy = p2.y + (p1.y - p2.y) * t + 8f
        drawCircle(Color(0xFFDDDDDD), radius = w * 0.05f, center = Offset(dx, dy))
    }

    // Layer 3: Top icing layer (colored)
    drawIsometricSlab(
        centerX = centerX,
        centerY = centerY,
        w = w * 0.92f,
        h = h * 0.92f,
        thickness = 6f,
        topBrush = SolidColor(accentColor),
        leftBrush = SolidColor(baseColor.darken(0.1f)),
        rightBrush = SolidColor(baseColor.darken(0.2f))
    )
}

fun DrawScope.drawGiantCakeBoard(width: Float, height: Float, zoom: Float) {
    val boardDim = minOf(width, height * 1.55f)
    val tw = (boardDim / 27.5f) * zoom
    val th = tw * 0.58f

    // Bottom Layer: giant base biscuit slab (covers the entire 15x15 tile grid comfortably with safety margin)
    val bottomSlabBrush = Brush.verticalGradient(listOf(Color(0xFF8D6E63), Color(0xFF5D4037)))
    val bottomSlabSideLeft = Brush.verticalGradient(listOf(Color(0xFF5D4037), Color(0xFF4E342E)))
    val bottomSlabSideRight = Brush.verticalGradient(listOf(Color(0xFF4E342E), Color(0xFF3E2723)))

    drawIsometricSlab(
        centerX = width / 2f,
        centerY = height / 2f + 25f, // Centered slightly better vertically
        w = tw * 32.5f,
        h = th * 32.5f,
        thickness = 26f,
        topBrush = bottomSlabBrush,
        leftBrush = bottomSlabSideLeft,
        rightBrush = bottomSlabSideRight
    )

    // Middle Layer: cream cake slab
    val midSlabBrush = Brush.verticalGradient(listOf(Color(0xFFFFF9E6), Color(0xFFFFF0D0)))
    val midSlabSideLeft = Brush.verticalGradient(listOf(Color(0xFFE2C488), Color(0xFFC2A468)))
    val midSlabSideRight = Brush.verticalGradient(listOf(Color(0xFFC2A468), Color(0xFFA28448)))

    drawIsometricSlab(
        centerX = width / 2f,
        centerY = height / 2f + 5f, // Centered slightly better vertically
        w = tw * 30.5f,
        h = th * 30.5f,
        thickness = 22f,
        topBrush = midSlabBrush,
        leftBrush = midSlabSideLeft,
        rightBrush = midSlabSideRight
    )
}

// UNIFIED DRAWABLE TILE ITEM FOR 3D ISOMETRIC Z-SORTING (PAINTER'S ALGORITHM)
// Sorting by (gridX + gridY) ensures back tiles (smaller screen Y) are drawn FIRST,
// and front tiles (larger screen Y) are drawn LATER, completely preventing back tiles from overlapping front tiles!
sealed class TileItem(val sortKey: Float) {
    data class Outer(val index: Int, val gridX: Float, val gridY: Float) : TileItem(gridX + gridY)
    data class Base(val color: PlayerColor, val gridX: Float, val gridY: Float) : TileItem(gridX + gridY)
    data class Home(val color: PlayerColor, val index: Int, val gridX: Float, val gridY: Float) : TileItem(gridX + gridY)
    data class CenterGoal(val gridX: Float = 7.0f, val gridY: Float = 7.0f) : TileItem(gridX + gridY + 0.05f)
}

fun DrawScope.drawTrackTilesAndCages(pawns: List<Pawn>, width: Float, height: Float, zoom: Float, state: GameState) {
    val boardDim = minOf(width, height * 1.55f)
    val tw = (boardDim / 27.5f) * zoom
    val th = tw * 0.58f

    // Helper to calculate tile compression sink offset when a pawn lands/hops on a tile
    fun getTileSinkY(gridX: Float, gridY: Float): Float {
        val hoppingPawn = pawns.find { p ->
            if (p.isBumping || !p.isHopping) return@find false
            val visualCoords = getPawnVisualCoords(p) ?: return@find false
            val px = visualCoords.first
            val py = visualCoords.second
            kotlin.math.abs(px - gridX) < 0.2f && kotlin.math.abs(py - gridY) < 0.2f
        } ?: return 0f

        val prog = hoppingPawn.hopProgress
        return when {
            prog < 0.25f -> (sin(prog / 0.25f * Math.PI).toFloat()) * 5f // Takeoff compression
            prog > 0.70f -> (sin((prog - 0.70f) / 0.30f * Math.PI).toFloat()) * 9f // Landing impact compression!
            else -> 0f
        }
    }

    val tileItems = mutableListOf<TileItem>()

    // 1. Collect outer track tiles
    LudoBoardConfig.outerTrack.forEachIndexed { index, coord ->
        tileItems.add(TileItem.Outer(index, coord.first.toFloat(), coord.second.toFloat()))
    }

    // 2. Collect corner bases
    PlayerColor.values().forEach { color ->
        val baseArea = when (color) {
            PlayerColor.RED -> Pair(11.5f, 11.5f)
            PlayerColor.GREEN -> Pair(2.5f, 11.5f)
            PlayerColor.YELLOW -> Pair(2.5f, 2.5f)
            PlayerColor.BLUE -> Pair(11.5f, 2.5f)
        }
        tileItems.add(TileItem.Base(color, baseArea.first, baseArea.second))
    }

    // 3. Collect home path tiles
    PlayerColor.values().forEach { color ->
        val path = LudoBoardConfig.homePaths[color] ?: return@forEach
        path.forEachIndexed { index, coord ->
            tileItems.add(TileItem.Home(color, index, coord.first.toFloat(), coord.second.toFloat()))
        }
    }

    // 4. Collect Center Goal Pedestal
    tileItems.add(TileItem.CenterGoal())

    // SORT ALL TILES BACK-TO-FRONT BY ISOMETRIC DEPTH (gridX + gridY)
    val sortedTileItems = tileItems.sortedBy { it.sortKey }

    // RENDER TILES IN PRECISE ISOMETRIC Z-ORDER
    sortedTileItems.forEach { tile ->
        when (tile) {
            is TileItem.Outer -> {
                val index = tile.index
                val isSafe = LudoBoardConfig.safeSpotIndices.contains(index)
                val center = getIsometricCoords(tile.gridX, tile.gridY, 0.08f, width, height, zoom)
                val sinkY = getTileSinkY(tile.gridX, tile.gridY)

                if (isSafe) {
                    val flowerColor = when (index) {
                        1, 47 -> PlayerColor.GREEN.sweetBaseColor()
                        8, 14 -> PlayerColor.YELLOW.sweetBaseColor()
                        21, 27 -> PlayerColor.BLUE.sweetBaseColor()
                        34, 40 -> PlayerColor.RED.sweetBaseColor()
                        else -> Color(0xFF6BCB77)
                    }
                    drawIsometricFlower(center.x, center.y + sinkY, tw * 1.35f, th * 1.35f, flowerColor)
                } else if (index == 12 || index == 38) {
                    // Teleport Portal tile 🌀
                    drawIsometricBiscuit(center.x, center.y + sinkY, tw * 1.35f, th * 1.35f, Color(0xFFE040FB))
                    drawIsometricFlower(center.x, center.y + sinkY, tw * 0.9f, th * 0.9f, Color(0xFF7C4DFF))
                } else {
                    // Neutral delicious cream biscuit tile 🍪
                    drawIsometricBiscuit(center.x, center.y + sinkY, tw * 1.35f, th * 1.35f, Color(0xFFFFF9C4))
                }
            }
            is TileItem.Base -> {
                val color = tile.color
                val baseCenter = getIsometricCoords(tile.gridX, tile.gridY, 0.4f, width, height, zoom)
                drawCakeBasePedestal(
                    centerX = baseCenter.x,
                    centerY = baseCenter.y,
                    w = tw * 5.2f,
                    h = th * 5.2f,
                    color = color
                )

                // Prominent Arrow tile next to base
                val startIdx = LudoBoardConfig.playerStartTrackIndex[color] ?: 0
                val deployCoord = LudoBoardConfig.outerTrack[startIdx]
                val arrowCenter = getIsometricCoords(deployCoord.first.toFloat(), deployCoord.second.toFloat(), 0.1f, width, height, zoom)
                val arrowSinkY = getTileSinkY(deployCoord.first.toFloat(), deployCoord.second.toFloat())
                drawIsometricBiscuit(arrowCenter.x, arrowCenter.y + arrowSinkY, tw * 1.35f, th * 1.35f, Color(0xFFFFF9C4))
                drawIsometricArrow(arrowCenter.x, arrowCenter.y + arrowSinkY, tw * 1.15f, th * 1.15f, color.sweetBaseColor())
            }
            is TileItem.Home -> {
                val color = tile.color
                val heightLevel = 0.12f
                val center = getIsometricCoords(tile.gridX, tile.gridY, heightLevel, width, height, zoom)
                val sinkY = getTileSinkY(tile.gridX, tile.gridY)
                
                // Draw vibrant 3D home tile
                drawIsometricHomeTile(center.x, center.y + sinkY, tw * 1.35f, th * 1.35f, color)
            }
            is TileItem.CenterGoal -> {
                drawGoalPortalCenter(state, width, height, zoom)
            }
        }
    }
}

fun DrawScope.drawIsometricHomeTile(cx: Float, cy: Float, w: Float, h: Float, color: PlayerColor) {
    val (topColor, sideColorDark, sideColorLight) = when (color) {
        PlayerColor.RED -> Triple(Color(0xFFFF3366), Color(0xFFC2185B), Color(0xFFE91E63))
        PlayerColor.GREEN -> Triple(Color(0xFF4CAF50), Color(0xFF1B5E20), Color(0xFF2E7D32))
        PlayerColor.YELLOW -> Triple(Color(0xFFFFB300), Color(0xFFE65100), Color(0xFFF57F17))
        PlayerColor.BLUE -> Triple(Color(0xFF29B6F6), Color(0xFF0D47A1), Color(0xFF1565C0))
    }

    // 1. Draw 3D isometric biscuit slab for home path
    drawIsometricSlab(
        centerX = cx,
        centerY = cy,
        w = w,
        h = h,
        thickness = 16f,
        topBrush = SolidColor(topColor),
        leftBrush = SolidColor(sideColorDark),
        rightBrush = SolidColor(sideColorLight)
    )
}

fun DrawScope.drawGoalPortalCenter(state: GameState, width: Float, height: Float, zoom: Float) {
    val boardDim = minOf(width, height * 1.55f)
    val tw = (boardDim / 27.5f) * zoom
    val th = tw * 0.58f
    val center = getIsometricCoords(7.0f, 7.0f, 0.7f, width, height, zoom)

    // Tier 1 (Base giant chocolate/cake slab): w = tw * 5.8f, h = th * 5.8f
    drawIsometricSlab(
        centerX = center.x,
        centerY = center.y + 12f,
        w = tw * 5.8f,
        h = th * 5.8f,
        thickness = 22f,
        topBrush = Brush.verticalGradient(listOf(Color(0xFF8D6E63), Color(0xFF5D4037))),
        leftBrush = SolidColor(Color(0xFF4E342E)),
        rightBrush = SolidColor(Color(0xFF3E2723))
    )

    // Tier 2 (Cream icing cake slab): w = tw * 4.8f, h = th * 4.8f
    drawIsometricSlab(
        centerX = center.x,
        centerY = center.y + 4f,
        w = tw * 4.8f,
        h = th * 4.8f,
        thickness = 18f,
        topBrush = Brush.verticalGradient(listOf(Color(0xFFFFFDF0), Color(0xFFFFF59D))),
        leftBrush = SolidColor(Color(0xFFE2C488)),
        rightBrush = SolidColor(Color(0xFFC2A468))
    )

    // Tier 3 (Golden Trophy Crown Pedestal): w = tw * 3.8f, h = th * 3.8f
    drawIsometricSlab(
        centerX = center.x,
        centerY = center.y - 4f,
        w = tw * 3.8f,
        h = th * 3.8f,
        thickness = 14f,
        topBrush = Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))),
        leftBrush = SolidColor(Color(0xFFE65100)),
        rightBrush = SolidColor(Color(0xFFBF360C))
    )

    // Sparkling Golden Victory Star in Center!
    drawPastelStarCookie(Offset(center.x, center.y - 4f), tw * 0.9f)

    // 4 Colored Home Spot Landing Circles with glowing white borders
    val r = tw * 0.28f
    val offsetDistW = tw * 1.55f
    val offsetDistH = th * 1.55f

    // GREEN
    val gCenter = Offset(center.x - offsetDistW, center.y)
    drawCircle(PlayerColor.GREEN.sweetBaseColor(), radius = r, center = gCenter)
    drawCircle(Color.White, radius = r, center = gCenter, style = Stroke(width = 3f))

    // YELLOW
    val yCenter = Offset(center.x, center.y - offsetDistH)
    drawCircle(PlayerColor.YELLOW.sweetBaseColor(), radius = r, center = yCenter)
    drawCircle(Color.White, radius = r, center = yCenter, style = Stroke(width = 3f))

    // BLUE
    val bCenter = Offset(center.x + offsetDistW, center.y)
    drawCircle(PlayerColor.BLUE.sweetBaseColor(), radius = r, center = bCenter)
    drawCircle(Color.White, radius = r, center = bCenter, style = Stroke(width = 3f))

    // RED
    val rCenter = Offset(center.x, center.y + offsetDistH)
    drawCircle(PlayerColor.RED.sweetBaseColor(), radius = r, center = rCenter)
    drawCircle(Color.White, radius = r, center = rCenter, style = Stroke(width = 3f))

    // DRAW THE DYNAMIC GLOWING SPACE PORTAL DOOR IF ANIMATING AT THE CENTER OF ARENA!
    val portalPawnColor = state.portalPawnColor
    if (portalPawnColor != null) {
        val portalCenter = getIsometricCoords(7.0f, 7.0f, 0.3f, width, height, zoom)
            val progress = state.portalProgress
            val scale = if (progress < 0.3f) (progress / 0.3f) else if (progress > 0.8f) ((1.0f - progress) / 0.2f) else 1.0f
            val portalW = tw * 1.6f * scale
            val portalH = th * 2.6f * scale

            if (scale > 0.05f) {
                // Upright capsule space door centered precisely over destination home
                drawRoundRect(
                    color = Color(0xFF1A0033).copy(alpha = 0.88f * scale.coerceIn(0f, 1f)),
                    topLeft = Offset(portalCenter.x - portalW / 2, portalCenter.y - portalH / 2),
                    size = Size(portalW, portalH),
                    cornerRadius = CornerRadius(portalW / 2, portalW / 2)
                )
                // Glowing neon magenta border
                drawRoundRect(
                    color = Color(0xFFFF007F).copy(alpha = scale.coerceIn(0f, 1f)),
                    topLeft = Offset(portalCenter.x - portalW / 2, portalCenter.y - portalH / 2),
                    size = Size(portalW, portalH),
                    cornerRadius = CornerRadius(portalW / 2, portalW / 2),
                    style = Stroke(width = 6f * scale)
                )
                // Innermost swirling cyan ring
                drawRoundRect(
                    color = Color(0xFF00FFFF).copy(alpha = scale.coerceIn(0f, 1f)),
                    topLeft = Offset(portalCenter.x - portalW * 0.35f, portalCenter.y - portalH * 0.35f),
                    size = Size(portalW * 0.7f, portalH * 0.7f),
                    cornerRadius = CornerRadius(portalW * 0.35f, portalW * 0.35f),
                    style = Stroke(width = 3f * scale)
                )
            }
        }
    }

sealed class LudoTileKey {
    data class Outer(val index: Int) : LudoTileKey()
    data class Home(val color: PlayerColor, val step: Int) : LudoTileKey()
    data class Base(val color: PlayerColor, val id: Int) : LudoTileKey()
}

fun getPawnTileKey(pawn: Pawn): LudoTileKey? {
    if (pawn.stepCount == -1) {
        return LudoTileKey.Base(pawn.color, pawn.id)
    }
    if (pawn.stepCount in 0..50) {
        val startIndex = LudoBoardConfig.playerStartTrackIndex[pawn.color] ?: 0
        val idx = (startIndex + pawn.stepCount) % 52
        return LudoTileKey.Outer(idx)
    }
    if (pawn.stepCount in 51..55) {
        return LudoTileKey.Home(pawn.color, pawn.stepCount)
    }
    return null
}

fun DrawScope.drawMyPawnArrow(cx: Float, cy: Float, scaleX: Float, scaleY: Float, hasBadge: Boolean, bobOffset: Float) {
    val tipY = if (hasBadge) {
        cy - 135f * scaleY + bobOffset
    } else {
        cy - 105f * scaleY + bobOffset
    }
    
    // Increased by +50% as requested!
    val arrowWidth = 20f * scaleX
    val arrowHeight = 24f * scaleY
    
    val path = Path().apply {
        moveTo(cx, tipY)
        lineTo(cx - arrowWidth / 2f, tipY - arrowHeight * 0.4f)
        lineTo(cx - arrowWidth * 0.2f, tipY - arrowHeight * 0.4f)
        lineTo(cx - arrowWidth * 0.2f, tipY - arrowHeight)
        lineTo(cx + arrowWidth * 0.2f, tipY - arrowHeight)
        lineTo(cx + arrowWidth * 0.2f, tipY - arrowHeight * 0.4f)
        lineTo(cx + arrowWidth / 2f, tipY - arrowHeight * 0.4f)
        close()
    }
    
    drawPath(
        path = path,
        color = Color(0xFF261912),
        style = Stroke(width = 3.5f)
    )
    
    drawPath(
        path = path,
        color = Color(0xFFFFEB3B)
    )
}

fun DrawScope.drawPawnGroupCountBadge(cx: Float, cy: Float, scaleX: Float, scaleY: Float, count: Int) {
    val badgeRadius = 18f * scaleX
    val badgeY = cy - 105f * scaleY
    
    drawCircle(
        color = Color(0xFF261912),
        radius = badgeRadius + 3.6f,
        center = Offset(cx, badgeY)
    )
    
    drawCircle(
        color = Color(0xFFE53935),
        radius = badgeRadius,
        center = Offset(cx, badgeY)
    )
    
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 18f * scaleX
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }
        val textHeightOffset = (paint.descent() + paint.ascent()) / 2f
        canvas.nativeCanvas.drawText(
            count.toString(),
            cx,
            badgeY - textHeightOffset,
            paint
        )
    }
}

fun DrawScope.drawSpeechBubbleWithEmoji(cx: Float, cy: Float, textOrEmoji: String) {
    val paint = android.graphics.Paint().apply {
        textSize = if (textOrEmoji.length <= 3) 33f else 19f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        color = android.graphics.Color.parseColor("#3E2723")
    }
    val textWidth = paint.measureText(textOrEmoji)
    val bubbleW = (textWidth + 36f).coerceAtLeast(78f)
    val bubbleH = 54f

    val bubbleY = cy
    // 1. White bubble background
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx - bubbleW / 2f, bubbleY - bubbleH / 2f),
        size = Size(bubbleW, bubbleH),
        cornerRadius = CornerRadius(16f, 16f)
    )
    // 2. Dark brown border
    drawRoundRect(
        color = Color(0xFF4E342E),
        topLeft = Offset(cx - bubbleW / 2f, bubbleY - bubbleH / 2f),
        size = Size(bubbleW, bubbleH),
        cornerRadius = CornerRadius(16f, 16f),
        style = Stroke(width = 3.5f)
    )
    // 3. Pointer tail pointing down towards head
    val pointerPath = Path().apply {
        moveTo(cx - 9f, bubbleY + bubbleH / 2f - 1f)
        lineTo(cx, bubbleY + bubbleH / 2f + 13f)
        lineTo(cx + 9f, bubbleY + bubbleH / 2f - 1f)
        close()
    }
    drawPath(pointerPath, SolidColor(Color.White))
    drawPath(pointerPath, SolidColor(Color(0xFF4E342E)), style = Stroke(width = 3f))

    // 4. Text / Emoji
    val textY = bubbleY - (paint.descent() + paint.ascent()) / 2f
    drawContext.canvas.nativeCanvas.drawText(textOrEmoji, cx, textY, paint)
}

fun DrawScope.drawCutePawns(
    pawns: List<Pawn>,
    state: GameState,
    width: Float,
    height: Float,
    zoom: Float,
    characterBitmaps: Map<String, ImageBitmap> = emptyMap(),
    bobOffset: Float = 0f,
    idleBreathScaleY: Float = 1f,
    idleFloatZ: Float = 0f
) {
    val visiblePawns = pawns.filter { it.stepCount != 56 || (state.portalPawnId == it.id && state.portalPawnColor == it.color && state.portalProgress <= 0.8f) }
    val pawnGroupCounts = visiblePawns.mapNotNull { p ->
        val key = getPawnTileKey(p)
        if (key != null) p to key else null
    }.groupBy({ it.second }, { it.first })

    val timeSec = (System.currentTimeMillis() % 1000000L) / 1000f

    // SORT PAWNS BACK-TO-FRONT BY ISOMETRIC SCREEN Y (gridX + gridY)
    val sortedPawns = pawns.sortedBy { p ->
        val c = getPawnVisualCoords(p)
        if (c != null) c.first + c.second else 0f
    }

    sortedPawns.forEach { pawn ->
        var charScale = 1.2f
        var px = 0f
        var py = 0f
        var pz = 0f
        var rollAngle = 0f

        val visualCoords = getPawnVisualCoords(pawn)
        val basePos = LudoBoardConfig.basePawnPositions[pawn.color]?.getOrNull(pawn.id)

        if (pawn.stepCount == 56) {
            if (state.portalPawnId == pawn.id && state.portalPawnColor == pawn.color) {
                // Currently jumping into the space portal!
                val progress = state.portalProgress
                val color = pawn.color
                val p55Coord = LudoBoardConfig.homePaths[color]?.getOrNull(4) ?: Pair(7, 7)
                val p56Coord = Pair(7.0f, 7.0f)

                if (progress < 0.2f) {
                    px = p55Coord.first.toFloat()
                    py = p55Coord.second.toFloat()
                    pz = 0.85f
                    charScale = 1.0f
                } else if (progress > 0.8f) {
                    return@forEach // Hidden completely!
                } else {
                    val jumpProgress = (progress - 0.2f) / 0.6f
                    px = p55Coord.first.toFloat() + (p56Coord.first.toFloat() - p55Coord.first.toFloat()) * jumpProgress
                    py = p55Coord.second.toFloat() + (p56Coord.second.toFloat() - p55Coord.second.toFloat()) * jumpProgress
                    pz = 0.85f + (1.2f - 0.85f) * jumpProgress + 3.0f * sin(jumpProgress * Math.PI).toFloat()
                    charScale = 1.0f - jumpProgress
                }
            } else {
                return@forEach // Hidden!
            }
        } else {
            if (visualCoords == null) return@forEach

            if (pawn.isBumping && basePos != null) {
                // High soaring parabolic fling back to base cage with dizzy spinning!
                val progress = pawn.bumpProgress
                px = visualCoords.first + (basePos.first - visualCoords.first) * progress + sin(progress * Math.PI * 2.0).toFloat() * 0.5f
                py = visualCoords.second + (basePos.second - visualCoords.second) * progress + cos(progress * Math.PI * 2.0).toFloat() * 0.3f
                // High parabolic arc fling!
                pz = visualCoords.third + sin(progress * Math.PI).toFloat() * 4.8f
                // Rotate 4 full dizzy spins (1440 degrees) as it gets flung back
                rollAngle = progress * 1440f
            } else if (pawn.isHopping && pawn.stepCount == 0 && basePos != null) {
                // High arc deployment trajectory jumping from base slot into tile 0 on the board!
                val progress = pawn.hopProgress
                px = basePos.first + (visualCoords.first - basePos.first) * progress
                py = basePos.second + (visualCoords.second - basePos.second) * progress
                val arcHeight = sin(progress * Math.PI).toFloat() * 3.8f // High soaring parabolic arc!
                pz = 1.1f + (visualCoords.third - 1.1f) * progress + arcHeight
            } else {
                px = visualCoords.first
                py = visualCoords.second
                pz = visualCoords.third

                // Apply Hop animation offset
                if (pawn.isHopping) {
                    val hopHeightMultiplier = if (pawn.isSuperHop) 3.2f else 1.3f
                    val hopHeight = sin(pawn.hopProgress * Math.PI).toFloat() * hopHeightMultiplier
                    pz += hopHeight
                }
            }
        }

        val center = getIsometricCoords(px, py, pz, width, height, zoom)

        // 3D ISOMETRIC GROUND SHADOW
        if (pawn.isHopping) {
            val prog = pawn.hopProgress
            val boardDim = minOf(width, height * 1.55f)
            val tw = (boardDim / 27.5f) * zoom
            val th = tw * 0.58f

            val groundZ = if (pawn.stepCount == 0 && basePos != null) {
                1.1f + ((visualCoords?.third ?: 0.12f) - 1.1f) * prog
            } else {
                visualCoords?.third ?: 0.12f
            }

            val groundCenter = getIsometricCoords(px, py, groundZ, width, height, zoom)

            // Dynamic ground shadow
            val shadowScale = (1.0f - sin(prog * Math.PI).toFloat() * 0.35f).coerceAtLeast(0.4f)
            val shadowW = tw * 1.1f * shadowScale
            val shadowH = th * 0.65f * shadowScale
            drawOval(
                color = Color.Black.copy(alpha = 0.28f * shadowScale),
                topLeft = Offset(groundCenter.x - shadowW / 2f, groundCenter.y - shadowH / 2f),
                size = Size(shadowW, shadowH)
            )
        }

        // Draw Bumping Shockwave / Explosion Effect when character gets kicked back to base!
        if (pawn.isBumping) {
            drawCircle(
                color = Color(0xFFFF1744).copy(alpha = 0.85f),
                radius = 36f,
                center = Offset(center.x, center.y)
            )
            drawCircle(
                color = Color(0xFFFFEA00),
                radius = 28f,
                center = Offset(center.x, center.y),
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = Color(0xFF00E676),
                radius = 18f,
                center = Offset(center.x, center.y),
                style = Stroke(width = 4f)
            )
        }

        // Draw Custom Cute Bird Pawn Character with responsive stretch and phase-offset breathing
        var scaleX = 1f
        var scaleY = if (pawn.isHopping) (1f + sin(pawn.hopProgress * Math.PI).toFloat() * 0.05f) else 1f

        if (!pawn.isHopping && !pawn.isBumping) {
            // Unique phase shift per pawn so every character breathes at a different time!
            val phase = pawn.color.ordinal * 1.8f + pawn.id * 1.3f
            val pawnBreathScaleY = 1.00f + sin(timeSec * 2.8f + phase).toFloat() * 0.035f
            scaleY *= pawnBreathScaleY
        }

        scaleX *= charScale
        scaleY *= charScale

        // Determine the character type skin
        val player = state.players.find { it.color == pawn.color }
        val activeCharType = player?.characterSkin ?: "char1"

        val isFacingLeft = if (pawn.isHopping && pawn.stepCount == 0 && basePos != null) {
            // Face towards deploy jump destination
            val startIdx = LudoBoardConfig.playerStartTrackIndex[pawn.color] ?: 0
            val deployCoord = LudoBoardConfig.outerTrack[startIdx]
            val dx = deployCoord.first.toFloat() - basePos.first
            val dy = deployCoord.second.toFloat() - basePos.second
            val screenDx = dx - dy
            screenDx > 0.01f
        } else if (pawn.isBumping && basePos != null && visualCoords != null) {
            val dx = basePos.first - visualCoords.first
            val dy = basePos.second - visualCoords.second
            val screenDx = dx - dy
            screenDx > 0.01f
        } else if (pawn.stepCount in 0..50) {
            val startIdx = LudoBoardConfig.playerStartTrackIndex[pawn.color] ?: 0
            val currIdx = (startIdx + pawn.stepCount) % 52
            val nextIdx = (startIdx + pawn.stepCount + 1) % 52
            val p1 = LudoBoardConfig.outerTrack[currIdx]
            val p2 = LudoBoardConfig.outerTrack[nextIdx]
            val dx = p2.first - p1.first
            val dy = p2.second - p1.second
            val screenDx = dx - dy
            screenDx > 0.01f
        } else if (pawn.stepCount in 51..55) {
            val path = LudoBoardConfig.homePaths[pawn.color] ?: emptyList()
            val idx = pawn.stepCount - 51
            val p1 = path.getOrNull(idx) ?: Pair(7, 7)
            val p2 = path.getOrNull(idx + 1) ?: LudoBoardConfig.centerHomes[pawn.color] ?: Pair(7, 7)
            val dx = p2.first - p1.first
            val dy = p2.second - p1.second
            val screenDx = dx - dy
            screenDx > 0.01f
        } else {
            // Base (chuồng): Tất cả quân cờ quay hướng nhìn vào giữa trung tâm màn hình
            when (pawn.color) {
                PlayerColor.RED -> false    // Phía dưới -> Quay hướng vào trung tâm
                PlayerColor.BLUE -> false   // Ô Xanh Dương (phía bên phải) -> Quay sang TRÁI hướng vào trung tâm (<---)
                PlayerColor.GREEN -> true   // Ô Xanh Lá (phía bên trái) -> Quay sang PHẢI hướng vào trung tâm (--->)
                PlayerColor.YELLOW -> true  // Phía trên -> Quay hướng vào trung tâm
            }
        }

        // Draw rotating red circle under my pawns' feet to mark standing position throughout the game
        val myPawnColor = slotToColor(state.myPlayerId)
        if (pawn.color == myPawnColor) {
            val rotationAngle = (timeSec * 120f) % 360f
            val shadowRx = 33f * scaleX
            val shadowRy = 13.2f * scaleY

            // Soft translucent red floor glow hugging foot shadow
            drawOval(
                color = Color(0x38FF1744),
                topLeft = Offset(center.x - shadowRx, center.y - shadowRy),
                size = Size(shadowRx * 2f, shadowRy * 2f)
            )

            // Dash rotation phase along perimeter (glides along floor oval without tilting shape)
            val dashPhase = timeSec * 90f * scaleX

            drawOval(
                color = Color(0xFFFF1744),
                topLeft = Offset(center.x - shadowRx, center.y - shadowRy),
                size = Size(shadowRx * 2f, shadowRy * 2f),
                style = Stroke(
                    width = 3.0f * scaleX,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f * scaleX, 12f * scaleX), dashPhase)
                )
            )

            // 4 yellow indicator dots rotating smoothly along the perimeter of the floor shadow
            for (i in 0..3) {
                val angleRad = Math.toRadians((rotationAngle + i * 90.0)).toFloat()
                val dotX = center.x + (shadowRx + 2f * scaleX) * kotlin.math.cos(angleRad)
                val dotY = center.y + (shadowRy + 2f * scaleY) * kotlin.math.sin(angleRad)
                drawCircle(Color(0xFFFFEA00), radius = 3.5f * scaleX, center = Offset(dotX, dotY))
            }
        }

        drawCuteBirdCharacter(
            cx = center.x,
            cy = center.y,
            color = pawn.color,
            scaleX = scaleX,
            scaleY = scaleY,
            isSelected = state.selectedPawnId == pawn.id && state.players[state.activePlayerIndex].color == pawn.color && !pawn.isHopping && !pawn.isBumping,
            charType = activeCharType,
            characterBitmaps = characterBitmaps,
            isFacingLeft = isFacingLeft,
            rollAngle = rollAngle
        )

        // Draw active random emote/exclamation bubble high above pawn head!
        val pawnEmoteInfo = state.activePawnEmotes["${pawn.color.name}_${pawn.id}"]
        if (pawnEmoteInfo != null && (System.currentTimeMillis() - pawnEmoteInfo.second) < 2500) {
            drawSpeechBubbleWithEmoji(
                cx = center.x,
                cy = center.y - 150f * scaleY,
                textOrEmoji = pawnEmoteInfo.first
            )
        }

        // Find grouping details for counts
        val groupKey = getPawnTileKey(pawn)
        val groupPawns = if (groupKey != null) pawnGroupCounts[groupKey] ?: emptyList() else emptyList()
        val groupSize = groupPawns.size
        val hasBadge = groupSize >= 2

        if (hasBadge) {
            drawPawnGroupCountBadge(cx = center.x, cy = center.y, scaleX = scaleX, scaleY = scaleY, count = groupSize)
        }

        // Add visual pointing down arrow on top of user's pawn
        val mySlotColor = if (state.mode == GameMode.TEAM_LOBBY && state.activeRoom != null) {
            slotToColor(state.myPlayerId)
        } else {
            state.players.getOrNull(state.activePlayerIndex)?.color ?: PlayerColor.RED
        }
        if (pawn.color == mySlotColor) {
            drawMyPawnArrow(cx = center.x, cy = center.y, scaleX = scaleX, scaleY = scaleY, hasBadge = hasBadge, bobOffset = bobOffset)
        }
    }
}

fun DrawScope.drawIsometricSlab(
    centerX: Float,
    centerY: Float,
    w: Float,
    h: Float,
    thickness: Float,
    topBrush: Brush,
    leftBrush: Brush,
    rightBrush: Brush
) {
    // 4 corners of top face
    val p0 = Offset(centerX, centerY - h / 2) // top
    val p1 = Offset(centerX + w / 2, centerY)   // right
    val p2 = Offset(centerX, centerY + h / 2) // bottom
    val p3 = Offset(centerX - w / 2, centerY)   // left

    // 1. Top face
    val topPath = Path().apply {
        moveTo(p0.x, p0.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }
    drawPath(topPath, topBrush)

    // 2. Left side face
    val leftPath = Path().apply {
        moveTo(p3.x, p3.y)
        lineTo(p2.x, p2.y)
        lineTo(p2.x, p2.y + thickness)
        lineTo(p3.x, p3.y + thickness)
        close()
    }
    drawPath(leftPath, leftBrush)

    // 3. Right side face
    val rightPath = Path().apply {
        moveTo(p2.x, p2.y)
        lineTo(p1.x, p1.y)
        lineTo(p1.x, p1.y + thickness)
        lineTo(p2.x, p2.y + thickness)
        close()
    }
    drawPath(rightPath, rightBrush)

    // 4. Soft subtle outline around top face
    drawPath(topPath, SolidColor(Color(0x224E342E)), style = Stroke(width = 0.8f.dp.toPx()))
}

fun DrawScope.drawIsometricBiscuit(cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    // Draw 3D isometric biscuit top
    drawIsometricSlab(
        centerX = cx,
        centerY = cy,
        w = w,
        h = h,
        thickness = 16f,
        topBrush = SolidColor(color),
        leftBrush = SolidColor(color.darken(0.15f)),
        rightBrush = SolidColor(color.darken(0.3f))
    )

    // Draw little biscuit indentations/dots (representing a sweet butter cracker!)
    val dx = w * 0.18f
    val dy = h * 0.18f
    val dotColor = color.darken(0.12f)
    val r = w * 0.04f
    drawCircle(dotColor, radius = r, center = Offset(cx - dx, cy - dy))
    drawCircle(dotColor, radius = r, center = Offset(cx + dx, cy - dy))
    drawCircle(dotColor, radius = r, center = Offset(cx - dx, cy + dy))
    drawCircle(dotColor, radius = r, center = Offset(cx + dx, cy + dy))
}

fun DrawScope.drawIsometricFlower(cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    // Draw standard biscuit underneath first
    drawIsometricBiscuit(cx, cy, w, h, Color(0xFFFFF9C4))
    
    // Draw flower petals on top
    val petalColor = color
    val r = w * 0.16f
    val offsetW = w * 0.15f
    val offsetH = h * 0.15f
    drawCircle(petalColor, radius = r, center = Offset(cx - offsetW, cy))
    drawCircle(petalColor, radius = r, center = Offset(cx + offsetW, cy))
    drawCircle(petalColor, radius = r, center = Offset(cx, cy - offsetH))
    drawCircle(petalColor, radius = r, center = Offset(cx, cy + offsetH))
    
    // Draw flower center cream
    drawCircle(Color(0xFFFFF9C4), radius = r * 0.8f, center = Offset(cx, cy))
}

fun DrawScope.drawIsometricArrow(cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    // Draw curved curved flow entry arrow on the biscuit
    val path = Path().apply {
        moveTo(cx, cy - h / 3f)
        quadraticBezierTo(cx + w / 3f, cy - h / 3f, cx + w / 3f, cy + h / 6f)
        lineTo(cx + w / 2f, cy + h / 6f)
        lineTo(cx + w / 4f, cy + h / 2f)
        lineTo(cx, cy + h / 6f)
        lineTo(cx + w / 6f, cy + h / 6f)
        quadraticBezierTo(cx + w / 6f, cy - h / 12f, cx, cy - h / 12f)
        close()
    }
    drawPath(path, SolidColor(color))
}

fun DrawScope.drawCuteBirdCharacter(
    cx: Float,
    cy: Float,
    color: PlayerColor,
    scaleX: Float,
    scaleY: Float,
    isSelected: Boolean,
    charType: String = "default",
    characterBitmaps: Map<String, ImageBitmap> = emptyMap(),
    isFacingLeft: Boolean = false,
    rollAngle: Float = 0f
) {
    // Fixed base dimensions
    val radius = 28.8f
    val heightFactor = 38.4f

    // Anchor character feet directly at tile center cy (matching Shop view transformOrigin)
    val feetY = cy

    // Draw darker, larger shadow under the character's feet
    drawOval(
        color = Color.Black.copy(alpha = 0.28f),
        topLeft = Offset(cx - radius * 1.1f, feetY - radius * 0.4f),
        size = Size(radius * 2.2f, radius * 0.8f)
    )

    withTransform({
        val sx = if (isFacingLeft) -scaleX else scaleX
        if (rollAngle != 0f) {
            rotate(degrees = rollAngle, pivot = Offset(cx, feetY - radius))
        }
        // Pivot at (cx, feetY) ensures feet remain 100% glued to the floor tile during breathing!
        scale(sx, scaleY, Offset(cx, feetY))
    }) {
        val bitmap = characterBitmaps[charType]
        if (bitmap != null) {
            // Draw PNG character skin anchored strictly at feet level (feetY)
            val dstW = radius * 2.6f
            val aspectRatio = if (bitmap.width > 0 && bitmap.height > 0) {
                bitmap.width.toFloat() / bitmap.height.toFloat()
            } else {
                1.0f
            }
            val baseH = dstW / aspectRatio
            val left = cx - dstW / 2f
            val top = feetY - baseH
            drawImage(
                image = bitmap,
                dstOffset = androidx.compose.ui.unit.IntOffset(left.toInt(), top.toInt()),
                dstSize = androidx.compose.ui.unit.IntSize(dstW.toInt(), baseH.toInt())
            )
        } else {
            val drawCy = feetY - 4f
            val activeCharType = if (color == PlayerColor.RED) charType else "default"

    if (activeCharType != "default" && color == PlayerColor.RED) {
        // DRAW CUSTOM PURCHASED CHARACTER!
        val bodyColor = when (activeCharType) {
            "bird" -> Color(0xFFFF7043) // Bright orange-red bird
            "pig" -> Color(0xFFF48FB1)  // Pink pig
            "cat" -> Color(0xFFFFCC80)  // Orange tabby cat
            "otter" -> Color(0xFF8D6E63) // Brown otter
            "monkey" -> Color(0xFF9E7860) // Brown monkey
            else -> Color(0xFF78909C)   // Grey-blue shark
        }

        // Body outline
        drawCircle(
            color = Color(0xFF1E0C06),
            radius = radius * 1.25f,
            center = Offset(cx, cy - heightFactor * 0.5f)
        )
        // Body
        drawCircle(
            color = bodyColor,
            radius = radius * 1.1f,
            center = Offset(cx, cy - heightFactor * 0.5f)
        )

        // CHARACTER SPECIFIC EXTRAS (Ears, Fins, combs)
        when (activeCharType) {
            "bird" -> {
                // Red comb/feathers on top
                drawCircle(Color(0xFFE51C23), radius = radius * 0.35f, center = Offset(cx, cy - heightFactor * 1.1f))
            }
            "pig" -> {
                // Floppy pig ears on top
                val leftEar = Path().apply {
                    moveTo(cx - radius * 0.9f, cy - heightFactor * 0.9f)
                    lineTo(cx - radius * 0.4f, cy - heightFactor * 1.1f)
                    lineTo(cx - radius * 0.8f, cy - heightFactor * 1.2f)
                    close()
                }
                val rightEar = Path().apply {
                    moveTo(cx + radius * 0.9f, cy - heightFactor * 0.9f)
                    lineTo(cx + radius * 0.4f, cy - heightFactor * 1.1f)
                    lineTo(cx + radius * 0.8f, cy - heightFactor * 1.2f)
                    close()
                }
                drawPath(leftEar, SolidColor(Color(0xFFF06292)))
                drawPath(rightEar, SolidColor(Color(0xFFF06292)))
            }
            "cat" -> {
                // Pointy cat ears on top
                val leftEar = Path().apply {
                    moveTo(cx - radius * 0.9f, cy - heightFactor * 0.8f)
                    lineTo(cx - radius * 0.3f, cy - heightFactor * 1.1f)
                    lineTo(cx - radius * 0.7f, cy - heightFactor * 1.3f)
                    close()
                }
                val rightEar = Path().apply {
                    moveTo(cx + radius * 0.9f, cy - heightFactor * 0.8f)
                    lineTo(cx + radius * 0.3f, cy - heightFactor * 1.1f)
                    lineTo(cx + radius * 0.7f, cy - heightFactor * 1.3f)
                    close()
                }
                drawPath(leftEar, SolidColor(Color(0xFFFB8C00)))
                drawPath(rightEar, SolidColor(Color(0xFFFB8C00)))
            }
            "otter" -> {
                // Small round ears
                drawCircle(Color(0xFF5D4037), radius = radius * 0.28f, center = Offset(cx - radius * 0.7f, cy - heightFactor * 1.0f))
                drawCircle(Color(0xFF5D4037), radius = radius * 0.28f, center = Offset(cx + radius * 0.7f, cy - heightFactor * 1.0f))
            }
            "monkey" -> {
                // Big round monkey ears on sides
                drawCircle(Color(0xFF8D6E63), radius = radius * 0.38f, center = Offset(cx - radius * 1.1f, cy - heightFactor * 0.5f))
                drawCircle(Color(0xFFF5E6D3), radius = radius * 0.22f, center = Offset(cx - radius * 1.1f, cy - heightFactor * 0.5f))
                drawCircle(Color(0xFF8D6E63), radius = radius * 0.38f, center = Offset(cx + radius * 1.1f, cy - heightFactor * 0.5f))
                drawCircle(Color(0xFFF5E6D3), radius = radius * 0.22f, center = Offset(cx + radius * 1.1f, cy - heightFactor * 0.5f))
            }
            "shark" -> {
                // Shark fin on top
                val fin = Path().apply {
                    moveTo(cx - radius * 0.3f, cy - heightFactor * 1.0f)
                    lineTo(cx + radius * 0.3f, cy - heightFactor * 1.0f)
                    lineTo(cx, cy - heightFactor * 1.4f)
                    close()
                }
                drawPath(fin, SolidColor(Color(0xFF546E7A)))
            }
        }

        // Eyes (Common cute style)
        val eyeY = cy - heightFactor * 0.55f
        val eyeXOffset = radius * 0.35f
        val eyeRadius = radius * 0.32f

        // Left Eye
        drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeXOffset, eyeY))
        drawCircle(Color(0xFF212121), radius = eyeRadius * 0.55f, center = Offset(cx - eyeXOffset, eyeY))
        drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx - eyeXOffset - 1f, eyeY - 1f))

        // Right Eye
        drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeXOffset, eyeY))
        drawCircle(Color(0xFF212121), radius = eyeRadius * 0.55f, center = Offset(cx + eyeXOffset, eyeY))
        drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx + eyeXOffset - 1f, eyeY - 1f))

        // CHARACTER FACIAL FEATURES
        when (activeCharType) {
            "bird" -> {
                // Cute orange beak
                val beakPath = Path().apply {
                    moveTo(cx - radius * 0.2f, cy - heightFactor * 0.42f)
                    lineTo(cx + radius * 0.2f, cy - heightFactor * 0.42f)
                    lineTo(cx, cy - heightFactor * 0.26f)
                    close()
                }
                drawPath(beakPath, SolidColor(Color(0xFFFFA726)))
            }
            "pig" -> {
                // Pink snout with nostrils
                drawOval(
                    color = Color(0xFFF8BBD0),
                    topLeft = Offset(cx - radius * 0.35f, cy - heightFactor * 0.45f),
                    size = Size(radius * 0.7f, radius * 0.45f)
                )
                drawCircle(Color(0xFFC2185B), radius = radius * 0.06f, center = Offset(cx - radius * 0.12f, cy - heightFactor * 0.33f))
                drawCircle(Color(0xFFC2185B), radius = radius * 0.06f, center = Offset(cx + radius * 0.12f, cy - heightFactor * 0.33f))
            }
            "cat" -> {
                // Cat nose, mouth, whiskers
                drawCircle(Color(0xFFF48FB1), radius = radius * 0.1f, center = Offset(cx, cy - heightFactor * 0.42f))
                // Whiskers
                drawLine(Color(0xFF4E342E), Offset(cx - radius * 0.3f, cy - heightFactor * 0.4f), Offset(cx - radius * 0.8f, cy - heightFactor * 0.45f), strokeWidth = 2.5f)
                drawLine(Color(0xFF4E342E), Offset(cx - radius * 0.3f, cy - heightFactor * 0.35f), Offset(cx - radius * 0.8f, cy - heightFactor * 0.35f), strokeWidth = 2.5f)
                drawLine(Color(0xFF4E342E), Offset(cx + radius * 0.3f, cy - heightFactor * 0.4f), Offset(cx + radius * 0.8f, cy - heightFactor * 0.45f), strokeWidth = 2.5f)
                drawLine(Color(0xFF4E342E), Offset(cx + radius * 0.3f, cy - heightFactor * 0.35f), Offset(cx + radius * 0.8f, cy - heightFactor * 0.35f), strokeWidth = 2.5f)
            }
            "otter" -> {
                // Otter muzzle and whiskers
                drawCircle(Color(0xFFEEEEEE), radius = radius * 0.25f, center = Offset(cx - radius * 0.12f, cy - heightFactor * 0.38f))
                drawCircle(Color(0xFFEEEEEE), radius = radius * 0.25f, center = Offset(cx + radius * 0.12f, cy - heightFactor * 0.38f))
                drawCircle(Color.Black, radius = radius * 0.08f, center = Offset(cx, cy - heightFactor * 0.44f))
            }
            "monkey" -> {
                // Monkey mouth/muzzle
                drawOval(
                    color = Color(0xFFF5E6D3),
                    topLeft = Offset(cx - radius * 0.45f, cy - heightFactor * 0.45f),
                    size = Size(radius * 0.9f, radius * 0.42f)
                )
                // Happy curved smile line
                val smilePath = Path().apply {
                    moveTo(cx - radius * 0.22f, cy - heightFactor * 0.33f)
                    quadraticBezierTo(cx, cy - heightFactor * 0.20f, cx + radius * 0.22f, cy - heightFactor * 0.33f)
                }
                drawPath(smilePath, SolidColor(Color(0xFF4E342E)), style = Stroke(width = 3f))
            }
            "shark" -> {
                // Sharp white teeth and dangerous mouth
                val mouthPath = Path().apply {
                    moveTo(cx - radius * 0.35f, cy - heightFactor * 0.35f)
                    lineTo(cx + radius * 0.35f, cy - heightFactor * 0.35f)
                    lineTo(cx, cy - heightFactor * 0.2f)
                    close()
                }
                drawPath(mouthPath, SolidColor(Color(0xFF263238)))
                // Teeth
                drawCircle(Color.White, radius = radius * 0.08f, center = Offset(cx - radius * 0.15f, cy - heightFactor * 0.32f))
                drawCircle(Color.White, radius = radius * 0.08f, center = Offset(cx + radius * 0.15f, cy - heightFactor * 0.32f))
            }
        }
    } else {
        when (color) {
            PlayerColor.RED -> {
                // ==============================================
                // 1. RED TEAM: Brown/Grey Hen with Red Sash
                // ==============================================
                val mainBrown = Color(0xFF8D6E63) // Brown/grey body color
                val lightBeige = Color(0xFFF5E6D3) // Face zone color

                // Bold outline for rich comic visual pop and absolute legibility
                drawCircle(
                    color = Color(0xFF1E0C06),
                    radius = radius * 1.25f,
                    center = Offset(cx, cy - heightFactor * 0.5f)
                )

                // Body
                drawCircle(
                    color = mainBrown,
                    radius = radius * 1.1f,
                    center = Offset(cx, cy - heightFactor * 0.5f)
                )

                // Red comb on top (Two rounded lobes)
                drawCircle(Color(0xFFE51C23), radius = radius * 0.45f, center = Offset(cx - radius * 0.25f, cy - heightFactor * 1.1f))
                drawCircle(Color(0xFFE51C23), radius = radius * 0.45f, center = Offset(cx + radius * 0.25f, cy - heightFactor * 1.15f))

                // White/Light Beige Face mask area
                drawCircle(
                    color = lightBeige,
                    radius = radius * 0.85f,
                    center = Offset(cx, cy - heightFactor * 0.5f)
                )

                // Big white eyes with lashes
                val eyeY = cy - heightFactor * 0.58f
                val eyeXOffset = radius * 0.35f
                val eyeRadius = radius * 0.35f

                // Left Eye
                drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeXOffset, eyeY))
                drawCircle(Color(0xFF4E342E), radius = eyeRadius * 0.5f, center = Offset(cx - eyeXOffset + 1f, eyeY + 1f))
                drawCircle(Color.White, radius = eyeRadius * 0.18f, center = Offset(cx - eyeXOffset - 1f, eyeY - 1f))

                // Right Eye
                drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeXOffset, eyeY))
                drawCircle(Color(0xFF4E342E), radius = eyeRadius * 0.5f, center = Offset(cx + eyeXOffset - 1f, eyeY + 1f))
                drawCircle(Color.White, radius = eyeRadius * 0.18f, center = Offset(cx + eyeXOffset - 2f, eyeY - 1f))

                // Lashes/liner
                drawLine(Color.Black, Offset(cx - eyeXOffset * 1.8f, eyeY - eyeRadius), Offset(cx - eyeXOffset * 1.2f, eyeY - eyeRadius * 1.3f), strokeWidth = 2.5f)
                drawLine(Color.Black, Offset(cx + eyeXOffset * 1.8f, eyeY - eyeRadius), Offset(cx + eyeXOffset * 1.2f, eyeY - eyeRadius * 1.3f), strokeWidth = 2.5f)

                // Orange beak
                val beakPath = Path().apply {
                    moveTo(cx - radius * 0.18f, cy - heightFactor * 0.45f)
                    lineTo(cx + radius * 0.18f, cy - heightFactor * 0.45f)
                    lineTo(cx, cy - heightFactor * 0.32f)
                    close()
                }
                drawPath(beakPath, SolidColor(Color(0xFFFF9800)))

                // Red wattles under the beak
                drawCircle(Color(0xFFE51C23), radius = radius * 0.18f, center = Offset(cx - radius * 0.08f, cy - heightFactor * 0.28f))
                drawCircle(Color(0xFFE51C23), radius = radius * 0.18f, center = Offset(cx + radius * 0.08f, cy - heightFactor * 0.28f))

                // Red sash diagonally draped
                val sashPath = Path().apply {
                    moveTo(cx - radius * 0.8f, cy - heightFactor * 0.35f)
                    lineTo(cx - radius * 0.5f, cy - heightFactor * 0.22f)
                    lineTo(cx + radius * 0.6f, cy - heightFactor * 0.55f)
                    lineTo(cx + radius * 0.8f, cy - heightFactor * 0.65f)
                    close()
                }
                drawPath(sashPath, SolidColor(Color(0xFFFF4081)))

                // Little Brown Wings
                drawOval(
                    color = mainBrown,
                    topLeft = Offset(cx - radius * 1.15f, cy - heightFactor * 0.58f),
                    size = Size(radius * 0.4f, radius * 0.75f)
                )
                drawOval(
                    color = mainBrown,
                    topLeft = Offset(cx + radius * 0.75f, cy - heightFactor * 0.58f),
                    size = Size(radius * 0.4f, radius * 0.75f)
                )
            }
        PlayerColor.GREEN -> {
            // ==============================================
            // 2. GREEN TEAM: Beige Pony with dark brown mane
            // ==============================================
            val bodyBeige = Color(0xFFF9E7D0) // Warm beige
            val maneBrown = Color(0xFF5D4037) // Dark chocolate mane
            val muzzlePink = Color(0xFFFFC0CB) // Pink muzzle

            // Bold outline for rich comic visual pop and absolute legibility
            drawCircle(
                color = Color(0xFF1E0C06),
                radius = radius * 1.25f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Body
            drawCircle(
                color = bodyBeige,
                radius = radius * 1.1f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Ears
            val leftEarPath = Path().apply {
                moveTo(cx - radius * 0.9f, cy - heightFactor * 0.8f)
                lineTo(cx - radius * 0.4f, cy - heightFactor * 0.85f)
                lineTo(cx - radius * 0.7f, cy - heightFactor * 1.25f)
                close()
            }
            drawPath(leftEarPath, SolidColor(bodyBeige))
            val leftEarInnerPath = Path().apply {
                moveTo(cx - radius * 0.8f, cy - heightFactor * 0.82f)
                lineTo(cx - radius * 0.5f, cy - heightFactor * 0.85f)
                lineTo(cx - radius * 0.65f, cy - heightFactor * 1.15f)
                close()
            }
            drawPath(leftEarInnerPath, SolidColor(muzzlePink))

            val rightEarPath = Path().apply {
                moveTo(cx + radius * 0.9f, cy - heightFactor * 0.8f)
                lineTo(cx + radius * 0.4f, cy - heightFactor * 0.85f)
                lineTo(cx + radius * 0.7f, cy - heightFactor * 1.25f)
                close()
            }
            drawPath(rightEarPath, SolidColor(bodyBeige))
            val rightEarInnerPath = Path().apply {
                moveTo(cx + radius * 0.8f, cy - heightFactor * 0.82f)
                lineTo(cx + radius * 0.5f, cy - heightFactor * 0.85f)
                lineTo(cx + radius * 0.65f, cy - heightFactor * 1.15f)
                close()
            }
            drawPath(rightEarInnerPath, SolidColor(muzzlePink))

            // Dark brown mane / hair (fringe on forehead and neck)
            drawCircle(maneBrown, radius = radius * 0.45f, center = Offset(cx, cy - heightFactor * 0.95f))
            drawCircle(maneBrown, radius = radius * 0.35f, center = Offset(cx - radius * 0.3f, cy - heightFactor * 0.92f))
            drawCircle(maneBrown, radius = radius * 0.35f, center = Offset(cx + radius * 0.3f, cy - heightFactor * 0.92f))

            // Big eyes
            val eyeY = cy - heightFactor * 0.55f
            val eyeXOffset = radius * 0.35f
            val eyeRadius = radius * 0.32f

            // Left Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeXOffset, eyeY))
            drawCircle(Color(0xFF4E342E), radius = eyeRadius * 0.55f, center = Offset(cx - eyeXOffset, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx - eyeXOffset - 1f, eyeY - 1f))

            // Right Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeXOffset, eyeY))
            drawCircle(Color(0xFF4E342E), radius = eyeRadius * 0.55f, center = Offset(cx + eyeXOffset, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx + eyeXOffset - 1f, eyeY - 1f))

            // Prominent pink snout muzzle (oval at the bottom of face)
            drawOval(
                color = muzzlePink,
                topLeft = Offset(cx - radius * 0.65f, cy - heightFactor * 0.36f),
                size = Size(radius * 1.3f, radius * 0.6f)
            )
            // Smile line and 2 nostrils on muzzle
            drawCircle(Color(0xFFE51C23).copy(alpha = 0.5f), radius = radius * 0.05f, center = Offset(cx - radius * 0.18f, cy - heightFactor * 0.22f))
            drawCircle(Color(0xFFE51C23).copy(alpha = 0.5f), radius = radius * 0.05f, center = Offset(cx + radius * 0.18f, cy - heightFactor * 0.22f))
        }
        PlayerColor.YELLOW -> {
            // ==============================================
            // 3. YELLOW TEAM: Yellow Baby Chicken with brown sash
            // ==============================================
            val brightYellow = Color(0xFFFFD93D)
            val darkBrown = Color(0xFF4E342E)

            // Bold outline for rich comic visual pop and absolute legibility
            drawCircle(
                color = Color(0xFF1E0C06),
                radius = radius * 1.25f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Body
            drawCircle(
                color = brightYellow,
                radius = radius * 1.1f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Red comb/tuft on head (heart-like design)
            drawCircle(Color(0xFFE51C23), radius = radius * 0.38f, center = Offset(cx - radius * 0.1f, cy - heightFactor * 1.05f))
            drawCircle(Color(0xFFE51C23), radius = radius * 0.38f, center = Offset(cx + radius * 0.1f, cy - heightFactor * 1.05f))

            // Big cartoon eyes
            val eyeY = cy - heightFactor * 0.58f
            val eyeXOffset = radius * 0.35f
            val eyeRadius = radius * 0.35f

            // Left Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeXOffset, eyeY))
            drawCircle(darkBrown, radius = eyeRadius * 0.5f, center = Offset(cx - eyeXOffset + 1f, eyeY + 1f))
            drawCircle(Color.White, radius = eyeRadius * 0.18f, center = Offset(cx - eyeXOffset - 1f, eyeY - 1f))

            // Right Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeXOffset, eyeY))
            drawCircle(darkBrown, radius = eyeRadius * 0.5f, center = Offset(cx + eyeXOffset - 1f, eyeY + 1f))
            drawCircle(Color.White, radius = eyeRadius * 0.18f, center = Offset(cx + eyeXOffset - 2f, eyeY - 1f))

            // Lashes
            drawLine(Color.Black, Offset(cx - eyeXOffset * 1.8f, eyeY - eyeRadius), Offset(cx - eyeXOffset * 1.2f, eyeY - eyeRadius * 1.3f), strokeWidth = 2.5f)
            drawLine(Color.Black, Offset(cx + eyeXOffset * 1.8f, eyeY - eyeRadius), Offset(cx + eyeXOffset * 1.2f, eyeY - eyeRadius * 1.3f), strokeWidth = 2.5f)

            // Orange beak
            val beakPath = Path().apply {
                moveTo(cx - radius * 0.18f, cy - heightFactor * 0.45f)
                lineTo(cx + radius * 0.18f, cy - heightFactor * 0.45f)
                lineTo(cx, cy - heightFactor * 0.32f)
                close()
            }
            drawPath(beakPath, SolidColor(Color(0xFFFF9800)))

            // Red wattles
            drawCircle(Color(0xFFE51C23), radius = radius * 0.18f, center = Offset(cx - radius * 0.08f, cy - heightFactor * 0.28f))
            drawCircle(Color(0xFFE51C23), radius = radius * 0.18f, center = Offset(cx + radius * 0.08f, cy - heightFactor * 0.28f))

            // Dark brown sash diagonally draped
            val sashPath = Path().apply {
                moveTo(cx - radius * 0.8f, cy - heightFactor * 0.35f)
                lineTo(cx - radius * 0.5f, cy - heightFactor * 0.22f)
                lineTo(cx + radius * 0.6f, cy - heightFactor * 0.55f)
                lineTo(cx + radius * 0.8f, cy - heightFactor * 0.65f)
                close()
            }
            drawPath(sashPath, SolidColor(darkBrown))

            // Little yellow wings
            drawOval(
                color = brightYellow,
                topLeft = Offset(cx - radius * 1.15f, cy - heightFactor * 0.58f),
                size = Size(radius * 0.4f, radius * 0.75f)
            )
            drawOval(
                color = brightYellow,
                topLeft = Offset(cx + radius * 0.75f, cy - heightFactor * 0.58f),
                size = Size(radius * 0.4f, radius * 0.75f)
            )
        }
        PlayerColor.BLUE -> {
            // ==============================================
            // 4. BLUE TEAM: Dark Brown Bull with horns
            // ==============================================
            val bullBrown = Color(0xFF4A342E) // Dark brown
            val snoutTan = Color(0xFF8D6E63) // Lighter brown/tan snout
            val hornCream = Color(0xFFFFF1C1) // Cream color for horns

            // Bold outline for rich comic visual pop and absolute legibility
            drawCircle(
                color = Color(0xFF150A05),
                radius = radius * 1.25f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Body
            drawCircle(
                color = bullBrown,
                radius = radius * 1.1f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Curved Horns on top of head
            val leftHornPath = Path().apply {
                moveTo(cx - radius * 0.5f, cy - heightFactor * 0.8f)
                quadraticBezierTo(cx - radius * 1.3f, cy - heightFactor * 1.1f, cx - radius * 1.0f, cy - heightFactor * 1.35f)
                quadraticBezierTo(cx - radius * 0.8f, cy - heightFactor * 1.1f, cx - radius * 0.2f, cy - heightFactor * 0.85f)
                close()
            }
            drawPath(leftHornPath, SolidColor(hornCream))

            val rightHornPath = Path().apply {
                moveTo(cx + radius * 0.5f, cy - heightFactor * 0.8f)
                quadraticBezierTo(cx + radius * 1.3f, cy - heightFactor * 1.1f, cx + radius * 1.0f, cy - heightFactor * 1.35f)
                quadraticBezierTo(cx + radius * 0.8f, cy - heightFactor * 1.1f, cx + radius * 0.2f, cy - heightFactor * 0.85f)
                close()
            }
            drawPath(rightHornPath, SolidColor(hornCream))

            // Big cartoon eyes
            val eyeY = cy - heightFactor * 0.55f
            val eyeXOffset = radius * 0.35f
            val eyeRadius = radius * 0.32f

            // Left Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeXOffset, eyeY))
            drawCircle(Color.Black, radius = eyeRadius * 0.55f, center = Offset(cx - eyeXOffset, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx - eyeXOffset - 1f, eyeY - 1f))

            // Right Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeXOffset, eyeY))
            drawCircle(Color.Black, radius = eyeRadius * 0.55f, center = Offset(cx + eyeXOffset, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx + eyeXOffset - 1f, eyeY - 1f))

            // Tan muzzle snout
            drawOval(
                color = snoutTan,
                topLeft = Offset(cx - radius * 0.65f, cy - heightFactor * 0.36f),
                size = Size(radius * 1.3f, radius * 0.6f)
            )
            // Nostrils
            drawCircle(Color(0xFF2D1500), radius = radius * 0.05f, center = Offset(cx - radius * 0.15f, cy - heightFactor * 0.22f))
            drawCircle(Color(0xFF2D1500), radius = radius * 0.05f, center = Offset(cx + radius * 0.15f, cy - heightFactor * 0.22f))
        }
    }
    }
    }
    }
}

@Composable
fun InventoryShopScreen(
    state: GameState,
    viewModel: LudoViewModel,
    characterBitmaps: Map<String, ImageBitmap>,
    onClose: () -> Unit
) {
    val isVi = state.language == "vi"
    var selectedTab by remember { mutableStateOf(1) } // 0: Xúc xắc, 1: Quân cờ, 2: Chuồng (Locked), 3: Thức ăn (Locked)
    var previewItemId by remember { mutableStateOf(state.selectedCharacter) }
    var previewColor by remember { mutableStateOf(PlayerColor.BLUE) }
    var showQuickFeedDialog by remember { mutableStateOf(false) }
    var showRansomDialog by remember { mutableStateOf(false) }
    var buyQuantity by remember { mutableStateOf(1) }
    val context = LocalContext.current

    data class ShopItem(
        val id: String,
        val nameVi: String,
        val nameEn: String,
        val goldPrice: Int,
        val diamondPrice: Int,
        val stars: Int,
        val emoji: String
    )

    val characterItems = listOf(
        ShopItem("char1", "Gà Vàng Sơ Sinh", "Golden Chick", 0, 0, 1, "🐥"),
        ShopItem("char2", "Gà Nâu Tinh Anh", "Brown Chick", 5000, 0, 1, "🐤"),
        ShopItem("char3", "Gấu Vàng Dũng Sĩ", "Golden Bear", 15000, 0, 2, "🐻"),
        ShopItem("char4", "Gấu Nâu Chiến Thần", "Brown Bear", 30000, 0, 2, "🐻‍❄️"),
        ShopItem("char5", "Ngựa Bạch Tuyết Thần Tốc", "Snow White Horse", 50000, 0, 2, "🦄"),
        ShopItem("char6", "Ngựa Nâu Vương Giả (VIP)", "Royal Brown Horse VIP", 0, 120, 3, "🐴"),
        ShopItem("char7", "Trâu Thần Tài (VIP)", "Wealth Bull VIP", 0, 250, 3, "🐂"),
        ShopItem("char8", "Trâu Đen Bá Vương (VIP)", "Overlord Black Bull VIP", 0, 500, 3, "🐂"),
        ShopItem("char9", "Lạc Đà Alpaca Tinh Tinh", "Alpaca Llama", 20000, 0, 2, "🦙"),
        ShopItem("char10", "Rái Cá Thông Thái", "Smart Otter", 25000, 0, 2, "🦦"),
        ShopItem("char11", "Tắc Kè Biến Hình", "Chameleon", 35000, 0, 2, "🦎"),
        ShopItem("char12", "Cáo Tuyết Huyền Thoại", "Legendary Fox", 40000, 0, 2, "🦊"),
        ShopItem("char13", "Gấu Trúc Kungfu", "Kungfu Panda", 45000, 0, 2, "🐼"),
        ShopItem("char14", "Kỳ Giông Mexico (VIP)", "Axolotl VIP", 0, 100, 3, "🦎"),
        ShopItem("char15", "Chim Công Rực Rỡ (VIP)", "Peacock VIP", 0, 150, 3, "🦚"),
        ShopItem("char16", "Nhím Gai Thần Tốc (VIP)", "Hedgehog VIP", 0, 200, 3, "🦔"),
        ShopItem("char17", "Cánh Cụt Băng Giá (VIP)", "Frost Penguin VIP", 0, 300, 3, "🐧"),
        ShopItem("char18", "Cá Sấu Chúa (VIP)", "King Crocodile VIP", 0, 400, 3, "🐊")
    )

    val diceItems = listOf(
        ShopItem("dice1", "Xúc xắc Sữa", "Milk Cream Dice", 0, 0, 1, "🎲"),
        ShopItem("dice2", "Xúc xắc Sô-cô-la", "Chocolate Fudge Dice", 3000, 0, 1, "🍫"),
        ShopItem("dice3", "Xúc xắc Dâu Tây", "Strawberry Dice", 8000, 0, 2, "🍓"),
        ShopItem("dice4", "Xúc xắc Ngân Hà", "Galaxy Blue Dice", 15000, 0, 2, "🌌"),
        ShopItem("dice5", "Xúc xắc Hoàng Gia", "Royal Gold Dice", 0, 100, 3, "👑")
    )

    val pedestalItems = listOf(
        ShopItem("pedestal1", "Chuồng Bánh Kem", "Cupcake Base", 0, 0, 1, "🏠")
    )

    val foodItems = listOf(
        ShopItem("food_hoa_qua", "Hoa Quả Tươi 🍎", "Fresh Fruits", 2000, 0, 1, "🍎"),
        ShopItem("food_co", "Cỏ Tươi Xanh 🌿", "Fresh Grass", 1500, 0, 1, "🌿"),
        ShopItem("food_rom", "Rơm Vàng Thơm 🌾", "Golden Hay", 3000, 0, 2, "🌾"),
        ShopItem("food_ca", "Cá Tươi 🐟", "Fresh Fish", 2000, 0, 1, "🐟"),
        ShopItem("food_con_trung", "Côn Trùng 🦗", "Insects", 1800, 0, 1, "🦗"),
        ShopItem("food_truc", "Trúc Tươi 🎋", "Bamboo", 2200, 0, 2, "🎋")
    )

    val currentList = when (selectedTab) {
        0 -> diceItems
        1 -> characterItems
        2 -> pedestalItems
        else -> foodItems
    }

    val selectedItem = currentList.find { it.id == previewItemId } ?: currentList.first()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCCFDEBCA))
            .pointerInput(Unit) {}
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
                // TOP BAR: Back arrow, Title "Quay lại", Gold & Diamonds
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onClose() },
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(4.dp, CircleShape)
                                .background(Color(0xFFE2C488), CircleShape)
                                .border(2.5.dp, Color(0xFF8D6E63), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF5D4037),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF5D4037), RoundedCornerShape(16.dp))
                                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isVi) "Quay lại" else "Back",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFF8E1)
                            )
                        }
                    }

                    // Balances
                    val tienPainter = rememberAssetImagePainter("IMG/tien.png")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .shadow(2.dp, RoundedCornerShape(50.dp))
                                .background(Color(0xFFFFF3E0), RoundedCornerShape(50.dp))
                                .border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(50.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            if (tienPainter != null) {
                                androidx.compose.foundation.Image(
                                    painter = tienPainter,
                                    contentDescription = "Gold",
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("🍭", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format("%,d", state.userGold),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFF4E342E)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .shadow(2.dp, RoundedCornerShape(50.dp))
                                .background(Color(0xFFFFF3E0), RoundedCornerShape(50.dp))
                                .border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(50.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            if (tienPainter != null) {
                                androidx.compose.foundation.Image(
                                    painter = tienPainter,
                                    contentDescription = "Diamond",
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("💎", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format("%,d", state.userDiamonds),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFF4E342E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // MAIN CONTENT: LEFT SHOWCASE + RIGHT TABS & GRID
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LEFT SHOWCASE PANEL (42% width)
                    Column(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title of Selected Item with high-contrast badge & tier rating
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF5D4037), RoundedCornerShape(16.dp))
                                    .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (isVi) selectedItem.nameVi else selectedItem.nameEn,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFF8E1)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            if (selectedTab == 3) {
                                val foodOwned = state.foodInventory[selectedItem.id] ?: 0
                                Text(
                                    text = if (isVi) "Số lượng đang có: $foodOwned" else "In stock: $foodOwned",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E342E)
                                )
                            } else {
                                val tierLabel = when (selectedItem.stars) {
                                    3 -> "👑 VIP (Kim cương)"
                                    2 -> "⭐⭐ Cao cấp"
                                    else -> "⭐ Thường"
                                }
                                Text(
                                    text = tierLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E342E)
                                )
                            }
                        }

                        // Center Showcase Display (3D Dice Preview or Character Pedestal)
                        val infiniteTransition = rememberInfiniteTransition()
                        val bounceScaleY by infiniteTransition.animateFloat(
                            initialValue = 0.94f,
                            targetValue = 1.06f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(900, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            // Cupcake Pedestal Base anchored at Alignment.BottomCenter
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Canvas(modifier = Modifier.padding(bottom = 6.dp).size(208.dp, 100.dp)) {
                                    val cx = size.width / 2f
                                    val cy = size.height * 0.70f
                                    // Bottom Cake Layer
                                    drawOval(
                                        color = Color(0xFF795548),
                                        topLeft = Offset(cx - 91f, cy - 15.6f),
                                        size = Size(182f, 57.2f)
                                    )
                                    // Cream Layer
                                    drawOval(
                                        color = Color(0xFFFFF8E1),
                                        topLeft = Offset(cx - 80.6f, cy - 31.2f),
                                        size = Size(161.2f, 52f)
                                    )
                                    // Whipped Frosting Swirl Top
                                    drawOval(
                                        color = Color.White,
                                        topLeft = Offset(cx - 70.2f, cy - 44.2f),
                                        size = Size(140.4f, 44.2f)
                                    )
                                    // Gold Trim Ring
                                    drawOval(
                                        color = Color(0xFFFFD54F),
                                        topLeft = Offset(cx - 70.2f, cy - 44.2f),
                                        size = Size(140.4f, 44.2f),
                                        style = Stroke(width = 3.9f)
                                    )
                                }

                                if (selectedTab == 0) {
                                    // DRAW 3D DICE PREVIEW ANCHORED ON PEDESTAL!
                                    val (diceBg, diceBorder, dotColor) = when (selectedItem.id) {
                                        "dice2" -> Triple(Color(0xFF4E2A1E), Color(0xFF8D6E63), Color(0xFFFFD54F))
                                        "dice3" -> Triple(Color(0xFFFF80AB), Color(0xFFF50057), Color.White)
                                        "dice4" -> Triple(Color(0xFF1A237E), Color(0xFF00E5FF), Color(0xFF00E5FF))
                                        "dice5" -> Triple(Color(0xFFFFD700), Color(0xFFFF8F00), Color(0xFFD50000))
                                        else -> Triple(Color(0xFFFFFDF0), Color(0xFFE2C488), Color(0xFF261204))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-42).dp)
                                            .size(96.dp)
                                            .graphicsLayer {
                                                scaleY = bounceScaleY
                                                transformOrigin = TransformOrigin(0.5f, 1.0f)
                                            }
                                            .shadow(6.dp, RoundedCornerShape(16.dp))
                                            .background(diceBg, RoundedCornerShape(16.dp))
                                            .border(3.dp, diceBorder, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        DrawDiceFace(value = 6, dotColor = dotColor)
                                    }
                                } else {
                                    // Character Avatar Preview Anchored on Pedestal
                                    val bitmap = characterBitmaps[selectedItem.id]
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-42).dp)
                                            .graphicsLayer {
                                                scaleY = bounceScaleY
                                                transformOrigin = TransformOrigin(0.5f, 1.0f)
                                            }
                                    ) {
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap,
                                                contentDescription = selectedItem.nameVi,
                                                modifier = Modifier.size(100.dp)
                                            )
                                        } else {
                                            Text(
                                                text = selectedItem.emoji,
                                                fontSize = 66.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Fixed-height status bar container (36.dp) so showcase box height is strictly constant
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .padding(bottom = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedTab == 0) {
                                val diceNote = when (selectedItem.id) {
                                    "dice5" -> if (isVi) "👑 Xúc xắc VIP (+20% tỉ lệ ra 5 & 6)" else "👑 VIP Dice (+20% prob 5 & 6)"
                                    "dice3", "dice4" -> if (isVi) "⭐ Xúc xắc May Mắn (+10% tỉ lệ ra 5 & 6)" else "⭐ Lucky Dice (+10% prob 5 & 6)"
                                    else -> if (isVi) "🎲 Xúc xắc Thường (Tỉ lệ ngẫu nhiên)" else "🎲 Standard Dice (Standard prob)"
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF4E342E), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = diceNote, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            } else if (selectedTab == 1) {
                                if (selectedItem.stars < 2) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF4E342E), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (isVi) "⭐ Nhân vật 1-Sao (Không cần cho ăn 😊)" else "⭐ 1-Star Character (No feed required 😊)",
                                            color = Color(0xFFFFD54F),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                } else {
                                    val isUnlocked = state.unlockedCharacters.contains(selectedItem.id) || selectedItem.id == "char1"
                                    val petData = state.petDataMap[selectedItem.id] ?: PetData(selectedItem.id, selectedItem.stars, 80)
                                    val displayedFullness = if (isUnlocked) petData.fullness else 100
                                    val statusColor = if (!isUnlocked) Color(0xFF4CAF50) else if (petData.isRunaway) Color(0xFFFF5252) else if (petData.fullness <= 50) Color(0xFFFF9800) else Color(0xFF4CAF50)
                                    val statusText = if (!isUnlocked) "😊 Khỏe mạnh" else if (petData.isRunaway) "🏃 Bỏ trốn" else if (petData.fullness <= 50) "😟 Đang đói" else "😊 Khỏe mạnh"

                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF4E342E), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🍖 No: ${displayedFullness}/100", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(statusText, color = statusColor, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        }
                                        val reqFood = getRequiredFoodForPet(selectedItem.id)
                                        Button(
                                            onClick = {
                                                if (!isUnlocked) {
                                                    android.widget.Toast.makeText(context, if (isVi) "Bạn chưa sở hữu linh thú này!" else "You do not own this character!", android.widget.Toast.LENGTH_SHORT).show()
                                                } else if (petData.isRunaway) {
                                                    android.widget.Toast.makeText(context, if (isVi) "Linh thú đang bỏ trốn, bạn cần chuộc lại trước!" else "Pet is runaway, ransom required first!", android.widget.Toast.LENGTH_SHORT).show()
                                                } else {
                                                    val success = viewModel.feedPet(selectedItem.id, reqFood.id)
                                                    if (success) {
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            if (isVi) "Đã cho ${selectedItem.nameVi} ăn ${reqFood.nameVi}! (+${reqFood.restoreFullness} Độ No)" else "Fed ${selectedItem.nameEn} with ${reqFood.nameEn}!",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            if (isVi) "Không đủ ${reqFood.nameVi} hoặc Kẹo mút (cần ${reqFood.priceCandy / 1000}K 🍭)!" else "Not enough ${reqFood.nameEn} or Candy!",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("Cho Ăn ${reqFood.icon}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Actions Row (Price Button + Try 3 Games Button or Buy Food)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (selectedTab == 0) {
                                // DICE TAB ACTIONS
                                val isUnlocked = state.unlockedDice.contains(selectedItem.id) || selectedItem.id == "dice1"
                                val isEquipped = state.selectedDice == selectedItem.id

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isEquipped) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(42.dp)
                                                .background(Color(0xFF81C784), RoundedCornerShape(22.dp))
                                                .border(2.dp, Color.White, RoundedCornerShape(22.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isVi) "✓ Đã trang bị" else "✓ Equipped",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp
                                            )
                                        }
                                    } else if (isUnlocked) {
                                        Button(
                                            onClick = {
                                                viewModel.selectDice(selectedItem.id)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                                            shape = RoundedCornerShape(22.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(42.dp)
                                        ) {
                                            Text(
                                                text = if (isVi) "Sử dụng" else "Equip",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                val success = viewModel.buyDice(selectedItem.id, selectedItem.goldPrice, selectedItem.diamondPrice)
                                                if (!success) {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        if (isVi) "Không đủ Kẹo mút hoặc Kim cương!" else "Not enough Gold or Diamonds!",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                                            shape = RoundedCornerShape(22.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                        ) {
                                            Text(
                                                text = if (selectedItem.goldPrice > 0) "🍭 ${selectedItem.goldPrice / 1000}K" else "💎 ${selectedItem.diamondPrice}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp
                                            )
                                        }

                                        // Dice Trial Button (3 Uses)
                                        val leftUses = state.trialDiceUsesLeft
                                        val isTrialActive = state.trialDice == selectedItem.id
                                        val canTrial = leftUses > 0 || isTrialActive
                                        Button(
                                            onClick = {
                                                if (canTrial) {
                                                    viewModel.activateTrialDice(selectedItem.id)
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        if (isVi) "Đã chọn dùng thử ${selectedItem.nameVi} (Còn $leftUses lượt)!" else "Selected ${selectedItem.nameEn} trial ($leftUses left)!",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            enabled = canTrial,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isTrialActive) Color(0xFF2E7D32) else if (canTrial) Color(0xFF66BB6A) else Color(0xFFB0BEC5),
                                                disabledContainerColor = Color(0xFFB0BEC5)
                                            ),
                                            shape = RoundedCornerShape(22.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                        ) {
                                            Text(
                                                text = if (isTrialActive) (if (isVi) "✓ Đang thử ($leftUses/3)" else "✓ Trial ($leftUses/3)")
                                                       else if (canTrial) (if (isVi) "🎬 Thử ($leftUses/3 lượt)" else "🎬 Try ($leftUses/3)")
                                                       else (if (isVi) "Hết lượt thử" else "No trial left"),
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            } else if (selectedTab == 1) {
                                // PAWN CHARACTER TAB ACTIONS
                                val petData = state.petDataMap[selectedItem.id] ?: PetData(selectedItem.id, selectedItem.stars, 80)
                                val isUnlocked = state.unlockedCharacters.contains(selectedItem.id) || selectedItem.id == "char1"
                                if (isUnlocked && petData.isRunaway) {
                                    Button(
                                        onClick = { showRansomDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                        shape = RoundedCornerShape(22.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                    ) {
                                        Text(
                                            text = if (isVi) "🚨 Chuộc Linh Thú (500 🍭)" else "🚨 Ransom Pet (500 🍭)",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    val isUnlocked = state.unlockedCharacters.contains(selectedItem.id) || selectedItem.id == "char1"
                                    val isEquipped = state.selectedCharacter == selectedItem.id

                                    if (isUnlocked) {
                                        if (selectedItem.stars < 2) {
                                            // 1-Star characters: No feeding required, only equip button
                                            if (isEquipped) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(42.dp)
                                                        .background(Color(0xFF81C784), RoundedCornerShape(22.dp))
                                                        .border(2.dp, Color.White, RoundedCornerShape(22.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = if (isVi) "✓ Đang dùng" else "✓ Equipped",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            } else {
                                                Button(
                                                    onClick = { viewModel.selectCharacter(selectedItem.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                    shape = RoundedCornerShape(22.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(42.dp)
                                                ) {
                                                    Text(
                                                        text = if (isVi) "Trang bị" else "Equip",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        } else {
                                            // 2+ Star characters: Feed button + Equip button row
                                            val reqFood = getRequiredFoodForPet(selectedItem.id)
                                            val foodOwned = state.foodInventory[reqFood.id] ?: 0
                                            val costLabel = if (foodOwned > 0) "Có $foodOwned" else "${reqFood.priceCandy / 1000}K 🍭"

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        val success = viewModel.feedPet(selectedItem.id, reqFood.id)
                                                        if (success) {
                                                            android.widget.Toast.makeText(
                                                                context,
                                                                if (isVi) "Đã cho ${selectedItem.nameVi} ăn ${reqFood.nameVi}! (+${reqFood.restoreFullness} Độ No)" else "Fed ${selectedItem.nameEn} with ${reqFood.nameEn}!",
                                                                android.widget.Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            android.widget.Toast.makeText(
                                                                context,
                                                                if (isVi) "Không đủ ${reqFood.nameVi} hoặc Kẹo mút (cần ${reqFood.priceCandy / 1000}K 🍭)!" else "Not enough ${reqFood.nameEn} or Candy!",
                                                                android.widget.Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                                    shape = RoundedCornerShape(22.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(42.dp)
                                                ) {
                                                    Text(
                                                        text = if (isVi) "Cho ăn ${reqFood.icon} ($costLabel)" else "Feed ${reqFood.icon} ($costLabel)",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 12.sp
                                                    )
                                                }

                                                if (isEquipped) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(42.dp)
                                                            .background(Color(0xFF81C784), RoundedCornerShape(22.dp))
                                                            .border(2.dp, Color.White, RoundedCornerShape(22.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = if (isVi) "✓ Đang dùng" else "✓ Equipped",
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = { viewModel.selectCharacter(selectedItem.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                        shape = RoundedCornerShape(22.dp),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(42.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isVi) "Trang bị" else "Equip",
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val success = viewModel.buyCharacter(selectedItem.id, selectedItem.goldPrice, selectedItem.diamondPrice)
                                                    if (!success) {
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            if (isVi) "Không đủ Kẹo mút hoặc Kim cương!" else "Not enough Gold or Diamonds!",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                                                shape = RoundedCornerShape(22.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(42.dp)
                                            ) {
                                                Text(
                                                    text = if (selectedItem.goldPrice > 0) "🍭 ${selectedItem.goldPrice / 1000}K" else "💎 ${selectedItem.diamondPrice}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp
                                                )
                                            }

                                            // Character Trial Button (3 Uses)
                                            val leftUses = state.trialCharUsesLeft
                                            val isTrialActive = state.trialCharacter == selectedItem.id
                                            val canTrial = leftUses > 0 || isTrialActive
                                            Button(
                                                onClick = {
                                                    if (canTrial) {
                                                        viewModel.activateTrialCharacter(selectedItem.id)
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            if (isVi) "Đã chọn dùng thử ${selectedItem.nameVi} (Còn $leftUses lượt)!" else "Selected ${selectedItem.nameEn} trial ($leftUses left)!",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                },
                                                enabled = canTrial,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isTrialActive) Color(0xFF2E7D32) else if (canTrial) Color(0xFF66BB6A) else Color(0xFFB0BEC5),
                                                    disabledContainerColor = Color(0xFFB0BEC5)
                                                ),
                                                shape = RoundedCornerShape(22.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(42.dp)
                                            ) {
                                                Text(
                                                    text = if (isTrialActive) (if (isVi) "✓ Đang thử ($leftUses/3)" else "✓ Trial ($leftUses/3)")
                                                           else if (canTrial) (if (isVi) "🎬 Thử ($leftUses/3 lượt)" else "🎬 Try ($leftUses/3)")
                                                           else (if (isVi) "Hết lượt thử" else "No trial left"),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (selectedTab == 3) {
                                // FOOD TAB ACTIONS: BUY FOOD WITH QUANTITY SELECTOR
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isVi) "Số lượng mua: " else "Quantity: ",
                                        color = Color(0xFF4E342E),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Button(
                                        onClick = { if (buyQuantity > 1) buyQuantity-- },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Text("-", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$buyQuantity",
                                        color = Color(0xFF3E2723),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .background(Color.White, RoundedCornerShape(6.dp))
                                            .border(1.dp, Color(0xFF8D6E63), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { if (buyQuantity < 99) buyQuantity++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Text("+", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }
                                }

                                val totalPrice = selectedItem.goldPrice * buyQuantity
                                Button(
                                    onClick = {
                                        val success = viewModel.buyFood(selectedItem.id, buyQuantity)
                                        if (success) {
                                            android.widget.Toast.makeText(
                                                context,
                                                if (isVi) "Đã mua $buyQuantity ${selectedItem.nameVi}!" else "Bought $buyQuantity ${selectedItem.nameEn}!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                            buyQuantity = 1
                                        } else {
                                            android.widget.Toast.makeText(
                                                context,
                                                if (isVi) "Không đủ Kẹo mút 🍭!" else "Not enough candy 🍭!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(22.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                ) {
                                    Text(
                                        text = if (isVi) "🛒 Mua Lương Thực (${String.format("%,d", totalPrice)} 🍭)" else "🛒 Buy Food (${String.format("%,d", totalPrice)} 🍭)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                // LOCKED TAB ACTIONS (selectedTab == 2)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .background(Color(0xFF8D6E63), RoundedCornerShape(22.dp))
                                        .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(22.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isVi) "🔒 Tạm khóa (Sắp ra mắt)" else "🔒 Feature Locked",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // RIGHT EQUIPMENT CONTAINER (58% width)
                    Column(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                    ) {
                        // TABS HEADER WITH LOCKED STATUS ON TAB 2 (Chuồng)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                Pair(0, if (isVi) "Xúc xắc" else "Dice"),
                                Pair(1, if (isVi) "Quân cờ" else "Pawns"),
                                Pair(2, if (isVi) "🔒 Chuồng" else "🔒 Base"),
                                Pair(3, if (isVi) "🍖 Thức ăn" else "🍖 Food")
                            ).forEach { (tabIdx, tabTitle) ->
                                val isActive = selectedTab == tabIdx
                                val isLockedTab = (tabIdx == 2)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .background(
                                            if (isActive) Color(0xFFB8865B) else Color(0xFF8D6E63),
                                            RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                        )
                                        .border(
                                            width = if (isActive) 2.dp else 1.dp,
                                            color = if (isActive) Color(0xFFFFD54F) else Color.Transparent,
                                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                        )
                                        .clickable {
                                            if (isLockedTab) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    if (isVi) "Tính năng Chuồng đang tạm khóa!" else "Base feature locked!",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                selectedTab = tabIdx
                                                LudoSoundSynth.playClick()
                                                val first = when (tabIdx) {
                                                    0 -> diceItems[0].id
                                                    1 -> characterItems[0].id
                                                    3 -> foodItems[0].id
                                                    else -> pedestalItems[0].id
                                                }
                                                previewItemId = first
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tabTitle,
                                        color = if (isActive) Color.White else Color(0xFFD7CCC8),
                                        fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // MAIN EQUIPMENT GRID CONTAINER
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color(0xFFB8865B), RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp, topEnd = 12.dp))
                                .border(2.5.dp, Color(0xFF8D6E63), RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp, topEnd = 12.dp))
                                .padding(10.dp)
                        ) {
                            if (selectedTab == 2) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("🔒", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (isVi) "Tính năng đang tạm khóa!" else "Feature Coming Soon!",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFF8E1)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isVi) "Mục này sẽ ra mắt ở bản cập nhật tới." else "Will be available in next update.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD7CCC8)
                                    )
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                items(currentList.size) { idx ->
                                    val item = currentList[idx]
                                    val isPreviewing = previewItemId == item.id
                                    val isUnlocked = if (selectedTab == 0) {
                                        state.unlockedDice.contains(item.id) || item.id == "dice1"
                                    } else if (selectedTab == 3) {
                                        true
                                    } else {
                                        state.unlockedCharacters.contains(item.id) || item.id == "char1"
                                    }
                                    val isEquipped = if (selectedTab == 0) {
                                        state.selectedDice == item.id
                                    } else {
                                        state.selectedCharacter == item.id
                                    }

                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .shadow(3.dp, RoundedCornerShape(16.dp))
                                            .background(Color(0xFFE5C4A3), RoundedCornerShape(16.dp))
                                            .border(
                                                width = if (isPreviewing || isEquipped) 3.5.dp else 1.5.dp,
                                                color = if (isEquipped) Color(0xFF4CAF50) else if (isPreviewing) Color(0xFFFFD54F) else Color(0xFF8D6E63),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                previewItemId = item.id
                                                LudoSoundSynth.playClick()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Item Thumbnail
                                        if (selectedTab == 0) {
                                            Text(item.emoji, fontSize = 32.sp)
                                        } else {
                                            val bm = characterBitmaps[item.id]
                                            if (bm != null) {
                                                Image(
                                                    bitmap = bm,
                                                    contentDescription = item.nameVi,
                                                    modifier = Modifier.size(46.dp)
                                                )
                                            } else {
                                                Text(item.emoji, fontSize = 32.sp)
                                            }
                                        }

                                        // Lock Overlay
                                        if (!isUnlocked) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🔒", fontSize = 20.sp)
                                            }
                                        }

                                        // Runaway Pet Overlay
                                        if (selectedTab == 1) {
                                            val petData = state.petDataMap[item.id] ?: PetData(item.id, item.stars, 80)
                                            val isUnlocked = state.unlockedCharacters.contains(item.id) || item.id == "char1"
                                            if (isUnlocked && petData.isRunaway) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(16.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("🏃", fontSize = 20.sp)
                                                        Text("Bỏ trốn", color = Color(0xFFFF5252), fontWeight = FontWeight.Black, fontSize = 9.sp)
                                                    }
                                                }
                                            }
                                        }

                                        // Checkmark badge if equipped
                                        if (isEquipped) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(4.dp)
                                                    .size(20.dp)
                                                    .background(Color(0xFF4CAF50), CircleShape)
                                                    .border(1.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("✓", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }
                                        }

                                        // Star Rating (Only for non-food items)
                                        if (selectedTab != 3) {
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 3.dp)
                                            ) {
                                                repeat(item.stars) {
                                                    Text("⭐", fontSize = 8.sp)
                                                }
                                            }
                                        } else {
                                            val foodOwnedCount = state.foodInventory[item.id] ?: 0
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(3.dp)
                                                    .background(Color(0xFF3E2723), RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "x$foodOwnedCount",
                                                    color = Color(0xFFFFD54F),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    // QUICK FEED DIALOG
    if (showQuickFeedDialog) {
        val reqFood = getRequiredFoodForPet(previewItemId)
        val foodOwned = state.foodInventory[reqFood.id] ?: 0
        Dialog(
            onDismissRequest = { showQuickFeedDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .clickable { showQuickFeedDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4E342E)),
                    border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clickable(enabled = false) {}
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(reqFood.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isVi) "CHO ${selectedItem.nameVi.uppercase()} ĂN" else "FEED ${selectedItem.nameEn.uppercase()}",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        val petData = state.petDataMap[previewItemId] ?: PetData(previewItemId, 2, 80)
                        Text(
                            text = if (isVi) "Linh thú này chỉ ăn ${reqFood.nameVi}!\nĐộ No hiện tại: ${petData.fullness}/100 🍖"
                            else "This pet only eats ${reqFood.nameEn}!\nCurrent Fullness: ${petData.fullness}/100 🍖",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF3E2723), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(reqFood.icon, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isVi) reqFood.nameVi else reqFood.nameEn,
                                        color = Color(0xFFFFD54F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (foodOwned > 0) "Có sẵn: $foodOwned" else "Giá: ${reqFood.priceCandy / 1000}K 🍭",
                                        color = Color(0xFFD7CCC8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    val isUnlocked = state.unlockedCharacters.contains(previewItemId) || previewItemId == "char1" || previewItemId == "character1"
                                    val petData = state.petDataMap[previewItemId] ?: PetData(previewItemId, 2, 80)
                                    if (!isUnlocked) {
                                        android.widget.Toast.makeText(
                                            context,
                                            if (isVi) "Bạn chưa sở hữu nhân vật này!" else "You do not own this character!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (petData.isRunaway) {
                                        android.widget.Toast.makeText(
                                            context,
                                            if (isVi) "Linh thú đang bỏ trốn, bạn cần chuộc lại trước!" else "Pet is runaway, ransom required first!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        val success = viewModel.feedPet(previewItemId, reqFood.id)
                                        if (success) {
                                            android.widget.Toast.makeText(
                                                context,
                                                if (isVi) "Đã cho ${selectedItem.nameVi} ăn ${reqFood.nameVi}!" else "Fed ${selectedItem.nameEn}!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                            showQuickFeedDialog = false
                                        } else {
                                            android.widget.Toast.makeText(
                                                context,
                                                if (isVi) "Không đủ Kẹo 🍭 hoặc Thức ăn!" else "Not enough candy 🍭 or food!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (foodOwned > 0) "Cho ăn" else "🍭 ${reqFood.priceCandy / 1000}K",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showQuickFeedDialog = false }) {
                                Text(if (isVi) "Đóng" else "Close", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // RANSOM PET DIALOG
    if (showRansomDialog) {
        Dialog(
            onDismissRequest = { showRansomDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .clickable { showRansomDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4E342E)),
                    border = BorderStroke(2.dp, Color(0xFFFF5252)),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clickable(enabled = false) {}
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚨", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isVi) "CHUỘC LINH THÚ BỎ TRỐN" else "RANSOM RUNAWAY PET",
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = if (isVi)
                                "Linh thú ${selectedItem.nameVi} đã bỏ trốn do bị đói quá lâu (Độ no = 0)!\n\nBạn có muốn trả phí chuộc 5,250 🍭 Kẹo (1.5x Sữa cao cấp) để đưa Linh Thú trở lại không?"
                            else
                                "Pet ${selectedItem.nameEn} ran away due to hunger!\n\nPay 5,250 🍭 candy ransom to bring your pet back?",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showRansomDialog = false }) {
                                Text(if (isVi) "Hủy" else "Cancel", color = Color(0xFFD7CCC8))
                            }
                            Button(
                                onClick = {
                                    val success = viewModel.ransomPet(previewItemId)
                                    if (success) {
                                        android.widget.Toast.makeText(
                                            context,
                                            if (isVi) "Đã chuộc thành công ${selectedItem.nameVi}!" else "Successfully ransomed ${selectedItem.nameEn}!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        showRansomDialog = false
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            if (isVi) "Không đủ Kẹo 🍭!" else "Not enough candy 🍭!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isVi) "Chuộc (5,250 🍭)" else "Ransom (5,250 🍭)", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
}


// ==================================================
// 1. TẠO / VÀO PHÒNG DIALOG (TEAM LOBBY OPTION DIALOG)
// ==================================================
@Composable
fun TeamLobbyOptionDialog(
    state: GameState,
    viewModel: LudoViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Create, 1: Join
    var selectedGameType by remember { mutableStateOf("STANDARD") } // "STANDARD" or "ARENA"
    var createPassword by remember { mutableStateOf("") }
    var joinRoomCode by remember { mutableStateOf("") }
    var joinPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isVi = state.language == "vi"

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            border = BorderStroke(3.dp, Color(0xFF8D6E63))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isVi) "CÔNG TRÌNH GHÉP ĐỘI" else "TEAM LOBBY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4E342E)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0D0C0), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0; errorMessage = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) Color(0xFFFF7043) else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isVi) "TẠO PHÒNG" else "CREATE",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color.White else Color(0xFF4E342E)
                        )
                    }
                    Button(
                        onClick = { selectedTab = 1; errorMessage = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) Color(0xFFFF7043) else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isVi) "VÀO PHÒNG" else "JOIN",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.White else Color(0xFF4E342E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Create Room Form (Default to STANDARD)
                    var selectedGameType by remember { mutableStateOf("STANDARD") }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isVi) "Mật khẩu phòng (không bắt buộc):" else "Room password (optional):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = createPassword,
                        onValueChange = { createPassword = it },
                        placeholder = { Text("Nhập mật khẩu...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(2.dp, Color(0xFF8D6E63))
                        ) {
                            Text(if (isVi) "ĐÓNG" else "CLOSE", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                        }

                        Button(
                            onClick = {
                                LudoSoundSynth.playClick()
                                viewModel.createTeamRoom(createPassword, selectedGameType)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (isVi) "TẠO PHÒNG MỚI" else "CREATE ROOM", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                } else {
                    // Join Room Form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isVi) "Mã phòng (4 chữ số):" else "Room code (4 digits):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = joinRoomCode,
                                onValueChange = { if (it.length <= 4) joinRoomCode = it },
                                placeholder = { Text("Ví dụ: 4829") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isVi) "Mật khẩu phòng:" else "Room password:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = joinPassword,
                                onValueChange = { joinPassword = it },
                                placeholder = { Text("Mật khẩu...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(2.dp, Color(0xFF8D6E63))
                        ) {
                            Text(if (isVi) "ĐÓNG" else "CLOSE", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                        }

                        Button(
                            onClick = {
                                if (joinRoomCode.isBlank()) {
                                    errorMessage = "Vui lòng nhập mã phòng!"
                                    return@Button
                                }
                                LudoSoundSynth.playClick()
                                viewModel.joinTeamRoom(joinRoomCode, joinPassword) { success, err ->
                                    if (!success) {
                                        errorMessage = err ?: "Không thể vào phòng!"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (isVi) "THAM GIA PHÒNG" else "JOIN ROOM", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SingleGrandStaticIslandStage(
    room: MultiplayerRoom?,
    characterBitmaps: Map<String, ImageBitmap>,
    activePawnEmotes: Map<String, Pair<String, Long>>,
    isVi: Boolean,
    modifier: Modifier = Modifier
) {
    val slots = listOf(
        Triple("player1", PlayerColor.RED, 0.208f),
        Triple("player2", PlayerColor.BLUE, 0.408f),
        Triple("player3", PlayerColor.YELLOW, 0.608f),
        Triple("player4", PlayerColor.GREEN, 0.808f)
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val screenW = maxWidth
        val screenH = maxHeight
        val itemWidth = 110.dp

        slots.forEach { (slotId, color, xFraction) ->
            val player = room?.players?.get(slotId)
            val chatData = activePawnEmotes[slotId]
            val isChatActive = chatData != null && chatData.second > System.currentTimeMillis()

            val centerX = screenW * xFraction

            Box(
                modifier = Modifier
                    .offset(x = centerX - (itemWidth / 2), y = 0.dp)
                    .width(itemWidth)
                    .height(screenH * 0.58f),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Chat Bubble above Hero
                    if (isChatActive && !chatData?.first.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .shadow(6.dp, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(2.dp, color.baseColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = chatData!!.first,
                                color = Color(0xFF3E2723),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Player Name Badge or "Chủ Phòng" or "Đang chờ..."
                    if (player != null) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            border = BorderStroke(2.dp, if (player.isHost) Color(0xFFFF9800) else color.baseColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (player.isHost) {
                                    Text("👑", fontSize = 12.sp)
                                    Text(
                                        text = if (isVi) "Chủ Phòng" else "Host",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFD84315)
                                    )
                                } else {
                                    Text(
                                        text = player.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF3E2723),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xCC37474F),
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = if (isVi) "Đang chờ..." else "Waiting...",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // HERO CHARACTER / SILHOUETTE
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .zIndex(2f),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (player != null) {
                            val bm = characterBitmaps[player.skinId] ?: characterBitmaps["char1"]
                            if (bm != null) {
                                Image(
                                    bitmap = bm,
                                    contentDescription = player.name,
                                    modifier = Modifier.size(78.dp)
                                )
                            } else {
                                Text("🐥", fontSize = 56.sp)
                            }
                        } else {
                            // Dark silhouette for waiting hero with white "?" inside
                            val defaultBm = characterBitmaps["char1"] ?: characterBitmaps.values.firstOrNull()
                            Box(contentAlignment = Alignment.Center) {
                                if (defaultBm != null) {
                                    Image(
                                        bitmap = defaultBm,
                                        contentDescription = "Empty Slot Silhouette",
                                        modifier = Modifier
                                            .size(74.dp)
                                            .alpha(0.85f),
                                        colorFilter = ColorFilter.tint(Color(0xFF263238), BlendMode.SrcIn)
                                    )
                                } else {
                                    Text("🐥", fontSize = 52.sp, color = Color(0xFF263238), modifier = Modifier.alpha(0.85f))
                                }
                                Text(
                                    text = "?",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================================================
// 2. MÀN HÌNH CHỜ / LOBBY UI (GHÉP ĐỘI)
// ==================================================
@Composable
fun GhepDoiLobbyScreen(
    state: GameState,
    viewModel: LudoViewModel,
    characterBitmaps: Map<String, ImageBitmap>,
    onExit: () -> Unit
) {
    val room = state.activeRoom
    val isVi = state.language == "vi"
    val backgroundTaophongPainter = rememberAssetImagePainter("IMG/background_taophong.jpg") ?: rememberAssetImagePainter("IMG/background.png")
    val maphongPainter = rememberAssetImagePainter("IMG/maphong.png")
    var showChatDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF81D4FA))
    ) {
        // 1. Full Landscape Background Image (background_taophong.jpg)
        if (backgroundTaophongPainter != null) {
            Image(
                painter = backgroundTaophongPainter,
                contentDescription = "Room Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        // 2. Single Grand Stage positioned relative to background pedestals
        SingleGrandStaticIslandStage(
            room = room,
            characterBitmaps = characterBitmaps,
            activePawnEmotes = state.activePawnEmotes,
            isVi = isVi,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Top Header Bar & Bottom Action Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP HEADER BAR: [THOÁT] --- [MÃ PHÒNG] --- [TÚI ĐỒ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Exit Button
                Button(
                    onClick = {
                        LudoSoundSynth.playClick()
                        onExit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📖", fontSize = 16.sp)
                        Text(if (isVi) "THOÁT" else "EXIT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }

                // Room Code Badge using maphong.png
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(min = 200.dp, max = 260.dp)
                ) {
                    if (maphongPainter != null) {
                        Image(
                            painter = maphongPainter,
                            contentDescription = "Room Code Frame",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFFFBEA),
                            border = BorderStroke(2.dp, Color(0xFFFFD54F))
                        ) {
                            Spacer(modifier = Modifier.fillMaxSize())
                        }
                    }
                    Text(
                        text = "MÃ PHÒNG: ${room?.roomId ?: "4748"}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3E2723),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Inventory Button
                Button(
                    onClick = {
                        LudoSoundSynth.playClick()
                        viewModel.openInventoryInLobby()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🎒", fontSize = 16.sp)
                        Text(if (isVi) "TÚI ĐỒ" else "INVENTORY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }

            // BOTTOM BAR: CHAT ICON BUTTON & STATUS BANNER / START GAME
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // CHAT ICON BUTTON
                FloatingActionButton(
                    onClick = {
                        LudoSoundSynth.playClick()
                        showChatDialog = true
                    },
                    containerColor = Color(0xFF0091EA),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(52.dp)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Text("💬", fontSize = 24.sp)
                }

                // STATUS / START GAME BANNER
                val isHost = room?.players?.get(state.myPlayerId)?.isHost == true || state.myPlayerId == "player1"
                val playerCount = room?.players?.size ?: 1
                val canStart = isHost && playerCount >= 2

                if (isHost && canStart) {
                    Button(
                        onClick = {
                            LudoSoundSynth.playClick()
                            viewModel.startTeamLobbyGame()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier
                            .height(48.dp)
                            .widthIn(min = 280.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Text(
                            text = if (isVi) "BẮT ĐẦU GAME 🎮" else "START GAME 🎮",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFFFFBEA),
                        border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .height(48.dp)
                            .widthIn(min = 280.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
                            val statusMsg = if (isHost) {
                                if (isVi) "ĐĂNG CHỜ NGƯỜI CHƠI (${playerCount}/4)..." else "WAITING FOR PLAYERS (${playerCount}/4)..."
                            } else {
                                if (isVi) "ĐANG CHỜ CHỦ PHÒNG BẮT ĐẦU..." else "WAITING FOR HOST..."
                            }
                            Text(
                                text = statusMsg,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF3E2723)
                            )
                        }
                    }
                }

                // Spacer to balance Chat FAB on left
                Spacer(modifier = Modifier.size(52.dp))
            }
        }
    }

    // EXPANDABLE CHAT DIALOG
    if (showChatDialog) {
        Dialog(onDismissRequest = { showChatDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = BorderStroke(3.dp, Color(0xFF8D6E63))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isVi) "GỬI TIN NHẮN PHÒNG 💬" else "SEND LOBBY CHAT 💬",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3E2723)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.lobbyChatInput,
                        onValueChange = { if (it.length <= 30) viewModel.setLobbyChatInput(it) },
                        placeholder = { Text(if (isVi) "Nhập tin nhắn (tối đa 30 ký tự)..." else "Message (max 30 chars)...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showChatDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isVi) "ĐÓNG" else "CLOSE", color = Color(0xFF5D4037), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                LudoSoundSynth.playClick()
                                viewModel.sendLobbyChat(state.lobbyChatInput)
                                showChatDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isVi) "GỬI TIN NHẮN" else "SEND", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

// ==================================================
// 3. POP-UP TÚI ĐỒ (INVENTORY) & TRANG BỊ SKIN QUÂN CỜ
// ==================================================
@Composable
fun InventoryDialogInLobby(
    state: GameState,
    characterBitmaps: Map<String, ImageBitmap>,
    onEquipSkin: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val isVi = state.language == "vi"
    val skins = PawnSkinCatalog.skins

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = BorderStroke(3.dp, Color(0xFF8D6E63))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isVi) "🎒 TÚI ĐỒ QUÂN CỜ" else "🎒 PAWN SKIN INVENTORY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4E342E)
                )

                Text(
                    text = if (isVi) "Chọn skin quân cờ để đồng bộ thời gian thực" else "Equip pawn skin to sync in realtime",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(skins.size) { index ->
                        val skin = skins[index]
                        val isEquipped = state.selectedCharacter == skin.id

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isEquipped) Color(0xFFE8F5E9) else Color.White
                            ),
                            border = BorderStroke(
                                width = if (isEquipped) 2.5.dp else 1.dp,
                                color = if (isEquipped) Color(0xFF4CAF50) else Color(0xFFBCAAA4)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // 1. 2D/3D PREVIEW IMAGE
                                val bm = characterBitmaps[skin.id]
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color(0xFFFFF8E1), CircleShape)
                                        .border(1.5.dp, Color(0xFFFFB74D), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bm != null) {
                                        Image(
                                            bitmap = bm,
                                            contentDescription = skin.name,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    } else {
                                        Text("🧁", fontSize = 36.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2. UNIQUE INDIVIDUAL PAWN NAME (NOT COLOR NAMES)
                                Text(
                                    text = skin.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF3E2723),
                                    maxLines = 1
                                )

                                Text(
                                    text = skin.description,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 3. BUTTON "TRANG BỊ" / "ĐANG SỬ DỤNG"
                                val isSkinUnlocked = state.unlockedCharacters.contains(skin.id) || skin.id == "char1"
                                val isRunaway = isSkinUnlocked && (state.petDataMap[skin.id]?.isRunaway == true)
                                val context = LocalContext.current

                                Button(
                                    onClick = {
                                        if (isRunaway) {
                                            android.widget.Toast.makeText(context, if (isVi) "Linh thú đã bỏ trốn, hãy chuộc lại ở Cửa Hàng!" else "Pet has run away, ransom in Shop!", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            LudoSoundSynth.playClick()
                                            onEquipSkin(skin.id, skin.iconResName)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRunaway) Color(0xFFD32F2F) else if (isEquipped) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Text(
                                        text = if (isRunaway) "🏃 Bỏ trốn" else if (isEquipped) (if (isVi) "✓ Đang Dùng" else "✓ Equipped") else (if (isVi) "Trang Bị" else "Equip"),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isVi) "HOÀN TẤT" else "DONE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}





