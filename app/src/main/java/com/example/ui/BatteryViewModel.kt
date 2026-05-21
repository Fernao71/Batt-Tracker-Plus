package com.example.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BatteryRecord
import com.example.data.BatteryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Internal state representation of live statistics
data class LiveBatteryState(
    val level: Int = 100,
    val status: Int = BatteryManager.BATTERY_STATUS_UNKNOWN,
    val health: Int = BatteryManager.BATTERY_HEALTH_GOOD,
    val temperature: Float = 25.0f, // in °C
    val voltage: Int = 4000,        // in mV
    val plugged: Int = 0            // 0: Battery, 1: AC, 2: USB, 4: Wireless
)

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = BatteryRepository(database.batteryDao())

    // Flow of history from room database
    val historyRecords: StateFlow<List<BatteryRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _liveState = MutableStateFlow(LiveBatteryState())
    val liveState: StateFlow<LiveBatteryState> = _liveState.asStateFlow()

    private var lastSavedLevel: Int = -1
    private var lastSavedStatus: Int = -1
    private var lastSavedTime: Long = 0L

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val levelPercent = if (level != -1 && scale != -1) {
                (level * 100 / scale.toFloat()).toInt()
            } else {
                100
            }

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
            val temperatureRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val temperature = temperatureRaw / 10.0f // BatteryManager returns tenths of a degree Celsius
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)

            val newState = LiveBatteryState(
                level = levelPercent,
                status = status,
                health = health,
                temperature = temperature,
                voltage = voltage,
                plugged = plugged
            )

            _liveState.update { newState }
            
            // Auto log database changes (avoiding duplication spam)
            val currentTime = System.currentTimeMillis()
            if (levelPercent != lastSavedLevel || status != lastSavedStatus || (currentTime - lastSavedTime) > 60000) {
                logRecord(newState)
                lastSavedLevel = levelPercent
                lastSavedStatus = status
                lastSavedTime = currentTime
            }
        }
    }

    init {
        // Register BroadcastReceiver for live battery notifications safely
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                application.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                application.registerReceiver(batteryReceiver, filter)
            }
        } catch (e: Exception) {
            // Fallback for custom ROMs or secure sandbox environments
            try {
                application.registerReceiver(batteryReceiver, filter)
            } catch (ex: Exception) {
                null
            }
        }
        // Manually trigger initial state parsing from sticky intent
        if (initialIntent != null) {
            batteryReceiver.onReceive(application, initialIntent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    // Insert an active record
    fun logRecord(state: LiveBatteryState, time: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.insert(
                BatteryRecord(
                    timestamp = time,
                    level = state.level,
                    status = state.status,
                    health = state.health,
                    temperature = state.temperature,
                    voltage = state.voltage,
                    plugged = state.plugged
                )
            )
        }
    }

    // Delete a record
    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    // Clear whole history
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // Inserts a custom mock record
    fun addManualMockRecord(level: Int, temp: Float, volt: Int, isCharging: Boolean, offsetHoursAgo: Int = 0) {
        val status = if (isCharging) BatteryManager.BATTERY_STATUS_CHARGING else BatteryManager.BATTERY_STATUS_DISCHARGING
        val plugged = if (isCharging) BatteryManager.BATTERY_PLUGGED_USB else 0
        val timestamp = System.currentTimeMillis() - (offsetHoursAgo * 3600000L)
        
        viewModelScope.launch {
            repository.insert(
                BatteryRecord(
                    timestamp = timestamp,
                    level = level,
                    status = status,
                    health = BatteryManager.BATTERY_HEALTH_GOOD,
                    temperature = temp,
                    voltage = volt,
                    plugged = plugged
                )
            )
        }
    }

    // Simulates dynamic scenario datasets
    fun simulateScenario(type: String) {
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repository.clearAll() // Let's clear and re-populate for clean visuals
            
            when (type) {
                "drain" -> {
                    // Simulates battery draining over 10 hours
                    val steps = listOf(
                        Triple(98, 31.2f, 4150),
                        Triple(86, 30.5f, 4000),
                        Triple(72, 33.1f, 3890), // heavy usage heating it up a bit
                        Triple(55, 31.8f, 3750),
                        Triple(38, 29.9f, 3680),
                        Triple(19, 28.5f, 3550),
                        Triple(6, 27.2f, 3400)
                    )
                    steps.forEachIndexed { index, data ->
                        val timeOffset = (7 - index) * 5400000L // spaced out over 10.5 hours
                        repository.insert(
                            BatteryRecord(
                                timestamp = now - timeOffset,
                                level = data.first,
                                status = BatteryManager.BATTERY_STATUS_DISCHARGING,
                                health = BatteryManager.BATTERY_HEALTH_GOOD,
                                temperature = data.second,
                                voltage = data.third,
                                plugged = 0
                            )
                        )
                    }
                }
                "charge" -> {
                    // Simulates charging from 5% to 100% over the last 3 hours
                    val steps = listOf(
                        Triple(5, 26.5f, 3450),
                        Triple(22, 36.5f, 3800), // AC charging generates heat!
                        Triple(45, 38.2f, 4050),
                        Triple(68, 35.1f, 4120),
                        Triple(88, 32.4f, 4220),
                        Triple(99, 29.8f, 4350)
                    )
                    steps.forEachIndexed { index, data ->
                        val timeOffset = (6 - index) * 1800000L // spaced over 3 hrs
                        repository.insert(
                            BatteryRecord(
                                timestamp = now - timeOffset,
                                level = data.first,
                                status = BatteryManager.BATTERY_STATUS_CHARGING,
                                health = BatteryManager.BATTERY_HEALTH_GOOD,
                                temperature = data.second,
                                voltage = data.third,
                                plugged = BatteryManager.BATTERY_PLUGGED_AC
                            )
                        )
                    }
                }
                "cycle" -> {
                    // Full daily cycle (Drain from 100 -> 30, then full charge back to 100)
                    val steps = listOf(
                        // Morning drain
                        Quad(100, 26.0f, 4200, false, 18),
                        Quad(82, 29.5f, 4050, false, 15),
                        Quad(54, 34.0f, 3850, false, 12),
                        Quad(31, 31.2f, 3690, false, 9),
                        // Evening charge
                        Quad(45, 37.8f, 3950, true, 7),
                        Quad(75, 35.4f, 4150, true, 5),
                        Quad(94, 30.2f, 4280, true, 2),
                        Quad(100, 27.5f, 4350, true, 1)
                    )
                    steps.forEach { data ->
                        val timeOffset = data.hoursAgo * 3600000L
                        repository.insert(
                            BatteryRecord(
                                timestamp = now - timeOffset,
                                level = data.level,
                                status = if (data.charging) BatteryManager.BATTERY_STATUS_CHARGING else BatteryManager.BATTERY_STATUS_DISCHARGING,
                                health = BatteryManager.BATTERY_HEALTH_GOOD,
                                temperature = data.temp,
                                voltage = data.volt,
                                plugged = if (data.charging) BatteryManager.BATTERY_PLUGGED_AC else 0
                            )
                        )
                    }
                }
            }
        }
    }

    private data class Quad(val level: Int, val temp: Float, val volt: Int, val charging: Boolean, val hoursAgo: Int)
}
