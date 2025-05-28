package com.matstudios.mywatchlist.adapter

    import android.os.Parcelable
    import com.google.firebase.Timestamp
    import kotlinx.parcelize.Parcelize

    @Parcelize
    data class content(
        //val adicionadoEm: Long? = null,
        val adicionadoEm: Timestamp? = null,
        val ano: String = "",
        val autor: String = "",
        val avaliacao: String = "",
        val capaUrl: String = "",
        val duracao: String? = "",
        val episLista: Map<String, List<String>>? = mapOf(),
        val episodios: String? = "",
        val estudio: String = "",
        val genero: Map<String, List<String>> = mapOf(),
        val id: String = "",
        val premios: String = "",
        val sequencias: List<String> = listOf(),
        val sinopse: Map<String, String> = mapOf(),
        val status: String = "",
        val tags: Map<String, List<String>> = mapOf(),
        val temporadas: String? = "",
        val tipoID: String = "",
        val tipo: Map<String, String> = mapOf(),
        val titulo: Map<String, String> = mapOf(),
        val trailer: String = ""
    ) : Parcelable
