package douglas.oliveira.diariojogadorapp.models

import com.google.gson.annotations.SerializedName

data class Treino(
    @SerializedName("_id") val id: String? = null,
    val data: String,
    val tipo: String,         // Ex: cardio
    val duracaoMin: Int,
    val intensidade: String,  // Ex: alta
    val sensacao: Int,
    val observacoes: String
)
