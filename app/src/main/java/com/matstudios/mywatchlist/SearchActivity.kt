package com.matstudios.mywatchlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.compose.animation.core.copy
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.databinding.adapters.SearchViewBindingAdapter.OnQueryTextSubmit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.matstudios.mywatchlist.adapter.SearchListItem
import com.matstudios.mywatchlist.adapter.content
import com.matstudios.mywatchlist.adapter.searchAdapter
import com.matstudios.mywatchlist.databinding.ActivitySearchBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var db: FirebaseFirestore
    private var collections = listOf("filmes", "series", "animes")
    private lateinit var adapter: searchAdapter
    //private val searchResults: MutableList<content> = mutableListOf()

    private lateinit var historyManager: SearchHistoryManager
    private var emptySearchMessage: TextView? = null
    private var currentSearchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        historyManager = SearchHistoryManager(this)
        emptySearchMessage = binding.emptySearchMessage

        // Configuração do RecyclerView
        setupRecyclerView()
        setupSearchView()

        // Carregar histórico de buscas
        loadSearchHistory()

//        val searchView = binding.searchView2
//        searchView.requestFocus()
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun OnQueryTextSubmit(query: String?): Boolean {
//                buscarConteudo(query ?: "")
//                return true
//            }
//
//            override fun OnQueryTextChange(newText: String?): Boolean {
//                buscarConteudo(newText ?: "")
//                return true
//            }
//        })
    }

    private fun buscarConteudo(termo: String) {
        val tempResult = mutableListOf<content>()
        var consultasFinalizadas = 0

        for (collection in collections) {
            db.collection(collection)
                .whereArrayContains("titulo", termo)
                .get().addOnSuccessListener {
                    for (doc in it) {
                        val item = doc.toObject(content::class.java)
                        val tituloPt = item.titulo?.get("pt")?.lowercase() ?: ""

                        if (tituloPt.contains(termo.lowercase())) {
                            tempResult.add(item)
                        }
                    }
                    consultasFinalizadas++
                    if (consultasFinalizadas == collections.size) {
                        atualizarResultados(tempResult)
                    }
                }
        }
    }

    private fun atualizarResultados(resultados: List<content>) {
        searchResults.clear()
        searchResults.addAll(resultados)
        adapter.notifyDataSetChanged()
    }

    private fun abrirDetalhes(item: content) {
        val intent = Intent(this, DetailContentActivity::class.java)
        intent.putExtra("item", item)
        startActivity(intent)
    }

    private fun showEmptyState(show: Boolean, message: String? = null) {
        binding.emptySearchMessage.isVisible = show
        if (show && message != null) {
            binding.emptySearchMessage.text = message
        }
        binding.searchRecyclerView.isVisible = !show
    }

    private fun setupRecyclerView() {
        adapter = searchAdapter(this, mutableListOf(), onSearchResultClickListener = { item ->
            historyManager.addToSearchHistory(item)
            abrirDetalhes(item)
        }, onHistoryItemClickListener = { item ->
            historyManager.addToSearchHistory(item)
            abrirDetalhes(item)
        }, onHistoryItemRemoveClickListener = { itemRemove ->
            itemRemove.id?.let {
                historyManager.removeFromSearchHistory(it)
                loadSearchHistory()
            }
        }, onMoreOptionsClickListener = { item, anchorView ->
            showItemOptionsMenu(item, anchorView)
        }
        )
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView2.requestFocus()
        binding.searchView2.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchJob?.cancel() // Cancela busca anterior
                val searchTerm = query?.trim()
                if (!searchTerm.isNullOrEmpty()) {
                    performSearch(searchTerm)
                } else {
                    loadSearchHistory() // Se limpar e submeter, mostrar histórico
                }
                binding.searchView2.clearFocus() // Opcional: esconder teclado
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchJob?.cancel() // Cancela busca anterior
                val searchTerm = newText?.trim()
                if (!searchTerm.isNullOrEmpty()) {
                    if (searchTerm.length >= 2) { // Começa a buscar com pelo menos X caracteres
                        currentSearchJob = lifecycleScope.launch {
                            delay(400) // Debounce para evitar buscas a cada letra
                            performSearch(searchTerm)
                        }
                    } else {
                        // Query muito curta, pode limpar resultados anteriores, mas não mostrar histórico ainda
                        adapter.submitList(emptyList())
                        showEmptyState(false) // Esconder mensagem de "nenhum resultado"
                    }
                } else {
                    // Texto limpo, mostrar histórico
                    loadSearchHistory()
                }
                return true
            }
        })

        // Opcional: Lidar com o fechamento do SearchView para mostrar o histórico
        binding.searchView2.setOnCloseListener {
            loadSearchHistory()
            false // Retornar false para que o SearchView possa limpar o texto

        }
    }

    private fun loadSearchHistory() {
        val historyItems = historyManager.getSearchHistory()
        if (historyItems.isNotEmpty()) {
            val listItems = historyItems.map { SearchListItem.HistoryItem(it) }
            adapter.submitList(listItems)
            showEmptyState(false)
        } else {
            adapter.submitList(emptyList())
            showEmptyState(true, "Seu histórico de busca está vazio.")
        }
    }

    private fun showItemOptionsMenu(item: content, anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.menuInflater.inflate(R.menu.item_options, popupMenu.menu) // SEU MENU XML
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_to_mylist_item_context -> {
                    // TODO: Implementar lógica para adicionar à "Minha Lista" do usuário no Firebase
                    Toast.makeText(this, "Adicionar '${item.titulo?.get("pt")}' à lista (TODO)", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_delete_mylist_item_context -> {
                    // TODO: Implementar lógica para remover da "Minha Lista" do usuário no Firebase
                    Toast.makeText(this, "Remover '${item.titulo?.get("pt")}' da lista (TODO)", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_change_status_mylist_item -> {
                    // TODO: Implementar lógica de compartilhamento
                    Toast.makeText(this, "Compartilhar '${item.titulo?.get("pt")}' (TODO)", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun performSearch(termo: String) {
        Log.d("SearchActivity", "Iniciando busca por: $termo")
        showEmptyState(true, "Buscando...") // Mostrar estado de carregamento/busca

        currentSearchJob = lifecycleScope.launch {
            val tempResult = mutableListOf<content>()
            try {
                for (collectionName in collections) {
                    val querySnapshot = db.collection(collectionName)
                        // AVISO: Firestore é case-sensitive. Para busca flexível, considere
                        // armazenar uma versão lowercase do título ou usar Algolia/Typesense.
                        // Esta busca é por prefixo no campo 'titulo.pt'.
                        .orderBy("titulo.pt") // Necessário para queries de range em strings
                        .startAt(termo.lowercase(androidx.compose.ui.text.intl.Locale.getDefault()))
                        .endAt(termo.lowercase(androidx.compose.ui.text.intl.Locale.getDefault()) + '\uf8ff')
                        .limit(10) // Limitar resultados por coleção
                        .get()
                        .await() // Usar await de kotlinx-coroutines-play-services

                    for (doc in querySnapshot.documents) {
                        val item = doc.toObject(content::class.java)?.copy(id = doc.id) // Salva o ID do documento
                        if (item != null) {
                            // Adicionar somente se não existir na lista (evitar duplicatas entre coleções)
                            if (tempResult.none { it.id == item.id }) {
                                tempResult.add(item)
                            }
                        }
                    }
                }

                if (tempResult.isNotEmpty()) {
                    // Ordenar resultados (ex: por relevância se você tiver, ou título)
                    tempResult.sortBy { it.titulo?.get("pt")?.lowercase() }
                    val searchListItems = tempResult.map { SearchListItem.SearchResultItem(it) }
                    adapter.submitList(searchListItems)
                    showEmptyState(false)
                } else {
                    adapter.submitList(emptyList())
                    showEmptyState(true, "Nenhum resultado encontrado para '$termo'.")
                }

            } catch (e: Exception) {
                Log.e("SearchActivity", "Erro durante a busca por '$termo'", e)
                adapter.submitList(emptyList())
                showEmptyState(true, "Erro ao buscar. Tente novamente.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentSearchJob?.cancel() // Importante para evitar memory leaks
    }
}