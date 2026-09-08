# Proyecto 2: Flujo de Envíos y Paquetería 📦

## 📋 Descripción del Proyecto

Desarrolla una API REST con Spring Boot para gestionar el ciclo de vida de envíos de una empresa de paquetería.

El sistema permitirá registrar paquetes, consultar y modificar sus datos logísticos y, sobre todo, controlar correctamente cómo cambia el **estado** de cada envío desde que se registra hasta que se entrega o cancela.

Este proyecto introduce una idea fundamental del backend profesional:

> **No todo cambio de datos válido a nivel técnico es válido a nivel de negocio.**

Un cliente no debe poder convertir directamente un envío `REGISTERED` en `DELIVERED` enviando un JSON. La aplicación debe decidir qué transiciones están permitidas.

Este proyecto continúa lo practicado en el Proyecto 1 y añade:

- Reglas de negocio basadas en estados.
- Operaciones REST que representan acciones del dominio.
- Uso de `409 Conflict` para conflictos de negocio.
- Campos que pueden modificarse y campos que deben permanecer inmutables.
- Prevención de códigos de seguimiento duplicados.
- Mayor responsabilidad de la capa `Service`.

---

## 🎯 Objetivos de Aprendizaje

Al completar este proyecto deberías ser capaz de:

1. Construir un CRUD REST completo utilizando Spring Boot y Spring Data JPA.
2. Mantener correctamente la arquitectura `Controller → Service → Repository`.
3. Aplicar Bean Validation a los datos recibidos.
4. Diferenciar **validación estructural** de **validación de negocio**.
5. Modelar estados mediante `enum` y persistirlos con `EnumType.STRING`.
6. Implementar reglas de transición entre estados.
7. Evitar que una actualización general (`PUT`) pueda alterar información controlada por el backend.
8. Detectar duplicados mediante el Repository.
9. Crear excepciones de negocio específicas y tratarlas globalmente.
10. Elegir entre `400`, `404` y `409` según la naturaleza real del error.
11. Mantener auditoría automática con `createdAt` y `updatedAt`.
12. Trabajar con métodos pequeños y responsabilidades claras en el Service.

---

## 📐 Requisitos Funcionales

### Entidad principal: `Shipment`

| Campo | Tipo | Descripción | Restricciones |
|---|---|---|---|
| `id` | `Long` | Identificador interno | Autogenerado |
| `trackingCode` | `String` | Código público de seguimiento | Obligatorio, único, 8-20 caracteres |
| `recipientName` | `String` | Nombre del destinatario | Obligatorio, 3-120 caracteres |
| `destinationCity` | `String` | Ciudad de destino | Obligatoria, 2-100 caracteres |
| `parcelType` | `ParcelType` | Tipo/tamaño del paquete | Obligatorio |
| `specialInstructions` | `String` | Observaciones para el reparto | Opcional, máximo 500 caracteres |
| `status` | `ShipmentStatus` | Estado actual del envío | Gestionado por el backend |
| `createdAt` | `LocalDateTime` | Momento de creación | Automático |
| `updatedAt` | `LocalDateTime` | Última modificación | Automático |

### `ParcelType`

- `DOCUMENTS`
- `SMALL_PACKAGE`
- `MEDIUM_PACKAGE`
- `LARGE_PACKAGE`

### `ShipmentStatus`

- `REGISTERED` — el envío ha sido registrado.
- `IN_TRANSIT` — ha salido hacia la red logística.
- `OUT_FOR_DELIVERY` — está en reparto final.
- `DELIVERED` — ha sido entregado.
- `CANCELLED` — ha sido cancelado antes de salir.

---

## 🔄 Reglas de transición de estado

Este es el núcleo del proyecto.

```text
REGISTERED ───────→ IN_TRANSIT ───────→ OUT_FOR_DELIVERY ───────→ DELIVERED
     │
     └────────────→ CANCELLED
```

### Transiciones permitidas

| Estado actual | Acción | Nuevo estado |
|---|---|---|
| `REGISTERED` | Despachar | `IN_TRANSIT` |
| `IN_TRANSIT` | Sacar a reparto | `OUT_FOR_DELIVERY` |
| `OUT_FOR_DELIVERY` | Confirmar entrega | `DELIVERED` |
| `REGISTERED` | Cancelar | `CANCELLED` |

Cualquier otra combinación debe ser rechazada con **409 Conflict**.

Ejemplos que deben fallar:

- `REGISTERED → DELIVERED`
- `REGISTERED → OUT_FOR_DELIVERY`
- `IN_TRANSIT → CANCELLED`
- `DELIVERED → IN_TRANSIT`
- `CANCELLED → REGISTERED`
- Ejecutar dos veces la misma transición.

`DELIVERED` y `CANCELLED` son estados finales.

---

## 🛠️ Requisitos Técnicos

### Stack recomendado

- Java 21.
- Spring Boot 4.1.x.
- Maven.
- Spring MVC.
- Spring Data JPA.
- Bean Validation.
- H2 para desarrollo.
- Postman o equivalente para probar la API.

### Dependencias principales

- `spring-boot-starter-webmvc`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `h2`
- `spring-boot-h2console` para la consola H2 en Spring Boot 4.

> En este proyecto **no es obligatorio utilizar Lombok**. Es preferible seguir practicando constructores, getters y setters de Java mientras consolidamos los fundamentos.

### Arquitectura esperada

```text
com.eduardo.shipmentworkflow
├── controller
│   └── ShipmentController
├── exception
│   ├── ApiError
│   ├── DuplicateTrackingCodeException
│   ├── GlobalExceptionHandler
│   ├── InvalidShipmentTransitionException
│   ├── ShipmentDeletionNotAllowedException
│   └── ShipmentNotFoundException
├── model
│   ├── ParcelType
│   ├── Shipment
│   └── ShipmentStatus
├── repository
│   └── ShipmentRepository
├── service
│   ├── ShipmentService
│   └── ShipmentServiceImpl
└── ShipmentWorkflowApplication
```

### Responsabilidades

#### Controller

Debe ocuparse de HTTP:

- Recibir parámetros y cuerpos JSON.
- Ejecutar validación mediante `@Valid`.
- Delegar en el Service.
- Construir las respuestas HTTP.

No debe decidir si una transición es válida.

#### Service

Debe contener las reglas del negocio:

- Verificar existencia del envío.
- Controlar duplicados.
- Decidir qué campos pueden actualizarse.
- Aplicar las transiciones de estado.
- Impedir eliminaciones no permitidas.

#### Repository

Debe limitarse al acceso a datos.

---

## 🔌 Endpoints a Implementar

### CRUD

| Método | URI | Operación | Respuesta esperada |
|---|---|---|---|
| `POST` | `/api/shipments` | Registrar envío | `201 Created` |
| `GET` | `/api/shipments` | Listar envíos | `200 OK` |
| `GET` | `/api/shipments/{id}` | Consultar envío | `200 OK` / `404` |
| `PUT` | `/api/shipments/{id}` | Modificar datos editables | `200 OK` / `404` / `409` |
| `DELETE` | `/api/shipments/{id}` | Eliminar envío cancelado | `204 No Content` / `404` / `409` |

### Acciones del flujo

| Método | URI | Transición | Respuesta |
|---|---|---|---|
| `PATCH` | `/api/shipments/{id}/dispatch` | `REGISTERED → IN_TRANSIT` | `200 OK` / `409` |
| `PATCH` | `/api/shipments/{id}/out-for-delivery` | `IN_TRANSIT → OUT_FOR_DELIVERY` | `200 OK` / `409` |
| `PATCH` | `/api/shipments/{id}/deliver` | `OUT_FOR_DELIVERY → DELIVERED` | `200 OK` / `409` |
| `PATCH` | `/api/shipments/{id}/cancel` | `REGISTERED → CANCELLED` | `200 OK` / `409` |

---

## 📤 Ejemplos de Payload

### Registrar envío

```http
POST /api/shipments
Content-Type: application/json
```

```json
{
  "trackingCode": "PKG20260001",
  "recipientName": "Lucía Gómez",
  "destinationCity": "Sevilla",
  "parcelType": "SMALL_PACKAGE",
  "specialInstructions": "Llamar antes de entregar"
}
```

La aplicación debe asignar automáticamente:

```json
"status": "REGISTERED"
```

aunque el cliente intente mandar otro estado.

### Actualizar información

```http
PUT /api/shipments/1
Content-Type: application/json
```

```json
{
  "trackingCode": "ESTE-VALOR-NO-DEBE-CAMBIAR",
  "recipientName": "Lucía Gómez Pérez",
  "destinationCity": "Dos Hermanas",
  "parcelType": "MEDIUM_PACKAGE",
  "specialInstructions": "Entregar por la tarde",
  "status": "DELIVERED"
}
```

El `PUT` **no debe permitir modificar**:

- `id`
- `trackingCode`
- `status`
- `createdAt`

En este módulo todavía utilizamos la entidad directamente como entrada/salida. Esta limitación quedará resuelta profesionalmente con DTOs en el Módulo 2.

---

## ✅ Validaciones Requeridas

### Bean Validation

#### `trackingCode`

- Obligatorio.
- Entre 8 y 20 caracteres.
- Solo mayúsculas y números.
- Debe ser único.

Ejemplo válido:

```text
PKG20260001
```

#### `recipientName`

- Obligatorio.
- Entre 3 y 120 caracteres.

#### `destinationCity`

- Obligatoria.
- Entre 2 y 100 caracteres.

#### `parcelType`

- Obligatorio.

#### `specialInstructions`

- Opcional.
- Máximo 500 caracteres.

### Validaciones de negocio

1. Un `trackingCode` no puede estar repetido.
2. Al crear un envío, el estado siempre será `REGISTERED`.
3. Un `PUT` no puede alterar directamente el estado.
4. Un `PUT` no puede cambiar el código de seguimiento original.
5. Solo pueden ejecutarse las transiciones indicadas en la tabla de estados.
6. Un envío `DELIVERED` no puede cambiar de estado.
7. Un envío `CANCELLED` no puede reactivarse.
8. **Solo se puede eliminar físicamente un envío cuyo estado sea `CANCELLED`.**
9. Cualquier operación sobre un ID inexistente debe producir `404 Not Found`.

---

## ⚠️ Códigos HTTP y Errores

Debes distinguir correctamente:

### `400 Bad Request`

El cliente ha enviado datos estructuralmente inválidos.

Ejemplos:

- `recipientName` vacío.
- Código demasiado corto.
- `parcelType` inexistente.

### `404 Not Found`

El envío solicitado no existe.

### `409 Conflict`

La petición es técnicamente comprensible, pero entra en conflicto con el estado actual o con una regla del sistema.

Ejemplos:

- Código de seguimiento duplicado.
- Intentar entregar un paquete todavía `REGISTERED`.
- Intentar cancelar un envío `IN_TRANSIT`.
- Intentar borrar un envío que no esté `CANCELLED`.

---

## 🧠 Conceptos Clave a Aplicar

### 1. El estado pertenece a la lógica de negocio

No hagas esto en el Controller:

```java
if (shipment.getStatus() == ShipmentStatus.REGISTERED) {
    shipment.setStatus(ShipmentStatus.IN_TRANSIT);
}
```

La decisión pertenece al Service.

### 2. No confundir `PUT` con una transición de negocio

Una modificación de dirección y una confirmación de entrega son operaciones conceptualmente distintas.

Por eso tendremos:

```text
PUT   /api/shipments/{id}
PATCH /api/shipments/{id}/deliver
```

### 3. No confiar en valores controlados por el cliente

Aunque el JSON recibido contenga:

```json
{
  "status": "DELIVERED"
}
```

un envío recién creado debe seguir comenzando como `REGISTERED`.

### 4. Método auxiliar para buscar o lanzar excepción

Cuando varios métodos del Service necesiten recuperar un envío por ID, evita repetir continuamente la misma lógica.

Piensa en un método privado que centralice:

```text
buscar → si existe devolver → si no lanzar excepción
```

### 5. `EnumType.STRING`

Los estados deben persistirse por nombre, no por posición numérica.

---

## 🚀 Criterios de Aceptación

El proyecto estará terminado cuando:

- [ ] El proyecto arranca sin errores.
- [ ] Se pueden registrar envíos.
- [ ] El alta devuelve `201 Created`.
- [ ] La respuesta de creación contiene una cabecera `Location` válida.
- [ ] Todos los envíos nuevos comienzan en `REGISTERED`.
- [ ] El código de seguimiento no puede repetirse.
- [ ] El listado funciona.
- [ ] La consulta por ID devuelve `404` cuando corresponde.
- [ ] El `PUT` modifica únicamente los campos editables.
- [ ] El `PUT` no cambia el código de seguimiento.
- [ ] El `PUT` no cambia el estado.
- [ ] `dispatch` funciona únicamente desde `REGISTERED`.
- [ ] `out-for-delivery` funciona únicamente desde `IN_TRANSIT`.
- [ ] `deliver` funciona únicamente desde `OUT_FOR_DELIVERY`.
- [ ] `cancel` funciona únicamente desde `REGISTERED`.
- [ ] Las transiciones inválidas devuelven `409 Conflict`.
- [ ] Solo un envío `CANCELLED` puede eliminarse.
- [ ] Bean Validation produce respuestas `400` claras.
- [ ] Los errores tienen una estructura JSON consistente.
- [ ] `createdAt` se genera automáticamente.
- [ ] `updatedAt` cambia cuando corresponde.
- [ ] El Controller no contiene reglas de negocio.
- [ ] El Controller nunca accede directamente al Repository.

---

## 🌟 Funcionalidades Extra Opcionales

Cuando el proyecto obligatorio funcione puedes ampliar con alguno de estos desafíos:

1. **Consultar historial básico de cambios de estado en memoria** durante la ejecución.
2. **Filtrar por estado** mediante `GET /api/shipments?status=IN_TRANSIT`.
3. **Buscar por código de seguimiento**.
4. Impedir modificar los datos del destinatario después de entrar en `OUT_FOR_DELIVERY`.
5. Crear un estado `DELIVERY_FAILED` y diseñar tú mismo las transiciones válidas.
6. Añadir una operación de devolución sin romper las reglas actuales.

> Los filtros son opcionales aquí porque se trabajarán expresamente en un proyecto posterior.

---

## 📝 Notas Importantes

### No conviertas el Service en un simple puente

Esto no es suficiente:

```text
Controller → Service → repository.save(...)
```

En este proyecto el Service debe aportar valor real. Ahí vive el flujo de estados.

### No escribas un método universal `changeStatus`

Para este nivel resulta más interesante crear operaciones expresivas como:

```text
dispatchShipment(...)
markOutForDelivery(...)
deliverShipment(...)
cancelShipment(...)
```

Así el código representa acciones reales del dominio.

### No utilices `EnumType.ORDINAL`

Si mañana insertas un nuevo estado en medio del enum, los valores numéricos existentes podrían cambiar de significado.

### Cuidado con sobrescribir la entidad completa en un `PUT`

Recibir una entidad nueva y hacer directamente `repository.save(requestBody)` puede provocar que:

- se pierda el ID correcto,
- se altere el estado,
- cambie el código de seguimiento,
- se pierda información de auditoría.

Recupera primero la entidad existente y modifica únicamente lo permitido.

### `409 Conflict` no es `400 Bad Request`

Un paquete existente en `DELIVERED` es un recurso válido. El problema es que su **estado actual** no permite realizar la acción solicitada.

---

## 🧪 Cómo Probar tu API

Se incluye una colección de Postman en:

```text
/postman/Project2_ShipmentWorkflow.postman_collection.json
```

### Secuencia mínima recomendada

1. Crear un envío.
2. Comprobar que aparece como `REGISTERED`.
3. Intentar crearlo otra vez con el mismo `trackingCode` → debe fallar con `409`.
4. Intentar entregarlo directamente → debe fallar con `409`.
5. Ejecutar `dispatch`.
6. Ejecutar `out-for-delivery`.
7. Ejecutar `deliver`.
8. Intentar cancelar el envío entregado → `409`.
9. Crear un segundo envío.
10. Cancelarlo mientras está `REGISTERED`.
11. Eliminarlo → `204`.
12. Intentar eliminar el primer envío entregado → `409`.
13. Enviar datos inválidos para comprobar los `400`.

---

## 📚 Recursos Útiles

- Spring Boot Reference Documentation.
- Spring MVC / REST Controllers.
- Spring Data JPA.
- Jakarta Bean Validation.
- `ResponseEntity`.
- Java `enum`.
- HTTP Semantics: códigos `400`, `404` y `409`.

Conceptos para repasar del bootcamp:

- `@RestController`
- `@RequestMapping`
- `@PathVariable`
- `@RequestBody`
- `@Valid`
- `@Service`
- `JpaRepository`
- `Optional`
- `@RestControllerAdvice`
- `@ExceptionHandler`
- `@Enumerated(EnumType.STRING)`
- `@CreatedDate`
- `@LastModifiedDate`

---

## 🗺️ Plan de Desarrollo Sugerido

### Fase 1 — Modelo

1. Crear `ParcelType`.
2. Crear `ShipmentStatus`.
3. Crear la entidad `Shipment`.
4. Añadir Bean Validation.
5. Añadir auditoría.

No continúes hasta comprender qué campos controla el usuario y cuáles controla el backend.

### Fase 2 — Persistencia

1. Crear `ShipmentRepository`.
2. Añadir la operación necesaria para comprobar si existe un `trackingCode`.
3. Arrancar la aplicación y verificar la creación de la tabla.

### Fase 3 — CRUD básico

1. Crear el Service.
2. Implementar alta.
3. Implementar listado.
4. Implementar consulta por ID.
5. Implementar actualización.
6. Implementar eliminación con su regla de negocio.
7. Exponer los endpoints desde el Controller.

### Fase 4 — Excepciones

1. Crear `ShipmentNotFoundException`.
2. Crear excepción para código duplicado.
3. Crear excepción para transición inválida.
4. Crear excepción para borrado no permitido.
5. Crear `GlobalExceptionHandler`.
6. Unificar el formato JSON de errores.

### Fase 5 — Flujo de estados

Implementa en este orden:

1. `REGISTERED → IN_TRANSIT`
2. `IN_TRANSIT → OUT_FOR_DELIVERY`
3. `OUT_FOR_DELIVERY → DELIVERED`
4. `REGISTERED → CANCELLED`

Después prueba expresamente todas las combinaciones inválidas.

### Fase 6 — Revisión profesional

Comprueba:

1. ¿Hay reglas de negocio en el Controller?
2. ¿Accede el Controller al Repository?
3. ¿Hay código repetido para buscar por ID?
4. ¿Puede el cliente modificar el estado con el `PUT`?
5. ¿Puede modificar `trackingCode` una vez creado?
6. ¿Los conflictos devuelven realmente `409`?
7. ¿Las excepciones tienen nombres específicos y comprensibles?

---

## 💡 Consejo Final

En el Proyecto 1 el objetivo era aprender a construir correctamente el recorrido completo de un CRUD.

En este Proyecto 2 empieza una de las diferencias más importantes entre un CRUD académico y un backend profesional:

> **el backend no se limita a guardar lo que recibe; protege las reglas del negocio.**

No intentes resolver el flujo de estados con muchos `if` improvisados dentro del Controller. Diseña primero las reglas, decide qué capa es responsable de ellas y después implementa.

Cuando una operación falle, no pienses únicamente en *“¿cómo hago que funcione?”*. Pregúntate también:

**“¿Qué regla del dominio estoy intentando representar con este código?”**

---

**Proyecto 2/20 — Módulo 1: CRUDs Básicos y Fundamentos**
