package douglas.oliveira.diariojogadorapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * Tela "Sobre o App".
 * Esta é uma Activity informativa estática.
 * Sua única função lógica é exibir o layout e permitir voltar para a tela anterior.
 */
class SobreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o layout visual (XML) que contém o texto, versão e créditos
        setContentView(R.layout.activity_sobre)

        // --- Configuração do Botão Voltar ---
        // Encontra o botão pelo ID definido no XML
        findViewById<Button>(R.id.btnVoltarSobre).setOnClickListener {


            // Encerra (destrói) esta Activity atual e a remove da pilha.
            finish()
        }
    }
}