package com.example.game

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object FirebaseRealtimeManager {
    private const val JSONBLOB_URL = "https://jsonblob.com/api/jsonBlob/019f8ea4-5293-7f1a-96c2-eab57d61e219"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null

    private val _currentRoom = MutableStateFlow<MultiplayerRoom?>(null)
    val currentRoom: StateFlow<MultiplayerRoom?> = _currentRoom

    private var localRoomId: String? = null
    var myPlayerId: String = "player1"
        private set

    private val localRoomStore = java.util.concurrent.ConcurrentHashMap<String, MultiplayerRoom>()

    fun setMyPlayerId(id: String) {
        myPlayerId = id
    }

    fun createRoom(
        roomId: String,
        password: String,
        gameType: String = "STANDARD",
        hostName: String,
        hostSkinId: String,
        hostSkinIcon: String,
        onResult: (Boolean) -> Unit
    ) {
        myPlayerId = "player1"
        val cleanRoomId = roomId.trim()
        val cleanPassword = password.trim()
        localRoomId = cleanRoomId
        val hostPlayer = RoomPlayer(
            id = "player1",
            name = hostName,
            skinId = hostSkinId,
            skinIcon = hostSkinIcon,
            isHost = true,
            active = true,
            color = PlayerColor.RED
        )
        val room = MultiplayerRoom(
            roomId = cleanRoomId,
            password = cleanPassword,
            gameType = gameType,
            status = "waiting",
            turn = "player1",
            players = mapOf("player1" to hostPlayer)
        )
        _currentRoom.value = room
        localRoomStore[cleanRoomId] = room

        scope.launch {
            val success = pushRoomToFirebase(room)
            withContext(Dispatchers.Main) {
                onResult(success)
                startSyncLoop(cleanRoomId)
            }
        }
    }

    fun joinRoom(
        roomId: String,
        password: String,
        playerName: String,
        skinId: String,
        skinIcon: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val cleanRoomId = roomId.trim()
        val cleanPassword = password.trim()
        localRoomId = cleanRoomId

        scope.launch {
            var existing: MultiplayerRoom? = null
            // Retry fetching from cloud up to 4 times to ensure host's room is propagated
            for (attempt in 1..4) {
                existing = fetchRoomFromFirebase(cleanRoomId)
                if (existing == null) {
                    existing = localRoomStore[cleanRoomId]
                }
                if (existing == null && _currentRoom.value?.roomId == cleanRoomId) {
                    existing = _currentRoom.value
                }
                if (existing != null) break
                delay(350)
            }

            if (existing == null) {
                withContext(Dispatchers.Main) { onResult(false, "Không tìm thấy phòng $cleanRoomId! Vui lòng kiểm tra lại mã phòng.") }
                return@launch
            }

            if (existing.password.isNotEmpty() && existing.password != cleanPassword) {
                withContext(Dispatchers.Main) { onResult(false, "Sai mật khẩu phòng!") }
                return@launch
            }
            if (existing.players.size >= 4) {
                withContext(Dispatchers.Main) { onResult(false, "Phòng đã đầy (4/4)!") }
                return@launch
            }

            val availableSlots = listOf("player1", "player2", "player3", "player4")
            val assignedId = availableSlots.firstOrNull { !existing.players.containsKey(it) } ?: "player2"
            myPlayerId = assignedId

            val slotColor = slotToColor(assignedId)

            val newPlayer = RoomPlayer(
                id = assignedId,
                name = playerName,
                skinId = skinId,
                skinIcon = skinIcon,
                isHost = false,
                active = true,
                color = slotColor
            )

            val updatedPlayers = existing.players.toMutableMap()
            updatedPlayers[assignedId] = newPlayer
            val updatedRoom = existing.copy(players = updatedPlayers)
            _currentRoom.value = updatedRoom
            localRoomStore[cleanRoomId] = updatedRoom

            pushRoomToFirebase(updatedRoom)
            withContext(Dispatchers.Main) {
                onResult(true, null)
                startSyncLoop(cleanRoomId)
            }
        }
    }

    fun updateEquippedSkin(skinId: String, skinIcon: String) {
        val room = _currentRoom.value ?: return
        val player = room.players[myPlayerId] ?: return
        val updatedPlayer = player.copy(skinId = skinId, skinIcon = skinIcon)
        val updatedPlayers = room.players.toMutableMap()
        updatedPlayers[myPlayerId] = updatedPlayer
        val updatedRoom = room.copy(players = updatedPlayers)
        _currentRoom.value = updatedRoom

        scope.launch {
            patchPlayerOnFirebase(room.roomId, myPlayerId, skinId, skinIcon)
        }
    }

    fun sendChatMessage(text: String) {
        val room = _currentRoom.value ?: return
        val trimmed = text.take(30)
        val chat = RoomChat(sender = myPlayerId, text = trimmed, timestamp = System.currentTimeMillis())
        val updatedRoom = room.copy(chat = chat)
        _currentRoom.value = updatedRoom

        scope.launch {
            patchChatOnFirebase(room.roomId, chat)
        }
    }

    fun startGame() {
        val room = _currentRoom.value ?: return
        val updatedRoom = room.copy(status = "playing")
        _currentRoom.value = updatedRoom
        scope.launch {
            patchStatusOnFirebase(room.roomId, "playing")
        }
    }

    fun updateTurn(nextTurnPlayerId: String) {
        val room = _currentRoom.value ?: return
        val updatedRoom = room.copy(turn = nextTurnPlayerId)
        _currentRoom.value = updatedRoom
        scope.launch {
            patchTurnOnFirebase(room.roomId, nextTurnPlayerId)
        }
    }

    fun updateGameplayState(diceValue: Int, turn: String, pawnStates: String = "", gameStatusStr: String = "WAITING_FOR_ROLL") {
        val room = _currentRoom.value ?: return
        val updatedRoom = room.copy(
            diceValue = diceValue,
            turn = turn,
            pawnStates = pawnStates,
            gameStatusStr = gameStatusStr
        )
        _currentRoom.value = updatedRoom
        scope.launch {
            pushRoomToFirebase(updatedRoom)
        }
    }

    fun leaveRoom() {
        syncJob?.cancel()
        syncJob = null
        val room = _currentRoom.value
        if (room != null) {
            val updatedPlayers = room.players.toMutableMap()
            updatedPlayers.remove(myPlayerId)
            val updatedRoom = room.copy(players = updatedPlayers)
            _currentRoom.value = null
            scope.launch {
                pushRoomToFirebase(updatedRoom)
            }
        } else {
            _currentRoom.value = null
        }
        localRoomId = null
    }

    private fun startSyncLoop(roomId: String) {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                delay(250)
                try {
                    val remote = fetchRoomFromFirebase(roomId)
                    if (remote != null) {
                        _currentRoom.value = remote
                        localRoomStore[roomId] = remote
                    }
                } catch (e: Exception) {
                    // Ignore transient network errors
                }
            }
        }
    }

    private val cachedRoomsJson = JSONObject()

    private fun fetchAllRoomsFromCloud(): JSONObject {
        return try {
            val request = Request.Builder()
                .url(JSONBLOB_URL)
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null && body.isNotBlank()) {
                        val parsed = JSONObject(body).optJSONObject("rooms") ?: JSONObject()
                        val keys = parsed.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            cachedRoomsJson.put(k, parsed.opt(k))
                        }
                        parsed
                    } else cachedRoomsJson
                } else cachedRoomsJson
            }
        } catch (e: Exception) {
            cachedRoomsJson
        }
    }

    private fun pushRoomToFirebase(room: MultiplayerRoom): Boolean {
        return try {
            cachedRoomsJson.put(room.roomId, roomToJson(room))
            val payload = JSONObject().apply {
                put("rooms", cachedRoomsJson)
            }.toString()

            val request = Request.Builder()
                .url(JSONBLOB_URL)
                .put(payload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            true // Local memory fallback
        }
    }

    private fun patchPlayerOnFirebase(roomId: String, playerId: String, skinId: String, skinIcon: String) {
        val room = _currentRoom.value ?: return
        pushRoomToFirebase(room)
    }

    private fun patchChatOnFirebase(roomId: String, chat: RoomChat) {
        val room = _currentRoom.value ?: return
        pushRoomToFirebase(room)
    }

    private fun patchStatusOnFirebase(roomId: String, status: String) {
        val room = _currentRoom.value ?: return
        pushRoomToFirebase(room)
    }

    private fun patchTurnOnFirebase(roomId: String, turn: String) {
        val room = _currentRoom.value ?: return
        pushRoomToFirebase(room)
    }

    private fun patchGameplayOnFirebase(roomId: String, diceValue: Int, turn: String) {
        val room = _currentRoom.value ?: return
        pushRoomToFirebase(room)
    }

    private fun fetchRoomFromFirebase(roomId: String): MultiplayerRoom? {
        return try {
            val allRooms = fetchAllRoomsFromCloud()
            val roomJson = allRooms.optJSONObject(roomId)
            if (roomJson != null) {
                jsonToRoom(roomId, roomJson)
            } else {
                localRoomStore[roomId] ?: _currentRoom.value
            }
        } catch (e: Exception) {
            localRoomStore[roomId] ?: _currentRoom.value
        }
    }

    private fun roomToJson(room: MultiplayerRoom): JSONObject {
        val root = JSONObject()
        root.put("password", room.password)
        root.put("gameType", room.gameType)
        root.put("status", room.status)
        root.put("turn", room.turn)
        root.put("diceValue", room.diceValue)
        root.put("pawnStates", room.pawnStates)
        root.put("gameStatusStr", room.gameStatusStr)

        val playersObj = JSONObject()
        room.players.forEach { (id, player) ->
            val pObj = JSONObject()
            pObj.put("name", player.name)
            pObj.put("skinId", player.skinId)
            pObj.put("skinIcon", player.skinIcon)
            pObj.put("isHost", player.isHost)
            pObj.put("active", player.active)
            playersObj.put(id, pObj)
        }
        root.put("players", playersObj)

        if (room.chat != null) {
            val chatObj = JSONObject()
            chatObj.put("sender", room.chat.sender)
            chatObj.put("text", room.chat.text)
            chatObj.put("timestamp", room.chat.timestamp)
            root.put("chat", chatObj)
        }

        return root
    }

    private fun jsonToRoom(roomId: String, json: JSONObject): MultiplayerRoom {
        val password = json.optString("password", "")
        val gameType = json.optString("gameType", "STANDARD")
        val status = json.optString("status", "waiting")
        val turn = json.optString("turn", "player1")
        val diceValue = json.optInt("diceValue", 1)
        val pawnStates = json.optString("pawnStates", "")
        val gameStatusStr = json.optString("gameStatusStr", "WAITING_FOR_ROLL")

        val playersMap = mutableMapOf<String, RoomPlayer>()
        val playersObj = json.optJSONObject("players")
        if (playersObj != null) {
            val keys = playersObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val pObj = playersObj.optJSONObject(key) ?: continue
                val color = when (key) {
                    "player1" -> PlayerColor.RED
                    "player2" -> PlayerColor.BLUE
                    "player3" -> PlayerColor.YELLOW
                    "player4" -> PlayerColor.GREEN
                    else -> PlayerColor.BLUE
                }
                playersMap[key] = RoomPlayer(
                    id = key,
                    name = pObj.optString("name", "Người chơi"),
                    skinId = pObj.optString("skinId", "char1"),
                    skinIcon = pObj.optString("skinIcon", "IMG/char1.png"),
                    isHost = pObj.optBoolean("isHost", false),
                    active = pObj.optBoolean("active", true),
                    color = color
                )
            }
        }

        var chat: RoomChat? = null
        val chatObj = json.optJSONObject("chat")
        if (chatObj != null) {
            chat = RoomChat(
                sender = chatObj.optString("sender", ""),
                text = chatObj.optString("text", ""),
                timestamp = chatObj.optLong("timestamp", 0L)
            )
        }

        return MultiplayerRoom(
            roomId = roomId,
            password = password,
            gameType = gameType,
            status = status,
            turn = turn,
            players = playersMap,
            chat = chat,
            diceValue = diceValue,
            pawnStates = pawnStates,
            gameStatusStr = gameStatusStr
        )
    }
}
