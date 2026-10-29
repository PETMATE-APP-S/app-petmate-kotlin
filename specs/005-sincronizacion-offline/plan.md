# Plan 005 — Sincronización offline

> Plan técnico de la unidad `005-sincronizacion-offline`
> Estado: **redactado, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) aprobada
> Alineado con: [`constitution.md`](../../constitution.md) v1.1.0
> Fecha: 29/10/2026

Este documento responde **cómo**. El **qué** y el **por qué** están en la spec.

---

## 1. Principios de diseño

1. **Una política, un lugar.** La tabla por entidad vive en `domain`; ningún
   repositorio decide por su cuenta cómo sincronizar.
2. **La cola es de 001.** Se extiende `001/K-29`; no se crea otra.
3. **El motor no conoce entidades.** Trabaja con un contrato de entidad
   sincronizable; 007 y 008 aportan sus adaptadores.
4. **Nada se pierde y nada se duplica.** Idempotencia y lápidas desde el
   primer día.
5. **El usuario ve el estado.** Pendiente, sincronizando y error son estados
   de producto, no detalles técnicos.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| SY-01 | Política por entidad | `domain/sync` | Tabla de crear/editar/borrar y conflicto por entidad |
| SY-02 | Contrato de entidad sincronizable | `domain/sync` | Identidad, dependencia, serialización y conflicto |
| SY-03 | Extensión del outbox | `data/sync` | Reutiliza `001/K-29` y agrega estado y dependencias |
| SY-04 | Planificador | `data/sync` | Orden FIFO por entidad y respeto del grafo |
| SY-05 | Idempotencia | `data/sync` | Clave estable por operación |
| SY-06 | Reintentos y errores | `data/sync` | Backoff, transitorio contra permanente |
| SY-07 | Resolución de conflictos | `data/sync` | Servidor para clínico, reciente para mascota |
| SY-08 | Lápidas | `data/sync` | Borrado offline que no resucita |
| SY-09 | Estado de sincronización | `data/sync` + `ui/components` | `Flow` de estado por elemento e indicador visual |
| SY-10 | Pruebas y entidad de prueba | `test` | Motor probado sin depender de 007 ni 008 |

### Lo que se reutiliza de 001 y 002

- `001/K-29` outbox y `001/K-28` conectividad: se extienden.
- `001/K-10` Room, `001/K-17` caché, `001/K-26` reintentos.
- `001/K-04`, `001/K-05`, `001/K-06`, `001/K-07` y `001/K-21`.
- `002/ID-06` sesión y `002/ID-08` errores, como referencia; no se modifican.

---

## 3. Dependencias y orden de construcción

```
SY-01 política ──→ SY-02 contrato ──→ SY-03 outbox ──→ SY-04 planificador
                          │                                  │
                          ├──→ SY-05 idempotencia ───────────┤
                          ├──→ SY-06 reintentos ─────────────┤
                          ├──→ SY-07 conflictos ─────────────┤
                          └──→ SY-08 lápidas ────────────────┤
                                                             ▼
                                                      SY-09 estado
                                                             │
                                                      SY-10 pruebas
```

### Fases

| Fase | Componentes | Puede ir en paralelo |
|---|---|---|
| **F1 — Política** | SY-01, SY-02 | SY-10 con SY-02 |
| **F2 — Cola** | SY-03, SY-04 | — (SY-04 depende de SY-03) |
| **F3 — Garantías** | SY-05, SY-06, SY-07, SY-08 | Las cuatro entre sí |
| **F4 — Visibilidad** | SY-09 | — |
| **F5 — Cierre** | SY-10 | — |

---

## 4. Decisiones técnicas

### D-01 — La política vive en `domain`
**Decisión:** `SyncPolicy` declara por entidad si se crea, edita o borra sin
conexión y qué conflicto aplica. Los repositorios consultan esa tabla.
**Por qué:** cumple R-13 y evita cinco comportamientos distintos. Si cada
unidad decidiera, el carnet y la mascota se comportarían diferente ante el
mismo corte de red.
**Alternativa descartada:** política dentro de cada repositorio — se duplica y
se desincroniza.

### D-02 — Se extiende el outbox de 001
**Decisión:** `001/K-29` gana estado por operación (pendiente, enviando, error
permanente), dependencia y clave de idempotencia. No se crea una segunda cola.
**Por qué:** cumple R-01 y R-02. Dos colas compiten por el mismo recurso y
pierden el orden.
**Alternativa descartada:** cola por entidad — rompe el orden entre mascota y
vacuna.

### D-03 — Id de cliente más clave de idempotencia
**Decisión:** al crear sin red, el cliente genera un id estable y una clave de
idempotencia derivada de ese id. El servidor debe aceptar ambos.
**Por qué:** cumple R-04. Sin id estable, un reintento crea un duplicado; sin
clave, el servidor no puede distinguir un reintento de una creación nueva.
**Alternativa descartada:** crear en local con id temporal y esperar el del
servidor — obliga a reescribir relaciones y falla si el proceso muere.
**Pendiente:** Q-01 confirma que el backend lo acepta.

### D-04 — Orden por grafo, no solo FIFO
**Decisión:** el planificador respeta la dependencia declarada por la entidad
(una vacuna depende de su mascota) y dentro de cada nivel aplica FIFO.
**Por qué:** cumple R-03. El FIFO puro envía la vacuna antes de que exista la
mascota y el servidor la rechaza.
**Alternativa descartada:** reintentar hasta que la mascota llegue — genera
errores 4xx permanentes falsos.

### D-05 — Conflicto: servidor en clínico, reciente en mascota
**Decisión:** en vacunas, desparasitación, peso, alergias y condiciones gana
la versión del servidor y se avisa. En los datos de la mascota gana la versión
con marca más reciente.
**Por qué:** decisión aprobada en la spec. Lo clínico lo escribe un
veterinario y no debe sobrescribirse desde el dispositivo; los datos de la
mascota son del dueño y su último cambio es el válido.
**Alternativa descartada:** una sola regla para todo — o se pierde trabajo del
dueño o se pisa el diagnóstico.
**Pendiente:** Q-07 define qué reloj es la autoridad.

### D-06 — Lápida para el borrado
**Decisión:** borrar sin red deja una marca de borrado que se envía y se
conserva hasta confirmación; al sincronizar no se recrea el elemento.
**Por qué:** cumple R-09. Sin lápida, el servidor devuelve el elemento en la
siguiente lectura y "reaparece".
**Alternativa descartada:** borrar solo en local — el dato vuelve.

### D-07 — 4xx es permanente, 5xx y red son transitorios
**Decisión:** el planificador separa los errores. Un 4xx se marca como error
permanente, se muestra y no se reintenta; 5xx, timeout y corte de red se
reintentan con backoff.
**Por qué:** cumple R-05 y R-06. Reintentar un 400 castiga al servidor y oculta
el problema al usuario.
**Alternativa descartada:** reintentar todo — el error real nunca se ve.

### D-08 — Estado observable por elemento
**Decisión:** el outbox expone un `Flow<SyncStatus>` por operación; la UI lo
pinta con un indicador y un botón de reintento.
**Por qué:** cumple R-10. El usuario necesita saber si su cambio ya llegó.
**Alternativa descartada:** un solo estado global — no dice qué falló.

### D-09 — Sin `WorkManager` en la primera versión
**Decisión:** la sincronización se dispara al abrir la app, al recuperar la
red y al guardar. No se agrega trabajo periódico en segundo plano.
**Por qué:** cumple el alcance sin una dependencia nueva y sin pelear con las
restricciones de batería. Q-09 decide si hace falta después.
**Alternativa descartada:** `WorkManager` desde el inicio — más complejidad y
una dependencia que hay que autorizar.

---

## 5. Dependencias a agregar

**Ninguna.** Se usa el outbox y la conectividad de 001. Si Q-09 exige trabajo
en segundo plano, se aplica la skill `agregar-dependencia` y se pide
autorización.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El backend no acepta ids de cliente | Q-01 antes de implementar; si no, se deshabilita la creación offline y se documenta |
| RT-02 | El motor se acopla a la mascota y no sirve para el carnet | Contrato de entidad y entidad de prueba; 007/008 solo agregan adaptadores |
| RT-03 | El grafo de dependencias tiene ciclos | Validación al registrar una entidad; prueba de arranque |
| RT-04 | La cola crece sin control | Límite y estado visible; Q-04 |
| RT-05 | El conflicto elige mal por reloj del dispositivo | Se prefiere la marca del servidor; Q-07 |
| RT-06 | La sincronización bloquea la UI | Dispatchers inyectados y estado de carga; R-11 |
| RT-07 | Un texto pendiente de moderación se envía antes del veredicto | Integración con 004 y prueba C-12 |

---

## 7. Puntos de verificación

| Fase | Verificación |
|---|---|
| F1 | Política y contrato probados; `grep` sin Android en `domain/sync` |
| F2 | Crear sin red encola y sobrevive a la muerte del proceso (C-01, C-09) |
| F3 | Idempotencia, orden, 4xx, conflictos y lápidas (C-03…C-08) |
| F4 | Indicador de estado y reintento manual (C-11) |
| F5 | `./gradlew ktlintCheck detekt test`; matriz completa y lectura offline (C-10, C-12) |

---

## 8. Lo que este plan no incluye

- No implementa las pantallas de mascotas ni del carnet.
- No define la política de chat ni de consultas.
- No modera contenido.
- No agrega trabajo periódico en segundo plano.
- No resuelve conflictos entre dos usuarios del mismo negocio.

---

## 9. Puerta de aprobación

- [ ] Los componentes y su orden son aceptables.
- [ ] Las decisiones D-01 a D-09 están revisadas.
- [ ] Se confirma que no se agregan dependencias nuevas.
- [ ] Los riesgos técnicos y sus mitigaciones son suficientes.
- [ ] **Aprobado para pasar a `task.md`.**
