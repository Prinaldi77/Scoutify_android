package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class Kegiatan(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("nama_kegiatan")
    val nama: String?,
    val tanggal: String?,
    @SerializedName("waktu_mulai")
    val waktu: String?,
    val lokasi: String?,
    val deskripsi: String?,
    val kategori: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Int? = null
)
