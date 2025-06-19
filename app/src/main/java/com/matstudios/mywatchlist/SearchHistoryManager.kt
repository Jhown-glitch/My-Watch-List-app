package com.matstudios.mywatchlist

import android.content.Context
import com.google.gson.Gson
import com.google.common.reflect.TypeToken
import com.matstudios.mywatchlist.adapter.content

class SearchHistoryManager(context: Context) {

    private val prefs = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val HISTORY_KEY = "search_history"
    private val MAX_HISTORY_SIZE = 20

    fun getSearchHistory(): List<content> {
        val json = prefs.getString(HISTORY_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<content>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun addToSearchHistory(item: content) {
        if (item.id == null) return // Não adicionar itens sem ID

        val history = getSearchHistory().toMutableList()
        // Remover o item antigo se estiver presente para movê-lo para o topo
        history.removeAll { it.id == item.id }
        history.add(0, item)

        // Limitar o tamanho do histórico
        val limitedHistory = if (history.size > MAX_HISTORY_SIZE) {
            history.subList(0, MAX_HISTORY_SIZE)
        } else {
            history
        }

        val json = gson.toJson(limitedHistory)
        prefs.edit().putString(HISTORY_KEY, json).apply()
    }

    fun removeFromSearchHistory(itemId: String) {
        val history = getSearchHistory().toMutableList()
        history.removeAll { it.id == itemId }
        val json = gson.toJson(history)
        prefs.edit().putString(HISTORY_KEY, json).apply()
    }

    fun clearSearchHistory() {
        prefs.edit().remove(HISTORY_KEY).apply()
    }

}