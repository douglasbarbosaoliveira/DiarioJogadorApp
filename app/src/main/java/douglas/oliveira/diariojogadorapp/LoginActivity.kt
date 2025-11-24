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

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Verifica se já está logado
        if (SessionManager.getToken(this) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Inicialização dos Componentes
        val edtEmail = findViewById<EditText>(R.id.edtEmailLogin)
        val edtSenha = findViewById<EditText>(R.id.edtSenhaLogin)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val txtIrParaCadastro = findViewById<TextView>(R.id.txtIrParaCadastro)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarLogin) // Certifique-se que este ID existe no XML

        // --- Lógica de Login ---
        btnEntrar.setOnClickListener {
            val email = edtEmail.text.toString()
            val senha = edtSenha.text.toString()

            // Validação simples antes de chamar a API
            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val creds = LoginCreds(email, senha)
            val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

            // [UI] Inicia o estado de Carregamento
            progressBar.visibility = View.VISIBLE
            btnEntrar.isEnabled = false
            btnEntrar.text = "Carregando..."

            service.logar(creds).enqueue(object : Callback<AuthResposta> {
                override fun onResponse(call: Call<AuthResposta>, response: Response<AuthResposta>) {
                    // [UI] Restaura o estado original (independente de sucesso ou erro)
                    progressBar.visibility = View.GONE
                    btnEntrar.isEnabled = true
                    btnEntrar.text = "ENTRAR"

                    if (response.isSuccessful) {
                        val authResponse = response.body()!!

                        // Salva Token e Nome
                        SessionManager.saveToken(this@LoginActivity, authResponse.token)
                        SessionManager.saveName(this@LoginActivity, authResponse.user.nome)
                        SessionManager.saveId(this@LoginActivity, authResponse.user.id ?: "")
                        SessionManager.saveEmail(this@LoginActivity, authResponse.user.email)

                        // Navega para o App
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login falhou! Verifique email/senha.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResposta>, t: Throwable) {
                    // [UI] Restaura o estado original em caso de falha de rede
                    progressBar.visibility = View.GONE
                    btnEntrar.isEnabled = true
                    btnEntrar.text = "ENTRAR"

                    Toast.makeText(this@LoginActivity, "Erro de conexão: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // --- Navegação para Cadastro ---
        txtIrParaCadastro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}