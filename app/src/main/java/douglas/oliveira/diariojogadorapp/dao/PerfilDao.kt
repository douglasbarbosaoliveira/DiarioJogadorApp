package douglas.oliveira.diariojogadorapp.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import douglas.oliveira.diariojogadorapp.models.PerfilLocal

/**
 * DAO (Data Access Object) para o Perfil.
 * Responsável por toda a comunicação com o banco de dados SQLite interno do celular.
 */
class PerfilDao(context: Context) {

    // Configurações do Banco de Dados
    private val nomeBanco = "diario_jogador.db"
    private val versaoBanco = 2 // Versão do banco (se mudar a estrutura, aumente este número)
    private val tabelaPerfil = "Perfil"

    // Nomes das Colunas (Prática para evitar erros de digitação no SQL)
    private val colId = "id"
    private val colUserIdApi = "user_id_api" // A chave que vincula este perfil ao login da API
    private val colNome = "nome"
    private val colNascimento = "data_nascimento"
    private val colTelefone = "telefone"
    private val colEndereco = "endereco"
    private val colFoto = "foto"

    /**
     * Helper interno que gerencia a criação e atualização do banco.
     * O Android chama o onCreate automaticamente se o banco não existir no celular.
     */
    private val dbHelper = object : SQLiteOpenHelper(context, nomeBanco, null, versaoBanco) {

        // Executado apenas na primeira vez que o app é instalado/rodado
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
            db?.execSQL(sql) // Executa o comando SQL de criação
        }

        // Executado se mudar 'versaoBanco' (ex: de 1 para 2)
        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            // Estratégia simples: Apaga a tabela antiga e cria uma nova
            db?.execSQL("DROP TABLE IF EXISTS $tabelaPerfil")
            onCreate(db)
        }
    }

    // Obtém uma instância do banco pronta para escrita/leitura
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    /**
     * Método Inteligente: Salvar ou Atualizar (Upsert).
     * Verifica se o usuário já tem dados salvos. Se tiver, atualiza. Se não, cria.
     */
    fun salvarOuAtualizar(perfil: PerfilLocal) {
        // Prepara os dados para serem enviados ao banco (Mapeia Coluna -> Valor)
        val valores = ContentValues().apply {
            put(colUserIdApi, perfil.userIdApi)
            put(colNome, perfil.nome)
            put(colNascimento, perfil.dataNascimento)
            put(colTelefone, perfil.telefone)
            put(colEndereco, perfil.endereco)
            put(colFoto, perfil.foto)
        }

        // Tenta fazer um UPDATE na tabela onde o user_id_api for igual ao do usuário logado
        // Retorna o número de linhas afetadas
        val linhas = db.update(
            tabelaPerfil,
            valores,
            "$colUserIdApi = ?",
            arrayOf(perfil.userIdApi)
        )

        // Se linhas == 0, significa que o update falhou (não existe esse usuário no banco ainda)
        // Faz um INSERT
        if (linhas == 0) {
            db.insert(tabelaPerfil, null, valores)
        }
    }

    /**
     * Busca os dados de perfil filtrando pelo ID da API.
     * Isso garante que um usuário não veja os dados de outro no mesmo celular.
     */
    fun recuperarPerfil(userIdApi: String): PerfilLocal? {
        // Faz o SELECT * FROM Perfil WHERE user_id_api = ?
        val cursor = db.query(
            tabelaPerfil,
            null, // null = todas as colunas
            "$colUserIdApi = ?", // Cláusula WHERE
            arrayOf(userIdApi), // Valor do WHERE
            null, null, null
        )

        var perfil: PerfilLocal? = null

        // Se o cursor encontrar algum resultado (moveToNext)
        if (cursor.moveToNext()) {
            // Reconstrói o objeto PerfilLocal pegando os dados das colunas
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

        cursor.close() // Importante: fecha o cursor para liberar memória
        return perfil
    }
}