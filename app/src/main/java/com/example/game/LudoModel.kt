package com.example.game

import androidx.compose.ui.graphics.Color

enum class PlayerColor(val displayName: String, val baseColor: Color, val accentColor: Color) {
    RED("Đỏ", Color(0xFFFF5252), Color(0xFFFF8A80)),
    GREEN("Xanh Lá", Color(0xFF4CAF50), Color(0xFFB9F6CA)),
    YELLOW("Vàng", Color(0xFFFFEB3B), Color(0xFFFFFF8D)),
    BLUE("Xanh Dương", Color(0xFF2196F3), Color(0xFF82B1FF))
}

fun slotToColor(slotId: String): PlayerColor = when (slotId) {
    "player1" -> PlayerColor.RED
    "player2" -> PlayerColor.GREEN
    "player3" -> PlayerColor.YELLOW
    "player4" -> PlayerColor.BLUE
    else -> PlayerColor.RED
}

fun colorToSlot(color: PlayerColor): String = when (color) {
    PlayerColor.RED -> "player1"
    PlayerColor.GREEN -> "player2"
    PlayerColor.YELLOW -> "player3"
    PlayerColor.BLUE -> "player4"
}

enum class GameMode {
    ONLINE,      // Simulated online matchmaking with custom profiles and bots
    OFFLINE,     // Play against offline AI bots
    WITH_FRIENDS,// Local multiplayer with friends
    ARENA,       // Competitive tournament mode with high gold stakes and smart bots
    CO_VINA,     // Cờ Vina Fast Mode (2 pawns to win, teleport portals, skill cards, combo bump rewards)
    TEAM_LOBBY   // Realtime Firebase Team Lobby "Ghép đội" mode
}

data class RoomPlayer(
    val id: String = "", // "player1", "player2", "player3", "player4"
    val name: String = "",
    val skinId: String = "char1",
    val skinIcon: String = "IMG/char1.png",
    val isHost: Boolean = false,
    val active: Boolean = true,
    val color: PlayerColor = PlayerColor.RED
)

data class RoomChat(
    val sender: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

data class MultiplayerRoom(
    val roomId: String = "",
    val password: String = "",
    val gameType: String = "STANDARD", // "STANDARD" or "ARENA"
    val status: String = "waiting", // "waiting", "playing", "ended"
    val turn: String = "player1",
    val players: Map<String, RoomPlayer> = emptyMap(),
    val chat: RoomChat? = null,
    val diceValue: Int = 1,
    val pawnStates: String = "",
    val gameStatusStr: String = "WAITING_FOR_ROLL"
)

data class PawnSkinInfo(
    val id: String,
    val name: String,
    val iconResName: String,
    val description: String = ""
)

object PawnSkinCatalog {
    val skins = listOf(
        PawnSkinInfo("char1", "Vua Kẹo Bánh", "IMG/Character/Character1.png", "Chúa tể bánh bơ ngọt ngào"),
        PawnSkinInfo("char2", "Dâu Tây Ngọt Ngào", "IMG/Character/Character2.png", "Công chúa mứt dâu thơm lừng"),
        PawnSkinInfo("char3", "Bánh Donut Vui Nhộn", "IMG/Character/Character3.png", "Thần bánh Donut phô mai"),
        PawnSkinInfo("char4", "Gấu Nâu Chiến Thần", "IMG/Character/Character4.png", "Chiến binh gấu trúc nhồi bông"),
        PawnSkinInfo("char5", "Ngựa Bạch Tuyết Thần Tốc", "IMG/Character/Character5.png", "Chiến mã bạch tuyết huyền thoại thần tốc"),
        PawnSkinInfo("char6", "Ngựa Nâu Vương Giả", "IMG/Character/Character6.png", "Chiến mã vương giả dũng cảm"),
        PawnSkinInfo("char7", "Trâu Thần Tài", "IMG/Character/Character7.png", "Linh thú Trâu Thần Tài Hoàng Gia cao cấp"),
        PawnSkinInfo("char8", "Trâu Đen Bá Vương", "IMG/Character/Character8.png", "Trâu đen bá chủ dũng mãnh"),
        PawnSkinInfo("char9", "Lạc Đà Alpaca Tinh Tinh", "IMG/Character/Character9.png", "Lạc đà Alpaca đáng yêu miền núi"),
        PawnSkinInfo("char10", "Rái Cá Thông Thái", "IMG/Character/Character10.png", "Rái cá lội nước thông minh"),
        PawnSkinInfo("char11", "Tắc Kè Biến Hình", "IMG/Character/Character11.png", "Bậc thầy ngụy trang màu sắc"),
        PawnSkinInfo("char12", "Cáo Tuyết Huyền Thoại", "IMG/Character/Character12.png", "Cáo tinh anh rừng tuyết"),
        PawnSkinInfo("char13", "Gấu Trúc Kungfu", "IMG/Character/Character13.png", "Gấu trúc võ lâm thượng thừa"),
        PawnSkinInfo("char14", "Kỳ Giông Mexico (VIP)", "IMG/Character/Character14.png", "Kỳ giông Axolotl hồng cực hiếm"),
        PawnSkinInfo("char15", "Chim Công Rực Rỡ (VIP)", "IMG/Character/Character15.png", "Chim công khoe sắc lộng lẫy"),
        PawnSkinInfo("char16", "Nhím Gai Thần Tốc (VIP)", "IMG/Character/Character16.png", "Nhím siêu tốc độ siêu phàm"),
        PawnSkinInfo("char17", "Cánh Cụt Băng Giá (VIP)", "IMG/Character/Character17.png", "Chim cánh cụt Nam Cực VIP"),
        PawnSkinInfo("char18", "Cá Sấu Chúa (VIP)", "IMG/Character/Character18.png", "Bá chủ đầm lầy cá sấu"),
        PawnSkinInfo("pawn_dragon", "Chiến Binh Rồng", "IMG/Character/Character1.png", "Rồng lửa chiến thần dũng cảm"),
        PawnSkinInfo("pawn_frost", "Ngựa Bạch Tuyết Thần Tốc", "IMG/Character/Character5.png", "Chiến mã bạch tuyết dũng cảm")
    )

    fun getSkin(id: String): PawnSkinInfo {
        return skins.find { it.id == id } ?: skins[0]
    }
}

data class PlayerStats(
    val guestId: String,
    val name: String,
    val avatarId: Int, // index to a preselected avatar
    val gold: Int,
    val diamonds: Int,
    val totalGames: Int,
    val wins: Int,
    val winRate: Int // percentage
)

data class Pawn(
    val id: Int, // 0..3
    val color: PlayerColor,
    // -1 means in Base, 0..50 on main track, 51..55 on home path, 56 at center home
    val stepCount: Int = -1,
    val isBumping: Boolean = false,
    val bumpProgress: Float = 0f,
    val isHopping: Boolean = false,
    val hopProgress: Float = 0f, // for jump animation interpolation
    val isSuperHop: Boolean = false
)

data class LudoPlayer(
    val color: PlayerColor,
    val name: String,
    val isBot: Boolean,
    val avatarId: Int,
    val totalGames: Int = 120,
    val wins: Int = 65,
    val winRate: Int = 54,
    val gold: Int = 2500,
    val diamonds: Int = 50,
    val isOnline: Boolean = true,
    val characterSkin: String = "char1"
)

enum class EmoteType(val symbol: String, val soundName: String) {
    ANGRY("😡", "angry"),
    CRY("😭", "cry"),
    LOVE("❤️", "love"),
    LAUGH("😂", "laugh"),
    SLEEPY("😴", "sleepy"),
    APPLE("🍎", "throw")
}

data class ActiveEmote(
    val playerColor: PlayerColor,
    val emote: EmoteType,
    val timestamp: Long,
    val targetColor: PlayerColor? = null,
    val chatText: String? = null
)

data class PetData(
    val petId: String,
    val star: Int = 1,
    val fullness: Int = 100, // 0 - 100
    val lastFedTimestamp: Long = System.currentTimeMillis() / 1000L, // Unix Epoch seconds
    val isRunaway: Boolean = false
)

data class FoodItem(
    val id: String,
    val nameVi: String,
    val nameEn: String,
    val priceCandy: Int, // Soft currency 🍭
    val restoreFullness: Int,
    val descriptionVi: String,
    val descriptionEn: String,
    val icon: String
)

object FoodCatalog {
    val items = listOf(
        FoodItem("food_hoa_qua", "Hoa Quả Tươi 🍎", "Fresh Fruits", 200, 45, "Thức ăn dinh dưỡng dành cho Cáo & Gấu (+45 Độ No)", "Favorite food for Bears & Foxes (+45 Fullness)", "🍎"),
        FoodItem("food_co", "Cỏ Tươi Xanh 🌿", "Fresh Grass", 150, 40, "Nguồn năng lượng dồi dào dành cho Ngựa & Lạc Đà Alpaca (+40 Độ No)", "Rich energy for Horses & Alpacas (+40 Fullness)", "🌿"),
        FoodItem("food_rom", "Rơm Vàng Thơm 🌾", "Golden Hay", 250, 60, "Bữa ăn cao cấp thượng hạng dành cho Trâu Vàng (+60 Độ No)", "High nutrition food for Bulls (+60 Fullness)", "🌾"),
        FoodItem("food_ca", "Cá Tươi 🐟", "Fresh Fish", 200, 45, "Thức ăn khoái khẩu cho Rái Cá, Kỳ Giông, Cánh Cụt & Cá Sấu (+45 Độ No)", "Fresh fish for Otters, Axolotls, Penguins & Crocodiles (+45 Fullness)", "🐟"),
        FoodItem("food_con_trung", "Côn Trùng 🦗", "Insects", 180, 40, "Thức ăn bổ dưỡng cho Tắc Kè, Chim Công & Nhím (+40 Độ No)", "Tasty insects for Chameleons, Peacocks & Hedgehogs (+40 Fullness)", "🦗"),
        FoodItem("food_truc", "Trúc Tươi 🎋", "Bamboo", 220, 50, "Cây trúc tươi ngọt dành riêng cho Gấu Trúc (+50 Độ No)", "Fresh sweet bamboo for Pandas (+50 Fullness)", "🎋")
    )
}

fun getRequiredFoodForPet(petId: String): FoodItem {
    val foodId = when {
        petId.contains("char3") || petId.contains("char4") || petId.contains("char12") || petId.contains("bear") || petId.contains("fox") || petId.contains("gau") || petId.contains("cao") -> "food_hoa_qua" // Trái cây 🍎 (Gấu, Cáo)
        petId.contains("char8") || petId.contains("bull") || petId.contains("trau") -> "food_rom" // Rơm 🌾 (Trâu)
        petId.contains("char5") || petId.contains("char6") || petId.contains("char7") || petId.contains("char9") || petId.contains("horse") || petId.contains("alpaca") || petId.contains("ngua") || petId.contains("lac_da") -> "food_co" // Cỏ 🌿 (Ngựa, Alpaca)
        petId.contains("char10") || petId.contains("char14") || petId.contains("char17") || petId.contains("char18") || petId.contains("otter") || petId.contains("axolotl") || petId.contains("penguin") || petId.contains("crocodile") -> "food_ca" // Cá 🐟 (Rái cá, Kỳ giông, Cánh cụt, Cá sấu)
        petId.contains("char11") || petId.contains("char15") || petId.contains("char16") || petId.contains("chameleon") || petId.contains("peacock") || petId.contains("hedgehog") -> "food_con_trung" // Côn trùng 🦗 (Tắc kè, Chim công, Nhím)
        petId.contains("char13") || petId.contains("panda") -> "food_truc" // Thân cây trúc 🎋 (Gấu trúc)
        else -> "food_hoa_qua"
    }
    return FoodCatalog.items.firstOrNull { it.id == foodId } ?: FoodCatalog.items.first()
}

enum class GameStateStatus {
    MAIN_MENU,
    MATCHMAKING,
    INTRO_CAMERA,
    WAITING_FOR_ROLL,
    ROLLING_DICE,
    WAITING_FOR_MOVE,
    MOVING_PAWN,
    GOAL_EFFECT,
    MATCH_ENDED
}

data class GameState(
    val mode: GameMode = GameMode.OFFLINE,
    val status: GameStateStatus = GameStateStatus.MAIN_MENU,
    val players: List<LudoPlayer> = emptyList(),
    val activePlayerIndex: Int = 0,
    val diceValue: Int = 1,
    val isDiceRolling: Boolean = false,
    val consecutiveSixes: Int = 0,
    val pawns: List<Pawn> = emptyList(),
    val selectedPawnId: Int? = null,
    val bannerText: String = "",
    val turnTimeLeft: Int = 30, // seconds
    val showProfileStatsPlayer: LudoPlayer? = null,
    val activeEmotes: List<ActiveEmote> = emptyList(),
    val logs: List<String> = emptyList(),
    val isMusicOn: Boolean = true,
    val isSfxOn: Boolean = true,
    val musicVolume: Float = 0.5f,
    val sfxVolume: Float = 0.8f,
    val language: String = "vi", // "en" or "vi"
    val userGold: Int = 3000,
    val userDiamonds: Int = 10,
    val userTotalGames: Int = 120,
    val userWonGames: Int = 65,
    val foodInventory: Map<String, Int> = mapOf("food_hoa_qua" to 3, "food_co" to 5, "food_rom" to 2, "food_ca" to 3, "food_con_trung" to 3, "food_truc" to 2),
    val unlockedCharacters: Set<String> = setOf("char1"),
    val selectedCharacter: String = "char1",
    val trialCharacter: String? = null,
    val hasTrialCharUsed: Boolean = false,
    val trialCharUsesLeft: Int = 3,
    val unlockedDice: Set<String> = setOf("dice1"),
    val selectedDice: String = "dice1",
    val trialDice: String? = null,
    val hasTrialDiceUsed: Boolean = false,
    val trialDiceUsesLeft: Int = 3,
    val petDataMap: Map<String, PetData> = mapOf(
        "char1" to PetData("char1", star = 1, fullness = 100, isRunaway = false),
        "char2" to PetData("char2", star = 1, fullness = 100, isRunaway = false),
        "char3" to PetData("char3", star = 2, fullness = 100, isRunaway = false),
        "char4" to PetData("char4", star = 2, fullness = 100, isRunaway = false),
        "char5" to PetData("char5", star = 2, fullness = 100, isRunaway = false),
        "char6" to PetData("char6", star = 3, fullness = 100, isRunaway = false),
        "char7" to PetData("char7", star = 3, fullness = 100, isRunaway = false),
        "char8" to PetData("char8", star = 3, fullness = 100, isRunaway = false),
        "char9" to PetData("char9", star = 2, fullness = 100, isRunaway = false),
        "char10" to PetData("char10", star = 2, fullness = 100, isRunaway = false),
        "char11" to PetData("char11", star = 2, fullness = 100, isRunaway = false),
        "char12" to PetData("char12", star = 2, fullness = 100, isRunaway = false),
        "char13" to PetData("char13", star = 2, fullness = 100, isRunaway = false),
        "char14" to PetData("char14", star = 3, fullness = 100, isRunaway = false),
        "char15" to PetData("char15", star = 3, fullness = 100, isRunaway = false),
        "char16" to PetData("char16", star = 3, fullness = 100, isRunaway = false),
        "char17" to PetData("char17", star = 3, fullness = 100, isRunaway = false),
        "char18" to PetData("char18", star = 3, fullness = 100, isRunaway = false)
    ),
    val activePawnEmotes: Map<String, Pair<String, Long>> = emptyMap(), // key "color_id", value emoji & timestamp
    val lastClaimedRewardTimestamp: Long = 0L,
    val lastWeeklyWheelTimestamp: Long = 0L,
    val portalPawnId: Int? = null,
    val portalPawnColor: PlayerColor? = null,
    val portalProgress: Float = 0f,
    val matchRewardText: String = "",
    val playerName: String = "",
    val boardShakeTrigger: Long = 0L,
    val shieldActivePlayers: Set<PlayerColor> = emptySet(),
    val shieldCharges: Map<PlayerColor, Int> = mapOf(PlayerColor.RED to 1, PlayerColor.GREEN to 1, PlayerColor.YELLOW to 1, PlayerColor.BLUE to 1),
    val rerollCharges: Map<PlayerColor, Int> = mapOf(PlayerColor.RED to 1, PlayerColor.GREEN to 1, PlayerColor.YELLOW to 1, PlayerColor.BLUE to 1),
    val rocketCharges: Map<PlayerColor, Int> = mapOf(PlayerColor.RED to 1, PlayerColor.GREEN to 1, PlayerColor.YELLOW to 1, PlayerColor.BLUE to 1),
    val bumpComboCount: Map<PlayerColor, Int> = emptyMap(),
    val showSuperBumpBanner: Boolean = false,
    val superBumpText: String = "",
    val activeRoom: MultiplayerRoom? = null,
    val myPlayerId: String = "player1",
    val showTeamLobbyDialog: Boolean = false,
    val showInventoryDialogInLobby: Boolean = false,
    val lobbyChatInput: String = ""
)

// Board Coordinates
object LudoBoardConfig {
    // 52-tile outer track coordinates on the 15x15 grid
    val outerTrack = listOf(
        Pair(0, 6), Pair(1, 6), Pair(2, 6), Pair(3, 6), Pair(4, 6), Pair(5, 6), // Left arm top row
        Pair(6, 5), Pair(6, 4), Pair(6, 3), Pair(6, 2), Pair(6, 1), Pair(6, 0), // Top arm left column
        Pair(7, 0), // Connection
        Pair(8, 0), Pair(8, 1), Pair(8, 2), Pair(8, 3), Pair(8, 4), Pair(8, 5), // Top arm right column
        Pair(9, 6), Pair(10, 6), Pair(11, 6), Pair(12, 6), Pair(13, 6), Pair(14, 6), // Right arm top row
        Pair(14, 7), // Connection
        Pair(14, 8), Pair(13, 8), Pair(12, 8), Pair(11, 8), Pair(10, 8), Pair(9, 8), // Right arm bottom row
        Pair(8, 9), Pair(8, 10), Pair(8, 11), Pair(8, 12), Pair(8, 13), Pair(8, 14), // Bottom arm right column
        Pair(7, 14), // Connection
        Pair(6, 14), Pair(6, 13), Pair(6, 12), Pair(6, 11), Pair(6, 10), Pair(6, 9), // Bottom arm left column
        Pair(5, 8), Pair(4, 8), Pair(3, 8), Pair(2, 8), Pair(1, 8), Pair(0, 8), // Left arm bottom row
        Pair(0, 7) // Connection
    )

    // Starting indices of players in the outer track
    val playerStartTrackIndex = mapOf(
        PlayerColor.RED to 40,      // (6, 13)
        PlayerColor.GREEN to 1,     // (1, 6)
        PlayerColor.YELLOW to 14,    // (8, 1)
        PlayerColor.BLUE to 27      // (13, 8)
    )

    // 5-tile home path coordinates for each player leading up to the center
    val homePaths = mapOf(
        PlayerColor.RED to listOf(Pair(7, 13), Pair(7, 12), Pair(7, 11), Pair(7, 10), Pair(7, 9)),
        PlayerColor.GREEN to listOf(Pair(1, 7), Pair(2, 7), Pair(3, 7), Pair(4, 7), Pair(5, 7)),
        PlayerColor.YELLOW to listOf(Pair(7, 1), Pair(7, 2), Pair(7, 3), Pair(7, 4), Pair(7, 5)),
        PlayerColor.BLUE to listOf(Pair(13, 7), Pair(12, 7), Pair(11, 7), Pair(10, 7), Pair(9, 7))
    )

    // Center Home coordinates
    val centerHomes = mapOf(
        PlayerColor.RED to Pair(7, 8),
        PlayerColor.GREEN to Pair(6, 7),
        PlayerColor.YELLOW to Pair(7, 6),
        PlayerColor.BLUE to Pair(8, 7)
    )

    // Safe Spot Indices on the 52-tile outer track (8 total)
    val safeSpotIndices = setOf(1, 8, 14, 21, 27, 34, 40, 47)

    // Mini-grid positions of pawns in base (4 pawns per player centered in 2x2 grid)
    val basePawnPositions = mapOf(
        PlayerColor.RED to listOf(Pair(11.0f, 11.0f), Pair(12.0f, 11.0f), Pair(11.0f, 12.0f), Pair(12.0f, 12.0f)),
        PlayerColor.GREEN to listOf(Pair(2.0f, 11.0f), Pair(3.0f, 11.0f), Pair(2.0f, 12.0f), Pair(3.0f, 12.0f)),
        PlayerColor.YELLOW to listOf(Pair(2.0f, 2.0f), Pair(3.0f, 2.0f), Pair(2.0f, 3.0f), Pair(3.0f, 3.0f)),
        PlayerColor.BLUE to listOf(Pair(11.0f, 2.0f), Pair(12.0f, 2.0f), Pair(11.0f, 3.0f), Pair(12.0f, 3.0f))
    )
}
