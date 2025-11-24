package douglas.oliveira.diariojogadorapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import douglas.oliveira.diariojogadorapp.models.Jogo
import douglas.oliveira.diariojogadorapp.retrofit.ClientRetrofit
import douglas.oliveira.diariojogadorapp.services.DiarioService
import douglas.oliveira.diariojogadorapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class FormJogoActivity : AppCompatActivity() {

    private var jogoId: String? = null

    // Views
    private lateinit var edtData: EditText
    private lateinit var edtAdversario: EditText
    private lateinit var spnTipo: Spinner
    private lateinit var spnResultado: Spinner
    private lateinit var spnGols: Spinner
    private lateinit var spnAssist: Spinner
    private lateinit var edtNota: EditText
    private lateinit var edtSensacao: EditText
    private lateinit var edtComentarios: EditText

    // Calendar para a data
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_jogo)

        // Header
        val txtHeader = findViewById<TextView>(R.id.txtNomeHeader)
        val nomeUsuario = SessionManager.getName(this)
        txtHeader.text = "Olá, $nomeUsuario"

        // Inicializar componentes
        inicializarComponentes()

        // Configurar Spinners (Listas)
        configurarSpinners()

        // Configurar DatePicker (Calendário)
        configurarData()

        // Verificar Modo Edição
        initEditMode()

        findViewById<Button>(R.id.btnSalvarJogo).setOnClickListener {
            if (jogoId == null) cadastrarJogo() else atualizarJogo(jogoId!!)
        }
    }

    private fun inicializarComponentes() {
        edtData = findViewById(R.id.edtData)
        edtAdversario = findViewById(R.id.edtAdversario)
        spnTipo = findViewById(R.id.spnTipo)
        spnResultado = findViewById(R.id.spnResultado)
        spnGols = findViewById(R.id.spnGols)
        spnAssist = findViewById(R.id.spnAssist)
        edtNota = findViewById(R.id.edtNota)
        edtSensacao = findViewById(R.id.edtSensacao)
        edtComentarios = findViewById(R.id.edtComentarios)
    }

    private fun configuringData() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            atualizarLabelData()
        }

        edtData.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    // Correção do nome da função chamada acima
    private fun configurarData() = configuringData()

    private fun atualizarLabelData() {
        // Formato compatível com a API e banco de dados padrão
        val formato = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(formato, Locale.US)
        edtData.setText(sdf.format(calendar.time))
    }

    private fun configurarSpinners() {
        // Configura listas
        val tipos = arrayOf("Jogo oficial", "Amistoso", "Partida Beneficente")
        val resultados = arrayOf("Vitória", "Empate", "Derrota")
        // Cria lista de 0 a 20 para gols/assistências
        val numeros = (0..20).map { it.toString() }.toTypedArray()

        // Cria Adapters (estilo padrão do android)
        val adapterTipo = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val adapterRes = ArrayAdapter(this, android.R.layout.simple_spinner_item, resultados)
        adapterRes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val adapterNum = ArrayAdapter(this, android.R.layout.simple_spinner_item, numeros)
        adapterNum.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Aplica aos Spinners
        spnTipo.adapter = adapterTipo
        spnResultado.adapter = adapterRes
        spnGols.adapter = adapterNum
        spnAssist.adapter = adapterNum
    }

    private fun initEditMode() {
        jogoId = intent.getStringExtra("JOGO_ID")
        if (jogoId != null) {
            // Preenche Campos de Texto
            edtData.setText(intent.getStringExtra("JOGO_DATA")?.take(10))
            edtAdversario.setText(intent.getStringExtra("JOGO_ADVERSARIO"))
            edtNota.setText(intent.getStringExtra("JOGO_NOTA"))
            edtSensacao.setText(intent.getIntExtra("JOGO_SENSACAO", 0).toString())
            edtComentarios.setText(intent.getStringExtra("JOGO_COMENTARIOS"))

            // Preenche Spinners (Seleciona o item correto)
            selecionarSpinner(spnTipo, intent.getStringExtra("JOGO_TIPO"))
            selecionarSpinner(spnResultado, intent.getStringExtra("JOGO_RESULTADO"))

            // Para inteiros passados no intent
            val gols = intent.getIntExtra("JOGO_GOLS", 0).toString()
            selecionarSpinner(spnGols, gols)

            val assist = intent.getIntExtra("JOGO_ASSISTENCIAS", 0).toString()
            selecionarSpinner(spnAssist, assist)
        }
    }

    // Função auxiliar para encontrar o texto na lista do Spinner e marcar como selecionado
    private fun selecionarSpinner(spinner: Spinner, valor: String?) {
        if (valor == null) return
        val adapter = spinner.adapter as ArrayAdapter<String>
        val position = adapter.getPosition(valor)
        if (position >= 0) {
            spinner.setSelection(position)
        }
    }

    private fun createJogoObject(id: String?): Jogo {
        return Jogo(
            id = id,
            data = edtData.text.toString(),
            adversario = edtAdversario.text.toString(),
            // Pega o valor selecionado no Spinner
            tipo = spnTipo.selectedItem.toString(),
            resultado = spnResultado.selectedItem.toString(),
            gols = spnGols.selectedItem.toString().toInt(),
            assistencias = spnAssist.selectedItem.toString().toInt(),
            nota = edtNota.text.toString().toDoubleOrNull() ?: 0.0,
            sensacao = edtSensacao.text.toString().toIntOrNull() ?: 0,
            comentarios = edtComentarios.text.toString()
        )
    }

    private fun cadastrarJogo() {
        val novoJogo = createJogoObject(null)
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)
        service.cadastrarJogo(novoJogo).enqueue(getCallback(this, "Jogo Salvo!"))
    }

    private fun atualizarJogo(id: String) {
        val jogoAtualizado = createJogoObject(id)
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)
        service.atualizarJogo(id, jogoAtualizado).enqueue(getCallback(this, "Jogo Atualizado!"))
    }

    private fun getCallback(context: AppCompatActivity, msg: String): Callback<Jogo> {
        return object : Callback<Jogo> {
            override fun onResponse(call: Call<Jogo>, response: Response<Jogo>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    context.finish()
                } else {
                    Toast.makeText(context, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Jogo>, t: Throwable) {
                Toast.makeText(context, "Erro de rede", Toast.LENGTH_SHORT).show()
            }
        }
    }
}