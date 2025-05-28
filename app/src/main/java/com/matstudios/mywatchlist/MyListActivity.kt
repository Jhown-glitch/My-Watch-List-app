package com.matstudios.mywatchlist

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.matstudios.mywatchlist.adapter.contentUser
import com.matstudios.mywatchlist.adapter.mylistFullAdapter
import com.matstudios.mywatchlist.databinding.ActivityMyListBinding

class MyListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_list)
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
        db.collection("users").document(uid).collection("mylist").get()
            .addOnSuccessListener { snapshot ->
                val total = snapshot.size()
                var carregados = 0

                for (document in snapshot) {
                    // Obtém a referência do documento
                    val ref = document.getDocumentReference("ref")
                    val status = document.getString("status") ?: ""
                    val progresso = document.getString("progresso") ?: "0.0"
                    val minhaNota = document.getString("minhaNota") ?: "0.0"

                    ref?.get()?.addOnSuccessListener { doc ->
                        val item = doc.toObject(contentUser::class.java)
                        if (item != null) {
                            val content = contentUser(
                                item.content,
                                status = status,
                                progresso = progresso,
                                minhaNota = minhaNota
                            )
                            minhaLista.add(content)
                        }
                        carregados++
                        if (carregados == total) {
                            // Todas as consultas foram finalizadas, exibir o RecyclerView
                            // Configura o RecyclerView
                            binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
                            binding.recyclerView.adapter = mylistFullAdapter(minhaLista)
                        }
                    }
                }
            } .addOnFailureListener { exception ->
                // Trate erros aqui
                println("Erro ao carregar dados Minha Lista: $exception")
            }

    }
}