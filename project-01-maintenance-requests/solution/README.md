# Solution

Implementación completa de referencia para el Proyecto 1.

## Decisiones principales

- Arquitectura `Controller → Service → Repository`.
- Inyección por constructor.
- `JpaRepository` como abstracción de persistencia.
- Bean Validation en la entidad únicamente por el alcance pedagógico del Módulo 1.
- `@RestControllerAdvice` para centralizar errores.
- Excepción específica para recursos inexistentes.
- `201 Created` + cabecera `Location` en creación.
- `204 No Content` en eliminación.
- JPA Auditing para `createdAt` y `updatedAt`.
- `EnumType.STRING` para no persistir posiciones ordinales frágiles.
- `spring.jpa.open-in-view=false` para mantener el acceso a persistencia dentro de la capa transaccional.

## Nota sobre DTOs

La solución expone la entidad directamente porque este proyecto pertenece al Módulo 1. No es la estrategia que utilizaremos en los módulos siguientes. A partir del Módulo 2 separaremos el modelo de persistencia del contrato HTTP mediante DTOs.

## Nota sobre `save()` en update

La entidad recuperada está gestionada dentro de una transacción, por lo que Hibernate puede persistir los cambios mediante dirty checking. Se mantiene `repository.save(existing)` de forma explícita en este primer proyecto para que el flujo resulte fácil de seguir. Más adelante compararemos ambas estrategias.
