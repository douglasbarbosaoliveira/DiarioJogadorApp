package douglas.oliveira.diariojogadorapp.retrofit

import android.content.Context
import douglas.oliveira.diariojogadorapp.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Configuração do Cliente Retrofit.
 * Utiliza 'object' para criar um Singleton (uma única instância para o app todo),
 * economizando memória e recursos.
 */
object ClientRetrofit {

    // A URL base da API.
    private const val BASE_URL = "https://api-jogadores.onrender.com/"

    /**
     * Método que constrói e retorna o objeto Retrofit pronto para uso.
     * Recebe o 'context' para poder acessar o SessionManager (SharedPreferences).
     */
    fun getCliente(context: Context): Retrofit {

        // 1. Configuração do LOG (HttpLoggingInterceptor)
        // Permite ver no Logcat (abaixo) tudo que é enviado e recebido (JSON, Erros, Headers).
        // O nível 'BODY' mostra o conteúdo completo da requisição.
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 2. Configuração do Cliente HTTP (OkHttp)
        // OBS: O OkHttp é quem realmente faz o trabalho pesado de conexão. O Retrofit usa ele por baixo dos panos.
        val client = OkHttpClient.Builder()
            .addInterceptor(logging) // Adiciona o espião (Logger) para vermos o que acontece

            // --- INTERCEPTADOR DE AUTENTICAÇÃO ---
            // Este bloco roda antes de TODA requisição sair do celular.
            .addInterceptor { chain ->
                // Pega a requisição original
                val req = chain.request().newBuilder()

                // Tenta buscar o Token salvo no celular
                val token = SessionManager.getToken(context)

                // Se tiver token, injeta ele no Cabeçalho (Header) da requisição
                // Formato padrão: "Authorization: Bearer eyJhbGciOiJIUz..."
                if (token != null) {
                    req.addHeader("Authorization", "Bearer $token")
                }

                // Deixa a requisição seguir viagem para o servidor
                chain.proceed(req.build())
            }
            .build()

        // 3. Construção do Retrofit
        return Retrofit.Builder()
            .baseUrl(BASE_URL) // Define o endereço do servidor
            .client(client) // Acopla o cliente OkHttp que configuramos acima
            .addConverterFactory(GsonConverterFactory.create()) // Ensina o Retrofit a converter JSON em Objetos Kotlin (e vice-versa)
            .build()
    }
}