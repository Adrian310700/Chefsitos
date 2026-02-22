package com.chefsitos.uamishop.ordenes.controller.dto;

import java.math.BigDecimal;

/**
 * DTO que representa un item dentro de la orden.
 */
public record ItemOrdenRequest(

    String productoId,
    String nombreProducto,
    String sku,
    int cantidad,
    BigDecimal precioUnitario

) {
}
