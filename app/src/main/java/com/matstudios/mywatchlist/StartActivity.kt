package com.matstudios.mywatchlist

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.matstudios.mywatchlist.ui.login.LoginActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Pega o usuário atual
        val user = FirebaseAuth.getInstance().currentUser

        //Verifica se o usuário está logado
        if (user != null) {
            //Cria os documentos do usuário no Firestore
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(user.uid)

            userDoc.get().addOnSuccessListener { documentSnapshot ->

                //Se o documento não existir, cria um novo documento com os dados do usuário
                if (!documentSnapshot.exists()) {
                    val perfilData = hashMapOf(
                        "tipo" to if (user.isAnonymous) "visitante" else "logado",
                        "nome" to (user.displayName ?: user.email?.substringBefore("@") ?: "Usuário"),
                        "criadoEm" to FieldValue.serverTimestamp()
                    )
                    userDoc.set(perfilData)
                }
            }

            //Usuário já está logado, redirecionar para a tela principal
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Usuário não está logado, exibir a tela de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}