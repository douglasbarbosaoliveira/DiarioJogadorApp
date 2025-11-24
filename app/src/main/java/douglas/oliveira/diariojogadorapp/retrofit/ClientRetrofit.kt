package douglas.oliveira.diariojogadorapp.retrofit

import android.content.Context
import douglas.oliveira.diariojogadorapp.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClientRetrofit {
    private const val BASE_URL = "https://api-jogadores.onrender.com/"

    fun getCliente(context: Context): Retrofit {

        // 1. Cria o Interceptor de Logging e define o nível BODY (para ver tudo)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 2. Constrói o Cliente OkHttp, adicionando o Logger e o Interceptor de Token
        val client = OkHttpClient.Builder()
            .addInterceptor(logging) // Adiciona o Logger PRIMEIRO
            .addInterceptor { chain -> // Seu Interceptor de Token existente
                val req = chain.request().newBuilder()
                val token = SessionManager.getToken(context)
                if (token != null) {
                    req.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(req.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Usa o cliente OkHttp configurado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}