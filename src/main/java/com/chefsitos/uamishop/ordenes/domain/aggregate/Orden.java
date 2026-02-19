package com.chefsitos.uamishop.ordenes.domain.aggregate;

import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoPago;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;

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

  // Relación OneToMany con la entidad hija ItemOrden.
  // CascadeType.ALL: Si guardo Orden, se guardan los Items.
  // orphanRemoval: Si quito un item de la lista, se borra de la BD.
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
      @AttributeOverride(name = "estadoPago", column = @Column(name = "pago_estado")),
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

  // Manejo de Dinero (Subtotal, Descuento, Total)
  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "valor", column = @Column(name = "subtotal_monto")),
      @AttributeOverride(name = "moneda", column = @Column(name = "subtotal_moneda"))
  })
  private Money subtotal;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "valor", column = @Column(name = "descuento_monto")),
      @AttributeOverride(name = "moneda", column = @Column(name = "descuento_moneda"))
  })
  private Money descuento;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "valor", column = @Column(name = "total_monto")),
      @AttributeOverride(name = "moneda", column = @Column(name = "total_moneda"))
  })
  private Money total;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false)
  private EstadoOrden estado;

  @Column(name = "fecha_creacion", nullable = false)
  private LocalDateTime fechaCreacion;

  // Historial como colección de elementos
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "orden_historial_estados", joinColumns = @JoinColumn(name = "orden_id"))
  private List<CambioEstado> historialEstados;

  private Orden() {
  }

  // Constructor privado para Factory Method
  private Orden(OrdenId id, String numeroOrden, ClienteId clienteId, List<ItemOrden> items,
      DireccionEnvio direccionEnvio) {
    this.id = id;
    this.numeroOrden = numeroOrden;
    this.clienteId = clienteId;
    this.items = items != null ? items : new ArrayList<>();
    this.direccionEnvio = direccionEnvio;
    this.fechaCreacion = LocalDateTime.now();
    this.historialEstados = new ArrayList<>();

    // Estado inicial
    this.estado = EstadoOrden.PENDIENTE;
    this.resumenPago = null;
    this.infoEnvio = null;

    calcularTotales();

    // Registrar el primer estado en el historial
    registrarCambioEstado(EstadoOrden.PENDIENTE, "Orden creada", "SISTEMA");
  }

  public static Orden crear(ClienteId clienteId, List<ItemOrden> items, DireccionEnvio direccionEnvio) {
    // RN-ORD-01: Una orden debe tener al menos un item
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("La orden debe tener al menos un item.");
    }

    OrdenId id = OrdenId.generar();
    // Generación de numero de orden
    String numeroOrden = "ORD-" + LocalDateTime.now().getYear() + "-"
        + id.valor().toString().substring(0, 6).toUpperCase();

    Orden nuevaOrden = new Orden(id, numeroOrden, clienteId, items, direccionEnvio);

    // RN-ORD-02: El total de la orden debe ser mayor a cero
    return nuevaOrden;
  }

  private void calcularTotales() {
    if (items.isEmpty())
      return;

    // Asumimos que todos los items tienen la misma moneda, tomamos la del primero
    String moneda = items.get(0).getPrecioUnitario().moneda();

    // Calcular Subtotal
    Money subtotalTemp = Money.zero(moneda);
    for (ItemOrden item : items) {
      subtotalTemp = subtotalTemp.sumar(item.calcularSubtotal());
    }
    this.subtotal = subtotalTemp;

    // Calcular Descuento
    // TODO: falta decidir si es porcentaje o monto
    this.descuento = Money.zero(moneda);

    // 3. Calcular Total
    this.total = this.subtotal.restar(this.descuento);

    // Validar RN-ORD-02
    if (!this.total.esMayorQueCero()) {
      throw new IllegalStateException("RN-ORD-02: El total de la orden debe ser mayor a cero.");
    }
  }

  public void confirmar() {
    // RN-ORD-05
    if (obtenerEstadoActual() != EstadoOrden.PENDIENTE) {
      throw new IllegalStateException("RN-ORD-05: Solo se puede confirmar una orden en estado PENDIENTE.");
    }
    // RN-ORD-06
    registrarCambioEstado(EstadoOrden.CONFIRMADA, "Orden confirmada por el usuario", "CLIENTE");
  }

  public void procesarPago(String referenciaPago) {
    // RN-ORD-07
    if (obtenerEstadoActual() != EstadoOrden.CONFIRMADA) {
      throw new IllegalStateException("RN-ORD-07: Solo se puede procesar pago si la orden está CONFIRMADA.");
    }
    // RN-ORD-08
    if (referenciaPago == null || referenciaPago.isBlank()) {
      throw new IllegalArgumentException("RN-ORD-08: La referencia de pago no puede estar vacía.");
    }

    this.resumenPago = new ResumenPago("TARJETA", referenciaPago, EstadoPago.APROBADO, LocalDateTime.now());
    registrarCambioEstado(EstadoOrden.PAGO_PROCESADO, "Pago aprobado. Ref: " + referenciaPago, "PASARELA_PAGOS");
  }

  public void marcarEnProceso() {
    // RN-ORD-09
    if (obtenerEstadoActual() != EstadoOrden.PAGO_PROCESADO) {
      throw new IllegalStateException("RN-ORD-09: Solo se puede marcar en proceso si el pago fue procesado.");
    }
    registrarCambioEstado(EstadoOrden.EN_PREPARACION, "Orden enviada a almacén para preparación", "ALMACEN");
  }

  public void marcarEnviada(String guiaEnvio, String proveedor) {
    // RN-ORD-10
    if (obtenerEstadoActual() != EstadoOrden.EN_PREPARACION) {
      throw new IllegalStateException("Solo se puede marcar enviada si está EN_PREPARACION.");
    }
    // RN-ORD-11 y RN-ORD-12 (Validaciones de longitud de guía)
    if (guiaEnvio == null || guiaEnvio.length() < 10) {
      throw new IllegalArgumentException("El número de guía debe tener al menos 10 caracteres.");
    }

    this.infoEnvio = new InfoEnvio(proveedor, guiaEnvio, LocalDateTime.now().plusDays(5)); // Fecha estimada ejemplo
    registrarCambioEstado(EstadoOrden.ENVIADA, "Orden despachada con guía: " + guiaEnvio, "LOGISTICA");
  }

  public void marcarEntregada() {
    // RN-ORD-13
    if (obtenerEstadoActual() != EstadoOrden.ENVIADA) {
      throw new IllegalStateException("Solo se puede marcar entregada si está ENVIADA.");
    }
    registrarCambioEstado(EstadoOrden.ENTREGADA, "Paquete entregado al cliente", "LOGISTICA");
  }

  public void cancelar(String motivo, String usuario) {
    // RN-ORD-14
    if (obtenerEstadoActual() == EstadoOrden.ENVIADA || obtenerEstadoActual() == EstadoOrden.ENTREGADA) {
      throw new IllegalStateException("No se puede cancelar una orden que ya ha sido enviada o entregada.");
    }
    // RN-ORD-15 y RN-ORD-16
    if (motivo == null || motivo.trim().length() < 10) {
      throw new IllegalArgumentException("El motivo de cancelación debe tener al menos 10 caracteres.");
    }

    registrarCambioEstado(EstadoOrden.CANCELADA, motivo, usuario);
  }

  // Método auxiliar para gestionar el historial y cambio de estado
  private void registrarCambioEstado(EstadoOrden nuevoEstado, String motivo, String usuarioResponsable) {
    CambioEstado cambio = new CambioEstado(
        obtenerEstadoActual(), // estado anterior
        nuevoEstado, // estado nuevo
        LocalDateTime.now(),
        motivo,
        usuarioResponsable);

    this.historialEstados.add(cambio);
    this.estado = nuevoEstado;
  }

  public OrdenId getId() {
    return id;
  }

  public String getNumeroOrden() {
    return numeroOrden;
  }

  public EstadoOrden getEstado() {
    return estado;
  }

  public Money getTotal() {
    return total;
  }

  public List<CambioEstado> obtenerHistorial() {
    return Collections.unmodifiableList(historialEstados);
  }

  public EstadoOrden obtenerEstadoActual() {
    return this.estado;
  }
}
