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
  # recibir argumentos opcionales
  * def args = __arg ? __arg : {}

  * def cantidad = args.cantidad ? args.cantidad : 1

  # 🔹 1. crear producto (reusar helper existente)
  * def producto = call read('classpath:helpers/productos.feature') args
  * def idProducto = producto.result.idProducto

  # 🔹 2. crear carrito
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201

  * def idCarrito = response.carritoId
  * def idCliente = response.clienteId

  # 🔹 3. agregar producto al carrito
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


  # 🔹 4. retornar datos útiles
  * def result =
  """
  {
    idCliente : '#(idCliente)',
    idCarrito: '#(idCarrito)',
    idProducto: '#(idProducto)',
    cantidad: '#(cantidad)'
  }
  """
