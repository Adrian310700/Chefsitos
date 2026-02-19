package com.chefsitos.uamishop.catalogo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chefsitos.uamishop.catalogo.controller.dto.CambioEstadoRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;
import com.chefsitos.uamishop.catalogo.service.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

  @Autowired
  private ProductoService productoService;

  @PostMapping
  public ResponseEntity<ProductoResponse> crear(@RequestBody @Valid ProductoRequest request) {
    ProductoResponse response = productoService.crear(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductoResponse> obtener(@PathVariable UUID id) {
    ProductoResponse response = productoService.buscarPorId(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<ProductoResponse>> buscarTodos() {
    List<ProductoResponse> productos = productoService.buscarTodos();
    return ResponseEntity.ok(productos);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductoResponse> actualizar(
      @PathVariable UUID id,
      @RequestBody @Valid ProductoRequest request) {
    ProductoResponse response = productoService.actualizar(id, request);
    return ResponseEntity.ok(response);
  }

  // TODO: considerar las imagenes para poder activar el producto y diseño de
  // endpoint
  // Sub-recurso: estado activo del producto
  @PatchMapping("/{id}/activo")
  public ResponseEntity<ProductoResponse> cambiarEstado(
      @PathVariable UUID id,
      @RequestBody @Valid CambioEstadoRequest request) {

    ProductoResponse response = request.activo()
        ? productoService.activar(id)
        : productoService.desactivar(id);

    return ResponseEntity.ok(response);
  }
}
