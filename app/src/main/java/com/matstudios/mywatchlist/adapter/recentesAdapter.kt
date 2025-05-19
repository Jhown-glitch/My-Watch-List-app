package com.matstudios.mywatchlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matstudios.mywatchlist.R

class recentesAdapter(private val animeList: List<anime>) : RecyclerView.Adapter<recentesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val capa: ImageView = view.findViewById(R.id.capa)
        val titulo: TextView = view.findViewById(R.id.titulo)
        val episodios: TextView = view.findViewById(R.id.episodios)
        val duracao: TextView = view.findViewById(R.id.duracao)
        val episodiosLabel: TextView = view.findViewById(R.id.episodiosLabel)
        val duracaoLabel: TextView = view.findViewById(R.id.duracaoLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.new_content, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = animeList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = animeList[position]

        Glide.with(holder.itemView.context)
            .load(item.capaUrl)
            .into(holder.capa)
        holder.titulo.text = item.titulo

        //Mostrar episódios ou duração, não os dois ao mesmo tempo
        if (!item.episodios.isNullOrEmpty()) {
            holder.episodiosLabel.visibility = View.VISIBLE
            holder.episodios.visibility = View.VISIBLE
            holder.duracaoLabel.visibility = View.GONE
            holder.duracao.visibility = View.GONE
            holder.episodios.text = item.episodios
        } else if (!item.duracao.isNullOrEmpty()) {
            holder.episodiosLabel.visibility = View.GONE
            holder.episodios.visibility = View.GONE
            holder.duracaoLabel.visibility = View.VISIBLE
            holder.duracao.visibility = View.VISIBLE
            holder.duracao.text = item.duracao
        } else {
            // Se nenhum dos campos estiver preenchido, ocultar os campos
            holder.episodiosLabel.visibility = View.GONE
            holder.episodios.visibility = View.GONE
            holder.duracaoLabel.visibility = View.GONE
            holder.duracao.visibility = View.GONE
        }
    }


}