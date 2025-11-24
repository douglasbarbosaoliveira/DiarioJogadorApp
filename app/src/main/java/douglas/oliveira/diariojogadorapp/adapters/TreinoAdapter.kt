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

class TreinoAdapter(val listaTreinos: List<Treino>) :
    RecyclerView.Adapter<TreinoAdapter.ViewHolderTreino>() {

    var posicaoClicada: Int = -1

    inner class ViewHolderTreino(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener
    {
        val txtTipo: TextView = itemView.findViewById(R.id.txtTipo)
        val txtDuracao: TextView = itemView.findViewById(R.id.txtDuracao)
        val txtData: TextView = itemView.findViewById(R.id.txtData)

        init {
            itemView.setOnCreateContextMenuListener(this)
            itemView.setOnLongClickListener {
                posicaoClicada = adapterPosition
                false
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            // [ATUALIZADO] Agora inflamos o menu XML para ter Editar e Excluir
            android.view.MenuInflater(v?.context).inflate(R.menu.menu_treino, menu)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTreino {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_treino, parent, false)
        return ViewHolderTreino(view)
    }

    override fun onBindViewHolder(holder: ViewHolderTreino, position: Int) {
        val treino = listaTreinos[position]
        holder.txtTipo.text = treino.tipo
        holder.txtDuracao.text = "${treino.duracaoMin} min | ${treino.intensidade}"

        // Usa o utilitário de data para formatar bonito (DD/MM/AAAA)
        holder.txtData.text = DateUtils.formatarDataParaExibicao(treino.data)
    }

    override fun getItemCount(): Int = listaTreinos.size
}