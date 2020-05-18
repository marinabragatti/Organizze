package com.example.organizze.activity.config;

import com.example.organizze.activity.helper.Base64Custom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracaoFirebase {

    //O atributo static será sempre o mesmo independente de quantas instâncias se crie da classe.
    //Se eu alterar o valor autenticacao em uma classe, o valor será alterado em todoo o projeto.
    private static FirebaseAuth autenticacao;
    private static DatabaseReference firebase;

    //Retorna a instânica do FirebaseDataBase (banco de dados)
    public static DatabaseReference getFirebaseData(){
        if(firebase == null){
            firebase = FirebaseDatabase.getInstance().getReference();
        }
        return firebase;
    }

    //Retorna a instânica do FirebaseAuth (login)
    public static FirebaseAuth getFirebaseAuth() {
        if (autenticacao == null) {
            autenticacao = FirebaseAuth.getInstance();
        }
        return autenticacao;
    }

    public static DatabaseReference recuperarUsuario(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();//Chamei os métodos acima para instanciar o usuario
        firebase = ConfiguracaoFirebase.getFirebaseData();//Chamei os métodos acima para instanciar dataBase
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebase.child("usuarios")
                .child(idUsuario);

        return usuarioRef; //Retorno o email do usuário codificado na Base64 (passível de conversão para email novamente)
    }
}
