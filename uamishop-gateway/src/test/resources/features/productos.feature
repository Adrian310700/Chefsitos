Feature: Pruebas sobre los endpoints de productos en el microservicio de catalogo

Background:
    * url baseUrl
    * def crearCategoriaRequest =
    """
    function(nombre, descripcion, idPadre) {
        return {
            nombreCategoria: nombre,
            descripcion: descripcion,
            categoriaPadreId: idPadre
        }
    }
    """
    * def crearProductoRequest =
    """
    function(nombre, descripcion, precio, moneda, idCategoria) {
        return {
            nombreProducto: nombre,
            descripcion: descripcion,
            precio: precio,
            moneda: moneda,
            idCategoria: idCategoria
        }
    }
    """
    * def AgregarImagen =
    """
    function(url) {
        return {
            urlImagen: url
        }
    }
    """
Scenario: Dado crear producto cuando se envia un request de producto valido entonces devuelve 201, header y valida respuesta
  # Crear primero una categoria
  * def bodyCategoria = crearCategoriaRequest('Electronica', 'Productos electronicos', null)

  Given path 'api', 'v1', 'categorias'
  And request bodyCategoria
  When method POST
  Then status 201
  And match header Location != null
  * def idCategoria = response.idCategoria

  # Crear el producto
  * def body = crearProductoRequest('MacBook Pro 16', 'Laptop', 45000.00, 'MXN', idCategoria)

  Given path 'api', 'v1', 'productos'
  And request body
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  And match response.nombreProducto == 'MacBook Pro 16'
  And match response.descripcion == 'Laptop'
  And match response.precio == 45000.00
  And match response.moneda == 'MXN'
  And match response.idCategoria == idCategoria

Scenario: Dado buscar un producto por ID cuando se crea el producto previamente y se utiliza su id entonces devuelve 200, header y valida respuesta
  # Crear primero una categoria
  * def bodyCategoria = crearCategoriaRequest('Celulares', 'Productos celulares', null)

  Given path 'api', 'v1', 'categorias'
  And request bodyCategoria
  When method POST
  Then status 201
  And match header Location != null
  * def idCategoria = response.idCategoria

  # Crear el producto
  * def body = crearProductoRequest('iPhone 16', 'celular apple', 15000.00, 'MXN', idCategoria)

  Given path 'api', 'v1', 'productos'
  And request body
  When method POST
  Then status 201
  And match header Location != null
  * def idProducto = response.idProducto

  # Buscar por ID
  Given path 'api', 'v1', 'productos', idProducto
  When method GET
  Then status 200
  And match response != null
  And match response.nombreProducto == 'iPhone 16'
  And match response.descripcion == 'celular apple'
  And match response.precio == 15000.00
  And match response.moneda == 'MXN'
  And match response.idCategoria == idCategoria
Scenario: Dado obtener todos los productos cuando se crean al menos 2 productos previamente entonces devuelve 200 y valida la lista de respuesta
  que contenga al menos los dos productos creados en este test
  # Crear primero una categoria
  * def bodyCategoria = crearCategoriaRequest('Electronica', 'Productos electronicos', null)

  Given path 'api', 'v1', 'categorias'
  And request bodyCategoria
  When method POST
  Then status 201
  And match header Location != null
  * def idCategoria = response.idCategoria
  # Crear los productos
  * def bodyProducto1 = crearProductoRequest('MacBook Pro', 'Laptop', 38000.00, 'MXN', idCategoria)
  * def bodyProducto2 = crearProductoRequest('iPhone 15', 'Celular', 18000.00, 'MXN', idCategoria)

  Given path 'api', 'v1', 'productos'
  And request bodyProducto1
  When method POST
  Then status 201
  And match header Location != null
  * def nombre1 = bodyProducto1.nombreProducto

  Given path 'api', 'v1', 'productos'
  And request bodyProducto2
  When method POST
  Then status 201
  And match header Location != null
  * def nombre2 = bodyProducto2.nombreProducto

  # Prueba del endpoint GET asegurando que contenga los productos creados anteriormente
  Given path 'api', 'v1', 'productos'
  When method GET
  Then status 200
  And match response[*].nombreProducto contains nombre1
  And match response[*].nombreProducto contains nombre2

Scenario: Dado actualizar un producto cuando se crea previamente y se envia un request patch valido entonces devuelve 200, header y
  se valida la respuesta con la informacion actualizada
  # Categoria original del producto
  * def bodyCategoria = crearCategoriaRequest('Electronica', 'Productos electronicos', null)

  Given path 'api', 'v1', 'categorias'
  And request bodyCategoria
  When method POST
  Then status 201
  And match header Location != null
  * def idCategoria = response.idCategoria
  # Informacion original del producto
  * def bodyProductoOriginal = crearProductoRequest('MacBook Pro', 'Laptop', 32000.00, 'MXN', idCategoria)

  Given path 'api', 'v1', 'productos'
  And request bodyProductoOriginal
  When method POST
  Then status 201
  And match header Location != null
  * def idProducto = response.idProducto
  # Nueva categoria que con la que se actualizara el producto
  * def bodyCategoriaNueva = crearCategoriaRequest('Computación', 'Productos de computo', null)

  Given path 'api', 'v1', 'categorias'
  And request bodyCategoriaNueva
  When method POST
  Then status 201
  And match header Location != null
  * def idCategoriaNueva = response.idCategoria
  # Actualizar informacion del producto
  * def bodyProductoActualizado = crearProductoRequest('MacBook Pro M3', 'Laptop profesional', 38000.00, 'MXN', idCategoriaNueva)

  Given path 'api', 'v1', 'productos', idProducto
  And request bodyProductoActualizado
  When method PATCH
  Then status 200
  And match response != null
  And match response.nombreProducto == 'MacBook Pro M3'
  And match response.descripcion == 'Laptop profesional'
  And match response.precio == 38000.00
  And match response.moneda == 'MXN'
  And match response.idCategoria == idCategoriaNueva

Scenario: Dado actualizar imagen cuando se crea un producto previamente y se envia un request con la url de la imagen valido entonces
  devuelve 200 y valida respuesta de la url agregada al producto
  * def bodyCategoria = crearCategoriaRequest('Electronica', 'Productos electronicos', null)

  Given path 'api', 'v1', 'categorias'
  And request bodyCategoria
  When method POST
  Then status 201
  And match header Location != null
  * def idCategoria = response.idCategoria
  # Crear producto sin imagen aun
  * def bodyProducto = crearProductoRequest('MacBook Pro', 'Laptop', 32000.00, 'MXN', idCategoria)

  Given path 'api', 'v1', 'productos'
  And request bodyProducto
  When method POST
  Then status 201
  And match header Location != null
  * def idProducto = response.idProducto
  # Agregar una url (imagen) al producto
  * def bodyImagen = AgregarImagen('https://example.com/image.jpg')

  Given path 'api', 'v1', 'productos', idProducto, 'imagen'
  And request bodyImagen
  When method PATCH
  Then status 200
  And match response != null
  And match response.urlImagen == 'https://example.com/image.jpg'

Scenario: Dado activar producto cuando se crea un producto y se le agrega una imagen con url valido previamente entonces devuelve 200 y
  valida que disponible sea cierto
  * def bodyCategoria = crearCategoriaRequest('Electronica', 'Productos electronicos', null)

  Given path 'api', 'v1', 'categorias'
  And request bodyCategoria
  When method POST
  Then status 201
  And match header Location != null
  * def idCategoria = response.idCategoria
  # Crear producto sin imagen aun
  * def bodyProducto = crearProductoRequest('MacBook Pro', 'Laptop', 32000.00, 'MXN', idCategoria)

  Given path 'api', 'v1', 'productos'
  And request bodyProducto
  When method POST
  Then status 201
  And match header Location != null
  * def idProducto = response.idProducto
  # Agregar una url (imagen) al producto
  * def bodyImagen = AgregarImagen('https://example.com/image.jpg')

  Given path 'api', 'v1', 'productos', idProducto, 'imagen'
  And request bodyImagen
  When method PATCH
  Then status 200
  And match response != null

  # Ejecutar endpoint activar
  Given path 'api', 'v1', 'productos', idProducto, 'activar'
  When method POST
  Then status 200
  And match response != null
  And match response.disponible == true
Scenario: Dado desactivar un producto cuando se crea un producto, se agrega una imagen con url valido y se activa previamente
  entonces regresa 200 y se valida que disponible sea falso
  * def bodyCategoria = crearCategoriaRequest('Electronica', 'Productos electronicos', null)

  Given path 'api', 'v1', 'categorias'
  And request bodyCategoria
  When method POST
  Then status 201
  And match header Location != null
  * def idCategoria = response.idCategoria
  # Crear producto sin imagen aun
  * def bodyProducto = crearProductoRequest('MacBook Pro', 'Laptop', 32000.00, 'MXN', idCategoria)

  Given path 'api', 'v1', 'productos'
  And request bodyProducto
  When method POST
  Then status 201
  And match header Location != null
  * def idProducto = response.idProducto
  # Agregar una url (imagen) al producto
  * def bodyImagen = AgregarImagen('https://example.com/image.jpg')

  Given path 'api', 'v1', 'productos', idProducto, 'imagen'
  And request bodyImagen
  When method PATCH
  Then status 200
  And match response != null

  # Se activa
  Given path 'api', 'v1', 'productos', idProducto, 'activar'
  When method POST
  Then status 200
  And match response != null
  And match response.disponible == true

  #Ejecutar endpoint para desactivar
  Given path 'api', 'v1', 'productos', idProducto, 'desactivar'
  When method POST
  Then status 200
  And match response != null
  And match response.disponible == false
