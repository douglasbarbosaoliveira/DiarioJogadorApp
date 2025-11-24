package douglas.oliveira.diariojogadorapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns // IMPORTANTE: Certifique-se de que este import esteja aqui
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

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val edtNome = findViewById<EditText>(R.id.edtNomeCadastro)
        val edtEmail = findViewById<EditText>(R.id.edtEmailCadastro)
        val edtSenha = findViewById<EditText>(R.id.edtSenhaCadastro)
        val edtConfirmSenha = findViewById<EditText>(R.id.edtConfirmSenhaCadastro)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener {
            val nome = edtNome.text.toString()
            val email = edtEmail.text.toString()
            val senha = edtSenha.text.toString()
            val confirmSenha = edtConfirmSenha.text.toString()

            // 1. Validação de campos vazios
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. [NOVO] VALIDAÇÃO DE E-MAIL
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.error = "Digite um e-mail válido!"
                edtEmail.requestFocus()
                return@setOnClickListener
            }

            // 3. Validação de senha igual
            if (senha != confirmSenha) {
                edtConfirmSenha.error = "As senhas não conferem!"
                edtConfirmSenha.requestFocus()
                return@setOnClickListener
            }

            // Se passou, cria o objeto
            val novoUsuario = Usuario(
                nome = nome,
                email = email,
                senha = senha
            )

            // Chama a API
            val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

            service.registrar(novoUsuario).enqueue(object : Callback<AuthResposta> {
                override fun onResponse(call: Call<AuthResposta>, response: Response<AuthResposta>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Cadastro realizado!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Erro ao cadastrar.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<AuthResposta>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Erro de conexão", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}