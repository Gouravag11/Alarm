package com.agarwaldevs.alarm

data class Alarm(
    val id: Int = 0,
    val time: String,
    val name: String,
    val days: List<String>,
    val ringtone: String,
    val vibrate: Boolean,
    var isEnabled: Boolean
)
