package com.matstudios.mywatchlist

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matstudios.mywatchlist.adapter.anime
import com.matstudios.mywatchlist.adapter.sugestAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



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
        recyclerViewSugest.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewMylist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewSugest.adapter = sugestAdapter(animeList)
        recyclerViewMylist.adapter = sugestAdapter(animeList)


    }
}