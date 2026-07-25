package com.example.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlin.random.Random

class LudoViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var botMoveJob: Job? = null
    private var matchmakingJob: Job? = null
    private var introAnimationJob: Job? = null

    // Expanded bot names list for Vietnamese & English
    private val botNamesVi = listOf(
        "Nguyễn Văn Lộc", "Trần Thị Mai", "Phạm Tuấn Anh", "Hoàng Minh Trí", "Vũ Đức Cường",
        "Bùi Thị Lan", "Đặng Thu Hà", "Hồ Ngọc Hải", "Đinh Văn Thắng", "Lý Hữu Đạt",
        "Đoàn Thanh Tùng", "Ngô Văn Nam", "Lương Ngọc Ánh", "Châu Gia Bảo", "Mai Anh Tuấn",
        "Trịnh Xuân Khôi", "Đào Thị Bích", "Phan Đăng Khoa", "Tô Vĩnh Khang", "Thái Minh Quân",
        "Dương Đức Hiếu", "Lâm Nhật Anh", "Quách Đình Trọng", "Đỗ Gia Huy", "Chu Chí Kiên",
        "Tạ Quang Sáng", "Phùng Hữu Thiện", "Kiều Thanh Vân", "Khổng Trọng Thủy", "Cù Văn Toàn",
        "La Quốc Vượng", "Thiều Bảo Ngọc", "Tôn Thất Hùng", "Vi Văn Thái", "Bàn Văn Thành",
        "Lò Cầm Ba", "Nông Đức Phúc", "Giàng A Pháo", "Vừ A Chảo", "Đinh Tuấn Hưng",
        "Hắc Mộc Nhai", "Cô Độc Kiếm", "Thiên Tôn", "Lãnh Huyết", "Vô Tình",
        "Thiết Thủ", "Truy Mệnh", "Lạc Khôi", "Phong Vô Song", "Vân Tiếu Tiếu",
        "Mộ Dung Thu", "Nam Cung Viễn", "Bắc Tôn Võ", "Tây Môn Hận", "Đông Phương Tử",
        "Độc Cô Kiếm", "Huyết Ma Tôn", "Băng Vô Tà", "Dạ Vô Minh", "Tử Xuyên",
        "Mộng Tàn Hoa", "Sầu Hồng Nhan", "Tiêu Dao Khách", "Vô Ngấn Kiếm", "Lăng Hư Cung",
        "Phi Thiên Vũ", "Tuyết Nhan", "Cửu Châu", "Thiên Cơ Tử", "Bách Sự Thông",
        "Ngạo Thế", "Cuồng Phong", "Tịch Diệt", "Phá Môn", "Đạp Tuyết",
        "Tầm Hoan", "Túy Quyền", "Thiết Linh", "Vô Dịch", "Huyền Minh",
        "Mất Kết Nối", "Lỗi Mạng Rồi", "Tha Cho Em", "Đừng Bắn Trư", "Top 1 Server",
        "Cục Đá Trôi Sông", "Mầm Non Giải Trí", "Sát Thủ Bán Mắm", "Ăn Hành Ngập Mồm", "Đang Nạp Thẻ",
        "Máy Chủ Quá Tải", "Mình Là Bot", "Bot VIP Pro", "Người Đi Đường", "Cầm Thuốc Nổ",
        "Chạy Ngay Đi", "Bố Đánh Đấy", "Thích Thể Hiện", "Vừa Học Vừa Chơi", "Đợi Mẹ Gọi",
        "Nick Mượn", "Chơi Bằng Chân", "Gà Công Nghiệp", "Súc Vật Đồng Đội", "Hút Cần Tử",
        "Đẹp Trai Từ Bé", "FA Kinh Niên", "Trùm Giấu Tên", "Não Cá Vàng", "Não Cắn Dở",
        "Đại Ca Xóm Liều", "Chuyên Gia Phá Hoại", "Núp Lùm Trộm Gà", "AFK Lấy Đồ", "Thánh Chết Báo",
        "Phím Hỏng Rồi", "Chuột Hết Pin", "Mạng Lag Quá", "Xin Lỗi Anh Em", "Đứa Con Thần Lag",
        "Tử Thần", "Sát Thủ Giấu Mặt", "Bóng Ma", "Kẻ Hủy Diệt", "Đoạt Mệnh",
        "Hắc Ám", "Lãnh Chúa", "Quyền Lực Đen", "Đạn Xuyên Não", "Bách Phát Bách Trúng",
        "Hỏa Long", "Băng Giá", "Sấm Sét", "Cuồng Bạo", "Huyết Lệ",
        "Dạ Xoa", "Tàn Sát", "Ác Mộng", "Kẻ Phán Xét", "Thợ Săn",
        "Bão Táp", "Mũi Tên Độc", "Cuồng Sát", "Lưỡi Dao Lạnh", "Độc Sứ",
        "Kẻ Độc Hành", "Ma Tôn", "Hủy Diệt Đỉnh Cao", "Chúa Tể", "Bóng Tối",
        "Huyết Lang", "Hắc Hổ", "Sói Cô Độc", "Chim Cắt", "Mắt Đại Bàng",
        "Nhát Chém Định Mệnh", "Quyết Tử", "Bạo Chúa", "Kẻ Trừng Phạt", "Tử Địch",
        "Củ Khoai Tây", "Bánh Bao Thịt", "Trà Sữa Trân Châu", "Cà Phê Đá", "Gà Rán Giòn",
        "Mèo Lười", "Cún Ngốc", "Heo Đất", "Bé Dâu Tây", "Kem Mùa Đông",
        "Kẹo Mút", "Bánh Tráng Trộn", "Bé Hạt Tiêu", "Chuột Nhắt", "Gấu Béo",
        "Sóc Nhỏ", "Thỏ Con", "Nhím Xù", "Bún Bò Huế", "Phở Đặc Biệt",
        "Cơm Rang", "Xúc Xích Nướng", "Mít Ướt", "Táo Tàu", "Đào Tiên",
        "Sầu Riêng", "Mây Trắng", "Cỏ Ba Lá", "Hoa Cúc Nhỏ", "Mưa Mùa Hạ",
        "Gió Heo May", "Nắng Sớm", "Tuyết Mùa Đông", "Trăng Khuyết", "Bé Mũm Mĩm",
        "Nhóc Quậy", "Siêu Nhân Gà", "Vịt Bối Rối", "Gấu Ngủ Đông", "Sứa Biển Ngáo"
    )

    private val botNamesEn = listOf(
        "SweetTooth", "CandyCrush", "ChocoChip", "BerryGlaze", "CreamPuff", "HoneyBun", "SugarPlum", "Marshmallow",
        "GummyBear", "DonutKing", "CookieMonster", "WaffleQueen", "PancakePal", "CupcakeKid", "CaramelDrop", "VanillaSky",
        "JellyBean", "ToffeeTwist", "FudgeFantasy", "BrownieBite", "MacaronMagic", "TiramisuTiger", "PiePiper", "MuffinMan",
        "TartTitan", "SconeStar", "CroissantCat", "MochiMouse", "SorbetSun", "GelatoGamer", "SundaeSmile", "ParfaitPixie",
        "LicoriceLion", "BubblegumBoy", "LollipopLord", "GumdropGirl", "ChocoChamp", "SugarRush", "SweetiePie", "HoneyComb",
        "Butterscotch", "PeppermintPatty", "CottonCandy", "FruitPunch", "BerryBliss", "CherryCheer", "PeachPerfect", "PlumPrincess",
        "MangoMadness", "LemonLime", "OrangeZest", "KiwiKid", "BananaSplit", "PineapplePal", "CoconutCool", "StrawberryShort",
        "RaspberryRipple", "BlackberryBoss", "BlueberryBoy", "MelonMagic", "AppleAngi", "GrapeGlow", "FigFantastic", "DateDream",
        "NuttyProfessor", "AlmondJoy", "HazelnutHero", "PistachioPete", "CashewCaptain", "WalnutWizard", "PeanutPower", "PecanPie",
        "ChestnutChief", "MacadamiaMac", "SesameSweet", "MapleMaster", "SyrupSam", "CinnamonCurl", "NutmegKnight", "VanillaViper",
        "CocoaCommander", "EspressoEagle", "LatteLegend", "MochaMarvel", "CappuccinoCap", "MilkyWay", "GalaxyGlaze", "StarSugar",
        "MoonMarshmallow", "SunSundae", "CometCandy", "AsteroidAlmond", "NebulaNut", "OrbitOreo", "CosmicCookie", "StarlightSweet",
        "RainbowRush", "CloudCandy", "SkySugar", "WindWaffle", "BreezeBerry", "StormSweet", "ThunderToffee", "FrostyFudge",
        "SnowflakeSugar", "IceCreamIcon", "BlizzardBrownie", "GlacierGelato", "CrystalCandy", "SparkleSugar", "ShineSweet", "GlowGummy",
        "BeamBerry", "FlashFudge", "BurstBubble", "PopPancake", "SnapScone", "CrackCrater", "CrunchyCookie", "ChewyChoco",
        "CrispyCrumb", "SilkySugar", "VelvetVanilla", "FluffyFudge", "CreamyCaramel", "GlossyGlaze", "SweetSurprise", "TastyTreat",
        "YummyYeti", "DeliciousDonut", "DivineDessert", "HeavenlyHoney", "ParadisePie", "BlissfulBerry", "MagicMarshmallow", "WonderWaffle"
    )

    private fun getBotNamesForMode(isOnlineOrArena: Boolean): List<String> {
        val baseName = if (isOnlineOrArena) "on" else "off"
        return try {
            val assets = getApplication<Application>().assets
            val fileName = try {
                assets.open("$baseName.txt").close()
                "$baseName.txt"
            } catch (e: Exception) {
                baseName
            }
            val lines = assets.open(fileName).bufferedReader().use { it.readLines() }
                .map { it.trim() }
                .filter { it.isNotBlank() }
            if (lines.isNotEmpty()) lines else if (isOnlineOrArena) botNamesEn else botNamesVi
        } catch (e: Exception) {
            if (isOnlineOrArena) botNamesEn else botNamesVi
        }
    }

    private val prefs = getApplication<Application>().getSharedPreferences("ludo_prefs", Context.MODE_PRIVATE)

    init {
        // Play click on start
        LudoSoundSynth.playClick()
        loadPersistentState()

        viewModelScope.launch {
            FirebaseRealtimeManager.currentRoom.collect { room ->
                if (room != null) {
                    val activeEmotes = mutableMapOf<String, Pair<String, Long>>()
                    if (room.chat != null && room.chat.text.isNotEmpty()) {
                        val text = room.chat.text
                        val hideDurationMs = 1500L + (text.length * 100L)
                        val expireTime = room.chat.timestamp + hideDurationMs
                        if (expireTime > System.currentTimeMillis()) {
                            activeEmotes[room.chat.sender] = Pair(text, expireTime)
                        }
                    }

                    _uiState.update { st ->
                        st.copy(
                            activeRoom = room,
                            activePawnEmotes = if (activeEmotes.isNotEmpty()) {
                                st.activePawnEmotes + activeEmotes
                            } else st.activePawnEmotes
                        )
                    }

                    if (room.status == "playing") {
                        val currentStatus = _uiState.value.status
                        if (currentStatus == GameStateStatus.MAIN_MENU &&
                            (_uiState.value.mode == GameMode.TEAM_LOBBY || _uiState.value.mode == GameMode.ARENA)) {
                            setupTeamLobbyPlayers(room)
                            startIntroAnimation()
                        } else if (currentStatus != GameStateStatus.MAIN_MENU) {
                            val isMultiplayer = _uiState.value.mode == GameMode.TEAM_LOBBY || _uiState.value.mode == GameMode.ARENA || _uiState.value.activeRoom != null
                            if (isMultiplayer) {
                                val turnColor = slotToColor(room.turn)
                                _uiState.update { st ->
                                    val updatedPlayers = st.players.map { player ->
                                        val slotId = colorToSlot(player.color)
                                        val roomP = room.players[slotId]
                                        if (roomP != null && roomP.name.isNotBlank()) {
                                            player.copy(name = roomP.name, characterSkin = roomP.skinId)
                                        } else player
                                    }

                                    val turnIndex = updatedPlayers.indexOfFirst { it.color == turnColor }.let { if (it >= 0) it else 0 }

                                    val myColor = slotToColor(st.myPlayerId)
                                    var updatedPawns = st.pawns
                                    if (room.pawnStates.isNotBlank()) {
                                        val parsedPawns = parsePawnStates(room.pawnStates, st.pawns)
                                        
                                        var movedRemotePawn: Triple<PlayerColor, Int, Pair<Int, Int>>? = null
                                        for (i in st.pawns.indices) {
                                            val oldP = st.pawns[i]
                                            val newP = parsedPawns[i]
                                            if (oldP.color != myColor && oldP.color == newP.color && oldP.id == newP.id) {
                                                if ((oldP.stepCount < newP.stepCount) || (oldP.stepCount == -1 && newP.stepCount >= 0)) {
                                                    movedRemotePawn = Triple(oldP.color, oldP.id, Pair(oldP.stepCount, newP.stepCount))
                                                    break
                                                }
                                            }
                                        }

                                        if (movedRemotePawn != null && !isRemoteAnimating) {
                                            val (pColor, pId, steps) = movedRemotePawn
                                            animateRemotePawnMove(pColor, pId, steps.first, steps.second)
                                        } else if (!isRemoteAnimating) {
                                            updatedPawns = parsedPawns
                                        }
                                    }

                                    val nextStatus = when (room.gameStatusStr) {
                                        "WAITING_FOR_MOVE" -> GameStateStatus.WAITING_FOR_MOVE
                                        "WAITING_FOR_ROLL" -> GameStateStatus.WAITING_FOR_ROLL
                                        else -> st.status
                                    }

                                    st.copy(
                                        players = updatedPlayers,
                                        activePlayerIndex = turnIndex,
                                        diceValue = room.diceValue,
                                        pawns = updatedPawns,
                                        status = if (st.status == GameStateStatus.MOVING_PAWN || st.status == GameStateStatus.ROLLING_DICE || isRemoteAnimating) st.status else nextStatus
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun serializePawns(pawns: List<Pawn>): String {
        return pawns.joinToString(";") { "${it.color.name}_${it.id}:${it.stepCount}" }
    }

    private fun parsePawnStates(serialized: String, currentPawns: List<Pawn>): List<Pawn> {
        if (serialized.isBlank()) return currentPawns
        val map = serialized.split(";").mapNotNull { token ->
            val parts = token.split(":")
            if (parts.size == 2) {
                val key = parts[0]
                val step = parts[1].toIntOrNull()
                if (step != null) key to step else null
            } else null
        }.toMap()

        return currentPawns.map { pawn ->
            val key = "${pawn.color.name}_${pawn.id}"
            val step = map[key]
            if (step != null) pawn.copy(stepCount = step) else pawn
        }
    }

    private fun loadPersistentState() {
        val gold = prefs.getInt("user_gold", 3000)
        val diamonds = prefs.getInt("user_diamonds", 10)
        val userTotalGames = prefs.getInt("user_total_games", 120)
        val userWonGames = prefs.getInt("user_won_games", 65)
        val lastClaim = prefs.getLong("last_claimed_timestamp", 0L)
        val lastWheel = prefs.getLong("last_weekly_wheel_timestamp", 0L)
        val selectedChar = prefs.getString("selected_character", "char1") ?: "char1"
        val unlockedCharsString = prefs.getString("unlocked_characters", "char1") ?: "char1"
        val unlockedCharsSet = unlockedCharsString.split(",").filter { it.isNotEmpty() }.toSet()
        val selectedDice = prefs.getString("selected_dice", "dice1") ?: "dice1"
        val unlockedDiceString = prefs.getString("unlocked_dice", "dice1") ?: "dice1"
        val unlockedDiceSet = unlockedDiceString.split(",").filter { it.isNotEmpty() }.toSet()
        val hasTrialDiceUsed = prefs.getBoolean("has_trial_dice_used", false)
        val hasTrialCharUsed = prefs.getBoolean("has_trial_char_used", false)
        val trialCharUses = prefs.getInt("trial_char_uses_left", 3)
        val trialDiceUses = prefs.getInt("trial_dice_uses_left", 3)
        val musicVol = prefs.getFloat("music_volume", 0.5f)
        val sfxVol = prefs.getFloat("sfx_volume", 0.8f)
        val savedName = prefs.getString("player_name", "Người chơi 1")?.ifBlank { "Người chơi 1" } ?: "Người chơi 1"
        val petString = prefs.getString("pet_data_string", "") ?: ""
        val loadedPetMap = parsePetDataString(petString, _uiState.value.petDataMap)

        _uiState.update {
            it.copy(
                userGold = gold,
                userDiamonds = diamonds,
                userTotalGames = userTotalGames,
                userWonGames = userWonGames,
                lastClaimedRewardTimestamp = lastClaim,
                lastWeeklyWheelTimestamp = lastWheel,
                selectedCharacter = selectedChar,
                unlockedCharacters = unlockedCharsSet,
                selectedDice = selectedDice,
                unlockedDice = unlockedDiceSet,
                hasTrialDiceUsed = hasTrialDiceUsed,
                hasTrialCharUsed = hasTrialCharUsed,
                trialCharUsesLeft = trialCharUses,
                trialDiceUsesLeft = trialDiceUses,
                musicVolume = musicVol,
                sfxVolume = sfxVol,
                playerName = savedName,
                petDataMap = loadedPetMap
            )
        }
        LudoSoundSynth.updateVolumes(musicVol, sfxVol)
        calculateFullnessOnLogin()
    }

    private fun serializePetDataMap(map: Map<String, PetData>): String {
        return map.values.joinToString(";") {
            "${it.petId}:${it.star}:${it.fullness}:${it.lastFedTimestamp}:${if (it.isRunaway) 1 else 0}"
        }
    }

    private fun parsePetDataString(serialized: String, defaultMap: Map<String, PetData>): Map<String, PetData> {
        if (serialized.isBlank()) return defaultMap
        val result = defaultMap.toMutableMap()
        serialized.split(";").forEach { token ->
            val parts = token.split(":")
            if (parts.size >= 5) {
                val petId = parts[0]
                val star = parts[1].toIntOrNull() ?: 1
                val fullness = parts[2].toIntOrNull() ?: 100
                val lastFed = parts[3].toLongOrNull() ?: (System.currentTimeMillis() / 1000L)
                val isRunaway = parts[4] == "1"
                result[petId] = PetData(petId, star, fullness, lastFed, isRunaway)
            }
        }
        return result
    }

    fun getEffectiveCharacterSkin(preferredChar: String): String {
        val state = _uiState.value
        val charId = state.trialCharacter ?: preferredChar
        val petData = state.petDataMap[charId]
        if (petData != null && petData.isRunaway) {
            return if (state.unlockedCharacters.contains("char2") && state.petDataMap["char2"]?.isRunaway != true) "char2" else "char1"
        }
        return charId
    }

    fun calculateFullnessOnLogin() {
        val nowSec = System.currentTimeMillis() / 1000L
        _uiState.update { st ->
            val updatedMap = st.petDataMap.mapValues { (petId, pet) ->
                if (pet.star < 2 || petId == "char1" || petId == "char2") {
                    pet.copy(fullness = 100, isRunaway = false)
                } else {
                    val elapsedSec = maxOf(0L, nowSec - pet.lastFedTimestamp)
                    val pointsLost = (elapsedSec * 25 / 86400L).toInt()
                    val currentFullness = maxOf(0, pet.fullness - pointsLost)
                    val isRunaway = (currentFullness == 0)
                    pet.copy(
                        fullness = currentFullness,
                        isRunaway = isRunaway
                    )
                }
            }
            var newSelectedChar = st.selectedCharacter
            if (updatedMap[newSelectedChar]?.isRunaway == true) {
                newSelectedChar = if (st.unlockedCharacters.contains("char2")) "char2" else "char1"
            }
            st.copy(
                petDataMap = updatedMap,
                selectedCharacter = newSelectedChar
            )
        }
        savePersistentState()
    }

    fun buyFood(foodId: String, quantity: Int): Boolean {
        val state = _uiState.value
        val food = FoodCatalog.items.find { it.id == foodId } ?: return false
        val totalCost = food.priceCandy * quantity
        if (quantity <= 0 || state.userGold < totalCost) {
            return false
        }

        _uiState.update { st ->
            val currentCount = st.foodInventory[foodId] ?: 0
            st.copy(
                userGold = st.userGold - totalCost,
                foodInventory = st.foodInventory + (foodId to (currentCount + quantity))
            )
        }
        LudoSoundSynth.playGoalCelebration()
        savePersistentState()
        return true
    }

    fun feedPet(petId: String, foodId: String): Boolean {
        val state = _uiState.value
        val isUnlocked = state.unlockedCharacters.contains(petId) || petId == "char1" || petId == "character1"
        if (!isUnlocked) {
            return false // Cannot feed unowned character!
        }

        val pet = state.petDataMap[petId] ?: PetData(petId, 1, 80)
        if (pet.isRunaway) {
            return false // Cannot feed runaway pet! Must ransom first.
        }

        val reqFood = getRequiredFoodForPet(petId)
        val actualFoodId = if (foodId.isEmpty() || foodId != reqFood.id) reqFood.id else foodId
        val food = FoodCatalog.items.find { it.id == actualFoodId } ?: return false
        val currentCount = state.foodInventory[actualFoodId] ?: 0

        val nowSec = System.currentTimeMillis() / 1000L
        val newFullness = minOf(100, pet.fullness + food.restoreFullness)
        val updatedPet = pet.copy(
            fullness = newFullness,
            lastFedTimestamp = nowSec,
            isRunaway = false
        )

        if (currentCount > 0) {
            _uiState.update { st ->
                st.copy(
                    foodInventory = st.foodInventory + (actualFoodId to (currentCount - 1)),
                    petDataMap = st.petDataMap + (petId to updatedPet)
                )
            }
        } else {
            if (state.userGold < food.priceCandy) {
                return false
            }
            _uiState.update { st ->
                st.copy(
                    userGold = st.userGold - food.priceCandy,
                    petDataMap = st.petDataMap + (petId to updatedPet)
                )
            }
        }
        LudoSoundSynth.playGoalCelebration()
        savePersistentState()
        return true
    }

    fun ransomPet(petId: String): Boolean {
        val state = _uiState.value
        val pet = state.petDataMap[petId] ?: return false
        val ransomCost = 500

        if (state.userGold < ransomCost) {
            return false
        }

        val nowSec = System.currentTimeMillis() / 1000L
        val restoredPet = pet.copy(
            fullness = 100,
            lastFedTimestamp = nowSec,
            isRunaway = false
        )

        _uiState.update { st ->
            st.copy(
                userGold = st.userGold - ransomCost,
                petDataMap = st.petDataMap + (petId to restoredPet)
            )
        }
        LudoSoundSynth.playGoalCelebration()
        savePersistentState()
        return true
    }

    private fun savePersistentState() {
        val state = _uiState.value
        prefs.edit()
            .putInt("user_gold", state.userGold)
            .putInt("user_diamonds", state.userDiamonds)
            .putInt("user_total_games", state.userTotalGames)
            .putInt("user_won_games", state.userWonGames)
            .putLong("last_claimed_timestamp", state.lastClaimedRewardTimestamp)
            .putLong("last_weekly_wheel_timestamp", state.lastWeeklyWheelTimestamp)
            .putString("selected_character", state.selectedCharacter)
            .putString("unlocked_characters", state.unlockedCharacters.joinToString(","))
            .putString("selected_dice", state.selectedDice)
            .putString("unlocked_dice", state.unlockedDice.joinToString(","))
            .putBoolean("has_trial_dice_used", state.hasTrialDiceUsed)
            .putBoolean("has_trial_char_used", state.hasTrialCharUsed)
            .putInt("trial_char_uses_left", state.trialCharUsesLeft)
            .putInt("trial_dice_uses_left", state.trialDiceUsesLeft)
            .putFloat("music_volume", state.musicVolume)
            .putFloat("sfx_volume", state.sfxVolume)
            .putString("player_name", state.playerName)
            .putString("pet_data_string", serializePetDataMap(state.petDataMap))
            .apply()
    }

    fun updatePlayerName(newName: String) {
        _uiState.update {
            it.copy(playerName = newName)
        }
        savePersistentState()
    }

    fun setMusicVolume(vol: Float) {
        _uiState.update { it.copy(musicVolume = vol) }
        LudoSoundSynth.updateVolumes(vol, _uiState.value.sfxVolume)
        savePersistentState()
    }

    fun setSfxVolume(vol: Float) {
        _uiState.update { it.copy(sfxVolume = vol) }
        LudoSoundSynth.updateVolumes(_uiState.value.musicVolume, vol)
        savePersistentState()
    }

    fun canClaimDailyReward(): Boolean {
        val lastClaimTime = _uiState.value.lastClaimedRewardTimestamp
        if (lastClaimTime == 0L) return true
        
        val now = java.util.Calendar.getInstance()
        val last = java.util.Calendar.getInstance().apply { timeInMillis = lastClaimTime }
        
        val nowYear = now.get(java.util.Calendar.YEAR)
        val nowMonth = now.get(java.util.Calendar.MONTH)
        val nowDay = now.get(java.util.Calendar.DAY_OF_MONTH)
        
        val lastYear = last.get(java.util.Calendar.YEAR)
        val lastMonth = last.get(java.util.Calendar.MONTH)
        val lastDay = last.get(java.util.Calendar.DAY_OF_MONTH)
        
        return nowYear != lastYear || nowMonth != lastMonth || nowDay != lastDay
    }

    fun claimDailyReward(): Boolean {
        if (!canClaimDailyReward()) return false
        
        val newGold = _uiState.value.userGold + 200
        val nowTime = System.currentTimeMillis()
        
        _uiState.update {
            it.copy(
                userGold = newGold,
                lastClaimedRewardTimestamp = nowTime
            )
        }
        savePersistentState()
        return true
    }

    fun canSpinWeeklyWheel(): Boolean {
        val lastWheelTime = _uiState.value.lastWeeklyWheelTimestamp
        if (lastWheelTime == 0L) return true
        val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
        return System.currentTimeMillis() - lastWheelTime >= sevenDaysMs
    }

    fun spinWeeklyWheel(sliceIndex: Int): String {
        val nowTime = System.currentTimeMillis()
        val isVi = _uiState.value.language == "vi"
        var rewardMsg = ""

        var currentGold = _uiState.value.userGold
        var currentDiamonds = _uiState.value.userDiamonds
        var currentUnlocked = _uiState.value.unlockedCharacters.toMutableSet()

        when (sliceIndex % 8) {
            0 -> {
                currentGold += 300
                rewardMsg = if (isVi) "🍭 +300 Kẹo mút ngọt ngào!" else "🍭 +300 Sweet Lollipops!"
            }
            1 -> {
                currentDiamonds += 10
                rewardMsg = if (isVi) "💎 +10 Kim cương lấp lánh!" else "💎 +10 Sparkling Diamonds!"
            }
            2 -> {
                // Ô trống / May mắn lần sau
                rewardMsg = if (isVi) "❌ Ô trống! Chúc bạn may mắn lần sau!" else "❌ Empty! Better luck next time!"
            }
            3 -> {
                // Character 2-3 star reward
                val pool23 = listOf("char3", "char4", "char5", "char6", "char7", "char8", "char9", "char10", "char11", "char12", "char13", "char14", "char15", "char16", "char17", "char18")
                val locked = pool23.filter { !currentUnlocked.contains(it) }
                if (locked.isNotEmpty()) {
                    val unlockedChar = locked.random()
                    currentUnlocked.add(unlockedChar)
                    rewardMsg = if (isVi) "🎭 Mở khóa thành công Nhân vật 2-3⭐ Mới!" else "🎭 Unlocked a 2-3⭐ Character!"
                } else {
                    currentGold += 500
                    rewardMsg = if (isVi) "🍭 +500 Kẹo mút (Đã sở hữu đủ NV 2-3⭐)!" else "🍭 +500 Lollipops!"
                }
            }
            4 -> {
                currentGold += 500
                rewardMsg = if (isVi) "🍭 +500 Kẹo mút ngọt ngào!" else "🍭 +500 Sweet Lollipops!"
            }
            5 -> {
                currentDiamonds += 20
                rewardMsg = if (isVi) "💎 +20 Kim cương lấp lánh!" else "💎 +20 Sparkling Diamonds!"
            }
            6 -> {
                // Ô trống / May mắn tuần sau
                rewardMsg = if (isVi) "❌ Ô trống! Rất tiếc, chúc bạn may mắn tuần sau!" else "❌ Empty! Better luck next time!"
            }
            7 -> {
                // Character 2-3 star VIP reward
                val pool23 = listOf("char3", "char4", "char5", "char6", "char7", "char8", "char9", "char10", "char11", "char12", "char13", "char14", "char15", "char16", "char17", "char18")
                val locked = pool23.filter { !currentUnlocked.contains(it) }
                if (locked.isNotEmpty()) {
                    val unlockedChar = locked.random()
                    currentUnlocked.add(unlockedChar)
                    rewardMsg = if (isVi) "🎭 Mở khóa thành công Nhân vật VIP 2-3⭐!" else "🎭 Unlocked a 2-3⭐ VIP Character!"
                } else {
                    currentDiamonds += 25
                    rewardMsg = if (isVi) "💎 +25 Kim cương (Đã sở hữu đủ NV 2-3⭐)!" else "💎 +25 Diamonds!"
                }
            }
        }

        _uiState.update {
            it.copy(
                userGold = currentGold,
                userDiamonds = currentDiamonds,
                unlockedCharacters = currentUnlocked,
                lastWeeklyWheelTimestamp = nowTime
            )
        }
        savePersistentState()
        return rewardMsg
    }

    fun purchaseCharacter(charId: String, price: Int): Boolean {
        val state = _uiState.value
        if (state.unlockedCharacters.contains(charId)) return false
        if (state.userGold < price) return false

        _uiState.update {
            it.copy(
                userGold = it.userGold - price,
                unlockedCharacters = it.unlockedCharacters + charId
            )
        }
        savePersistentState()
        return true
    }

    fun selectCharacter(charId: String) {
        val petData = _uiState.value.petDataMap[charId]
        if (petData != null && petData.isRunaway) {
            return
        }
        _uiState.update { it.copy(selectedCharacter = charId, trialCharacter = null) }
        savePersistentState()
        LudoSoundSynth.playClick()
    }

    fun toggleMusic() {
        val next = !_uiState.value.isMusicOn
        _uiState.update { it.copy(isMusicOn = next) }
        LudoSoundSynth.isMusicEnabled = next
        if (next) {
            LudoSoundSynth.startMusic(_uiState.value.status == GameStateStatus.MAIN_MENU)
        } else {
            LudoSoundSynth.stopMusic()
        }
    }

    fun toggleSfx() {
        val next = !_uiState.value.isSfxOn
        _uiState.update { it.copy(isSfxOn = next) }
        LudoSoundSynth.isSfxEnabled = next
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(language = lang) }
    }

    fun startNewGame(mode: GameMode, context: Context? = null, customNames: List<String>? = null, humanCount: Int = 1, noBots: Boolean = false) {
        // Cancel existing jobs
        timerJob?.cancel()
        botMoveJob?.cancel()

        val isVi = _uiState.value.language == "vi"

        // Check network connectivity for online modes
        if (mode == GameMode.ONLINE || mode == GameMode.ARENA || mode == GameMode.TEAM_LOBBY) {
            if (context != null && !isNetworkAvailable(context)) {
                android.widget.Toast.makeText(
                    context,
                    if (isVi) "Không có kết nối Internet! Vui lòng kiểm tra mạng." else "No Internet connection! Please check network.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        // Check Gold/Candy balance for Online mode (2,000 entry fee)
        if (mode == GameMode.ONLINE) {
            val entryFee = 2000
            val currentGold = _uiState.value.userGold
            if (currentGold < entryFee) {
                if (context != null) {
                    android.widget.Toast.makeText(
                        context,
                        if (isVi) "Không đủ 2,000 Kẹo 🍭 để tham gia chơi Online! (Bạn hiện có $currentGold 🍭)"
                        else "Not enough 2,000 Candy 🍭 to play Online! (You have $currentGold 🍭)",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }

        // Consume 1 trial attempt if trial item is active
        _uiState.update { st ->
            var trialChar = st.trialCharacter
            var charUses = st.trialCharUsesLeft
            if (trialChar != null) {
                charUses = (charUses - 1).coerceAtLeast(0)
                if (charUses == 0) {
                    trialChar = null
                }
            }
            var trialDice = st.trialDice
            var diceUses = st.trialDiceUsesLeft
            if (trialDice != null) {
                diceUses = (diceUses - 1).coerceAtLeast(0)
                if (diceUses == 0) {
                    trialDice = null
                }
            }
            st.copy(
                mode = mode,
                status = if (mode == GameMode.ONLINE) GameStateStatus.MATCHMAKING else GameStateStatus.INTRO_CAMERA,
                bannerText = when (mode) {
                    GameMode.ONLINE -> "Đang tìm đối thủ..."
                    GameMode.CO_VINA -> "CỜ VINA 🚀 TỐC ĐỘ"
                    GameMode.WITH_FRIENDS -> "CHƠI CÙNG BẠN BÈ"
                    else -> "SWEETY LUDO"
                },
                consecutiveSixes = 0,
                activePlayerIndex = 0,
                diceValue = 1,
                isDiceRolling = false,
                selectedPawnId = null,
                showProfileStatsPlayer = null,
                activeEmotes = emptyList(),
                logs = listOf("Trận đấu mới bắt đầu!"),
                trialCharacter = trialChar,
                trialCharUsesLeft = charUses,
                hasTrialCharUsed = if (charUses == 0) true else st.hasTrialCharUsed,
                trialDice = trialDice,
                trialDiceUsesLeft = diceUses,
                hasTrialDiceUsed = if (diceUses == 0) true else st.hasTrialDiceUsed
            )
        }
        savePersistentState()

        if (mode == GameMode.ONLINE) {
            val entryFee = 2000
            val currentGold = _uiState.value.userGold
            val newGold = (currentGold - entryFee).coerceAtLeast(0)
            _uiState.update { it.copy(userGold = newGold, logs = it.logs + "Trừ $entryFee Kẹo mút phí vào phòng Online 🍭") }
            savePersistentState()
            simulateMatchmaking()
        } else {
            setupPlayers(mode, customNames, humanCount, noBots)
            startIntroAnimation()
        }
    }

    private fun simulateMatchmaking() {
        matchmakingJob = viewModelScope.launch {
            delay(1500) // Simulate finding players
            if (_uiState.value.status != GameStateStatus.MATCHMAKING) return@launch
            val isVi = _uiState.value.language == "vi"
            val names = getBotNamesForMode(isOnlineOrArena = true).shuffled()
            
            val humanSkin = getEffectiveCharacterSkin(_uiState.value.selectedCharacter)
            val displayName = if (_uiState.value.playerName.isNotBlank()) _uiState.value.playerName else (if (isVi) "Người chơi 1" else "Player 1")
            val userTotalG = _uiState.value.userTotalGames
            val userWonG = _uiState.value.userWonGames
            val userWinR = if (userTotalG > 0) (userWonG * 100 / userTotalG) else 0

            val human = LudoPlayer(
                color = PlayerColor.RED,
                name = displayName,
                isBot = false,
                avatarId = 0,
                totalGames = userTotalG,
                wins = userWonG,
                winRate = userWinR,
                gold = _uiState.value.userGold,
                diamonds = _uiState.value.userDiamonds,
                characterSkin = humanSkin
            )

            val b1Total = Random.nextInt(50, 200)
            val b1Wins = Random.nextInt(25, (b1Total * 0.75f).toInt().coerceAtLeast(26))
            val bot1 = LudoPlayer(
                color = PlayerColor.GREEN,
                name = names[0],
                isBot = true,
                avatarId = Random.nextInt(1, 6),
                totalGames = b1Total,
                wins = b1Wins,
                winRate = b1Wins * 100 / b1Total,
                gold = Random.nextInt(1000, 15000),
                diamonds = Random.nextInt(10, 150),
                characterSkin = "char${Random.nextInt(1, 9)}"
            )

            val b2Total = Random.nextInt(50, 200)
            val b2Wins = Random.nextInt(25, (b2Total * 0.75f).toInt().coerceAtLeast(26))
            val bot2 = LudoPlayer(
                color = PlayerColor.YELLOW,
                name = names[1],
                isBot = true,
                avatarId = Random.nextInt(1, 6),
                totalGames = b2Total,
                wins = b2Wins,
                winRate = b2Wins * 100 / b2Total,
                gold = Random.nextInt(1000, 15000),
                diamonds = Random.nextInt(10, 150),
                characterSkin = "char${Random.nextInt(1, 9)}"
            )

            val b3Total = Random.nextInt(50, 200)
            val b3Wins = Random.nextInt(25, (b3Total * 0.75f).toInt().coerceAtLeast(26))
            val bot3 = LudoPlayer(
                color = PlayerColor.BLUE,
                name = names[2],
                isBot = true,
                avatarId = Random.nextInt(1, 6),
                totalGames = b3Total,
                wins = b3Wins,
                winRate = b3Wins * 100 / b3Total,
                gold = Random.nextInt(1000, 15000),
                diamonds = Random.nextInt(10, 150),
                characterSkin = "char${Random.nextInt(1, 9)}"
            )

            _uiState.update {
                it.copy(
                    players = listOf(human, bot1, bot2, bot3),
                    activePlayerIndex = Random.nextInt(4),
                    pawns = createInitialPawns(listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE))
                )
            }

            delay(1000)
            if (_uiState.value.status != GameStateStatus.MATCHMAKING) return@launch
            _uiState.update { it.copy(status = GameStateStatus.INTRO_CAMERA) }
            startIntroAnimation()
        }
    }

    fun updatePlayerName(color: PlayerColor, newName: String) {
        if (newName.isBlank()) return
        _uiState.update { st ->
            val updatedPlayers = st.players.map { player ->
                if (player.color == color) {
                    player.copy(name = newName.trim())
                } else {
                    player
                }
            }
            val updatedProfilePlayer = if (st.showProfileStatsPlayer?.color == color) {
                st.showProfileStatsPlayer?.copy(name = newName.trim())
            } else st.showProfileStatsPlayer

            st.copy(
                players = updatedPlayers,
                showProfileStatsPlayer = updatedProfilePlayer
            )
        }
    }

    private fun setupPlayers(mode: GameMode, customNames: List<String>? = null, humanCount: Int = 1, noBots: Boolean = false) {
        val isVi = _uiState.value.language == "vi"
        val isOnlineOrArena = (mode == GameMode.ONLINE || mode == GameMode.ARENA)
        val names = getBotNamesForMode(isOnlineOrArena = isOnlineOrArena).shuffled()

        val playersList = mutableListOf<LudoPlayer>()
        val colors = listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE)

        when (mode) {
            GameMode.WITH_FRIENDS -> {
                val activeChar = getEffectiveCharacterSkin(_uiState.value.trialCharacter ?: _uiState.value.selectedCharacter)
                if (noBots) {
                    val totalPlayers = humanCount.coerceIn(2, 4)
                    for (i in 0 until totalPlayers) {
                        val color = colors[i]
                        val skin = getEffectiveCharacterSkin("char${(i % 8) + 1}")
                        val defaultName = if (i == 0) {
                            if (_uiState.value.playerName.isNotBlank()) _uiState.value.playerName else (if (isVi) "Người chơi 1" else "Player 1")
                        } else {
                            if (isVi) "Người chơi ${i + 1}" else "Player ${i + 1}"
                        }
                        val name = if (!customNames.isNullOrEmpty() && customNames.getOrNull(i)?.isNotBlank() == true) {
                            customNames[i]!!
                        } else defaultName

                        playersList.add(
                            LudoPlayer(
                                color = color,
                                name = name,
                                isBot = false,
                                avatarId = i % 6,
                                gold = 2500,
                                characterSkin = skin
                            )
                        )
                    }
                } else {
                    // Custom / Free mode in WITH_FRIENDS:
                    // Any filled name is a human player, empty ones become Bots up to 4 players total
                    for (i in 0 until 4) {
                        val color = colors[i]
                        val enteredName = customNames?.getOrNull(i)?.trim()
                        val skin = getEffectiveCharacterSkin("char${(i % 8) + 1}")
                        if (!enteredName.isNullOrEmpty()) {
                            playersList.add(
                                LudoPlayer(
                                    color = color,
                                    name = enteredName,
                                    isBot = false,
                                    avatarId = i % 6,
                                    gold = 2500,
                                    characterSkin = skin
                                )
                            )
                        } else if (i == 0 && _uiState.value.playerName.isNotBlank()) {
                            playersList.add(
                                LudoPlayer(
                                    color = color,
                                    name = _uiState.value.playerName,
                                    isBot = false,
                                    avatarId = 0,
                                    gold = 2500,
                                    characterSkin = activeChar
                                )
                            )
                        } else {
                            playersList.add(
                                LudoPlayer(
                                    color = color,
                                    name = names.getOrElse(i) { "Bot ${i + 1}" },
                                    isBot = true,
                                    avatarId = Random.nextInt(1, 6),
                                    gold = 2500,
                                    characterSkin = skin
                                )
                            )
                        }
                    }
                }
            }
            GameMode.OFFLINE -> {
                val activeChar = getEffectiveCharacterSkin(_uiState.value.trialCharacter ?: _uiState.value.selectedCharacter)
                val effectiveHumans = humanCount.coerceIn(1, 4)

                for (i in 0 until 4) {
                    val color = colors[i]
                    val isHuman = i < effectiveHumans
                    val skin = if (i == 0) activeChar else getEffectiveCharacterSkin("char${(i % 8) + 1}")

                    val defaultName = if (isHuman) {
                        if (i == 0) {
                            if (_uiState.value.playerName.isNotBlank()) _uiState.value.playerName else (if (isVi) "Người chơi 1" else "Player 1")
                        } else {
                            if (isVi) "Người chơi ${i + 1}" else "Player ${i + 1}"
                        }
                    } else {
                        names.getOrElse(i) { "Bot $i" }
                    }

                    val name = if (!customNames.isNullOrEmpty() && customNames.getOrNull(i)?.isNotBlank() == true) {
                        customNames[i]!!
                    } else defaultName

                    val uTotalG = _uiState.value.userTotalGames
                    val uWonG = _uiState.value.userWonGames
                    val uWinR = if (uTotalG > 0) (uWonG * 100 / uTotalG) else 0

                    val bTotal = Random.nextInt(50, 200)
                    val bWins = Random.nextInt(25, (bTotal * 0.75f).toInt().coerceAtLeast(26))
                    val bWinR = bWins * 100 / bTotal

                    playersList.add(
                        LudoPlayer(
                            color = color,
                            name = name,
                            isBot = !isHuman,
                            avatarId = if (isHuman) 0 else Random.nextInt(1, 6),
                            totalGames = if (isHuman) uTotalG else bTotal,
                            wins = if (isHuman) uWonG else bWins,
                            winRate = if (isHuman) uWinR else bWinR,
                            gold = 2500,
                            characterSkin = skin
                        )
                    )
                }
            }
            GameMode.ARENA, GameMode.CO_VINA -> {
                // Red is Human, others are Bots
                val displayName = if (_uiState.value.playerName.isNotBlank()) _uiState.value.playerName else (if (isVi) "Người chơi 1" else "Player 1")
                val activeChar = getEffectiveCharacterSkin(_uiState.value.trialCharacter ?: _uiState.value.selectedCharacter)
                val uTotalG = _uiState.value.userTotalGames
                val uWonG = _uiState.value.userWonGames
                val uWinR = if (uTotalG > 0) (uWonG * 100 / uTotalG) else 0

                playersList.add(LudoPlayer(PlayerColor.RED, displayName, false, 0, totalGames = uTotalG, wins = uWonG, winRate = uWinR, gold = if (mode == GameMode.ARENA) 8000 else 2500, characterSkin = activeChar))
                
                for (bIdx in 0..2) {
                    val bColor = colors[bIdx + 1]
                    val bTotal = Random.nextInt(50, 200)
                    val bWins = Random.nextInt(25, (bTotal * 0.75f).toInt().coerceAtLeast(26))
                    val bWinR = bWins * 100 / bTotal
                    playersList.add(LudoPlayer(bColor, names[bIdx], true, Random.nextInt(1, 6), totalGames = bTotal, wins = bWins, winRate = bWinR, characterSkin = "char${Random.nextInt(1, 9)}"))
                }
            }
            else -> {}
        }

        val activeColors = playersList.map { it.color }
        val startingIndex = if (playersList.isNotEmpty()) Random.nextInt(playersList.size) else 0

        _uiState.update {
            it.copy(
                players = playersList,
                activePlayerIndex = startingIndex,
                pawns = createInitialPawns(activeColors),
                shieldActivePlayers = emptySet(),
                shieldCharges = mapOf(PlayerColor.RED to 1, PlayerColor.GREEN to 1, PlayerColor.YELLOW to 1, PlayerColor.BLUE to 1),
                rerollCharges = mapOf(PlayerColor.RED to 1, PlayerColor.GREEN to 1, PlayerColor.YELLOW to 1, PlayerColor.BLUE to 1),
                rocketCharges = mapOf(PlayerColor.RED to 1, PlayerColor.GREEN to 1, PlayerColor.YELLOW to 1, PlayerColor.BLUE to 1),
                bumpComboCount = emptyMap(),
                showSuperBumpBanner = false,
                superBumpText = ""
            )
        }
    }

    private fun createInitialPawns(colors: List<PlayerColor>): List<Pawn> {
        val list = mutableListOf<Pawn>()
        for (color in colors) {
            for (id in 0..3) {
                list.add(Pawn(id = id, color = color, stepCount = -1)) // All in Base
            }
        }
        return list
    }

    private fun startIntroAnimation() {
        introAnimationJob = viewModelScope.launch {
            _uiState.update { it.copy(bannerText = "SWEETY LUDO") }
            delay(1800) // Give time for isometric zoom transition
            if (_uiState.value.status != GameStateStatus.INTRO_CAMERA) return@launch
            _uiState.update { it.copy(status = GameStateStatus.WAITING_FOR_ROLL) }
            startTurn()
        }
    }

    private fun startTurn() {
        val state = _uiState.value
        if (state.status == GameStateStatus.MAIN_MENU) return
        val activePlayer = state.players.getOrNull(state.activePlayerIndex) ?: return
        val isVi = state.language == "vi"
        
        val isMultiplayer = state.mode == GameMode.TEAM_LOBBY || state.mode == GameMode.ARENA || state.activeRoom != null
        val mySlotColor = slotToColor(state.myPlayerId)
        val isMyTurn = if (isMultiplayer) (activePlayer.color == mySlotColor) else (!activePlayer.isBot)

        val bannerMsg = if (state.mode == GameMode.WITH_FRIENDS) {
            if (isVi) "Đến lượt ${activePlayer.name}" else "${activePlayer.name}'s turn"
        } else if (isMyTurn) {
            if (isVi) "ĐẾN LƯỢT BẠN!" else "YOUR TURN!"
        } else {
            if (isVi) "Đến lượt ${activePlayer.name}" else "${activePlayer.name}'s turn"
        }

        _uiState.update {
            it.copy(
                status = GameStateStatus.WAITING_FOR_ROLL,
                bannerText = bannerMsg,
                turnTimeLeft = 5,
                selectedPawnId = null
            )
        }

        if (activePlayer.isBot) {
            // Turn off sound effects when it is another player's (bot/opponent) turn to go
        } else {
            // Only play turn-alert sound if it is the local human user (PlayerColor.RED) or when playing locally with friends on the same device
            if (activePlayer.color == PlayerColor.RED) {
                LudoSoundSynth.playYourTurnAlert()
            }
        }
        startTimer()

        // If it's a bot, trigger rolling after a short artistic delay
        if (activePlayer.isBot) {
            triggerBotRoll()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _uiState.value.turnTimeLeft
                if (current > 0) {
                    _uiState.update { it.copy(turnTimeLeft = current - 1) }
                } else {
                    // Time out!
                    handleTimeout()
                    break
                }
            }
        }
    }

    private fun handleTimeout() {
        viewModelScope.launch {
            val state = _uiState.value
            val activePlayer = state.players[state.activePlayerIndex]
            if (activePlayer.isBot) {
                // If bot timed out (unlikely, but safe), just switch turn
                switchTurn()
            } else {
                // If human player timed out, automatically roll and auto-move or switch turn
                if (state.status == GameStateStatus.WAITING_FOR_ROLL) {
                    rollDice()
                } else if (state.status == GameStateStatus.WAITING_FOR_MOVE) {
                    val legalPawns = getLegalMoves(activePlayer.color, state.diceValue)
                    if (legalPawns.isNotEmpty()) {
                        movePawn(legalPawns.random().id)
                    } else {
                        switchTurn()
                    }
                }
            }
        }
    }

    fun buffAdminCurrency(gold: Int = 10000, diamonds: Int = 500) {
        _uiState.update {
            it.copy(
                userGold = it.userGold + gold,
                userDiamonds = it.userDiamonds + diamonds
            )
        }
        savePersistentState()
    }

    private fun getCharacterStars(charId: String): Int {
        return when (charId) {
            "char6", "char7", "char8", "char14", "char15", "char16", "char17", "char18" -> 3
            "char3", "char4", "char5", "char9", "char10", "char11", "char12", "char13" -> 2
            else -> 1
        }
    }

    private fun getDiceStars(diceId: String): Int {
        return when (diceId) {
            "dice5" -> 3
            "dice3", "dice4" -> 2
            else -> 1
        }
    }

    private fun rollWeightedDice(charSkin: String, diceSkin: String): Int {
        val charStar = getCharacterStars(charSkin)
        val diceStar = getDiceStars(diceSkin)

        val charBonus = (charStar - 1) * 0.05f
        val diceBonus = (diceStar - 1) * 0.05f
        val totalBonus = (charBonus + diceBonus).coerceAtMost(0.20f)

        val prob56 = (2.0f / 6.0f) + totalBonus
        val probOthers = (1.0f - prob56).coerceAtLeast(0.0f)
        val probEach14 = probOthers / 4.0f
        val probEach56 = prob56 / 2.0f

        val r = Random.nextFloat()
        return when {
            r < probEach14 -> 1
            r < probEach14 * 2 -> 2
            r < probEach14 * 3 -> 3
            r < probEach14 * 4 -> 4
            r < probEach14 * 4 + probEach56 -> 5
            else -> 6
        }
    }

    fun rollDice() {
        val state = _uiState.value
        if (state.status == GameStateStatus.MAIN_MENU) return
        if (state.isDiceRolling || state.status != GameStateStatus.WAITING_FOR_ROLL) return

        val isMultiplayer = state.mode == GameMode.TEAM_LOBBY || state.mode == GameMode.ARENA || state.activeRoom != null
        if (isMultiplayer) {
            val activePlayer = state.players.getOrNull(state.activePlayerIndex)
            val mySlotColor = slotToColor(state.myPlayerId)
            if (activePlayer == null || activePlayer.color != mySlotColor) {
                return // Not my turn!
            }
        }

        timerJob?.cancel() // Stop timer during dice roll
        _uiState.update {
            it.copy(
                status = GameStateStatus.ROLLING_DICE,
                isDiceRolling = true
            )
        }

        LudoSoundSynth.playDiceRoll()

        val activePlayer = state.players.getOrNull(state.activePlayerIndex)
        val activeCharSkin = activePlayer?.characterSkin ?: state.selectedCharacter
        val activeDiceSkin = if (activePlayer == null || !activePlayer.isBot) {
            state.trialDice ?: state.selectedDice
        } else {
            "dice1"
        }

        viewModelScope.launch {
            // Animate rolling values
            for (i in 1..8) {
                if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
                val tempVal = rollWeightedDice(activeCharSkin, activeDiceSkin)
                _uiState.update { it.copy(diceValue = tempVal) }
                delay(100)
            }

            if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
            // Final value
            val finalDice = rollWeightedDice(activeCharSkin, activeDiceSkin)
            
            _uiState.update {
                it.copy(
                    diceValue = finalDice,
                    isDiceRolling = false
                )
            }

            if (isMultiplayer) {
                FirebaseRealtimeManager.updateGameplayState(
                    diceValue = finalDice,
                    turn = state.myPlayerId,
                    pawnStates = serializePawns(_uiState.value.pawns),
                    gameStatusStr = "ROLLING_DICE"
                )
            }

            evaluateRollResult(finalDice)
        }
    }

    private fun evaluateRollResult(dice: Int) {
        val state = _uiState.value
        if (state.status == GameStateStatus.MAIN_MENU) return
        val activePlayer = state.players[state.activePlayerIndex]
        val isVi = state.language == "vi"
        val isMultiplayer = state.mode == GameMode.TEAM_LOBBY || state.mode == GameMode.ARENA || state.activeRoom != null

        // Handle consecutive 6s rule!
        if (dice == 6) {
            val nextConsecutive = state.consecutiveSixes + 1
            if (nextConsecutive == 3) {
                // Rolled 6 three times! Turn voided.
                _uiState.update {
                    it.copy(
                        consecutiveSixes = 0,
                        bannerText = if (isVi) "Mất lượt (3 lần đổ 6)!" else "Turn Voided (3 Sixes!)",
                        logs = state.logs + "${activePlayer.name} đổ 3 lần số 6 liên tiếp!"
                    )
                }
                LudoSoundSynth.playBumpScream()
                viewModelScope.launch {
                    delay(1800)
                    if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
                    switchTurn()
                }
                return
            } else {
                _uiState.update { it.copy(consecutiveSixes = nextConsecutive) }
            }
        } else {
            // Reset consecutive 6s on non-6 roll
            _uiState.update { it.copy(consecutiveSixes = 0) }
        }

        // Get list of legal moves
        val legalPawns = getLegalMoves(activePlayer.color, dice)

        if (legalPawns.isEmpty()) {
            _uiState.update {
                it.copy(
                    bannerText = if (isVi) "Không có nước đi hợp lệ!" else "No valid moves!",
                    logs = state.logs + "${activePlayer.name} đổ $dice nhưng không thể di chuyển."
                )
            }
            viewModelScope.launch {
                delay(1500)
                if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
                switchTurn()
            }
        } else {
            _uiState.update {
                it.copy(
                    status = GameStateStatus.WAITING_FOR_MOVE,
                    bannerText = if (activePlayer.isBot) "" else (if (isVi) "CHỌN QUÂN CỜ ĐỂ ĐI!" else "TAP A PAWN TO MOVE!"),
                    logs = state.logs + "${activePlayer.name} đổ được $dice."
                )
            }

            if (isMultiplayer) {
                FirebaseRealtimeManager.updateGameplayState(
                    diceValue = dice,
                    turn = state.myPlayerId,
                    pawnStates = serializePawns(_uiState.value.pawns),
                    gameStatusStr = "WAITING_FOR_MOVE"
                )
            }

            // Intelligent Auto-Move:
            // "If a player has only one legal, valid move available after rolling, the game must automatically move that pawn to ensure fast-paced gameplay."
            if (legalPawns.size == 1) {
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(bannerText = if (isVi) "Tự động di chuyển..." else "Auto moving...")
                    }
                    delay(1000)
                    if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
                    movePawn(legalPawns.first().id)
                }
            } else {
                _uiState.update { it.copy(turnTimeLeft = 15) }
                startTimer() // Restart timer for move selection
                if (activePlayer.isBot) {
                    triggerBotMove(legalPawns)
                }
            }
        }
    }

    fun getLegalMoves(color: PlayerColor, dice: Int): List<Pawn> {
        val state = _uiState.value
        val playerPawns = state.pawns.filter { it.color == color }
        val legalList = mutableListOf<Pawn>()

        for (pawn in playerPawns) {
            if (pawn.stepCount == -1) {
                // To deploy from Base, must roll exactly 6
                if (dice == 6) {
                    // Check if starting tile has double block of ANOTHER color
                    val startCoord = LudoBoardConfig.outerTrack[LudoBoardConfig.playerStartTrackIndex[color]!!]
                    if (!isOpponentDoubleBlockedAt(startCoord, color)) {
                        legalList.add(pawn)
                    }
                }
            } else if (pawn.stepCount < 56) {
                // Must enter center home (56) with exact roll
                val targetStep = pawn.stepCount + dice
                if (targetStep <= 56) {
                    // Check if path is blocked by opponent double blocks (standard Ludo rules: double block prevents passing/landing)
                    if (!isPathBlockedByDoubleBlock(color, pawn.stepCount, targetStep)) {
                        legalList.add(pawn)
                    }
                }
            }
        }
        return legalList
    }

    private fun isPathBlockedByDoubleBlock(color: PlayerColor, startStep: Int, targetStep: Int): Boolean {
        // Evaluate each tile along the path
        for (step in (startStep + 1)..targetStep) {
            val coord = getCoordinateForStep(color, step) ?: continue
            if (isOpponentDoubleBlockedAt(coord, color)) {
                return true
            }
        }
        return false
    }

    private fun isOpponentDoubleBlockedAt(coord: Pair<Int, Int>, myColor: PlayerColor): Boolean {
        // Filter pawns on this tile
        val pawnsOnTile = _uiState.value.pawns.filter {
            it.stepCount != -1 && getCoordinateForStep(it.color, it.stepCount) == coord
        }
        if (pawnsOnTile.size >= 2) {
            val blockColor = pawnsOnTile.first().color
            if (blockColor != myColor) {
                return true
            }
        }
        return false
    }

    private fun getCoordinateForStep(color: PlayerColor, step: Int): Pair<Int, Int>? {
        if (step == -1) return null
        if (step == 56) return LudoBoardConfig.centerHomes[color]
        if (step in 0..50) {
            val startIndex = LudoBoardConfig.playerStartTrackIndex[color] ?: 0
            val idx = (startIndex + step) % 52
            return LudoBoardConfig.outerTrack[idx]
        }
        if (step in 51..55) {
            val path = LudoBoardConfig.homePaths[color] ?: return null
            return path[step - 51]
        }
        return null
    }

    fun movePawn(pawnId: Int) {
        val state = _uiState.value
        if (state.status != GameStateStatus.WAITING_FOR_MOVE) return

        val isMultiplayer = (state.mode == GameMode.TEAM_LOBBY) && state.activeRoom != null
        if (isMultiplayer) {
            val activePlayer = state.players.getOrNull(state.activePlayerIndex)
            val mySlotColor = slotToColor(state.myPlayerId)
            if (activePlayer == null || activePlayer.color != mySlotColor) {
                return // Not my turn!
            }
        }

        val activePlayer = state.players[state.activePlayerIndex]
        val dice = state.diceValue
        val targetPawn = state.pawns.find { it.color == activePlayer.color && it.id == pawnId } ?: return

        // Verify target pawn is a legal move according to Ludo rules
        val legalPawns = getLegalMoves(activePlayer.color, dice)
        if (legalPawns.none { it.id == pawnId }) {
            return
        }

        timerJob?.cancel() // Cancel timer during movement

        _uiState.update {
            it.copy(
                status = GameStateStatus.MOVING_PAWN,
                selectedPawnId = pawnId
            )
        }

        viewModelScope.launch {
            val isDeployment = targetPawn.stepCount == -1
            
            if (isDeployment) {
                // High arc jump deployment from base cage to start tile 0
                updatePawnStep(activePlayer.color, pawnId, 0, isHopping = true)
                LudoSoundSynth.playPawnHop()
                
                for (frame in 1..10) {
                    updatePawnHopProgress(activePlayer.color, pawnId, frame / 10f)
                    delay(40)
                }
                
                updatePawnStep(activePlayer.color, pawnId, 0, isHopping = false)
                _uiState.update { it.copy(boardShakeTrigger = System.currentTimeMillis()) }
                LudoSoundSynth.playPawnHop()
                delay(100)
            } else {
                var currentStep = targetPawn.stepCount
                val stepsToTake = dice

                // Smooth stepwise animation
                for (step in 1..stepsToTake) {
                    if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
                    currentStep = currentStep + 1
                    val isSpecialFinalStep = (stepsToTake in 5..6 && step == stepsToTake)
                    
                    // Animate hopping state
                    updatePawnStep(activePlayer.color, pawnId, currentStep, isHopping = true, isSuperHop = isSpecialFinalStep)
                    
                    if (isSpecialFinalStep) {
                        LudoSoundSynth.playSuperPawnHop()
                    } else {
                        LudoSoundSynth.playPawnHop()
                    }
                    
                    // Jump time interpolation delay for rendering squash-and-stretch
                    val totalFrames = if (isSpecialFinalStep) 10 else 5
                    val frameDelay = if (isSpecialFinalStep) 45L else 40L
                    for (frame in 1..totalFrames) {
                        updatePawnHopProgress(activePlayer.color, pawnId, frame / totalFrames.toFloat())
                        delay(frameDelay)
                    }
                    
                    updatePawnStep(activePlayer.color, pawnId, currentStep, isHopping = false, isSuperHop = false)
                    
                    if (isSpecialFinalStep) {
                        LudoSoundSynth.playSpecialFinalLanding()
                        _uiState.update { it.copy(boardShakeTrigger = System.currentTimeMillis()) }
                    }
                    
                    delay(60)
                }
            }

            if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
            // Movement ended, check conditions
            val finalStep = if (isDeployment) 0 else targetPawn.stepCount + dice
            handleMovementLanding(activePlayer.color, pawnId, finalStep, dice)
        }
    }

    private fun updatePawnStep(color: PlayerColor, id: Int, step: Int, isHopping: Boolean, isSuperHop: Boolean = false) {
        _uiState.update { state ->
            val updatedPawns = state.pawns.map {
                if (it.color == color && it.id == id) {
                    it.copy(stepCount = step, isHopping = isHopping, isSuperHop = isSuperHop)
                } else it
            }
            state.copy(pawns = updatedPawns)
        }
    }

    private fun updatePawnHopProgress(color: PlayerColor, id: Int, progress: Float) {
        _uiState.update { state ->
            val updatedPawns = state.pawns.map {
                if (it.color == color && it.id == id) {
                    it.copy(hopProgress = progress)
                } else it
            }
            state.copy(pawns = updatedPawns)
        }
    }

    private var isRemoteAnimating = false

    private fun animateRemotePawnMove(color: PlayerColor, pawnId: Int, fromStep: Int, toStep: Int) {
        if (isRemoteAnimating) return
        isRemoteAnimating = true

        viewModelScope.launch {
            _uiState.update { it.copy(status = GameStateStatus.MOVING_PAWN) }

            if (fromStep == -1) {
                // High arc jump deployment from base cage to start tile 0
                updatePawnStep(color, pawnId, 0, isHopping = true)
                LudoSoundSynth.playPawnHop()

                for (frame in 1..10) {
                    updatePawnHopProgress(color, pawnId, frame / 10f)
                    delay(40)
                }

                updatePawnStep(color, pawnId, 0, isHopping = false)
                _uiState.update { it.copy(boardShakeTrigger = System.currentTimeMillis()) }
                LudoSoundSynth.playPawnHop()
                delay(100)
            } else {
                var currentStep = fromStep
                val stepsToTake = toStep - fromStep

                for (step in 1..stepsToTake) {
                    if (_uiState.value.status == GameStateStatus.MAIN_MENU) break
                    currentStep = currentStep + 1
                    val isSpecialFinalStep = (stepsToTake in 5..6 && step == stepsToTake)

                    updatePawnStep(color, pawnId, currentStep, isHopping = true, isSuperHop = isSpecialFinalStep)

                    if (isSpecialFinalStep) {
                        LudoSoundSynth.playSuperPawnHop()
                    } else {
                        LudoSoundSynth.playPawnHop()
                    }

                    val totalFrames = if (isSpecialFinalStep) 10 else 5
                    val frameDelay = if (isSpecialFinalStep) 45L else 40L
                    for (frame in 1..totalFrames) {
                        updatePawnHopProgress(color, pawnId, frame / totalFrames.toFloat())
                        delay(frameDelay)
                    }

                    updatePawnStep(color, pawnId, currentStep, isHopping = false, isSuperHop = false)

                    if (isSpecialFinalStep) {
                        LudoSoundSynth.playSpecialFinalLanding()
                        _uiState.update { it.copy(boardShakeTrigger = System.currentTimeMillis()) }
                    }

                    delay(60)
                }
            }

            isRemoteAnimating = false
            _uiState.update { st ->
                st.copy(status = GameStateStatus.WAITING_FOR_ROLL)
            }
        }
    }

    private suspend fun handleMovementLanding(color: PlayerColor, pawnId: Int, finalStep: Int, rolledDice: Int) {
        val state = _uiState.value
        val activePlayer = state.players[state.activePlayerIndex]
        val landingCoord = getCoordinateForStep(color, finalStep)
        val isVi = state.language == "vi"

        var gotBonusRoll = false
        var isGoal = false

        // Check if pawn reached final Home (56)
        if (finalStep == 56) {
            isGoal = true
            gotBonusRoll = true // Goal grants bonus roll
            _uiState.update {
                it.copy(
                    bannerText = if (isVi) "MỤC TIÊU! +1 LƯỢT ĐỒ" else "GOAL! BONUS ROLL",
                    logs = it.logs + "${activePlayer.name} đưa một quân cờ về đích!",
                    portalPawnId = pawnId,
                    portalPawnColor = color,
                    portalProgress = 0f
                )
            }
            LudoSoundSynth.playGoalCelebration()
            LudoSoundSynth.playPortalTeleport()

            // Animate portalProgress from 0f to 1f over 1500ms
            val duration = 1500L
            val steps = 30
            val delayPerStep = duration / steps
            for (step in 1..steps) {
                delay(delayPerStep)
                _uiState.update {
                    it.copy(portalProgress = step.toFloat() / steps)
                }
            }

            // Reset portal state after completion
            _uiState.update {
                it.copy(
                    portalPawnId = null,
                    portalPawnColor = null,
                    portalProgress = 0f
                )
            }
            delay(300)

            // Check if player has won!
            val neededPawns = if (_uiState.value.mode == GameMode.CO_VINA || _uiState.value.mode == GameMode.ARENA) 2 else 4
            val homePawnsCount = _uiState.value.pawns.count { it.color == color && it.stepCount == 56 }
            val hasWon = homePawnsCount >= neededPawns
            if (hasWon) {
                // Determine rankings of players based on total step counts
                val sortedPlayersByProgress = _uiState.value.players.map { p ->
                    val totalSteps = _uiState.value.pawns.filter { it.color == p.color }.sumOf { it.stepCount }
                    p to totalSteps
                }.sortedByDescending { it.second }

                // Find where our human player is in the ranking
                val humanPlayerColor = PlayerColor.RED
                val humanRankIndex = sortedPlayersByProgress.indexOfFirst { it.first.color == humanPlayerColor }

                val mode = _uiState.value.mode
                val isArena = mode == GameMode.ARENA

                val goldReward = when (humanRankIndex) {
                    0 -> if (mode == GameMode.ONLINE || mode == GameMode.ARENA) 10000 else 2000
                    1 -> if (mode == GameMode.ONLINE || mode == GameMode.ARENA) 3000 else 500
                    else -> 0
                }

                val rewardMsg = if (isVi) {
                    when (humanRankIndex) {
                        0 -> "Bạn về Nhất! Nhận +$goldReward Kẹo 🍭"
                        1 -> "Bạn về Nhì! Nhận +$goldReward Kẹo 🍭"
                        else -> "Bạn về thứ ${humanRankIndex + 1}! Hãy cố gắng hơn nhé!"
                    }
                } else {
                    when (humanRankIndex) {
                        0 -> "You got 1st Place! +$goldReward Candy 🍭"
                        1 -> "You got 2nd Place! +$goldReward Candy 🍭"
                        else -> "You got Place #${humanRankIndex + 1}! Try again!"
                    }
                }

                _uiState.update {
                    val updatedGold = it.userGold + goldReward
                    val isHumanWinner = (activePlayer.color == PlayerColor.RED)
                    val newTotal = it.userTotalGames + 1
                    val newWon = if (isHumanWinner) it.userWonGames + 1 else it.userWonGames

                    val updatedPlayers = it.players.map { player ->
                        if (!player.isBot || player.color == PlayerColor.RED) {
                            val calcRate = if (newTotal > 0) (newWon * 100 / newTotal) else 0
                            player.copy(totalGames = newTotal, wins = newWon, winRate = calcRate)
                        } else {
                            val isBotWinner = (player.color == activePlayer.color)
                            val bTotal = player.totalGames + 1
                            val bWins = if (isBotWinner) player.wins + 1 else player.wins
                            val bRate = if (bTotal > 0) (bWins * 100 / bTotal) else 0
                            player.copy(totalGames = bTotal, wins = bWins, winRate = bRate)
                        }
                    }

                    it.copy(
                        status = GameStateStatus.MATCH_ENDED,
                        userGold = updatedGold,
                        userTotalGames = newTotal,
                        userWonGames = newWon,
                        players = updatedPlayers,
                        matchRewardText = rewardMsg,
                        bannerText = if (isVi) {
                            if (activePlayer.color == PlayerColor.RED) "BẠN CHIẾN THẮNG!" else "${activePlayer.name} CHIẾN THẮNG!"
                        } else {
                            if (activePlayer.color == PlayerColor.RED) "YOU WIN!" else "${activePlayer.name} WINS!"
                        },
                        logs = it.logs + "${activePlayer.name} đã giành chiến thắng chung cuộc!"
                    )
                }
                savePersistentState()
                return
            }
        } else if (landingCoord != null) {
            // Check Teleport Portals in Cờ Vina Mode
            var adjustedStep = finalStep
            if ((_uiState.value.mode == GameMode.CO_VINA || _uiState.value.mode == GameMode.ARENA) && finalStep in 0..40) {
                val startIndex = LudoBoardConfig.playerStartTrackIndex[color] ?: 0
                val trackIdx = (startIndex + finalStep) % 52
                if (trackIdx == 12 || trackIdx == 38) {
                    adjustedStep += 10
                    updatePawnStep(color, pawnId, adjustedStep, isHopping = false)
                    LudoSoundSynth.playPortalTeleport()
                    _uiState.update {
                        it.copy(
                            bannerText = "🌀 CỔNG DỊCH CHUYỂN! NHẢY CÓC +10 BƯỚC!",
                            logs = it.logs + "${activePlayer.name} dẫm trúng Cổng Dịch Chuyển, nhảy vọt +10 bước!"
                        )
                    }
                    delay(700)
                }
            }

            // Check Safe Spot immunity
            val effectiveCoord = getCoordinateForStep(color, adjustedStep) ?: landingCoord
            val startIndex = LudoBoardConfig.playerStartTrackIndex[color] ?: 0
            val isSafeSpot = adjustedStep in 0..50 && LudoBoardConfig.safeSpotIndices.contains((startIndex + adjustedStep) % 52)

            if (!isSafeSpot) {
                // Find opponents to bump (excluding Safe spots and double blocks of same-color opponents)
                val opponentsOnTile = _uiState.value.pawns.filter {
                    it.color != color && it.stepCount != -1 && getCoordinateForStep(it.color, it.stepCount) == effectiveCoord
                }

                if (opponentsOnTile.isNotEmpty()) {
                    // Check Shield Immunity
                    val unshielded = opponentsOnTile.filter { opp ->
                        !_uiState.value.shieldActivePlayers.contains(opp.color)
                    }

                    if (unshielded.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                bannerText = "🛡️ KHIÊN BẢO VỆ ĐÃ BẢO VỆ ĐỐI THỦ!",
                                logs = it.logs + "Quân cờ đối thủ được bảo vệ bởi Khiên Thần!"
                            )
                        }
                        delay(1000)
                    } else {
                        gotBonusRoll = true
                        val kickBonusGold = 300
                        val rewardText = if (isVi) {
                            "💥 PHẦN THƯỞNG ĐÁ CỜ! +1 LƯỢT +300 Kẹo mút 🍭"
                        } else {
                            "💥 KICK REWARD! EXTRA TURN +300 Candy 🍭"
                        }

                        val isHumanBumper = (color == slotToColor(_uiState.value.myPlayerId)) || (_uiState.value.mode == GameMode.OFFLINE && color == PlayerColor.RED)

                        // Play scream sound
                        LudoSoundSynth.playBumpScream()

                        // Update opponent pawns to tumble and roll back to base
                        val tumbleJobs = unshielded.map { oppPawn ->
                            viewModelScope.launch {
                                for (frame in 1..25) {
                                    val progress = frame / 25f
                                    _uiState.update { s ->
                                        val updated = s.pawns.map {
                                            if (it.color == oppPawn.color && it.id == oppPawn.id) {
                                                it.copy(isBumping = true, bumpProgress = progress)
                                            } else it
                                        }
                                        s.copy(pawns = updated)
                                    }
                                    delay(40)
                                }
                                _uiState.update { s ->
                                    val updated = s.pawns.map {
                                        if (it.color == oppPawn.color && it.id == oppPawn.id) {
                                            it.copy(stepCount = -1, isBumping = false, bumpProgress = 0f)
                                        } else it
                                    }
                                    s.copy(pawns = updated)
                                }
                            }
                        }

                        // Wait for pawns to reach chuồng (base)
                        tumbleJobs.forEach { it.join() }

                        // Pawn has landed back in chuồng -> Now show reward banner and add 300 Candy!
                        _uiState.update {
                            val addedGold = if (isHumanBumper) kickBonusGold else 0
                            val newGold = it.userGold + addedGold
                            it.copy(
                                userGold = newGold,
                                showSuperBumpBanner = isHumanBumper,
                                superBumpText = if (isHumanBumper) rewardText else "",
                                bannerText = rewardText,
                                logs = it.logs + "${activePlayer.name} đã đá bay quân cờ của ${unshielded.first().color.displayName}! " + (if (isHumanBumper) "(+$addedGold Kẹo mút 🍭)" else "")
                            )
                        }

                        LudoSoundSynth.playGoalCelebration()

                        delay(1200)

                        // Emote triggers for the bumped bot! Extremely realistic bot behaviors!
                        unshielded.firstOrNull()?.let { bumped ->
                            triggerBotEmoteReaction(bumped.color, EmoteType.ANGRY)
                            triggerBotEmoteReaction(color, EmoteType.LAUGH)
                        }
                    }
                }
            }
        }

        // Did we roll a 6?
        if (rolledDice == 6 && !gotBonusRoll) {
            gotBonusRoll = true
            _uiState.update {
                it.copy(
                    bannerText = if (isVi) "THÊM LƯỢT! (ĐỔ 6)" else "BONUS ROLL! (ROLLED 6)",
                    logs = it.logs + "${activePlayer.name} nhận thêm lượt đổ do được 6."
                )
            }
            delay(1000)
        }

        if (gotBonusRoll) {
            // Keep turn with same active player
            _uiState.update {
                it.copy(
                    status = GameStateStatus.WAITING_FOR_ROLL,
                    selectedPawnId = null
                )
            }
            val st = _uiState.value
            val isMultiplayer = st.mode == GameMode.TEAM_LOBBY && st.activeRoom != null
            if (isMultiplayer) {
                FirebaseRealtimeManager.updateGameplayState(
                    diceValue = st.diceValue,
                    turn = st.myPlayerId,
                    pawnStates = serializePawns(st.pawns),
                    gameStatusStr = "WAITING_FOR_ROLL"
                )
            }
            startTurn()
        } else {
            switchTurn()
        }
    }

    private fun switchTurn() {
        if (_uiState.value.status == GameStateStatus.MAIN_MENU) return
        _uiState.update {
            val nextIndex = (it.activePlayerIndex + 1) % it.players.size
            it.copy(
                activePlayerIndex = nextIndex,
                consecutiveSixes = 0,
                selectedPawnId = null
            )
        }
        val st = _uiState.value
        val isMultiplayer = st.mode == GameMode.TEAM_LOBBY && st.activeRoom != null
        if (isMultiplayer) {
            val activePlayer = st.players.getOrNull(st.activePlayerIndex)
            val nextSlot = if (activePlayer != null) colorToSlot(activePlayer.color) else "player1"
            FirebaseRealtimeManager.updateGameplayState(
                diceValue = st.diceValue,
                turn = nextSlot,
                pawnStates = serializePawns(st.pawns),
                gameStatusStr = "WAITING_FOR_ROLL"
            )
        }
        startTurn()
    }

    // AI bot behavior logic
    private fun triggerBotRoll() {
        botMoveJob = viewModelScope.launch {
            val state = _uiState.value
            val activePlayer = state.players.getOrNull(state.activePlayerIndex)
            if (activePlayer != null && Random.nextFloat() < 0.15f) {
                val emotes = listOf(EmoteType.LAUGH, EmoteType.LOVE, EmoteType.SLEEPY)
                triggerBotEmoteReaction(activePlayer.color, emotes.random())
                delay(1500)
            } else {
                delay(1200 + Random.nextLong(0, 1000)) // Human-like reaction delay
            }
            rollDice()
        }
    }

    private fun triggerBotMove(legalPawns: List<Pawn>) {
        botMoveJob = viewModelScope.launch {
            delay(1000 + Random.nextLong(0, 800)) // Decision-making delay

            // Select the best strategic move using scoring algorithm!
            var bestPawn = legalPawns.first()
            var maxScore = -9999

            for (pawn in legalPawns) {
                val score = calculateMoveScore(pawn, _uiState.value.diceValue)
                if (score > maxScore) {
                    maxScore = score
                    bestPawn = pawn
                }
            }

            // Bot throws emotes sometimes based on stakes!
            if (maxScore >= 100 && Random.nextFloat() < 0.6f) {
                // Going to bump someone! Show excited or laughing emote
                triggerBotEmoteReaction(bestPawn.color, EmoteType.LAUGH)
            }

            movePawn(bestPawn.id)
        }
    }

    private fun calculateMoveScore(pawn: Pawn, dice: Int): Int {
        var score = 10 // baseline
        val targetStep = pawn.stepCount + dice

        // Reaching Home is huge
        if (targetStep == 56) {
            score += 150
        }

        // Deploying from base
        if (pawn.stepCount == -1 && dice == 6) {
            score += 80
        }

        // Check landing coordinate
        val landingCoord = getCoordinateForStep(pawn.color, targetStep)
        if (landingCoord != null) {
            // Can we bump opponent?
            val opponentsOnTile = _uiState.value.pawns.filter {
                it.color != pawn.color && it.stepCount != -1 && getCoordinateForStep(it.color, it.stepCount) == landingCoord
            }
            if (opponentsOnTile.isNotEmpty()) {
                score += 200 // Maximum priority!
            }

            // Moving into Safe Spot
            val startIndex = LudoBoardConfig.playerStartTrackIndex[pawn.color] ?: 0
            val isSafe = targetStep in 0..50 && LudoBoardConfig.safeSpotIndices.contains((startIndex + targetStep) % 52)
            if (isSafe) {
                score += 45
            }
        }

        // Progress bonus (prefer moving pawns closer to home)
        score += pawn.stepCount / 2

        // Avoid leaving safe spots if an opponent is trailing behind us
        if (pawn.stepCount != -1 && pawn.stepCount < 51) {
            val startIndex = LudoBoardConfig.playerStartTrackIndex[pawn.color] ?: 0
            val currentlySafe = LudoBoardConfig.safeSpotIndices.contains((startIndex + pawn.stepCount) % 52)
            if (currentlySafe) {
                score -= 20 // minor penalty to discourage leaving safe spots needlessly
            }
        }

        return score
    }

    // Emotes triggering
    fun throwEmote(emote: EmoteType, targetColor: PlayerColor? = null) {
        throwEmoteWithChat(emote, null, targetColor)
    }

    fun throwEmoteWithChat(emote: EmoteType, chatText: String? = null, targetColor: PlayerColor? = null) {
        val state = _uiState.value
        val finalColor = PlayerColor.RED
        
        val activeEmote = ActiveEmote(
            playerColor = finalColor,
            emote = emote,
            timestamp = System.currentTimeMillis(),
            targetColor = targetColor,
            chatText = chatText
        )

        val emojiChar = when (emote) {
            EmoteType.LAUGH -> "😂"
            EmoteType.ANGRY -> "😡"
            EmoteType.CRY -> "😭"
            EmoteType.LOVE -> "😍"
            EmoteType.SLEEPY -> "💤"
            EmoteType.APPLE -> "💩"
        }
        val activePawns = state.pawns.filter { it.color == finalColor && it.stepCount in 0..55 }
        val newPawnEmotes = state.activePawnEmotes.toMutableMap()
        for (p in activePawns) {
            newPawnEmotes["${p.color.name}_${p.id}"] = Pair(chatText ?: emojiChar, System.currentTimeMillis())
        }

        LudoSoundSynth.playEmoteSound(emote)

        _uiState.update {
            it.copy(
                activeEmotes = it.activeEmotes + activeEmote,
                activePawnEmotes = newPawnEmotes
            )
        }

        // Clear after 2.5 seconds (2/3 of previous duration)
        viewModelScope.launch {
            delay(2500)
            _uiState.update { s ->
                s.copy(activeEmotes = s.activeEmotes.filter { it != activeEmote })
            }
        }
    }

    private fun getRandomBotQuote(color: PlayerColor, emote: EmoteType): String {
        val extraQuotesVi = listOf(
            "Cơm không ăn đòi ăn đá nè!", "Né xa ta ra nha!", "Á à con cờ này ngông nhỉ!", "Đừng đá tớ mà hứa ngoan!", "Chờ đấy lát ta phục thù!",
            "Bác ơi tha cho cháu!", "Đi đâu mà vội mà vàng...", "Chạy trời không khỏi nắng!", "Game là dễ haha!", "Ui xui quá đổ ra 1...",
            "Cần lắm một con 6 cứu rỗi!", "Trân châu đường đen thẳng tiến!", "Lên cờ thôi anh em ơi!", "Ngồi yên ta chở về chuồng!", "Chờ ngày này lâu lắm rồi!",
            "Ối dời ơi tha tớ!", "Chơi xịn vãi!", "Có gan thì đá đi!", "Một bước lên mây luôn nè!", "Anh em ơi quây nó lại!",
            "Thắng bại tại kỹ năng nhé!", "Nhẹ tay thôi bạn ơi 🥺", "Chân cứng đá mềm!", "Sao ai cũng nhắm vào tớ vậy?", "Hihi ngọt xỉu luôn!",
            "Đừng làm tớ khóc nha!", "Tới lượt ta tỏa sáng!", "Một con 6 đẹp đẽ làm sao!", "Vòng quay may mắn gọi tên tớ!", "Sắp về đích rồi hô hô!",
            "Thôi xong, bay màu!", "Xin nhẹ cái ô an toàn nha!", "Cửa này khó quá bỏ qua!", "Tình nghĩa anh em chắc có bền lâu?", "Ai đá tớ là tớ nhớ đời!",
            "Chọt nhẹ một phát!", "Hahaha vui quá đi!", "Trời mưa thì mặc trời mưa, tớ đi đường tắt!", "Ahihi đồ ngốc!", "Chào thân ái và quyết thắng!",
            "Cho xin miếng bánh kem nha!", "Ngã ở đâu gấp đôi ở đó!", "Không phải dạng vừa đâu!", "Cố lên tớ ơi!", "Thua keo này ta bày keo khác!",
            "Có không giữ mất đừng tìm!", "Độc bộc hành giả nè!", "Nhảy chân chim cực chill!", "Chạy đi đâu cho thoát!", "Một phát ăn ngay!",
            "Đổ xúc xắc xịn xịn nào!", "Toát mồ hôi hột luôn!", "Tính cả rồi nhé!", "Đường về nhà sao xa quá!", "Thêm lượt nữa nè!",
            "Cảm ơn vì đã nhường đường!", "Xuất quân thành công!", "Né chướng ngại vật nào!", "Phút 89 rồi đấy!", "Chạy nhanh kẻo lỡ!",
            "Bắt được bạn rồi nhé!", "Ủa alo sao đá tớ?", "Hic đen như cột nhà cháy!", "Bình tĩnh sống bạn ơi!", "Alo 1234 nghe rõ trả lời!",
            "Lêu lêu con cờ chậm chạp!", "Hôm nay số đỏ quá đi!", "Bảo bối thần kỳ xuất hiện!", "Nhảy cao như kangaroo!", "Về đích ăn mừng nào!",
            "Thả tim nhè nhẹ 💕", "Bạn tốt nhất hệ mặt trời!", "Cuộc đời nở hoa hay bế tắc?", "Cực phẩm xúc xắc nè!", "Thổi nến sinh nhật thôi!",
            "Đừng hòng đuổi kịp tớ!", "Chạy như bay luôn!", "Ô an toàn là chân lý!", "Đá chết con cờ kia đi!", "Bơ đi mà sống!",
            "Ai rồi cũng phải về chuồng thôi!", "Cú đúp may mắn!", "Đừng đùa với ma vương!", "Thôi xong phim rồi!", "Này thì ngông này!",
            "Tha cho tớ ván này đi!", "Quá nhanh quá nguy hiểm!", "Mèo kẹo bông xuất chiêu!", "Gà cờ vua đẳng cấp!", "Cáo mật ong chạy trốn!",
            "Sư tử bơ ra trận!", "Cứ từ từ khoai sẽ chốc!", "Nhỏ nhưng có võ đấy!", "Hẹn gặp lại ở vạch đích!", "Tụi mình hòa nhau nhé!",
            "Xúc xắc ảo diệu quá!", "Một cú lội ngược dòng!", "Ai làm tớ khóc dợ?", "Cười xiên xẹo luôn!", "Xúc xắc quay đều quay đều!",
            "Đỏ quên đi nhé!", "Đá nhẹ cái rồi chạy!", "Thần may mắn độ tớ rồi!", "Điềm báo chiến thắng!", "Lên đỉnh tháp bánh nào!",
            "Thèm ly trà sữa quá!", "Bánh trôi nước trôi vèo vèo!", "Sữa chua dâu thơm phức!", "Su kem béo ngậy tới đây!", "Thỏ pastel tung tăng!",
            "Gấu bông cute xuất trận!", "Khủng long mập lạch bạch!", "Rùa siêu tốc bứt phá!", "Cái kết đắng lòng!", "Mở tiệc bánh kẹo thôi!"
        )

        val isVi = _uiState.value.language == "vi"
        if (isVi && Random.nextFloat() < 0.65f) {
            return extraQuotesVi.random()
        }

        return when (emote) {
            EmoteType.ANGRY -> listOf(
                "Ơ kìa, chơi ác thế! 😡",
                "Chờ đấy, tớ sẽ phục thù! ⚔️",
                "Đừng có đùa với tớ nhé! 😤",
                "Sao lại đá tớ về chuồng dợ? 😭",
                "Hic, ác như thú vậy! 😡"
            ).random()
            EmoteType.CRY -> listOf(
                "Bánh ngọt của tớ... bay màu rồi! 😭",
                "Hu hu, sao ai cũng bắt nạt tớ vậy? 🥺",
                "Đen đủi quá đi mất! 😭",
                "Mẹ ơi tớ muốn về nhà! 😭",
                "Trời ơi là trời! 🌧️"
            ).random()
            EmoteType.LOVE -> listOf(
                "Hehe, yêu quá cơ! ❤️",
                "Cảm ơn vì đã nhường đường nha! 😘",
                "Bạn tốt nhất hệ mặt trời! 🥰",
                "Moa moa chụt chụt! 💕",
                "Thả tim nhẹ nhàng! ❤️"
            ).random()
            EmoteType.LAUGH -> listOf(
                "Ahihi đồ ngốc, cho về chuồng nhé! 😂",
                "Kaka, số đỏ không đỡ được! 🎲",
                "Hehe ngọt xỉu luôn! 🍩",
                "Haha, né xa ta ra nha! 🏃‍♂️",
                "Cười ẻ, quá dễ dàng! 😂"
            ).random()
            EmoteType.SLEEPY -> listOf(
                "Đang ngủ gật đây này... 😴",
                "Nhanh lên bạn ơi, sốt ruột quá! 🥱",
                "Zzz... buồn ngủ ghê á! 😴",
                "Ủa tới lượt ai dợ? 🧐",
                "Lâu quá đi mất, ngủ một giấc đã! 💤"
            ).random()
            else -> listOf(
                "Ném trái táo nè! 🍎",
                "Ăn táo không bạn? 🍎",
                "Trúng đầu nè! Hehe 🎯"
            ).random()
        }
    }

    private fun triggerBotEmoteReaction(color: PlayerColor, emote: EmoteType) {
        viewModelScope.launch {
            delay(500)
            val quote = getRandomBotQuote(color, emote)
            val activeEmote = ActiveEmote(
                playerColor = color,
                emote = emote,
                timestamp = System.currentTimeMillis(),
                chatText = quote
            )
            LudoSoundSynth.playEmoteSound(emote)
            _uiState.update {
                it.copy(activeEmotes = it.activeEmotes + activeEmote)
            }
            delay(4000)
            _uiState.update { s ->
                s.copy(activeEmotes = s.activeEmotes.filter { it != activeEmote })
            }
        }
    }

    // Trigger random contextual exclamations on top of active/idle pawns
    fun triggerRandomPawnEmote() {
        val state = _uiState.value
        val isVi = state.language == "vi"
        val allPawns = state.pawns
        if (allPawns.isEmpty()) return

        val randomPawn = allPawns.random()
        val text = if (randomPawn.stepCount == -1) {
            // Idle in base exclamations
            if (isVi) {
                listOf(
                    "Xúc xắc 6 đâu rồi? 🎲",
                    "Mở cửa chuồng đi! 🚪",
                    "Cho tôi ra ngoài! 🏃",
                    "Nằm đây chán quá! 💤",
                    "Gà gáy rồi kìa! 🐓",
                    "Tê chân ở chuồng rồi! 🦵",
                    "Thả tôi ra chơi với! 🔓",
                    "Bao giờ mới được đi? ⏳"
                ).random()
            } else {
                listOf(
                    "Need a 6! 🎲",
                    "Let me out! 🚪",
                    "Want to play! 🏃",
                    "Stuck in base! 💤",
                    "Bored in cage! 😴",
                    "Roll a 6 please! 🙏",
                    "Open the door! 🔓"
                ).random()
            }
        } else {
            // Idle on board track exclamations
            if (isVi) {
                listOf(
                    "Cho tôi đi với! 🏃",
                    "Sắp rêu phong rồi! 🌿",
                    "Chờ lâu quá xá! ⏳",
                    "Nóng ruột quá đi! 🔥",
                    "Đứng đây mỏi chân! 🦶",
                    "Cho đi tiếp đi mà! 👉",
                    "Đừng bỏ quên tôi! 🥺",
                    "Di chuyển thôi nào! 🚀"
                ).random()
            } else {
                listOf(
                    "Move me! 🏃",
                    "Growing moss! 🌿",
                    "Waiting forever! ⏳",
                    "Tired feet! 🦶",
                    "Don't forget me! 🥺",
                    "Let's go! 🚀"
                ).random()
            }
        }

        val key = "${randomPawn.color.name}_${randomPawn.id}"
        _uiState.update {
            val current = it.activePawnEmotes.toMutableMap()
            current[key] = Pair(text, System.currentTimeMillis())
            it.copy(activePawnEmotes = current)
        }
    }

    fun activateTrialCharacter(charId: String): Boolean {
        val state = _uiState.value
        if (state.hasTrialCharUsed && state.trialCharUsesLeft <= 0) {
            return false
        }
        _uiState.update {
            it.copy(
                trialCharacter = charId,
                trialCharUsesLeft = 3,
                hasTrialCharUsed = false
            )
        }
        savePersistentState()
        LudoSoundSynth.playGoalCelebration()
        return true
    }

    fun selectDice(diceId: String) {
        _uiState.update { it.copy(selectedDice = diceId, trialDice = null) }
        savePersistentState()
        LudoSoundSynth.playClick()
    }

    fun buyDice(diceId: String, goldPrice: Int, diamondPrice: Int = 0): Boolean {
        val state = _uiState.value
        if (state.unlockedDice.contains(diceId)) {
            selectDice(diceId)
            return true
        }
        if (state.userGold >= goldPrice && state.userDiamonds >= diamondPrice) {
            _uiState.update {
                it.copy(
                    userGold = it.userGold - goldPrice,
                    userDiamonds = it.userDiamonds - diamondPrice,
                    unlockedDice = it.unlockedDice + diceId,
                    selectedDice = diceId
                )
            }
            savePersistentState()
            LudoSoundSynth.playGoalCelebration()
            return true
        }
        return false
    }

    fun activateTrialDice(diceId: String): Boolean {
        val state = _uiState.value
        if (state.hasTrialDiceUsed && state.trialDiceUsesLeft <= 0) {
            return false
        }
        _uiState.update {
            it.copy(
                trialDice = diceId,
                trialDiceUsesLeft = 3,
                hasTrialDiceUsed = false
            )
        }
        savePersistentState()
        LudoSoundSynth.playGoalCelebration()
        return true
    }

    fun buyCharacter(charId: String, goldPrice: Int, diamondPrice: Int = 0): Boolean {
        val state = _uiState.value
        if (state.unlockedCharacters.contains(charId)) {
            selectCharacter(charId)
            return true
        }
        if (state.userGold >= goldPrice && state.userDiamonds >= diamondPrice) {
            _uiState.update {
                it.copy(
                    userGold = it.userGold - goldPrice,
                    userDiamonds = it.userDiamonds - diamondPrice,
                    unlockedCharacters = it.unlockedCharacters + charId,
                    selectedCharacter = charId
                )
            }
            savePersistentState()
            LudoSoundSynth.playGoalCelebration()
            return true
        }
        return false
    }

    fun showProfileStats(player: LudoPlayer?) {
        LudoSoundSynth.playClick()
        _uiState.update { it.copy(showProfileStatsPlayer = player) }
    }

    fun exitToMainMenu() {
        LudoSoundSynth.playClick()
        timerJob?.cancel()
        botMoveJob?.cancel()
        matchmakingJob?.cancel()
        introAnimationJob?.cancel()
        _uiState.update {
            it.copy(
                status = GameStateStatus.MAIN_MENU,
                bannerText = "SWEETY LUDO"
            )
        }
    }

    fun useShieldSkill() {
        val state = _uiState.value
        val activePlayer = state.players.getOrNull(state.activePlayerIndex) ?: return
        val charges = state.shieldCharges[activePlayer.color] ?: 0
        if (charges <= 0) return

        val newCharges = state.shieldCharges.toMutableMap().apply { put(activePlayer.color, charges - 1) }
        val newShields = state.shieldActivePlayers + activePlayer.color

        _uiState.update {
            it.copy(
                shieldCharges = newCharges,
                shieldActivePlayers = newShields,
                bannerText = "🛡️ ĐÃ KÍCH HOẠT KHIÊN BẢO VỆ!",
                logs = it.logs + "${activePlayer.name} vừa kích hoạt Khiên Bảo Vệ!"
            )
        }
        LudoSoundSynth.playClick()
    }

    fun useRerollSkill() {
        val state = _uiState.value
        if (state.status != GameStateStatus.WAITING_FOR_MOVE) return
        val activePlayer = state.players.getOrNull(state.activePlayerIndex) ?: return
        val charges = state.rerollCharges[activePlayer.color] ?: 0
        if (charges <= 0) return

        val newCharges = state.rerollCharges.toMutableMap().apply { put(activePlayer.color, charges - 1) }
        val activeCharSkin = activePlayer.characterSkin
        val activeDiceSkin = if (!activePlayer.isBot) (state.trialDice ?: state.selectedDice) else "dice1"
        val newDice = rollWeightedDice(activeCharSkin, activeDiceSkin)

        _uiState.update {
            it.copy(
                rerollCharges = newCharges,
                diceValue = newDice,
                bannerText = "🎲 ĐỔI VẬN! XÚC XẮC MỚI: $newDice",
                logs = it.logs + "${activePlayer.name} sử dụng Đổi Vận: Xúc xắc ra $newDice!"
            )
        }
        LudoSoundSynth.playDiceRoll()
    }

    fun useRocketSkill() {
        val state = _uiState.value
        if (state.status != GameStateStatus.WAITING_FOR_MOVE) return
        val activePlayer = state.players.getOrNull(state.activePlayerIndex) ?: return
        val charges = state.rocketCharges[activePlayer.color] ?: 0
        if (charges <= 0) return

        val newCharges = state.rocketCharges.toMutableMap().apply { put(activePlayer.color, charges - 1) }
        val boostedDice = (state.diceValue + 2).coerceAtMost(56)

        _uiState.update {
            it.copy(
                rocketCharges = newCharges,
                diceValue = boostedDice,
                bannerText = "🚀 TĂNG TỐC ROCKET! +2 BƯỚC (Tổng $boostedDice)",
                logs = it.logs + "${activePlayer.name} kích hoạt Tăng Tốc Rocket: +2 bước đi!"
            )
        }
        LudoSoundSynth.playGoalCelebration()
    }

    fun dismissSuperBumpBanner() {
        _uiState.update { it.copy(showSuperBumpBanner = false) }
    }

    fun openTeamLobbyOptions() {
        _uiState.update { it.copy(showTeamLobbyDialog = true) }
    }

    fun closeTeamLobbyOptions() {
        _uiState.update { it.copy(showTeamLobbyDialog = false) }
    }

    fun openInventoryInLobby() {
        _uiState.update { it.copy(showInventoryDialogInLobby = true) }
    }

    fun closeInventoryInLobby() {
        _uiState.update { it.copy(showInventoryDialogInLobby = false) }
    }

    fun createTeamRoom(password: String, gameType: String = "STANDARD") {
        val randomCode = (1000..9999).random().toString()
        val hostName = if (_uiState.value.playerName.isNotBlank()) _uiState.value.playerName else "Chủ Phòng"
        val hostSkinId = _uiState.value.selectedCharacter
        val hostSkinIcon = "IMG/$hostSkinId.png"

        _uiState.update {
            it.copy(
                mode = GameMode.TEAM_LOBBY,
                myPlayerId = "player1",
                showTeamLobbyDialog = false
            )
        }

        FirebaseRealtimeManager.createRoom(
            roomId = randomCode,
            password = password,
            gameType = gameType,
            hostName = hostName,
            hostSkinId = hostSkinId,
            hostSkinIcon = hostSkinIcon
        ) { }
    }

    fun joinTeamRoom(roomId: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val cleanCode = roomId.trim()
        val cleanPass = password.trim()
        val playerName = if (_uiState.value.playerName.isNotBlank()) _uiState.value.playerName else "Người Chơi"
        val skinId = _uiState.value.selectedCharacter
        val skinIcon = "IMG/$skinId.png"

        FirebaseRealtimeManager.joinRoom(
            roomId = cleanCode,
            password = cleanPass,
            playerName = playerName,
            skinId = skinId,
            skinIcon = skinIcon
        ) { success, errorMsg ->
            if (success) {
                val currentRoom = FirebaseRealtimeManager.currentRoom.value
                val isArena = currentRoom?.gameType == "ARENA"
                _uiState.update {
                    it.copy(
                        mode = if (isArena) GameMode.ARENA else GameMode.TEAM_LOBBY,
                        myPlayerId = FirebaseRealtimeManager.myPlayerId,
                        showTeamLobbyDialog = false
                    )
                }
            }
            onResult(success, errorMsg)
        }
    }

    fun equipSkinInLobby(skinId: String, skinIcon: String) {
        _uiState.update { it.copy(selectedCharacter = skinId) }
        savePersistentState()
        FirebaseRealtimeManager.updateEquippedSkin(skinId, skinIcon)
    }

    fun sendLobbyChat(text: String) {
        if (text.isBlank()) return
        val trimmed = text.take(30)
        FirebaseRealtimeManager.sendChatMessage(trimmed)
        _uiState.update { it.copy(lobbyChatInput = "") }
    }

    fun setLobbyChatInput(text: String) {
        _uiState.update { it.copy(lobbyChatInput = text.take(30)) }
    }

    fun startTeamLobbyGame() {
        val room = _uiState.value.activeRoom ?: FirebaseRealtimeManager.currentRoom.value ?: return
        if (room.players.size < 2) return

        FirebaseRealtimeManager.startGame()
        setupTeamLobbyPlayers(room)
        startIntroAnimation()
    }

    fun leaveTeamRoom() {
        FirebaseRealtimeManager.leaveRoom()
        _uiState.update {
            it.copy(
                mode = GameMode.OFFLINE,
                activeRoom = null,
                status = GameStateStatus.MAIN_MENU,
                showTeamLobbyDialog = false,
                showInventoryDialogInLobby = false
            )
        }
    }

    private fun setupTeamLobbyPlayers(room: MultiplayerRoom) {
        val playerList = mutableListOf<LudoPlayer>()
        val pawnsList = mutableListOf<Pawn>()

        val isArenaType = room.gameType == "ARENA"
        val pawnsPerPlayer = if (isArenaType) 2 else 4

        listOf("player1", "player2", "player3", "player4").forEach { slotId ->
            val p = room.players[slotId]
            if (p != null) {
                val color = slotToColor(slotId)
                playerList.add(
                    LudoPlayer(
                        color = color,
                        name = p.name,
                        isBot = false,
                        avatarId = 0,
                        characterSkin = p.skinId
                    )
                )
                for (pawnId in 0 until pawnsPerPlayer) {
                    pawnsList.add(Pawn(id = pawnId, color = color))
                }
            }
        }

        _uiState.update {
            it.copy(
                mode = if (isArenaType) GameMode.ARENA else GameMode.TEAM_LOBBY,
                status = GameStateStatus.INTRO_CAMERA,
                players = playerList,
                pawns = pawnsList,
                activePlayerIndex = 0
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        botMoveJob?.cancel()
        matchmakingJob?.cancel()
        introAnimationJob?.cancel()
    }
}
