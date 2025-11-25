package douglas.oliveira.diariojogadorapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import douglas.oliveira.diariojogadorapp.models.AuthResposta
import douglas.oliveira.diariojogadorapp.models.LoginCreds
import douglas.oliveira.diariojogadorapp.retrofit.ClientRetrofit
import douglas.oliveira.diariojogadorapp.services.DiarioService
import douglas.oliveira.diariojogadorapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Tela de Login.
 * Responsável pela autenticação do usuário via API.
 * Gerencia token, sessão e feedback visual de carregamento.
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // --- Verificação de Sessão Existente ---
        // Antes de tudo, verifica se o usuário já tem um token salvo.
        // Se tiver, pula o login e vai direto para a MainActivity.
        if (SessionManager.getToken(this) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Fecha o Login para o usuário não voltar aqui ao apertar "Voltar"
        }

        // Inicialização dos Componentes Visuais (Views)
        val edtEmail = findViewById<EditText>(R.id.edtEmailLogin)
        val edtSenha = findViewById<EditText>(R.id.edtSenhaLogin)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val txtIrParaCadastro = findViewById<TextView>(R.id.txtIrParaCadastro)

        // ProgressBar: Inicia invisível (definido no XML ou por padrão)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarLogin)

        // --- Configuração do Botão Entrar ---
        btnEntrar.setOnClickListener {
            val email = edtEmail.text.toString()
            val senha = edtSenha.text.toString()

            // 1. Validação Local: Garante que não enviamos dados vazios para a API
            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Para a execução aqui
            }

            // Prepara os dados para envio
            val creds = LoginCreds(email, senha)
            val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

            // 2. [UX/UI] Início do Carregamento
            // Mostra a barra de progresso, desabilita o botão e muda o texto
            // Evita que o usuário clique várias vezes e mostra que algo está acontecendo
            progressBar.visibility = View.VISIBLE
            btnEntrar.isEnabled = false
            btnEntrar.text = "Carregando..."

            // 3. Chamada à API (POST /auth/login)
            service.logar(creds).enqueue(object : Callback<AuthResposta> {

                // Resposta do Servidor (Seja sucesso ou erro de credencial)
                override fun onResponse(call: Call<AuthResposta>, response: Response<AuthResposta>) {
                    // [UX/UI] Fim do Carregamento: Restaura a tela
                    progressBar.visibility = View.GONE
                    btnEntrar.isEnabled = true
                    btnEntrar.text = "ENTRAR"

                    if (response.isSuccessful) {
                        // Login Sucesso (HTTP 200)
                        val authResponse = response.body()!!

                        // --- CRUCIAL: SALVAMENTO DE SESSÃO ---
                        // Salva todos os dados necessários para o funcionamento do app
                        SessionManager.saveToken(this@LoginActivity, authResponse.token)
                        SessionManager.saveName(this@LoginActivity, authResponse.user.nome)
                        // Salva o ID para vincular com o SQLite
                        SessionManager.saveId(this@LoginActivity, authResponse.user.id ?: "")
                        // Salva o E-mail para exibir no Perfil
                        SessionManager.saveEmail(this@LoginActivity, authResponse.user.email)

                        // Navega para a tela principal
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        // Erro de Credencial (HTTP 401, 400, etc.)
                        Toast.makeText(this@LoginActivity, "Login falhou! Verifique email/senha.", Toast.LENGTH_SHORT).show()
                    }
                }

                // Falha de Rede (Sem internet, servidor fora do ar)
                override fun onFailure(call: Call<AuthResposta>, t: Throwable) {
                    // [UX/UI] Fim do Carregamento também no erro
                    progressBar.visibility = View.GONE
                    btnEntrar.isEnabled = true
                    btnEntrar.text = "ENTRAR"

                    Toast.makeText(this@LoginActivity, "Erro de conexão: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // --- Navegação para Cadastro ---
        // Leva o usuário para a tela de criar conta
        txtIrParaCadastro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}