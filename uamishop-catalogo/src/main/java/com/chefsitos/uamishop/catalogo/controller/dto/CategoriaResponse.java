package com.chefsitos.uamishop.catalogo.controller.dto;

import java.util.UUID;

public record CategoriaResponse(

    UUID idCategoria,
    String nombreCategoria,
    String descripcion,
    UUID idCategoriaPadre

) {
}
