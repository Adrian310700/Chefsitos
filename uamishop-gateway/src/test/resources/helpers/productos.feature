Feature: Helper productos

Background:
    * url baseUrl
Scenario:

  * def args = __arg ? __arg : {}
  * def nombre = args.nombreProducto
  * def precio = args.precio
  * def moneda = args.moneda

  * if (!nombre) nombre = 'Producto de prueba #' + java.util.UUID.randomUUID()
  * if (!precio) precio = 100
  * if (!moneda) moneda = 'MXN'

  * def categoria =
  """
  {
    nombreCategoria: 'Categoria de pruebas #' ,
    descripcion: 'Categoria test',
    categoriaPadreId: null
  }
  """

  Given path 'api', 'v1', 'categorias'
  And request categoria
  When method POST
  Then status 201

  * def idCategoria = response.idCategoria

  * def producto =
  """
  {
    nombreProducto: '#(nombre)',
    descripcion: 'Producto test',
    precio: '#(precio)',
    moneda: '#(moneda)',
    idCategoria: '#(idCategoria)'
  }
  """

  Given path 'api', 'v1', 'productos'
  And request producto
  When method POST
  Then status 201

  * def result =
  """
  {
    idProducto: '#(response.idProducto)',
    nombreProducto: '#(nombre)',
    precio: '#(precio)'
  }
  """
