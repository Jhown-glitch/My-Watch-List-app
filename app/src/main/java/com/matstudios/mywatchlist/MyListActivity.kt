package com.matstudios.mywatchlist

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
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
import java.util.Locale
import kotlin.text.equals
import kotlin.text.filter
import kotlin.text.toMutableList

class MyListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyListBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var itemsAdapter: mylistFullAdapter
    private var contentList: MutableList<contentUser> = mutableListOf()
    private var myListListener: ListenerRegistration? = null
    // Declaração do launcher
    private lateinit var detailContentLauncher: ActivityResultLauncher<Intent>
    // IDs dos itens para fácil remoção
    private var itemPositionClicked: Int = -1 // Para saber qual item foi clicado e possivelmente modificado
    private var itemIdClicked: String? = null
    // Variáveis para o item do clique longo
    private var longClickedItemData: contentUser? = null
    private var longClickedItemPositionAdapter: Int = -1
    // Guarda a lista original sem filtros
    private var listaOriginal: MutableList<contentUser> = mutableListOf()
    // Rastreia o filtro atual, 'all' por padrão
    private var filtroAtual: String = "all"

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

        initializeDetailContentLauncher()
        setupRecyclerView()

        // Obtendo UID do usuário logado
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        if (uid != null) {
            carregarMinhaLista(uid = uid.toString())
        } else {
            Log.w("MyListActivity", "UID do usuário não encontrado")
        }

        val filterButton = binding.filterButton
        filterButton.setOnClickListener { view ->
            showFilterMenu(view)
        }
    }

    private fun showFilterMenu(view: View) {
        val popupMenu = android.widget.PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.filter_options, popupMenu.menu)
        when (filtroAtual) {
            "naoAssistido" -> popupMenu.menu.findItem(R.id.filter_naoAssistido).isChecked = true
            "assistindo" -> popupMenu.menu.findItem(R.id.filter_assistindo).isChecked = true
            "concluido" -> popupMenu.menu.findItem(R.id.filter_concluido).isChecked = true
            "alfabeto" -> popupMenu.menu.findItem(R.id.filter_alfabeto).isChecked = true
            "alfaInvertido" -> popupMenu.menu.findItem(R.id.filter_alfaInvertido).isChecked = true
            "type" -> popupMenu.menu.findItem(R.id.filter_type).isChecked = true
            "nota" -> popupMenu.menu.findItem(R.id.filter_nota).isChecked = true
            else -> popupMenu.menu.findItem(R.id.filter_all).isChecked = true
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.filter_all -> {
                    filtroAtual = "all"
                    aplicarFiltroNaLista()
                    true
                }
                R.id.filter_naoAssistido -> {
                    filtroAtual = "naoAssistido"
                    aplicarFiltroNaLista()
                    true
                }
                R.id.filter_assistindo -> {
                    filtroAtual = "assistindo"
                    aplicarFiltroNaLista()
                    true
                }
                R.id.filter_concluido -> {
                    filtroAtual = "concluido"
                    aplicarFiltroNaLista()
                    true
                }
                R.id.filter_alfabeto -> {
                    filtroAtual = "alfabeto"
                    aplicarFiltroNaLista()
                    true
                }
                R.id.filter_alfaInvertido -> {
                    filtroAtual = "alfaInvertido"
                    aplicarFiltroNaLista()
                    true
                }
                R.id.filter_type -> {
                    filtroAtual = "type"
                    aplicarFiltroNaLista()
                    true
                }
                R.id.filter_nota -> {
                    filtroAtual = "nota"
                    aplicarFiltroNaLista()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun aplicarFiltroNaLista() {
        Log.d("MyListActivity", "Aplicando filtro: $filtroAtual")
        val listaFiltrada: List<contentUser> = when (filtroAtual) {
            "naoAssistido" -> listaOriginal.filter {
                it.status.equals("Não Assistido", ignoreCase = true)
            }
            "assistindo" -> listaOriginal.filter {
                it.status.equals("Assistindo", ignoreCase = true)
            }
            "concluido" -> listaOriginal.filter {
                it.status.equals("Concluído", ignoreCase = true)
            }
            "alfabeto" -> listaOriginal.sortedBy {
                it.content?.titulo?.get("pt")?.lowercase() ?: ""
            }
            "alfaInvertido" -> listaOriginal.sortedByDescending {
                it.content?.titulo?.get("pt")?.lowercase() ?: ""
            }
            "type" -> listaOriginal.sortedBy {
                it.content?.tipo?.get("pt")?.lowercase() ?: ""
            }
            "nota" -> listaOriginal.sortedByDescending {
                it.minhaNota.toDoubleOrNull() ?: 0.0
            }
            else -> listaOriginal // "all" ou qualquer outro
        }

//        val statusAlvo = when (filtroAtual) {
//            "naoAssistido" -> "Não Assistido"
//            "assistindo" -> "Assistindo"
//            "concluido" -> "Concluído"
//            else -> null
//        }
//        if (statusAlvo != null) {
//            Log.d("MyListActivity", "Aplicando filtro: $filtroAtual")
//        }
//        val listaFiltrada = if (filtroAtual == "ALL") {
//            ArrayList(listaOriginal) // Cria uma cópia da lista original
//        } else {
//            listaOriginal.filter { it.status.equals(filtroAtual, ignoreCase = true) }
//                .toMutableList()
//        }
        // Atualiza a lista que o adapter está usando e notifica
        updateAdapterWithData(listaFiltrada.toMutableList())
    }

    private fun initializeDetailContentLauncher() {
        detailContentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Este callback é chamado quando DetailContentActivity finaliza
            if (result.resultCode == RESULT_OK) {
                // Algo mudou e DetailContentActivity está retornando um resultado positivo
                val data = result.data
                val itemWasRemoved = data?.getBooleanExtra("ITEM_REMOVED", false) ?: false
                val itemWasAdded = data?.getBooleanExtra("ITEM_ADDED", false) ?: false
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
    }

    private fun setupRecyclerView() {
        itemsAdapter = mylistFullAdapter(
            contentList,
            {item, position -> onItemClicked(item, position)}, // Lambda para o clique
            {item, position, view -> onItemLongClicked(item, position, view)} // Lambda para o clique longo
        )
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
            intent.putExtra("CONTENT_ID", item.content.id)
        }
        detailContentLauncher.launch(intent) // <<== Inicie a activity usando o launcher
    }

    private fun onItemLongClicked(item: contentUser, position: Int, view: View): Boolean {
        longClickedItemData = item
        longClickedItemPositionAdapter = position
        view.showContextMenu()
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.item_options, menu)

        val currentItem = longClickedItemData
        val title = currentItem?.content?.titulo?.get(Locale.getDefault().language)
            ?: currentItem?.content?.titulo?.get("en")
            ?: "Ações do Item"
        menu?.setHeaderTitle(title)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val currentItem = longClickedItemData
        val currentPosition = longClickedItemPositionAdapter

        if (currentItem == null || currentPosition == -1) {
            Log.w("MyListActivity", "Tentativa de uso de menu de contexto sem dados válidos")
            return super.onContextItemSelected(item)
        }

        return when (item.itemId) {
            R.id.action_change_status_mylist_item -> {
                Toast.makeText(this, "Mudar status: ${currentItem.content?.titulo?.get("pt")}", Toast.LENGTH_SHORT).show()
                val statusOption = arrayOf("Não Assistido", "Assistindo", "Concluído")
                AlertDialog.Builder(this).setTitle("Mudar Status").setItems(statusOption) { _, which ->
                    val newStatus = statusOption[which]
                    alterarStatusDoItem(currentItem, longClickedItemPositionAdapter, newStatus)
                }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            }
            R.id.action_delete_mylist_item_context -> {
                val itemIdToRemove = currentItem.content?.id
                if (itemIdToRemove != null) {
                    removerItemDaListaComConfirmacao(itemIdToRemove, currentPosition, currentItem) // Exemplo com confirmação
                } else {
                    Toast.makeText(this, "ID do item não encontrado para remoção.", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    // Função para alterar status
    private fun alterarStatusDoItem(itemParaAlterar: contentUser, positionInAdapter: Int, newStatus: String) {
        // Lógica para alterar o status do item
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuário não logado.", Toast.LENGTH_SHORT).show();
            return
        }

        val itemID = itemParaAlterar.content?.id
        if (itemID == null) {
            Toast.makeText(this, "ID do item não encontrado para alteração de status.", Toast.LENGTH_SHORT).show();
            return
        }

        // Atualização do Firestore
        db.collection("users").document(user.uid).collection("mylist").document(itemID)
            .update("status", newStatus)
            .addOnSuccessListener {
                Log.d("MyListActivity", "Status do item $itemID alterado para $newStatus")
                Toast.makeText(this, "Status de $itemID alterado com sucesso para $newStatus.", Toast.LENGTH_SHORT).show()

                if (positionInAdapter in contentList.indices) {
                    // Atualizar o item na lista local
                    contentList[positionInAdapter].status = newStatus
                    itemsAdapter.notifyItemChanged(positionInAdapter)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MyListActivity", "Erro ao alterar status do item $itemID", e)
                Toast.makeText(this, "Erro ao alterar status.", Toast.LENGTH_SHORT).show()
            }
        // Atualizar a lista local e o adapter
        itemParaAlterar.status = newStatus
        itemsAdapter.notifyItemChanged(positionInAdapter)
    }

    // Função para remover
    private fun removerItemDaListaComConfirmacao(itemId: String, positionInAdapter: Int, itemParaRemover: contentUser) {
        // Opcional: Mostrar um diálogo de confirmação antes de remover
         AlertDialog.Builder(this)
        .setTitle("Confirmar Remoção")
        .setMessage("Tem certeza que deseja remover '${itemParaRemover.content?.titulo?.get("pt")}' da sua lista?")
        .setPositiveButton("Remover") { dialog, _ ->
        // Lógica de remoção real aqui
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuário não logado.", Toast.LENGTH_SHORT).show(); return@setPositiveButton
        }
        db.collection("users").document(user.uid).collection("mylist").document(itemId)
            .delete()
            .addOnSuccessListener {
                Log.d("MyListActivity", "Item $itemId removido do Firestore (menu).")
                Toast.makeText(this, "'${itemParaRemover.content?.titulo?.get("pt")}' removido.", Toast.LENGTH_SHORT).show()

                // Atualizar a lista local e o adapter
                if (positionInAdapter >= 0 && positionInAdapter < contentList.size) {
                    // Verificar se o item na posição ainda é o mesmo (segurança extra)
                    if (contentList[positionInAdapter].content?.id == itemId) {
                        contentList.removeAt(positionInAdapter)
                        itemsAdapter.notifyItemRemoved(positionInAdapter)
                        // Se as posições dos itens subsequentes são importantes, notifique a mudança de range:
                        // itemsAdapter.notifyItemRangeChanged(positionInAdapter, contentList.size - positionInAdapter)
                        updateEmptyViewVisibility() // Atualiza se a lista ficou vazia
                    } else {
                        // Inconsistência, melhor recarregar
                        carregarMinhaLista(user.uid)
                    }
                } else {
                    // Posição inválida, recarregar
                    carregarMinhaLista(user.uid)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MyListActivity", "Erro ao remover $itemId do Firestore (menu)", e)
                Toast.makeText(this, "Erro ao remover.", Toast.LENGTH_SHORT).show()
            }
         }
        .setNegativeButton("Cancelar", null)
        .show()
    }

    // Carregando dados da Main Activity da sessão Minha Lista
    private fun carregarMinhaLista(uid: String) {
        Log.d("MyListActivity", "Carregando Lista para: $uid")
        myListListener?.remove() // Remove o listener anterior, se houver

        myListListener = db.collection("users").document(uid).collection("mylist")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("MyListActivity", "Erro ao carregar Minha Lista", error)
                    listaOriginal.clear()
                    updateAdapterWithData(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    Log.d("MyListActivity", "Lista do usuário vazia ou snapshot nulo")
                    listaOriginal.clear()
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
                    listaOriginal.clear()
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
                    } else {
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
                                Log.d("MyListActivity", "Todas as refs carregadas (${refsCarregadas}/${totalRefs}). Atualizando listaOriginal.")
                                listaOriginal.clear()
                                listaOriginal.addAll(tempList)

                                aplicarFiltroNaLista()
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
                                Log.d("MyListActivity", "Todas as refs carregadas com falhas (${refsCarregadas}/${totalRefs}). Atualizando listaOriginal.")
                                listaOriginal.clear()
                                listaOriginal.addAll(tempList)
                                updateAdapterWithData(tempList)
                            }
                        }
                    }

                }

            }
        Log.d("MyListActivity", "Listener de Minha Lista configurado para: $uid")
    }

    private fun updateAdapterWithData(newList: List<contentUser>) {
        Log.d("MyListActivity", "Atualizando adapter com ${newList.size} itens (após filtro: $filtroAtual).")
        contentList.clear()
        contentList.addAll(newList)

        // Chama o método updateData DO ADAPTER
        if (::itemsAdapter.isInitialized) {
            itemsAdapter.updateData(ArrayList(contentList))
        } else {
            Log.w("MyListActivity", "itemsAdapter não foi inicializada")
        }
        Log.d("MyListActivity", "Dados da Minha Lista atualizados com ${contentList.size} itens")

        updateEmptyViewVisibility()
    }

    private fun updateEmptyViewVisibility() {
         if (contentList.isEmpty()) {
             binding.emptyListMessage.visibility = View.VISIBLE
             binding.recyclerView.visibility = View.GONE
         } else {
             binding.emptyListMessage.visibility = View.GONE
             binding.recyclerView.visibility = View.VISIBLE
         }
    }


    override fun onStop() {
        super.onStop()
        myListListener?.remove()
    }
}
