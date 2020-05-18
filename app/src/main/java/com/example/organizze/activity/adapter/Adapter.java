package com.example.organizze.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.organizze.R;
import com.example.organizze.activity.model.Movimentacao;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder>{

    private List<Movimentacao> movimentacaoList;
    private Context context;

    public Adapter(List<Movimentacao> movimentacaoList, Context context) {
        this.movimentacaoList = movimentacaoList;
        this.context = context;
    }

    @NonNull
    @Override //Método chamado para criar as visualizações que aparecem na tela do user
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //LayoutInflater converte um XML em view
        //from parent recupera o contexto do parent que o itemLista está dentro
        View itemList = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_lista, parent, false);
        return new MyViewHolder(itemList);
    }

    @Override //Quando o onCreate termina de criar as views, este método é chamado para exibir os elementos
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Movimentacao movimentacao = movimentacaoList.get(position); //Pego a posição para exibir o item correspondente a ela
        holder.titulo.setText(movimentacao.getDescricao());
        holder.valor.setText("R$ " + movimentacao.getValor());
        holder.categoria.setText(movimentacao.getCategoria());
        holder.valor.setTextColor(context.getResources().getColor(R.color.colorPrimaryReceita));

        if(movimentacao.getTipo() == "d" || movimentacao.getTipo().equals("d")){
            holder.valor.setTextColor(context.getResources().getColor(R.color.colorPrimaryDespesa));
            holder.valor.setText("- R$ " + movimentacao.getValor());
        }
    }

    //Este método irá chamar o onBindViewHolder o número de vezes do seu retorno.
    //Desta forma a position vai sendo incrementada de acordo com as chamadas
    @Override
    public int getItemCount() {
        return movimentacaoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        //itens que a view ieá exibir
        TextView titulo, valor, categoria;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            titulo = itemView.findViewById(R.id.textDescricao);
            valor = itemView.findViewById(R.id.textValor);
            categoria = itemView.findViewById(R.id.textCategoria);
        }
    }

}
