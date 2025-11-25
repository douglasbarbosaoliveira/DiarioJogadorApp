package douglas.oliveira.diariojogadorapp.adapters

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import douglas.oliveira.diariojogadorapp.R
import douglas.oliveira.diariojogadorapp.models.Treino
import douglas.oliveira.diariojogadorapp.utils.DateUtils

/**
 * Adapter para a lista de Treinos.
 * Padrão de Projeto: ADAPTER.
 * Função: Conectar a fonte de dados (List<Treino>) com o componente visual (RecyclerView).
 * Ele "adapta" o objeto Kotlin para virar um Layout XML na tela.
 */
class TreinoAdapter(val listaTreinos: List<Treino>) :
    RecyclerView.Adapter<TreinoAdapter.ViewHolderTreino>() {

    // Variável auxiliar para guardar qual posição da lista foi clicada.
    // Isso é necessário porque o evento de "Deletar/Editar" acontece na Activity,
    // e a Activity precisa saber QUAL item o usuário selecionou aqui no Adapter.
    var posicaoClicada: Int = -1

    /**
     * ViewHolder (Portador da Visualização).
     * Padrão de Projeto: VIEWHOLDER.
     * Função: Guardar as referências dos elementos do layout (TextViews) em memória.
     * Isso evita que o Android tenha que ficar procurando (findViewById) toda hora que você rola a lista,
     * o que deixaria o app lento.
     */
    inner class ViewHolderTreino(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener // Implementa interface para criar menus flutuantes
    {
        // Mapeamento dos componentes do XML (item_treino.xml)
        val txtTipo: TextView = itemView.findViewById(R.id.txtTipo)
        val txtDuracao: TextView = itemView.findViewById(R.id.txtDuracao)
        val txtData: TextView = itemView.findViewById(R.id.txtData)

        init {
            // 1. Avisa que este item tem um menu de contexto (ao segurar o clique)
            itemView.setOnCreateContextMenuListener(this)

            // 2. Configura o clique longo (Long Click)
            itemView.setOnLongClickListener {
                posicaoClicada = adapterPosition // Salva a posição do item clicado (0, 1, 2...)
                false // Retorna 'false' para permitir que o menu de contexto seja exibido logo em seguida
            }
        }

        /**
         * Cria o menu de opções (Editar/Excluir) quando o usuário segura o item.
         */
        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            // Infla o arquivo XML de menu (res/menu/menu_treino.xml)
            android.view.MenuInflater(v?.context).inflate(R.menu.menu_treino, menu)
        }
    }

    /**
     * onCreateViewHolder: Criar a "Gaveta".
     * Este método é chamado apenas algumas vezes para criar os layouts visuais iniciais
     * que cabem na tela (e mais alguns de reserva).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTreino {
        // Pega o XML (item_treino) e o transforma em um objeto View (Inflate)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_treino, parent, false)
        return ViewHolderTreino(view)
    }

    /**
     * onBindViewHolder: Encher a "Gaveta" com dados.
     * Este método é chamado MUITAS VEZES. Toda vez que um item aparece na tela durante a rolagem,
     * este método pega os dados da lista e coloca nos TextViews.
     */
    override fun onBindViewHolder(holder: ViewHolderTreino, position: Int) {
        // 1. Pega o objeto Treino correspondente à posição atual
        val treino = listaTreinos[position]

        // 2. Preenche os dados na tela
        holder.txtTipo.text = treino.tipo
        holder.txtDuracao.text = "${treino.duracaoMin} min | ${treino.intensidade}"

        // 3. Usa nossa classe utilitária para formatar a data (De: 2025-11-23 Para: 23/11/2025)
        holder.txtData.text = DateUtils.formatarDataParaExibicao(treino.data)
    }

    /**
     * getItemCount: Quantos itens existem?
     * O RecyclerView precisa saber o tamanho total da lista para calcular o tamanho da barra de rolagem.
     */
    override fun getItemCount(): Int = listaTreinos.size
}