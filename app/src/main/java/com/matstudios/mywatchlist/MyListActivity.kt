package com.matstudios.mywatchlist

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.matstudios.mywatchlist.adapter.content
import com.matstudios.mywatchlist.adapter.contentUser
import com.matstudios.mywatchlist.adapter.mylistFullAdapter
import com.matstudios.mywatchlist.databinding.ActivityMyListBinding

class MyListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMyListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtendo UID do usuário logado
        //val user = FirebaseAuth.getInstance().currentUser
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        carregarMinhaLista(uid = uid.toString())

    }

    // Carregando dados da Main Activity da sessão Minha Lista
    private fun carregarMinhaLista(uid: String) {
        val db = FirebaseFirestore.getInstance()
        val minhaLista = mutableListOf<contentUser>()

        // Acessa os dados do Firestore
        db.collection("users").document(uid).collection("mylist").get().addOnSuccessListener { snapshot ->
            val total = snapshot.size()
            var carregados = 0

            for (document in snapshot) {
                try {
                    // Obtém a referência do documento
                    Log.d("DEBUG_DOC", "Documento recebido: ${document.data}")
                    val ref = document.getDocumentReference("ref")
                    val status = document.getString("status") ?: ""
                    val progresso = document.getString("progresso") ?: "0.0"
                    val minhaNota = document.getString("minhaNota") ?: "0.0"
                    Log.d("DEBUG_FIRE", "Ref do conteúdo: $ref")

                    ref?.get()?.addOnSuccessListener { doc ->
                        val conteudo = doc.toObject(content::class.java)
                        if (conteudo != null) {
                            val item = contentUser(
                                content = conteudo,
                                status = status,
                                progresso = progresso,
                                minhaNota = minhaNota
                            )
                            minhaLista.add(item)
                            Log.d("DEBUG_FIRE", "Dados do conteúdo: $conteudo adicionado")
                        } else {
                            Log.d("DEBUG_FIRE", "Item nulo ao desserializar")
                        }

                        carregados++
                        if (carregados == total) {
                            // Todas as consultas foram finalizadas, exibir o RecyclerView
                            // Configura o RecyclerView
                            binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
                            binding.recyclerView.adapter = mylistFullAdapter(minhaLista)
                            Log.d("DEBUG_FIRE", "Dados da Minha Lista carregados")
                        }
                    }?.addOnFailureListener { exception ->
                        // Trate erros aqui
                        Log.e("DEBUG_FIRE", "Erro ao carregar dados Minha Lista: $exception")
                    }
                } catch (e: Exception) {
                    Log.e("DEBUG_FIRE", "Erro ao ler dados de usuário: ${e.message}")
                }
            }

        } .addOnFailureListener { exception ->
            // Trate erros aqui
            println("Erro ao carregar dados Minha Lista: $exception")
        }
    }
}