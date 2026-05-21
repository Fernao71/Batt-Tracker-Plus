package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_history")
data class BatteryRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: Int,
    val status: Int,
    val health: Int,
    val temperature: Float, // Temperature in Celsius: e.g. 29.5f
    val voltage: Int,      // Voltage in mV: e.g. 4100
    val plugged: Int       // Power source: 0 for battery, or PLUGGED_AC/USB/WIRELESS
)
