package douglas.oliveira.diariojogadorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import douglas.oliveira.diariojogadorapp.utils.SessionManager

/**
 * Tela Principal (Dashboard).
 * É a primeira tela que o usuário vê após o login.
 * Centraliza o acesso a todas as funcionalidades do app.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- 1. Configuração do Cabeçalho Personalizado ---

        // Localiza o TextView do nome.
        // Nota: Como usamos a tag <include> no XML, o ID 'txtNomeHeader'
        // faz parte da hierarquia de views desta Activity e pode ser encontrado diretamente.
        val txtUsuario = findViewById<TextView>(R.id.txtNomeHeader)

        // Recupera o nome salvo no SharedPreferences durante o Login
        val nomeUsuario = SessionManager.getName(this)

        // Define o texto de boas-vindas
        txtUsuario.text = "Olá, $nomeUsuario"


        // --- 2. Inicialização dos Botões de Navegação ---

        // Botões de Imagem (ImageButton) para as funcionalidades principais
        val btnJogos = findViewById<ImageButton>(R.id.btnVerJogos)
        val btnTreinos = findViewById<ImageButton>(R.id.btnVerTreinos)

        // Botões de Texto (Button) para ações secundárias
        val btnPerfil = findViewById<Button>(R.id.btnMeuPerfil)
        val btnSair = findViewById<Button>(R.id.btnSair)


        // --- 3. Definição das Ações de Clique (Listeners) ---

        // Navega para a tela de Perfil (SQLite + Dados Pessoais)
        btnPerfil.setOnClickListener {
            // Intent é a "intenção" de sair de uma tela (this) para outra (class.java)
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        // Navega para a lista de Jogos (API)
        btnJogos.setOnClickListener {
            startActivity(Intent(this, ListaJogosActivity::class.java))
        }

        // Navega para a lista de Treinos (API)
        btnTreinos.setOnClickListener {
            startActivity(Intent(this, ListaTreinosActivity::class.java))
        }

        // --- 4. Lógica de Logout (Sair) ---
        btnSair.setOnClickListener {
            // Passo A: Limpa todos os dados salvos (Token, Nome, ID, Email)
            // Garante que, ao reabrir o app, ele peça login novamente.
            SessionManager.clearData(this)

            // Passo B: Redireciona para a tela de Login
            startActivity(Intent(this, LoginActivity::class.java))

            // Passo C: Encerra a MainActivity.
            // Impede que o usuário aperte o botão "Voltar" do celular e retorne para cá sem estar logado.
            finish()
        }
    }
}