package com.chefsitos.uamishop.ordenes.controller.dto;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import java.util.List;
import java.util.UUID;

public record OrdenRequest(
    UUID clienteId,
    // Datos de Dirección desglosados
    String nombreDestinatario,
    String calle,
    String ciudad,
    String estado,
    String codigoPostal,
    String pais,
    String telefono,
    String instrucciones,
    // Items
    List<ItemOrdenRequest> items
) {
    public record ItemOrdenRequest(
        String productoId,
        String nombreProducto,
        String sku,
        int cantidad,
        Money precioUnitario
    ) {}
}
