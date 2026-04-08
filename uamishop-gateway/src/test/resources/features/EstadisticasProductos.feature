Feature: Pruebas sobre los endpoints de estadisticas sobre productos en el microservicio de catalogo

Background:
    * url baseUrl
Scenario: Dado obtener productos mas vendidos, cuando se crean 3 productos y se registran ventas con diferentes cantidades entonces
  devuelve 200 y valida lista de response comprobando que esten los productos en orden descendente

  * def p1 = call read('classpath:helpers/producto-con-estadisticas.feature') { cantidad: 9 }
  * def idP1 = p1.result.idProducto
  * def p2 = call read('classpath:helpers/producto-con-estadisticas.feature') { cantidad: 10 }
  * def idP2 = p2.result.idProducto
  * def p3 = call read('classpath:helpers/producto-con-estadisticas.feature') { cantidad: 8 }
  * def idP3 = p3.result.idProducto

  * configure retry = { count: 10, interval: 1000 }

  # Por cada producto espera a que se actualicen las estadisticas asincronicas
  Given path 'api', 'v1', 'productos', idP1, 'estadisticas'
  And retry until response.ventasTotales > 0
  When method GET
  Then status 200
  And match response != null
  And match response.cantidadVendida == 9

  Given path 'api', 'v1', 'productos', idP2, 'estadisticas'
  And retry until response.ventasTotales > 0
  When method GET
  Then status 200
  And match response != null
  And match response.cantidadVendida == 10

  Given path 'api', 'v1', 'productos', idP3, 'estadisticas'
  And retry until response.ventasTotales > 0
  When method GET
  Then status 200
  And match response != null
  And match response.cantidadVendida == 8

  # Ejecuta el endpoint a probar
  Given path 'api', 'v1', 'productos', 'mas-vendidos'
  # Espera a que todas las estadisticas sean creadas
  And retry until response.length >= 3
  When method GET
  Then status 200
  And match response != null
  # Validar que los productos estan en orden correcto
  And match response[0].cantidadVendida == 10
  And match response[1].cantidadVendida == 9
  And match response[2].cantidadVendida == 8
  # POR REFACTORIZAR
Scenario: Dado obtener estadisticas de producto cuando se crea un producto, se agrega a un carrito y a una orden craendo estadisticas y
  se utiliza su id entonces devuelve 200 y valida response con estadisticas correctas

  # Se crea un producto y se realiza todo el flujo de compra completo para disparar el evento que genera
  # las estadisticas de un producto
  * def prodcutoConEstadisticas = call read('classpath:helpers/producto-con-estadisticas.feature') { cantidad: 2 }
  * def id = prodcutoConEstadisticas.result.idProducto

  # Se ejecuta el endpoint a probar
  # esperar a que se generen estadísticas, debido a que el evento es asincrono toma unos segundos
  * configure retry = { count: 10, interval: 1000 }
  Given path 'api', 'v1', 'productos', id, 'estadisticas'
  And retry until response.ventasTotales > 0 && response.vecesAgregadoAlCarrito > 0
  When method GET
  Then status 200
  And match response != null
  # Validar respuesta de estadisticas
  And match response.productoId == id
  And match response.ventasTotales == 1
  And match response.cantidadVendida == 2
  And match response.vecesAgregadoAlCarrito == 1
