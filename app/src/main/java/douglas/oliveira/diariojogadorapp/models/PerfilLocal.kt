package douglas.oliveira.diariojogadorapp.models

data class PerfilLocal(
    var id: Long = 0,
    var userIdApi: String, // Vínculo com a API
    var nome: String,
    var dataNascimento: String,
    var telefone: String,
    var endereco: String,
    var foto: String
)
