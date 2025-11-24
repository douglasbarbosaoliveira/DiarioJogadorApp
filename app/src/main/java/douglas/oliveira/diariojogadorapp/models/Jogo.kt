package douglas.oliveira.diariojogadorapp.models

import com.google.gson.annotations.SerializedName

data class Jogo(
    @SerializedName("_id") val id: String? = null,
    val data: String,
    val adversario: String,
    val tipo: String,      // Ex: amistoso
    val resultado: String, // Ex: vitoria, derrota
    val gols: Int,
    val assistencias: Int,
    val nota: Double,
    val sensacao: Int,     // 1 a 10
    val comentarios: String
)
