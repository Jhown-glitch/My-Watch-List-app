package com.matstudios.mywatchlist

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.matstudios.mywatchlist.adapter.content
import com.matstudios.mywatchlist.databinding.ActivityDetailContentBinding
import jp.wasabeef.glide.transformations.BlurTransformation
import java.util.Locale

class DetailContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailContentBinding
    private var currentContentObject: content? = null // Para armazenar o objeto content completo
    private var currentContentId: String? = null
    private var estaNaMinhaListaGlobal: Boolean = false // Para rastrear o estado atual
    private var resultadoOkDefinido: Boolean = false // Para rastrear se RESULT_OK foi chamado

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

        currentContentObject = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("content", content::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("content")
        }

        currentContentId = intent.getStringExtra("CONTENT_ID") ?: currentContentObject?.id
        estaNaMinhaListaGlobal = intent.getBooleanExtra("naMinhaLista", false)

        if (currentContentObject == null || currentContentId == null || currentContentId!!.isBlank()) {
            Toast.makeText(this, "Erro: Dados do conteúdo inválidos.", Toast.LENGTH_LONG).show()
            Log.e("DetailContentActivity", "Content object ou ID está nulo/vazio. ID: $currentContentId, Object: $currentContentObject")
            setResult(Activity.RESULT_CANCELED) // Informa que nada mudou ou houve erro
            finish()
            return
        }

        popularViews(currentContentObject!!)
        atualizarBotaoAddRem()

        resultadoOkDefinido = false // Reseta ao criar a activity
        binding.addRem.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "Você precisa de um UID para fazer isso.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Usar currentContentId que já foi validado
            if (estaNaMinhaListaGlobal) {
                removerItemDaLista(user.uid, currentContentId!!)
            } else {
                adicionarItemNaLista(user.uid, currentContentObject!!, currentContentId!!)
            }
        }
    }

    private fun popularViews(contentData: content) {
        val idioma= Locale.getDefault().language
        Log.d("DetailActivityDebug", "Populando views com: ${contentData.titulo}")
        Glide.with(this) .load(contentData.capaUrl) .transform(BlurTransformation(15, 3)) .into(binding.fundoBlur)
        Glide.with(this) .load(contentData.capaUrl) .into(binding.capa)
        binding.titulo.text = contentData.titulo[idioma] ?: "Sem Título"
        binding.sinopse.text = contentData.sinopse[idioma] ?: "Sem Sinopse"
        binding.genero.text = contentData.genero[idioma]?.joinToString(", ") ?: "-"
        binding.ratingBar.rating = contentData.avaliacao.toFloatOrNull() ?: 0f
        binding.avaliacao.text = contentData.avaliacao
        binding.status.text = contentData.status
        binding.anoLanca.text = contentData.ano
        binding.tipo.text = contentData.tipo[idioma] ?: "Sem Tipo"

        if (!contentData.episodios.isNullOrEmpty()) {
            binding.episodiosLabel.visibility = View.VISIBLE
            binding.episodios.visibility = View.VISIBLE
            binding.temporadaLabel.visibility = View.VISIBLE
            binding.temporada.visibility = View.VISIBLE
            binding.duracaoLabel.visibility = View.GONE
            binding.duracao.visibility = View.GONE
            binding.episodios.text = contentData.episodios
            binding.temporada.text = contentData.temporadas
        } else {
            binding.episodiosLabel.visibility = View.GONE
            binding.episodios.visibility = View.GONE
            binding.temporadaLabel.visibility = View.GONE
            binding.temporada.visibility = View.GONE
            binding.episAtualLabel.visibility = View.GONE
            binding.episAtual.visibility = View.GONE
            binding.duracaoLabel.visibility = View.VISIBLE
            binding.duracao.visibility = View.VISIBLE
            binding.duracao.text = contentData.duracao
        }
    }

    private fun atualizarBotaoAddRem() {
        if (estaNaMinhaListaGlobal) {
            binding.addRem.text = "Remover"
        } else {
            binding.addRem.text = "Adicionar à lista"
        }
    }

    private fun adicionarItemNaLista(uid: String, contentData: content, contentId: String) {
        val db = FirebaseFirestore.getInstance()

        // Determinar a coleção original (filmes, series, animes, etc.) com base no tipoID
        val tipoOriginalCollection = when (contentData.tipoID.lowercase(Locale.ROOT)) {
            "anime" -> "animes"
            "filme" -> "filmes"
            "serie" -> "series"
            // Adicione outros tipos conforme necessário
            else -> {
                Log.e("DetailContentActivity", "Tipo de conteúdo desconhecido: ${contentData.tipoID}")
                Toast.makeText(this, "Erro: Tipo de conteúdo desconhecido.", Toast.LENGTH_SHORT).show()
                return
            }
        }
        val contentDocumentRef = db.collection(tipoOriginalCollection).document(contentId)
        val itemParaMinhaLista = hashMapOf(
            "ref" to contentDocumentRef, // Referência ao documento original
            "status" to "Não assistido", // Status inicial padrão
            "minhaNota" to "0", // Nota inicial padrão
            "progresso" to "0"  // Progresso inicial padrão
            // Você pode adicionar um timestamp de quando foi adicionado, se desejar
            // "adicionadoEm" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid).collection("mylist").document(contentId).set(itemParaMinhaLista)
            .addOnSuccessListener {
                Toast.makeText(this, "Item adicionado à sua lista!", Toast.LENGTH_SHORT).show()
                estaNaMinhaListaGlobal = true // Atualizar o estado
                atualizarBotaoAddRem() // Atualizar o botão

                val resultIntent = Intent()
                resultIntent.putExtra("ITEM_ADDED", true)
                resultIntent.putExtra("MODIFIED_ITEM_ID", contentId)
                setResult(RESULT_OK, resultIntent)
                resultadoOkDefinido = true // Informa que RESULT_OK foi chamado
                //finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao adicionar item à sua lista: $e", Toast.LENGTH_SHORT).show()
                Log.e("DetailContentActivity", "Erro ao adicionar item $contentId à lista", e)
            }

    }

    private fun removerItemDaLista(uid: String, contentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).collection("mylist").document(contentId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Item removido da sua lista!", Toast.LENGTH_SHORT).show()
                estaNaMinhaListaGlobal = false // Atualizar o estado
                atualizarBotaoAddRem() // Atualizar o botão

                val resultIntent = Intent()
                resultIntent.putExtra("ITEM_REMOVED", true)
                resultIntent.putExtra("MODIFIED_ITEM_ID", contentId)
                setResult(RESULT_OK, resultIntent)
                resultadoOkDefinido = true // Informa que RESULT_OK foi chamado
                //finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao remover item da sua lista: $e", Toast.LENGTH_SHORT).show()
                Log.e("DetailContentActivity", "Erro ao remover item $contentId da lista", e)
            }

    }

    override fun onBackPressed() {
        // Se nenhuma ação de adicionar/remover foi explicitamente bem-sucedida
        // para chamar setResult(Activity.RESULT_OK, ...),
        // então o resultado padrão será RESULT_CANCELED ou o que foi definido por último.
        if (!resultadoOkDefinido) { // Verifica se RESULT_OK já não foi setado por add/remove
            setResult(Activity.RESULT_CANCELED)
        }
        super.onBackPressed()
    }
}