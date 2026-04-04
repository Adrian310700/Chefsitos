Feature: Pruebas sobre los endpoints de categorias del microservicio catalogo

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

Scenario: Crear categoria sin padre
  * def body = crearCategoriaRequest('Electronica', 'Productos electronicos', null)
  Given path 'api', 'v1', 'categorias'
  And request body
  When method POST
  Then status 201
  And match header Location != null
  And match response != null
  And match response.nombreCategoria == 'Electronica'
  And match response.descripcion == 'Productos electronicos'
  And match response.idCategoriaPadre == null

Scenario: Crear categoria con padre

  # Creacion de la categoria padre, se persiste en la BD y se recupera el ID para asginarlo a una nueva categoria
  * def categoriaPadre = crearCategoriaRequest('Electrodomesticos', 'Categoria padre', null)

  Given path 'api', 'v1', 'categorias'
  And request categoriaPadre
  When method POST
  Then status 201
  And match header Location != null
  * def idPadre = response.idCategoria
  And match idPadre == '#string'

  # Categoria hijo donde se quiere comprobar que se asigna una categoria padre
  * def categoriaHijo = crearCategoriaRequest('Lavadoras', 'Categoria hijo', idPadre)

  Given path 'api', 'v1', 'categorias'
  And request categoriaHijo
  When method POST
  Then status 201
  And match response.idCategoriaPadre == idPadre
  And match response.nombreCategoria == 'Lavadoras'

Scenario: Buscar categoria por Id

  # Crear la categoria para generar un id y recuperarlo posteriormente
  * def categoriaBuscar = crearCategoriaRequest('Muebles', 'Categoria a buscar', null)
  Given path 'api', 'v1', 'categorias'
  And request categoriaBuscar
  When method POST
  Then status 201
  And match header Location != null
  * def idGet = response.idCategoria
  And match idGet == '#string'

  # Usar el GET con ID
  Given path 'api', 'v1', 'categorias', idGet
  When method GET
  Then status 200
  And match response.nombreCategoria == 'Muebles'
  And match response.descripcion == 'Categoria a buscar'
  And match response.idCategoriaPadre == null

Scenario: Obtener una lista de todos las categorias
  # Crear 2 categorias independientemente si se ejecutaron otros test anteriormente que hayan persistido en la BD
  * def categoria1 = crearCategoriaRequest('Ropa', 'Categoria 1', null)
  * def categoria2 = crearCategoriaRequest('Celulares', 'Categoria 2', null)
  Given path 'api', 'v1', 'categorias'
  And request categoria1
  When method POST
  Then status 201
  * def nombre1 = categoria1.nombreCategoria

  Given path 'api', 'v1', 'categorias'
  And request categoria2
  When method POST
  Then status 201
  * def nombre2 = categoria2.nombreCategoria

  Given path 'api', 'v1', 'categorias'
  When method GET
  Then status 200
  And match response[*].nombreCategoria contains nombre1
  And match response[*].nombreCategoria contains nombre2

Scenario: Actualizar categoria

  # Crear una categoria
  * def categoriaOriginal = crearCategoriaRequest('Bebidas', 'Descripcion original', null)

  Given path 'api', 'v1', 'categorias'
  And request categoriaOriginal
  When method POST
  Then status 201
  And match header Location != null
  * def id = response.idCategoria

  # Crear request de actualización
  * def requestUpdate = crearCategoriaRequest('Bebidas actualizada', 'Nueva descripcion de bebidas', null)

  Given path 'api', 'v1', 'categorias', id
  And request requestUpdate
  When method PUT
  Then status 200


  # Validar cambios
  And match response.nombreCategoria == 'Bebidas actualizada'
  And match response.descripcion == 'Nueva descripcion de bebidas'
  And match response.idCategoriaPadre == null
