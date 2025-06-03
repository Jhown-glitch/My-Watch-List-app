package com.matstudios.mywatchlist.adapter

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class contentUser(
    val content: content? = null,
    val minhaNota: String = "",
    val progresso: String = "",
    var status: String = ""
) : Parcelable
