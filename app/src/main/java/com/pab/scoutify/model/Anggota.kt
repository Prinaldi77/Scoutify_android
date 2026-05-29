package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class Anggota(
    val id: Int,
    @SerializedName("nama")
    val nama: String?,
    val kelas: String?,
    val jabatan: String?,
    val angkatan: String?,
    @SerializedName("foto_url")
    val fotoUrl: String?,
    val status: String?
)
