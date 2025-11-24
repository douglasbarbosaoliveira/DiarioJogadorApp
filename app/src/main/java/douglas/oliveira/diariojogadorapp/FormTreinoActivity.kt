package douglas.oliveira.diariojogadorapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import douglas.oliveira.diariojogadorapp.models.Treino
import douglas.oliveira.diariojogadorapp.retrofit.ClientRetrofit
import douglas.oliveira.diariojogadorapp.services.DiarioService
import douglas.oliveira.diariojogadorapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class FormTreinoActivity : AppCompatActivity() {

    private var treinoId: String? = null

    // Views
    private lateinit var edtData: EditText
    private lateinit var spnTipo: Spinner
    private lateinit var spnIntensidade: Spinner
    private lateinit var edtDuracao: EditText
    private lateinit var edtSensacao: EditText
    private lateinit var edtObservacoes: EditText

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_treino)

        val txtHeader = findViewById<TextView>(R.id.txtNomeHeader)
        val nomeUsuario = SessionManager.getName(this)
        txtHeader.text = "Olá, $nomeUsuario"

        inicializarComponentes()
        configurarSpinners()
        configurarData()

        // [NOVO] Verifica se veio dados para edição
        initEditMode()

        findViewById<Button>(R.id.btnSalvarTreino).setOnClickListener {
            if (treinoId == null) cadastrarTreino() else atualizarTreino(treinoId!!)
        }
    }

    private fun inicializarComponentes() {
        edtData = findViewById(R.id.edtDataTreino)
        spnTipo = findViewById(R.id.spnTipoTreino)
        spnIntensidade = findViewById(R.id.spnIntensidade)
        edtDuracao = findViewById(R.id.edtDuracao)
        edtSensacao = findViewById(R.id.edtSensacaoTreino)
        edtObservacoes = findViewById(R.id.edtObservacoes)
    }

    private fun initEditMode() {
        treinoId = intent.getStringExtra("TREINO_ID")
        if (treinoId != null) {
            // Preenche campos de texto
            edtData.setText(intent.getStringExtra("TREINO_DATA")?.take(10))
            edtDuracao.setText(intent.getIntExtra("TREINO_DURACAO", 0).toString())
            edtSensacao.setText(intent.getIntExtra("TREINO_SENSACAO", 0).toString())
            edtObservacoes.setText(intent.getStringExtra("TREINO_OBS"))

            // Preenche Spinners
            selecionarSpinner(spnTipo, intent.getStringExtra("TREINO_TIPO"))
            selecionarSpinner(spnIntensidade, intent.getStringExtra("TREINO_INTENSIDADE"))
        }
    }

    // Função auxiliar para selecionar item no Spinner
    private fun selecionarSpinner(spinner: Spinner, valor: String?) {
        if (valor == null) return
        val adapter = spinner.adapter as ArrayAdapter<String>
        val position = adapter.getPosition(valor)
        if (position >= 0) {
            spinner.setSelection(position)
        }
    }

    private fun configurarData() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            edtData.setText(sdf.format(calendar.time))
        }
        edtData.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun configurarSpinners() {
        val tipos = arrayOf("Físico", "Tático", "Técnico", "Regenerativo", "Academia")
        val intensidades = arrayOf("Baixa", "Média", "Alta", "Muito Alta")

        spnTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spnIntensidade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intensidades).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun createTreinoObject(id: String?): Treino {
        return Treino(
            id = id,
            data = edtData.text.toString(),
            tipo = spnTipo.selectedItem.toString(),
            intensidade = spnIntensidade.selectedItem.toString(),
            duracaoMin = edtDuracao.text.toString().toIntOrNull() ?: 0,
            sensacao = edtSensacao.text.toString().toIntOrNull() ?: 0,
            observacoes = edtObservacoes.text.toString()
        )
    }

    private fun cadastrarTreino() {
        val novoTreino = createTreinoObject(null)
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)
        service.cadastrarTreino(novoTreino).enqueue(getCallback("Treino Salvo!"))
    }

    // [NOVO] Função de Atualizar
    private fun atualizarTreino(id: String) {
        val treinoAtualizado = createTreinoObject(id)
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)
        service.atualizarTreino(id, treinoAtualizado).enqueue(getCallback("Treino Atualizado!"))
    }

    // Callback genérico para evitar repetição de código
    private fun getCallback(msgSucesso: String): Callback<Treino> {
        return object : Callback<Treino> {
            override fun onResponse(call: Call<Treino>, response: Response<Treino>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@FormTreinoActivity, msgSucesso, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@FormTreinoActivity, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Treino>, t: Throwable) {
                Toast.makeText(this@FormTreinoActivity, "Erro de rede", Toast.LENGTH_SHORT).show()
            }
        }
    }
}