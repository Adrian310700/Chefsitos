package com.chefsitos.uamishop.ordenes.domain.aggregate;

import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoPago;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.exception.BusinessRuleException;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "ordenes")
public class Orden {

  @EmbeddedId
  @AttributeOverride(name = "valor", column = @Column(name = "id"))
  private OrdenId id;

  @Column(name = "numero_orden", nullable = false, unique = true)
  private String numeroOrden;

  @Embedded
  @AttributeOverride(name = "valor", column = @Column(name = "cliente_id"))
  private ClienteId clienteId;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "orden_id", nullable = false)
  private List<ItemOrden> items;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "nombreDestinatario", column = @Column(name = "envio_nombre_destinatario")),
    @AttributeOverride(name = "calle", column = @Column(name = "envio_calle")),
    @AttributeOverride(name = "ciudad", column = @Column(name = "envio_ciudad")),
    @AttributeOverride(name = "estado", column = @Column(name = "envio_estado")),
    @AttributeOverride(name = "codigoPostal", column = @Column(name = "envio_cp")),
    @AttributeOverride(name = "pais", column = @Column(name = "envio_pais")),
    @AttributeOverride(name = "telefono", column = @Column(name = "envio_telefono")),
    @AttributeOverride(name = "instrucciones", column = @Column(name = "envio_instrucciones"))
  })
  private DireccionEnvio direccionEnvio;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "metodoPago", column = @Column(name = "pago_metodo")),
    @AttributeOverride(name = "referenciaExterna", column = @Column(name = "pago_referencia")),
    @AttributeOverride(name = "estado", column = @Column(name = "pago_estado")),
    @AttributeOverride(name = "fechaProcesamiento", column = @Column(name = "pago_fecha"))
  })
  private ResumenPago resumenPago;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "proveedorLogistico", column = @Column(name = "envio_proveedor")),
    @AttributeOverride(name = "numeroGuia", column = @Column(name = "envio_guia")),
    @AttributeOverride(name = "fechaEstimadaEntrega", column = @Column(name = "envio_fecha_estimada"))
  })
  private InfoEnvio infoEnvio;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "cantidad", column = @Column(name = "subtotal_monto")),
    @AttributeOverride(name = "moneda", column = @Column(name = "subtotal_moneda"))
  })
  private Money subtotal;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "cantidad", column = @Column(name = "descuento_monto")),
    @AttributeOverride(name = "moneda", column = @Column(name = "descuento_moneda"))
  })
  private Money descuento;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "cantidad", column = @Column(name = "total_monto")),
    @AttributeOverride(name = "moneda", column = @Column(name = "total_moneda"))
  })
  private Money total;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_orden", nullable = false)
  private EstadoOrden estado;

  @Column(name = "fecha_creacion", nullable = false)
  private LocalDateTime fechaCreacion;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "orden_historial_estados", joinColumns = @JoinColumn(name = "orden_id"))
  private List<CambioEstado> historialEstados;

  private Orden() {
  }

  public static Orden crear(ClienteId clienteId, List<ItemOrden> items, DireccionEnvio direccionEnvio,
                            ResumenPago resumenPago) {
    if (items == null || items.isEmpty()) {
      throw new BusinessRuleException("RN-ORD-01", "La orden debe tener al menos un item.");
    }

    Orden orden = new Orden();
    orden.id = OrdenId.generar();
    orden.numeroOrden = "ORD-" + LocalDateTime.now().getYear() + "-"
      + orden.id.valor().toString().substring(0, 6).toUpperCase();
    orden.clienteId = clienteId;
    orden.items = new ArrayList<>(items);
    orden.direccionEnvio = direccionEnvio;
    orden.resumenPago = resumenPago;
    orden.fechaCreacion = LocalDateTime.now();
    orden.historialEstados = new ArrayList<>();

    orden.estado = EstadoOrden.PENDIENTE;
    orden.infoEnvio = null;

    orden.calcularTotales();
    orden.registrarCambioEstado(EstadoOrden.PENDIENTE, "Orden creada", "SISTEMA");

    return orden;
  }

  public static Orden crear(ClienteId clienteId, List<ItemOrden> items, DireccionEnvio direccionEnvio) {
    return crear(clienteId, items, direccionEnvio, null);
  }

  private void calcularTotales() {
    if (items.isEmpty())
      return;

    String moneda = items.get(0).getPrecioUnitario().moneda();

    Money subtotalTemp = Money.zero(moneda);
    for (ItemOrden item : items) {
      subtotalTemp = subtotalTemp.sumar(item.calcularSubtotal());
    }
    this.subtotal = subtotalTemp;
    this.descuento = Money.zero(moneda);
    this.total = this.subtotal.restar(this.descuento);

    if (!this.total.esMayorQueCero()) {
      throw new BusinessRuleException("RN-ORD-02", "El total de la orden debe ser mayor a cero.");
    }
  }

  public void confirmar() {
    if (obtenerEstadoActual() != EstadoOrden.PENDIENTE) {
      throw new BusinessRuleException("RN-ORD-05", "Solo se puede confirmar una orden en estado PENDIENTE.");
    }
    registrarCambioEstado(EstadoOrden.CONFIRMADA, "Orden confirmada por el usuario", "CLIENTE");
  }

  public void procesarPago(String referenciaPago) {
    if (obtenerEstadoActual() != EstadoOrden.CONFIRMADA) {
      throw new BusinessRuleException("RN-ORD-07", "Solo se puede procesar pago si la orden está CONFIRMADA.");
    }
    if (referenciaPago == null || referenciaPago.isBlank()) {
      throw new BusinessRuleException("RN-ORD-08", "La referencia de pago no puede estar vacía.");
    }

    this.resumenPago = new ResumenPago("TARJETA", referenciaPago, EstadoPago.APROBADO, LocalDateTime.now());
    registrarCambioEstado(EstadoOrden.PAGO_PROCESADO, "Pago aprobado. Ref: " + referenciaPago, "PASARELA_PAGOS");
  }

  public void marcarEnProceso() {
    if (obtenerEstadoActual() != EstadoOrden.PAGO_PROCESADO) {
      throw new BusinessRuleException("RN-ORD-09", "Solo se puede marcar en proceso si el pago fue procesado.");
    }
    registrarCambioEstado(EstadoOrden.EN_PREPARACION, "Orden enviada a almacén para preparación", "ALMACEN");
  }

  public void marcarEnviada(String guiaEnvio, String proveedor) {
    if (obtenerEstadoActual() != EstadoOrden.EN_PREPARACION) {
      throw new BusinessRuleException("RN-ORD-10", "Solo se puede marcar enviada si está EN_PREPARACION.");
    }
    if (guiaEnvio == null || guiaEnvio.length() < 10) {
      throw new BusinessRuleException("RN-ORD-11", "El número de guía debe tener al menos 10 caracteres.");
    }

    this.infoEnvio = new InfoEnvio(proveedor, guiaEnvio, LocalDateTime.now().plusDays(5));
    registrarCambioEstado(EstadoOrden.ENVIADA, "Orden despachada con guía: " + guiaEnvio, "LOGISTICA");
  }

  public void marcarEnviada(InfoEnvio infoEnvio) {
    if (obtenerEstadoActual() != EstadoOrden.EN_PREPARACION) {
      throw new BusinessRuleException("RN-ORD-10", "Solo se puede marcar enviada si está EN_PREPARACION.");
    }
    this.infoEnvio = infoEnvio;
    registrarCambioEstado(EstadoOrden.ENVIADA,
      "Orden despachada con guía: " + infoEnvio.numeroGuia(), "LOGISTICA");
  }

  public void marcarEnTransito() {
    if (obtenerEstadoActual() != EstadoOrden.ENVIADA) {
      throw new BusinessRuleException("RN-ORD-13", "Solo se puede marcar en tránsito si está ENVIADA.");
    }
    registrarCambioEstado(EstadoOrden.EN_TRANSITO, "Paquete en tránsito", "LOGISTICA");
  }

  public void marcarEntregada() {
    EstadoOrden estadoActual = obtenerEstadoActual();
    if (estadoActual != EstadoOrden.ENVIADA && estadoActual != EstadoOrden.EN_TRANSITO) {
      throw new BusinessRuleException("RN-ORD-13", "Solo se puede marcar entregada si está ENVIADA o EN_TRANSITO.");
    }
    registrarCambioEstado(EstadoOrden.ENTREGADA, "Paquete entregado al cliente", "LOGISTICA");
  }

  public void cancelar(String motivo, String usuario) {
    EstadoOrden estadoActual = obtenerEstadoActual();
    if (estadoActual == EstadoOrden.ENVIADA || estadoActual == EstadoOrden.EN_TRANSITO
      || estadoActual == EstadoOrden.ENTREGADA) {
      throw new BusinessRuleException("RN-ORD-14", "No se puede cancelar una orden que ya ha sido enviada o entregada.");
    }
    if (motivo == null || motivo.trim().length() < 10) {
      throw new BusinessRuleException("RN-ORD-15", "El motivo de cancelación debe tener al menos 10 caracteres.");
    }

    registrarCambioEstado(EstadoOrden.CANCELADA, motivo, usuario);
  }

  public void cancelar(String motivo) {
    cancelar(motivo, "CLIENTE");
  }

  private void registrarCambioEstado(EstadoOrden nuevoEstado, String motivo, String usuario) {
    CambioEstado cambio = new CambioEstado(
      obtenerEstadoActual(),
      nuevoEstado,
      LocalDateTime.now(),
      motivo,
      usuario);

    this.historialEstados.add(cambio);
    this.estado = nuevoEstado;
  }

  public OrdenId getId() { return id; }
  public String getNumeroOrden() { return numeroOrden; }
  public EstadoOrden getEstado() { return estado; }
  public Money getTotal() { return total; }
  public List<CambioEstado> obtenerHistorial() { return Collections.unmodifiableList(historialEstados); }
  public EstadoOrden obtenerEstadoActual() { return this.estado; }
  public List<ItemOrden> getItems() { return items; }
  public DireccionEnvio getDireccionEnvio() { return direccionEnvio; }
  public ClienteId getClienteId() { return clienteId; }
}
