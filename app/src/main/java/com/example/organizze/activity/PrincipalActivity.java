package com.example.organizze.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.organizze.activity.adapter.Adapter;
import com.example.organizze.activity.config.ConfiguracaoFirebase;
import com.example.organizze.activity.helper.Base64Custom;
import com.example.organizze.activity.model.Movimentacao;
import com.example.organizze.activity.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.organizze.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity {

    private MaterialCalendarView materialCalendarView;
    private TextView textoSaudacao;
    private TextView textoSaldo;
    private Double despesaTotal = 0.0;
    private Double receitaTotal = 0.0;
    private Double resumoUsuario = 0.0;

    private FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
    private DatabaseReference userRef;
    private DatabaseReference dataBaseRef = ConfiguracaoFirebase.getFirebaseData();
    private DatabaseReference movimentacaoReferencia;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;

    private List<Movimentacao> movimentacaoList = new ArrayList<>();
    private Movimentacao movimentacao;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private String mesAnoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar); //Este método faz com que a toolbar funcione corretamente em versões anteriores do android

        textoSaudacao = findViewById(R.id.textSaudacao);
        textoSaldo = findViewById(R.id.textSaldo);
        recyclerView = findViewById(R.id.recyclerMovimentacao);

        materialCalendarView = findViewById(R.id.calendarView);
        //Configurar Adapter
        adapter = new Adapter(movimentacaoList, this);

        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this); //Tipo de layout que eu quero
        recyclerView.setLayoutManager(layoutManager); //set do layout escolhido acima
        recyclerView.setHasFixedSize(true); //set de layout fixo por recomendação do Google
        recyclerView.setAdapter(adapter);

        configuraCalendarView();
        swipe();

        //Floating Action Button para add receita
        com.github.clans.fab.FloatingActionButton fabReceita = findViewById(R.id.menu_receita);
        fabReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Quando coloco o contexto dentro do OnClick não basta por o this, devo colocar também o nome do pacote atual.
                //Quando coloco em um método à parte posso por somente o this para referenciar o pacote.
                startActivity(new Intent(PrincipalActivity.this, ReceitasActivity.class));
            }
        });

        //Floating Action Button para add despesa
        com.github.clans.fab.FloatingActionButton fabDespesa = findViewById(R.id.menu_despesa);
        fabDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PrincipalActivity.this, DespesasActivity.class));
            }
        });
    }

    @Override //Cria opções no menu da toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        //Converte xml em uma view
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override //Botão para logout na toolbar
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menuSair){
            //firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth(); *Está instanciado acima* Preciso recuperar o usuário logado para poder dar signOut abaixo
            firebaseAuth.signOut();
            startActivity(new Intent(this, MainActivity.class)); //Envia o usuário para o menu principal
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void configuraCalendarView(){ //Configura os meses em português
        //Configura a exibição dos meses no calendário
        CharSequence meses[] = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        materialCalendarView.setTitleMonths(meses);

        CalendarDay dataAtual = materialCalendarView.getCurrentDate();
        mesAnoSelecionado = dataAtual.getMonth() +""+ dataAtual.getYear();

        //Método para ler o recycleView correspondente a cada mês conforme mudança na navegação do calendar
        materialCalendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                mesAnoSelecionado = date.getMonth() +""+ date.getYear();
                movimentacaoReferencia.removeEventListener(valueEventListenerMovimentacoes); //Remove o evento anterior
                recuperarMovimentacoes(); //adiciona novo evento
            }
        });
    }



    public void recuperarMovimentacoes(){
        String emailUsuario = firebaseAuth.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario); //codifico email utilizando base64
        movimentacaoReferencia = dataBaseRef.child("movimentacao")
                                            .child(idUsuario) //Acesso às movimentacoes daquele usuario no firebase (acesso ao usuario por email codificado em base64)
                                            .child(mesAnoSelecionado);//Acesso às movimentacoes do mêsAno selecionado

        //O objeto valueEventListener terá acesso ao eventoListener para que possa parar de chamar dados no firebase no OnStop
        valueEventListenerMovimentacoes = movimentacaoReferencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)//Com dataSnapshot recupero todas as movimentações dentre do mêsAno selecionado
            {
                movimentacaoList.clear();
                for(DataSnapshot dados : dataSnapshot.getChildren())//getChildren percorre cada uma das movimentações com tudo que há dentro dela
                {
                    Movimentacao movimentacao = dados.getValue(Movimentacao.class);
                    movimentacao.setMovKey(dados.getKey());//Recuperar a chave da movimentação gerada pelo firebase
                    movimentacaoList.add(movimentacao);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void recuperarResumo(){ //Recuperar valor atual de receita - despesa / dizer olá com nome do user
        userRef = ConfiguracaoFirebase.recuperarUsuario();

        //O objeto valueEventListener terá acesso ao eventoListener para que possa parar de chamar dados no firebase no OnStop
        valueEventListenerUsuario = userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);//Recuperar todos os dados do usuario logado
                despesaTotal = usuario.getDespesaTotal();
                receitaTotal = usuario.getReceitaTotal();
                resumoUsuario = receitaTotal - despesaTotal;

                DecimalFormat decimalFormat = new DecimalFormat("0.##");//Formata duas casas decimais após a vírgula
                String resultadoFomatado = decimalFormat.format(resumoUsuario);//Pego o valor e tranformo em string no formato acima

                textoSaudacao.setText("Olá " + usuario.getNome() + "!");
                textoSaldo.setText("R$ " + resultadoFomatado);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void swipe(){
        ItemTouchHelper.Callback itCallback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {
                int draFlags = ItemTouchHelper.ACTION_STATE_IDLE; //Deixar eventos drag and drop inativos
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END; //Permite o swipe da esquerda para direita e vice versa
                return makeMovementFlags(draFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                excluirMovimentacao(viewHolder); //ViewHolder recupera a posição do item da lista
            }
        };

        new ItemTouchHelper(itCallback).attachToRecyclerView(recyclerView); //Adiciona o swipe no recycler view
    }

    public void excluirMovimentacao(final RecyclerView.ViewHolder viewHolder){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        //Configura AlertDialog
        alertDialog.setTitle("Excluir Movimentação da Conta");
        alertDialog.setMessage("Você tem certeza que deseja excluir esta movimentação da sua conta?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = viewHolder.getAdapterPosition(); //Recupera a posição do item deslisado
                movimentacao = movimentacaoList.get(position); //Recupera o objeto movimentação

                String emailUsuario = firebaseAuth.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64(emailUsuario); //codifico email utilizando base64
                movimentacaoReferencia = dataBaseRef.child("movimentacao")
                        .child(idUsuario) //Acesso às movimentacoes daquele usuario no firebase (acesso ao usuario por email codificado em base64)
                        .child(mesAnoSelecionado);//Acesso às movimentacoes do mêsAno selecionado

                movimentacaoReferencia.child(movimentacao.getMovKey()).removeValue(); //Remove item da lista no firebase
                adapter.notifyItemRemoved(position);//Atualiza lista sem item removido
                atualizarSaldo();
            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PrincipalActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged(); //Manter o item na lista após cancelar a caixa de dialog
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    public void atualizarSaldo(){
        userRef = ConfiguracaoFirebase.recuperarUsuario();
        if(movimentacao.getTipo().equals("r")){
            receitaTotal = receitaTotal - movimentacao.getValor();
            userRef.child("receitaTotal").setValue(receitaTotal);
        }else if(movimentacao.getTipo().equals("d")){
            despesaTotal = despesaTotal - movimentacao.getValor();
            userRef.child("despesaTotal").setValue(despesaTotal);
        }
    }

    @Override //Quando o usuário entrar no app o resumo será chamado no firebase
    protected void onStart() {
        super.onStart();
        recuperarResumo();
        recuperarMovimentacoes(); //Recupero as movimentações somente após o método do calendário ter sido chamado no onCreate
                                  //Assim as movimentações chamadas são sempre referentes ao mês escolhido
    }

    @Override //Quando o app não estiver sendo utilizado, o firebase não será chamado, o listener é removido
    protected void onStop() {
        super.onStop();
        userRef.removeEventListener(valueEventListenerUsuario);
        //movimentacaoReferencia.removeEventListener(valueEventListenerMovimentacoes);
    }
}

// Material Calendar View em: https://github.com/prolificinteractive/material-calendarview
