package com.chefsitos.uamishop.ordenes.controller;

import com.chefsitos.uamishop.ordenes.controller.dto.CancelarOrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.CrearOrdenDesdeCarritoRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.DireccionEnvioRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.InfoEnvioRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenResponseDTO;
import com.chefsitos.uamishop.ordenes.controller.dto.PagarOrdenRequest;
import com.chefsitos.uamishop.ordenes.domain.valueObject.DireccionEnvio;
import com.chefsitos.uamishop.ordenes.domain.valueObject.InfoEnvio;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ordenes.service.OrdenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.headers.Header;

import com.chefsitos.uamishop.shared.ApiErrors;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.lang.annotation.ElementType;

@RestController
@RequestMapping("/api/${api.V1}/ordenes")
@Tag(name = "Órdenes", description = "Operaciones relacionadas con órdenes")
@ApiErrors.GlobalErrorResponses
public class OrdenController {

  private final OrdenService ordenService;

  public OrdenController(OrdenService ordenService) {
    this.ordenService = ordenService;
  }

  @Operation(summary = "Crear orden (Manual/Directa)", description = "Crea una nueva orden a partir del request del cliente, sin usar carrito.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Orden creada exitosamente", headers = @Header(name = "Location", description = "URI del recurso creado", schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = OrdenResponseDTO.class)))
  })
  @ApiErrors.BadRequest
  @ApiErrors.UnprocessableEntity
  @PostMapping("/directa")
  public ResponseEntity<OrdenResponseDTO> crear(@Valid @RequestBody OrdenRequest request) {
    OrdenResponseDTO response = ordenService.crear(request);
    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(response.id())
        .toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(summary = "Crear orden desde carrito", description = "Convierte un carrito (en estado EN_CHECKOUT) en una Orden.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Orden creada exitosamente desde el carrito", headers = @Header(name = "Location", description = "URI del recurso creado", schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = OrdenResponseDTO.class)))
  })
  @ApiErrors.BadRequest
  @ApiErrors.NotFound
  @ApiErrors.UnprocessableEntity
  @PostMapping("/desde-carrito")
  public ResponseEntity<OrdenResponseDTO> crearDesdeCarrito(@Valid @RequestBody CrearOrdenDesdeCarritoRequest request) {
    DireccionEnvio direccionEnvio = new DireccionEnvio(
        request.direccion().nombreDestinatario(),
        request.direccion().calle(),
        request.direccion().ciudad(),
        request.direccion().estado(),
        request.direccion().codigoPostal(),
        "México",
        request.direccion().telefono(),
        request.direccion().instrucciones());

    OrdenResponseDTO response = ordenService.crearDesdeCarrito(CarritoId.of(request.carritoId().toString()),
        direccionEnvio);
    URI location = ServletUriComponentsBuilder
        .fromCurrentContextPath() // Utilizamos fromCurrentContextPath porque este endpoint es /desde-carrito
        .path("/api/v1/ordenes/{id}")
        .buildAndExpand(response.id())
        .toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(summary = "Listar órdenes", description = "Devuelve la lista de todas las órdenes")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrdenResponseDTO.class))))
  })
  @GetMapping
  public ResponseEntity<List<OrdenResponseDTO>> buscarTodas() {
    return ResponseEntity.ok(ordenService.buscarTodas());
  }

  @Operation(summary = "Buscar orden por ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Orden encontrada", content = @Content(schema = @Schema(implementation = OrdenResponseDTO.class)))
  })
  @ApiErrors.NotFound
  @GetMapping("/{id}")
  public ResponseEntity<OrdenResponseDTO> buscarPorId(@PathVariable UUID id) {
    var orden = ordenService.buscarPorId(id);
    OrdenResponseDTO response = OrdenService.mapToResponseDTO(orden);
    return ResponseEntity.ok(response);
  }

  // Acción de transición de estado: PENDIENTE → CONFIRMADA
  @Operation(summary = "Confirmar orden", description = "Confirma una orden en estado PENDIENTE")
  @ApiErrors.NotFound
  @ApiErrors.UnprocessableEntity
  @PostMapping("/{id}/confirmar")
  public ResponseEntity<OrdenResponseDTO> confirmar(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.confirmar(id));
  }

  // Acción de transición de estado: CONFIRMADA → PAGO_PROCESADO
  @Operation(summary = "Procesar pago", description = "Registra el pago de una orden CONFIRMADA")
  @ApiErrors.NotFound
  @ApiErrors.BadRequest
  @ApiErrors.UnprocessableEntity
  @PostMapping("/{id}/pago")
  public ResponseEntity<OrdenResponseDTO> procesarPago(
      @PathVariable UUID id,
      @Valid @RequestBody PagarOrdenRequest request) {
    return ResponseEntity.ok(ordenService.procesarPago(id, request.referenciaPago()));
  }

  // Acción de transición de estado: PAGO_PROCESADO → EN_PREPARACION
  @Operation(summary = "Marcar en preparación", description = "Marca una orden como en preparación (pago procesado)")
  @ApiErrors.NotFound
  @ApiErrors.UnprocessableEntity
  @PostMapping("/{id}/en-preparacion")
  public ResponseEntity<OrdenResponseDTO> marcarEnProceso(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.marcarEnProceso(id));
  }

  // Acción de transición de estado: EN_PREPARACION → ENVIADA
  @Operation(summary = "Marcar como enviada", description = "Marca una orden en preparación como enviada con datos de envío")
  @ApiErrors.NotFound
  @ApiErrors.BadRequest
  @ApiErrors.UnprocessableEntity
  @PostMapping("/{id}/enviada")
  public ResponseEntity<OrdenResponseDTO> marcarEnviada(
      @PathVariable UUID id,
      @Valid @RequestBody InfoEnvioRequest request) {
    InfoEnvio infoEnvio = new InfoEnvio(
        request.proveedorLogistico(),
        request.numeroGuia(),
        LocalDateTime.now().plusDays(5));
    return ResponseEntity.ok(ordenService.marcarEnviada(id, infoEnvio));
  }

  // Acción de transición de estado: ENVIADA | EN_TRANSITO → ENTREGADA
  @Operation(summary = "Marcar como entregada", description = "Marca una orden como entregada al cliente")
  @ApiErrors.NotFound
  @ApiErrors.UnprocessableEntity
  @PostMapping("/{id}/entregada")
  public ResponseEntity<OrdenResponseDTO> marcarEntregada(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.marcarEntregada(id));
  }

  @Operation(summary = "Cancelar orden", description = "Cancela una orden que aún no ha sido enviada ni entregada")
  @ApiErrors.NotFound
  @ApiErrors.BadRequest
  @ApiErrors.UnprocessableEntity
  @PostMapping("/{id}/cancelar")
  public ResponseEntity<OrdenResponseDTO> cancelar(
      @PathVariable UUID id,
      @Valid @RequestBody CancelarOrdenRequest request) {
    return ResponseEntity.ok(ordenService.cancelar(id, request.motivo()));
  }
}
