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
import douglas.oliveira.diariojogadorapp.adapters.TreinoAdapter
import douglas.oliveira.diariojogadorapp.models.Treino
import douglas.oliveira.diariojogadorapp.retrofit.ClientRetrofit
import douglas.oliveira.diariojogadorapp.services.DiarioService
import douglas.oliveira.diariojogadorapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity responsável por listar os Treinos.
 * Funcionalidades: Listagem (GET), Navegação para Cadastro, Menu de Edição/Exclusão.
 */
class ListaTreinosActivity : AppCompatActivity() {

    // Componentes visuais e de dados
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TreinoAdapter
    private var listaTreinos: List<Treino> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_treinos)

        // --- Configuração do Cabeçalho (Header) ---
        // Busca o TextView que está dentro do layout incluído (<include layout="layout_header">)
        val txtHeader = findViewById<TextView>(R.id.txtNomeHeader)
        val nomeUsuario = SessionManager.getName(this)
        txtHeader.text = "Olá, $nomeUsuario"

        // --- Configuração da Lista (RecyclerView) ---
        recycler = findViewById(R.id.recyclerTreinos)
        recycler.layoutManager = LinearLayoutManager(this) // Lista vertical padrão

        // --- Botão Flutuante (FAB) ---
        findViewById<FloatingActionButton>(R.id.fabAdicionarTreino).setOnClickListener {
            // Abre o formulário limpo para criar um novo treino
            startActivity(Intent(this, FormTreinoActivity::class.java))
        }

        // IMPORTANTE: Registra a RecyclerView para aceitar menus de contexto (Long Click)
        // Sem isso, o método onContextItemSelected não seria chamado corretamente.
        registerForContextMenu(recycler)
    }

    /**
     * onResume é chamado quando a Activity volta a ficar visível.
     * Ideal para recarregar a lista caso o usuário tenha adicionado/editado um treino e voltado.
     */
    override fun onResume() {
        super.onResume()
        carregarTreinos()
    }

    /**
     * Busca a lista de treinos na API (GET /treinos).
     */
    private fun carregarTreinos() {
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

        // Chamada Assíncrona
        service.buscarTreinos().enqueue(object : Callback<List<Treino>> {
            override fun onResponse(call: Call<List<Treino>>, response: Response<List<Treino>>) {
                if (response.isSuccessful) {
                    // Atualiza a lista local e o Adapter
                    listaTreinos = response.body() ?: emptyList()
                    adapter = TreinoAdapter(listaTreinos)
                    recycler.adapter = adapter
                } else {
                    Toast.makeText(this@ListaTreinosActivity, "Falha ao carregar treinos.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Treino>>, t: Throwable) {
                Toast.makeText(this@ListaTreinosActivity, "Erro de rede.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Gerencia o clique nas opções do Menu de Contexto (Editar/Excluir).
     * Esse menu aparece quando o usuário segura o dedo sobre um item da lista.
     */
    override fun onContextItemSelected(item: MenuItem): Boolean {
        // Validação de segurança: verifica se a posição clicada é válida
        if (adapter.posicaoClicada == -1 || adapter.posicaoClicada >= listaTreinos.size) {
            return super.onContextItemSelected(item)
        }

        // Recupera o objeto Treino específico que foi clicado
        val treino = listaTreinos[adapter.posicaoClicada]

        return when (item.itemId) {
            // Caso: EDITAR
            R.id.menu_editar_treino -> {
                // Cria um Intent e coloca TODOS os dados do treino nele (Extras)
                // Isso permite que o formulário já abra preenchido
                val intent = Intent(this, FormTreinoActivity::class.java).apply {
                    putExtra("TREINO_ID", treino.id)
                    putExtra("TREINO_DATA", treino.data)
                    putExtra("TREINO_TIPO", treino.tipo)
                    putExtra("TREINO_INTENSIDADE", treino.intensidade)
                    putExtra("TREINO_DURACAO", treino.duracaoMin)
                    putExtra("TREINO_SENSACAO", treino.sensacao)
                    putExtra("TREINO_OBS", treino.observacoes)
                }
                startActivity(intent)
                true
            }
            // Caso: EXCLUIR
            R.id.menu_deletar_treino -> {
                deletarTreino(treino.id ?: "")
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    /**
     * Chama a API para deletar um treino (DELETE /treinos/{id}).
     */
    private fun deletarTreino(id: String) {
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

        service.deleteTreino(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ListaTreinosActivity, "Treino excluído com sucesso!", Toast.LENGTH_SHORT).show()
                    carregarTreinos() // Recarrega a lista para remover o item visualmente
                } else {
                    Toast.makeText(this@ListaTreinosActivity, "Falha ao excluir treino.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ListaTreinosActivity, "Erro de rede.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}