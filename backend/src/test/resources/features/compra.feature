# language: es
Característica: Compra de productos en FitStore
  Como cliente de FitStore
  Quiero comprar productos del catálogo
  Para recibir mi pedido en el domicilio

  Antecedentes:
    Dado que el servidor REST está disponible en "http://localhost:8080/api"
    Y existen los siguientes productos en el catálogo:
      | id | nombre                | categoría  | precio | stock | emoji |
      | 1  | Proteína Whey 2kg     | SUPLEMENTO | 45000  | 10    | 💪    |
      | 2  | Mancuerna 5kg         | EQUIPO     | 25000  | 5     | 🏋️   |
      | 3  | Pantalón Gym          | ROPA       | 35000  | 8     | 👖    |
      | 4  | Creatina Monohidrato  | SUPLEMENTO | 30000  | 2     | 🥤    |
    Y existe un cliente registrado con:
      | email              | nombre  | clave      | rol    |
      | juan@email.com     | Juan    | clave123   | CLIENT |
      | admin@fitstore.com | Admin   | admin123   | ADMIN  |


  Escenario: Compra exitosa de múltiples productos
    Dado que soy usuario autenticado como "juan@email.com" con token JWT
    Y tengo el carrito vacío
    Cuando agrego 2 unidades del producto "Proteína Whey 2kg" al carrito
    Y agrego 1 unidad del producto "Mancuerna 5kg" al carrito
    Y agrego 1 unidad del producto "Pantalón Gym" al carrito
    Entonces el total del carrito es "$130,000"
    Y el carrito contiene 4 items
    Cuando confirmo la compra
    Entonces recibo un pedido con estado "CONFIRMADO"
    Y el pedido contiene 3 líneas de items
    Y el stock de "Proteína Whey 2kg" se reduce de 10 a 8
    Y el stock de "Mancuerna 5kg" se reduce de 5 a 4
    Y el stock de "Pantalón Gym" se reduce de 8 a 7
    Y se envía un email de confirmación a "juan@email.com"


  Escenario: Compra con stock insuficiente
    Dado que soy usuario autenticado como "juan@email.com" con token JWT
    Y el producto "Creatina Monohidrato" tiene stock de 2 unidades
    Cuando intento agregar 3 unidades del producto "Creatina Monohidrato" al carrito
    Entonces recibo un mensaje de error "Stock insuficiente"
    Y el producto no se agrega al carrito
    Y el carrito sigue vacío


  Escenario: Compra rechazada por stock insuficiente durante confirmación
    Dado que soy usuario autenticado como "juan@email.com" con token JWT
    Y agrego 2 unidades del producto "Creatina Monohidrato" al carrito
    Cuando otro cliente compra 1 unidad de "Creatina Monohidrato"
    Y intento confirmar mi compra
    Entonces recibo un error de validación
    Y el pedido NO se crea
    Y el carrito se vacía


  Escenario: Login fallido con credenciales inválidas
    Dado que intento iniciar sesión con email "juan@email.com" y clave incorrecta "claveErrada"
    Entonces recibo un error de autenticación
    Y no recibo token JWT
    Y sigo sin sesión activa


  Escenario: Login fallido con usuario no registrado
    Dado que intento iniciar sesión con email "noexiste@email.com" y clave "cualquiera123"
    Entonces recibo un error de autenticación "Usuario no encontrado"
    Y no recibo token JWT


  Escenario: Verificación de stock en tiempo real desde la API
    Dado que soy usuario autenticado como "juan@email.com" con token JWT
    Cuando consulto el stock del producto "Proteína Whey 2kg" (id: 1)
    Entonces obtengo la información del producto con stock actual 10
    Cuando otro cliente compra 3 unidades de "Proteína Whey 2kg"
    Y vuelvo a consultar el stock del producto "Proteína Whey 2kg"
    Entonces el stock se actualiza a 7


  Escenario: Verificación de disponibilidad en stock (endpoint dedicado)
    Dado que soy usuario autenticado como "juan@email.com" con token JWT
    Cuando verifico disponibilidad para 5 unidades del producto "Mancuerna 5kg"
    Entonces recibo respuesta: disponible=true
    Cuando verifico disponibilidad para 6 unidades del producto "Mancuerna 5kg"
    Entonces recibo respuesta: disponible=false


  Escenario: Stock actualizado en Redis después de compra
    Dado que el caché Redis está disponible
    Y el stock de "Proteína Whey 2kg" en Redis es 10
    Cuando compro 2 unidades de "Proteína Whey 2kg"
    Entonces el stock en Redis se actualiza a 8
    Y el stock en BD también es 8
    Cuando consulto nuevamente desde otra sesión
    Entonces obtengo el stock desde Redis (caché) con valor 8


  Escenario: Historial de cambios de stock en admin
    Dado que soy usuario autenticado como "admin@fitstore.com" con token JWT
    Cuando accedo al panel de admin
    Entonces puedo ver la tabla de productos con stock actual
    Cuando actualizo el stock de "Proteína Whey 2kg" a 20
    Entonces se guarda el nuevo stock en BD
    Y se actualiza el caché Redis
    Y aparece un evento de auditoría con timestamp


  Escenario: Carrito persiste en localStorage
    Dado que soy usuario autenticado como "juan@email.com" con token JWT
    Y agrego 2 unidades de "Proteína Whey 2kg" al carrito
    Y agrego 1 unidad de "Mancuerna 5kg" al carrito
    Entonces el carrito se persiste en localStorage
    Cuando recargo la página
    Entonces el carrito contiene los mismos 3 items
    Y el total sigue siendo el mismo


  Escenario: Categorías de productos filtradas correctamente
    Dado que accedo a la vista de catálogo
    Cuando filtro por categoría "SUPLEMENTO"
    Entonces solo veo productos de categoría SUPLEMENTO:
      | nombre                | precio |
      | Proteína Whey 2kg     | 45000  |
      | Creatina Monohidrato  | 30000  |
    Cuando filtro por categoría "EQUIPO"
    Entonces solo veo productos de categoría EQUIPO:
      | nombre       | precio |
      | Mancuerna 5kg | 25000  |
    Cuando filtro por categoría "ROPA"
    Entonces solo veo productos de categoría ROPA:
      | nombre        | precio |
      | Pantalón Gym  | 35000  |


  Escenario: Búsqueda de productos por nombre
    Dado que accedo a la vista de catálogo
    Cuando busco "Proteína"
    Entonces aparece el producto "Proteína Whey 2kg"
    Y no aparecen otros productos
    Cuando busco "5kg"
    Entonces aparecen los productos que contienen "5kg":
      | nombre        |
      | Mancuerna 5kg |


  Escenario: Estados de pedido transitan correctamente
    Dado que compro productos como cliente "juan@email.com"
    Y recibo un pedido con estado "CONFIRMADO"
    Cuando accedo como admin a "admin@fitstore.com"
    Y cambio el estado del pedido a "EN_CAMINO"
    Entonces el cliente ve su pedido con estado "EN_CAMINO"
    Cuando cambio el estado a "ENTREGADO"
    Entonces el cliente ve su pedido con estado "ENTREGADO"
    Y el pedido aparece en el historial de compras realizadas


  Escenario: Cancelación de pedido por admin
    Dado que existe un pedido con estado "CONFIRMADO"
    Cuando accedo como admin y cambio el estado a "CANCELADO"
    Entonces el stock de todos los productos se restaura
    Y el cliente recibe notificación de cancelación


  Escenario: Validación de email en notificaciones
    Dado que compro productos como "juan@email.com"
    Cuando confirmo la compra
    Entonces se envía email a "juan@email.com"
    Y el email contiene el número del pedido
    Y el email contiene el desglose de items
    Y el email contiene el total a pagar
