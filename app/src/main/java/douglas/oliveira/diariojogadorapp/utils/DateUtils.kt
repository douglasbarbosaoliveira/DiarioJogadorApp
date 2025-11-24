package douglas.oliveira.diariojogadorapp.utils

import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {

    fun formatarDataParaExibicao(dataApi: String?): String {
        if (dataApi.isNullOrEmpty()) return "--/--/----"

        // A API manda: 2025-11-23T00:00:00.000Z ou 2025-11-23
        // Vamos pegar apenas os 10 primeiros caracteres (2025-11-23)
        val dataLimpa = if (dataApi.length >= 10) dataApi.substring(0, 10) else dataApi

        try {
            // O formato que VEM (Entrada)
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            // O formato que VAI SAIR (Saída) -> DD/MM/YYYY
            val formatoSaida = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

            val data = formatoEntrada.parse(dataLimpa)
            return formatoSaida.format(data!!)
        } catch (e: Exception) {
            return dataLimpa // Se der erro, devolve a original
        }
    }
}