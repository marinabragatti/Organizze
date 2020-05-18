package com.example.organizze.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.organizze.R;
import com.example.organizze.activity.config.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;

public class MainActivity extends IntroActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main); Não utilizo este layout qdo uso a biblioteca de Slides

        //Deixar os botões back e next invisíveis
        setButtonBackVisible(false);
        setButtonNextVisible(false);

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_1)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_2)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_3)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_4)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_cadastro)
                .canGoForward(false)
                .build());
    }

    // OnStart é chamado quando a Activity de Cadastro é fechada e voltamos para a MainActivity(que já executou o OnCreate anteriormente.
    // O método verificarUsuarioLogado é chamado, então o método abrirTelaPrincipal é chamado, que finalmente dá start em uma nova activity, PrincipalActivity.
    @Override
    protected void onStart() {
        super.onStart();
        verificarUsuarioLogado();
    }

    public void buttonCadastrar(View view){
        startActivity(new Intent(this, CadastroActivity.class));
    }

    public void buttonEntrar(View view){
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void verificarUsuarioLogado(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        //autenticacao.signOut();
        //Verifica se o usuário está logado
        if(autenticacao.getCurrentUser() != null){
            abrirTelaPrincipal();
        }
    }

    public void abrirTelaPrincipal(){
        startActivity(new Intent(this, PrincipalActivity.class));
    }
}

// BIBLIOTECAS
// SLIDE: https://github.com/heinrichreimer/material-intro
// FLOAT ACTION MENU: https://github.com/Clans/FloatingActionButton
