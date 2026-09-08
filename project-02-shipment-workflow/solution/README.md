# Solution — Proyecto 2

Implementación completa de referencia.

Consulta esta carpeta cuando hayas terminado tu versión o quieras comparar una decisión concreta.

## Decisiones destacadas

- El Service controla el estado.
- El POST fuerza `REGISTERED`.
- El PUT copia solo campos editables.
- Cada transición tiene una operación explícita.
- Las transiciones inválidas y duplicados producen `409 Conflict`.
- Solo los envíos cancelados pueden eliminarse.
- La búsqueda por ID se centraliza en un método auxiliar privado.

No se utilizan DTOs todavía de forma intencionada: serán el foco del Módulo 2.
