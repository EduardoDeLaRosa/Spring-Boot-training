# Proyecto 1 — API de Solicitudes de Mantenimiento 🛠️

> **Módulo 1 · CRUDs Básicos y Fundamentos**  
> Dificultad: ⭐☆☆☆☆  
> Objetivo principal: construir un CRUD REST completo respetando una arquitectura backend limpia antes de introducir DTOs y relaciones JPA.

---

## 📋 1. Descripción del proyecto

Una empresa que gestiona oficinas y edificios necesita una pequeña API para registrar **solicitudes de mantenimiento** comunicadas por empleados o responsables de las instalaciones.

Cada solicitud representa una incidencia concreta: una avería eléctrica, una fuga de agua, un problema de climatización, una incidencia de seguridad, etc. El sistema debe permitir registrar la solicitud, consultarla, modificarla y eliminarla.

En este primer proyecto **no habrá relaciones entre entidades ni DTOs de dominio**. Trabajaremos deliberadamente con una única entidad para que el foco esté en dominar:

- el flujo `Controller → Service → Repository → JPA`,
- la validación de entradas,
- el manejo global de excepciones,
- los códigos HTTP correctos,
- la persistencia con Spring Data JPA,
- y la auditoría automática mediante `createdAt` y `updatedAt`.

> ⚠️ **Regla de trabajo:** intenta completar primero `/working`. No abras `/solution` hasta haber realizado tu propia implementación o hasta que quieras comparar una decisión concreta.

---

## 🎯 2. Objetivos de aprendizaje

Al completar este proyecto deberías ser capaz de:

1. Crear una entidad JPA correctamente modelada.
2. Configurar una clave primaria autogenerada.
3. Implementar un repositorio con `JpaRepository`.
4. Separar correctamente responsabilidades entre Controller, Service y Repository.
5. Implementar un CRUD REST completo.
6. Utilizar `ResponseEntity` y códigos HTTP adecuados.
7. Aplicar Bean Validation sobre los datos recibidos.
8. Capturar errores de validación mediante un `@RestControllerAdvice`.
9. Crear y lanzar una excepción de recurso no encontrado.
10. Implementar timestamps automáticos de creación y modificación mediante auditoría JPA.
11. Evitar que el Controller contenga reglas de negocio o acceso directo al Repository.
12. Mantener una API consistente ante escenarios correctos e incorrectos.

---

## 📐 3. Requisitos funcionales

### Entidad principal: `MaintenanceRequest`

| Campo | Tipo | Descripción | Restricciones |
|---|---|---|---|
| `id` | `Long` | Identificador único | Autogenerado |
| `title` | `String` | Resumen de la incidencia | Obligatorio, 5-120 caracteres |
| `description` | `String` | Detalle del problema | Obligatorio, 10-1000 caracteres |
| `requesterName` | `String` | Persona que registra la solicitud | Obligatorio, 3-80 caracteres |
| `requesterEmail` | `String` | Email de contacto | Obligatorio, formato email válido, máx. 150 |
| `location` | `String` | Zona donde ocurre la incidencia | Obligatorio, 2-120 caracteres |
| `category` | `MaintenanceCategory` | Tipo de incidencia | Obligatorio |
| `priority` | `Priority` | Prioridad operativa | Obligatorio |
| `createdAt` | `LocalDateTime` | Fecha/hora de alta | Automática, solo lectura |
| `updatedAt` | `LocalDateTime` | Última modificación | Automática, solo lectura |

### Categorías (`MaintenanceCategory`)

- `ELECTRICAL`
- `PLUMBING`
- `HVAC`
- `ELEVATOR`
- `CLEANING`
- `SECURITY`
- `FURNITURE`
- `OTHER`

### Prioridades (`Priority`)

- `LOW`
- `MEDIUM`
- `HIGH`
- `URGENT`

### Comportamiento esperado

La API debe permitir:

- crear solicitudes,
- listar todas las solicitudes,
- recuperar una solicitud por ID,
- actualizar los datos editables de una solicitud,
- eliminar una solicitud,
- devolver `404 Not Found` cuando el ID no exista,
- devolver `400 Bad Request` cuando el JSON incumpla las validaciones,
- asignar automáticamente `id`, `createdAt` y `updatedAt`,
- y mantener `createdAt` sin cambios después de una actualización.

---

## 🛠️ 4. Requisitos técnicos

### Stack recomendado

- Java 21
- Spring Boot 4.1.x
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- H2 Database
- Maven

### Dependencias mínimas

- `spring-boot-starter-webmvc`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `h2`
- `spring-boot-h2console` (solo para inspección local durante desarrollo)

### Arquitectura esperada

```text
src/main/java/com/eduardo/maintenancerequests/
├── MaintenanceRequestsApplication.java
├── controller/
│   └── MaintenanceRequestController.java
├── model/
│   ├── MaintenanceRequest.java
│   ├── MaintenanceCategory.java
│   └── Priority.java
├── repository/
│   └── MaintenanceRequestRepository.java
├── service/
│   ├── MaintenanceRequestService.java
│   └── MaintenanceRequestServiceImpl.java
└── exception/
    ├── MaintenanceRequestNotFoundException.java
    ├── ApiError.java
    └── GlobalExceptionHandler.java
```

### Restricciones arquitectónicas

- El Controller **no puede inyectar el Repository**.
- El acceso a datos debe pasar por el Service.
- El Service debe resolver la existencia del recurso antes de actualizarlo o eliminarlo.
- El Controller debe ocuparse principalmente del protocolo HTTP.
- No se debe utilizar `CommandLineRunner` para insertar datos temporales.
- No se deben utilizar relaciones JPA en este proyecto.
- No es obligatorio utilizar DTOs de dominio todavía; se introducirán de forma estricta en el Módulo 2.

---

## 🔌 5. Endpoints a implementar

| Método | URI | Función | Respuesta correcta |
|---|---|---|---|
| `POST` | `/api/maintenance-requests` | Crear solicitud | `201 Created` |
| `GET` | `/api/maintenance-requests` | Listar solicitudes | `200 OK` |
| `GET` | `/api/maintenance-requests/{id}` | Consultar por ID | `200 OK` / `404 Not Found` |
| `PUT` | `/api/maintenance-requests/{id}` | Actualizar solicitud | `200 OK` / `404 Not Found` |
| `DELETE` | `/api/maintenance-requests/{id}` | Eliminar solicitud | `204 No Content` / `404 Not Found` |

### Ejemplo de creación

```http
POST /api/maintenance-requests
Content-Type: application/json
```

```json
{
  "title": "Fuga de agua en cocina",
  "description": "Se observa una fuga continua debajo del fregadero de la cocina principal.",
  "requesterName": "Ana García",
  "requesterEmail": "ana.garcia@empresa.com",
  "location": "Edificio A - Planta 2 - Cocina",
  "category": "PLUMBING",
  "priority": "HIGH"
}
```

Respuesta esperada:

```text
201 Created
Location: http://localhost:8080/api/maintenance-requests/1
```

El cuerpo deberá contener el recurso creado con su `id`, `createdAt` y `updatedAt` generados por la aplicación.

### Actualización

El `PUT` debe actualizar los campos editables, pero:

- el `id` se conserva,
- `createdAt` se conserva,
- `updatedAt` debe cambiar automáticamente.

---

## ✅ 6. Validaciones requeridas

### `title`

- `@NotBlank`
- entre 5 y 120 caracteres

### `description`

- `@NotBlank`
- entre 10 y 1000 caracteres

### `requesterName`

- `@NotBlank`
- entre 3 y 80 caracteres

### `requesterEmail`

- `@NotBlank`
- `@Email`
- máximo 150 caracteres

### `location`

- `@NotBlank`
- entre 2 y 120 caracteres

### `category`

- no puede ser `null`
- debe contener un valor válido de `MaintenanceCategory`

### `priority`

- no puede ser `null`
- debe contener un valor válido de `Priority`

### Mensajes

Los mensajes de validación deben ser comprensibles. Evita respuestas del estilo:

```text
Validation failed for object...
```

La respuesta de error debe permitir identificar qué campo ha fallado y por qué.

---

## 🎓 7. Conceptos clave a aplicar

### Separación de responsabilidades

```text
HTTP Request
     ↓
Controller
     ↓
Service
     ↓
Repository
     ↓
JPA / Hibernate
     ↓
Database
```

Piensa siempre qué capa debe tomar cada decisión.

### `Optional`

`findById` devuelve un `Optional`. No devuelvas `null` para representar un recurso inexistente. Convierte la ausencia del recurso en una excepción de negocio comprensible.

### `ResponseEntity`

No todos los endpoints deben devolver `200 OK`:

- creación → `201 Created`,
- consulta → `200 OK`,
- actualización → `200 OK`,
- eliminación → `204 No Content`,
- recurso inexistente → `404 Not Found`,
- datos inválidos → `400 Bad Request`.

### Auditoría

Los timestamps deben ser responsabilidad de la aplicación/persistencia, no del cliente.

El cliente **no debería decidir** cuándo se creó o modificó un registro.

### Inyección por constructor

Utiliza inyección por constructor en lugar de `@Autowired` sobre atributos.

---

## 🚀 8. Criterios de aceptación

El proyecto se considera finalizado cuando:

- [ ] La aplicación arranca sin errores.
- [ ] La entidad se persiste correctamente mediante JPA.
- [ ] `POST` devuelve `201 Created`.
- [ ] La respuesta de creación contiene un `Location` válido.
- [ ] `GET /api/maintenance-requests` devuelve todas las solicitudes.
- [ ] `GET /{id}` devuelve `404` para un ID inexistente.
- [ ] `PUT /{id}` modifica el registro existente sin crear otro nuevo.
- [ ] `DELETE /{id}` devuelve `204` cuando elimina correctamente.
- [ ] Eliminar un ID inexistente devuelve `404`.
- [ ] Las validaciones devuelven `400` con mensajes legibles.
- [ ] Un email con formato incorrecto es rechazado.
- [ ] Un enum inexistente es rechazado por la API.
- [ ] `createdAt` se genera automáticamente.
- [ ] `updatedAt` se genera automáticamente.
- [ ] `createdAt` no cambia después de hacer un `PUT`.
- [ ] El Controller no utiliza directamente el Repository.
- [ ] La lógica de búsqueda/actualización/eliminación está en el Service.
- [ ] No se utiliza `CommandLineRunner`.
- [ ] Los nombres de clases, métodos y variables siguen las convenciones Java.

---

## 🌟 9. Funcionalidades extra opcionales

Cuando tengas el CRUD terminado puedes intentar, sin mirar `/solution`:

1. **Ordenar el listado** por `createdAt` descendente.
2. **Filtrar por prioridad** mediante query methods.
3. **Filtrar por categoría** mediante query methods.
4. Crear un endpoint que devuelva el número total de solicitudes.
5. Permitir buscar solicitudes cuyo título contenga un texto ignorando mayúsculas/minúsculas.
6. Cambiar H2 por MySQL y comprobar que el código Java apenas necesita modificaciones.

> Estos extras no forman parte del mínimo obligatorio del Proyecto 1. Algunos aparecerán como requisito principal en proyectos posteriores.

---

## 📝 10. Notas importantes

### No uses el Repository desde el Controller

Antipatrón:

```text
Controller → Repository
```

Objetivo:

```text
Controller → Service → Repository
```

### No confíes únicamente en `repository.save()` para actualizar

Antes de actualizar debes determinar qué ocurre si el recurso solicitado no existe. El comportamiento de la API tiene que estar definido por ti, no quedar como efecto accidental de JPA.

### No devuelvas `null` cuando no exista el recurso

La ausencia de un recurso debe convertirse en una respuesta HTTP adecuada.

### No permitas que el cliente controle los timestamps

`createdAt` y `updatedAt` son información interna de auditoría.

### DTOs

En una API empresarial normalmente no expondríamos directamente una entidad JPA como contrato HTTP. **En este proyecto se permite de forma intencionada por motivos pedagógicos.** En el Módulo 2 esta decisión desaparecerá y utilizaremos DTOs Request/Response y mapeos.

### `ddl-auto`

El proyecto utiliza `spring.jpa.hibernate.ddl-auto=update` únicamente para facilitar el desarrollo local. En producción se utilizan estrategias de migración controladas, como Flyway o Liquibase.

---

## 🧪 11. Cómo probar tu API

Puedes utilizar Postman, Insomnia o cURL.

### Caso 1 — Crear correctamente

Envía el JSON de ejemplo al endpoint `POST`.

Comprueba:

- código `201`,
- cabecera `Location`,
- `id` generado,
- timestamps generados.

### Caso 2 — Datos inválidos

Prueba, por separado:

- título vacío,
- descripción demasiado corta,
- email `esto-no-es-un-email`,
- `category: null`,
- prioridad inexistente.

Debes obtener `400 Bad Request`.

### Caso 3 — ID inexistente

```http
GET /api/maintenance-requests/999999
```

Debe devolver `404 Not Found`.

### Caso 4 — Actualización

1. Crea una solicitud.
2. Guarda el valor de `createdAt`.
3. Actualízala con `PUT`.
4. Comprueba que `createdAt` sigue igual y `updatedAt` cambia.

### Caso 5 — Eliminación

1. Elimina un recurso existente.
2. Comprueba `204 No Content`.
3. Intenta recuperarlo.
4. Debes obtener `404 Not Found`.

---

## 📚 12. Recursos útiles

- Spring Boot Reference Documentation
- Spring Data JPA Reference Documentation
- Spring Framework — Validation
- Jakarta Bean Validation
- Java `Optional`
- HTTP Semantics — códigos `200`, `201`, `204`, `400` y `404`
- JPA Auditing (`@CreatedDate`, `@LastModifiedDate`)

Documentación oficial recomendada:

- https://docs.spring.io/spring-boot/reference/
- https://docs.spring.io/spring-data/jpa/reference/
- https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html

---

## 🗺️ 13. Plan de desarrollo sugerido

### Paso 1 — Entender el dominio

Antes de programar, identifica:

- qué representa una solicitud,
- qué campos son obligatorios,
- qué campos controla el cliente,
- qué campos controla el servidor.

### Paso 2 — Crear enums

Implementa:

- `MaintenanceCategory`
- `Priority`

### Paso 3 — Crear la entidad

Implementa `MaintenanceRequest` con:

- mapeo JPA,
- validaciones,
- clave primaria,
- enums almacenados como texto.

No implementes todavía el Controller.

### Paso 4 — Crear Repository

Crea la abstracción de persistencia utilizando Spring Data JPA.

### Paso 5 — Crear Service

Primero diseña qué operaciones necesita el caso de uso:

- crear,
- listar,
- buscar por ID,
- actualizar,
- eliminar.

Después implementa el Service.

### Paso 6 — Recurso no encontrado

Implementa la excepción específica y úsala desde el Service.

### Paso 7 — Crear Controller

Conecta HTTP con tu Service.

Presta especial atención a los códigos de estado.

### Paso 8 — Bean Validation

Añade `@Valid` en el punto correcto y prueba entradas incorrectas.

### Paso 9 — Manejo global de errores

Implementa el `@RestControllerAdvice` y transforma los errores en una respuesta consistente.

### Paso 10 — Auditoría

Configura `createdAt` y `updatedAt` para que se actualicen automáticamente.

### Paso 11 — Prueba completa

Ejecuta todos los casos indicados en la sección de pruebas y revisa el checklist de aceptación uno a uno.

### Paso 12 — Revisión arquitectónica

Antes de mirar `/solution`, pregúntate:

- ¿mi Controller conoce JPA?
- ¿mi Service conoce HTTP?
- ¿estoy repitiendo lógica?
- ¿qué ocurre con un ID inexistente?
- ¿qué ocurre con JSON inválido?
- ¿el cliente puede modificar datos que debería controlar el servidor?

---

## 💡 14. Consejo final

Este proyecto es deliberadamente pequeño. La dificultad no está en escribir cinco endpoints, sino en conseguir que **cada pieza esté en su sitio**.

Un CRUD mal estructurado puede escribirse en pocos minutos. Un CRUD sencillo con responsabilidades claras, validación consistente, errores coherentes y persistencia bien controlada es la base sobre la que después construiremos DTOs, relaciones, seguridad y tests.

Cuando termines `/working`, compáralo con `/solution` preguntándote no solo **qué código es diferente**, sino **por qué se ha tomado cada decisión**.

**Objetivo del Proyecto 1: que CRUD deje de significar “sé hacer `save()`” y pase a significar “sé construir correctamente el flujo completo de una API”.**
