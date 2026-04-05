Feature: Pruebas sobre los endpoints de estadisticas sobre productos en el microservicio de catalogo

Background:
    * url baseUrl
    * def crearCarritoRequest =
    """
    function() {
      return {
        clienteId: java.util.UUID.randomUUID() + ''
      }
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
    function(carritoId, nombreDestinatario, calle, ciudad, estado, codigoPostal, telefono) {
      return {
        carritoId: carritoId,
        direccion: {
          nombreDestinatario: nombreDestinatario,
          calle: calle,
          ciudad: ciudad,
          estado: estado,
          codigoPostal: codigoPostal,
          telefono: telefono,
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

  # POR REFACTORIZAR
Scenario: Dado obtener estadisticas de producto cuando se crea un producto, se agrega a un carrito y a una ordene craendo estadisticas y
  se utiliza su id entonces devuelve 200 y valida response con estadisticas correctas
  # Se crea un producto con un helper externo
  * def producto = call read('classpath:helpers/productos.feature')
  * def idProducto = producto.result.idProducto
  # Se crea un carrito para el producto
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201
  And match response != null
  * def idCarrito = response.carritoId
  # Se añade al carrito con cantidad = 2
  * def bodyProducto = cantidadProducto(idProducto, 2)
  Given path 'api', 'v1', 'carritos', idCarrito, 'productos'
  And request bodyProducto
  When method POST
  Then status 200
  And match response != null
  # Se cambia el estado a EN_CHECKOUT
  Given path 'api', 'v1', 'carritos', idCarrito, 'checkout'
  When method POST
  Then status 200
  And match response != null
  # Se crea la orden desde carrito y al mismo tiempo se genera estadisticas del producto
  # Crear request para la orden
  * def body = ordenDesdeCarrito(idCarrito, "Manuel", "Calle 7", "Iztapalapa", "CDMX", "12345", "5501020304")
  Given path 'api', 'v1', 'ordenes', 'desde-carrito'
  And request body
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  # Recupera id de la orden
  * def idOrden = response.id
  # Se cambia el estado a confirmada
  Given path 'api', 'v1', 'ordenes', idOrden, 'confirmar'
  When method POST
  Then status 200
  And match response != null
  # Se procesa el pago
  * def bodyPago = { referenciaPago: java.util.UUID.randomUUID() + '' }
  Given path 'api', 'v1', 'ordenes', idOrden, 'pago'
  And request bodyPago
  When method POST
  Then status 200
  And match response != null
  # Se marca en preparacion
  Given path 'api', 'v1', 'ordenes', idOrden, 'en-preparacion'
  When method POST
  Then status 200
  And match response != null
  # Se marca la orden como enviada
  * def bodyProveedor = asignarProveedor()
  Given path 'api', 'v1', 'ordenes', idOrden, 'enviada'
  And request bodyProveedor
  When method POST
  Then status 200
  And match response != null
  # Se marca como entregada
  Given path 'api', 'v1', 'ordenes', idOrden, 'entregada'
  When method POST
  Then status 200
  And match response != null

  # Se ejecuta el endpoint a probar
  # esperar a que se generen estadísticas
  * configure retry = { count: 10, interval: 1000 }
  Given path 'api', 'v1', 'productos', idProducto, 'estadisticas'
  And retry until response.ventasTotales > 0
  When method GET
  Then status 200
  And match response != null
  # Validar respuesta
  And match response.productoId == idProducto
  And match response.ventasTotales == 1
  And match response.cantidadVendida == 2
  And match response.vecesAgregadoAlCarrito == 1

 # INCOMPLETO
Scenario: Dado obtener productos mas vendidos, cuando se crean 3 productos y se registran ventas con diferentes cantidades entonces
  devuelve 200 y valida lista de response comprobando que esten los productos en orden descendente

  * def p1 = call read('classpath:helpers/producto-con-estadisticas.feature') { cantidad: 9 }
  * def p2 = call read('classpath:helpers/producto-con-estadisticas.feature') { cantidad: 10 }
  * def p3 = call read('classpath:helpers/producto-con-estadisticas.feature') { cantidad: 8 }

  * configure retry = { count: 10, interval: 1000 }

  Given path 'api', 'v1', 'productos', p1.idProducto, 'estadisticas'
  And retry until response.cantidadVendida >= 2
  When method GET
  Then status 200
  And match response != null
  And match response.cantidadVendida == 9
