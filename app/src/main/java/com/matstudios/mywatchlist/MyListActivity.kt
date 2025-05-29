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
import com.google.firebase.firestore.ListenerRegistration
import com.matstudios.mywatchlist.adapter.content
import com.matstudios.mywatchlist.adapter.contentUser
import com.matstudios.mywatchlist.adapter.mylistFullAdapter
import com.matstudios.mywatchlist.databinding.ActivityMyListBinding

class MyListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyListBinding
    private lateinit var db: FirebaseFirestore

    private lateinit var itemsAdapter: mylistFullAdapter
    private val contentList = mutableListOf<contentUser>()

    private var myListListener: ListenerRegistration? = null

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

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()

        // Obtendo UID do usuário logado
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        if (uid != null) {
            carregarMinhaLista(uid = uid.toString())
        } else {
            Log.w("MyListActivity", "UID do usuário não encontrado")
        }
    }

    private fun setupRecyclerView() {
        itemsAdapter = mylistFullAdapter(contentList)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = itemsAdapter
        Log.d("MyListActivity", "RecyclerView configurado")
    }

    // Carregando dados da Main Activity da sessão Minha Lista
    private fun carregarMinhaLista(uid: String) {
        Log.d("MyListActivity", "Carregando Lista para: $uid")
        myListListener?.remove() // Remove o listener anterior, se houver

        myListListener = db.collection("users").document(uid).collection("mylist")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("MyListActivity", "Erro ao carregar Minha Lista", error)
                    updateAdapterWithData(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    Log.d("MyListActivity", "Lista do usuário vazia ou snapshot nulo")
                    updateAdapterWithData(emptyList())
                    return@addSnapshotListener
                }

                Log.d(
                    "MyListActivity",
                    "Dados da Lista recebidos (listener). Documentos: ${snapshot.documents.size}"
                )
                val tempList = mutableListOf<contentUser>()
                val totalRefs = snapshot.size()
                var refsCarregadas = 0

                if (totalRefs == 0) {
                    updateAdapterWithData(emptyList())
                    return@addSnapshotListener
                }

                snapshot.documents.forEach { document ->
                    val contentRef = document.getDocumentReference("ref")

                    // Pegar os dados específicos do usuário DESTE documento da subcoleção 'mylist'
                    val notaUsuario = document.getString("minhaNota") ?: ""
                    val progressoUsuario = document.getString("progresso") ?: ""
                    val statusUsuario = document.getString("status") ?: ""

                    if (contentRef == null) {
                        Log.w("MyListActivity", "Documento sem referência de conteúdo")
                        // Ainda criar o contentUser com os dados do usuário, mas content aninhado será null
                        val itemUsuario = contentUser(
                            content = null,
                            minhaNota = notaUsuario,
                            progresso = progressoUsuario,
                            status = statusUsuario
                        )
                        tempList.add(itemUsuario)
                        refsCarregadas++
                        if (refsCarregadas == totalRefs) {
                            updateAdapterWithData(tempList)
                        }
                        return@forEach
                    }
                    contentRef.get().addOnSuccessListener { contentDoc ->
                        val contentPrincipal: content?

                        if (contentDoc.exists()) {
                            // Converter o documento referenciado para a classe 'content'
                            contentPrincipal = contentDoc.toObject(content::class.java)
                            if (contentPrincipal == null) {
                                Log.w("MyListActivity", "Falha ao converter doc ${contentDoc.id} para content.class.java")
                            }
                        } else {
                            Log.w("MyListActivity", "Documento de conteúdo ${contentRef.path} não existe")
                            contentPrincipal = null
                        }

                        // Criar o objeto contentUser combinando os dados
                        val itemUsuario = contentUser(
                            content = contentPrincipal, // objetoContentPrincipal pode ser null aqui
                            minhaNota = notaUsuario,
                            progresso = progressoUsuario,
                            status = statusUsuario
                        )
                        tempList.add(itemUsuario)
                        refsCarregadas++
                        if (refsCarregadas == totalRefs) {
                            updateAdapterWithData(tempList)
                        }
                    }.addOnFailureListener { exception ->
                        Log.e(
                            "MyListActivity",
                            "Erro ao carregar conteúdo (listener) ${contentRef.path}",
                            exception
                        )
                        // Em caso de falha ao buscar o conteúdo, ainda adicionamos o item
                        // com os dados do usuário e 'content' como null.
                        val itemUsuario = contentUser(
                            content = null,
                            minhaNota = notaUsuario,
                            progresso = progressoUsuario,
                            status = statusUsuario
                        )
                        tempList.add(itemUsuario)
                        refsCarregadas++
                        if (refsCarregadas == totalRefs) {
                            updateAdapterWithData(tempList)
                        }
                    }
                }
            }
        Log.d("MyListActivity", "Listener de Minha Lista configurado para: $uid")
    }

    private fun updateAdapterWithData(newData: List<contentUser>) {
        contentList.clear()
        contentList.addAll(newData)
        itemsAdapter.notifyDataSetChanged()
        Log.d("MyListActivity", "Dados da Minha Lista atualizados com ${contentList.size} itens")
        // Verificar se a lista está vazia e exibir a mensagem apropriada
//        if (contentList.isEmpty()) {
//            binding.recyclerView.visibility = RecyclerView.GONE
//            binding.emptyView.visibility = RecyclerView.VISIBLE
//        } else {
//            binding.recyclerView.visibility = RecyclerView.VISIBLE
//            binding.emptyView.visibility = RecyclerView.GONE
//        }
    }

    override fun onStop() {
        super.onStop()
        myListListener?.remove()
    }
}
