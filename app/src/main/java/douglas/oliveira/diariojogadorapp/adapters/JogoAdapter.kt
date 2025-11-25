package douglas.oliveira.diariojogadorapp.adapters

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import douglas.oliveira.diariojogadorapp.R
import douglas.oliveira.diariojogadorapp.models.Jogo
import douglas.oliveira.diariojogadorapp.utils.DateUtils

/**
 * Adapter responsável por gerenciar a lista de Jogos na RecyclerView.
 * Ele converte os dados da lista (List<Jogo>) em visualizações (XML) na tela.
 */
class JogoAdapter(private val listaJogos: List<Jogo>) :
    RecyclerView.Adapter<JogoAdapter.ViewHolderJogo>() {

    // Variável auxiliar para rastrear qual item da lista sofreu o "Long Click" (clique longo).
    // A Activity (ListaJogosActivity) lê essa variável para saber qual jogo Editar ou Excluir.
    var posicaoClicada: Int = -1

    /**
     * ViewHolder: Classe interna que guarda as referências dos componentes visuais (TextViews) de UM item.
     * Implementa View.OnCreateContextMenuListener para permitir o menu de contexto (Editar/Excluir).
     */
    inner class ViewHolderJogo(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener
    {
        // Mapeia os campos do layout XML (item_jogo.xml)
        val txtAdversario: TextView = itemView.findViewById(R.id.txtAdversario)
        val txtResultado: TextView = itemView.findViewById(R.id.txtResultado)
        val txtData: TextView = itemView.findViewById(R.id.txtData)

        init {
            // 1. Registra o listener para criar o menu de contexto quando segurar o clique
            itemView.setOnCreateContextMenuListener(this)

            // 2. Configura o clique longo para salvar a posição atual
            itemView.setOnLongClickListener {
                posicaoClicada = adapterPosition // Salva o índice (0, 1, 2...) do item clicado
                false // Retorna false para que o evento continue e o menu seja exibido
            }
        }

        /**
         * Método chamado quando o menu de contexto está sendo construído.
         * Aqui "inflamos" o arquivo de menu XML específico para Jogos.
         */
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            // Carrega as opções definidas em 'res/menu/menu_jogo.xml' (Editar Jogo, Excluir Jogo)
            android.view.MenuInflater(v?.context).inflate(R.menu.menu_jogo, menu)
        }
    }

    /**
     * Cria a visualização do item (o layout XML) quando necessário.
     * O RecyclerView chama isso apenas o suficiente para preencher a tela visível (+ buffer).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderJogo {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_jogo, parent, false)
        return ViewHolderJogo(view)
    }

    /**
     * Conecta os dados ao visual. Chamado para cada linha da lista que aparece na tela.
     */
    override fun onBindViewHolder(holder: ViewHolderJogo, position: Int) {
        // 1. Pega o objeto Jogo da lista na posição correspondente
        val jogo = listaJogos[position]

        // 2. Preenche os textos na tela
        holder.txtAdversario.text = jogo.adversario
        // Formata uma string combinando resultado e gols (Ex: "Vitória - 3 Gols")
        holder.txtResultado.text = "${jogo.resultado} - ${jogo.gols} Gols"

        // 3. Formata a data usando o utilitário (De "YYYY-MM-DD" para "DD/MM/AAAA")
        holder.txtData.text = DateUtils.formatarDataParaExibicao(jogo.data)
    }

    /**
     * Retorna o tamanho total da lista. O RecyclerView precisa disso para saber quando parar de rolar.
     */
    override fun getItemCount() = listaJogos.size
}