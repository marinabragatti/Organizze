package com.example.organizze.activity.helper;

import android.util.Base64;

public class Base64Custom {

    public static String codificarBase64(String texto){
        // Primeiro parâmetro texto em bytes(a conversão já será feita diretamente pelo método)
        // Segundo parâmetro tipo de codificação
        return Base64.encodeToString(texto.getBytes(), Base64.DEFAULT)
                .replaceAll("\\n|\\r", ""); //1°param caracteres a serem removidos(neste caso espaços no começo e ao final)
                                                              //2°param caracteres para repor os retirados(neste caso vazio)
    }

    public static String decodificarBase64(String textoCodificado){
        //O método .decode não retorna uma string, por isso será preciso criar uma new String
        return new String(Base64.decode(textoCodificado, Base64.DEFAULT));
    }
}
