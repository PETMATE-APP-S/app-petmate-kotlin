---
name: crear-spec
description: Crea una unidad de trabajo SDD completa (carpeta specs/NNN-nombre/ con spec.md, plan.md y task.md) siguiendo las puertas de aprobación del proyecto. Úsala al abrir cualquier feature, módulo o cambio de arquitectura nuevo, antes de escribir código.
---

# Crear una unidad de trabajo SDD

Procedimiento para abrir una unidad de trabajo nueva en `specs/`.

## Cuándo usarla
- Empieza una feature, un módulo o un cambio de arquitectura.
- El cambio toca más de un archivo o tomaría más de 30 minutos.
- Los requisitos están ambiguos o incompletos.

**Cuándo NO:** correcciones de una línea, erratas, cambios con requisitos
obvios y autocontenidos.

## Paso 0 — Contexto
Lee `constitution.md`, `memoria.md` y `specs/` para no duplicar una unidad que
ya existe.

## Paso 1 — Numera y crea la carpeta
Toma el siguiente número libre, en formato `NNN-nombre-en-kebab-case`:

```bash
ls specs/
mkdir -p specs/002-mi-feature
```

El `NNN` nunca se renumera ni se renombra después. Es el identificador estable
con el que las tareas, los commits y los PRs se refieren a esta unidad.

## Paso 2 — Supuestos antes de redactar
Enumera tus supuestos y pídele al humano que los corrija. No avances en
silencio.

## Paso 3 — `spec.md` (QUÉ y POR QUÉ)

```markdown
# Spec NNN — <Nombre>

## 1. Objetivo
Qué construimos, por qué y para quién.

## 2. Alcance
Lo que esta unidad sí entrega.

## 3. Fuera de alcance
Lo que esta unidad NO entrega y dónde se atenderá.

## 4. Requisitos
| ID | Requisito | Trazabilidad |
|---|---|---|
| R-01 | ... | RF-xx / RNF-xx |

## 5. Criterios de éxito
Condiciones verificables por alguien que no escribió la spec.

## 6. Riesgos
| Riesgo | Impacto | Mitigación |

## 7. Dependencias

## 8. Preguntas abiertas
```

**→ PUERTA: el humano aprueba la spec antes de seguir.**

## Paso 4 — `plan.md` (CÓMO)
Componentes y responsabilidades · dependencias y orden de construcción ·
decisiones técnicas con justificación y alternativas descartadas · riesgos
técnicos · puntos de verificación.

**→ PUERTA: el humano aprueba el plan antes de seguir.**

## Paso 5 — `task.md` (tareas)

```markdown
- [ ] T-001 — <descripción en una línea>
  - Depende de: —
  - Criterio de aceptación: <qué debe ser cierto al terminar>
  - Verificación: <comando exacto>
  - Archivos: <rutas>
```

Reglas: ordenadas por dependencia (no por importancia), una sesión por tarea,
máximo ~5 archivos por tarea, toda tarea con verificación ejecutable.

**→ PUERTA: el humano aprueba las tareas antes de implementar.**

## Paso 6 — Registra
Agrega la unidad nueva a `memoria.md` (§1 y bitácora).

## Reglas duras
- La spec no contiene nombres de clases ni librerías: eso es del plan.
- Todo debe ser coherente con `constitution.md`. Si choca, gana la constitución.
- Lo que no se sepa va a Preguntas abiertas. **Nunca se inventa.**
- No cruces una puerta sin un "sí" explícito.
