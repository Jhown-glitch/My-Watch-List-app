package com.matstudios.mywatchlist

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.matstudios.mywatchlist.adapter.anime
import com.matstudios.mywatchlist.databinding.ActivityDetailContentBinding
import jp.wasabeef.glide.transformations.BlurTransformation

class DetailContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailContentBinding
    //private lateinit var animeItem: anime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val anime = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("anime", anime::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("anime")
        }

        android.util.Log.d("DetailActivityDebug", "Anime recebido: $anime")
        anime?.let {
            android.util.Log.d("DetailActivityDebug", "Populando views com: ${it.titulo}")
            Glide.with(this) .load(it.capaUrl) .transform(BlurTransformation(15, 3)) .into(binding.fundoBlur)
            Glide.with(this) .load(it.capaUrl) .into(binding.capa)
            binding.titulo.text = it.titulo
            binding.sinopse.text = it.sinopse
            binding.genero.text = it.genero
            binding.ratingBar.rating = it.avaliacao.toFloatOrNull() ?: 0f
            binding.avaliacao.text = it.avaliacao
            binding.status.text = it.status

            if (!it.episodios.isNullOrEmpty()) {
                binding.episodiosLabel.visibility = View.VISIBLE
                binding.episodios.visibility = View.VISIBLE
                binding.duracaoLabel.visibility = View.GONE
                binding.duracao.visibility = View.GONE
                binding.episodios.text = it.episodios
            } else {
                binding.episodiosLabel.visibility = View.GONE
                binding.episodios.visibility = View.GONE
                binding.temporadaLabel.visibility = View.GONE
                binding.temporada.visibility = View.GONE
                binding.episAtualLabel.visibility = View.GONE
                binding.episAtual.visibility = View.GONE
                binding.duracaoLabel.visibility = View.VISIBLE
                binding.duracao.visibility = View.VISIBLE
                binding.duracao.text = it.duracao
            }

            binding.addRem.setOnClickListener {
                val user = FirebaseAuth.getInstance().currentUser

                if (user == null) {
                    Toast.makeText(this, "Você precisa estar logado para adicionar ou remover itens da sua lista.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection("users").document(user.uid) .collection("mylist").document(anime.titulo)


                docRef.set(anime)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Item adicionado à sua lista!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao adicionar item à sua lista: $e", Toast.LENGTH_SHORT).show()
                    }

            }


        }

    }
}