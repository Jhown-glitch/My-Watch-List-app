package com.matstudios.mywatchlist.adapter

import android.content.Context
import android.app.appsearch.SearchResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matstudios.mywatchlist.R
import java.util.Locale

sealed class SearchListItem {
    data class SearchResultItem(val item: content) : SearchListItem()
    data class HistoryItem(val item: content, val timestamp: Long = System.currentTimeMillis()) : SearchListItem()
}

class searchAdapter (
    private val context: Context,
    private var items: MutableList<SearchListItem> = mutableListOf(),
    private val onSearchResultClickListener: (content) -> Unit,
    private val onHistoryItemClickListener: (content) -> Unit,
    private val onHistoryItemRemoveClickListener: (content) -> Unit,
    private val onMoreOptionsClickListener: (content, View) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_SEARCH_RESULT = 0
        private const val VIEW_TYPE_HISTORY_ITEM = 1
//        private const val VIEW_TYPE_LOADING = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SearchListItem.SearchResultItem -> VIEW_TYPE_SEARCH_RESULT
            is SearchListItem.HistoryItem -> VIEW_TYPE_HISTORY_ITEM
//            is SearchListItem.LoadingItem -> VIEW_TYPE_LOADING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SEARCH_RESULT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.new_search, parent, false)
                SearchResultViewHolder(view)
            }
            VIEW_TYPE_HISTORY_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.old_search, parent, false)
                HistoryItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = items[position]
        when (holder) {
            is SearchResultViewHolder -> {
                //val item = items[position] as SearchListItem.SearchResultItem
                holder.bind((currentItem as SearchListItem.SearchResultItem).item, onSearchResultClickListener, onMoreOptionsClickListener)
            }
            is HistoryItemViewHolder -> {
                val item = items[position] as SearchListItem.HistoryItem
                holder.bind((currentItem as SearchListItem.HistoryItem).item, item.timestamp, onHistoryItemClickListener, onHistoryItemRemoveClickListener)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<SearchListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // ViewHolders
    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.item_search_image)
        private val titleView: TextView = itemView.findViewById(R.id.item_search_title)
        private val temporadaView: TextView = itemView.findViewById(R.id.item_search_temporada)
        private val episodesView: TextView = itemView.findViewById(R.id.item_search_episodes)
        private val durationView: TextView = itemView.findViewById(R.id.item_search_duration)
        private val typeView: TextView = itemView.findViewById(R.id.item_search_type)
        private val moreOptionsButton: ImageView = itemView.findViewById(R.id.item_search_more_options)

        fun bind(item: content, clickListener: (content) -> Unit, moreOptionsClickListener: (content, View) -> Unit) {
            val idioma = Locale.getDefault().language
            titleView.text = item.titulo[idioma] ?: item.titulo["en"] ?: "No title available"
            if (item.temporadas != null || item.episodios != null) {
                temporadaView.visibility = View.VISIBLE
                temporadaView.text = item.temporadas
                episodesView.visibility = View.VISIBLE
                episodesView.text = item.episodios
                durationView.visibility = View.GONE
            } else {
                temporadaView.visibility = View.GONE
                episodesView.visibility = View.GONE
                durationView.visibility = View.VISIBLE
                durationView.text = item.duracao
            }
            typeView.text = item.tipo[idioma] ?: item.tipo["en"] ?: "No type available"
            Glide.with(itemView.context).load(item.capaUrl).into(imageView) //todo Criar um placeholder e uma imagem de erro
            itemView.setOnClickListener { clickListener(item) }
            moreOptionsButton.setOnClickListener { anchorView -> moreOptionsClickListener(item, anchorView) }
        }
    }

    class HistoryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //private val imageView: ImageView = itemView.findViewById(R.id.item_history_image)
        private val titleView: TextView = itemView.findViewById(R.id.item_history_title)
        private val removeButton: ImageView = itemView.findViewById(R.id.item_history_remove)

        fun bind(item: content, timestamp: Long, clickListener: (content) -> Unit, removeClickListener: (content) -> Unit) {
            val idioma = Locale.getDefault().language
            titleView.text = item.titulo[idioma] ?: item.titulo["en"] ?: "No title available"
            itemView.setOnClickListener { clickListener(item) }
            removeButton.setOnClickListener { removeClickListener(item) }
        }
    }
}