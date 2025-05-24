package com.matstudios.mywatchlist

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.matstudios.mywatchlist.adapter.MylistAdapter
import com.matstudios.mywatchlist.adapter.SugestAdapter
import com.matstudios.mywatchlist.adapter.content
import com.matstudios.mywatchlist.adapter.recentesAdapter
import com.matstudios.mywatchlist.databinding.ActivityMainBinding
import java.util.Date

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

        //Mensagem e Verificação de Usuário
        val user = FirebaseAuth.getInstance().currentUser
        val uid = FirebaseAuth.getInstance().currentUser!!?.uid

        val nomeUsuario = when {
            user == null -> "Usuário"
            user.isAnonymous -> "Visitante"
            !user.displayName.isNullOrEmpty() -> user.displayName
            !user.email.isNullOrEmpty() -> user.email?.substringBefore("@")
            else -> "Usuário"
        }
        binding.HelloU.text = "Olá, $nomeUsuario!"

        carregarRecentes()
        carregarSugestoes()
        carregarMinhaLista(uid = uid.toString())


//        val animeList = listOf(
//            content(
//                capaUrl = "https://upload.wikimedia.org/wikipedia/pt/c/c9/Shingeki_no_Kyojin_4%C2%AA_temporada.jpg",
//                titulo = "Exemplo Título",
//                episodios = "00",
//                sinopse = "Exemplo Sinopse",
//                genero = "Exemplo Gênero",
//                avaliacao = "0.0",
//                status = "Planejando"
//            )
//        )
    }

    //Carregando dados para a sessão Recentes
    private fun carregarRecentes() {
        val db = FirebaseFirestore.getInstance()
        val seteDias = Timestamp(Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000))
        val listaRecentes = mutableListOf<content>()

        val colecoes = listOf("animes", "filmes", "series")
        var consultasFinalizadas = 0

        for (colecao in colecoes) {
            db.collection(colecao)
                .whereGreaterThanOrEqualTo("adicionadoEm", seteDias)
                .get()
                .addOnSuccessListener { snapshot ->
                    for (document in snapshot) {
                        val item = document.toObject(content::class.java)
                        listaRecentes.add(item)
                    }
                    consultasFinalizadas++
                    if (consultasFinalizadas == colecoes.size) {
                        // Todas as consultas foram finalizadas, exibir o RecyclerView
                        // Configuração do RecyclerView Recentes
                        binding.recentes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        binding.recentes.adapter = recentesAdapter(listaRecentes)
                    }
                }
                .addOnFailureListener { exception ->
                    // Trate erros aqui
                    println("Erro ao carregar Recentes: $exception")
                }

        }

    }

    //Carregando dados para a sessão Recomendados
    private fun carregarSugestoes() {
        val db = FirebaseFirestore.getInstance()

        db.collection("animes")
            .get()
            .addOnSuccessListener { result ->
                val contentList = result.mapNotNull { it.toObject(content::class.java) }

                // Configuração do RecyclerView Sugestão
                binding.sugestSection.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.sugestSection.adapter = SugestAdapter(contentList)
            }
            .addOnFailureListener { exception ->
                // Trate erros aqui
                println("Erro ao carregar Sugestões: $exception")
            }
    }

    //Carregando dados para a sessão Minha Lista
    private fun carregarMinhaLista(uid: String) {
        val db = FirebaseFirestore.getInstance()
        val listaMinhaLista = mutableListOf<content>()

        db.collection("users").document(uid).collection("mylist")
            .get()
            .addOnSuccessListener { snapshot ->
                val total = snapshot.size()
                var carregados = 0

                for (document in snapshot) {
                    val ref = document.getDocumentReference("ref")
                    ref?.get()?.addOnSuccessListener { doc ->
                        val item = doc.toObject(content::class.java)
                        if (item != null) {
                            listaMinhaLista.add(item)
                        }
                        carregados++
                        if (carregados == total) {
                            // Todas as consultas foram finalizadas, exibir o RecyclerView
                            // Configuração do RecyclerView Minha Lista
                            binding.mylistSection.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                            binding.mylistSection.adapter = MylistAdapter(listaMinhaLista)
                        }

                    }
                }
            }
            .addOnFailureListener { exception ->
                // Trate erros aqui
                println("Erro ao carregar Minha Lista: $exception")
            }
    }
}
