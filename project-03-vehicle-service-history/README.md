# Proyecto 3: Historial de Mantenimiento de Vehículos 🚗🔧

## 📋 1. Descripción del Proyecto

Desarrolla una API REST con Spring Boot para registrar y consultar el historial de mantenimiento de vehículos.

Una empresa de renting, un taller o incluso un pequeño gestor de flota necesita saber **qué mantenimiento se realizó, a qué vehículo, en qué fecha, con qué kilometraje y cuándo debería revisarse de nuevo**.

A diferencia de los proyectos anteriores, aquí el reto principal no será controlar un flujo de estados, sino trabajar correctamente con **fechas, rangos temporales, enumeraciones y filtros mediante Spring Data JPA**.

El sistema permitirá:

- Registrar actuaciones de mantenimiento.
- Consultar el historial completo.
- Buscar mantenimientos de un vehículo concreto.
- Filtrar por tipo de mantenimiento.
- Consultar actuaciones realizadas entre dos fechas.
- Obtener las revisiones que vencen próximamente.
- Consultar cuál es el último mantenimiento registrado de un vehículo.
- Validar reglas donde una fecha depende de otra.

Este proyecto continúa la progresión del Módulo 1:

```text
Proyecto 1 → CRUD + capas + validación + excepciones
Proyecto 2 → estados + transiciones + reglas de negocio
Proyecto 3 → fechas + enums + filtros + consultas derivadas
```

> **Idea central:** que un dato tenga el tipo correcto no significa que sea válido para el negocio. `2026-03-01` y `2025-01-01` son dos fechas válidas, pero una fecha de próxima revisión anterior al mantenimiento que la genera no tiene sentido.

---

## 🎯 2. Objetivos de Aprendizaje

Al completar este proyecto deberías ser capaz de:

1. Construir nuevamente un CRUD REST completo sin depender de código copiado.
2. Mantener la separación `Controller → Service → Repository`.
3. Trabajar con `LocalDate` en entidades, JSON y parámetros HTTP.
4. Utilizar `enum` persistidos mediante `EnumType.STRING`.
5. Aplicar Bean Validation sobre Strings, números, fechas y enums.
6. Diferenciar validaciones declarativas de reglas que necesitan comparar varios datos.
7. Diseñar Query Methods de Spring Data JPA a partir del nombre del método.
8. Ordenar resultados directamente desde el Repository.
9. Filtrar información por rangos de fechas.
10. Trabajar con `Optional` cuando una consulta puede no devolver resultados.
11. Normalizar datos antes de persistirlos.
12. Tratar correctamente errores de conversión de fechas y enums recibidos por HTTP.
13. Conservar correctamente `id`, `createdAt` y `updatedAt` durante una actualización.
14. Mantener la auditoría automática con Spring Data JPA.

---

## 📐 3. Requisitos Funcionales

### Entidad principal: `ServiceRecord`

Cada registro representa una actuación de mantenimiento realizada sobre un vehículo.

| Campo | Tipo | Descripción | Restricciones |
|---|---|---|---|
| `id` | `Long` | Identificador interno | Autogenerado |
| `vehiclePlate` | `String` | Matrícula/identificador del vehículo | Obligatorio, 5-12 caracteres, letras/números/guion |
| `serviceType` | `ServiceType` | Tipo de mantenimiento | Obligatorio |
| `serviceDate` | `LocalDate` | Fecha en la que se realizó | Obligatoria, no futura |
| `mileage` | `Integer` | Kilometraje del vehículo | Obligatorio, 0 o superior |
| `workshopName` | `String` | Taller o centro que realizó el servicio | Obligatorio, 2-120 caracteres |
| `notes` | `String` | Observaciones | Opcional, máximo 500 caracteres |
| `nextServiceDate` | `LocalDate` | Próxima revisión recomendada | Opcional; si existe, debe ser posterior a `serviceDate` |
| `createdAt` | `LocalDateTime` | Alta del registro en el sistema | Automático |
| `updatedAt` | `LocalDateTime` | Última modificación | Automático |

### Enum `ServiceType`

Debe contener como mínimo:

- `OIL_CHANGE` — cambio de aceite.
- `BRAKE_SERVICE` — revisión/reparación de frenos.
- `TIRE_CHANGE` — cambio de neumáticos.
- `INSPECTION` — inspección/revisión periódica.
- `BATTERY` — batería.
- `REPAIR` — reparación general.
- `OTHER` — otro tipo de intervención.

### Funcionalidades obligatorias

La API debe permitir:

1. Crear un registro de mantenimiento.
2. Listar todos los registros ordenados por fecha de servicio descendente.
3. Obtener un registro por ID.
4. Actualizar un registro existente.
5. Eliminar un registro.
6. Consultar todos los mantenimientos de una matrícula.
7. Filtrar por tipo de mantenimiento.
8. Filtrar por un rango de fechas inclusive.
9. Consultar mantenimientos cuya próxima revisión se encuentre entre hoy y una fecha indicada.
10. Obtener el mantenimiento más reciente de un vehículo.

---

## 🛠️ 4. Requisitos Técnicos

### Stack

- Java 21.
- Spring Boot 4.1.x.
- Maven.
- Spring MVC.
- Spring Data JPA.
- Bean Validation.
- H2 para desarrollo.
- Postman, Thunder Client o equivalente.

### Dependencias recomendadas

- `spring-boot-starter-webmvc`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `h2`
- `spring-boot-h2console`

### Estructura esperada

```text
com.eduardo.vehicleservice
├── controller
│   └── ServiceRecordController
├── exception
│   ├── ApiError
│   ├── GlobalExceptionHandler
│   ├── InvalidDateRangeException
│   ├── InvalidNextServiceDateException
│   └── ServiceRecordNotFoundException
├── model
│   ├── ServiceRecord
│   └── ServiceType
├── repository
│   └── ServiceRecordRepository
├── service
│   ├── ServiceRecordService
│   └── ServiceRecordServiceImpl
└── VehicleServiceHistoryApplication
```

### Responsabilidades por capa

#### Controller

Debe encargarse exclusivamente de HTTP:

- recibir JSON, path variables y query params;
- lanzar la validación con `@Valid`;
- delegar en el Service;
- devolver códigos y cabeceras HTTP adecuados.

No debe:

- consultar directamente el Repository;
- comparar fechas de negocio;
- filtrar colecciones manualmente si la consulta pertenece a JPA;
- decidir cómo se normaliza una matrícula.

#### Service

Debe contener la lógica del caso de uso:

- verificar existencia;
- normalizar la matrícula;
- validar `nextServiceDate` respecto a `serviceDate`;
- validar rangos temporales;
- calcular el intervalo de próximas revisiones;
- decidir qué campos se modifican durante un `PUT`.

#### Repository

Debe encargarse del acceso a datos y de las consultas derivadas.

En este proyecto **no debes cargar todos los registros y filtrarlos con un `stream()` en el Service** cuando Spring Data JPA puede hacer la consulta directamente.

### Restricciones pedagógicas del proyecto

Todavía no son obligatorios:

- DTOs de dominio.
- MapStruct.
- relaciones JPA.
- JPQL manual.
- Specifications.
- paginación.
- tests automatizados.

Los introduciremos progresivamente en los módulos siguientes.

---

## 🔌 5. Endpoints a Implementar

### CRUD básico

| Método | URI | Operación | Respuesta esperada |
|---|---|---|---|
| `POST` | `/api/service-records` | Registrar mantenimiento | `201 Created` / `400` |
| `GET` | `/api/service-records` | Listar todos | `200 OK` |
| `GET` | `/api/service-records/{id}` | Buscar por ID | `200 OK` / `404` |
| `PUT` | `/api/service-records/{id}` | Actualizar registro | `200 OK` / `400` / `404` |
| `DELETE` | `/api/service-records/{id}` | Eliminar registro | `204 No Content` / `404` |

### Consultas y filtros

| Método | URI | Operación | Respuesta |
|---|---|---|---|
| `GET` | `/api/service-records/vehicle/{vehiclePlate}` | Historial de un vehículo | `200 OK` |
| `GET` | `/api/service-records/type/{serviceType}` | Filtrar por tipo | `200 OK` / `400` |
| `GET` | `/api/service-records/between?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` | Filtrar por período | `200 OK` / `400` |
| `GET` | `/api/service-records/due?untilDate=YYYY-MM-DD` | Próximas revisiones | `200 OK` / `400` |
| `GET` | `/api/service-records/vehicle/{vehiclePlate}/latest` | Último mantenimiento | `200 OK` / `404` |

### Ejemplo: crear mantenimiento

```http
POST /api/service-records
Content-Type: application/json
```

```json
{
  "vehiclePlate": "1234ABC",
  "serviceType": "OIL_CHANGE",
  "serviceDate": "2026-09-01",
  "mileage": 84500,
  "workshopName": "Motor Sur",
  "notes": "Aceite y filtro sustituidos",
  "nextServiceDate": "2027-03-01"
}
```

Respuesta esperada:

```http
HTTP/1.1 201 Created
Location: /api/service-records/1
```

### Ejemplo: buscar por período

```http
GET /api/service-records/between?startDate=2026-01-01&endDate=2026-09-30
```

El intervalo debe incluir ambos extremos.

### Ejemplo: próximas revisiones

Si hoy fuese `2026-09-08`:

```http
GET /api/service-records/due?untilDate=2026-10-08
```

Debe devolver registros cuyo `nextServiceDate` esté entre:

```text
2026-09-08 y 2026-10-08
```

ordenados por `nextServiceDate` ascendente.

No debe devolver mantenimientos cuya próxima revisión ya haya pasado.

### Ejemplo: último mantenimiento

```http
GET /api/service-records/vehicle/1234ABC/latest
```

Debe devolver un único registro: el más reciente según `serviceDate`.

Si dos registros tienen la misma fecha, puedes utilizar el kilometraje descendente como segundo criterio de ordenación.

---

## ✅ 6. Validaciones Requeridas

### Bean Validation

#### `vehiclePlate`

- obligatorio;
- no vacío;
- entre 5 y 12 caracteres;
- únicamente letras, números y guion;
- debe aceptarse en minúsculas o mayúsculas;
- antes de guardar debe normalizarse a mayúsculas.

Ejemplo:

```text
1234abc → 1234ABC
```

#### `serviceType`

- obligatorio;
- debe corresponder con un valor válido de `ServiceType`.

#### `serviceDate`

- obligatoria;
- no puede estar en el futuro.

#### `mileage`

- obligatorio;
- debe ser `>= 0`.

#### `workshopName`

- obligatorio;
- no vacío;
- longitud entre 2 y 120 caracteres.

#### `notes`

- opcional;
- máximo 500 caracteres.

### Validaciones de negocio

Estas reglas deben vivir en el Service.

#### Regla 1 — Próxima revisión

Si `nextServiceDate` tiene valor:

```text
nextServiceDate > serviceDate
```

No es válido:

```text
serviceDate     = 2026-09-01
nextServiceDate = 2026-09-01   ❌
```

ni:

```text
serviceDate     = 2026-09-01
nextServiceDate = 2026-08-01   ❌
```

#### Regla 2 — Rango de búsqueda

En `/between` debe cumplirse:

```text
startDate <= endDate
```

Si no se cumple, responde `400 Bad Request`.

#### Regla 3 — Próximas revisiones

En `/due`, `untilDate` no puede ser anterior a la fecha actual.

El Service debe construir el intervalo:

```text
LocalDate.now() → untilDate
```

#### Regla 4 — Actualización

Al hacer `PUT`:

- el recurso debe existir;
- el `id` original debe conservarse;
- `createdAt` debe conservarse;
- `updatedAt` debe ser gestionado automáticamente;
- las mismas validaciones de fechas deben volver a comprobarse.

---

## 🧠 7. Conceptos Clave a Aplicar

### `LocalDate`

Utiliza la API moderna de fechas de Java:

```java
LocalDate
```

No utilices:

```java
java.util.Date
```

para este caso de uso.

### `@PastOrPresent`

La fecha en la que se realizó un mantenimiento no puede pertenecer al futuro.

La comparación entre `serviceDate` y `nextServiceDate`, sin embargo, **no puede resolverse simplemente anotando cada campo por separado**, porque la validez de uno depende del otro.

Esa regla debe estar en el Service en este nivel del bootcamp.

### Enums persistidos como String

```java
@Enumerated(EnumType.STRING)
```

Evita `ORDINAL`, porque el significado persistido dependería de la posición del valor dentro del enum.

### Consultas derivadas

Debes resolver las consultas del proyecto utilizando **Query Methods**.

No se proporciona el nombre exacto de todos los métodos porque parte del ejercicio consiste en construirlos.

Debes ser capaz de expresar mediante Spring Data conceptos como:

- buscar por matrícula ignorando mayúsculas/minúsculas;
- ordenar por fecha descendente;
- buscar por enum;
- buscar entre dos fechas;
- ordenar una próxima revisión ascendentemente;
- recuperar solo el primer elemento de una consulta ordenada.

### `Optional`

El endpoint de “último mantenimiento” puede no encontrar ningún registro para la matrícula solicitada.

Ese escenario no debe convertirse en `null` ni en un `500`.

### Normalización

Datos equivalentes no deberían terminar persistidos con formatos distintos:

```text
1234abc
1234ABC
1234AbC
```

Para este sistema deben representar:

```text
1234ABC
```

La normalización pertenece al Service.

---

## 🚀 8. Criterios de Aceptación

El proyecto se considera terminado cuando:

- [ ] El proyecto inicia sin errores.
- [ ] La entidad se persiste mediante JPA/Hibernate.
- [ ] `POST` devuelve `201 Created` con `Location`.
- [ ] `GET /api/service-records` devuelve registros ordenados por `serviceDate` descendente.
- [ ] Buscar un ID inexistente devuelve `404`.
- [ ] `PUT` conserva el ID y la fecha de creación.
- [ ] `DELETE` devuelve `204` cuando elimina correctamente.
- [ ] Una fecha de mantenimiento futura devuelve `400`.
- [ ] Un kilometraje negativo devuelve `400`.
- [ ] Una próxima revisión igual o anterior al servicio devuelve `400`.
- [ ] Una matrícula se persiste normalizada en mayúsculas.
- [ ] El filtro por matrícula funciona ignorando mayúsculas y minúsculas.
- [ ] El filtro por `ServiceType` funciona.
- [ ] El filtro por período incluye los extremos.
- [ ] Un rango invertido devuelve `400`.
- [ ] `/due` utiliza la fecha actual como inicio del período.
- [ ] `/latest` devuelve solamente el registro más reciente.
- [ ] Un enum inválido devuelve `400` y no `500`.
- [ ] Una fecha HTTP con formato inválido devuelve `400`.
- [ ] Las excepciones se gestionan con `@RestControllerAdvice`.
- [ ] El Controller no accede al Repository.
- [ ] El Service contiene las reglas de negocio.
- [ ] No se usa `CommandLineRunner` para cargar pruebas temporales.

---

## 🌟 9. Funcionalidades Extra Opcionales

Cuando termines todo lo obligatorio puedes añadir **una o varias** de estas mejoras:

### Extra 1 — Filtro por kilometraje

```http
GET /api/service-records/mileage?min=50000&max=100000
```

Valida que:

```text
min <= max
```

### Extra 2 — Historial por vehículo y tipo

```http
GET /api/service-records/vehicle/1234ABC/type/OIL_CHANGE
```

Aquí tendrás que construir una consulta derivada con varios criterios.

### Extra 3 — Revisiones vencidas

```http
GET /api/service-records/overdue
```

Devuelve registros cuyo `nextServiceDate` sea anterior a hoy.

### Extra 4 — Contador por vehículo

```http
GET /api/service-records/vehicle/1234ABC/count
```

Intenta resolverlo desde el Repository en lugar de recuperar toda la lista y hacer `.size()`.

### Extra 5 — Orden configurable

Permite solicitar ascendente o descendente sin introducir todavía paginación.

---

## 📝 10. Notas Importantes

### No filtres todo en memoria

Evita esta estrategia para los requisitos principales:

```text
repository.findAll()
→ stream()
→ filter(...)
```

El objetivo es que la base de datos reciba la consulta adecuada.

### No pongas reglas temporales en el Controller

Esto no pertenece al Controller:

```text
si nextServiceDate <= serviceDate...
```

El Controller no debería conocer esa regla.

### Cuidado con el `PUT`

No hagas directamente algo equivalente a:

```text
incomingEntity.id = pathId
repository.save(incomingEntity)
```

sin pensar qué datos gestionados por el servidor puedes destruir o sobrescribir.

Recupera el registro persistido, modifica los campos editables y conserva los datos gestionados por la aplicación.

### Fechas HTTP

Los parámetros deben utilizar ISO-8601:

```text
YYYY-MM-DD
```

Ejemplo válido:

```text
2026-09-08
```

Ejemplos no válidos para esta API:

```text
08/09/2026
08-09-2026
```

### Colecciones vacías

Un filtro que no encuentra registros puede devolver:

```json
[]
```

con `200 OK`.

No es necesario devolver `404` por una colección vacía.

Sin embargo, el endpoint `/latest` busca **un recurso concreto derivado de una matrícula**, por lo que en este proyecto se espera `404` cuando no existe ningún mantenimiento registrado para ella.

---

## 🧪 11. Cómo Probar tu API

### Orden recomendado

#### 1. Crear varios registros

Crea al menos:

- dos vehículos diferentes;
- varios tipos de mantenimiento;
- distintas fechas;
- algunos registros con `nextServiceDate` y otros sin ella.

#### 2. Probar CRUD

Comprueba:

```text
POST
GET all
GET by id
PUT
DELETE
```

#### 3. Probar validaciones

Envía intencionadamente:

- matrícula vacía;
- `serviceType` inexistente;
- fecha futura;
- kilometraje negativo;
- taller vacío;
- próxima revisión anterior al mantenimiento.

#### 4. Probar filtros

Comprueba cada Query Method con datos que permitan distinguir fácilmente el resultado esperado.

#### 5. Probar errores temporales

Por ejemplo:

```http
GET /api/service-records/between?startDate=2026-12-31&endDate=2026-01-01
```

Debe devolver `400`.

También:

```http
GET /api/service-records/between?startDate=hola&endDate=2026-12-31
```

Debe devolver `400`, no `500`.

### Consola H2

Con la configuración proporcionada:

```text
http://localhost:8080/h2-console
```

JDBC URL:

```text
jdbc:h2:file:./data/vehicleservicedb
```

Usuario:

```text
sa
```

Contraseña vacía.

---

## 📚 12. Recursos Útiles

- Spring Data JPA — Query Methods: https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html
- Spring Data JPA — Repositories: https://docs.spring.io/spring-data/jpa/reference/repositories.html
- Bean Validation en Spring Framework: https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html
- `LocalDate`: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/LocalDate.html
- `DateTimeFormat`: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/format/annotation/DateTimeFormat.html

---

## 🗺️ 13. Plan de Desarrollo Sugerido

Sigue este orden. No intentes implementar todos los endpoints a la vez.

### Fase 1 — Modelo

1. Crear `ServiceType`.
2. Crear `ServiceRecord`.
3. Añadir el mapeo JPA.
4. Añadir Bean Validation.
5. Configurar auditoría.
6. Arrancar la aplicación y comprobar que Hibernate crea la tabla.

### Fase 2 — CRUD

7. Crear `ServiceRecordRepository`.
8. Crear `ServiceRecordService`.
9. Implementar `create`.
10. Implementar `findAll`.
11. Implementar `findById`.
12. Implementar `update`.
13. Implementar `delete`.
14. Crear el Controller.
15. Probar CRUD completo.

### Fase 3 — Excepciones

16. Crear `ServiceRecordNotFoundException`.
17. Crear `ApiError`.
18. Crear `GlobalExceptionHandler`.
19. Gestionar errores de Bean Validation.
20. Gestionar JSON/enum inválido.
21. Gestionar parámetros de fecha inválidos.

### Fase 4 — Reglas temporales

22. Validar `nextServiceDate`.
23. Crear excepción específica para esa regla.
24. Validar rangos de fechas.
25. Crear excepción específica para rangos inválidos.

### Fase 5 — Query Methods

26. Implementar historial por matrícula.
27. Implementar filtro por tipo.
28. Implementar filtro entre fechas.
29. Implementar próximas revisiones.
30. Implementar último mantenimiento de un vehículo.
31. Comprobar el orden de cada resultado.

### Fase 6 — Revisión

32. Revisar que el Controller no contenga negocio.
33. Revisar que el Service no filtre manualmente lo que puede consultar JPA.
34. Revisar nombres de métodos y variables.
35. Ejecutar todos los casos de Postman.
36. Añadir algún extra solo cuando lo obligatorio funcione.

---

## 💡 14. Consejo Final

En este proyecto intenta no pensar primero en “qué código tengo que escribir”.

Antes de crear cada Query Method, formula la petición en lenguaje natural:

```text
Quiero los mantenimientos...
del vehículo X...
ordenados por fecha descendente.
```

Después traduce esa intención al Repository.

Haz lo mismo con las reglas temporales:

```text
¿Qué dos fechas estoy comparando?
¿Cuándo sería válida la relación entre ellas?
¿Qué capa debe decidirlo?
```

Si consigues razonar así, Spring Data JPA deja de parecer una colección de nombres de métodos que hay que memorizar y empieza a convertirse en una herramienta que puedes diseñar conscientemente.

**No abras `/solution` para copiar. Úsala únicamente al terminar o cuando quieras comparar una decisión concreta de diseño.**
