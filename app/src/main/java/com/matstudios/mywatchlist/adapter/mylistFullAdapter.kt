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
import java.util.Locale

class mylistFullAdapter (private var contentList: List<contentUser>, private val onItemClicked: (item: contentUser, position: Int) -> Unit): RecyclerView.Adapter<mylistFullAdapter.ViewHolder>(){

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val capa = view.findViewById<ImageView>(R.id.capa)
        val titulo = view.findViewById<TextView>(R.id.titulo)
        val avaliaPessoal = view.findViewById<TextView>(R.id.avaliaPessoal)
        val status = view.findViewById<TextView>(R.id.status)
        val progresso = view.findViewById<TextView>(R.id.progresso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mylist_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = contentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = contentList[position]
        val idioma = Locale.getDefault().language

        Glide.with(holder.itemView.context).load(item.content?.capaUrl).into(holder.capa)
        holder.titulo.text = item.content?.titulo?.get(idioma) ?: "Sem Título"
        holder.avaliaPessoal.text = item.minhaNota
        holder.status.text = item.status
        holder.progresso.text = item.progresso
        holder.itemView.setOnClickListener {
            onItemClicked(item, position) // Chama a função lambda passada pela Activity
//            val context = holder.itemView.context
//            val intent = Intent(holder.itemView.context, DetailContentActivity::class.java)
//            intent.putExtra("content", item.content)
//            intent.putExtra("naMinhaLista", true)
//            context.startActivity(intent)
        }
    }

    fun updateData(newList: List<contentUser>) {
        contentList = newList
        notifyDataSetChanged() // Ou usar DiffUtil para melhor performance
    }
}