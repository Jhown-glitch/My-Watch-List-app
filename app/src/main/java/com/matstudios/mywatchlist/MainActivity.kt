package com.matstudios.mywatchlist

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matstudios.mywatchlist.adapter.MylistAdapter
import com.matstudios.mywatchlist.adapter.SugestAdapter
import com.matstudios.mywatchlist.adapter.anime
import com.matstudios.mywatchlist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Configuração do RecyclerView
        setContentView(R.layout.activity_main)
        val recyclerViewSugest = findViewById<RecyclerView>(R.id.sugestSection)
        val recyclerViewMylist = findViewById<RecyclerView>(R.id.mylistSection)

        val animeList = listOf(
            anime(
                capaUrl = "https://www.google.com",
                titulo = "Exemplo Título",
                episodios = "00",
                sinopse = "Exemplo Sinopse",
                genero = "Exemplo Gênero",
                avaliacao = "0.0",
                status = "Planejando"
            )
        )
        //RecyclerView Sugestão
        recyclerViewSugest.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewSugest.adapter = SugestAdapter(animeList)

        //RecyclerView Minha Lista
        recyclerViewMylist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewMylist.adapter = MylistAdapter(animeList)


    }
}