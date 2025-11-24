package douglas.oliveira.diariojogadorapp.models

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("_id") val id: String? = null,
    val nome: String,
    val email: String,
    val senha: String? = null
)
