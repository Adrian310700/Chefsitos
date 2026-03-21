package com.chefsitos.uamishop.catalogo.controller.dto;

// Se usa como request y response
public record ImagenDTO(

    String url,
    String altText,
    Integer orden) {
}
