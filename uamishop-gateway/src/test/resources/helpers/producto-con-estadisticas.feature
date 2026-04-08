Feature: Crear producto con flujo completo de orden y estadisticas

Background:
  * url baseUrl

Scenario:
  * def args = __arg ? __arg : {}

  * def cantidad = args.cantidad ? args.cantidad : 1

  * def crearCarritoRequest =
  """
  function() {
    return { clienteId: java.util.UUID.randomUUID() + '' }
  }
  """

  * def cantidadProducto =
  """
  function(idProducto, cantidad) {
    return {
      productoId: idProducto,
      cantidad: cantidad
    }
  }
  """

  * def ordenDesdeCarrito =
  """
  function(carritoId) {
    return {
      carritoId: carritoId,
      direccion: {
        nombreDestinatario: "Test",
        calle: "Calle Test",
        ciudad: "CDMX",
        estado: "CDMX",
        codigoPostal: "12345",
        telefono: "5500000000",
        instrucciones: ""
      }
    }
  }
  """

  * def asignarProveedor =
  """
  function(){
    return{
      numeroGuia: java.util.UUID.randomUUID() + '',
      proveedorLogistico: 'FedEx'
    }
  }
  """

  * def producto = call read('classpath:helpers/productos.feature')
  * def idProducto = producto.result.idProducto
  * def bodyCarrito = crearCarritoRequest()

  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201

  * def idCarrito = response.carritoId

  * def bodyProducto = cantidadProducto(idProducto, cantidad)

  Given path 'api', 'v1', 'carritos', idCarrito, 'productos'
  And request bodyProducto
  When method POST
  Then status 200
  Given path 'api', 'v1', 'carritos', idCarrito, 'checkout'
  When method POST
  Then status 200

  * def bodyOrden = ordenDesdeCarrito(idCarrito)

  Given path 'api', 'v1', 'ordenes', 'desde-carrito'
  And request bodyOrden
  When method POST
  Then status 201

  * def idOrden = response.id

  Given path 'api', 'v1', 'ordenes', idOrden, 'confirmar'
  When method POST
  Then status 200

  * def bodyPago = { referenciaPago: java.util.UUID.randomUUID() + '' }

  Given path 'api', 'v1', 'ordenes', idOrden, 'pago'
  And request bodyPago
  When method POST
  Then status 200

  Given path 'api', 'v1', 'ordenes', idOrden, 'en-preparacion'
  When method POST
  Then status 200

  * def bodyProveedor = asignarProveedor()

  Given path 'api', 'v1', 'ordenes', idOrden, 'enviada'
  And request bodyProveedor
  When method POST
  Then status 200

  Given path 'api', 'v1', 'ordenes', idOrden, 'entregada'
  When method POST
  Then status 200

  * def result =
  """
  {
    idProducto: '#(idProducto)',
    cantidad: '#(cantidad)'
  }
  """
