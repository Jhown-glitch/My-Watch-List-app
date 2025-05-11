package com.matstudios.mywatchlist.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matstudios.mywatchlist.R

class SugestAdapter (private val animeList: List<anime>): RecyclerView.Adapter<SugestAdapter.ViewHolder>(){

    //Classe interna para representar cada item da lista
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val capa: ImageView = view.findViewById(R.id.capa)
        val titulo: TextView = view.findViewById(R.id.titulo)
        val episodios: TextView = view.findViewById(R.id.episodios)
        val sinopse: TextView = view.findViewById(R.id.sinopse)
        val genero: TextView = view.findViewById(R.id.genero)
        val avaliacao: TextView = view.findViewById(R.id.avaliacao)
    }

    //Infla o layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sugest_content, parent, false)
        return ViewHolder(view)
    }

    //Vincula os dados aos componentes do layout
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = animeList[position]

        Log.d("SugestAdapter", "Essa merda funcionou: ${item.titulo}")

        Glide.with(holder.itemView.context)
            .load(item.capaUrl)
            .into(holder.capa)
        holder.titulo.text = item.titulo
        holder.episodios.text = item.episodios
        holder.sinopse.text = item.sinopse
        holder.genero.text = item.genero
        holder.avaliacao.text = item.avaliacao
    }

    override fun getItemCount(): Int = animeList.size
}