# Plan 008 — Carnet de salud

> Plan técnico de la unidad `008-carnet-salud`
> Estado: **redactado, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) aprobada
> Alineado con: [`constitution.md`](../../constitution.md) v1.1.0
> Fecha: 29/10/2026

Este documento responde **cómo**. El **qué** y el **por qué** están en la spec.

---

## 1. Principios de diseño

1. **El carnet vive en el teléfono.** Se lee de Room; la red solo lo actualiza.
2. **Las alergias no se negocian.** Son críticas, siempre visibles y siempre
   compartidas.
3. **Lo del veterinario no se toca.** El origen manda: un registro veterinario
   es de solo lectura desde el carnet.
4. **Una sola línea de tiempo.** Las seis secciones son vistas filtradas del
   mismo flujo de eventos.
5. **Sin conexión es el caso normal**, no la excepción.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| CS-01 | Modelos del carnet | `domain/health` | Vacuna, desparasitación, peso, alergia, condición y consulta |
| CS-02 | Contratos y casos de uso | `domain/health` | Observar, registrar, editar y borrar según origen |
| CS-03 | Entidades y DAOs | `data/local` | Persistencia de las seis secciones y su `Flow` |
| CS-04 | DTOs y mapeo | `data/remote` + `data/mapper` | Contrato de red y traducción |
| CS-05 | Repositorio | `data/repository` | Room primero y sincronización según 005 |
| CS-06 | Línea de tiempo y filtros | `domain/health` + `ui` | Unifica eventos, ordena y filtra |
| CS-07 | Alergias críticas | `domain/health` | Caso de uso que 009 consume sin filtros |
| CS-08 | Pantallas del carnet | `ui/health` | Resumen, secciones y formularios |
| CS-09 | Regla de origen | `domain/health` | Impide editar o borrar lo veterinario |
| CS-10 | Adjuntos | `data/health` | Comprobante con permiso y moderación |
| CS-11 | Pruebas y dobles | `test` | Reloj fijo, datos de ejemplo y API falsa |

### Lo que se reutiliza de 001 a 007

- `001/K-10` Room, `001/K-17` caché, `001/K-20` componentes y `001/K-18`.
- `004` moderación de imagen, `005` política offline y `007` mascota y foto.
- `002/ID-08` errores cerrados.

---

## 3. Dependencias y orden de construcción

```
CS-01 modelos ──→ CS-02 contratos ──→ CS-05 repositorio ──→ CS-08 pantallas
      │                 │                    │
      ├──→ CS-03 Room ──┤                    ├──→ CS-06 línea de tiempo
      ├──→ CS-04 DTOs ──┤                    ├──→ CS-07 alergias
      └──→ CS-09 origen ┘                    └──→ CS-10 adjuntos

CS-11 acompaña desde CS-01 (no va al final)
```

### Fases

| Fase | Componentes | Puede ir en paralelo |
|---|---|---|
| **F1 — Dominio** | CS-01, CS-02, CS-07, CS-09 | CS-07 y CS-09 con CS-02 |
| **F2 — Datos** | CS-03, CS-04, CS-05 | CS-03 con CS-04 |
| **F3 — Vista** | CS-06 | — |
| **F4 — Pantallas** | CS-08, CS-10 | CS-08 con CS-10 |
| **F5 — Cierre** | CS-11 | — |

---

## 4. Decisiones técnicas

### D-01 — Seis secciones, una línea de tiempo
**Decisión:** todas las secciones se modelan como eventos con tipo y fecha; la
línea de tiempo los une y los filtros son vistas del mismo flujo.
**Por qué:** cumple R-01 y R-09. Seis listas separadas duplican el orden y la
paginación, y el usuario no puede ver el historial completo.
**Alternativa descartada:** una pantalla por sección con su propia consulta —
más código y seis formas de ordenar.

### D-02 — El carnet se lee de Room, sin red
**Decisión:** la pantalla del carnet no dispara peticiones; el repositorio
sincroniza en segundo plano y Room notifica.
**Por qué:** cumple R-10, C-01 y C-02. En la veterinaria sin señal, el carnet
tiene que abrir igual.
**Alternativa descartada:** refrescar al abrir — sin red, la pantalla queda
vacía o en error.

### D-03 — Las alergias son una regla de dominio
**Decisión:** `GetCriticalAllergiesUseCase` devuelve todas las alergias activas
sin aceptar filtros ni selección de secciones. 009 lo consume directamente.
**Por qué:** cumple R-06 y G-02. Si la regla viviera en la UI de compartir, un
cambio de pantalla podría ocultarlas.
**Alternativa descartada:** marcar la alergia como "siempre incluida" en el
payload de 009 — depende de que 009 se acuerde.

### D-04 — Lo veterinario es inmutable desde el carnet
**Decisión:** cada registro lleva `origin` (dueño o veterinario). El caso de
uso de edición y borrado rechaza los de origen veterinario.
**Por qué:** cumple R-11 y G-03. El dueño no debe alterar un diagnóstico.
**Alternativa descartada:** permitir editar y auditar después — el daño ya está
hecho.

### D-05 — El origen se muestra, no se deduce
**Decisión:** la tarjeta de cada evento muestra una etiqueta de origen y un
icono distinto.
**Por qué:** cumple R-12. El usuario necesita saber qué escribió él y qué
escribió el veterinario.
**Alternativa descartada:** deducirlo por el nombre del autor — frágil y a veces
desconocido.

### D-06 — Peso en kilogramos con evolución simple
**Decisión:** se guarda en kilogramos y se muestra una lista cronológica con una
evolución simple; no se convierte de unidad.
**Por qué:** cumple R-05 y evita errores de conversión. La gráfica detallada
queda para Q-03.
**Alternativa descartada:** admitir varias unidades y convertir — más superficie
de error sin beneficio claro.

### D-07 — Filtros en el cliente
**Decisión:** los filtros por tipo y fecha se aplican sobre el flujo local.
**Por qué:** el carnet ya está en memoria y filtrar en local es inmediato y
funciona sin red.
**Alternativa descartada:** filtrar en el servidor — obliga a red para una
operación de lectura.

### D-08 — Escritura offline según 005
**Decisión:** el carnet no define su propia cola; usa la política y el motor de
005.
**Por qué:** cumple R-15 y evita un segundo comportamiento offline.
**Alternativa descartada:** cola propia del carnet — dos colas y dos órdenes.

### D-09 — El comprobante pasa por 004
**Decisión:** la foto del comprobante se modera antes de subirse, igual que la
foto de la mascota.
**Por qué:** cumple R-16. Un comprobante es contenido del usuario y puede
contener cualquier cosa.
**Alternativa descartada:** exceptuar los comprobantes — abre un hueco en P13.

### D-10 — El historial médico espera a 014
**Decisión:** la sección existe con estado vacío explicativo hasta que 014
registre consultas.
**Por qué:** cumple R-08 sin inventar datos ni adelantar 014.
**Alternativa descartada:** ocultar la sección — el usuario no entiende por qué
falta.

---

## 5. Dependencias a agregar

**Ninguna.** Se usan Room, el cliente HTTP, Coil y la moderación ya declarados.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El contrato del carnet no está y se inventa | DTOs aislados y Q-01 antes de la primera tarea de red |
| RT-02 | El esquema de Room crece y la migración falla | T-002 exige migración probada; cambio de esquema requiere autorización |
| RT-03 | La pantalla del carnet dispara red sin querer | Prueba C-02 con contador de llamadas en cero |
| RT-04 | Un registro veterinario se puede editar por un camino alterno | La regla vive en el caso de uso, no en la UI; prueba C-07 |
| RT-05 | Las alergias se filtran por error al compartir | Caso de uso sin filtros; prueba C-06 y consumo desde 009 |
| RT-06 | El peso se muestra desordenado por fechas iguales | Orden estable por fecha e id |
| RT-07 | El comprobante rechazado queda adjunto | 004 borra el temporal; prueba C-12 |

---

## 7. Puntos de verificación

| Fase | Verificación |
|---|---|
| F1 | Dominio puro; alergias críticas y regla de origen probadas (C-06, C-07) |
| F2 | Room con migración; carnet completo desde Room sin red (C-01, C-02) |
| F3 | Línea de tiempo y filtros con datos fijos (C-03, C-09, C-10) |
| F4 | Seis secciones, formularios, origen y adjuntos (C-04, C-05, C-08, C-12) |
| F5 | `./gradlew ktlintCheck detekt test`; escritura offline (C-11) |

---

## 8. Lo que este plan no incluye

- No implementa el QR ni el compartir (009).
- No implementa la consulta, la receta ni el cobro (014).
- No implementa el seguimiento (015) ni la IA (019).
- No envía recordatorios: solo guarda las fechas para 006.
- No edita los datos de la mascota (007).

---

## 9. Puerta de aprobación

- [ ] Los componentes y su orden son aceptables.
- [ ] Las decisiones D-01 a D-10 están revisadas.
- [ ] Se confirma que no se agregan dependencias nuevas.
- [ ] Los riesgos técnicos y sus mitigaciones son suficientes.
- [ ] **Aprobado para pasar a `task.md`.**
