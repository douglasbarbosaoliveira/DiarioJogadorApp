package douglas.oliveira.diariojogadorapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import douglas.oliveira.diariojogadorapp.adapters.JogoAdapter
import douglas.oliveira.diariojogadorapp.models.Jogo
import douglas.oliveira.diariojogadorapp.retrofit.ClientRetrofit
import douglas.oliveira.diariojogadorapp.services.DiarioService
import douglas.oliveira.diariojogadorapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListaJogosActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: JogoAdapter
    private var listaJogos: List<Jogo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_jogos) // Supondo o nome do seu layout

        // Localiza o TextView que está dentro do <include layout="@layout/layout_header" />
        val txtHeader = findViewById<TextView>(R.id.txtNomeHeader)

        // Recupera o nome salvo no login
        val nomeUsuario = SessionManager.getName(this)

        // Define o texto (Ex: "Olá, Douglas")
        txtHeader.text = "Olá, $nomeUsuario"

        recycler = findViewById(R.id.recyclerJogos) // Supondo o ID do RecyclerView
        recycler.layoutManager = LinearLayoutManager(this)

        // Inicializa o adapter com uma lista vazia, será preenchida em onResume
        adapter = JogoAdapter(listaJogos)
        recycler.adapter = adapter

        val fab: FloatingActionButton = findViewById(R.id.fabAdicionarJogo) // Supondo o ID do FAB
        fab.setOnClickListener {
            val intent = Intent(this, FormJogoActivity::class.java)
            startActivity(intent)
        }

        // Registra o RecyclerView para receber o Context Menu
        registerForContextMenu(recycler)
    }

    override fun onResume() {
        super.onResume()
        carregarJogos()
    }

    // A função carregarJogos DEVE estar na classe, e NÃO aninhada em outro lugar.
    fun carregarJogos() {
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

        // Aqui dentro, usamos um 'object : Callback', que É um objeto anônimo.
        service.buscarJogos().enqueue(object : Callback<List<Jogo>> {

            // O override DEVE estar aqui dentro do objeto anônimo, NÃO fora dele.
            override fun onResponse(call: Call<List<Jogo>>, response: Response<List<Jogo>>) {
                if (response.isSuccessful) {
                    listaJogos = response.body() ?: emptyList() // Salva a lista aqui

                    // IMPORTANTE: Se a lista de jogos mudar, você precisa criar um novo adapter
                    // OU notificar o adapter atual que os dados mudaram.
                    // A melhor prática para grandes alterações é criar um novo adapter,
                    // mas notifyDataSetChanged() também funciona (porém menos eficiente).
                    adapter = JogoAdapter(listaJogos)
                    recycler.adapter = adapter
                } else {
                    Toast.makeText(this@ListaJogosActivity, "Falha ao carregar jogos: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Jogo>>, t: Throwable) {
                Toast.makeText(this@ListaJogosActivity, "Erro de rede: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- Lógica do CRUD ---
    // Este método está CORRETO e é implementado na Activity.
    override fun onContextItemSelected(item: MenuItem): Boolean {
        // Pega o Jogo que foi clicado
        if (adapter.posicaoClicada == -1 || adapter.posicaoClicada >= listaJogos.size) {
            return super.onContextItemSelected(item)
        }
        val jogo = listaJogos[adapter.posicaoClicada]

        return when (item.itemId) {
            R.id.menu_editar_jogo -> {
                // Navega para o formulário no modo edição, passando todos os dados
                val intent = Intent(this, FormJogoActivity::class.java).apply {
                    putExtra("JOGO_ID", jogo.id)
                    putExtra("JOGO_ADVERSARIO", jogo.adversario)
                    putExtra("JOGO_TIPO", jogo.tipo)
                    putExtra("JOGO_RESULTADO", jogo.resultado)
                    putExtra("JOGO_GOLS", jogo.gols)
                    putExtra("JOGO_ASSISTENCIAS", jogo.assistencias)
                    putExtra("JOGO_NOTA", jogo.nota.toString())
                    putExtra("JOGO_SENSACAO", jogo.sensacao)
                    putExtra("JOGO_COMENTARIOS", jogo.comentarios)

                    // A data é crucial para edição e deve ser passada no formato original (ex: YYYY-MM-DD)
                    putExtra("JOGO_DATA", jogo.data)
                }
                startActivity(intent)
                true
            }
            R.id.menu_deletar_jogo -> {
                deletarJogo(jogo.id ?: "")
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun deletarJogo(id: String) {
        if (id.isEmpty()) return
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)
        service.deleteJogo(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ListaJogosActivity, "Jogo excluído com sucesso!", Toast.LENGTH_SHORT).show()
                    carregarJogos() // Recarrega a lista
                } else {
                    Toast.makeText(this@ListaJogosActivity, "Falha ao excluir. Código: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ListaJogosActivity, "Erro de rede ao deletar.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}