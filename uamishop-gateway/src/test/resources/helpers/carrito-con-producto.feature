Feature: Helper para crear carrito con producto

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

Scenario:
  * def args = __arg ? __arg : {}

  * def cantidad = args.cantidad ? args.cantidad : 1

  * def producto = call read('classpath:helpers/productos.feature') args
  * def idProducto = producto.result.idProducto

  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201

  * def idCarrito = response.carritoId
  * def idCliente = response.clienteId

  * def bodyProducto =
  """
  {
    productoId: '#(idProducto)',
    cantidad: '#(cantidad)'
  }
  """

  Given path 'api', 'v1', 'carritos', idCarrito, 'productos'
  And request bodyProducto
  When method POST
  Then status 200

  Given path 'api', 'v1', 'carritos', idCarrito, 'checkout'
  When method POST
  Then status 200
  And match response != null

  * def result =
  """
  {
    idCliente : '#(idCliente)',
    idCarrito: '#(idCarrito)',
    idProducto: '#(idProducto)',
    cantidad: '#(cantidad)'
  }
  """
