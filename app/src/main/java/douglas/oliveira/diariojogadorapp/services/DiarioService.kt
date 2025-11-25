package douglas.oliveira.diariojogadorapp.services

import douglas.oliveira.diariojogadorapp.models.AuthResposta
import douglas.oliveira.diariojogadorapp.models.Jogo
import douglas.oliveira.diariojogadorapp.models.LoginCreds
import douglas.oliveira.diariojogadorapp.models.Treino
import douglas.oliveira.diariojogadorapp.models.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interface de Serviço (API Contract).
 * Definição de todos os endpoints (URLs), métodos HTTP e tipos de dados
 * que o Retrofit usará para se comunicar com o servidor.
 */
interface DiarioService {

    // ========================================================================
    // ÁREA DE AUTENTICAÇÃO (Login e Registro)
    // ========================================================================

    /**
     * Cria uma nova conta de usuário.
     * @POST: Envia dados para o servidor criar um recurso.
     * @Body: Converte o objeto 'Usuario' (Kotlin) para JSON e envia no corpo da requisição.
     */
    @POST("auth/register")
    fun registrar(@Body usuario: Usuario): Call<AuthResposta>

    /**
     * Autentica um usuário existente.
     * Retorna um 'AuthResposta' que contém o Token JWT necessário para as outras chamadas.
     */
    @POST("auth/login")
    fun logar(@Body credenciais: LoginCreds): Call<AuthResposta>


    // ========================================================================
    // CRUD DE JOGOS (Create, Read, Update, Delete)
    // ========================================================================

    /**
     * Salva uma nova partida.
     * Nota: O Token de autorização é injetado automaticamente pelo 'ClientRetrofit'.
     */
    @POST("jogos")
    fun cadastrarJogo(@Body jogo: Jogo): Call<Jogo>

    /**
     * Busca a lista de jogos do usuário.
     * @GET: Solicita dados do servidor.
     * Retorno: Call<List<Jogo>> -> O JSON de resposta será convertido em uma Lista.
     */
    @GET("jogos")
    fun buscarJogos(): Call<List<Jogo>>

    /**
     * Exclui um jogo específico.
     * @DELETE: Método HTTP para remoção.
     * URL: "jogos/{id}" -> O '{id}' é um placeholder dinâmico.
     * @Path("id"): Pega o valor do parâmetro 'id' da função e substitui no '{id}' da URL.
     */
    @DELETE("jogos/{id}")
    fun deleteJogo(@Path("id") id: String): Call<Void> // Void pois não esperamos corpo de resposta

    /**
     * Atualiza os dados de um jogo existente.
     * @PUT: Método HTTP para atualização.
     * Combina @Path (para saber qual jogo alterar) e @Body (com os novos dados).
     */
    @PUT("jogos/{id}")
    fun atualizarJogo(@Path("id") id: String, @Body jogo: Jogo): Call<Jogo>


    // ========================================================================
    // CRUD DE TREINOS
    // ========================================================================

    /**
     * Salva um novo treino.
     */
    @POST("treinos")
    fun cadastrarTreino(@Body treino: Treino): Call<Treino>

    /**
     * Busca a lista de treinos.
     */
    @GET("treinos")
    fun buscarTreinos(): Call<List<Treino>>

    /**
     * Exclui um treino específico pelo ID.
     */
    @DELETE("treinos/{id}")
    fun deleteTreino(@Path("id") id: String): Call<Void>

    /**
     * Atualiza os dados de um treino existente.
     */
    @PUT("treinos/{id}")
    fun atualizarTreino(@Path("id") id: String, @Body treino: Treino): Call<Treino>
}