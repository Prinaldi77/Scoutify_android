package com.pab.scoutify.model

data class Member(
    val id: Int,
    val name: String,
    val rank: String,
    val regu: String,
    val photo: Int, // Resource ID for now
    val isActive: Boolean = true
)