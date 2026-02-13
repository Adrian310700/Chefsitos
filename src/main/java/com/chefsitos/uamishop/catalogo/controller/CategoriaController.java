package com.chefsitos.uamishop.catalogo.controller;

import java.util.UUID;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chefsitos.uamishop.catalogo.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/categorias")

public class CategoriaController {

  @Autowired
  ProductoService productoService;

  @PostMapping
  public ResponseEntity<CategoriaResponse> crearCategoria(@RequestBody @Valid CategoriaRequest request) {
    CategoriaResponse response = productoService.crearCategoria(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoriaResponse> buscarCategoriaPorId(@PathVariable UUID id) {

    CategoriaResponse response = productoService.buscarCategoriaPorId(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<CategoriaResponse>> buscarTodasCategorias() {

    List<CategoriaResponse> categorias = productoService.buscarTodasCategorias();
    return ResponseEntity.ok(categorias);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoriaResponse> actualizarCategoria(
      @PathVariable UUID id,
      @RequestBody @Valid CategoriaRequest request) {

    categoriaResponse response = productoService.actualizarCategoria(id, request);

    return ResponseEntity.ok(response);
  }
}
