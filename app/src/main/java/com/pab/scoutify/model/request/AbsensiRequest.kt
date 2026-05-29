package com.pab.scoutify.model.request

import com.google.gson.annotations.SerializedName

data class AbsensiRequest(
    @SerializedName("anggota_id")
    val anggotaId: Int,
    @SerializedName("kegiatan_id")
    val kegiatanId: Long?,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val tanggal: String,
    @SerializedName("jam_masuk")
    val jamMasuk: String?,
    val keterangan: String?
)
