# Plan 004 — Moderación de contenido

> Plan técnico de la unidad `004-moderacion-contenido`
> Estado: **redactado, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) aprobada
> Alineado con: [`constitution.md`](../../constitution.md) v1.1.0
> Fecha: 29/10/2026

Este documento responde **cómo**. El **qué** y el **por qué** están en la spec.

---

## 1. Principios de diseño

1. **Un solo punto.** Todo contenido de usuario pasa por el mismo gateway; no
   hay atajos por pantalla.
2. **La app no decide.** Aplica el veredicto del servidor; los umbrales viven
   en la nube.
3. **Sin llave en el dispositivo.** La app habla con el backend; el backend
   habla con Azure.
4. **Fail-closed.** Si no se puede validar, no se publica.
5. **El rechazo es un estado.** No es una excepción ni un error de red; la UI
   lo muestra y ofrece corregir.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| MD-01 | Modelos de moderación | `domain/moderation` | Tipo de contenido, solicitud, veredicto y motivo cerrado |
| MD-02 | Contrato del gateway | `domain/moderation` | Extiende `001/K-30`; modera texto, imagen y video |
| MD-03 | Cliente del backend | `data/moderation/remote` | Endpoint, DTOs y mapeo del veredicto |
| MD-04 | Política y veredicto | `data/moderation` | Traduce la respuesta a aprobado/rechazado/temporal |
| MD-05 | Moderación de texto en línea | `data/moderation` | Modera antes de guardar y enviar cuando hay red |
| MD-06 | Moderación diferida | `data/moderation` | Marca el texto pendiente y lo modera al sincronizar |
| MD-07 | Moderación de archivos | `data/moderation` | Valida imagen y video antes de subirlos |
| MD-08 | Errores, mensajes y auditoría | `data/moderation` + `core` | Mensaje único, sin contenido en registros |
| MD-09 | Pruebas y dobles | `test` | Gateway falso, archivos de prueba y contador de llamadas |

### Lo que se reutiliza de 001 y 002

- `001/K-30` `ContentModerationGateway`: se implementa, no se duplica.
- `001/K-11` cliente HTTP, `001/K-14` `ErrorMapper`, `001/K-04` y `001/K-05`.
- `001/K-06` `Logger`, `001/K-07` dispatchers y `001/K-21` pruebas.
- `002/ID-05` repositorio de identidad y `002/ID-08` errores cerrados, como
  referencia de estilo; no se modifican.

---

## 3. Dependencias y orden de construcción

```
MD-01 modelos ──→ MD-02 contrato ──→ MD-04 veredicto ──→ MD-05 texto
                        │                    │
                        ├──→ MD-03 cliente   ├──→ MD-06 diferido
                        │                    ├──→ MD-07 archivos
                        │                    └──→ MD-08 errores
                        └──→ MD-09 dobles

MD-09 acompaña desde MD-02 (no va al final)
```

### Fases

| Fase | Componentes | Puede ir en paralelo |
|---|---|---|
| **F1 — Contrato** | MD-01, MD-02, MD-09 | MD-09 con MD-02 |
| **F2 — Red** | MD-03, MD-04, MD-08 | MD-03 con MD-08 |
| **F3 — Texto** | MD-05, MD-06 | — (MD-06 depende de MD-05) |
| **F4 — Archivos** | MD-07 | — |
| **F5 — Cierre** | MD-09 | — |

---

## 4. Decisiones técnicas

### D-01 — Un gateway, tres tipos de contenido
**Decisión:** `ModerationGateway` recibe el tipo (texto, imagen, video) y
devuelve un veredicto cerrado. No hay un método por pantalla ni un gateway por
feature.
**Por qué:** cumple R-01 y R-14. Un segundo punto de intercepción es la forma
más fácil de saltarse la política sin darse cuenta.
**Alternativa descartada:** moderar dentro de cada repositorio — repite la
llamada y deja huecos.

### D-02 — La app llama al backend, nunca a Azure
**Decisión:** el cliente apunta a un endpoint del backend; la llave de Azure
vive en la nube. Si se exigiera llamada directa, se usa un token efímero
emitido por el backend, nunca una llave estática.
**Por qué:** cumple P6 y R-09. Una llave en el APK es una llave pública.
**Alternativa descartada:** SDK de Azure en la app con llave embebida —
inaceptable por seguridad y por costo.

### D-03 — Texto en línea y texto diferido
**Decisión:** con red, el texto se modera antes de guardarse y enviarse. Sin
red, se guarda localmente marcado como "pendiente de moderación" y se modera
al sincronizar; si se rechaza, queda "rechazado" y no se envía.
**Por qué:** cumple R-02, R-03 y P4. Guardar texto local no expone a nadie;
publicarlo sin moderar sí.
**Alternativa descartada:** bloquear todo el texto sin conexión — rompe
offline-first sin ganar seguridad real.

### D-04 — Imagen y video no se suben sin veredicto
**Decisión:** el archivo se modera antes de la subida. Si se rechaza, se borra
el temporal y la entidad se guarda sin el archivo. Sin red, la entidad se
guarda sin archivo y el archivo no se encola.
**Por qué:** cumple R-04, R-05 y R-06. Un archivo sin moderar en la cola es una
publicación pendiente de aprobación.
**Alternativa descartada:** subir y moderar después — el contenido ya es
público cuando se detecta.

### D-05 — Fail-closed
**Decisión:** timeout, error 5xx o respuesta ambigua se traducen a rechazo
temporal con mensaje de reintento. Nunca a aprobado.
**Por qué:** cumple R-08. Aprobar por defecto convierte una caída en una fuga.
**Alternativa descartada:** fail-open para no bloquear al usuario — inaceptable
para contenido público.

### D-06 — El veredicto es del servidor
**Decisión:** la app no calcula puntajes ni compara umbrales; solo interpreta
aprobado, rechazado o revisión pendiente.
**Por qué:** cumple R-10 y G-06. Cambiar un umbral no debe requerir una nueva
versión de la app.
**Alternativa descartada:** umbrales en el cliente — se desincronizan y
filtran la política.

### D-07 — Auditoría sin contenido
**Decisión:** se registra el tipo, el resultado, la duración y un
identificador opaco; nunca el texto, la imagen, el nombre del usuario ni el
puntaje.
**Por qué:** cumple R-11. Un registro de moderación con contenido es una copia
del contenido rechazado.
**Alternativa descartada:** registrar el texto rechazado para depurar — viola
privacidad y P6.

### D-08 — Idempotencia por contenido
**Decisión:** cada solicitud lleva una clave derivada del contenido y del
usuario; repetirla devuelve el mismo veredicto sin volver a cobrar.
**Por qué:** cumple R-13 y G-07. Los reintentos de red son normales y no deben
duplicar cargos.
**Alternativa descartada:** reintentar sin clave — duplica llamadas y costos.

---

## 5. Dependencias a agregar

**Ninguna.** Se usa el cliente HTTP de 001. El SDK de Azure no entra en la
app. Si apareciera una necesidad nueva, se aplica la skill
`agregar-dependencia` y se pide autorización.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El contrato del backend no está y se inventa | DTOs aislados, pruebas con dobles y Q-01 antes de la primera tarea de red |
| RT-02 | El texto diferido se envía sin veredicto por un orden incorrecto | La sincronización consulta el estado de moderación antes de enviar; prueba C-05 |
| RT-03 | El archivo temporal sobrevive al rechazo | Borrado explícito y prueba de inspección de caché (C-07) |
| RT-04 | El timeout se confunde con rechazo definitivo | Dos estados distintos: rechazado y no validado; ambos bloquean la publicación |
| RT-05 | La clave de idempotencia se genera distinto en cada reintento | Se deriva del contenido y del usuario, no de la hora |
| RT-06 | La app termina con una llave de Azure en `BuildConfig` | T-010 revisa el APK y el repositorio; R-09 |
| RT-07 | El mensaje de rechazo se duplica en cada pantalla | Un solo `UiText` en `core`; C-13 |

---

## 7. Puntos de verificación

| Fase | Verificación |
|---|---|
| F1 | Contrato puro, dobles listos y `grep` sin Android en `domain/moderation` |
| F2 | Pruebas de contrato con servidor local: aprobado, rechazado, timeout y 5xx |
| F3 | Texto en línea y diferido: aprobado se envía, rechazado no (C-02…C-05) |
| F4 | Imagen y video: sin veredicto no se sube; rechazado no queda en caché (C-06…C-09) |
| F5 | `./gradlew ktlintCheck detekt test`; sin llaves; mensaje único (C-11…C-13) |

---

## 8. Lo que este plan no incluye

- No implementa las pantallas que suben contenido (007, 016).
- No implementa el backend ni la configuración de Azure.
- No define categorías ni umbrales: los aplica el servidor.
- No modera contenido ya almacenado.
- No incluye revisión humana ni consola de revisión.

---

## 9. Puerta de aprobación

- [ ] Los componentes y su orden son aceptables.
- [ ] Las decisiones D-01 a D-08 están revisadas.
- [ ] Se confirma que no se agregan dependencias nuevas.
- [ ] Los riesgos técnicos y sus mitigaciones son suficientes.
- [ ] **Aprobado para pasar a `task.md`.**
