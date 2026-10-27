# Tareas 001 — Arquitectura base de la app móvil

> Lista de tareas de la unidad `001-arquitectura-base`
> Estado: **redactada, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) y [`plan.md`](plan.md) aprobados
> Fecha: 27/10/2026

---

## ⛔ NO EJECUTAR

**Ninguna tarea de este archivo se ejecuta todavía.** Instrucción explícita del
profesor: los archivos SDD se entregan redactados y las tareas se ejecutan
después. Un agente que implemente estas tareas sin autorización viola la
constitución §4 ("Nunca: ejecutar tareas de `task.md` sin aprobación explícita
del humano").

Progreso: **0 / 32 tareas** · Fases completadas: **0 / 8**

---

## Cómo leer una tarea

```
- [ ] T-0NN — descripción
  - Fase · Depende de · Criterio de aceptación · Verificación · Archivos
```

Reglas: se ejecutan **en orden de dependencia**, una a la vez; una tarea toca
a lo más ~5 archivos; se marca `[x]` **solo** cuando su verificación pasó de
verdad; al cerrarla se actualiza `memoria.md`; después de cada tarea el agente
**se detiene y reporta**.

Puerta de calidad de toda tarea que toque código:
```bash
./gradlew ktlintCheck detekt test
```

---

## Fase F1 — Cimientos

- [ ] **T-001** — Escribir la matriz de versionamiento
  - Depende de: — · Bloqueada por: **Q-07**
  - Aceptación: existe `docs/matriz-versionamiento.md` con el esquema de
    `versionName` (semántico) y `versionCode` (regla de cálculo), la tabla de
    entregas planeadas y la regla de quién la actualiza y cuándo.
  - Verificación: revisión del documento por el responsable del proyecto.
  - Archivos: `docs/matriz-versionamiento.md`
  - Nota: resuelve la deuda D-04 de `memoria.md`. Los lineamientos exigen la
    matriz **antes** de desarrollar, por eso es la tarea 1.

- [ ] **T-002** — Corregir la configuración base del módulo `app`
  - Depende de: T-001 · Bloqueada por: **Q-01, Q-05**
  - Aceptación: `minSdk` confirmado; `release` con `isMinifyEnabled = true`,
    `isShrinkResources = true`, `isDebuggable = false` y reglas de ProGuard
    (se elimina el `optimization { enable = false }` actual); `buildConfig`
    habilitado; `applicationId` decidido.
  - Verificación: `./gradlew assembleDebug bundleRelease` compila.
  - Archivos: `app/build.gradle.kts`, `app/proguard-rules.pro`

- [ ] **T-003** — Verificar la compatibilidad del catálogo y agregar KSP
  - Depende de: T-002
  - Aceptación: queda comprobado que AGP, Kotlin, Compose BOM, KSP, Hilt y
    Room son compatibles entre sí; el plugin de KSP está declarado; las
    versiones elegidas quedan registradas en `memoria.md` con su justificación.
  - Verificación: `./gradlew build` compila sin advertencias de versión.
  - Archivos: `gradle/libs.versions.toml`, `build.gradle.kts`,
    `app/build.gradle.kts`, `memoria.md`
  - Nota: mitiga el riesgo G-05. Si hay incompatibilidad, **se detiene y se
    reporta** antes de seguir.

- [ ] **T-004** — Configurar la puerta de calidad (ktlint + detekt)
  - Depende de: T-003
  - Aceptación: `ktlintCheck` y `detekt` corren sobre todo el código y pasan
    en cero advertencias, con su configuración versionada.
  - Verificación: `./gradlew ktlintCheck detekt`
  - Archivos: `build.gradle.kts`, `app/build.gradle.kts`,
    `config/detekt/detekt.yml`, `gradle/libs.versions.toml`

- [ ] **T-005** — Configurar la URL base por entorno y revisar `.gitignore`
  - Depende de: T-002 · Bloqueada por: **Q-02**
  - Aceptación: la URL base llega por `buildConfigField` y se lee de
    `local.properties` o de una variable de entorno, con un valor por omisión
    para desarrollo; `local.properties`, `*.jks`, `*.keystore` y
    `google-services.json` están en `.gitignore`; no hay ninguna URL ni
    secreto escrito en el código.
  - Verificación: `./gradlew assembleDebug` compila y `BuildConfig.BASE_URL`
    tiene el valor esperado; `git status` no lista archivos sensibles.
  - Archivos: `app/build.gradle.kts`, `.gitignore`, `local.properties.example`

- [ ] **T-006** — Crear el esqueleto de paquetes de las tres capas
  - Depende de: T-003
  - Aceptación: existen los paquetes `core/`, `di/`, `domain/{model,repository,usecase}`,
    `data/{local,remote,mapper,repository,session,cache}`,
    `ui/{navigation,theme,components}`, cada uno con un `README.md` de una
    línea que dice qué va ahí y qué no.
  - Verificación: la estructura coincide con `constitution.md` §3.
  - Archivos: árbol bajo `app/src/main/java/com/example/petmateedel/`

> **Punto de verificación F1:** la matriz existe · el estilo y el análisis
> estático corren · `assembleDebug` y `bundleRelease` compilan.

---

## Fase F2 — Núcleo

- [ ] **T-007** — Modelo de resultado y de error (`AppResult`, `AppError`)
  - Depende de: T-006
  - Aceptación: `AppResult<T>` como interfaz sellada con `Success` y
    `Failure`; `AppError` como jerarquía sellada que cubre red, tiempo de
    espera, servidor, autenticación, validación y desconocido; funciones de
    extensión (`map`, `onSuccess`, `onFailure`); todo en Kotlin puro.
  - Verificación: `./gradlew test` con las pruebas de las extensiones en verde.
  - Archivos: `core/result/AppResult.kt`, `core/result/AppError.kt`,
    `test/.../core/result/AppResultTest.kt`

- [ ] **T-008** — `UiText`
  - Depende de: T-007
  - Aceptación: `UiText` sellada con `Resource(id, args)` y `Dynamic(value)`,
    y un `asString()` que se resuelve en el Composable; ningún `ViewModel`
    necesita `Context` para producir un mensaje.
  - Verificación: `./gradlew test`
  - Archivos: `core/ui/UiText.kt`, `test/.../core/ui/UiTextTest.kt`

- [ ] **T-009** — `Logger` y `DispatcherProvider`
  - Depende de: T-007
  - Aceptación: interfaz `Logger` con implementación que registra en `debug` y
    queda silenciosa o remota en `release`, y que **nunca** registra tokens ni
    datos personales; interfaz `DispatcherProvider` con `io`, `default`, `main`
    y una implementación de prueba.
  - Verificación: `./gradlew test`; `grep -rn "println(" app/src/main/` no
    devuelve resultados.
  - Archivos: `core/log/Logger.kt`, `core/coroutines/DispatcherProvider.kt`,
    `test/.../core/`

- [ ] **T-010** — Activar Hilt en la aplicación
  - Depende de: T-003, T-009
  - Aceptación: `PetMateApplication` con `@HiltAndroidApp`, declarada en el
    manifiesto; `MainActivity` con `@AndroidEntryPoint`; un módulo
    `CoreModule` que provee `Logger` y `DispatcherProvider`; la app arranca.
  - Verificación: `./gradlew assembleDebug` compila y la app abre en un
    dispositivo o emulador.
  - Archivos: `PetMateApplication.kt`, `MainActivity.kt`,
    `di/CoreModule.kt`, `app/src/main/AndroidManifest.xml`

> **Punto de verificación F2:** la app arranca con Hilt · `AppResult` y
> `AppError` probados · el dispatcher se sustituye en pruebas.

---

## Fase F3 — Datos

- [ ] **T-011** — Base de datos Room
  - Depende de: T-010
  - Aceptación: `PetMateDatabase` con una entidad de prueba desechable, su DAO
    exponiendo `Flow`, conversores de tipo para fechas, y provista por Hilt en
    `DatabaseModule`; la base se crea al abrir la app.
  - Verificación: `./gradlew connectedAndroidTest` con una prueba de base en
    memoria que escribe, lee y observa cambios (C-05).
  - Archivos: `data/local/PetMateDatabase.kt`, `data/local/dao/`,
    `data/local/converter/`, `di/DatabaseModule.kt`,
    `androidTest/.../data/local/PetMateDatabaseTest.kt`
  - Nota: cambiar el esquema de Room requiere autorización (constitución §4).

- [ ] **T-012** — Cliente HTTP
  - Depende de: T-010, T-005
  - Aceptación: Retrofit con OkHttp y `kotlinx.serialization`; tiempos de
    espera explícitos de conexión, lectura y escritura; interceptor de
    registro **solo** en `debug`; `cleartextTrafficPermitted` en `false`;
    provisto por Hilt en `NetworkModule`.
  - Verificación: `./gradlew test` con una prueba de `MockWebServer` que
    consulta el endpoint de salud y deserializa la respuesta (C-06).
  - Archivos: `data/remote/ApiService.kt`, `di/NetworkModule.kt`,
    `app/src/main/res/xml/network_security_config.xml`,
    `test/.../data/remote/ApiServiceTest.kt`

- [ ] **T-013** — `ErrorMapper`
  - Depende de: T-012 · Bloqueada por: **Q-03**
  - Aceptación: toda falla HTTP (4xx, 5xx), de conexión, de tiempo de espera y
    de deserialización se traduce a un `AppError` concreto; ninguna excepción
    sale de la capa `data`.
  - Verificación: `./gradlew test` con pruebas por cada tipo de falla.
  - Archivos: `data/remote/ErrorMapper.kt`,
    `test/.../data/remote/ErrorMapperTest.kt`

> **Punto de verificación F3:** Room en memoria en verde · cliente HTTP contra
> `MockWebServer` en verde.

---

## Fase F4 — Sesión

- [ ] **T-014** — `SessionStore` cifrado
  - Depende de: T-010
  - Aceptación: los tokens se guardan en `EncryptedSharedPreferences`; existen
    operaciones de guardar, leer y borrar; si la inicialización del cifrado
    falla, se registra y se fuerza reautenticación en lugar de caer (G-08).
  - Verificación: `./gradlew connectedAndroidTest`; inspección manual del
    almacenamiento confirma que no hay texto claro (C-10).
  - Archivos: `data/session/SessionStore.kt`, `di/SessionModule.kt`,
    `androidTest/.../data/session/SessionStoreTest.kt`

- [ ] **T-015** — Interfaz y repositorio de sesión
  - Depende de: T-014, T-013
  - Aceptación: `SessionRepository` declarada en `domain/repository/` y
    `SessionRepositoryImpl` en `data/repository/`; expone el estado de sesión
    como `Flow`; el cierre de sesión invalida en el servidor y **borra todo
    dato local**.
  - Verificación: `./gradlew test` con una prueba que confirma el
    almacenamiento vacío tras cerrar sesión (C-09).
  - Archivos: `domain/repository/SessionRepository.kt`,
    `data/repository/SessionRepositoryImpl.kt`,
    `test/.../data/repository/SessionRepositoryImplTest.kt`

- [ ] **T-016** — `AuthInterceptor`
  - Depende de: T-015
  - Aceptación: agrega la cabecera `Authorization` cuando hay token y la omite
    en las rutas públicas; **no** intenta renovar nada.
  - Verificación: `./gradlew test` con pruebas de `MockWebServer` que revisan
    la cabecera enviada.
  - Archivos: `data/remote/interceptor/AuthInterceptor.kt`,
    `test/.../data/remote/interceptor/AuthInterceptorTest.kt`

- [ ] **T-017** — `TokenAuthenticator` (renovación automática)
  - Depende de: T-016 · Bloqueada por: **Q-04**
  - Aceptación: ante un 401 renueva el token y reintenta la petición **una
    sola vez**; la renovación está serializada con `Mutex`, de modo que varias
    peticiones en paralelo con 401 producen **una** renovación; si la
    renovación falla, cierra la sesión; no existe posibilidad de ciclo
    infinito.
  - Verificación: `./gradlew test` con pruebas de `MockWebServer` para: 401
    seguido de renovación exitosa y reintento (C-08); 401 con renovación
    fallida que cierra sesión; varias peticiones concurrentes con 401 que
    provocan una sola renovación.
  - Archivos: `data/remote/interceptor/TokenAuthenticator.kt`,
    `di/NetworkModule.kt`,
    `test/.../data/remote/interceptor/TokenAuthenticatorTest.kt`
  - Nota: mitiga el riesgo G-04, el más grave del plan. No se cierra sin la
    prueba de concurrencia.

> **Punto de verificación F4:** renovación (C-08) y cierre de sesión (C-09) en
> verde · almacenamiento cifrado confirmado (C-10).

---

## Fase F5 — Caché

- [ ] **T-018** — Política de caché y `Clock` inyectable
  - Depende de: T-011 · Bloqueada por: **Q-06**
  - Aceptación: existe un `CachePolicy` con la duración de vigencia por tipo
    de dato y un `Clock` inyectable; una función decide si un dato vigente se
    sirve de la caché o se pide a la red.
  - Verificación: `./gradlew test` con pruebas que adelantan el `Clock` y
    confirman el cambio de decisión (C-11).
  - Archivos: `data/cache/CachePolicy.kt`, `core/time/Clock.kt`,
    `test/.../data/cache/CachePolicyTest.kt`

- [ ] **T-019** — Patrón de repositorio offline-first
  - Depende de: T-018, T-015
  - Aceptación: existe un repositorio de referencia, documentado, que
    implementa el camino red → Room → UI: la UI observa Room, la red refresca
    Room según la política de caché, y los errores de red no borran los datos
    locales.
  - Verificación: `./gradlew test` con pruebas de los casos: sin datos
    locales y red exitosa; con datos locales vigentes; con datos locales
    vencidos; con datos locales y red fallida (los datos se conservan y se
    reporta el error).
  - Archivos: `data/repository/` (repositorio de referencia),
    `domain/repository/`, `test/.../data/repository/`
  - Nota: este patrón es la plantilla que copiarán todas las features. Debe
    quedar bien documentado.

> **Punto de verificación F5:** la política de caché se prueba con el `Clock`
> inyectado (C-11).

---

## Fase F6 — Presentación

- [ ] **T-020** — Tema visual
  - Depende de: T-010
  - Aceptación: paleta de PetMate, tipografía y formas en Material 3, con modo
    claro y oscuro; el color dinámico queda decidido de forma explícita (a
    favor o en contra, con justificación).
  - Verificación: `@Preview` en claro y oscuro; revisión visual (C-14).
  - Archivos: `ui/theme/Color.kt`, `ui/theme/Theme.kt`, `ui/theme/Type.kt`

- [ ] **T-021** — Componentes base reutilizables
  - Depende de: T-020, T-008
  - Aceptación: existen los componentes de estado de carga, estado vacío,
    estado de error con reintento, botón primario y campo de texto; todos sin
    estado propio, con `@Preview`, `contentDescription` donde aplica y área
    táctil ≥ 48 dp.
  - Verificación: `./gradlew connectedAndroidTest` con una prueba de Compose
    del componente de error y su reintento.
  - Archivos: `ui/components/`, `androidTest/.../ui/components/`

- [ ] **T-022** — Navegación con rutas tipadas
  - Depende de: T-021
  - Aceptación: un solo `NavHost`; rutas tipadas (sin cadenas concatenadas);
    ningún Composable recibe el `NavController`, la navegación sube como
    lambda; el estado se conserva al rotar y al volver atrás.
  - Verificación: `./gradlew connectedAndroidTest` con una prueba de
    navegación entre dos destinos que sobrevive a un cambio de configuración
    (C-13).
  - Archivos: `ui/navigation/PetMateNavHost.kt`, `ui/navigation/Route.kt`,
    `MainActivity.kt`, `androidTest/.../ui/navigation/NavigationTest.kt`

> **Punto de verificación F6:** navegación (C-13) en verde · claro y oscuro
> revisados (C-14).

---

## Fase F7 — Verificación de extremo a extremo

- [ ] **T-023** — Infraestructura compartida de pruebas
  - Depende de: T-009
  - Aceptación: existen la regla del dispatcher de prueba, los constructores de
    datos falsos, los dobles de los repositorios y las utilidades de
    `MockWebServer`, todos reutilizables; la cobertura se mide y se reporta.
  - Verificación: `./gradlew test` con el reporte de cobertura generado.
  - Archivos: `test/.../util/`, `androidTest/.../util/`,
    `app/build.gradle.kts`
  - Nota: puede adelantarse y correr en paralelo desde la fase F2.

- [ ] **T-024** — Pantalla de verificación desechable
  - Depende de: T-022, T-019
  - Aceptación: una pantalla con su `UiState`, su `ViewModel` y su Composable
    sin estado recorre el camino completo pantalla → ViewModel → caso de uso →
    repositorio → Room + red, y muestra los tres estados (cargando, datos,
    error con reintento); el archivo está marcado como desechable y la deuda
    queda anotada en `memoria.md`.
  - Verificación: recorrido manual en dispositivo (C-21); prueba del
    `ViewModel` en verde.
  - Archivos: `ui/verificacion/`, `domain/usecase/`,
    `test/.../ui/verificacion/`, `memoria.md`

- [ ] **T-025** — Comprobar el funcionamiento sin conexión
  - Depende de: T-024
  - Aceptación: con el modo avión activado, la app abre y muestra los datos
    guardados previamente, y comunica la falta de conexión sin cerrarse.
  - Verificación: comprobación manual en dispositivo (C-12); evidencia en
    captura de pantalla.
  - Archivos: — (solo verificación; si falla, se corrige el repositorio)

- [ ] **T-026** — Revisión de capas
  - Depende de: T-024
  - Aceptación: la skill `revisar-capas` corre sin una sola violación: `domain`
    sin framework, `ui` sin importar `data`, nombres en su lugar, sin
    `println`, sin dispatchers escritos a mano, sin versiones literales.
  - Verificación: salida de la skill `revisar-capas` limpia (C-02, C-03, C-20).
  - Archivos: — (auditoría; las correcciones se hacen donde haga falta)

- [ ] **T-027** — Verificar las coberturas mínimas
  - Depende de: T-023, T-026
  - Aceptación: `domain` ≥ 80 %, `data` (repositorios y mappers) ≥ 70 %,
    ViewModels 100 %, según la constitución §5.
  - Verificación: reporte de cobertura (C-22).
  - Archivos: pruebas que hagan falta para alcanzar los umbrales

> **Punto de verificación F7:** recorrido completo (C-21) · modo avión (C-12)
> · capas limpias (C-02, C-03) · coberturas (C-22).

---

## Fase F8 — Entrega

- [ ] **T-028** — Flujo de CI para cambios propuestos
  - Depende de: T-027
  - Aceptación: `pr.yml` corre en cada propuesta de cambio con las etapas
    `ktlintCheck` → `detekt` → `test` → `assembleDebug`, con caché de Gradle;
    el flujo falla si cualquier etapa falla.
  - Verificación: la ejecución aparece en verde en GitHub Actions (C-16).
  - Archivos: `.github/workflows/pr.yml`
  - Nota: cambiar el CI requiere autorización (constitución §4).

- [ ] **T-029** — Etapas de seguridad en el pipeline
  - Depende de: T-028
  - Aceptación: el flujo de cambios propuestos incluye análisis estático de
    seguridad (CodeQL), revisión de dependencias vulnerables y búsqueda de
    secretos; un secreto introducido a propósito hace fallar el flujo.
  - Verificación: ejecución en verde; prueba deliberada con un secreto falso
    que es detectado.
  - Archivos: `.github/workflows/pr.yml`,
    `.github/workflows/security.yml`

- [ ] **T-030** — Probar que el pipeline sí reprueba
  - Depende de: T-029
  - Aceptación: un cambio propuesto con una prueba que falla a propósito deja
    el pipeline en rojo y no se puede integrar.
  - Verificación: evidencia de la ejecución en rojo (C-17); después se
    revierte el cambio.
  - Archivos: — (prueba temporal, se revierte)

- [ ] **T-031** — Flujo de generación del artefacto
  - Depende de: T-030
  - Aceptación: `release.yml` genera un AAB firmado al integrar a `main`, con
    el `versionCode` y `versionName` tomados de la matriz de versionamiento
    (T-001), usando secretos de GitHub para la firma; el artefacto queda
    guardado en la ejecución.
  - Verificación: ejecución del flujo con el AAB descargable (C-18); se
    confirma que el AAB está minificado y sin depuración.
  - Archivos: `.github/workflows/release.yml`, `app/build.gradle.kts`
  - Nota: **no** se conecta la subida a Google Play en esta unidad (G-06); eso
    es de la spec de liberación.

- [ ] **T-032** — Auditoría de seguridad y cierre de la unidad
  - Depende de: T-031
  - Aceptación: el agente `seguridad` audita el repositorio y no quedan
    hallazgos de severidad crítica ni alta; no hay secretos en el repositorio
    ni en su historial; se repasa la lista de los 22 criterios de éxito de la
    spec y todos están comprobados.
  - Verificación: reporte del agente `seguridad` (C-19); tabla de criterios de
    éxito completa en `memoria.md`.
  - Archivos: `memoria.md`

> **Punto de verificación F8:** pipeline en verde (C-16) · el pipeline reprueba
> cuando debe (C-17) · `release` endurecido (C-18) · sin secretos (C-19).

---

## Resumen

| Fase | Tareas | Entrega |
|---|---|---|
| F1 Cimientos | T-001 … T-006 | Matriz, build corregido, calidad, paquetes |
| F2 Núcleo | T-007 … T-010 | `AppResult`, `UiText`, `Logger`, dispatchers, Hilt |
| F3 Datos | T-011 … T-013 | Room, cliente HTTP, mapeo de errores |
| F4 Sesión | T-014 … T-017 | Credenciales cifradas, renovación, cierre de sesión |
| F5 Caché | T-018, T-019 | Política de caché y patrón offline-first |
| F6 Presentación | T-020 … T-022 | Tema, componentes, navegación |
| F7 Verificación | T-023 … T-027 | Recorrido completo, sin conexión, capas, cobertura |
| F8 Entrega | T-028 … T-032 | CI/CD, seguridad, artefacto firmado |

**Tareas bloqueadas por preguntas abiertas:** T-001 (Q-07), T-002 (Q-01, Q-05),
T-005 (Q-02), T-013 (Q-03), T-017 (Q-04), T-018 (Q-06).

Esas seis preguntas deben resolverse con el profesor y con el responsable del
backend antes de que empiece la ejecución.

---

## Puerta de aprobación

- [ ] El orden y el tamaño de las tareas están revisados.
- [ ] Las preguntas abiertas que bloquean tareas tienen respuesta.
- [ ] **Autorizada la ejecución** — hasta entonces, nada se implementa.
