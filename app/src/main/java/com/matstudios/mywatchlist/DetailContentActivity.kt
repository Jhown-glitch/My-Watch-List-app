package com.matstudios.mywatchlist

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
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


        }

    }
}