package com.example.organizze.activity.model;

import com.example.organizze.activity.config.ConfiguracaoFirebase;
import com.example.organizze.activity.helper.Base64Custom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class Movimentacao {

    private String data;
    private String categoria;
    private String descricao;
    private String tipo;
    private double valor;
    private String movKey;

    public Movimentacao() {
    }

    //Método para recuperar mês e ano em formato String sem /(barra) para salvar no firebase
    public static String mesAnoEscolhido(String data){
        String retornoData[] = data.split("/");
        String mes = retornoData[1];
        String ano = retornoData[2];
        String mesAno = mes + ano;
        return mesAno;
    }

    public void salvar(String data){
        //Mês e ano em formato String sem /(barra) para salvar no firebase
        String mesAno = mesAnoEscolhido(data);

        //Recuperar os dados de autenticação do usuário
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAuth();

        //A partir da autenticação recupero os dados do user atual e pego seu email para utilizar no método salvar abaixo
        String idUsuario = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());

        //Referência do dataBase do Firebase
        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseData();
        firebase.child("movimentacao")
                .child(idUsuario)
                .child(mesAno)
                .push() //gera um id prórpio da receita/despesa no firebase
                .setValue(this);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getMovKey() {
        return movKey;
    }

    public void setMovKey(String movKey) {
        this.movKey = movKey;
    }
}
