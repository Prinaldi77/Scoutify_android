package com.pab.scoutify.model

data class ScoutActivity(
    val id: Long,
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val location: String,
    val badgeText: String,
    val badgeColor: String // hex or resource name
)
