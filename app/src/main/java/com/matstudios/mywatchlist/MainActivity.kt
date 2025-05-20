package com.matstudios.mywatchlist

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.matstudios.mywatchlist.adapter.MylistAdapter
import com.matstudios.mywatchlist.adapter.SugestAdapter
import com.matstudios.mywatchlist.adapter.anime
import com.matstudios.mywatchlist.adapter.recentesAdapter
import com.matstudios.mywatchlist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        carregarRecentes()
        carregarSugestoes()
        carregarMinhaLista()

        val nomeUsuario: String? = null
        val mensagemBoasVindas = "Olá, ${nomeUsuario ?: "visitante"}!"
        binding.HelloU.text = mensagemBoasVindas



        // Configuração do RecyclerView
        val recyclerViewSugest = findViewById<RecyclerView>(R.id.sugestSection)
        val recyclerViewMylist = findViewById<RecyclerView>(R.id.mylistSection)
        val recyclerViewRecentes = findViewById<RecyclerView>(R.id.recentes)

        val animeList = listOf(
            anime(
                capaUrl = "https://upload.wikimedia.org/wikipedia/pt/c/c9/Shingeki_no_Kyojin_4%C2%AA_temporada.jpg",
                titulo = "Exemplo Título",
                episodios = "00",
                sinopse = "Exemplo Sinopse",
                genero = "Exemplo Gênero",
                avaliacao = "0.0",
                status = "Planejando"
            )
        )

        //RecyclerView Recentes
        recyclerViewRecentes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewRecentes.adapter = recentesAdapter(animeList)

        //RecyclerView Sugestão
        recyclerViewSugest.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewSugest.adapter = SugestAdapter(animeList)

        //RecyclerView Minha Lista
        recyclerViewMylist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewMylist.adapter = MylistAdapter(animeList)


    }

    //Carregando dados para a sessão Recentes
    private fun carregarRecentes() {
        val db = FirebaseFirestore.getInstance()

        db.collection("recentes")
            .get()
            .addOnSuccessListener { result ->
                val animeList = mutableListOf<anime>()
                for (document in result) {
                    val item = anime (
                        capaUrl = document.getString("capaUrl") ?: "",
                        titulo = document.getString("titulo") ?: "",
                        episodios = document.getString("episodios") ?: "",
                        duracao = document.getString("duracao") ?: ""
                    )
                    animeList.add(item)
                }
                binding.recentes.adapter = recentesAdapter(animeList)
            }
            .addOnFailureListener { exception ->
                // Trate erros aqui
                println("Erro ao carregar Recentes: $exception")
            }
    }

    //Carregando dados para a sessão Recomendados
    private fun carregarSugestoes() {
        val db = FirebaseFirestore.getInstance()

        db.collection("recomendados")
            .get()
            .addOnSuccessListener { result ->
                val animeList = mutableListOf<anime>()
                for (document in result) {
                    val item = anime (
                        capaUrl = document.getString("capaUrl") ?: "",
                        titulo = document.getString("titulo") ?: "",
                        episodios = document.getString("episodios") ?: "",
                        duracao = document.getString("duracao") ?: "",
                        sinopse = document.getString("sinopse") ?: "",
                        genero = document.getString("genero") ?: "",
                        avaliacao = document.getString("avaliacao") ?: "",
                        status = ""

                    )
                    animeList.add(item)
                }
                binding.sugestSection.adapter = SugestAdapter(animeList)
            }
            .addOnFailureListener { exception ->
                // Trate erros aqui
                println("Erro ao carregar Sugestões: $exception")
            }
    }

    //Carregando dados para a sessão Minha Lista
    private fun carregarMinhaLista() {
        val db = FirebaseFirestore.getInstance()

        db.collection("minhaLista")
            .get()
            .addOnSuccessListener { result ->
                val animeList = mutableListOf<anime>()
                for (document in result) {
                    val item = anime (
                        capaUrl = document.getString("capaUrl") ?: "",
                        titulo = document.getString("titulo") ?: "",
                        episodios = document.getString("episodios") ?: "",
                        duracao = document.getString("duracao") ?: "",
                        sinopse = document.getString("sinopse") ?: "",
                        genero = document.getString("genero") ?: "",
                        avaliacao = document.getString("avaliacao") ?: "",
                        status = document.getString("status") ?: ""
                    )
                    animeList.add(item)
                }
                binding.mylistSection.adapter = MylistAdapter(animeList)
            }
            .addOnFailureListener { exception ->
                // Trate erros aqui
                println("Erro ao carregar Minha Lista: $exception")
            }
    }
}
