package douglas.oliveira.diariojogadorapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns // Utilitário nativo do Android para validar padrões (como e-mail)
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import douglas.oliveira.diariojogadorapp.models.AuthResposta
import douglas.oliveira.diariojogadorapp.models.Usuario
import douglas.oliveira.diariojogadorapp.retrofit.ClientRetrofit
import douglas.oliveira.diariojogadorapp.services.DiarioService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Tela de Cadastro de Usuário.
 * Responsável por coletar dados, validar regras de negócio e enviar o registro para a API.
 */
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // --- Inicialização dos Componentes Visuais ---
        val edtNome = findViewById<EditText>(R.id.edtNomeCadastro)
        val edtEmail = findViewById<EditText>(R.id.edtEmailCadastro)
        val edtSenha = findViewById<EditText>(R.id.edtSenhaCadastro)
        val edtConfirmSenha = findViewById<EditText>(R.id.edtConfirmSenhaCadastro)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)

        // --- Ação do Botão Cadastrar ---
        btnCadastrar.setOnClickListener {

            // Coleta os textos digitados pelo usuário
            val nome = edtNome.text.toString()
            val email = edtEmail.text.toString()
            val senha = edtSenha.text.toString()
            val confirmSenha = edtConfirmSenha.text.toString()

            // --- ETAPA 1: Validações Locais (Antes de chamar a API) ---

            // 1. Verifica se algum campo está vazio
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Interrompe a execução aqui se falhar
            }

            // 2. Valida se o texto digitado é realmente um e-mail (ex: algo@algo.com)
            // Patterns.EMAIL_ADDRESS é uma expressão regular (Regex) nativa do Android
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.error = "Digite um e-mail válido!" // Mostra ícone de erro no campo
                edtEmail.requestFocus() // Coloca o foco/cursor no campo com erro
                return@setOnClickListener
            }

            // 3. Verifica se a senha e a confirmação são idênticas
            if (senha != confirmSenha) {
                edtConfirmSenha.error = "As senhas não conferem!"
                edtConfirmSenha.requestFocus()
                return@setOnClickListener
            }

            // --- ETAPA 2: Preparação dos Dados ---

            // Se passou por todas as validações, cria o objeto Usuario
            // Note que não enviamos a 'confirmSenha' para a API, ela serviu apenas para validação visual
            val novoUsuario = Usuario(
                nome = nome,
                email = email,
                senha = senha
            )

            // --- ETAPA 3: Comunicação com a API ---

            // Cria a instância do serviço Retrofit
            val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

            // Faz a chamada assíncrona (enqueue) para registrar o usuário
            service.registrar(novoUsuario).enqueue(object : Callback<AuthResposta> {

                // Resposta do servidor recebida
                override fun onResponse(call: Call<AuthResposta>, response: Response<AuthResposta>) {
                    if (response.isSuccessful) {
                        // Sucesso (HTTP 200/201)
                        Toast.makeText(this@RegisterActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show()

                        // Encerra a tela de cadastro e volta para a anterior (provavelmente LoginActivity)
                        finish()
                    } else {
                        // Erro de negócio (ex: E-mail já cadastrado no banco de dados)
                        Toast.makeText(this@RegisterActivity, "Erro ao cadastrar usuário.", Toast.LENGTH_SHORT).show()
                    }
                }

                // Falha técnica na conexão
                override fun onFailure(call: Call<AuthResposta>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Erro de conexão", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}