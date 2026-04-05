Feature: Probar endpoints del microservicio ordenes

Background:
  * url baseUrl
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
  * def ordenDirecta =
  """
    function(nombreDestinatario, calle, ciudad, estado, codigoPostal, telefono, productoId, cantidad) {
      return {
        clienteId: java.util.UUID.randomUUID() + '',
        direccion: {
          nombreDestinatario: nombreDestinatario,
          calle: calle,
          ciudad: ciudad,
          estado: estado,
          codigoPostal: codigoPostal,
          telefono: telefono,
          instrucciones: ""
        },
        items: [
          {
            productoId: productoId,
            cantidad: cantidad
          }
        ]
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
Scenario: Dado crear orden desde carrito cuando se crea un carrito con un producto añadido previamente, se utiliza el id de este carrito y
  se envia un request body adjuntando datos de direccion entonces valida respuesta
  # Se crea un carrito con un producto añadido
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente

  # Crear request para la orden
  * def body = ordenDesdeCarrito(idCarrito, "Manuel", "Calle 7", "Iztapalapa", "CDMX", "12345", "5501020304")
  # Ejecutar el endpoint a probar
  Given path 'api', 'v1', 'ordenes', 'desde-carrito'
  And request body
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  # Validar respuesta
  And match response.id != null
  And match response.clienteId == idCliente
  And match response.total == 100
  And match response.moneda == 'MXN'
  And match response.direccionResumen == 'Calle 7, Iztapalapa'
  And match response.estado == 'PENDIENTE'
Scenario: Dado crear orden directa cuando se envia un request con id de cliente, direccion y un item de proudcto existente entonces
  regresa 201 y valida respuesta
    se envia un request body adjuntando datos de direccion entonces valida respuesta
  # Se crea un producto
  * def producto = call read('classpath:helpers/productos.feature')
  * def idProducto = producto.result.idProducto

  # Crear request para la orden
  * def body = ordenDirecta("Manuel", "Calle 7", "Iztapalapa", "CDMX", "12345", "5501020304", idProducto, 1)
  * def idCliente = body.clienteId
  # Ejecutar el endpoint a probar
  Given path 'api', 'v1', 'ordenes', 'directa'
  And request body
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  # Validar respuesta
  And match response.id != null
  And match response.clienteId == idCliente
  And match response.total == 100
  And match response.moneda == 'MXN'
  And match response.direccionResumen == 'Calle 7, Iztapalapa'
  And match response.estado == 'PENDIENTE'
Scenario: Dado buscar orden por id cuando se crea una orden y se utiliza su id entonces regresa 200 y valida respuesta
  # Se crea la orden desde un carrito
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente
  * def body = ordenDesdeCarrito(idCarrito, "Manuel", "Calle 7", "Iztapalapa", "CDMX", "12345", "5501020304")
  Given path 'api', 'v1', 'ordenes', 'desde-carrito'
  And request body
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  # Recupera id de la orden
  * def idOrden = response.id

  # Se ejecuta el endpoint a probar
  Given path 'api', 'v1', 'ordenes', idOrden
  When method GET
  Then status 200
  And match response != null
  # Validar respuesta
  And match response.id != null
  And match response.clienteId == idCliente
  And match response.total == 100
  And match response.moneda == 'MXN'
  And match response.direccionResumen == 'Calle 7, Iztapalapa'
  And match response.estado == 'PENDIENTE'
Scenario: Dado listar todas las ordenes cuando se crean 2 ordenes previamente entonces devuelve 200 y valida que la lista de response
  contenga al menos las 2 ordenes creadas en este test
   # Se crea una primera orden desde un carrito
  * def carrito1 = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito1 = carrito1.idCarrito
  * def idCliente1 = carrito1.idCliente
  * def body1 = ordenDesdeCarrito(idCarrito1, "Manuel", "Calle 7", "Iztapalapa", "CDMX", "12345", "5501020304")
  Given path 'api', 'v1', 'ordenes', 'desde-carrito'
  And request body1
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  # Recupera id de la orden
  * def idOrden1 = response.id

  # Se crea una segunda orden desde un carrito
  * def carrito2 = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito2 = carrito2.idCarrito
  * def idCliente2 = carrito2.idCliente
  * def body2 = ordenDesdeCarrito(idCarrito2, "Sebastian", "Calle 4", "Iztapalapa", "CDMX", "67890", "5511223344")
  Given path 'api', 'v1', 'ordenes', 'desde-carrito'
  And request body2
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  # Recupera id de la orden
  * def idOrden2 = response.id

  # Se ejecuta el endpoint a probar
  Given path 'api', 'v1', 'ordenes'
  When method GET
  Then status 200
  And match response != null
  # Validar que se tenga la primera orden
  And match response[*].clienteId contains idCliente1
  # Validar que se tenga la segunda orden
  And match response[*].clienteId contains idCliente2
Scenario: Dado confirmar orden cuando se crea una orden en estado pendiente previamente y se utiliza su id entonces devuelve 200 y valida
  response comprobando que el estado cambio a "CONFIRMADA"
   # Se crea la orden desde un carrito
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente
  * def body = ordenDesdeCarrito(idCarrito, "Manuel", "Calle 7", "Iztapalapa", "CDMX", "12345", "5501020304")
  Given path 'api', 'v1', 'ordenes', 'desde-carrito'
  And request body
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  # Recupera id de la orden
  * def idOrden = response.id

  # Se ejecuta el endpoint a probar
  Given path 'api', 'v1', 'ordenes', idOrden, 'confirmar'
  When method POST
  Then status 200
  And match response != null
  And match response.clienteId == idCliente
  And match response.estado == 'CONFIRMADA'
Scenario: Dado procesar pago cuando se crea una orden y se establece como confirmada previamente, se utiliza su id y se envia un request valido con la referencia de pago
  entonces devuelve 200 y valida respuesta comprobando que el estado cambio a "PAGO_PROCESADO"
   # Se crea la orden desde un carrito
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente
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

  # Se pone a prueba el endpoint
  * def bodyPago = { referenciaPago: java.util.UUID.randomUUID() + '' }
  Given path 'api', 'v1', 'ordenes', idOrden, 'pago'
  And request bodyPago
  When method POST
  Then status 200
  And match response != null
  # Validar respuesta
  And match response.clienteId == idCliente
  And match response.estado == 'PAGO_PROCESADO'
Scenario: Dado marcar en preparacion cuando se crea una orden y se establece como pago procesado previamente y se utiliza su id
  entonces devuelve 200 y valida respuesta comprobando que el estado cambio a "EN_PREPARACION"
   # Se crea la orden desde un carrito
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente
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

  # Se pone a prueba el endpoint
  Given path 'api', 'v1', 'ordenes', idOrden, 'en-preparacion'
  When method POST
  Then status 200
  And match response != null
    # Validar respuesta
  And match response.clienteId == idCliente
  And match response.estado == 'EN_PREPARACION'
Scenario: Dado marcar como enviada cuando se crea una orden y llega a la parte de marcada como "En preparacion" previamente, se utiliza su id
  y se envia un request con los datos del proveedor logistico entonces devuelve 200 y valida respuesta comprobando
  que el estado cambio a "ENVIADA"
   # Se crea la orden desde un carrito
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente
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

  # Se pone a prueba el endpoint
  * def bodyProveedor = asignarProveedor()
  Given path 'api', 'v1', 'ordenes', idOrden, 'enviada'
  And request bodyProveedor
  When method POST
  Then status 200
  And match response != null
  # Validar respuesta
  And match response.clienteId == idCliente
  And match response.estado == 'ENVIADA'
Scenario: Dado marcar como entregada cuando se crea una orden y llega a la parte de marcada como "Enviada" previamente y se utiliza su id
  entonces devuelve 200 y valida respuesta comprobando que el estado cambio a "ENTREGADA"
   # Se crea la orden desde un carrito
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente
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

  # Se pone a prueba el endpoint
  Given path 'api', 'v1', 'ordenes', idOrden, 'entregada'
  When method POST
  Then status 200
  And match response != null
  # Validar respuesta
  And match response.clienteId == idCliente
  And match response.estado == 'ENTREGADA'
Scenario: Dado cancelar orden cuando la orden esta en el estado "PENDIENTE" y se utiliza su id entonces regresa 200 y se valida
  respuesta comprobando que el estado cambio a "CANCELADO"
  # Se crea la orden desde un carrito
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente
  * def body = ordenDesdeCarrito(idCarrito, "Manuel", "Calle 7", "Iztapalapa", "CDMX", "12345", "5501020304")
  Given path 'api', 'v1', 'ordenes', 'desde-carrito'
  And request body
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  # Recupera id de la orden
  * def idOrden = response.id

  # Se pone a prueba el endpoint
  * def bodyCancelacion = { motivo: 'Prueba de cancelacion #1'}
  Given path 'api', 'v1', 'ordenes', idOrden, 'cancelar'
  And request bodyCancelacion
  When method POST
  Then status 200
  And match response != null
  # Validar respuesta
  And match response.clienteId == idCliente
  And match response.estado == 'CANCELADA'
Scenario: Dado cancelar orden cuando la orden esta en el estado "CONFIRMADA" y se utiliza su id entonces regresa 200 y se valida
  respuesta comprobando que el estado cambio a "CANCELADO"
  # Se crea la orden desde un carrito
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente
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

  # Se pone a prueba el endpoint
  * def bodyCancelacion = { motivo: 'Prueba de cancelacion #1'}
  Given path 'api', 'v1', 'ordenes', idOrden, 'cancelar'
  And request bodyCancelacion
  When method POST
  Then status 200
  And match response != null
  # Validar respuesta
  And match response.clienteId == idCliente
  And match response.estado == 'CANCELADA'
Scenario: Dado cancelar orden cuando la orden esta en el estado "PAGO_PROCESADO" y se utiliza su id entonces regresa 200 y se valida
  respuesta comprobando que el estado cambio a "CANCELADO"
  # Se crea la orden desde un carrito
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente
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

  # Se pone a prueba el endpoint
  * def bodyCancelacion = { motivo: 'Prueba de cancelacion #1'}
  Given path 'api', 'v1', 'ordenes', idOrden, 'cancelar'
  And request bodyCancelacion
  When method POST
  Then status 200
  And match response != null
  # Validar respuesta
  And match response.clienteId == idCliente
  And match response.estado == 'CANCELADA'
Scenario: Dado cancelar orden cuando la orden esta en el estado "EN_PREPARACION" y se utiliza su id entonces regresa 200 y se valida
  respuesta comprobando que el estado cambio a "CANCELADO"
  # Se crea la orden desde un carrito
  * def carrito = call read('classpath:helpers/carrito-con-producto.feature')
  * def idCarrito = carrito.idCarrito
  * def idCliente = carrito.idCliente
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

  # Se pone a prueba el endpoint
  * def bodyCancelacion = { motivo: 'Prueba de cancelacion #1'}
  Given path 'api', 'v1', 'ordenes', idOrden, 'cancelar'
  And request bodyCancelacion
  When method POST
  Then status 200
  And match response != null
  # Validar respuesta
  And match response.clienteId == idCliente
  And match response.estado == 'CANCELADA'
