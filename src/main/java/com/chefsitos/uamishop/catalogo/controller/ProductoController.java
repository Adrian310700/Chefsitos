package com.chefsitos.uamishop.catalogo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chefsitos.uamishop.catalogo.controller.dto.ProductoRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;
import com.chefsitos.uamishop.catalogo.service.ProductoService;
import com.chefsitos.uamishop.shared.ApiErrors;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/${api.V1}/productos")
@Tag(name = "Productos", description = "Endpoints para la gestión de productos del catálogo")
@ApiErrors.GlobalErrorResponses
public class ProductoController {

  @Autowired
  private ProductoService productoService;

  @Operation(summary = "Crear producto", description = "Permite registrar un nuevo producto en el catálogo")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Producto creado exitosamente", headers = @Header(name = "Location", description = "URI del producto creado (ej: /api/productos/{id})", schema = @Schema(type = "string", format = "uri")), content = @Content(schema = @Schema(implementation = ProductoResponse.class)))
  })
  @ApiErrors.BadRequest
  @ApiErrors.UnprocessableEntity
  @PostMapping
  public ResponseEntity<ProductoResponse> crear(@RequestBody @Valid ProductoRequest request) {
    ProductoResponse response = productoService.crear(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Obtener producto por ID", description = "Devuelve los detalles de un producto específico dado su ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Producto encontrado", content = @Content(schema = @Schema(implementation = ProductoResponse.class)))
  })
  @ApiErrors.NotFound
  @GetMapping("/{id}")
  public ResponseEntity<ProductoResponse> obtener(
      @Parameter(description = "ID único del producto") @PathVariable UUID id) {
    ProductoResponse response = productoService.buscarPorId(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Listar productos", description = "Devuelve la lista de todos los productos disponibles en el catálogo")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente")
  })
  @GetMapping
  public ResponseEntity<List<ProductoResponse>> buscarTodos() {
    List<ProductoResponse> productos = productoService.buscarTodos();
    return ResponseEntity.ok(productos);
  }

  @Operation(summary = "Actualizar producto", description = "Cambia el estado del producto a disponible en el catálogo")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Producto activado exitosamente", content = @Content(schema = @Schema(implementation = ProductoResponse.class)))
  })
  @ApiErrors.NotFound
  @ApiErrors.UnprocessableEntity
  @PostMapping("/{id}/activar")
  public ResponseEntity<ProductoResponse> actualizar(
      @Parameter(description = "ID único del producto") @PathVariable UUID id) {
    ProductoResponse response = productoService.activar(id);
    // Tambien se podria usar un status 204 para decir que se activo correctamente
    // pero no devolver nuevamente el producto
    // return ResponseEntity.noContent().build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Desactivar producto", description = "Cambia el estado del producto a no disponible en el catálogo")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Producto desactivado exitosamente", content = @Content(schema = @Schema(implementation = ProductoResponse.class)))
  })
  @ApiErrors.NotFound
  @ApiErrors.UnprocessableEntity
  @PostMapping("/{id}/desactivar")
  public ResponseEntity<ProductoResponse> desactivar(
      @Parameter(description = "ID único del producto") @PathVariable UUID id) {
    ProductoResponse response = productoService.desactivar(id);
    return ResponseEntity.ok(response);
  }

  // TODO: considerar las imagenes para poder activar el producto y diseño de

}
