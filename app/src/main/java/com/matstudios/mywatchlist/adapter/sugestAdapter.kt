package com.matstudios.mywatchlist.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matstudios.mywatchlist.DetailContentActivity
import com.matstudios.mywatchlist.R

class SugestAdapter (private val animeList: List<anime>): RecyclerView.Adapter<SugestAdapter.ViewHolder>(){

    //Classe interna para representar cada item da lista
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val capa: ImageView = view.findViewById(R.id.capa)
        val titulo: TextView = view.findViewById(R.id.titulo)
        val episodiosLabel: TextView = view.findViewById(R.id.episodiosLabel)
        val episodios: TextView = view.findViewById(R.id.episodios)
        val sinopse: TextView = view.findViewById(R.id.sinopse)
        val genero: TextView = view.findViewById(R.id.genero)
        val avaliacao: TextView = view.findViewById(R.id.avaliacao)
        val duracaoLabel: TextView = view.findViewById(R.id.duracaoLabel)
        val duracao: TextView = view.findViewById(R.id.duracao)
    }

    //Infla o layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sugest_content, parent, false)
        return ViewHolder(view)
    }

    //Vincula os dados aos componentes do layout
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = animeList[position]

        Glide.with(holder.itemView.context)
            .load(item.capaUrl)
            .into(holder.capa)
        holder.titulo.text = item.titulo
        holder.sinopse.text = item.sinopse
        holder.genero.text = item.genero
        holder.avaliacao.text = item.avaliacao
        holder.itemView.setOnClickListener {
            // Lógica para lidar com o clique no item
            val context = holder.itemView.context
            val detalhes = Intent (context, DetailContentActivity::class.java)
            detalhes.putExtra("anime", item)
            context.startActivity(detalhes)
        }

        //Carrega Episódios ou Duração
        if (!item.episodios.isNullOrEmpty()) {
            holder.episodiosLabel.visibility = View.VISIBLE
            holder.episodios.visibility = View.VISIBLE
            holder.duracaoLabel.visibility = View.GONE
            holder.duracao.visibility = View.GONE
            holder.episodios.text = item.episodios
        } else {
            holder.episodiosLabel.visibility = View.GONE
            holder.episodios.visibility = View.GONE
            holder.duracaoLabel.visibility = View.VISIBLE
            holder.duracao.visibility = View.VISIBLE
            holder.duracao.text = item.duracao
        }
    }

    override fun getItemCount(): Int = animeList.size
}