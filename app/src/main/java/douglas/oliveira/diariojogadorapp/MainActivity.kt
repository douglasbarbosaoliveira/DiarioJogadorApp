package douglas.oliveira.diariojogadorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import douglas.oliveira.diariojogadorapp.utils.SessionManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Armazena o nome do usuário
        val txtUsuario = findViewById<TextView>(R.id.txtNomeHeader)

        val nomeUsuario = SessionManager.getName(this)
        txtUsuario.text = "Olá, $nomeUsuario"

        // Botões com imagem
        val btnJogos = findViewById<ImageButton>(R.id.btnVerJogos)
        val btnTreinos = findViewById<ImageButton>(R.id.btnVerTreinos)

        // No onCreate...
        val btnPerfil = findViewById<Button>(R.id.btnMeuPerfil)
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        // Botão sair
        val btnSair = findViewById<Button>(R.id.btnSair)

        btnJogos.setOnClickListener {
            startActivity(Intent(this, ListaJogosActivity::class.java))
        }

        btnTreinos.setOnClickListener {
            startActivity(Intent(this, ListaTreinosActivity::class.java))
        }

        btnSair.setOnClickListener {
            SessionManager.clearData(this) // Limpa token E nome
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}