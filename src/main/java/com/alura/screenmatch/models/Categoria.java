package com.alura.screenmatch.models;

public enum Categoria {
    ACAO("Action","Ação"),
    ROMANCE("Romance","Romance"),
    COMEDIA("Comedy","Comedia"),
    DRAMA("Drama","Drama"),
    CRIME("Crime","Crime"),
    ANIMACAO("Animation","Animação"),
    AVENTURA("Adventure","Aventura");

    private String categoriaOmdb;

    private String categoriaDoUsuario;

    Categoria(String categoriaOmdb, String categoriaDoUsuario){
        this.categoriaOmdb=categoriaOmdb;
        this.categoriaDoUsuario=categoriaDoUsuario;
    }

    public static Categoria fromPortugues(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.categoriaOmdb.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
    }

    public static Categoria fromPrompt(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.categoriaDoUsuario.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para o genero fornecido: " + text);
    }

}
