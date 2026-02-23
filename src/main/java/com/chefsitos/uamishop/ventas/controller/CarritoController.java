package com.chefsitos.uamishop.ventas.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.chefsitos.uamishop.shared.ApiErrors;
import com.chefsitos.uamishop.ventas.controller.dto.AgregarProductoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.CarritoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.CarritoResponse;
import com.chefsitos.uamishop.ventas.controller.dto.ModificarCantidadRequest;
import com.chefsitos.uamishop.ventas.service.CarritoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/${api.V1}/carritos")
@Tag(name = "Carritos", description = "Endpoints para la gestión del carrito de compras")
@ApiErrors.GlobalErrorResponses
public class CarritoController {

	private final CarritoService carritoService;

	@Autowired
	public CarritoController(CarritoService carritoService) {
		this.carritoService = carritoService;
	}

	@Operation(summary = "Crear carrito", description = "Crea un nuevo carrito para el cliente indicado")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Carrito creado exitosamente", headers = @Header(name = "Location", description = "URI del recurso creado (ej: /api/carritos/{id})", schema = @Schema(type = "string", format = "uri")), content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
	})
	@ApiErrors.BadRequest
	@ApiErrors.UnprocessableEntity
	@PostMapping
	public ResponseEntity<CarritoResponse> crear(@Valid @RequestBody CarritoRequest request) {
		CarritoResponse response = carritoService.crear(request);

		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.carritoId())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@Operation(summary = "Obtener carrito por ID", description = "Devuelve el estado actual de un carrito buscando por su id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Carrito encontrado", content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
	})
	@ApiErrors.NotFound
	@GetMapping("/{carritoId}")
	public ResponseEntity<CarritoResponse> obtenerCarrito(
			@Parameter(description = "ID único del carrito") @PathVariable UUID carritoId) {
		CarritoResponse response = carritoService.obtenerCarrito(carritoId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Agregar producto al carrito", description = "Agregar un producto al carrito segun la cantidad indicada")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Producto agregado exitosamente", content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
	})
	@ApiErrors.BadRequest
	@ApiErrors.NotFound
	@ApiErrors.UnprocessableEntity
	@PostMapping("/{carritoId}/productos")
	public ResponseEntity<CarritoResponse> agregarProducto(
			@Parameter(description = "ID único del carrito") @PathVariable UUID carritoId,
			@Valid @RequestBody AgregarProductoRequest request) {
		CarritoResponse response = carritoService.agregarProducto(carritoId, request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Modificar cantidad de un producto", description = "Actualiza la cantidad de un producto dentro del carrito. Si es 0 se elimina")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Cantidad modificada exitosamente", content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
	})
	@ApiErrors.BadRequest
	@ApiErrors.NotFound
	@ApiErrors.UnprocessableEntity
	@PatchMapping("/{carritoId}/productos/{productoId}")
	public ResponseEntity<CarritoResponse> modificarCantidad(
			@Parameter(description = "ID del carrito") @PathVariable UUID carritoId,
			@Parameter(description = "ID del producto a modificar") @PathVariable UUID productoId,
			@Valid @RequestBody ModificarCantidadRequest request) {
		CarritoResponse response = carritoService.modificarCantidad(carritoId, productoId, request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Eliminar producto del carrito", description = "Elimina un producto específico del carrito de compras")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente", content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
	})
	@ApiErrors.NotFound
	@DeleteMapping("/{carritoId}/productos/{productoId}")
	public ResponseEntity<CarritoResponse> eliminarProducto(
			@Parameter(description = "ID único del carrito") @PathVariable UUID carritoId,
			@Parameter(description = "ID único del producto a eliminar") @PathVariable UUID productoId) {
		CarritoResponse response = carritoService.eliminarProducto(carritoId, productoId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Vaciar carrito", description = "Elimina todos los productos del carrito, dejándolo vacío")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Carrito vaciado exitosamente", content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
	})
	@ApiErrors.NotFound
	@DeleteMapping("/{carritoId}/productos")
	public ResponseEntity<CarritoResponse> vaciar(
			@Parameter(description = "ID único del carrito a vaciar") @PathVariable UUID carritoId) {
		CarritoResponse response = carritoService.vaciar(carritoId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Iniciar checkout", description = "Cambia el estado del carrito a 'EN_CHECKOUT'")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Checkout iniciado exitosamente", content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
	})
	@ApiErrors.NotFound
	@ApiErrors.UnprocessableEntity
	@PostMapping("/{carritoId}/checkout")
	public ResponseEntity<CarritoResponse> iniciarCheckout(
			@Parameter(description = "ID único del carrito") @PathVariable UUID carritoId) {
		CarritoResponse response = carritoService.iniciarCheckout(carritoId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Completar checkout", description = "Marca el carrito como 'COMPLETADO'")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Checkout completado", content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
	})
	@ApiErrors.NotFound
	@ApiErrors.UnprocessableEntity
	@PostMapping("/{carritoId}/checkout/completar")
	public ResponseEntity<CarritoResponse> completarCheckout(
			@Parameter(description = "ID único del carrito") @PathVariable UUID carritoId) {
		CarritoResponse response = carritoService.completarCheckout(carritoId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Abandonar carrito", description = "Cambia el estado del carrito a 'ABANDONADO'")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Carrito abandonado", content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
	})
	@ApiErrors.NotFound
	@ApiErrors.UnprocessableEntity
	@PostMapping("/{carritoId}/abandonar")
	public ResponseEntity<CarritoResponse> abandonar(
			@Parameter(description = "ID único del carrito") @PathVariable UUID carritoId) {
		CarritoResponse response = carritoService.abandonar(carritoId);
		return ResponseEntity.ok(response);
	}
}
