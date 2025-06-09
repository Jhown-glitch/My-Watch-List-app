package com.matstudios.mywatchlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.matstudios.mywatchlist.adapter.MylistAdapter
import com.matstudios.mywatchlist.adapter.SugestAdapter
import com.matstudios.mywatchlist.adapter.content
import com.matstudios.mywatchlist.adapter.recentesAdapter
import com.matstudios.mywatchlist.databinding.ActivityMainBinding
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var mListaListener: ListenerRegistration? = null

    private lateinit var mylistAdapter: MylistAdapter
    private val mylistLista = mutableListOf<content>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Mensagem e Verificação de Usuário
        val user = FirebaseAuth.getInstance().currentUser
        //val uid = FirebaseAuth.getInstance().currentUser!!?.uid

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
        //carregarMinhaLista(uid = uid.toString())

        binding.mylistSection.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mylistAdapter = MylistAdapter(mylistLista)
        binding.mylistSection.adapter = mylistAdapter
        Log.d("MainActivity", "Seção Minha Lista configurada no onCreate.")

        // Clique para pesquisar
        binding.buttonSearchScreen.setOnClickListener {
            // Lógica para abrir a tela de Pesquisa
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        // Clique no Ver Tudo Minha Lista
        binding.verTudoML.setOnClickListener {
            // Lógica para abrir a tela de Minha Lista completa
            val intent = Intent(this, MyListActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            carregarMinhaLista(uid = user.uid)
        } else {
            // Limpa a lista e notifica o adapter existente
            mylistLista.clear()
            if (::mylistAdapter.isInitialized) { // Verifica se o adapter foi criado em onCreate
                mylistAdapter.notifyDataSetChanged()
            }
            Log.d("MainActivity", "Usuário não logado, Minha Lista limpa.")
        }
    }

    override fun onStop() {
        super.onStop()
        mListaListener?.remove()
        Log.d("MainActivity", "Listener de Minha Lista removido.")
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
        // Listener do Firestore (como nas versões anteriores que te mandei)
        mListaListener?.remove() // Remove listener antigo

        mListaListener = db.collection("users").document(uid).collection("mylist")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("MinhaLista", "Listen failed.", error)
                    // Atualiza a lista existente e notifica o adapter existente
                    mylistLista.clear()
                    if (::mylistAdapter.isInitialized) mylistAdapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    Log.d("MinhaLista", "Snapshot nulo recebido.")
                    mylistLista.clear()
                    if (::mylistAdapter.isInitialized) mylistAdapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                Log.d("MinhaLista", "Dados recebidos para Minha Lista. Documentos: ${snapshots.size()}")
                val documentosDaMinhaLista = snapshots.documents
                val listaTemporariaParaNovosItens = mutableListOf<content>() // Nova lista temporária para esta atualização

                if (documentosDaMinhaLista.isEmpty()) {
                    Log.d("MinhaLista", "Nenhum item na Minha Lista do usuário.")
                    mylistLista.clear()
                    if (::mylistAdapter.isInitialized) mylistAdapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                val totalDeReferencias = documentosDaMinhaLista.size
                var referenciasProcessadas = 0

                // Caso especial: se a lista de documentos da MinhaLista estiver vazia
                // mas totalDeReferencias for 0 (o que não deveria acontecer se isEmpty() já foi checado,
                // mas para segurança)
                if (totalDeReferencias == 0) {
                    mylistLista.clear()
                    if (::mylistAdapter.isInitialized) mylistAdapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                documentosDaMinhaLista.forEach { docMinhaLista ->
                    val refConteudo = docMinhaLista.getDocumentReference("ref")

                    if (refConteudo == null) {
                        Log.w("MinhaLista", "Referência 'ref' nula no documento ${docMinhaLista.id}")
                        referenciasProcessadas++
                        if (referenciasProcessadas == totalDeReferencias) {
                            // Todos processados, atualiza a lista do adapter
                            mylistLista.clear()
                            mylistLista.addAll(listaTemporariaParaNovosItens)
                            if (::mylistAdapter.isInitialized) mylistAdapter.notifyDataSetChanged()
                            Log.d("MinhaLista", "Adapter atualizado (com refs nulas). Itens: ${mylistLista.size}")
                        }
                        return@forEach
                    }

                    refConteudo.get()
                        .addOnSuccessListener { docConteudo ->
                            val item = docConteudo.toObject(content::class.java)
                            if (item != null) {
                                listaTemporariaParaNovosItens.add(item)
                            } else {
                                Log.w("MinhaLista", "Falha ao converter doc ${docConteudo.id} para content.")
                            }
                            referenciasProcessadas++
                            if (referenciasProcessadas == totalDeReferencias) {
                                mylistLista.clear()
                                mylistLista.addAll(listaTemporariaParaNovosItens)
                                if (::mylistAdapter.isInitialized) mylistAdapter.notifyDataSetChanged()
                                Log.d("MinhaLista", "Adapter atualizado. Itens: ${mylistLista.size}")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("MinhaLista", "Erro ao buscar conteúdo referenciado ${refConteudo.path}: $e")
                            referenciasProcessadas++
                            if (referenciasProcessadas == totalDeReferencias) {
                                mylistLista.clear()
                                mylistLista.addAll(listaTemporariaParaNovosItens)
                                if (::mylistAdapter.isInitialized) mylistAdapter.notifyDataSetChanged()
                                Log.d("MinhaLista", "Adapter atualizado (com falhas em refs). Itens: ${mylistLista.size}")
                            }
                        }
                }
            }
        Log.d("MainActivity", "Listener da Minha Lista configurado para UID: $uid")
    }

}
