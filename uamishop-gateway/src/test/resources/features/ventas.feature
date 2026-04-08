Feature: Pruebas sobre los endpoints del microservicio ventas

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
Scenario: Dado crear carrito cuando se envia un request valido entonces devuelve 201, header y valida respuesta
  # UUID que simula el id de un cliente
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  # Validar respuesta
  And match response.clienteId == bodyCarrito.clienteId
  # Validar que sea realmente nuevo
  And match response.items == []
  And match response.descuentos == []
  And match response.subtotal == 0
  And match response.total == 0
  And match response.estado == 'ACTIVO'
Scenario: Dado obtener carrito por id cuando se crea un carrito previamente y se utiliza su id entonces devuelve 200 y valida respuesta
  # Se crea un carrito previamente y se recupera el id
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201
  And match response != null
  * def idCarrito = response.carritoId

  # Ejecuta el endpoint a probar
  Given path 'api', 'v1', 'carritos', idCarrito
  When method GET
  Then status 200
  And match response != null

Scenario: Dado agregar producto al carrito cuando se crea un producto y carrito previamente y se envia un request valido entonces devuelve
  200 y valida respuesta
  # Crear un producto con un helper
  * def producto = call read('classpath:helpers/productos.feature')
  * def idProducto = producto.result.idProducto
  # Se crea un carrito previamente y se recupera el id
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201
  And match response != null
  * def idCarrito = response.carritoId
  # Se agregan 5 unidades del producto
  * def bodyProducto = cantidadProducto(idProducto, 5)
  # Se ejecuta el endpoint que se quiere probar
  Given path 'api', 'v1', 'carritos', idCarrito, 'productos'
  And request bodyProducto
  When method POST
  Then status 200
  And match response != null
  # Valida que el producto junto con la cantidad este en la lista de items de la respuesta
  And match response.items contains deep
  """
  [
    {
      productoId: '#(idProducto)',
      cantidad: 5
    }
  ]
  """
  # El precio del producto es de 100 por unidad
  And match response.subtotal == 500
  And match response.subtotal == 500
Scenario: Dado modificar cantidad de un producto cuando se agruega un producto existente a un carrito creado previamente y se envia un
  patch body con la nueva cantidad entonces regresa 200 y valida respuesta con la nueva cantidad
  # Crear un producto con un helper
  * def producto = call read('classpath:helpers/productos.feature')
  * def idProducto = producto.result.idProducto
  # Se crea un carrito previamente y se recupera el id
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201
  And match response != null
  * def idCarrito = response.carritoId

  * def bodyProducto = cantidadProducto(idProducto, 1)
  # Se añade al carrito con cantidad = 1
  Given path 'api', 'v1', 'carritos', idCarrito, 'productos'
  And request bodyProducto
  When method POST
  Then status 200
  And match response != null

  # Se prueba el endpoint, se modifica la cantidad a 8 unidades
  * def bodyNuevaCantidad = {nuevaCantidad: 8}
  Given path 'api', 'v1', 'carritos', idCarrito, 'productos', idProducto
  And request bodyNuevaCantidad
  When method PATCH
  Then status 200
  And match response != null
  And match response.items contains deep
  """
  [
    {
      productoId: '#(idProducto)',
      cantidad: 8
    }
  ]
  """
Scenario: Dado eliminar producto del carrito cuando se agregua producto al carrito y despues se utiliza el id del mismo producto
  entonces regresa 200 y valida que response ya no tenga el item del producto
   # Crear un producto con un helper
  * def producto = call read('classpath:helpers/productos.feature')
  * def idProducto = producto.result.idProducto
  # Se crea un carrito previamente y se recupera el id
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201
  And match response != null
  * def idCarrito = response.carritoId

  * def bodyProducto = cantidadProducto(idProducto, 1)
  # Se añade al carrito con cantidad = 1
  Given path 'api', 'v1', 'carritos', idCarrito, 'productos'
  And request bodyProducto
  When method POST
  Then status 200
  And match response != null
  * print response

  # Se prueba el endpoint para eliminar el producto del carrito
  Given path 'api', 'v1', 'carritos', idCarrito, 'productos', idProducto
  When method DELETE
  Then status 200
  And match response != null
  # Valida que el id no esta en la lista
  And match response.items[*].productoId !contains idProducto
  * print response
Scenario: Dado vaciar carrito cuando se agregan 2 productos existentes a un carrito creado previamente y se utiliza el id del mismo carrito
  entonces regresa 200 y valida response no tenga ningun item carrito
  # Crear un primer producto con un helper
  * def prod1 =
  """
  {
    nombreProducto: 'Bebida',
    precio: 10,
    moneda: 'MXN'
  }
  """
  * def producto1 = call read('classpath:helpers/productos.feature') prod1
  * def idProducto1 = producto1.result.idProducto
  # Crear un segundo producto
  * def prod2 =
  """
  {
    nombreProducto: 'Frituras',
    precio: 20,
    moneda: 'MXN'
  }
  """
  * def producto2 = call read('classpath:helpers/productos.feature') prod2
  * def idProducto2 = producto2.result.idProducto
  # Se crea un carrito previamente y se recupera el id
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201
  And match response != null
  * def idCarrito = response.carritoId
  # Agregar el primer producto al carrito
  * def bodyProducto1 = cantidadProducto(idProducto1, 2)
  Given path 'api', 'v1', 'carritos', idCarrito, 'productos'
  And request bodyProducto1
  When method POST
  Then status 200
  And match response != null
    # Agregar el segundo producto al carrito
  * def bodyProducto2 = cantidadProducto(idProducto2, 1)
  Given path 'api', 'v1', 'carritos', idCarrito, 'productos'
  And request bodyProducto2
  When method POST
  Then status 200
  And match response != null
  * print response

  # Se ejecuta el endpoint a probar
  Given path 'api', 'v1', 'carritos', idCarrito, 'productos'
  When method DELETE
  Then status 200
  And match response != null
  # Valida que la lista de items esta vacia
  And match response.items == []
  * print response
Scenario: Dado iniciar checkout cuando se agrega un producto existente a un carrito creado previamente y se utiliza el id del carrito
  entonces regresa 200 y valida respuesta comprobando que el estado del carrito sea "en checkout"
    # Crear un producto con un helper
  * def producto = call read('classpath:helpers/productos.feature')
  * def idProducto = producto.result.idProducto
  # Se crea un carrito previamente y se recupera el id
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201
  And match response != null
  * def idCarrito = response.carritoId

  * def bodyProducto = cantidadProducto(idProducto, 1)
  # Se añade al carrito con cantidad = 1
  Given path 'api', 'v1', 'carritos', idCarrito, 'productos'
  And request bodyProducto
  When method POST
  Then status 200
  And match response != null

  # Se prueba el endpoint
  Given path 'api', 'v1', 'carritos', idCarrito, 'checkout'
  When method POST
  Then status 200
  And match response != null
  # Valida el cambio de estado
  And match response.estado == 'EN_CHECKOUT'
Scenario: Dado completar checkout cuando hay un carrito con un producto creado previamente y se utiliza el id del carrito
  entonces responde 200 y valida response comprobando que el estado es "COMPLETADO"
  # Crear un producto con un helper
  * def producto = call read('classpath:helpers/productos.feature')
  * def idProducto = producto.result.idProducto
  # Se crea un carrito previamente y se recupera el id
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201
  And match response != null
  * def idCarrito = response.carritoId

  * def bodyProducto = cantidadProducto(idProducto, 1)
  # Se añade al carrito con cantidad = 1
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

  # Se prueba el endpoint
  Given path 'api', 'v1', 'carritos', idCarrito, 'checkout', 'completar'
  When method POST
  Then status 200
  And match response != null
  And match response.estado == 'COMPLETADO'
Scenario: Dado abandonar carrito cuando el carrito tiene el estado "EN_CHECKOUT" y se utiliza el id del carrito entonces regresa 200
  y se valida respuesta comprobando que el estado cambio a "ABANDONADO"
    # Crear un producto con un helper
  * def producto = call read('classpath:helpers/productos.feature')
  * def idProducto = producto.result.idProducto
  # Se crea un carrito previamente y se recupera el id
  * def bodyCarrito = crearCarritoRequest()
  Given path 'api', 'v1', 'carritos'
  And request bodyCarrito
  When method POST
  Then status 201
  And match response != null
  * def idCarrito = response.carritoId

  * def bodyProducto = cantidadProducto(idProducto, 1)
  # Se añade al carrito con cantidad = 1
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

  # Se prueba el endpoint
  Given path 'api', 'v1', 'carritos', idCarrito, 'abandonar'
  When method POST
  Then status 200
  And match response != null
  And match response.estado == 'ABANDONADO'
