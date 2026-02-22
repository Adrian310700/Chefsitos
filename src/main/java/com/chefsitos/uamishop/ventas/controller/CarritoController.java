package com.chefsitos.uamishop.ventas.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chefsitos.uamishop.ventas.controller.dto.AgregarProductoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.CarritoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.CarritoResponse;
import com.chefsitos.uamishop.ventas.controller.dto.ModificarCantidadRequest;
import com.chefsitos.uamishop.ventas.service.CarritoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

	private final CarritoService carritoService;

	public CarritoController(CarritoService carritoService) {
		this.carritoService = carritoService;
	}

	// crear carrito
	@PostMapping
	public ResponseEntity<CarritoResponse> crear(@Valid @RequestBody CarritoRequest request) {
		CarritoResponse response = carritoService.crear(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// buscar carrito activo
	@GetMapping("/{carritoId}")
	public ResponseEntity<CarritoResponse> obtenerCarrito(@PathVariable UUID carritoId) {
		CarritoResponse response = carritoService.obtenerCarrito(carritoId);
		return ResponseEntity.ok(response);
	} //

	// agregar producto al carrito
	@PostMapping("/{carritoId}/productos")
	public ResponseEntity<CarritoResponse> agregarProducto(
			@PathVariable UUID carritoId,
			@Valid @RequestBody AgregarProductoRequest request) {
		CarritoResponse response = carritoService.agregarProducto(carritoId, request);
		return ResponseEntity.ok(response);
	}

	// modificar cantidad de un producto en el carrito
	@PatchMapping("/{carritoId}/productos/{productoId}")
	public ResponseEntity<CarritoResponse> modificarCantidad(
			@PathVariable UUID carritoId,
			@PathVariable UUID productoId,
			@Valid @RequestBody ModificarCantidadRequest request) {
		CarritoResponse response = carritoService.modificarCantidad(carritoId, productoId, request);
		return ResponseEntity.ok(response);
	}

	// eliminar producto del carrito
	@DeleteMapping("/{carritoId}/productos/{productoId}")
	public ResponseEntity<CarritoResponse> eliminarProducto(
			@PathVariable UUID carritoId,
			@PathVariable UUID productoId) {
		CarritoResponse response = carritoService.eliminarProducto(carritoId, productoId);
		return ResponseEntity.ok(response);
	}

	// vaciar carrito
	@DeleteMapping("/{carritoId}/productos")
	public ResponseEntity<CarritoResponse> vaciar(@PathVariable UUID carritoId) {
		CarritoResponse response = carritoService.vaciar(carritoId);
		return ResponseEntity.ok(response);
	}

	// iniciar checkout
	@PostMapping("/{carritoId}/checkout")
	public ResponseEntity<CarritoResponse> iniciarCheckout(@PathVariable UUID carritoId) {
		CarritoResponse response = carritoService.iniciarCheckout(carritoId);
		return ResponseEntity.ok(response);
	}

	// completar checkout
	@PostMapping("/{carritoId}/checkout/completar")
	public ResponseEntity<CarritoResponse> completarCheckout(@PathVariable UUID carritoId) {
		CarritoResponse response = carritoService.completarCheckout(carritoId);
		return ResponseEntity.ok(response);
	}

	// abandonar carrito
	@PostMapping("/{carritoId}/abandonar")
	public ResponseEntity<CarritoResponse> abandonar(@PathVariable UUID carritoId) {
		CarritoResponse response = carritoService.abandonar(carritoId);
		return ResponseEntity.ok(response);
	}
}
