package com.matstudios.mywatchlist

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.matstudios.mywatchlist.ui.theme.MyWatchListTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        data class Anime(
            val titulo: String = "",
            val capa: String = "",
            val episodios: Int = 0,
            val sinopse: String = "",
            val status: String = ""
        )

        val container = findViewById<LinearLayout>(R.id.linearLayout)

        val db = Firebase.firestore.getInstance()
        db.collection("minhaLista").get().addOnSuccessListener { result ->
            for (document in result) {
                val anime = document.toObject(Anime::class.java)

                val view = layoutInflater.inflate(R.layout
                    .item_minha_lista, container, false)

                val img = view.findViewById<ImageView>(R.id.imageView3)
                val titulo = view.findViewById<TextView>(R.id.titulo)
                val eps = view.findViewById<TextView>(R.id.eps)
                val sinopse = view.findViewById<TextView>(R.id.sinopse)
                val status = view.findViewById<TextView>(R.id.status)

                titulo.text = anime.titulo
                eps.text = anime.episodios.toString() + "Ep"
                sinopse.text = anime.sinopse
                status.text = anime.status

                Glide.with(this).load(anime.capa).placeholder(R.drawable.capa_padrao).into(img)

                container.addView(view)
            }
        }
            .addOnFailureListener {
                exception ->
                Toast.makeText(this, "Ocorreu um erro ao carregar a lista", Toast
                    .LENGTH_LONG)
                    .show()
            }
    }
}