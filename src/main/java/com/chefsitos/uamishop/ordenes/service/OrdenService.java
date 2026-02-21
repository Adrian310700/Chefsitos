package com.chefsitos.uamishop.ordenes.service;

import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenResponseDTO;
import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;
import com.chefsitos.uamishop.ordenes.repository.OrdenJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrdenService {

  private static final String MONEDA_DEFAULT = "MXN";
  private final OrdenJpaRepository ordenRepository;

  public OrdenService(OrdenJpaRepository ordenRepository) {
    this.ordenRepository = ordenRepository;
  }

  public OrdenResponseDTO crear(OrdenRequest request) {
    // 1. Mapear Dirección de Envío (Value Object)
    DireccionEnvio direccion = new DireccionEnvio(
      request.direccion().nombreDestinatario(),
      request.direccion().calle(),
      request.direccion().ciudad(),
      request.direccion().estado(),
      request.direccion().codigoPostal(),
      request.direccion().pais(),
      request.direccion().telefono(),
      request.direccion().instrucciones()
    );

    // 2. Mapear Items (Entidades)
    List<ItemOrden> items = request.items().stream()
      .map(i -> new ItemOrden(
        ProductoId.of(i.productoId()),
        i.nombreProducto(),
        i.sku(),
        i.cantidad(),
        new Money(i.precioUnitario(), MONEDA_DEFAULT)
      ))
      .collect(Collectors.toList());

    // 3. Crear el Agregado (Lógica de dominio)
    Orden orden = Orden.crear(
      new ClienteId(request.clienteId()),
      items,
      direccion
    );

    // 4. Persistir y retornar DTO
    Orden guardada = ordenRepository.save(orden);
    return mapToResponseDTO(guardada);
  }

  @Transactional(readOnly = true)
  public OrdenResponseDTO buscarYConvertirDTO(UUID id) {
    return mapToResponseDTO(buscarPorIdInterno(id));
  }

  public OrdenResponseDTO confirmar(UUID id) {
    Orden orden = buscarPorIdInterno(id);
    orden.confirmar();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO marcarEnviada(UUID id, String numeroGuia, String proveedor) {
    Orden orden = buscarPorIdInterno(id);
    orden.marcarEnviada(numeroGuia, proveedor);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO procesarPago(UUID id, String referenciaPago) {
    Orden orden = buscarPorIdInterno(id);
    orden.procesarPago(referenciaPago);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO marcarEnProceso(UUID id) {
    Orden orden = buscarPorIdInterno(id);
    orden.marcarEnProceso();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO marcarEntregada(UUID id) {
    Orden orden = buscarPorIdInterno(id);
    orden.marcarEntregada();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO cancelar(UUID id, String motivo) {
    Orden orden = buscarPorIdInterno(id);
    orden.cancelar(motivo);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  private Orden buscarPorIdInterno(UUID id) {
    return ordenRepository.findById(new OrdenId(id))
      .orElseThrow(() -> new IllegalArgumentException("La orden con ID " + id + " no existe."));
  }

  // Mapper interno para no exponer el Agregado al Controller
  private OrdenResponseDTO mapToResponseDTO(Orden orden) {
    return new OrdenResponseDTO(
      orden.getId().valor(),
      orden.getNumeroOrden(),
      orden.getClienteId().valor(),
      orden.getEstado(),
      orden.getTotal() != null ? orden.getTotal().cantidad() : null,
      orden.getTotal() != null ? orden.getTotal().moneda() : MONEDA_DEFAULT,
      // Nota: Si DireccionEnvio es un record, usa .calle() y .ciudad()
      orden.getDireccionEnvio().calle() + ", " + orden.getDireccionEnvio().ciudad()
    );
  }
}
