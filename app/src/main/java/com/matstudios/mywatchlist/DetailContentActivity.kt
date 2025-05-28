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
import com.matstudios.mywatchlist.adapter.content
import com.matstudios.mywatchlist.databinding.ActivityDetailContentBinding
import jp.wasabeef.glide.transformations.BlurTransformation
import java.util.Locale

class DetailContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailContentBinding
    //private lateinit var animeItem: content? = null

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

        val content = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("content", content::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("content")
        }

        val idioma= Locale.getDefault().language

        android.util.Log.d("DetailActivityDebug", "Anime recebido: $content")
        content?.let {
            android.util.Log.d("DetailActivityDebug", "Populando views com: ${it.titulo}")
            Glide.with(this) .load(it.capaUrl) .transform(BlurTransformation(15, 3)) .into(binding.fundoBlur)
            Glide.with(this) .load(it.capaUrl) .into(binding.capa)
            binding.titulo.text = it.titulo[idioma] ?: "Sem Título"
            binding.sinopse.text = it.sinopse[idioma] ?: "Sem Sinopse"
            binding.genero.text = it.genero[idioma]?.joinToString(", ") ?: "-"
            binding.ratingBar.rating = it.avaliacao.toFloatOrNull() ?: 0f
            binding.avaliacao.text = it.avaliacao
            binding.status.text = it.status
            binding.anoLanca.text = it.ano
            binding.tipo.text = it.tipo[idioma] ?: "Sem Tipo"

            if (!it.episodios.isNullOrEmpty()) {
                binding.episodiosLabel.visibility = View.VISIBLE
                binding.episodios.visibility = View.VISIBLE
                binding.temporadaLabel.visibility = View.VISIBLE
                binding.temporada.visibility = View.VISIBLE
                binding.duracaoLabel.visibility = View.GONE
                binding.duracao.visibility = View.GONE
                binding.episodios.text = it.episodios
                binding.temporada.text = it.temporadas
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
                    Toast.makeText(this, "Você precisa de um UID para fazer isso.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (content == null || content.id.isBlank()) {
                    Log.e("DetailContentActivity", "ID do conteúdo está vazio ou nulo.")
                    Toast.makeText(this, "Erro: ID do conteúdo inválido.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection("users").document(user.uid).collection("mylist").document(content.id)
                // Verifica o tipo do conteúdo
                val tipo = content.tipoID.lowercase() // Garante que seja minúsculo
                val contentREF = db.collection(tipo + "s").document(content.id) // Define a coleção correta
                val referencia = hashMapOf(
                    "ref" to contentREF,
                    "status" to "Planejando",
                    "minhaNota" to "4,5",
                    "progresso" to "0"
                )

                docRef.set(referencia)
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