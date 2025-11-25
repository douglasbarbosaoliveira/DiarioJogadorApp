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

/**
 * Activity responsável pelo Formulário de TREINOS.
 * Gerencia o Cadastro (Create) e a Edição (Update) de treinos na API.
 */
class FormTreinoActivity : AppCompatActivity() {

    // Variável de controle: Se nulo, é um treino novo. Se tiver ID, é edição.
    private var treinoId: String? = null

    // Declaração dos componentes da interface (Views)
    private lateinit var edtData: EditText
    private lateinit var spnTipo: Spinner       // Dropdown para Tipo de Treino (Físico, Tático, etc)
    private lateinit var spnIntensidade: Spinner // Dropdown para Intensidade (Baixa, Alta, etc)
    private lateinit var edtDuracao: EditText
    private lateinit var edtSensacao: EditText // Cansaço (1-10)
    private lateinit var edtObservacoes: EditText

    // Instância do Calendário para manipular a seleção de data
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_treino)

        // --- Cabeçalho Personalizado ---
        // Exibe a saudação "Olá, [Nome]" no topo da tela
        val txtHeader = findViewById<TextView>(R.id.txtNomeHeader)
        val nomeUsuario = SessionManager.getName(this)
        txtHeader.text = "Olá, $nomeUsuario"

        // 1. Vincula variáveis aos IDs do XML
        inicializarComponentes()

        // 2. Preenche os Spinners com as opções de treino
        configurarSpinners()

        // 3. Configura o DatePicker no campo de data
        configurarData()

        // 4. Verifica se a tela abriu para EDIÇÃO (se veio dados da lista)
        initEditMode()

        // 5. Lógica do Botão Salvar
        findViewById<Button>(R.id.btnSalvarTreino).setOnClickListener {
            // Decide entre Cadastrar (novo) ou Atualizar (existente) baseado no ID
            if (treinoId == null) cadastrarTreino() else atualizarTreino(treinoId!!)
        }
    }

    // Vincula os componentes visuais
    private fun inicializarComponentes() {
        edtData = findViewById(R.id.edtDataTreino)
        spnTipo = findViewById(R.id.spnTipoTreino)
        spnIntensidade = findViewById(R.id.spnIntensidade)
        edtDuracao = findViewById(R.id.edtDuracao)
        edtSensacao = findViewById(R.id.edtSensacaoTreino)
        edtObservacoes = findViewById(R.id.edtObservacoes)
    }

    /**
     * Verifica se a Activity recebeu dados via Intent (Modo Edição).
     * Se sim, preenche os campos e seleciona os itens corretos nos Spinners.
     */
    private fun initEditMode() {
        // Recupera o ID enviado pela ListaTreinosActivity
        treinoId = intent.getStringExtra("TREINO_ID")

        if (treinoId != null) {
            // Preenche campos de texto
            edtData.setText(intent.getStringExtra("TREINO_DATA")?.take(10)) // Data YYYY-MM-DD
            // getIntExtra precisa de um valor padrão (0) caso falhe
            edtDuracao.setText(intent.getIntExtra("TREINO_DURACAO", 0).toString())
            edtSensacao.setText(intent.getIntExtra("TREINO_SENSACAO", 0).toString())
            edtObservacoes.setText(intent.getStringExtra("TREINO_OBS"))

            // Seleciona automaticamente a opção correta nos Spinners
            selecionarSpinner(spnTipo, intent.getStringExtra("TREINO_TIPO"))
            selecionarSpinner(spnIntensidade, intent.getStringExtra("TREINO_INTENSIDADE"))
        }
    }

    // Função auxiliar para selecionar item no Spinner pelo texto
    private fun selecionarSpinner(spinner: Spinner, valor: String?) {
        if (valor == null) return
        val adapter = spinner.adapter as ArrayAdapter<String>
        val position = adapter.getPosition(valor)
        if (position >= 0) {
            spinner.setSelection(position)
        }
    }

    /**
     * Configura o DatePicker (Calendário) para o campo de data.
     */
    private fun configurarData() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            // Formata para o padrão da API (AAAA-MM-DD)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            edtData.setText(sdf.format(calendar.time))
        }

        // Abre o calendário ao clicar no EditText
        edtData.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    /**
     * Preenche os Spinners com arrays de Strings definidos no código.
     */
    private fun configurarSpinners() {
        // Opções fixas para Tipo e Intensidade
        val tipos = arrayOf("Físico", "Tático", "Técnico", "Regenerativo", "Academia")
        val intensidades = arrayOf("Baixa", "Média", "Alta", "Muito Alta")

        // Cria e aplica os adapters
        spnTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spnIntensidade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intensidades).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    /**
     * Coleta os dados da tela e cria um objeto 'Treino' pronto para envio.
     */
    private fun createTreinoObject(id: String?): Treino {
        return Treino(
            id = id,
            data = edtData.text.toString(),
            tipo = spnTipo.selectedItem.toString(), // Pega do Spinner
            intensidade = spnIntensidade.selectedItem.toString(), // Pega do Spinner
            // Converte String para Int de forma segura (evita crash)
            duracaoMin = edtDuracao.text.toString().toIntOrNull() ?: 0,
            sensacao = edtSensacao.text.toString().toIntOrNull() ?: 0,
            observacoes = edtObservacoes.text.toString()
        )
    }

    /**
     * Envia um novo treino para a API (POST).
     */
    private fun cadastrarTreino() {
        val novoTreino = createTreinoObject(null) // ID é nulo
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)
        service.cadastrarTreino(novoTreino).enqueue(getCallback("Treino salvo com sucesso!"))
    }

    /**
     * Atualiza um treino existente na API (PUT).
     */
    private fun atualizarTreino(id: String) {
        val treinoAtualizado = createTreinoObject(id) // Usa o ID existente
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)
        service.atualizarTreino(id, treinoAtualizado).enqueue(getCallback("Treino atualizado com sucesso!"))
    }

    /**
     * Callback genérico para tratar a resposta da API (Sucesso/Erro).
     */
    private fun getCallback(msgSucesso: String): Callback<Treino> {
        return object : Callback<Treino> {
            override fun onResponse(call: Call<Treino>, response: Response<Treino>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@FormTreinoActivity, msgSucesso, Toast.LENGTH_SHORT).show()
                    finish() // Fecha a tela e volta para a lista
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