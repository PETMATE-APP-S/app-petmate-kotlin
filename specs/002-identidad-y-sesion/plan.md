# Plan 002 — Identidad y sesión

> Plan técnico de la unidad `002-identidad-y-sesion`
> Estado: **redactado, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) aprobada
> Alineado con: [`constitution.md`](../../constitution.md) v1.1.0
> Fecha: 29/10/2026

Este documento responde **cómo**. El **qué** y el **por qué** están en la spec.

---

## 1. Principios de diseño

1. **El dominio no conoce la red ni Android.** Modelos, contratos y casos de
   uso viven en `domain` como Kotlin puro.
2. **La sesión es una sola fuente de verdad.** La UI observa un estado de
   sesión; nunca lee tokens directamente.
3. **La app no inventa el contrato.** Rutas, campos, códigos de error y
   políticas del código se toman del backend; mientras no existan, se prueban
   con dobles y quedan en preguntas abiertas.
4. **Nada de credenciales en claro.** Ni en disco, ni en registros, ni en
   mensajes de error.
5. **La identidad no bloquea el arranque.** Si la sesión guardada no se puede
   restaurar, se pide login; si el almacenamiento cifrado falla, se degrada de
   forma controlada.

---

## 2. Componentes

Los identificadores `ID-xx` son de esta unidad. Los componentes de 001 se citan
como `001/K-xx`.

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| ID-01 | Modelos de identidad | `domain/identity/model` | Cuenta, estado de sesión, tokens, desafío de verificación y errores de identidad |
| ID-02 | Contratos de identidad | `domain/identity/repository` | Interfaces de identidad, sesión y renovación; sin detalles de red |
| ID-03 | Casos de uso | `domain/identity/usecase` | Alta, verificación, reenvío, login, recuperación, restauración y logout |
| ID-04 | Cliente remoto de identidad | `data/identity/remote` | Endpoints, DTOs, serialización y traducción a modelos de dominio |
| ID-05 | Repositorio de identidad | `data/identity/repository` | Orquesta red, sesión y errores; implementa los contratos de ID-02 |
| ID-06 | Estado de sesión | `data/session` | Extiende 001/K-15 y 001/K-16; expone `Flow` de sesión y persiste de forma cifrada |
| ID-07 | Renovación serializada | `data/remote` | Extiende 001/K-13; una sola renovación concurrente y un solo reintento |
| ID-08 | Errores y textos de identidad | `data/identity/error` + `core` | Mapa cerrado de fallas a `AppError` y `UiText`; mensajes genéricos |
| ID-09 | Pantallas y ViewModels | `ui/identity` | Bienvenida, alta, verificación, login y recuperación, cada una con su estado |
| ID-10 | Navegación y guardas | `ui/navigation` | Grafo de identidad, restauración de sesión y redirección sin parpadeo |
| ID-11 | Registro de push | `data/push` | Engancha login y logout con 001/K-31 de forma idempotente |
| ID-12 | Pruebas y dobles | `test` | API falsa, sesión en memoria, reglas de corrutinas y utilidades |

### Lo que se reutiliza de 001

- `001/K-04` `Result` / `AppError`, `001/K-05` `UiText`, `001/K-06` `Logger`,
  `001/K-07` `DispatcherProvider`.
- `001/K-08` y `001/K-09` para inyección; `001/K-11` para el cliente HTTP.
- `001/K-12` y `001/K-13` para el token y la renovación.
- `001/K-14` para traducir fallas de red.
- `001/K-15` y `001/K-16` para el almacenamiento y el estado de sesión.
- `001/K-18` para el `NavHost`, `001/K-20` para los componentes visuales.
- `001/K-21` para la infraestructura de pruebas y `001/K-31` para push.

No se duplica ninguno: se extienden o se usan tal cual.

---

## 3. Dependencias y orden de construcción

```
ID-01 modelos ──→ ID-02 contratos ──→ ID-03 casos de uso ──→ ID-09 pantallas ──→ ID-10 navegación
     │
     ├──→ ID-04 cliente ──→ ID-05 repositorio
     │                          ▲
     ├──→ ID-06 sesión ─────────┤
     │         │                │
     │         └──→ ID-11 push  │
     ├──→ ID-07 refresh ────────┤
     └──→ ID-08 errores ────────┘

ID-12 pruebas acompaña desde ID-01 (no va al final)
```

### Fases

| Fase | Componentes | Puede ir en paralelo |
|---|---|---|
| **F1 — Dominio** | ID-01, ID-02, ID-03 | ID-01 con ID-12 |
| **F2 — Red y errores** | ID-04, ID-08 | ID-04 con ID-08 |
| **F3 — Sesión** | ID-06, ID-07 | — (ID-07 depende de ID-06) |
| **F4 — Repositorio** | ID-05 | — |
| **F5 — Presentación** | ID-09, ID-10 | ID-09 primero; ID-10 en cuanto existan las rutas |
| **F6 — Integración** | ID-11, ID-12 | ID-11 con el cierre de pruebas |

Las pruebas no son una fase final: cada componente desde F1 llega con las
suyas. ID-12 solo agrupa los dobles compartidos.

---

## 4. Decisiones técnicas

### D-01 — Un ViewModel por pantalla, no un ViewModel global de identidad
**Decisión:** bienvenida, alta, verificación, login y recuperación tienen su
propio `ViewModel` con un `UiState` inmutable. La lógica compartida vive en los
casos de uso, no en un estado global.
**Por qué:** cumple P5 y hace cada pantalla probable por separado. Un
`ViewModel` global acumula estados que no se pueden limpiar y termina
gobernando la navegación desde la UI.
**Alternativa descartada:** un `AuthViewModel` único para todo el flujo — más
corto de escribir, imposible de probar por pantalla y con fugas de estado entre
pasos.

### D-02 — El dominio define los contratos; los DTOs viven en `data`
**Decisión:** `domain` declara las interfaces y los modelos; `data` implementa
con DTOs, serialización y mapeo. Ningún DTO ni anotación de red cruza a
`domain`.
**Por qué:** P3 y R-16 de 001. Si el contrato del backend cambia, solo se toca
`data`; el dominio y la UI quedan intactos.
**Alternativa descartada:** usar los DTOs como modelos de dominio — ahorra
mappers hoy y acopla toda la app al contrato de red.

### D-03 — La sesión se observa como un solo `Flow<SessionState>`
**Decisión:** `SessionRepository` expone un estado inmutable con cuatro casos:
sin sesión, restaurando, autenticado y expirado. La UI y la navegación observan
ese flujo; nadie más lee el almacenamiento de tokens.
**Por qué:** una sola fuente de verdad evita que dos pantallas decidan cosas
distintas sobre la misma sesión. También permite restaurar antes de pintar la
primera pantalla.
**Alternativa descartada:** cada pantalla consulta el almacén — duplica
lecturas, produce parpadeos y facilita condiciones de carrera.

### D-04 — Renovación single-flight con un solo reintento
**Decisión:** la renovación se serializa con un `Mutex`. Si llegan varias
peticiones con el token expirado, la primera renueva y las demás esperan el
mismo resultado. Cada petición original se reintenta **una** vez; un segundo
401 devuelve sesión expirada y lleva al login.
**Por qué:** cumple R-07 y evita el riesgo G-02. Reintentar sin límite produce
bucles y castiga al backend.
**Alternativa descartada:** renovar por petición — multiplica llamadas y rota
el refresh token varias veces, lo que puede invalidar la sesión.

### D-05 — Errores cerrados y mensajes genéricos
**Decisión:** `data` traduce cada falla a un conjunto cerrado de errores de
identidad (credenciales inválidas, código inválido, código expirado, límite de
intentos, correo no verificable, red, servidor). La UI muestra un `UiText`
propio; el mensaje crudo del backend nunca se pinta.
**Por qué:** cumple R-04, R-10 y R-15, y evita enumerar cuentas (G-03). Un
mensaje distinto para "correo no existe" y "contraseña incorrecta" revela qué
correos están registrados.
**Alternativa descartada:** mostrar el mensaje del backend tal cual — filtra
detalles internos y rompe la traducción.

### D-06 — La política del código la dicta el servidor
**Decisión:** la app muestra la expiración, el tiempo de reenvío y el límite de
intentos que devuelve el servidor. Si la respuesta no trae esos valores, la
pantalla no inventa un temporizador: muestra el estado de "código enviado" y
deja el reenvío sujeto a la respuesta del backend.
**Por qué:** cumple R-11 y no fija valores que el backend puede contradecir.
**Alternativa descartada:** un temporizador fijo de 60 s en la app — si el
backend permite reenviar a los 30 s o exige 5 minutos, la UI miente.

### D-07 — Validación de contraseña delegada al contrato
**Decisión:** la app solo valida que el campo no esté vacío y que no exceda un
máximo defensivo de 128 caracteres mientras Q-02 no se resuelva. La política
real (mínimo, complejidad, confirmación) se toma del contrato y se muestra como
ayuda; el backend sigue siendo la autoridad.
**Por qué:** cumple R-12 sin duplicar reglas que pueden cambiar. Una validación
local más estricta que la del backend bloquea altas válidas.
**Alternativa descartada:** imponer una regla propia (por ejemplo, 8 caracteres
y un símbolo) — puede contradecir al backend y genera mensajes distintos.

### D-08 — Logout local-first con invalidación pendiente
**Decisión:** el logout borra primero los datos locales y cambia el estado a
"sin sesión"; después intenta invalidar en el servidor. Si no hay red, guarda
una marca de invalidación pendiente y la reintenta en el siguiente arranque con
red. La UI nunca espera al servidor para cerrar.
**Por qué:** cumple R-08 y R-09. Bloquear el cierre de sesión por falta de red
deja al usuario atrapado en una sesión que ya no quiere.
**Alternativa descartada:** esperar la respuesta del servidor — sin red, el
usuario no puede salir.
**Pendiente:** la política exacta de la marca y su reintento dependen de Q-06.

### D-09 — Restauración de sesión antes de decidir la primera pantalla
**Decisión:** al arrancar se muestra un estado de carga mínimo mientras se lee
la sesión cifrada. Cuando el estado pasa a "autenticado" o "sin sesión", la
navegación elige el grafo principal o el de identidad. Nunca se pinta el login
y luego se salta al inicio.
**Por qué:** evita el parpadeo y cumple R-17. Además, el usuario no ve una
pantalla de login que no le corresponde.
**Alternativa descartada:** decidir en `MainActivity` después de componer — se
ve el login un instante y la navegación se vuelve impredecible.

### D-10 — Push se dispara por transición de sesión, no por pantalla
**Decisión:** un observador del estado de sesión registra el token al pasar a
"autenticado" y lo borra al pasar a "sin sesión". La operación es idempotente:
registrar dos veces el mismo token no duplica nada.
**Por qué:** cumple R-18 y 001/D-19. Si la pantalla de login lo hiciera, un
arranque con sesión restaurada nunca registraría el token.
**Alternativa descartada:** llamar al registro desde el botón de login — deja
fuera los arranques con sesión ya guardada y el registro tras renovar.

### D-11 — Pruebas con dobles y servidor local, nunca contra el backend real
**Decisión:** la API de identidad se prueba con un doble para los flujos y con
un servidor local de prueba para la serialización y los códigos de estado. No
se depende del backend desplegado.
**Por qué:** el backend de desarrollo aún no está disponible (Q-01) y C-18 está
diferido. Los dobles permiten verificar hoy todo lo demás.
**Alternativa descartada:** esperar al backend para probar — bloquea la unidad
y mezcla fallas de contrato con fallas de implementación.

---

## 5. Dependencias a agregar

**Ninguna.** Esta unidad usa las dependencias ya declaradas por 001: cliente
HTTP, serialización, almacenamiento cifrado, inyección, navegación, componentes
visuales y el juego de pruebas. Si al implementar aparece una necesidad nueva,
se aplica la skill `agregar-dependencia` y se pide autorización.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El contrato real difiere del doble y la app falla al conectar | DTOs aislados en `data`, pruebas de contrato con servidor local y Q-01 antes de la primera tarea de red |
| RT-02 | La renovación se dispara dos veces o entra en bucle | D-04 con `Mutex`, un solo reintento y pruebas de concurrencia |
| RT-03 | El almacenamiento cifrado falla en un dispositivo | Degradación controlada: se pide login, nunca se guarda en texto claro |
| RT-04 | Parpadeo de navegación al restaurar la sesión | D-09 con estado de carga y decisión única |
| RT-05 | Un mensaje del backend revela si el correo existe | D-05 con errores cerrados y pruebas de comparación |
| RT-06 | El logout sin red deja la sesión viva en el servidor | D-08 con marca pendiente y Q-06 para la política definitiva |
| RT-07 | El temporizador del código se desincroniza del servidor | D-06: se muestra lo que devuelve el servidor, sin valores locales |
| RT-08 | El token de push queda huérfano tras el logout | D-10: borrado en la transición a "sin sesión" y prueba C-16 |
| RT-09 | 001 no está implementada y este plan asume sus contratos | Las tareas de 002 dependen de las de 001; si 001 cambia, se ajusta el plan antes de ejecutar |

---

## 7. Puntos de verificación

Entre fases, no se avanza sin esto:

| Fase | Verificación |
|---|---|
| F1 | Pruebas de dominio en verde; `grep` confirma que `domain/identity` no importa Android ni red |
| F2 | Pruebas de contrato con servidor local: serialización, códigos de estado y errores cerrados (C-06, C-17) |
| F3 | La sesión sobrevive a un reinicio simulado (C-07); diez peticiones simultáneas producen una sola renovación (C-09) |
| F4 | El repositorio se prueba con API y sesión falsas; el logout deja el almacén vacío (C-10) |
| F5 | Pruebas de interfaz de las cinco pantallas; estado de carga antes de 300 ms (C-14); guardas de navegación (C-01…C-06) |
| F6 | Recorrido completo con doble de backend: alta → verificación → login → reinicio → logout; push registrado y borrado (C-07…C-17). C-18 queda diferido |

---

## 8. Lo que este plan no incluye

- No implementa "probar demo": llega con 010 (mapa e inicio).
- No implementa la afiliación ni el modo negocio: 011.
- No implementa eliminar cuenta, el texto legal ni la edición de perfil: 017.
- No implementa la recepción, el contenido ni la segmentación de push: 006.
- No implementa la moderación de contenido: 004.
- No define la política de sincronización por entidad: 005.
- No agrega SMS, biometría ni proveedores externos de identidad.
- No toca el backend ni la infraestructura de Azure.

---

## 9. Puerta de aprobación

- [ ] Los componentes y su orden son aceptables.
- [ ] Las decisiones D-01 a D-11 están revisadas.
- [ ] Se confirma que no se agregan dependencias nuevas.
- [ ] Los riesgos técnicos y sus mitigaciones son suficientes.
- [ ] **Aprobado para pasar a `task.md`.**
