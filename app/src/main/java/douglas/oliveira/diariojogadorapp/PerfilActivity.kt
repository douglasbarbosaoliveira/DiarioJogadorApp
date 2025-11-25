package douglas.oliveira.diariojogadorapp

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import douglas.oliveira.diariojogadorapp.dao.PerfilDao
import douglas.oliveira.diariojogadorapp.models.PerfilLocal
import douglas.oliveira.diariojogadorapp.utils.SessionManager
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de Perfil do Usuário.
 * Gerencia dados pessoais salvos localmente no dispositivo (SQLite).
 * Integra câmera para foto de perfil e cálculo automático de idade.
 */
class PerfilActivity : AppCompatActivity() {

    // --- Declaração de Views ---
    private lateinit var edtNome: EditText      // Apenas leitura (vem da API)
    private lateinit var edtEmail: EditText     // Apenas leitura (vem da API)
    private lateinit var edtDataNasc: EditText  // Abre DatePicker
    private lateinit var edtIdade: EditText     // Calculado automaticamente
    private lateinit var edtTelefone: EditText
    private lateinit var edtEndereco: EditText
    private lateinit var imgPerfil: ImageView   // Abre Câmera
    private lateinit var btnSalvar: Button
    private lateinit var btnSobre: Button       // Navega para tela Sobre
    private lateinit var btnFaleConosco: Button // Abre app de email

    // --- Variáveis de Estado ---
    private var caminhoFoto: String = "" // Armazena URI da foto salva
    private var usuarioIdApi: String = "" // ID único para vincular dados ao usuário logado
    private val calendar = Calendar.getInstance()

    /**
     * Contrato para tirar foto (TakePicturePreview).
     * Recebe um Bitmap (thumbnail) da câmera quando a foto é tirada.
     */
    private val tirarFotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Exibe a foto na tela
            imgPerfil.setImageBitmap(bitmap)
            // Converte e salva o caminho para persistir no banco
            caminhoFoto = bitmapToUri(bitmap).toString()
        }
    }

    /**
     * Contrato para pedir permissão de Câmera.
     * Se concedida, abre a câmera. Se negada, avisa o usuário.
     */
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            abrirCamera()
        } else {
            Toast.makeText(this, "Permissão de câmera necessária", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // 1. Recupera ID do usuário logado para garantir isolamento de dados
        usuarioIdApi = SessionManager.getId(this) ?: ""

        // 2. Recupera dados fixos da sessão (Nome e Email vindos do Login)
        val nomeUsuario = SessionManager.getName(this)
        val emailUsuario = SessionManager.getEmail(this)

        // Segurança: Se não tiver ID, não permite usar a tela
        if (usuarioIdApi.isEmpty()) {
            Toast.makeText(this, "Erro: Usuário não identificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializa componentes visuais
        edtNome = findViewById(R.id.edtNomePerfil)
        edtEmail = findViewById(R.id.edtEmailPerfil)
        edtDataNasc = findViewById(R.id.edtDataNasc)
        edtIdade = findViewById(R.id.edtIdadeCalculada)
        edtTelefone = findViewById(R.id.edtTelefonePerfil)
        edtEndereco = findViewById(R.id.edtEnderecoPerfil)
        imgPerfil = findViewById(R.id.imgFotoPerfil)
        btnSalvar = findViewById(R.id.btnSalvarPerfil)
        btnSobre = findViewById(R.id.btnSobreApp)
        btnFaleConosco = findViewById(R.id.btnFaleConosco)

        // Preenche campos travados
        edtNome.setText(nomeUsuario)
        edtEmail.setText(emailUsuario)

        // 3. Carrega dados do SQLite (se existirem)
        carregarPerfil()

        // --- Listeners de Clique ---

        imgPerfil.setOnClickListener {
            verificarPermissaoEAbriCamera()
        }

        btnSalvar.setOnClickListener {
            salvarPerfilLocal(nomeUsuario ?: "Jogador")
        }

        btnSobre.setOnClickListener {
            startActivity(Intent(this, SobreActivity::class.java))
        }

        btnFaleConosco.setOnClickListener {
            enviarEmailSuporte()
        }

        // Configura o seletor de data
        configurarDatePicker()
    }

    /**
     * Abre o aplicativo de e-mail do usuário com destinatário e assunto preenchidos.
     * Usa Intent Implícito ACTION_SENDTO.
     */
    private fun enviarEmailSuporte() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("faleconosco@diariodojogador.com.br"))
            putExtra(Intent.EXTRA_SUBJECT, "Suporte - Aplicativo Diário do Jogador")
            putExtra(Intent.EXTRA_TEXT, "Olá, gostaria de falar sobre...")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Nenhum app de e-mail encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Verifica se o app tem permissão de CAMERA antes de tentar abrir.
     */
    private fun verificarPermissaoEAbriCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamera()
        } else {
            // Se não tem, solicita ao usuário
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamera() {
        tirarFotoLauncher.launch(null)
    }

    /**
     * Função Utilitária: Converte um Bitmap (memória) em um arquivo temporário no MediaStore
     * e retorna a URI (caminho) desse arquivo para salvarmos no banco.
     */
    private fun bitmapToUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        // Insere na galeria e pega o caminho
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "FotoPerfil_${System.currentTimeMillis()}", null)
        return Uri.parse(path)
    }

    /**
     * Salva ou Atualiza os dados no SQLite usando o DAO.
     */
    private fun salvarPerfilLocal(nome: String) {
        val dao = PerfilDao(this)

        val perfil = PerfilLocal(
            userIdApi = usuarioIdApi, // Vínculo para multiusuário
            nome = nome,
            dataNascimento = edtDataNasc.text.toString(),
            telefone = edtTelefone.text.toString(),
            endereco = edtEndereco.text.toString(),
            foto = caminhoFoto // Salva o caminho da imagem
        )

        dao.salvarOuAtualizar(perfil)
        Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }

    /**
     * Busca os dados no SQLite filtrando pelo ID do usuário logado.
     */
    private fun carregarPerfil() {
        val dao = PerfilDao(this)
        val perfil = dao.recuperarPerfil(usuarioIdApi)

        if (perfil != null) {
            edtDataNasc.setText(perfil.dataNascimento)
            edtTelefone.setText(perfil.telefone)
            edtEndereco.setText(perfil.endereco)

            // Se tiver data, já calcula a idade visualmente
            if (perfil.dataNascimento.isNotEmpty()) {
                calcularEMostrarIdade(perfil.dataNascimento)
            }

            // Se tiver foto, carrega na ImageView
            if (perfil.foto.isNotEmpty()) {
                caminhoFoto = perfil.foto
                try {
                    imgPerfil.setImageURI(Uri.parse(perfil.foto))
                } catch (e: Exception) {
                    // Se a foto foi apagada da galeria, não quebra o app
                }
            }
        }
    }

    /**
     * Configura o DatePicker para selecionar data de nascimento.
     */
    private fun configuringDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val dataFormatada = sdf.format(calendar.time)

            edtDataNasc.setText(dataFormatada)
            calcularEMostrarIdade(dataFormatada) // Recalcula idade ao mudar data
        }

        edtDataNasc.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    // Atalho
    private fun configurarDatePicker() = configuringDatePicker()

    /**
     * Calcula a idade exata baseada na data de nascimento e data atual.
     */
    private fun calcularEMostrarIdade(dataNascString: String) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val dataNasc = sdf.parse(dataNascString)

            if (dataNasc != null) {
                val hoje = Calendar.getInstance()
                val nascimento = Calendar.getInstance()
                nascimento.time = dataNasc

                var idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR)

                // Ajuste se ainda não fez aniversário este ano
                if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) {
                    idade--
                }
                edtIdade.setText("$idade anos")
            }
        } catch (e: Exception) {
            edtIdade.setText("-")
        }
    }
}