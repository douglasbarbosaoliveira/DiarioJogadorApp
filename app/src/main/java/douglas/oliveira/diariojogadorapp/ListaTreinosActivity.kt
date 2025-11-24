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

class ListaTreinosActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TreinoAdapter
    private var listaTreinos: List<Treino> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_treinos)

        // Localiza o TextView que está dentro do <include layout="@layout/layout_header" />
        val txtHeader = findViewById<TextView>(R.id.txtNomeHeader)

        // Recupera o nome salvo no login
        val nomeUsuario = SessionManager.getName(this)

        // Define o texto (Ex: "Olá, Douglas")
        txtHeader.text = "Olá, $nomeUsuario"

        recycler = findViewById(R.id.recyclerTreinos)
        recycler.layoutManager = LinearLayoutManager(this)

        findViewById<FloatingActionButton>(R.id.fabAdicionarTreino).setOnClickListener {
            startActivity(Intent(this, FormTreinoActivity::class.java))
        }

        registerForContextMenu(recycler) // ESSENCIAL PARA O DELETE FUNCIONAR
    }

    override fun onResume() {
        super.onResume()
        carregarTreinos()
    }

    private fun carregarTreinos() {
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)
        service.buscarTreinos().enqueue(object : Callback<List<Treino>> {
            override fun onResponse(call: Call<List<Treino>>, response: Response<List<Treino>>) {
                if (response.isSuccessful) {
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

    // Lógica para o Menu de Contexto (Delete)
    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (adapter.posicaoClicada == -1 || adapter.posicaoClicada >= listaTreinos.size) {
            return super.onContextItemSelected(item)
        }

        val treino = listaTreinos[adapter.posicaoClicada]

        return when (item.itemId) {
            R.id.menu_editar_treino -> {
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
            R.id.menu_deletar_treino -> {
                deletarTreino(treino.id ?: "")
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun deletarTreino(id: String) {
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

        // 1. service.deleteTreino(id).enqueue(object : Callback<Void> {...
        service.deleteTreino(id).enqueue(object : Callback<Void> {

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ListaTreinosActivity, "Treino excluído!", Toast.LENGTH_SHORT).show()
                    carregarTreinos() // Recarrega a lista
                } else {
                    Toast.makeText(this@ListaTreinosActivity, "Falha ao excluir.", Toast.LENGTH_SHORT).show()
                }
            } // <--- FECHA O onResponse

            // 2. IMPLEMENTAÇÃO OBRIGATÓRIA: onFailure
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ListaTreinosActivity, "Erro de rede ao deletar.", Toast.LENGTH_SHORT).show()
            }

        }) // <--- FECHA O OBJETO CALLBACK E O MÉTODO ENQUEUE
    } // <--- FECHA A FUNÇÃO deletarTreino
}
