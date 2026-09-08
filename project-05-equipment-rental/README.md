# Proyecto 5: Gestión de Alquiler de Equipamiento Profesional 🧰📅

## 📋 Descripción del Proyecto

Desarrolla una API REST con Spring Boot para gestionar el alquiler temporal de equipamiento profesional: cámaras, herramientas, equipos audiovisuales, ordenadores, maquinaria ligera y otros recursos.

Este es el **proyecto integrador del Módulo 1**.

El sistema deberá permitir registrar reservas, evitar que un mismo equipo se alquile a dos clientes durante períodos incompatibles, activar alquileres, registrar devoluciones, calcular costes y recargos por retraso, cancelar reservas y consultar alquileres vencidos.

A diferencia de los proyectos anteriores, aquí tendrás que combinar varios conocimientos a la vez:

- CRUD;
- arquitectura por capas;
- validación;
- estados;
- fechas;
- `BigDecimal`;
- Query Methods;
- reglas de negocio;
- campos controlados por el servidor;
- manejo global de errores;
- auditoría;
- agregaciones.

Seguiremos utilizando **una única entidad principal** y **sin relaciones JPA**, porque las relaciones se trabajarán de forma específica en el Módulo 3.

---

# 🎯 Objetivos de Aprendizaje

Al finalizar deberías ser capaz de:

1. Diseñar un CRUD profesional completo.
2. Mantener una separación clara `Controller → Service → Repository`.
3. Modelar un ciclo de vida mediante un enum.
4. Impedir transiciones de estado inválidas.
5. Validar reglas entre varias fechas.
6. Detectar solapamientos de intervalos.
7. Diseñar Query Methods derivados más exigentes.
8. Utilizar `BigDecimal` en cálculos monetarios.
9. Aplicar `RoundingMode`.
10. Diferenciar correctamente `400`, `404` y `409`.
11. Proteger campos internos frente al consumidor.
12. Centralizar lógica compartida.
13. Consultar alquileres vencidos.
14. Crear pequeños reportes mediante Streams.
15. Aplicar auditoría automática.

---

# 🏢 Contexto del Problema

Una empresa alquila equipamiento profesional por días.

Cada unidad física tiene un código único, por ejemplo:

```text
CAM-001
LAPTOP-014
DRILL-032
PROJECTOR-008
```

Un mismo equipo puede tener muchos alquileres a lo largo del tiempo, pero **no puede tener dos alquileres activos o reservados cuyos períodos se solapen**.

Ejemplo válido:

```text
CAM-001

Alquiler A:
01/10/2026 ───── 05/10/2026

Alquiler B:
05/10/2026 ───── 08/10/2026
```

Se permite porque el primer alquiler termina el día en que comienza el segundo.

Ejemplo inválido:

```text
Alquiler A:
01/10/2026 ───────── 06/10/2026

Alquiler B:
04/10/2026 ───────── 08/10/2026
```

Existe solapamiento.

---

# 📐 Entidad Principal: RentalContract

| Campo | Tipo | Descripción | Control |
|---|---|---|---|
| id | Long | Identificador interno | Servidor |
| contractCode | String | Código público del contrato | Servidor |
| equipmentCode | String | Código físico del equipo | Cliente |
| equipmentName | String | Nombre descriptivo | Cliente |
| equipmentType | EquipmentType | Tipo de equipo | Cliente |
| customerName | String | Cliente | Cliente |
| startDate | LocalDate | Inicio del alquiler | Cliente |
| expectedReturnDate | LocalDate | Devolución prevista | Cliente |
| actualReturnDate | LocalDate | Devolución real | Servidor |
| dailyRate | BigDecimal | Precio diario | Cliente |
| status | RentalStatus | Estado | Servidor |
| estimatedCost | BigDecimal | Coste previsto | Servidor |
| lateDays | Integer | Días de retraso | Servidor |
| lateFee | BigDecimal | Recargo por retraso | Servidor |
| finalCost | BigDecimal | Coste final | Servidor |
| createdAt | LocalDateTime | Creación | Servidor |
| updatedAt | LocalDateTime | Modificación | Servidor |

---

# 🏷️ EquipmentType

Implementa:

```text
CAMERA
COMPUTER
AUDIO
VIDEO
POWER_TOOL
MACHINERY
OTHER
```

Persistir mediante:

```java
EnumType.STRING
```

---

# 🔄 RentalStatus

Implementa:

```text
RESERVED
ACTIVE
RETURNED
CANCELLED
```

Flujo permitido:

```text
                     ┌────────────→ CANCELLED
                     │
RESERVED ─────────→ ACTIVE ─────────→ RETURNED
```

`RETURNED` y `CANCELLED` son estados finales.

---

# 🧠 Reglas de Negocio

## 1. Código de contrato

`contractCode` lo genera el servidor.

Ejemplo:

```text
RNT-7C23B9A4-...
```

El cliente no puede elegirlo ni modificarlo.

---

## 2. Estado inicial

Todo alquiler nuevo comienza como:

```text
RESERVED
```

Aunque el JSON incluya:

```json
{
  "status": "RETURNED"
}
```

debe ignorarse.

---

## 3. Período

Debe cumplirse:

```text
startDate >= fecha actual
expectedReturnDate > startDate
```

Por tanto, un alquiler de un día puede representarse así:

```text
startDate          = 2026-10-01
expectedReturnDate = 2026-10-02
```

Consideraremos el intervalo como:

```text
[startDate, expectedReturnDate)
```

Es decir, el día de devolución prevista no bloquea un nuevo alquiler que comience ese mismo día.

---

## 4. Solapamientos

Un equipo no puede tener otro contrato `RESERVED` o `ACTIVE` que se solape con el período solicitado.

Dos intervalos se solapan cuando:

```text
existing.startDate < new.expectedReturnDate
AND
existing.expectedReturnDate > new.startDate
```

Los contratos:

```text
RETURNED
CANCELLED
```

no bloquean nuevas reservas.

---

## 5. Normalización

Antes de guardar:

```text
" cam-001 " → "CAM-001"
```

para `equipmentCode`.

También debes eliminar espacios exteriores de:

- equipmentName;
- customerName.

---

## 6. Precio diario

Debe ser:

```text
dailyRate > 0
```

y trabajar con `BigDecimal`.

---

## 7. Coste estimado

Al crear o modificar una reserva:

```text
estimatedDays =
DAYS.between(startDate, expectedReturnDate)

estimatedCost =
estimatedDays × dailyRate
```

Ejemplo:

```text
01/10 → 04/10
3 días

3 × 40 € = 120 €
```

Resultado monetario:

```text
scale = 2
RoundingMode.HALF_UP
```

---

# ▶️ Activar un alquiler

Endpoint:

```http
PATCH /api/rentals/{id}/activate
```

Solo puede ejecutarse si:

```text
status == RESERVED
```

y la fecha actual pertenece al período reservado:

```text
startDate <= hoy < expectedReturnDate
```

Si intentas activarlo antes de `startDate` o cuando el período ya ha vencido:

```http
409 Conflict
```

Al activarlo:

```text
RESERVED → ACTIVE
```

---

# ❌ Cancelar una reserva

Endpoint:

```http
PATCH /api/rentals/{id}/cancel
```

Solo puede cancelarse:

```text
status == RESERVED
```

y únicamente:

```text
hoy < startDate
```

No se puede cancelar un alquiler que ya debería haber comenzado.

Resultado:

```text
RESERVED → CANCELLED
```

---

# ↩️ Registrar devolución

Endpoint:

```http
PATCH /api/rentals/{id}/return?returnDate=2026-10-08
```

Solo puede devolverse un contrato:

```text
ACTIVE
```

`returnDate` debe:

```text
returnDate >= startDate
returnDate <= hoy
```

---

# 💰 Cálculo del coste final

## Días reales

```text
actualDays = DAYS.between(startDate, actualReturnDate)
```

Si el resultado fuese `0`, se factura como mínimo:

```text
1 día
```

---

## Retraso

```text
lateDays =
max(0, DAYS.between(expectedReturnDate, actualReturnDate))
```

---

## Recargo

Cada día de retraso tiene un recargo del:

```text
50 % de la tarifa diaria
```

Por tanto:

```text
lateFee =
lateDays × dailyRate × 0.50
```

---

## Coste base real

```text
baseCost =
actualDays × dailyRate
```

---

## Coste final

```text
finalCost =
baseCost + lateFee
```

Todo con `BigDecimal`.

---

# ✏️ Actualización mediante PUT

```http
PUT /api/rentals/{id}
```

Solo puede modificarse un contrato:

```text
RESERVED
```

Se pueden modificar:

- equipmentCode;
- equipmentName;
- equipmentType;
- customerName;
- startDate;
- expectedReturnDate;
- dailyRate.

No pueden modificarse manualmente:

- id;
- contractCode;
- status;
- actualReturnDate;
- estimatedCost;
- lateDays;
- lateFee;
- finalCost;
- createdAt;
- updatedAt.

Al cambiar fechas o equipo debes volver a comprobar:

```text
solapamientos
```

excluyendo el contrato que estás actualizando.

También debes recalcular:

```text
estimatedCost
```

---

# 🗑️ Eliminación

```http
DELETE /api/rentals/{id}
```

Para no perder trazabilidad, solo se puede eliminar físicamente un contrato:

```text
CANCELLED
```

Intentar borrar un contrato en cualquier otro estado:

```http
409 Conflict
```

---

# 🔎 Consultas y Filtros

## Por estado

```http
GET /api/rentals/status/ACTIVE
```

---

## Por equipo

```http
GET /api/rentals/equipment/CAM-001
```

Ordenar por `startDate` descendente.

---

## Por tipo

```http
GET /api/rentals/type/CAMERA
```

---

## Por cliente

```http
GET /api/rentals/customer/edu
```

Coincidencia parcial ignorando mayúsculas/minúsculas.

---

## Alquileres vencidos

```http
GET /api/rentals/overdue
```

Un contrato está vencido si:

```text
status == ACTIVE
AND
expectedReturnDate < hoy
```

---

# 🔍 Comprobar disponibilidad

```http
GET /api/rentals/availability
    ?equipmentCode=CAM-001
    &startDate=2026-10-10
    &endDate=2026-10-15
```

Respuesta orientativa:

```json
{
  "equipmentCode": "CAM-001",
  "startDate": "2026-10-10",
  "endDate": "2026-10-15",
  "available": true
}
```

En este módulo puedes utilizar una estructura sencilla como `Map`.

En el Módulo 2 sustituiremos este tipo de respuestas por DTOs específicos.

---

# 📊 Resumen del negocio

Implementa:

```http
GET /api/rentals/reports/summary
```

Respuesta orientativa:

```json
{
  "totalContracts": 25,
  "reserved": 4,
  "active": 3,
  "returned": 16,
  "cancelled": 2,
  "overdue": 1,
  "estimatedOpenValue": 820.00,
  "returnedRevenue": 6420.50
}
```

Donde:

```text
estimatedOpenValue
```

suma el `estimatedCost` de contratos `RESERVED` y `ACTIVE`.

Y:

```text
returnedRevenue
```

suma el `finalCost` de contratos `RETURNED`.

---

# 🛠️ Requisitos Técnicos

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring MVC
- Spring Data JPA
- Jakarta Bean Validation
- H2
- Postman

---

# 🏗️ Arquitectura Obligatoria

```text
Cliente HTTP
     ↓
Controller
     ↓
Service
     ↓
Repository
     ↓
JPA / Hibernate
     ↓
H2
```

## Controller

Debe:

- recibir datos;
- aplicar `@Valid`;
- devolver HTTP;
- delegar.

No debe:

- calcular precios;
- validar transiciones;
- consultar Repository directamente.

## Service

Debe contener:

- normalización;
- reglas de fecha;
- comprobación de solapamientos;
- transiciones;
- cálculos;
- búsquedas;
- reportes;
- excepciones.

## Repository

Solo persistencia y consultas.

---

# 📁 Estructura Sugerida

```text
com.eduardo.equipmentrental
├── EquipmentRentalApplication.java
├── controller/
│   └── RentalContractController.java
├── model/
│   ├── RentalContract.java
│   ├── RentalStatus.java
│   └── EquipmentType.java
├── repository/
│   └── RentalContractRepository.java
├── service/
│   ├── RentalContractService.java
│   └── RentalContractServiceImpl.java
└── exception/
    ├── ApiError.java
    ├── RentalContractNotFoundException.java
    ├── InvalidRentalPeriodException.java
    ├── EquipmentUnavailableException.java
    ├── InvalidRentalOperationException.java
    └── GlobalExceptionHandler.java
```

---

# 🔌 Endpoints a Implementar

| Método | URI | Código |
|---|---|---|
| POST | `/api/rentals` | 201 / 400 / 409 |
| GET | `/api/rentals` | 200 |
| GET | `/api/rentals/{id}` | 200 / 404 |
| PUT | `/api/rentals/{id}` | 200 / 400 / 404 / 409 |
| DELETE | `/api/rentals/{id}` | 204 / 404 / 409 |
| PATCH | `/api/rentals/{id}/activate` | 200 / 404 / 409 |
| PATCH | `/api/rentals/{id}/cancel` | 200 / 404 / 409 |
| PATCH | `/api/rentals/{id}/return?returnDate=` | 200 / 400 / 404 / 409 |
| GET | `/api/rentals/status/{status}` | 200 / 400 |
| GET | `/api/rentals/equipment/{code}` | 200 |
| GET | `/api/rentals/type/{type}` | 200 / 400 |
| GET | `/api/rentals/customer/{name}` | 200 |
| GET | `/api/rentals/overdue` | 200 |
| GET | `/api/rentals/availability?...` | 200 / 400 |
| GET | `/api/rentals/reports/summary` | 200 |

---

# 📤 Ejemplo de Creación

```http
POST /api/rentals
Content-Type: application/json
```

```json
{
  "equipmentCode": "cam-001",
  "equipmentName": "Sony FX3",
  "equipmentType": "CAMERA",
  "customerName": "Producciones Sur",
  "startDate": "2026-10-01",
  "expectedReturnDate": "2026-10-04",
  "dailyRate": 40.00
}
```

El servidor debe generar:

```text
contractCode
status = RESERVED
estimatedCost = 120.00
```

---

# ✅ Bean Validation

## equipmentCode

- `@NotBlank`
- 3-30 caracteres
- caracteres alfanuméricos y `-`

## equipmentName

- `@NotBlank`
- 2-120 caracteres

## equipmentType

- `@NotNull`

## customerName

- `@NotBlank`
- 2-120 caracteres

## startDate

- `@NotNull`
- `@FutureOrPresent`

## expectedReturnDate

- `@NotNull`

La relación:

```text
expectedReturnDate > startDate
```

debe comprobarse en Service.

## dailyRate

- `@NotNull`
- mínimo `0.01`
- máximo 2 decimales

---

# 🚨 Manejo de Errores

Utiliza:

```java
@RestControllerAdvice
```

## 400 Bad Request

- Bean Validation;
- JSON inválido;
- enum incorrecto;
- fecha mal formada;
- `endDate <= startDate`;
- fecha de devolución inválida.

## 404 Not Found

Contrato inexistente.

## 409 Conflict

Para reglas de negocio como:

- equipo no disponible;
- transición incorrecta;
- activar antes de tiempo;
- activar reserva expirada;
- cancelar fuera de plazo;
- editar contrato no reservado;
- borrar contrato no cancelado;
- devolver contrato que no está activo.

---

# 🎓 Conceptos Clave a Aplicar

## 1. Optional

Centraliza:

```text
findById
→ orElseThrow
```

Evita repetir esa lógica en cada método.

---

## 2. Estados

No permitas:

```java
contract.setStatus(...)
```

desde el Controller como forma de ejecutar procesos de negocio.

Utiliza operaciones explícitas:

```text
activate
cancel
returnRental
```

---

## 3. Intersección de fechas

No basta con buscar:

```text
startDate BETWEEN ...
```

Debes razonar cuándo dos intervalos realmente se cruzan.

---

## 4. BigDecimal

Utiliza:

```java
multiply
add
setScale
compareTo
```

Evita `double`.

---

## 5. Campos internos

Este proyecto vuelve a mostrar una limitación importante de recibir entidades directamente como entrada.

El consumidor puede intentar enviar:

```json
{
  "status": "RETURNED",
  "finalCost": 0
}
```

Tu Service debe ignorarlo.

Este problema será una de las razones principales para introducir DTOs en el **Módulo 2**.

---

# 🚀 Criterios de Aceptación

El proyecto estará completado cuando:

- [ ] CRUD funcionando.
- [ ] `POST` devuelve `201 Created`.
- [ ] Se devuelve cabecera `Location`.
- [ ] El servidor genera `contractCode`.
- [ ] El estado inicial siempre es `RESERVED`.
- [ ] El código del equipo se normaliza.
- [ ] Las fechas se validan.
- [ ] No existen reservas incompatibles para un mismo equipo.
- [ ] Reservas consecutivas sin solapamiento son válidas.
- [ ] El coste estimado se calcula correctamente.
- [ ] PUT solo funciona en `RESERVED`.
- [ ] PUT vuelve a comprobar disponibilidad.
- [ ] PUT conserva campos internos.
- [ ] Activación válida cambia a `ACTIVE`.
- [ ] Activaciones inválidas devuelven `409`.
- [ ] Cancelación válida cambia a `CANCELLED`.
- [ ] Cancelaciones inválidas devuelven `409`.
- [ ] Una devolución válida cambia a `RETURNED`.
- [ ] Se almacena `actualReturnDate`.
- [ ] Se calculan días reales.
- [ ] Se calculan días de retraso.
- [ ] Se calcula el recargo.
- [ ] Se calcula `finalCost`.
- [ ] `DELETE` solo elimina contratos cancelados.
- [ ] Filtros funcionan.
- [ ] Se pueden consultar alquileres vencidos.
- [ ] Disponibilidad funciona.
- [ ] Reporte resumen funciona.
- [ ] Los errores son consistentes.
- [ ] Auditoría automática funciona.
- [ ] Controller no contiene lógica de negocio.
- [ ] Repository no contiene lógica de negocio.

---

# 🌟 Funcionalidades Extra Opcionales

## Extra 1 — Máximo de retraso

Si un alquiler supera X días de retraso, añadir un recargo adicional fijo.

---

## Extra 2 — Tarifa por tipo

Configurar un mínimo de precio diario distinto para cada `EquipmentType`.

---

## Extra 3 — Búsqueda combinada

Crear:

```http
GET /api/rentals/search
```

con varios filtros opcionales.

---

## Extra 4 — CSV de contratos devueltos

Exportar contratos `RETURNED` a CSV usando Java I/O.

---

## Extra 5 — Estadísticas

Calcular:

- tipo de equipo más alquilado;
- cliente con más contratos;
- porcentaje de devoluciones con retraso;
- ingreso medio por contrato.

---

# 📝 Notas Importantes

## No uses `CommandLineRunner` para probar

Prueba la aplicación mediante:

- Postman;
- cURL;
- cliente HTTP.

El objetivo es trabajar mediante la arquitectura real de la aplicación.

---

## No hagas un `save(incoming)` directo en PUT

Si sustituyes indiscriminadamente la entidad puedes perder:

- ID;
- contractCode;
- estado;
- auditoría;
- costes calculados.

Primero recupera la entidad existente y modifica únicamente campos permitidos.

---

## No calcules el coste final en Controller

La fórmula pertenece al Service.

---

## No consultes Repository desde Controller

Debe mantenerse:

```text
Controller → Service → Repository
```

---

## No confundas error de entrada con conflicto de negocio

Ejemplo:

```text
expectedReturnDate <= startDate
```

es un:

```http
400
```

pero:

```text
el equipo ya está reservado
```

es:

```http
409
```

---

# 🧪 Cómo Probar tu API

## Flujo recomendado

1. Crear una reserva.
2. Comprobar `contractCode`.
3. Comprobar estado `RESERVED`.
4. Verificar `estimatedCost`.
5. Intentar crear otra reserva solapada.
6. Crear una reserva justo después de la primera.
7. Consultar disponibilidad.
8. Actualizar una reserva.
9. Intentar modificar campos internos.
10. Activar antes de fecha y comprobar `409`.
11. Activar una reserva válida.
12. Intentar editarla y comprobar `409`.
13. Consultar activos.
14. Consultar vencidos.
15. Devolver sin retraso.
16. Verificar coste final.
17. Crear otro caso con retraso.
18. Verificar `lateDays`.
19. Verificar `lateFee`.
20. Cancelar una reserva futura.
21. Intentar cancelar una activa.
22. Intentar borrar una devuelta.
23. Borrar una cancelada.
24. Ejecutar el reporte resumen.
25. Probar enums incorrectos.
26. Probar fechas incorrectas.
27. Probar ID inexistente.

Se incluye una colección en:

```text
/postman
```

---

# 📚 Recursos Útiles

- Spring Data JPA Query Methods
- Jakarta Bean Validation
- Java `LocalDate`
- Java `ChronoUnit`
- Java `BigDecimal`
- Java `RoundingMode`
- Spring `@RestControllerAdvice`
- Spring Data JPA Auditing
- Java `UUID`

---

# 🗺️ Plan de Desarrollo Sugerido

## Fase 1 — Modelo

1. Crear `EquipmentType`.
2. Crear `RentalStatus`.
3. Crear `RentalContract`.
4. Añadir Bean Validation.
5. Añadir auditoría.

## Fase 2 — Repository

6. Crear Repository.
7. CRUD básico.
8. Consulta por estado.
9. Consulta por equipo.
10. Consulta por tipo.
11. Consulta por cliente.
12. Consulta de vencidos.
13. Diseñar consulta de solapamientos.

## Fase 3 — Service Base

14. Crear interfaz.
15. Crear implementación.
16. Centralizar `findById`.
17. Normalizar entrada.
18. Generar `contractCode`.

## Fase 4 — Reserva

19. Validar período.
20. Consultar disponibilidad.
21. Calcular coste estimado.
22. Crear en estado `RESERVED`.

## Fase 5 — PUT

23. Validar estado.
24. Copiar solo campos permitidos.
25. Excluir el propio contrato del control de solapamiento.
26. Recalcular coste.

## Fase 6 — Estados

27. Implementar `activate`.
28. Implementar `cancel`.
29. Implementar `returnRental`.

## Fase 7 — Cálculo Final

30. Calcular días reales.
31. Calcular retraso.
32. Calcular recargo.
33. Calcular coste final.

## Fase 8 — REST

34. CRUD.
35. PATCH de activación.
36. PATCH de cancelación.
37. PATCH de devolución.
38. filtros.
39. disponibilidad.
40. reporte.

## Fase 9 — Excepciones

41. `404`.
42. período inválido.
43. equipo no disponible.
44. operación inválida.
45. `GlobalExceptionHandler`.

## Fase 10 — Revisión

46. Revisar códigos HTTP.
47. Revisar duplicación.
48. Revisar responsabilidades.
49. Probar límites.
50. Ejecutar colección completa de Postman.

---

# 💡 Consejo Final

Este proyecto no pretende enseñarte un concepto nuevo aislado.

Pretende comprobar si ya puedes **combinar varios fundamentos sin que la arquitectura se desordene**.

Cuando una regla empieza a depender de:

- estado;
- fecha;
- datos existentes;
- cálculos;
- persistencia;

es fácil terminar poniendo lógica en cualquier sitio.

Tu objetivo es mantener una idea muy clara:

```text
Controller = HTTP
Service = negocio
Repository = persistencia
Entity = estado persistente
```

Si consigues resolver este proyecto entendiendo por qué cada parte está donde está, habrás cerrado correctamente el **Módulo 1** y estaremos preparados para entrar en el **Módulo 2: DTOs y arquitectura lógica**.
