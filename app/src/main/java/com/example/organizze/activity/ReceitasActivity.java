package com.example.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.organizze.R;
import com.example.organizze.activity.config.ConfiguracaoFirebase;
import com.example.organizze.activity.helper.Base64Custom;
import com.example.organizze.activity.model.Movimentacao;
import com.example.organizze.activity.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ReceitasActivity extends AppCompatActivity {

    private TextInputEditText editData, editCategoria, editDescricao;
    private EditText valorReceita;

    private Calendar calendar;
    private DatePickerDialog datePickerDialog;
    private Movimentacao movimentacao;
    private Double receitaTotal;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        editData = findViewById(R.id.editData);
        editCategoria = findViewById(R.id.editCategoria);
        editDescricao = findViewById(R.id.editDescricao);
        valorReceita = findViewById(R.id.valorReceita);

        //Abre o DatePicker para escolher data -> colocar focusable:false no xml para usuário não digitar no campo
        editData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                datePickerDialog = new DatePickerDialog(ReceitasActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editData.setText(dayOfMonth + "/" + (month+1) + "/" + year);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }

        });

        recuperarReceitaTotal();

    }

    public void salvarReceita(View view){

        if(validarCamposReceita()){
            String data = editData.getText().toString();
            Double valorRecuperado = Double.parseDouble(valorReceita.getText().toString());
            movimentacao = new Movimentacao();
            movimentacao.setValor(valorRecuperado);
            movimentacao.setCategoria(editCategoria.getText().toString());
            movimentacao.setDescricao(editDescricao.getText().toString());
            movimentacao.setData(data);
            movimentacao.setTipo("r");
            movimentacao.mesAnoEscolhido(data);

            Double receitaAtualizada = receitaTotal + valorRecuperado; //Valor preenchido pelo usuário na tela de despesa + valor total no firebase
            atualizarReceita(receitaAtualizada);

            movimentacao.salvar(data);

            finish();
        }
    }

    //Verifica se todos os campos estão preenchidos
    public boolean validarCamposReceita(){
        String textoData = editData.getText().toString();
        String textoDescricao = editDescricao.getText().toString();
        String textoCategoria = editCategoria.getText().toString();
        String textoValor = valorReceita.getText().toString();

        if(!textoValor.isEmpty()){
            if(!textoData.isEmpty()){
                if(!textoCategoria.isEmpty()){
                    if(!textoDescricao.isEmpty()){
                        return true;
                    }else{
                        Toast.makeText(ReceitasActivity.this, "Preencha a descrição!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    Toast.makeText(ReceitasActivity.this, "Preencha a categoria!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else{
                Toast.makeText(ReceitasActivity.this, "Preencha a data!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(ReceitasActivity.this, "Preencha o valor!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //Recuperar o valor total de despesas que está no firebase
    public void recuperarReceitaTotal(){
        userRef = ConfiguracaoFirebase.recuperarUsuario();
        userRef.addValueEventListener(new ValueEventListener() { //Método acionado toda vez que dados são alterados
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Vai converter o retorno do firebase em um objeto Usuario. Vamos passar Usuario.class para o getValue e ele retornará o objeto Usuario já preenchido
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                receitaTotal = usuario.getReceitaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void atualizarReceita(Double receita){
        userRef = ConfiguracaoFirebase.recuperarUsuario();
        userRef.child("receitaTotal").setValue(receita);
    }
}
