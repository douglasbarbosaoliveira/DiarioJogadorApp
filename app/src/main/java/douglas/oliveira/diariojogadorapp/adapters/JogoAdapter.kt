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

class JogoAdapter(private val listaJogos: List<Jogo>) :
    RecyclerView.Adapter<JogoAdapter.ViewHolderJogo>() {

    // 1. Variável para rastrear qual item sofreu o Long Click
    var posicaoClicada: Int = -1

    inner class ViewHolderJogo(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener // 2. Implementa a interface para o menu
    {
        val txtAdversario: TextView = itemView.findViewById(R.id.txtAdversario)
        val txtResultado: TextView = itemView.findViewById(R.id.txtResultado)
        val txtData: TextView = itemView.findViewById(R.id.txtData)

        init {
            // Liga o ViewHolder à Activity para receber o menu
            itemView.setOnCreateContextMenuListener(this)

            // 3. Captura o Long Click e armazena a posição
            itemView.setOnLongClickListener {
                posicaoClicada = adapterPosition
                false // Retorna false para que o sistema possa exibir o menu
            }
        }

        // 4. Cria o menu inflando o XML
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            android.view.MenuInflater(v?.context).inflate(R.menu.menu_jogo, menu)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderJogo {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_jogo, parent, false)
        return ViewHolderJogo(view)
    }

    override fun onBindViewHolder(holder: ViewHolderJogo, position: Int) {
        val jogo = listaJogos[position]
        holder.txtAdversario.text = jogo.adversario
        holder.txtResultado.text = "${jogo.resultado} - ${jogo.gols} Gols"
        holder.txtData.text = DateUtils.formatarDataParaExibicao(jogo.data)
    }

    override fun getItemCount() = listaJogos.size
}