package com.matstudios.mywatchlist.adapter

import android.app.Activity
import android.content.Intent
import android.util.Log
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

class mylistFullAdapter (
    private var contentList: MutableList<contentUser>,
    private val onItemClicked: (item: contentUser, position: Int) -> Unit, //Lambda para o clique
    private val onItemLongClicked: (item: contentUser, position: Int, view: View) -> Boolean //Lambda para o clique longo
): RecyclerView.Adapter<mylistFullAdapter.ViewHolder>(){

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
        }
        holder.itemView.setOnLongClickListener { view ->
            onItemLongClicked(item, position, view) // Chama a função lambda passada pela Activity
            true // Retorna true para indicar que o clique longo foi tratado
        }

        (holder.itemView.context as? Activity)?. registerForContextMenu (holder.itemView)
    }

    fun updateData(newList: List<contentUser>) {
        Log.d("MyListActivity", "Atualizando dados do adapter com ${newList.size} itens")
        contentList.clear()
        contentList.addAll(newList)
        notifyDataSetChanged() // Ou usar DiffUtil para melhor performance
    }
}