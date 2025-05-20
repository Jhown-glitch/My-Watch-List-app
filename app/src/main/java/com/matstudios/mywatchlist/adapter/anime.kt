package com.matstudios.mywatchlist.adapter

    import android.os.Parcelable
    import kotlinx.parcelize.Parcelize

    @Parcelize
    data class anime(
        val capaUrl: String = "",
        val titulo: String = "",
        val episodios: String? = "",
        val temporadas: String? = "",
        val duracao: String? = "",
        val ano: String = "",
        val tipo: String = "",
        val sinopse: String = "",
        val genero: String = "",
        val avaliacao: String = "",
        val status: String = ""
    ) : Parcelable
