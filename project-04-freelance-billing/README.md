# Proyecto 4: Facturación de Servicios Freelance 💼💶

## 📋 Descripción del Proyecto

Desarrolla una API REST con Spring Boot para registrar trabajos facturables realizados por una persona autónoma o pequeña consultora.

Cada registro representa una jornada o bloque de trabajo realizado para un cliente. El sistema recibirá las horas trabajadas, tarifa por hora, descuento e impuestos y deberá **calcular automáticamente todos los importes derivados**.

El consumidor de la API no debe poder decidir el `subtotal`, el importe del descuento, los impuestos ni el total final. Esos valores pertenecen a la lógica del backend.

---

## 🎯 Objetivos de Aprendizaje

Al completar este proyecto practicarás:

1. CRUD REST completo.
2. Separación `Controller → Service → Repository`.
3. Uso correcto de `BigDecimal`.
4. Operaciones aritméticas y comparaciones con `BigDecimal`.
5. `RoundingMode`.
6. Campos calculados por el servidor.
7. `LocalDate`.
8. Enums persistidos como `STRING`.
9. Query Methods derivados.
10. Filtros por cliente, categoría, proyecto y período.
11. Agregaciones con Streams.
12. Manejo global de excepciones.
13. Auditoría automática.
14. Normalización de datos.

---

## 🧩 Contexto Funcional

Un profesional freelance presta servicios de desarrollo, consultoría, soporte, formación, diseño y otras tareas.

Cada trabajo debe registrar:

- cliente;
- proyecto;
- categoría;
- fecha;
- horas;
- tarifa por hora;
- descuento;
- porcentaje de impuestos.

El backend calculará:

```text
subtotal = horas × tarifa

descuento = subtotal × porcentajeDescuento / 100

baseImponible = subtotal - descuento

impuestos = baseImponible × porcentajeImpuesto / 100

total = baseImponible + impuestos
```

Todos los importes monetarios calculados deben quedar a **2 decimales** con `RoundingMode.HALF_UP`.

---

## 📐 Entidad Principal: BillingRecord

| Campo | Tipo | Descripción | Restricciones |
|---|---|---|---|
| id | Long | Identificador | Autogenerado |
| clientName | String | Cliente | 2-120 caracteres |
| projectCode | String | Código de proyecto | 3-30 caracteres |
| serviceCategory | ServiceCategory | Tipo de servicio | Obligatorio |
| workDate | LocalDate | Fecha del trabajo | No futura |
| hoursWorked | BigDecimal | Horas facturadas | > 0 y <= 24 |
| hourlyRate | BigDecimal | Precio por hora | > 0 |
| discountPercent | BigDecimal | Descuento | 0-50 |
| taxPercent | BigDecimal | Impuestos | 0-30 |
| subtotal | BigDecimal | Horas × tarifa | Calculado |
| discountAmount | BigDecimal | Descuento monetario | Calculado |
| taxableAmount | BigDecimal | Base imponible | Calculado |
| taxAmount | BigDecimal | Impuestos monetarios | Calculado |
| totalAmount | BigDecimal | Total final | Calculado |
| createdAt | LocalDateTime | Creación | Automático |
| updatedAt | LocalDateTime | Actualización | Automático |

### ServiceCategory

```text
DEVELOPMENT
CONSULTING
SUPPORT
TRAINING
DESIGN
OTHER
```

Persistir con `EnumType.STRING`.

---

## 🧠 Reglas de Negocio

### 1. Los importes derivados son responsabilidad del servidor

Aunque un cliente envíe:

```json
{
  "subtotal": 1,
  "totalAmount": 1
}
```

el backend debe ignorar esos valores y recalcularlos.

### 2. Ejemplo de cálculo

```text
Horas:          8
Tarifa:         50.00 €
Descuento:      10 %
Impuestos:      21 %

Subtotal:
8 × 50 = 400.00

Descuento:
400 × 10 / 100 = 40.00

Base imponible:
400 - 40 = 360.00

Impuestos:
360 × 21 / 100 = 75.60

Total:
360 + 75.60 = 435.60
```

### 3. Redondeo

Los importes monetarios deben trabajar con:

```text
scale = 2
RoundingMode.HALF_UP
```

### 4. Código de proyecto

Antes de persistir:

```text
"  web-2026  " → "WEB-2026"
```

### 5. Cliente

Eliminar espacios al inicio y final.

### 6. Fecha

`workDate` es obligatoria y no puede ser futura.

### 7. Horas

```text
0 < hoursWorked <= 24
```

### 8. Descuento

```text
0 <= discountPercent <= 50
```

### 9. Impuestos

```text
0 <= taxPercent <= 30
```

### 10. PUT

Se pueden modificar:

- clientName
- projectCode
- serviceCategory
- workDate
- hoursWorked
- hourlyRate
- discountPercent
- taxPercent

No pueden ser controlados manualmente:

- id
- subtotal
- discountAmount
- taxableAmount
- taxAmount
- totalAmount
- createdAt
- updatedAt

Después de cualquier modificación deben recalcularse todos los importes.

---

## 🛠️ Requisitos Técnicos

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring MVC
- Spring Data JPA
- Jakarta Bean Validation
- H2
- Postman

Arquitectura obligatoria:

```text
HTTP
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

### Controller

Recibe peticiones, valida entrada, construye respuestas HTTP y delega.

**No calcula dinero.**

### Service

Contiene:

- reglas;
- normalización;
- cálculos;
- búsqueda por ID;
- filtros;
- agregaciones;
- excepciones.

### Repository

Solo acceso a datos.

---

## 📁 Estructura Recomendada

```text
com.eduardo.freelancebilling
├── FreelanceBillingApplication.java
├── controller/
│   └── BillingRecordController.java
├── model/
│   ├── BillingRecord.java
│   └── ServiceCategory.java
├── repository/
│   └── BillingRecordRepository.java
├── service/
│   ├── BillingRecordService.java
│   └── BillingRecordServiceImpl.java
└── exception/
    ├── ApiError.java
    ├── BillingRecordNotFoundException.java
    ├── InvalidBillingPeriodException.java
    └── GlobalExceptionHandler.java
```

---

## 🔌 Endpoints a Implementar

| Método | URI | Código esperado |
|---|---|---|
| POST | `/api/billing-records` | 201 |
| GET | `/api/billing-records` | 200 |
| GET | `/api/billing-records/{id}` | 200 / 404 |
| PUT | `/api/billing-records/{id}` | 200 / 400 / 404 |
| DELETE | `/api/billing-records/{id}` | 204 / 404 |
| GET | `/api/billing-records/client/{clientName}` | 200 |
| GET | `/api/billing-records/category/{category}` | 200 / 400 |
| GET | `/api/billing-records/project/{projectCode}` | 200 |
| GET | `/api/billing-records/between?startDate=&endDate=` | 200 / 400 |
| GET | `/api/billing-records/reports/revenue?startDate=&endDate=` | 200 / 400 |

### Crear

```http
POST /api/billing-records
Content-Type: application/json
```

```json
{
  "clientName": "Acme Consulting",
  "projectCode": "web-2026",
  "serviceCategory": "DEVELOPMENT",
  "workDate": "2026-09-01",
  "hoursWorked": 8,
  "hourlyRate": 50.00,
  "discountPercent": 10,
  "taxPercent": 21
}
```

Debe devolver `201 Created` y cabecera `Location`.

Resultado económico esperado:

```json
{
  "subtotal": 400.00,
  "discountAmount": 40.00,
  "taxableAmount": 360.00,
  "taxAmount": 75.60,
  "totalAmount": 435.60
}
```

---

## 🔎 Consultas

### Por cliente

```http
GET /api/billing-records/client/acme
```

Coincidencia parcial e ignorando mayúsculas/minúsculas.

### Por categoría

```http
GET /api/billing-records/category/DEVELOPMENT
```

### Por proyecto

```http
GET /api/billing-records/project/web-2026
```

Ignorar mayúsculas/minúsculas.

### Entre fechas

```http
GET /api/billing-records/between?startDate=2026-09-01&endDate=2026-09-30
```

Regla:

```text
startDate <= endDate
```

Un rango invertido devuelve `400 Bad Request`.

---

## 📊 Reporte de Ingresos

```http
GET /api/billing-records/reports/revenue?startDate=2026-09-01&endDate=2026-09-30
```

Respuesta orientativa:

```json
{
  "startDate": "2026-09-01",
  "endDate": "2026-09-30",
  "recordCount": 12,
  "totalHours": 71.50,
  "totalSubtotal": 3575.00,
  "totalDiscount": 125.00,
  "totalTax": 724.50,
  "totalRevenue": 4174.50
}
```

En este módulo **todavía no exigimos arquitectura estricta con DTOs**.

Para este reporte puedes usar temporalmente una estructura sencilla como `Map<String, Object>`.

En el Módulo 2 veremos por qué esto debería sustituirse por un DTO de respuesta.

---

## ✅ Validaciones Requeridas

### clientName

- `@NotBlank`
- 2-120 caracteres

### projectCode

- `@NotBlank`
- 3-30 caracteres

### serviceCategory

- `@NotNull`

### workDate

- `@NotNull`
- `@PastOrPresent`

### hoursWorked

- obligatorio
- mínimo 0.01
- máximo 24
- máximo 2 decimales

### hourlyRate

- obligatorio
- mínimo 0.01
- máximo 2 decimales

### discountPercent

- obligatorio
- 0-50
- máximo 2 decimales

### taxPercent

- obligatorio
- 0-30
- máximo 2 decimales

---

## 🚨 Manejo Global de Excepciones

Usa `@RestControllerAdvice`.

### 400 Bad Request

Para:

- Bean Validation;
- enum inválido;
- fecha mal formada;
- período invertido;
- JSON mal formado.

### 404 Not Found

Para un `id` inexistente.

Los filtros sin coincidencias deben devolver:

```http
200 OK
```

con:

```json
[]
```

---

## 🎓 Conceptos Clave

### BigDecimal

Incorrecto:

```java
double total;
```

Correcto:

```java
BigDecimal total;
```

### BigDecimal es inmutable

Esto no modifica el valor original:

```java
subtotal.add(tax);
```

Debes utilizar el resultado devuelto.

### Comparación

No:

```java
amount == BigDecimal.ZERO
```

Usa `compareTo`.

### No conviertas a double

Evita convertir los importes a `double` para calcular.

### Centraliza la fórmula

No copies la fórmula económica en `create`, `update`, Controller y reportes.

---

## 🚀 Criterios de Aceptación

- [ ] CRUD completo.
- [ ] Controller sin lógica monetaria.
- [ ] Repository sin lógica de negocio.
- [ ] Cálculos en Service.
- [ ] Uso exclusivo de `BigDecimal` para dinero.
- [ ] Redondeo correcto.
- [ ] El servidor ignora importes derivados enviados por el cliente.
- [ ] POST devuelve 201 + Location.
- [ ] DELETE devuelve 204.
- [ ] ID inexistente devuelve 404.
- [ ] Bean Validation devuelve 400.
- [ ] Enum inválido devuelve 400.
- [ ] Fecha inválida devuelve 400.
- [ ] Auditoría automática.
- [ ] PUT conserva `id` y `createdAt`.
- [ ] PUT recalcula importes.
- [ ] `projectCode` se normaliza.
- [ ] Filtros funcionando.
- [ ] Rango invertido devuelve 400.
- [ ] Reporte suma correctamente.
- [ ] Se utilizan Query Methods derivados.

---

## 🌟 Extras Opcionales

### Extra 1 — Tarifa mínima configurable

Usa una propiedad como:

```properties
billing.minimum-hourly-rate=...
```

e investiga `@ConfigurationProperties`.

### Extra 2 — Máximo de 24 horas diarias

Impedir que la suma de todos los registros de un mismo día supere 24 horas.

### Extra 3 — Facturación por categoría

```http
GET /api/billing-records/reports/by-category
```

### Extra 4 — Proyecto más rentable

Determinar qué `projectCode` acumula más ingresos.

### Extra 5 — Exportación CSV

Exportar registros a CSV como primer acercamiento opcional a Java I/O.

---

## 📝 Notas Importantes

No confíes en valores económicos proporcionados por el cliente.

Evita duplicar cálculos.

No realices cálculos monetarios en Controller.

No introduzcas lógica de negocio en Repository.

No permitas que el cliente controle auditoría o campos calculados.

---

## 🧪 Cómo Probar la API

1. Crear un registro sin descuento.
2. Comprobar manualmente el cálculo.
3. Crear otro con descuento.
4. Comprobar impuestos.
5. Enviar un `totalAmount` falso y verificar que se ignora.
6. Modificar horas mediante PUT.
7. Comprobar que los importes cambian.
8. Buscar por cliente.
9. Buscar por proyecto.
10. Buscar por categoría.
11. Buscar entre fechas.
12. Probar rango invertido.
13. Consultar reporte.
14. Probar fecha futura.
15. Probar enum inválido.
16. Eliminar.
17. Consultar ID eliminado.

Se incluye colección Postman en `/postman`.

---

## 📚 Recursos Útiles

- Spring Data JPA Query Methods
- Jakarta Bean Validation
- Java `BigDecimal`
- Java `RoundingMode`
- Java `LocalDate`
- Spring `@RestControllerAdvice`
- Spring Data JPA Auditing

---

## 🗺️ Plan de Desarrollo Sugerido

### Fase 1 — Modelo

1. Crear `ServiceCategory`.
2. Crear `BillingRecord`.
3. Añadir validaciones.
4. Configurar auditoría.

### Fase 2 — Persistencia

5. Crear Repository.
6. Implementar CRUD inicial.

### Fase 3 — Servicio

7. Crear interfaz.
8. Crear implementación.
9. Centralizar búsqueda por ID.
10. Normalizar datos.

### Fase 4 — Cálculos

11. Subtotal.
12. Descuento.
13. Base imponible.
14. Impuestos.
15. Total.
16. Redondeo.

### Fase 5 — REST

17. POST.
18. GET.
19. PUT.
20. DELETE.

### Fase 6 — Excepciones

21. 404.
22. Rango inválido.
23. `ApiError`.
24. `GlobalExceptionHandler`.

### Fase 7 — Consultas

25. Cliente.
26. Proyecto.
27. Categoría.
28. Fechas.

### Fase 8 — Reporte

29. Obtener registros.
30. Sumar horas.
31. Sumar subtotal.
32. Sumar descuentos.
33. Sumar impuestos.
34. Sumar total.

### Fase 9 — Revisión

35. Probar límites.
36. Buscar duplicación.
37. Revisar responsabilidades.
38. Comprobar cálculos manualmente.

---

## 💡 Consejo Final

Este proyecto marca un cambio importante: el backend ya no se limita a mover datos entre HTTP y la base de datos.

Ahora el backend es **el propietario de una regla económica**.

No busques solo que el POST guarde. Busca que ningún consumidor pueda dejar la información económica en un estado incoherente.

Eso te preparará para entender por qué en el Módulo 2 necesitaremos DTOs de entrada y salida.
