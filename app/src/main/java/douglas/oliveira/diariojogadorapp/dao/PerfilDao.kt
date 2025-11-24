package douglas.oliveira.diariojogadorapp.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import douglas.oliveira.diariojogadorapp.models.PerfilLocal
import android.content.Context

class PerfilDao(context: Context) {

    private val nomeBanco = "diario_jogador.db"
    private val versaoBanco = 2 // [ATENÇÃO] Versão 2 para atualizar a estrutura
    private val tabelaPerfil = "Perfil"

    private val colId = "id"
    private val colUserIdApi = "user_id_api" // Coluna de vínculo
    private val colNome = "nome"
    private val colNascimento = "data_nascimento"
    private val colTelefone = "telefone"
    private val colEndereco = "endereco"
    private val colFoto = "foto"

    private val dbHelper = object : SQLiteOpenHelper(context, nomeBanco, null, versaoBanco) {
        override fun onCreate(db: SQLiteDatabase?) {
            val sql = """            
            CREATE TABLE $tabelaPerfil (            
                $colId INTEGER PRIMARY KEY AUTOINCREMENT,              
                $colUserIdApi TEXT, 
                $colNome TEXT,
                $colNascimento TEXT,                
                $colTelefone TEXT,
                $colEndereco TEXT,                
                $colFoto TEXT                            
            )
            """
            db?.execSQL(sql)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            // Apaga a tabela antiga e cria a nova se a versão mudar
            db?.execSQL("DROP TABLE IF EXISTS $tabelaPerfil")
            onCreate(db)
        }
    }
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    // Salva ou Atualiza (Baseado no ID da API)
    fun salvarOuAtualizar(perfil: PerfilLocal) {
        val valores = ContentValues().apply {
            put(colUserIdApi, perfil.userIdApi)
            put(colNome, perfil.nome)
            put(colNascimento, perfil.dataNascimento)
            put(colTelefone, perfil.telefone)
            put(colEndereco, perfil.endereco)
            put(colFoto, perfil.foto)
        }

        // Tenta atualizar se já existir esse usuário da API
        val linhas = db.update(tabelaPerfil, valores, "$colUserIdApi = ?", arrayOf(perfil.userIdApi))

        // Se não existe, insere
        if (linhas == 0) {
            db.insert(tabelaPerfil, null, valores)
        }
    }

    // Busca os dados APENAS do usuário logado
    fun recuperarPerfil(userIdApi: String): PerfilLocal? {
        val cursor = db.query(tabelaPerfil, null, "$colUserIdApi = ?", arrayOf(userIdApi), null, null, null)

        var perfil: PerfilLocal? = null
        if (cursor.moveToNext()) {
            perfil = PerfilLocal(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(colId)),
                userIdApi = cursor.getString(cursor.getColumnIndexOrThrow(colUserIdApi)),
                nome = cursor.getString(cursor.getColumnIndexOrThrow(colNome)),
                dataNascimento = cursor.getString(cursor.getColumnIndexOrThrow(colNascimento)),
                telefone = cursor.getString(cursor.getColumnIndexOrThrow(colTelefone)),
                endereco = cursor.getString(cursor.getColumnIndexOrThrow(colEndereco)),
                foto = cursor.getString(cursor.getColumnIndexOrThrow(colFoto))
            )
        }
        cursor.close()
        return perfil
    }
}