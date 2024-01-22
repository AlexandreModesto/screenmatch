package com.alura.screenmatch.dto;

import com.alura.screenmatch.models.Categoria;


public record SerieDTO(Long id,
                       String titulo,
                       Integer totalTemporadas,
                       Double avaliacao,
                       Categoria genero,
                       String atores,
                       String poster,
                       String sinopse){

}
