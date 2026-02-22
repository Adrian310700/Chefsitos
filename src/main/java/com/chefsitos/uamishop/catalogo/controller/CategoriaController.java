package com.chefsitos.uamishop.catalogo.controller;

import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaResponse;
import com.chefsitos.uamishop.catalogo.service.ProductoService;
import com.chefsitos.uamishop.shared.ApiErrors;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/${api.V1}/categorias")
@Tag(name = "Categorías", description = "Endpoints para la gestión de categorías del catálogo")
@ApiErrors.GlobalErrorResponses
public class CategoriaController {

  @Autowired
  ProductoService productoService;

  @Operation(summary = "Crear categoría", description = "Permite registrar una nueva categoría en el sistema")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente", headers = @Header(name = "Location", description = "URI del recurso creado (ej: /api/categorias/{id})", schema = @Schema(type = "string", format = "uri")), content = @Content(schema = @Schema(implementation = CategoriaResponse.class)))
  })
  @ApiErrors.BadRequest
  @ApiErrors.UnprocessableEntity
  @PostMapping
  public ResponseEntity<CategoriaResponse> crearCategoria(@RequestBody @Valid CategoriaRequest request) {
    CategoriaResponse response = productoService.crearCategoria(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Obtener categoría por ID", description = "Devuelve la información de una categoría específica buscando por su identificador único.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content(schema = @Schema(implementation = CategoriaResponse.class)))
  })
  @ApiErrors.NotFound
  @GetMapping("/{id}")
  public ResponseEntity<CategoriaResponse> buscarCategoriaPorId(
      @Parameter(description = "ID único de la categoría a buscar") @PathVariable UUID id) {

    CategoriaResponse response = productoService.buscarCategoriaPorId(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Listar categorías", description = "Obtiene la lista de todas las categorías registradas en el catálogo.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente")
  })
  @GetMapping
  public ResponseEntity<List<CategoriaResponse>> buscarTodasCategorias() {

    List<CategoriaResponse> categorias = productoService.buscarTodasCategorias();
    return ResponseEntity.ok(categorias);
  }

  @Operation(summary = "Actualizar categoría", description = "Modifica los datos (nombre, descripción, categoría padre) de una categoría existente.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente", content = @Content(schema = @Schema(implementation = CategoriaResponse.class)))
  })
  @ApiErrors.BadRequest
  @ApiErrors.NotFound
  @ApiErrors.UnprocessableEntity
  @PutMapping("/{id}")
  public ResponseEntity<CategoriaResponse> actualizarCategoria(
      @Parameter(description = "ID único de la categoría a actualizar") @PathVariable UUID id,
      @RequestBody @Valid CategoriaRequest request) {

    CategoriaResponse response = productoService.actualizarCategoria(id, request);

    return ResponseEntity.ok(response);
  }
}
