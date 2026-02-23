# Flujo de creación de recursos

1. Debe existir una categoría para crear un producto, si no existe, crear una.
2. Para crear una orden, debe existir un producto, si no existe, crear uno.
3. Para crear una orden de forma **directa** (`POST /api/v1/ordenes/directa`), solo se necesita el producto creado previamente y proporcionar los datos de envío directamente.
4. Para crear una orden **desde un carrito** (`POST /api/v1/ordenes/desde-carrito`), deben de seguirse los siguientes pasos:
   - Crear un carrito (`POST /api/v1/carritos`).
   - Agregar el producto al carrito (`POST /api/v1/carritos/{carritoId}/productos`).
   - Iniciar el checkout del carrito (`POST /api/v1/carritos/{carritoId}/checkout`) para que el carrito cambie a estado `EN_CHECKOUT`.
   - Crear la orden referenciando al `{carritoId}`.
