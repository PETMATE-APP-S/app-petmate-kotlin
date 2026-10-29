# Plan 001 — Arquitectura base de la app móvil

> Plan técnico de la unidad `001-arquitectura-base`
> Estado: **redactado, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) aprobada
> Alineado con: [`constitution.md`](../../constitution.md) v1.1.0
> Fecha: 27/10/2026

Este documento responde **cómo**. El **qué** y el **por qué** están en la spec.

---

## 1. Visión de la arquitectura

```
┌─────────────────────────────────────────────────────────┐
│  ui                                                     │
│  Screen (Composable sin estado)                         │
│      ↕ UiState / eventos                                │
│  ViewModel ── inyecta casos de uso ─────────────┐       │
│  navigation (un solo NavHost, rutas tipadas)    │       │
│  theme · components                             │       │
└─────────────────────────────────────────────────┼───────┘
                                                  │
┌─────────────────────────────────────────────────▼───────┐
│  domain            (Kotlin puro, sin framework)         │
│  model/        modelos de negocio                       │
│  repository/   INTERFACES de repositorio                │
│  usecase/      casos de uso                             │
└─────────────────────────────────────────────────▲───────┘
                                                  │ implementa
┌─────────────────────────────────────────────────┴───────┐
│  data                                                   │
│  local/       Room: entidades, DAOs, PetMateDatabase    │
│  remote/      Retrofit: servicios, DTOs, interceptores  │
│  mapper/      DTO ↔ Entity ↔ modelo de dominio          │
│  repository/  implementaciones (red → Room → UI)        │
│  session/     credenciales cifradas                     │
│  cache/       política de expiración                    │
│  sync/        outbox y reconciliación offline           │
│  moderation/  punto de intercepción de contenido        │
│  push/        token FCM y recepción                     │
└─────────────────────────────────────────────────────────┘

core/  Result, AppError, UiText, Logger, Dispatchers, Clock
       permissions/   PermissionController
       connectivity/  ConnectivityObserver
di/    módulos de Hilt
```

**La regla que gobierna todo:** las flechas apuntan hacia `domain`. `domain`
no conoce a nadie.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| K-01 | Matriz de versionamiento | documento | Define `versionCode`/`versionName` por entrega |
| K-02 | Catálogo de versiones | build | Única fuente de versiones de dependencias |
| K-03 | Estructura de paquetes | todas | Esqueleto de carpetas de las tres capas |
| K-04 | `Result` / `AppError` | core | Modelo único de éxito y falla |
| K-05 | `UiText` | core | Texto para la UI como recurso, no como `String` |
| K-06 | `Logger` | core | Registro de eventos y errores, sin datos sensibles |
| K-07 | `DispatcherProvider` | core | Dispatchers inyectables (hacen probables las corrutinas) |
| K-08 | `PetMateApplication` + Hilt | di | Contenedor de dependencias de la app |
| K-09 | Módulos de Hilt | di | Provisión de base de datos, red, sesión, dispatchers |
| K-10 | `PetMateDatabase` | data/local | Base Room, fuente de verdad de la UI |
| K-11 | Cliente HTTP | data/remote | Retrofit + OkHttp + serialización + tiempos de espera |
| K-12 | `AuthInterceptor` | data/remote | Agrega el token de acceso a cada petición |
| K-13 | `TokenAuthenticator` | data/remote | Renueva el token y reintenta una vez |
| K-14 | `ErrorMapper` | data/remote | Traduce fallas HTTP y de red a `AppError` |
| K-15 | `SessionStore` | data/session | Guarda credenciales cifradas |
| K-16 | `SessionRepository` | data + domain | Estado de la sesión, renovación y cierre |
| K-17 | Política de caché | data/cache | Expiración por tipo de dato (memoria + disco) |
| K-18 | `NavHost` y rutas | ui/navigation | Grafo único de navegación |
| K-19 | Tema | ui/theme | Colores, tipografía, claro/oscuro |
| K-20 | Componentes base | ui/components | Botón, campo, estado vacío, estado de error, cargando |
| K-21 | Infraestructura de pruebas | test | Dobles, reglas, dispatcher de prueba, utilidades |
| K-22 | Puerta de calidad | build | ktlint + detekt |
| K-23 | Pipeline de CI/CD | `.github/workflows` | Compilar, probar, seguridad, artefacto |
| K-24 | Configuración por entorno | build | `debug` / `release`, URL base, endurecimiento |
| K-25 | Pantalla de verificación | ui | Demostración desechable de extremo a extremo |
| K-26 | Resiliencia de red | data/remote | Timeouts, reintentos con backoff e idempotencia |
| K-27 | `PermissionController` | core/permissions | Pedir en uso, explicar antes y degradar si se niega |
| K-28 | `ConnectivityObserver` | core/connectivity | `Flow<Boolean>` de conectividad para el outbox y la UI |
| K-29 | `Outbox` / `SyncQueue` | data/sync | Cola persistente de escrituras y reconciliación al reconectar |
| K-30 | `ContentModerationGateway` | domain + data/moderation | Punto único de intercepción; implementación no-op en esta unidad |
| K-31 | `PushRegistrar` + servicio FCM | data/push | Token tras login, borrado en logout y recepción desacoplada |
| K-32 | `PlayPublisher` | `.github/workflows` | Firma con secretos y subida al track interno/cerrado |

---

## 3. Dependencias y orden de construcción

```
K-01 matriz ──┐
K-02 catálogo ┴──→ K-03 paquetes ──→ K-04 Result ──┬──→ K-05 UiText
                                                    ├──→ K-06 Logger
                                                    └──→ K-07 Dispatchers
                                                              │
                   K-08 Hilt ←───────────────────────────────┘
                        │
            ┌───────────┼───────────────┐
            ▼           ▼               ▼
      K-10 Room    K-11 HTTP      K-19 Tema
            │           │               │
            │     K-12/13/14 auth       │
            │           │               ▼
            │     K-15 SessionStore  K-20 Componentes
            │           │               │
            │     K-16 SessionRepo      ▼
            │           │          K-18 Navegación
            └─────┬─────┘               │
                  ▼                     │
            K-17 Caché                  │
                  │                     │
                  └──────┬──────────────┘
                         ▼
                   K-25 Pantalla de verificación
                         │
      K-21 Pruebas ──────┤  (acompaña desde K-04, no al final)
      K-22 Calidad ──────┤
      K-24 Entornos ─────┤
                         ▼
                   K-23 Pipeline CI/CD
```

### Fases

| Fase | Componentes | Puede ir en paralelo |
|---|---|---|
| **F1 — Cimientos** | K-01, K-02, K-03, K-22, K-24 | K-01 con K-02 |
| **F2 — Núcleo** | K-04, K-05, K-06, K-07, K-08 | K-05/K-06/K-07 entre sí |
| **F3 — Datos** | K-10, K-11, K-14 | K-10 con K-11 |
| **F4 — Sesión** | K-12, K-13, K-15, K-16 | — (secuencial, K-13 depende de K-16) |
| **F5 — Caché** | K-17 | — |
| **F6 — Presentación** | K-19, K-20, K-18 | K-19 con K-18 |
| **F7 — Verificación** | K-25, K-21 | — |
| **F8 — Entrega** | K-23 | — |
| **F9 — Plataforma y publicación** | K-26, K-27, K-28, K-29, K-30, K-31, K-32 | K-27 con K-28; K-30 tras K-04 |

Las pruebas (K-21) **no son una fase final**: cada componente desde F2 llega
con sus pruebas. K-21 solo agrupa la infraestructura compartida.

Los componentes K-26 a K-32 tampoco son una fase final: K-27 y K-28 deben
existir antes de la primera pantalla que pida un permiso, K-29 antes de la
primera escritura offline y K-30 antes de la primera subida de archivo. La
fase F9 solo los agrupa para el cierre de la unidad.

---

## 4. Decisiones técnicas

### D-01 — Un solo módulo Gradle, paquetes por capa
**Decisión:** `:app` como módulo único, con las capas separadas por paquete.
**Por qué:** el equipo es de cuatro personas con plazo al 02/12/2026. La
multimodularidad obliga la separación por compilación, pero cuesta
configuración y tiempos de build que no tenemos.
**Alternativas descartadas:**
- Módulos `:core`, `:domain`, `:data`, `:ui` — la frontera la garantizaría el
  compilador en lugar de la revisión. Descartada por costo de tiempo.
- Módulo por feature — sobra para el tamaño del proyecto.
**Mitigación del riesgo:** la skill `revisar-capas` verifica la dirección de
dependencias con `grep` en cada tarea y en el pipeline. Es más débil que el
compilador, pero es automática.

### D-02 — Room como fuente de verdad única
**Decisión:** los DAOs exponen `Flow`; los repositorios escriben en Room y la
UI observa Room. La red **nunca** llega directo a la UI.
**Por qué:** cumple offline-first (R-01, R-02) de la forma más simple: con un
solo camino de datos. Un `Flow` de Room notifica sola a la UI al escribir.
**Alternativa descartada:** devolver la respuesta de red a la UI y guardar en
paralelo — produce dos fuentes de verdad y estados inconsistentes.

### D-03 — `Result` propio, no excepciones
**Decisión:** `sealed interface AppResult<out T> { Success, Failure(AppError) }`
con `AppError` como jerarquía sellada (red, servidor, autenticación,
validación, desconocido).
**Por qué:** hace imposible olvidar manejar un error: el compilador exige el
`when` exhaustivo. Las excepciones cruzando capas son invisibles.
**Alternativa descartada:** `kotlin.Result` — no admite un tipo de error
propio ni es exhaustivo en `when`.
**Regla:** las excepciones se atrapan en `data` y **no salen** de ahí.

### D-04 — `UiText` en lugar de `String`
**Decisión:** los mensajes viajan como `UiText.Resource(id, args)` o
`UiText.Dynamic(value)`, y se resuelven en el Composable.
**Por qué:** el `ViewModel` no debe tocar `Context` (lo haría imposible de
probar) y los textos deben vivir en `strings.xml`.

### D-05 — Dispatchers inyectados
**Decisión:** interfaz `DispatcherProvider` con `io`, `default`, `main`,
provista por Hilt; en pruebas se sustituye por el dispatcher de prueba.
**Por qué:** escribir `Dispatchers.IO` dentro de una clase la vuelve no
determinista en pruebas. Es el error más común y el más caro de revertir.

### D-06 — Renovación de token con `Authenticator`, no con interceptor
**Decisión:** `AuthInterceptor` solo agrega la cabecera. La renovación va en un
`okhttp3.Authenticator`, que OkHttp invoca **solo** ante un 401, con un
`Mutex` para serializar y un tope de un reintento por petición.
**Por qué:** `Authenticator` es el punto que OkHttp diseñó para esto y evita el
ciclo infinito. El `Mutex` resuelve G-04: diez peticiones en paralelo que
reciben 401 provocan **una** renovación, no diez.
**Alternativa descartada:** renovar dentro de un interceptor — reentra y puede
ciclar.

### D-07 — Credenciales en `EncryptedSharedPreferences`; preferencias en DataStore
**Decisión:** tokens en `EncryptedSharedPreferences` (respaldada por Keystore);
preferencias no sensibles en DataStore.
**Por qué:** cumple R-06 sin criptografía propia. DataStore no cifra por sí
mismo.
**Riesgo (G-08):** la inicialización falla en algunos dispositivos; se atrapa,
se registra y se fuerza reautenticación en lugar de dejar la app inservible.

### D-08 — Caché con marca de tiempo en la fila
**Decisión:** cada tabla cacheable lleva `updatedAt`; el repositorio compara
contra una duración por tipo de dato y decide si pide a la red. Más una caché
en memoria de vida corta para lo que se lee muchas veces en una sesión.
**Por qué:** es el nivel de frontend de los tres niveles exigidos (P8), y se
prueba sin dispositivo inyectando un `Clock`.
**Pendiente:** las duraciones concretas son Q-06; la arquitectura entrega
valores por omisión documentados y configurables.

### D-09 — Navegación con rutas tipadas y un solo `NavHost`
**Decisión:** rutas como objetos/`data class` serializables (Navigation Compose
con tipos), un `NavHost` en `MainActivity`, y la navegación se propaga como
lambdas hacia arriba.
**Por qué:** las rutas como cadenas concatenadas fallan en tiempo de
ejecución; las tipadas fallan al compilar. Un Composable que recibe el
`NavController` se vuelve imposible de previsualizar y de probar.

### D-10 — Un entorno por tipo de build
**Decisión:** `buildConfigField` con la URL base: `debug` apunta al backend de
desarrollo, `release` al de producción. Las URLs salen de `local.properties` o
de secretos de CI, nunca del código.
**Por qué:** cumple R-19 y R-25 sin `buildTypes` adicionales que compliquen el
pipeline.

### D-11 — `release` endurecido
**Decisión:** `isMinifyEnabled = true`, `isShrinkResources = true`,
`isDebuggable = false`, reglas de ProGuard para los modelos serializados, y el
interceptor de registro solo en `debug`.
**Por qué:** R-22. Nótese que el andamio actual trae `optimization { enable =
false }` en `release`: **hay que corregirlo** (tarea T-002).

### D-12 — Pipeline en dos flujos
**Decisión:**
- `pr.yml` en cada propuesta de cambio: `ktlintCheck` → `detekt` → `test` →
  `assembleDebug` → análisis de seguridad (CodeQL, revisión de dependencias,
  búsqueda de secretos).
- `release.yml` al integrar a `main`: `bundleRelease` firmado con la versión de
  la matriz, y el artefacto queda guardado.
**Por qué:** R-21 y P7. La subida al track interno se hace en T-040 (D-20); el
rollout a producción queda fuera de esta unidad.
**Caché de Gradle** activada para que el pipeline no tarde de más.

### D-13 — Pruebas: JVM primero, dispositivo solo cuando es inevitable
**Decisión:** dominio, mappers, repositorios y ViewModels en `test/` (JVM, con
MockK, Turbine y `kotlinx-coroutines-test`). Solo DAOs de Room, Compose y
navegación en `androidTest/`.
**Por qué:** las pruebas de JVM corren en segundos y en CI sin emulador; las
instrumentadas necesitan emulador y son frágiles. Se reservan para lo que de
verdad requiere Android.
**Para la red:** `MockWebServer`, que prueba el cliente real contra respuestas
controladas (C-06, C-08).

### D-14 — La pantalla de verificación es desechable y se marca como tal
**Decisión:** una sola pantalla con un `TODO: eliminar al llegar la primera
feature`, registrada como deuda en `memoria.md`.
**Por qué:** C-21 exige demostrar el camino completo, pero una pantalla de
andamio que se queda se convierte en código muerto que nadie se atreve a
borrar.

### D-15 — Red resiliente contra Azure Container Apps
**Decisión:** URL base por `buildType` desde `local.properties` o secretos,
TLS obligatorio, **sin certificate pinning** (el balanceador rota
certificados), health check al arrancar y reintentos con backoff exponencial
solo para timeout, 5xx y corte de red. Los 4xx no se reintentan. Cada
operación de escritura lleva clave de idempotencia.
**Por qué:** el backend vive detrás del balanceador de Azure; un pinning rígido
convierte una rotación de certificado en una caída total. Reintentar un 4xx
multiplica errores del cliente y puede duplicar escrituras.
**Alternativa descartada:** interceptor ciego que reintenta todo — duplica
efectos y castiga al servidor.

### D-16 — Permisos en el momento de uso
**Decisión:** `PermissionController` en `core/permissions` encapsula el ciclo
explicar → pedir → resultado, con la justificación como `UiText`. Cada feature
decide qué hacer si se niega; la app nunca bloquea el arranque por un permiso.
**Por qué:** cumple P10 y R-28 sin repartir `ActivityResultContracts` por toda
la UI. Un permiso pedido al arrancar se deniega más y no se puede explicar.
**Alternativa descartada:** pedir todos los permisos en `MainActivity` — más
simple de escribir, peor aceptación y sin contexto de uso.

### D-17 — Escrituras offline con outbox
**Decisión:** toda escritura que no puede ir a la red se guarda en una tabla
`outbox` de Room con estado, clave de idempotencia y reintentos. Un
`WorkManager` con restricción de red la vacía al reconectar; la reconciliación
es por operación, no por entidad completa.
**Por qué:** P4 exige que la app funcione sin conexión, pero la unidad anterior
solo resolvía lectura. Sin outbox, una mascota creada en modo avión se pierde.
**Alternativa descartada:** reintentar en memoria mientras la app vive — se
pierde al cerrar la app y no sobrevive a la muerte del proceso.

### D-18 — Punto único de moderación
**Decisión:** interfaz `ContentModerationGateway` en `domain` con un método por
tipo de contenido (`text`, `image`, `video`) que devuelve `AppResult<Verdict>`.
La implementación de esta unidad es un **no-op explícito**; la real llega en la
spec 004. Toda subida pasa por `ModeratedContent` antes de persistir o enviar.
**Por qué:** P13 exige moderar antes de almacenar y el punto debe ser único;
si cada feature llama a la API por su cuenta, la política se dispersa.
**Alternativa descartada:** moderar dentro de cada repositorio — tres
implementaciones y tres criterios distintos.

### D-19 — Base de notificaciones push
**Decisión:** `PushRegistrar` obtiene el token FCM tras el login y lo envía al
backend; en logout lo borra y llama a `FirebaseMessaging.deleteToken()`. El
`FirebaseMessagingService` solo traduce el mensaje a un evento de dominio y
navega por deep link; **no** decide contenido ni segmentación (eso es spec 006).
**Por qué:** el token sin sesión no sirve y un token huérfano tras logout es un
riesgo de privacidad (P9).
**Alternativa descartada:** registrar el token en `Application.onCreate` — se
registra antes de saber quién es el usuario.

### D-20 — Publicación en Play desde CI
**Decisión:** `release.yml` firma con secretos (`KEYSTORE_BASE64`,
`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`), toma `versionCode` y
`versionName` de la matriz (K-01) y sube el AAB al **track interno**, que se
promueve a **prueba cerrada** tras la verificación. El rollout a producción no
se automatiza en esta unidad.
**Por qué:** cumple R-24 y el requisito de despliegue desde GitHub, y el track
interno permite detectar un AAB roto sin exponerlo.
**Alternativa descartada:** subir directo a producción — un error de firma o de
versión llega a los evaluadores.
**Nota:** la acción de subida es una dependencia de CI, no de la app; se
autoriza como cualquier otra (constitución §4).

---

## 5. Dependencias a agregar

Las dependencias de la app requieren autorización (constitución §4) y se
declaran en `gradle/libs.versions.toml`; la acción de publicación es una
dependencia de CI y se autoriza igual. Se sigue la skill `agregar-dependencia`.

| Para | Artefactos |
|---|---|
| Inyección de dependencias | `hilt-android`, `hilt-compiler` (KSP), `hilt-navigation-compose` |
| Persistencia | `room-runtime`, `room-ktx`, `room-compiler` (KSP) |
| Red | `retrofit`, `retrofit-kotlinx-serialization`, `okhttp`, `okhttp-logging-interceptor`, `kotlinx-serialization-json` |
| Sesión | `security-crypto`, `datastore-preferences` |
| Navegación | `navigation-compose` |
| Ciclo de vida | `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose` |
| Offline | `work-runtime-ktx` (outbox y reconciliación) |
| Push | `firebase-bom`, `firebase-messaging`, plugin `google-services` |
| Pruebas | `mockk`, `turbine`, `kotlinx-coroutines-test`, `mockwebserver`, `room-testing`, `navigation-testing` |
| Calidad | plugins de `ktlint` y `detekt` |
| Publicación | acción de subida a Google Play en GitHub Actions (no es dependencia de la app) |

**Fuera de esta unidad:** `coil-compose` se declara en la spec 007, que es la
primera que carga imágenes; en 001 no tiene consumidor y no se agrega.

**Primero se verifica la compatibilidad** del catálogo actual (AGP 9.2.1,
Kotlin 2.2.10, Compose BOM 2026.02.01) con Hilt, Room y KSP (riesgo G-05). Si
hay incompatibilidad, se fijan versiones estables compatibles y se registra la
decisión en `memoria.md`.

---

## 6. Riesgos técnicos y mitigación

| # | Riesgo | Mitigación en el plan |
|---|---|---|
| G-04 | Renovaciones de token duplicadas o en ciclo | D-06: `Authenticator` + `Mutex` + un solo reintento; prueba con `MockWebServer` |
| G-05 | Incompatibilidad entre AGP/Kotlin/KSP/Hilt | Tarea T-003 dedicada a verificar antes de seguir; fijar versiones estables |
| G-02 | Sobre-ingeniería | Toda pieza del plan traza a un criterio de éxito de la spec. Lo que no traza, se elimina |
| G-08 | Falla del cifrado de credenciales | D-07: degradación controlada y registrada, no caída |
| — | La frontera entre capas no la impone el compilador (consecuencia de D-01) | `revisar-capas` en cada tarea y como paso del pipeline |
| — | Tiempos de build largos con KSP | Caché de Gradle y configuración paralela; se mide en T-003 |
| G-09 | Rotación de certificados del balanceador de Azure | D-15: TLS sin pinning y health check |
| G-10 | Falla la subida a Play o la revisión | D-20: track interno, service account y checklist previo |
| G-11 | El outbox duplica escrituras | Clave de idempotencia por operación y prueba de reconexión (C-28) |
| G-12 | Firebase mal configurado impide arrancar | Arranque tolerante: sin push la app funciona (T-039) |

---

## 7. Puntos de verificación

Entre fases, no se avanza sin esto:

| Después de | Se verifica |
|---|---|
| F1 | La matriz existe; `ktlintCheck` y `detekt` corren; `assembleDebug` compila |
| F2 | La app arranca con Hilt; `AppResult` y `AppError` tienen pruebas; `DispatcherProvider` se sustituye en pruebas |
| F3 | Prueba de Room en memoria en verde; prueba del cliente HTTP con `MockWebServer` en verde |
| F4 | Pruebas de renovación (C-08) y de cierre de sesión (C-09) en verde; inspección del almacenamiento cifrado (C-10) |
| F5 | Prueba de la política de caché con `Clock` inyectado (C-11) en verde |
| F6 | Prueba de navegación (C-13) en verde; revisión visual de claro y oscuro (C-14) |
| F7 | Recorrido manual de extremo a extremo (C-21); modo avión (C-12); coberturas (C-22) |
| F8 | Pipeline en verde (C-16); el pipeline falla con una prueba rota a propósito (C-17); revisión del agente `seguridad` (C-19) |
| F9 | AAB en el track interno (C-23); HTTPS contra Azure (C-24); moderación rechaza (C-25); permiso denegado no rompe (C-26, C-27); outbox sin duplicados (C-28); token push limpio en logout (C-29); backoff correcto (C-30) |

---

## 8. Lo que este plan NO hace

- No define ninguna entidad, tabla ni endpoint de negocio de PetMate.
- No decide el proveedor de mapas, el modelo de ML ni el de IA generativa.
- No decide el contenido, la segmentación ni los textos de las notificaciones
  push; solo deja el token, el registro y la recepción (D-19).
- No implementa la moderación; solo su punto de intercepción (D-18).
- No define la política de permisos por pantalla; solo el mecanismo (D-16).
- No resuelve conflictos de negocio al sincronizar; solo el outbox genérico
  (D-17).
- No automatiza el rollout a producción de Google Play; llega hasta prueba
  cerrada (D-20).
- No toca el backend, la nube ni la landing.

---

## 9. Puerta de aprobación

- [ ] Las decisiones D-01 a D-20 están revisadas.
- [ ] El orden de fases es aceptable.
- [ ] La lista de dependencias está autorizada (o se autoriza por tarea).
- [ ] **Aprobado para pasar a `task.md`.**

Siguiente: [`task.md`](task.md)
