package com.chefsitos.uamishop.ordenes.domain.aggregate;

import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoPago;
import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Orden {
  private final OrdenId id;
  private final String numeroOrden;
  private final ClienteId clienteId;
  private final List<ItemOrden> items;
  private final DireccionEnvio direccionEnvio;
  private final LocalDateTime fechaCreacion;

  private EstadoOrden estado;
  private Money total;
  private ResumenPago resumenPago;
  private InfoEnvio infoEnvio;
  private List<CambioEstado> historialEstados;

  public Orden(OrdenId id, String numeroOrden, ClienteId clienteId,
               List<ItemOrden> items, DireccionEnvio direccionEnvio) {

    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException(" Una orden debe tener al menos un item"); // RN-ORD-01
    }

    this.id = id;
    this.numeroOrden = numeroOrden;
    this.clienteId = clienteId;
    this.items = new ArrayList<>(items);
    this.direccionEnvio = direccionEnvio;
    this.fechaCreacion = LocalDateTime.now();
    this.estado = EstadoOrden.PENDIENTE;
    this.historialEstados = new ArrayList<>();

    calcularTotal();
  }

  private void calcularTotal() {
    if (items.isEmpty()) return;
    String moneda = items.get(0).precioUnitario().moneda();
    Money suma = Money.zero(moneda);
    for (ItemOrden item : items) {
      suma = suma.sumar(item.calcularSubtotal());
    }
    if (!suma.esMayorQueCero()) {
      throw new IllegalArgumentException(" El total de la orden debe ser mayor a cero"); // RN-ORD-02
    }
    this.total = suma;
  }

  public void confirmar() {
    if (this.estado != EstadoOrden.PENDIENTE) {
      throw new IllegalStateException(" Solo se puede confirmar en estado pendiente"); // RN-ORD-05
    }
    registrarCambioEstado(EstadoOrden.CONFIRMADA, "Orden confirmada");
  }

  public void procesarPago(String referenciaPago) {
    if (this.estado != EstadoOrden.CONFIRMADA) {
      throw new IllegalStateException(" Solo se puede procesar pago si está confirmada"); // RN-ORD-07
    }
    if (referenciaPago == null || referenciaPago.isBlank()) {
      throw new IllegalArgumentException(" La referencia de pago no puede estar vacía"); // RN-ORD-08
    }

    // Se crea el resumen respetando el orden del Record: metodo, referencia, estado, fecha
    this.resumenPago = new ResumenPago("TARJETA", referenciaPago, EstadoPago.APROBADO, LocalDateTime.now());
    registrarCambioEstado(EstadoOrden.PAGO_PROCESADO, "Pago aprobado exitosamente!!");
  }

  public void marcarEnProceso() {
    if (this.estado != EstadoOrden.PAGO_PROCESADO) {
      throw new IllegalStateException(" Solo se puede marcar en proceso si el pago fue procesado"); // RN-ORD-09
    }
    registrarCambioEstado(EstadoOrden.EN_PREPARACION, "La orden ha entrado a almacén");
  }



  private void registrarCambioEstado(EstadoOrden nuevoEstado, String motivo) {
    // Se crea el cambio respetando el orden del Record: anterior, nuevo, fecha, motivo, usuario

    CambioEstado cambio = new CambioEstado(
      this.estado,
      nuevoEstado,
      LocalDateTime.now(),
      motivo,
      "SYSTEM"
    );
    this.historialEstados.add(cambio);
    this.estado = nuevoEstado;
  }

  // Para las pruebas
  public OrdenId getId() { return id; }
  public EstadoOrden getEstado() { return estado; }
  public Money getTotal() { return total; }


  // reglas faltantes
  // RN-ORD-10 a 12: Marcar como enviada y validar guía
  public void marcarEnviada(String guiaEnvio) {
    if (this.estado != EstadoOrden.EN_PREPARACION) {
      throw new IllegalStateException("RN-ORD-10: Solo se puede enviar si está EN_PREPARACION");
    }
    if (guiaEnvio == null || guiaEnvio.isBlank()) {
      throw new IllegalArgumentException("RN-ORD-11: La guía de envío es obligatoria");
    }
    if (guiaEnvio.length() < 10) {
      throw new IllegalArgumentException("RN-ORD-12: La guía debe tener al menos 10 caracteres");
    }

    this.infoEnvio = new InfoEnvio(guiaEnvio, "ESTAFETA", LocalDateTime.now()); // Simulado
    registrarCambioEstado(EstadoOrden.ENVIADA, "La orden ha sido despachada");
  }

  // RN-ORD-13: Marcar como entregada
  public void marcarEntregada() {
    if (this.estado != EstadoOrden.ENVIADA) {
      throw new IllegalStateException("RN-ORD-13: Solo se puede marcar entregada si ya fue ENVIADA");
    }
    registrarCambioEstado(EstadoOrden.ENTREGADA, "El cliente ha recibido el paquete");
  }

  public void cancelar(String motivo) {
    // RN-ORD-14: No se puede cancelar si ya se envió o entregó
    if (this.estado == EstadoOrden.ENVIADA || this.estado == EstadoOrden.ENTREGADA) {
      throw new IllegalStateException("RN-ORD-14: No se puede cancelar una orden enviada o entregada");
    }

    // RN-ORD-15 y 16: Validación de longitud del motivo
    if (motivo == null || motivo.isBlank()) {
      throw new IllegalArgumentException("RN-ORD-15: El motivo de cancelación es obligatorio");
    }
    if (motivo.trim().length() < 10) {
      throw new IllegalArgumentException("RN-ORD-16: El motivo debe tener al menos 10 caracteres");
    }

    registrarCambioEstado(EstadoOrden.CANCELADA, motivo);
  }





}
