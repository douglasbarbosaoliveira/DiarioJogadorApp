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

/**
 * Tela de Listagem de Jogos.
 * Responsável por buscar os dados na API e exibi-los em uma lista (RecyclerView).
 * Também gerencia o menu de contexto para Editar/Excluir.
 */
class ListaJogosActivity : AppCompatActivity() {

    // Componentes da UI
    private lateinit var recycler: RecyclerView

    // O Adapter conecta os dados (Lista de Jogos) com a visualização (XML do item)
    private lateinit var adapter: JogoAdapter

    // Lista local para armazenar os jogos baixados da API
    private var listaJogos: List<Jogo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_jogos)

        // --- Configuração do Cabeçalho ---
        // Localiza o TextView do cabeçalho (que veio via <include>) e define o nome do usuário
        val txtHeader = findViewById<TextView>(R.id.txtNomeHeader)
        val nomeUsuario = SessionManager.getName(this)
        txtHeader.text = "Olá, $nomeUsuario"

        // --- Configuração da RecyclerView ---
        recycler = findViewById(R.id.recyclerJogos)
        // Define que a lista será vertical (LinearLayoutManager)
        recycler.layoutManager = LinearLayoutManager(this)

        // Inicializa o adapter vazio para evitar erros antes dos dados chegarem
        adapter = JogoAdapter(listaJogos)
        recycler.adapter = adapter

        // --- Botão Flutuante (FAB) ---
        // Ao clicar, abre o formulário para criar um NOVO jogo
        val fab: FloatingActionButton = findViewById(R.id.fabAdicionarJogo)
        fab.setOnClickListener {
            val intent = Intent(this, FormJogoActivity::class.java)
            startActivity(intent)
        }

        // Habilita o Menu de Contexto (o menu que aparece ao segurar o clique no item)
        registerForContextMenu(recycler)
    }

    /**
     * O onResume é chamado sempre que a tela volta a ficar visível.
     * Usamos ele para recarregar a lista, garantindo que, se o usuário
     * adicionou ou editou um jogo e voltou, a lista esteja atualizada.
     */
    override fun onResume() {
        super.onResume()
        carregarJogos()
    }

    /**
     * Busca a lista de jogos na API (GET).
     */
    fun carregarJogos() {
        // Cria o cliente Retrofit com o Token de autenticação
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

        // Faz a chamada assíncrona para não travar o app
        service.buscarJogos().enqueue(object : Callback<List<Jogo>> {

            // Sucesso: O servidor respondeu
            override fun onResponse(call: Call<List<Jogo>>, response: Response<List<Jogo>>) {
                if (response.isSuccessful) {
                    // Pega a lista do corpo da resposta (ou lista vazia se for nulo)
                    listaJogos = response.body() ?: emptyList()

                    // Cria um novo adapter com os dados novos e atualiza a tela
                    adapter = JogoAdapter(listaJogos)
                    recycler.adapter = adapter
                } else {
                    Toast.makeText(this@ListaJogosActivity, "Falha ao carregar jogos: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            // Falha: Sem internet ou erro de conexão
            override fun onFailure(call: Call<List<Jogo>>, t: Throwable) {
                Toast.makeText(this@ListaJogosActivity, "Erro de rede: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Gerencia os cliques no Menu de Contexto (Editar/Excluir).
     * Esse menu é inflado dentro do Adapter, mas a lógica do clique fica aqui na Activity.
     */
    override fun onContextItemSelected(item: MenuItem): Boolean {
        // Verifica se a posição clicada é válida
        if (adapter.posicaoClicada == -1 || adapter.posicaoClicada >= listaJogos.size) {
            return super.onContextItemSelected(item)
        }

        // Recupera o objeto Jogo que foi clicado na lista
        val jogo = listaJogos[adapter.posicaoClicada]

        return when (item.itemId) {
            // Opção EDITAR
            R.id.menu_editar_jogo -> {
                // Cria um Intent para abrir o formulário
                val intent = Intent(this, FormJogoActivity::class.java).apply {
                    // Passa TODOS os dados do jogo para o formulário preencher os campos
                    putExtra("JOGO_ID", jogo.id)
                    putExtra("JOGO_ADVERSARIO", jogo.adversario)
                    putExtra("JOGO_TIPO", jogo.tipo)
                    putExtra("JOGO_RESULTADO", jogo.resultado)
                    putExtra("JOGO_GOLS", jogo.gols)
                    putExtra("JOGO_ASSISTENCIAS", jogo.assistencias)
                    putExtra("JOGO_NOTA", jogo.nota.toString())
                    putExtra("JOGO_SENSACAO", jogo.sensacao)
                    putExtra("JOGO_COMENTARIOS", jogo.comentarios)
                    putExtra("JOGO_DATA", jogo.data)
                }
                startActivity(intent)
                true
            }
            // Opção EXCLUIR
            R.id.menu_deletar_jogo -> {
                // Chama a função de deletar passando o ID do jogo
                deletarJogo(jogo.id ?: "")
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    /**
     * Chama a API para deletar um jogo (DELETE).
     */
    private fun deletarJogo(id: String) {
        if (id.isEmpty()) return // Segurança: não tenta deletar se não tiver ID

        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

        service.deleteJogo(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ListaJogosActivity, "Jogo excluído com sucesso!", Toast.LENGTH_SHORT).show()
                    carregarJogos() // Recarrega a lista para sumir com o item excluído
                } else {
                    Toast.makeText(this@ListaJogosActivity, "Falha ao excluir jogo. Código: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ListaJogosActivity, "Erro de rede.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}