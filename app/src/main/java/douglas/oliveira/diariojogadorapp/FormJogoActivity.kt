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

/**
 * Activity responsável pelo Formulário de Jogos.
 * Ela gerencia tanto o CADASTRO (Create) quanto a EDIÇÃO (Update).
 */
class FormJogoActivity : AppCompatActivity() {

    // Variável de controle: Se for nula, é um cadastro novo. Se tiver valor, é uma edição.
    private var jogoId: String? = null

    // Declaração dos componentes da interface (Views)
    private lateinit var edtData: EditText
    private lateinit var edtAdversario: EditText
    private lateinit var spnTipo: Spinner       // Dropdown para Tipo
    private lateinit var spnResultado: Spinner  // Dropdown para Resultado
    private lateinit var spnGols: Spinner       // Dropdown para Gols
    private lateinit var spnAssist: Spinner     // Dropdown para Assistências
    private lateinit var edtNota: EditText
    private lateinit var edtSensacao: EditText
    private lateinit var edtComentarios: EditText

    // Instância do Calendário para manipular a seleção de data
    private val calendar = Calendar.getInstance()

    /**
     * Método que roda ao abrir a tela.
     * Configura o layout, inicializa variáveis e define os cliques.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_jogo)

        // --- Configuração do Cabeçalho Personalizado ---
        // Busca o nome do usuário salvo na sessão (SharedPreferences) para exibir no topo
        val txtHeader = findViewById<TextView>(R.id.txtNomeHeader)
        val nomeUsuario = SessionManager.getName(this)
        txtHeader.text = "Olá, $nomeUsuario"

        // 1. Vincula as variáveis do Kotlin com os IDs do XML
        inicializarComponentes()

        // 2. Preenche as listas suspensas (Spinners) com as opções
        configurarSpinners()

        // 3. Configura o DatePicker para abrir ao clicar no campo de data
        configurarData()

        // 4. Verifica se a tela foi aberta para EDIÇÃO (se veio dados de outra tela)
        initEditMode()

        // 5. Configura o botão de Salvar
        findViewById<Button>(R.id.btnSalvarJogo).setOnClickListener {
            // Lógica condicional: Se não tem ID, cria novo. Se tem ID, atualiza o existente.
            if (jogoId == null) {
                cadastrarJogo()
            } else {
                atualizarJogo(jogoId!!)
            }
        }
    }

    // Vincula os componentes visuais
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

    /**
     * Configura a lógica do Calendário (DatePickerDialog).
     */
    private fun configuringData() {
        // Listener que escuta quando o usuário clica em "OK" no calendário
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            atualizarLabelData() // Atualiza o texto do campo visualmente
        }

        // Abre o calendário ao clicar no EditText
        edtData.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    // Atalho para chamar a função de configuração da data
    private fun configurarData() = configuringData()

    // Formata a data escolhida para o padrão aceito pela API (AAAA-MM-DD)
    private fun atualizarLabelData() {
        val formato = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(formato, Locale.US)
        edtData.setText(sdf.format(calendar.time))
    }

    /**
     * Cria os Adapters (adaptadores) para preencher os Spinners.
     * Um Adapter pega uma lista de dados (Array) e define como ela aparece na tela (Layout).
     */
    private fun configurarSpinners() {
        // Dados estáticos para as listas
        val tipos = arrayOf("Jogo oficial", "Amistoso", "Partida Beneficente")
        val resultados = arrayOf("Vitória", "Empate", "Derrota")

        // Cria uma lista dinâmica de números de "0" a "20" para gols/assistências
        val numeros = (0..20).map { it.toString() }.toTypedArray()

        // Configura os adapters usando layouts padrões do Android
        val adapterTipo = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val adapterRes = ArrayAdapter(this, android.R.layout.simple_spinner_item, resultados)
        adapterRes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val adapterNum = ArrayAdapter(this, android.R.layout.simple_spinner_item, numeros)
        adapterNum.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Liga os adapters aos componentes visuais
        spnTipo.adapter = adapterTipo
        spnResultado.adapter = adapterRes
        spnGols.adapter = adapterNum
        spnAssist.adapter = adapterNum
    }

    /**
     * Verifica se a Activity recebeu dados via Intent (Modo Edição).
     * Se sim, preenche os campos com os dados do jogo existente.
     */
    private fun initEditMode() {
        // Tenta pegar o ID enviado pela ListaJogosActivity
        jogoId = intent.getStringExtra("JOGO_ID")

        // Se o ID não for nulo, significa que estamos editando
        if (jogoId != null) {
            // Preenche os campos de texto
            // .take(10) pega apenas a data (YYYY-MM-DD) ignorando a hora se houver
            edtData.setText(intent.getStringExtra("JOGO_DATA")?.take(10))
            edtAdversario.setText(intent.getStringExtra("JOGO_ADVERSARIO"))
            edtNota.setText(intent.getStringExtra("JOGO_NOTA"))
            // getIntExtra precisa de um valor padrão (0) caso não encontre
            edtSensacao.setText(intent.getIntExtra("JOGO_SENSACAO", 0).toString())
            edtComentarios.setText(intent.getStringExtra("JOGO_COMENTARIOS"))

            // Seleciona os itens corretos nos Spinners
            selecionarSpinner(spnTipo, intent.getStringExtra("JOGO_TIPO"))
            selecionarSpinner(spnResultado, intent.getStringExtra("JOGO_RESULTADO"))

            val gols = intent.getIntExtra("JOGO_GOLS", 0).toString()
            selecionarSpinner(spnGols, gols)

            val assist = intent.getIntExtra("JOGO_ASSISTENCIAS", 0).toString()
            selecionarSpinner(spnAssist, assist)
        }
    }

    /**
     * Função auxiliar para definir o valor selecionado de um Spinner baseado em um Texto.
     * Ex: Se o jogo é "Amistoso", ela procura em qual posição está "Amistoso" na lista e seleciona.
     */
    private fun selecionarSpinner(spinner: Spinner, valor: String?) {
        if (valor == null) return
        val adapter = spinner.adapter as ArrayAdapter<String>
        val position = adapter.getPosition(valor)
        if (position >= 0) {
            spinner.setSelection(position)
        }
    }

    /**
     * Coleta todos os dados da tela e cria um objeto 'Jogo' pronto para enviar à API.
     */
    private fun createJogoObject(id: String?): Jogo {
        return Jogo(
            id = id,
            data = edtData.text.toString(),
            adversario = edtAdversario.text.toString(),
            // Pega o valor atual selecionado no Spinner
            tipo = spnTipo.selectedItem.toString(),
            resultado = spnResultado.selectedItem.toString(),
            // Converte String para Int/Double. "OrNull" evita que o app feche se estiver vazio
            gols = spnGols.selectedItem.toString().toInt(),
            assistencias = spnAssist.selectedItem.toString().toInt(),
            nota = edtNota.text.toString().toDoubleOrNull() ?: 0.0,
            sensacao = edtSensacao.text.toString().toIntOrNull() ?: 0,
            comentarios = edtComentarios.text.toString()
        )
    }

    /**
     * Envia um novo jogo para a API (POST).
     */
    private fun cadastrarJogo() {
        val novoJogo = createJogoObject(null) // ID é nulo na criação
        // Cria a instância do Retrofit já com o Token de autenticação
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

        // Faz a chamada assíncrona
        service.cadastrarJogo(novoJogo).enqueue(getCallback(this, "Partida salva com sucesso!"))
    }

    /**
     * Atualiza um jogo existente na API (PUT).
     */
    private fun atualizarJogo(id: String) {
        val jogoAtualizado = createJogoObject(id) // Usa o ID existente
        val service = ClientRetrofit.getCliente(this).create(DiarioService::class.java)

        // Faz a chamada assíncrona enviando o ID na URL
        service.atualizarJogo(id, jogoAtualizado).enqueue(getCallback(this, "Jogo atualizado com sucesso!"))
    }

    /**
     * Cria um Callback genérico para tratar a resposta da API.
     * Evita repetir o código de onResponse/onFailure duas vezes.
     */
    private fun getCallback(context: AppCompatActivity, msg: String): Callback<Jogo> {
        return object : Callback<Jogo> {
            // Sucesso na comunicação (pode ser 200, 201 ou erro de lógica 400, 500)
            override fun onResponse(call: Call<Jogo>, response: Response<Jogo>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    context.finish() // Fecha a tela e volta para a lista
                } else {
                    Toast.makeText(context, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            // Falha de conexão (sem internet, timeout)
            override fun onFailure(call: Call<Jogo>, t: Throwable) {
                Toast.makeText(context, "Erro de rede", Toast.LENGTH_SHORT).show()
            }
        }
    }
}