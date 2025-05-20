package com.matstudios.mywatchlist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.matstudios.mywatchlist.databinding.DetailContentBinding
import jp.wasabeef.glide.transformations.BlurTransformation

class DetailContentActivity : AppCompatActivity() {

    private lateinit var binding: DetailContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_content)

        binding = DetailContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val capaUrl = intent.getStringExtra("capaUrl")

        Glide.with(this)
            .load(capaUrl)
            .transform(BlurTransformation(25, 3))
            .into(binding.fundoBlur)

    }
}