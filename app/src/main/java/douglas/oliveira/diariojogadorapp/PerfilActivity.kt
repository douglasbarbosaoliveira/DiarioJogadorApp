package douglas.oliveira.diariojogadorapp

import android.Manifest
import android.app.DatePickerDialog
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

class PerfilActivity : AppCompatActivity() {

    // ... (mesmas variáveis de antes: views, dao, etc) ...
    // Views
    lateinit var edtDataNasc: EditText
    lateinit var txtIdade: TextView
    lateinit var edtTelefone: EditText
    lateinit var edtEndereco: EditText
    lateinit var imgPerfil: ImageView
    lateinit var btnSalvar: Button

    private var caminhoFoto: String = ""
    private var usuarioIdApi: String = ""
    private val calendar = Calendar.getInstance()

    // 1. [NOVO] Contrato para tirar foto (Retorna um Bitmap)
    private val tirarFotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            imgPerfil.setImageBitmap(bitmap)
            // Precisamos converter o Bitmap para uma URI ou Base64 para salvar no SQLite
            // Como SQLite prefere Strings, vamos salvar a URI temporária dessa imagem
            caminhoFoto = bitmapToUri(bitmap).toString()
        }
    }

    // 2. [NOVO] Contrato para pedir permissão
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

        // ... (Recuperação de sessão e setup de views igual ao anterior) ...
        // Recupera ID e Nome
        usuarioIdApi = SessionManager.getId(this) ?: ""
        val nomeUsuario = SessionManager.getName(this)
        val emailUsuario = SessionManager.getEmail(this) // Verifique se isso está vindo corretamente

        val edtNome = findViewById<EditText>(R.id.edtNomePerfil)
        val edtEmail = findViewById<EditText>(R.id.edtEmailPerfil)
        edtNome.setText(nomeUsuario)
        edtEmail.setText(emailUsuario)

        edtDataNasc = findViewById(R.id.edtDataNasc)
        txtIdade = findViewById(R.id.txtIdadeCalculada)
        edtTelefone = findViewById(R.id.edtTelefonePerfil)
        edtEndereco = findViewById(R.id.edtEnderecoPerfil)
        imgPerfil = findViewById(R.id.imgFotoPerfil)
        btnSalvar = findViewById(R.id.btnSalvarPerfil)

        // ... (Carregar dados do SQLite igual ao anterior) ...
        val dao = PerfilDao(this)
        val perfil = dao.recuperarPerfil(usuarioIdApi)
        if (perfil != null) {
            edtDataNasc.setText(perfil.dataNascimento)
            edtTelefone.setText(perfil.telefone)
            edtEndereco.setText(perfil.endereco)
            if (perfil.dataNascimento.isNotEmpty()) calcularEMostrarIdade(perfil.dataNascimento)
            if (perfil.foto.isNotEmpty()) {
                caminhoFoto = perfil.foto
                imgPerfil.setImageURI(Uri.parse(perfil.foto))
            }
        }

        // 3. [ALTERADO] Clique na foto agora verifica permissão e abre câmera
        imgPerfil.setOnClickListener {
            verificarPermissaoEAbriCamera()
        }

        btnSalvar.setOnClickListener {
            salvarPerfilLocal(nomeUsuario ?: "Jogador")
        }

        configurarDatePicker()
    }

    private fun verificarPermissaoEAbriCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamera() {
        tirarFotoLauncher.launch(null)
    }

    // Função auxiliar para converter o Bitmap da câmera em uma URI temporária para salvar no banco
    private fun bitmapToUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "FotoPerfil_${System.currentTimeMillis()}", null)
        return Uri.parse(path)
    }

    // ... (Funções salvarPerfilLocal, configurarDatePicker e calcularIdade mantêm-se iguais) ...

    private fun salvarPerfilLocal(nome: String) {
        val dao = PerfilDao(this)
        val perfil = PerfilLocal(
            userIdApi = usuarioIdApi,
            nome = nome,
            dataNascimento = edtDataNasc.text.toString(),
            telefone = edtTelefone.text.toString(),
            endereco = edtEndereco.text.toString(),
            foto = caminhoFoto
        )
        dao.salvarOuAtualizar(perfil)
        Toast.makeText(this, "Perfil salvo!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun configuringDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val dataFormatada = sdf.format(calendar.time)
            edtDataNasc.setText(dataFormatada)
            calcularEMostrarIdade(dataFormatada)
        }
        edtDataNasc.setOnClickListener {
            DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
    private fun configurarDatePicker() = configuringDatePicker()

    private fun calcularEMostrarIdade(dataNascString: String) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val dataNasc = sdf.parse(dataNascString)
            if (dataNasc != null) {
                val hoje = Calendar.getInstance()
                val nascimento = Calendar.getInstance()
                nascimento.time = dataNasc
                var idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR)
                if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) idade--
                txtIdade.text = "$idade anos"
            }
        } catch (e: Exception) { txtIdade.text = "-" }
    }
}