# UAMIShop - Mapa de Rutas API

Referencia de todas las rutas REST expuestas por los microservicios.
Todas las rutas usan el prefijo `/api/v1`. El API Gateway en el puerto `8080` redirige a cada servicio.

> **Base URL (vía Gateway):** `http://localhost:8080`

---

## Catálogo (`uamishop-catalogo` · puerto 8081)

### Productos `/api/v1/productos`

| Método   | Ruta                                | Descripción                      | Request Body          | Response        |
| -------- | ----------------------------------- | -------------------------------- | --------------------- | --------------- |
| `POST`   | `/api/v1/productos`                 | Crear producto                   | `ProductoRequest`     | `201` + Location |
| `GET`    | `/api/v1/productos`                 | Listar todos los productos       | —                     | `200` Lista     |
| `GET`    | `/api/v1/productos/{id}`            | Obtener producto por ID          | —                     | `200` Producto  |
| `PATCH`  | `/api/v1/productos/{id}`            | Actualizar producto              | `ProductoPatchRequest`| `200` Producto  |
| `POST`   | `/api/v1/productos/{id}/activar`    | Activar producto                 | —                     | `200` Producto  |
| `POST`   | `/api/v1/productos/{id}/desactivar` | Desactivar producto              | —                     | `200` Producto  |
| `GET`    | `/api/v1/productos/mas-vendidos`    | Productos más vendidos           | `?limit=10`           | `200` Lista     |
| `GET`    | `/api/v1/productos/{id}/estadisticas`| Estadísticas de un producto     | —                     | `200` Stats     |

### Categorías `/api/v1/categorias`

| Método   | Ruta                          | Descripción                | Request Body       | Response         |
| -------- | ----------------------------- | -------------------------- | ------------------ | ---------------- |
| `POST`   | `/api/v1/categorias`          | Crear categoría            | `CategoriaRequest` | `201` + Location |
| `GET`    | `/api/v1/categorias`          | Listar todas las categorías| —                  | `200` Lista      |
| `GET`    | `/api/v1/categorias/{id}`     | Obtener categoría por ID   | —                  | `200` Categoría  |
| `PUT`    | `/api/v1/categorias/{id}`     | Actualizar categoría       | `CategoriaRequest` | `200` Categoría  |

---

## Órdenes (`uamishop-ordenes` · puerto 8082)

### Órdenes `/api/v1/ordenes`

| Método   | Ruta                                   | Descripción                          | Request Body                    | Response       |
| -------- | -------------------------------------- | ------------------------------------ | ------------------------------- | -------------- |
| `POST`   | `/api/v1/ordenes/directa`              | Crear orden directa (sin carrito)    | `OrdenRequest`                  | `201` + Location |
| `POST`   | `/api/v1/ordenes/desde-carrito`        | Crear orden desde carrito            | `CrearOrdenDesdeCarritoRequest` | `201` + Location |
| `GET`    | `/api/v1/ordenes`                      | Listar todas las órdenes             | —                               | `200` Lista    |
| `GET`    | `/api/v1/ordenes/{id}`                 | Obtener orden por ID                 | —                               | `200` Orden    |
| `POST`   | `/api/v1/ordenes/{id}/confirmar`       | Confirmar orden (PENDIENTE → CONFIRMADA)         | —              | `200` Orden    |
| `POST`   | `/api/v1/ordenes/{id}/pago`            | Procesar pago (CONFIRMADA → PAGO_PROCESADO)      | `PagarOrdenRequest` | `200` Orden |
| `POST`   | `/api/v1/ordenes/{id}/en-preparacion`  | Marcar en preparación (PAGO_PROCESADO → EN_PREPARACION) | —       | `200` Orden    |
| `POST`   | `/api/v1/ordenes/{id}/enviada`         | Marcar como enviada (EN_PREPARACION → ENVIADA)   | `InfoEnvioRequest`  | `200` Orden |
| `POST`   | `/api/v1/ordenes/{id}/entregada`       | Marcar como entregada (ENVIADA → ENTREGADA)      | —              | `200` Orden    |
| `POST`   | `/api/v1/ordenes/{id}/cancelar`        | Cancelar orden                       | `CancelarOrdenRequest`          | `200` Orden    |

---

## Ventas / Carritos (`uamishop-ventas` · puerto 8083)

### Carritos `/api/v1/carritos`

| Método   | Ruta                                               | Descripción                     | Request Body               | Response        |
| -------- | -------------------------------------------------- | ------------------------------- | -------------------------- | --------------- |
| `POST`   | `/api/v1/carritos`                                 | Crear carrito                   | `CarritoRequest`           | `201` + Location |
| `GET`    | `/api/v1/carritos/{carritoId}`                     | Obtener carrito por ID          | —                          | `200` Carrito   |
| `POST`   | `/api/v1/carritos/{carritoId}/productos`           | Agregar producto al carrito     | `AgregarProductoRequest`   | `200` Carrito   |
| `PATCH`  | `/api/v1/carritos/{carritoId}/productos/{productoId}` | Modificar cantidad de producto | `ModificarCantidadRequest` | `200` Carrito   |
| `DELETE` | `/api/v1/carritos/{carritoId}/productos/{productoId}` | Eliminar producto del carrito  | —                          | `200` Carrito   |
| `DELETE` | `/api/v1/carritos/{carritoId}/productos`           | Vaciar carrito                  | —                          | `200` Carrito   |
| `POST`   | `/api/v1/carritos/{carritoId}/checkout`            | Iniciar checkout                | —                          | `200` Carrito   |
| `POST`   | `/api/v1/carritos/{carritoId}/checkout/completar`  | Completar checkout              | —                          | `200` Carrito   |
| `POST`   | `/api/v1/carritos/{carritoId}/abandonar`           | Abandonar carrito               | —                          | `200` Carrito   |

---

## Resumen de rutas del Gateway

| Patrón en Gateway        | Servicio destino           |
| ------------------------ | -------------------------- |
| `/api/v1/productos/**`   | `uamishop-catalogo:8081`   |
| `/api/v1/categorias/**`  | `uamishop-catalogo:8081`   |
| `/api/v1/ordenes/**`     | `uamishop-ordenes:8082`    |
| `/api/v1/carritos/**`    | `uamishop-ventas:8083`     |
