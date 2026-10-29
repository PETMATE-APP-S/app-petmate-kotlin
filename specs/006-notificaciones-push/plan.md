# Plan 006 — Notificaciones push

> Plan técnico de la unidad `006-notificaciones-push`
> Estado: **redactado, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) aprobada
> Alineado con: [`constitution.md`](../../constitution.md) v1.1.0
> Fecha: 29/10/2026

Este documento responde **cómo**. El **qué** y el **por qué** están en la spec.

---

## 1. Principios de diseño

1. **Categorías cerradas.** La app conoce un conjunto finito de categorías; un
   valor desconocido se descarta.
2. **El payload no es la pantalla.** La notificación muestra un texto genérico;
   el detalle se carga al abrir.
3. **El usuario manda.** Salvo seguridad, toda categoría se puede apagar y la
   decisión viaja al servidor.
4. **El destino nunca rompe.** Un deep link inválido abre el inicio.
5. **La base es de 001.** Se extiende `001/K-31`; no se reimplementa FCM.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| PS-01 | Categorías y preferencias | `domain/push` | Conjunto cerrado, valores por defecto y reglas de seguridad |
| PS-02 | Contrato de notificación | `domain/push` | Modelo de mensaje recibido y destino |
| PS-03 | Parser y validación | `data/push` | Convierte el payload en un modelo o lo descarta |
| PS-04 | Recepción y presentación | `data/push` | Primer plano, segundo plano y notificación del sistema |
| PS-05 | Deep links | `ui/navigation` | Traduce el destino a una ruta tipada |
| PS-06 | Preferencias y sincronización | `data/push` + `data/remote` | Persiste y envía las preferencias |
| PS-07 | Permiso al activar | `data/push` + `ui` | Integra 003 al encender la primera categoría |
| PS-08 | Deduplicación y colapso | `data/push` | Evita avisos repetidos |
| PS-09 | Pruebas y mensajes simulados | `test` | Payloads de ejemplo y doble de FCM |

### Lo que se reutiliza de 001, 002 y 003

- `001/K-31` `PushRegistrar` y servicio FCM: se extienden.
- `001/K-18` navegación, `001/K-17` caché, `001/K-04` y `001/K-05`.
- `002/ID-11` observador de sesión: registra y borra el token.
- `003` `PermissionGateway` y su degradación de notificaciones.

---

## 3. Dependencias y orden de construcción

```
PS-01 categorías ──→ PS-02 contrato ──→ PS-03 parser ──→ PS-04 recepción
        │                                     │                 │
        ├──→ PS-06 preferencias ──────────────┼─────────────────┤
        ├──→ PS-07 permiso ───────────────────┘                 │
        └──→ PS-08 dedupe ─────────────────────────────────────┤
                                                               ▼
                                                        PS-05 deep links
                                                               │
                                                        PS-09 pruebas
```

### Fases

| Fase | Componentes | Puede ir en paralelo |
|---|---|---|
| **F1 — Modelo** | PS-01, PS-02 | PS-09 con PS-02 |
| **F2 — Recepción** | PS-03, PS-04, PS-08 | PS-03 con PS-08 |
| **F3 — Destinos** | PS-05 | — |
| **F4 — Preferencias** | PS-06, PS-07 | PS-06 con PS-07 |
| **F5 — Cierre** | PS-09 | — |

---

## 4. Decisiones técnicas

### D-01 — Categorías cerradas y tipadas
**Decisión:** `NotificationCategory` es una enumeración con salud, seguimiento,
chat, novedades y seguridad. Un valor desconocido en el payload se descarta y
se registra sin contenido.
**Por qué:** cumple R-02 y R-12. Una categoría libre obliga a la UI a adivinar
y permite que el backend encienda cosas que la app no entiende.
**Alternativa descartada:** categoría como texto libre — imposible de validar.

### D-02 — Texto genérico, detalle bajo demanda
**Decisión:** la notificación muestra un texto fijo por categoría; al abrir, la
pantalla carga el detalle desde la base local o la red.
**Por qué:** cumple R-04 y G-02. Un push con "Firulais tiene displasia" en la
pantalla de bloqueo es una fuga real.
**Alternativa descartada:** incluir el detalle en el payload — cómodo y
peligroso.

### D-03 — Preferencias locales primero, servidor después
**Decisión:** el interruptor cambia de inmediato en local y se envía al
servidor; si el envío falla, queda pendiente y se reintenta.
**Por qué:** cumple R-03 y R-08. El usuario no espera a la red para apagar una
categoría.
**Alternativa descartada:** esperar la confirmación del servidor — el
interruptor "rebota" y el usuario cree que no funcionó.

### D-04 — El permiso se pide al encender, no antes
**Decisión:** al activar la primera categoría se usa el flujo de 003; si el
usuario niega, la categoría queda activa pero la app avisa dentro de la
interfaz.
**Por qué:** cumple R-07 y R-08. Pedirlo al instalar es la causa principal de
negativas permanentes.
**Alternativa descartada:** pedirlo en el arranque — hostil y penalizado por
Play.

### D-05 — Deep link con destino desconocido al inicio
**Decisión:** el destino se valida contra el grafo; si la ruta no existe o la
unidad no está construida, se navega al inicio.
**Por qué:** cumple R-06 y G-04. Un enlace roto no debe cerrar la app ni dejar
una pantalla en blanco.
**Alternativa descartada:** ignorar el toque — el usuario cree que la app no
responde.

### D-06 — Seguridad no desactivable
**Decisión:** la categoría de seguridad no tiene interruptor y el modelo
rechaza intentos de apagarla.
**Por qué:** cumple R-09. Un aviso de "nuevo inicio de sesión" es una medida de
protección, no una preferencia.
**Alternativa descartada:** dejar apagarla — el usuario pierde la única señal
de una cuenta comprometida.

### D-07 — Deduplicación por clave de evento
**Decisión:** cada mensaje trae una clave; si ya se mostró, se ignora.
**Por qué:** cumple R-13. El backend puede reintentar y FCM puede entregar dos
veces.
**Alternativa descartada:** deduplicar por texto — dos eventos distintos con el
mismo texto se perderían.

### D-08 — Primer plano como aviso interno
**Decisión:** con la app abierta se muestra un aviso dentro de la interfaz y no
una notificación del sistema; en segundo plano se usa la notificación.
**Por qué:** cumple R-01 y evita duplicar el aviso. Es el comportamiento
esperado en Android.
**Alternativa descartada:** mostrar siempre notificación del sistema — molesta
con la app abierta.

---

## 5. Dependencias a agregar

**Ninguna.** Firebase ya está en 001. Si Q-04 exige notificaciones con imagen o
acciones, se evalúa con la skill `agregar-dependencia`.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El payload real no coincide con el de ejemplo | Un solo parser y Q-01 antes de la recepción |
| RT-02 | Un campo clínico se cuela en el payload | Prueba de esquema que falla ante campos prohibidos (C-04) |
| RT-03 | El deep link abre una ruta inexistente | Validación contra el grafo y destino al inicio (C-06) |
| RT-04 | Las preferencias se pierden al cerrar la app | Persistencia local y prueba C-03 |
| RT-05 | El permiso se pide dos veces | Un solo punto de activación con el flujo de 003 |
| RT-06 | Una notificación duplicada confunde al usuario | Clave de evento y prueba C-11 |
| RT-07 | El token queda huérfano si el logout falla | 002/ID-11 lo borra en la transición; prueba C-10 |

---

## 7. Puntos de verificación

| Fase | Verificación |
|---|---|
| F1 | Categorías y contrato probados; `grep` sin Android en `domain/push` |
| F2 | Recepción en primer y segundo plano con payloads simulados (C-01, C-11) |
| F3 | Cada categoría abre su destino y un enlace inválido abre el inicio (C-05, C-06) |
| F4 | Preferencias persistidas y sincronizadas; permiso al activar (C-02, C-03, C-07, C-08) |
| F5 | `./gradlew ktlintCheck detekt test`; payload sin datos sensibles (C-04, C-09, C-10) |

---

## 8. Lo que este plan no incluye

- No implementa los interruptores visuales de 017.
- No implementa el envío desde el backend.
- No define las fechas de los recordatorios (008) ni los estados del
  seguimiento (015).
- No implementa el chat (016).
- No agrega notificaciones locales programadas.

---

## 9. Puerta de aprobación

- [ ] Los componentes y su orden son aceptables.
- [ ] Las decisiones D-01 a D-08 están revisadas.
- [ ] Se confirma que no se agregan dependencias nuevas.
- [ ] Los riesgos técnicos y sus mitigaciones son suficientes.
- [ ] **Aprobado para pasar a `task.md`.**
