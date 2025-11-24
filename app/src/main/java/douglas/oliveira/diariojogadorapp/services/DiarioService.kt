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

interface DiarioService {
    @POST("auth/register")
    fun registrar(@Body usuario: Usuario): Call<AuthResposta>

    @POST("auth/login")
    fun logar(@Body credenciais: LoginCreds): Call<AuthResposta>

    @POST("jogos")
    fun cadastrarJogo(@Body jogo: Jogo): Call<Jogo>

    @GET("jogos")
    fun buscarJogos(): Call<List<Jogo>>

    @DELETE("jogos/{id}")
    fun deleteJogo(@Path("id") id: String): Call<Void> // Exclui um jogo pelo ID

    @PUT("jogos/{id}")
    fun atualizarJogo(@Path("id") id: String, @Body jogo: Jogo): Call<Jogo> // Atualiza um jogo

    // CRUD para Treinos

    @POST("treinos")
    fun cadastrarTreino(@Body treino: Treino): Call<Treino>

    @GET("treinos")
    fun buscarTreinos(): Call<List<Treino>>

    @DELETE("treinos/{id}")
    fun deleteTreino(@Path("id") id: String): Call<Void>

    @PUT("treinos/{id}")
    fun atualizarTreino(@Path("id") id: String, @Body treino: Treino): Call<Treino> // Atualiza treino
}