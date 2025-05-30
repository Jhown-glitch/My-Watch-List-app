package com.matstudios.mywatchlist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
    private var contentList = mutableListOf<contentUser>()

    private var myListListener: ListenerRegistration? = null

    // Declaração do launcher
    private lateinit var detailContentLauncher: ActivityResultLauncher<Intent>

    // IDs dos itens para fácil remoção
    private var itemPositionClicked: Int = -1 // Para saber qual item foi clicado e possivelmente modificado
    private var itemIdClicked: String? = null

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

        detailContentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Este callback é chamado quando DetailContentActivity finaliza
            if (result.resultCode == RESULT_OK) {
                // Algo mudou e DetailContentActivity está retornando um resultado positivo
                val data = result.data
                val itemWasRemoved = data?.getBooleanExtra("ITEM_REMOVED", false) ?: false
                val itemWasAdded = data?.getBooleanExtra("ITEM_ADDED", false) ?: false // Se você também tiver adição
                val modifiedItemId = data?.getStringExtra("MODIFIED_ITEM_ID")

                if (itemWasRemoved && modifiedItemId != null) {
                    // Remover o item da lista
                    //Encontrar o item pelo ID e removê-lo:
                    val itemIndex = contentList.indexOfFirst { it.content?.id == modifiedItemId }
                    if (itemIndex != -1) {
                        contentList.removeAt(itemIndex)
                        itemsAdapter.notifyItemRemoved(itemIndex)
                    } else {
                        // Se não encontrar pelo ID, talvez recarregar tudo como fallback
                        carregarMinhaLista(uid = FirebaseAuth.getInstance().currentUser!!.uid)
                    }
                    Log.d("MyListActivity", "Item $modifiedItemId removido com sucesso")
                    Toast.makeText(this, "Item removido com sucesso", Toast.LENGTH_SHORT).show()
                } else if (itemWasAdded) {
                    // Se um item foi adicionADO na DetailActivity (menos comum para "minha lista",
                    // mas possível se DetailActivity puder adicionar itens que não vieram dela)
                    Log.d("MyListActivity", "Item adicionado, recarregando lista.")
                    carregarMinhaLista(FirebaseAuth.getInstance().currentUser!!.uid)
                    Toast.makeText(this, "Item adicionado com sucesso", Toast.LENGTH_SHORT).show()
                }
                // Adicione mais verificações se necessário, por exemplo, se um item foi EDITADO
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // O usuário pode ter pressionado "Voltar" sem fazer alterações significativas
                Log.d("MyListActivity", "DetailContentActivity retornou RESULT_CANCELED")
            }
        }

        // Obtendo UID do usuário logado
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        if (uid != null) {
            carregarMinhaLista(uid = uid.toString())
        } else {
            Log.w("MyListActivity", "UID do usuário não encontrado")
        }
    }

    private fun setupRecyclerView() {
        itemsAdapter = mylistFullAdapter(contentList, this::onItemClicked)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = itemsAdapter
        Log.d("MyListActivity", "RecyclerView configurado")
    }

    // Função que é chamada quando um item da lista é clicado
    // Esta função agora usará o 'detailContentLauncher'
    private fun onItemClicked(item: contentUser, position: Int) {
        itemPositionClicked = position
        itemIdClicked = item.content?.id

        val intent = Intent(this, DetailContentActivity::class.java)
        intent.putExtra("content", item.content) // item.content DEVE ser Parcelable
        intent.putExtra("naMinhaLista", true)
        if (item.content?.id != null) { // Passar o ID para DetailActivity é útil
            intent.putExtra("CONTENT_ID", item.content?.id)
        }
        detailContentLauncher.launch(intent) // <<== Inicie a activity usando o launcher
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
