# memoria.md — Estado del proyecto PetMate Móvil

> Memoria viva del repositorio. Los agentes la leen al empezar y la actualizan
> al terminar cada tarea. Si algo se decidió, está aquí.
>
> Última actualización: 29/10/2026

---

## 1. Dónde vamos

| Campo | Estado |
|---|---|
| Fase SDD actual | **SPECIFY / PLAN / TASK de las unidades 001 a 010 — redactados, pendientes de aprobación** |
| Código implementado | Ninguno más allá del andamio que generó Android Studio |
| Tareas ejecutadas | **0 — por instrucción del profesor, aún no se ejecutan** |
| Siguiente paso | Revisión y aprobación de los documentos de 001 a 010. Las unidades 011 a 020 siguen sin abrir. |

### Documentos listos
- [x] `constitution.md` (v1.1.0)
- [x] `AGENTS.md`
- [x] `memoria.md` (este archivo)
- [x] `.claude/agents/` — 5 subagentes
- [x] `.claude/skills/` — 4 skills
- [x] `specs/001-arquitectura-base/` — `spec.md`, `plan.md`, `task.md` (42 tareas)
- [x] `specs/002-identidad-y-sesion/` — `spec.md`, `plan.md`, `task.md` (18 tareas)
- [x] `specs/003-permisos/` — `spec.md`, `plan.md`, `task.md` (10 tareas)
- [x] `specs/004-moderacion-contenido/` — `spec.md`, `plan.md`, `task.md` (12 tareas)
- [x] `specs/005-sincronizacion-offline/` — `spec.md`, `plan.md`, `task.md` (12 tareas)
- [x] `specs/006-notificaciones-push/` — `spec.md`, `plan.md`, `task.md` (11 tareas)
- [x] `specs/007-mis-mascotas/` — `spec.md`, `plan.md`, `task.md` (13 tareas)
- [x] `specs/008-carnet-salud/` — `spec.md`, `plan.md`, `task.md` (14 tareas)
- [x] `specs/009-compartir-historial/` — `spec.md`, `plan.md`, `task.md` (11 tareas)
- [x] `specs/010-mapa-e-inicio/` — `spec.md`, `plan.md`, `task.md` (14 tareas)
- [x] `specs/MAPA-CAPACIDADES.md` — 20 unidades, dependencias y olas
- [ ] `specs/011-*` a `specs/020-*` — **no abrir todavía**

---

## 2. El producto en dos párrafos

PetMate es una app Android que funciona como **carnet digital de salud de la
mascota**. El dueño registra a sus mascotas con vacunas, desparasitaciones,
peso, alergias e historial médico, y puede **compartir ese historial mediante
un QR** con duración y secciones limitadas. La pantalla de inicio es un **mapa
de veterinarias, tiendas y estéticas afiliadas**.

Hay dos roles: **Usuario** (dueño) y **Afiliado** (negocio). Un usuario se
afilia desde su perfil eligiendo uno o varios tipos de negocio y entra en
"modo negocio" sin perder sus mascotas. El negocio escanea QRs, registra
consultas con cobro desglosado, da seguimiento al tratamiento y chatea con el
dueño.

---

## 3. Arquitectura decidida

- **Kotlin nativo + Jetpack Compose** (Material 3). *Queda descartado Flutter
  para este repositorio: el proyecto ya existe en Kotlin.*
- **MVVM + Clean Architecture** en tres capas: `ui` → `domain` ← `data`.
- **Hilt** para inyección de dependencias.
- **Room (SQLite)** como fuente de verdad local; la app es **offline-first**.
- **Retrofit + OkHttp + kotlinx.serialization** contra la API en `/api/v1`.
- **Navigation Compose** con un solo `NavHost` y rutas tipadas.
- **JWT**: token de acceso de 15 min + refresh rotativo de 30 días. La sesión
  no expira sola; solo el logout la cierra.
- **Caché frontend** (nivel 3 de los 3 exigidos): memoria + disco con
  expiración explícita.
- **FCM** para notificaciones push.
- Backend en **ASP.NET Core**, nube **Azure**, base **MongoDB Atlas + Redis**
  (fuera de este repositorio).

### Lo que falta decidir
- [ ] Función de **ML** definitiva (candidata: raza por foto con ML Kit/TFLite).
- [ ] Función de **IA generativa** definitiva (candidatas: "explícame fácil"
      la receta, resumen del historial).
- [ ] Proveedor de mapas (Google Maps vs. alternativa) — queda en `010/Q-01`
      y bloquea el SDK de esa unidad, no la spec ni el resto del plan.
- [ ] `minSdk` final: el andamio trae 24; los lineamientos piden Android 8+
      (API 26). **Confirmar con el profesor antes de fijarlo.**

---

## 4. Equipo y reparto

| Quién | Qué le toca |
|---|---|
| **Adrián Adonai García Jiménez** | App móvil (mapa, mascotas, carnet, QR, chat, cobro) + toda la nube en Azure (Terraform, 2 instancias, balanceador, HTTPS, Redis, CDN, moderación, observabilidad) + CI/CD + matriz de versiones + publicación en Google Play + **estructura SDD** |
| **Eduardo Olvera Camacho** | App móvil (seguimiento, notificaciones, afiliación, panel, nueva mascota, vacunas, alergias, permisos), SQLite sin internet, ML, IA generativa, landing con Google Analytics |
| **Miguel Angel Nuñez Gómez** | Backend (API) y NoSQL: MongoDB Atlas + Redis, JWT, caché, rate limit, moderación, chat, push |
| **Emanuel Cruz Cruz** | Pruebas unitarias, de seguridad, de integración, de nube y de regresión |

Organización GitHub: **PETMATE-APP-S**.

| Repositorio | Qué es | Visibilidad |
|---|---|---|
| `app-petmate-kotlin` | **Este**: app móvil Android (Kotlin + Compose) | privado |
| `backend-petmate-nosdd-aspnetcore` | Backend (API REST en ASP.NET Core) | privado |

URL de este repo: https://github.com/PETMATE-APP-S/app-petmate-kotlin
Ramas: `main` (estable) y `develop`.

> ⚠️ Existe un proyecto gemelo con otra profesora en la organización
> `PetMate-AMEL`. **No se mezclan repositorios, commits, tableros ni entregas.**

---

## 5. Restricciones que no se negocian

- Spec Driven Development + DevSecOps; desarrollo con agentes de IA.
- Pruebas unitarias, de seguridad y de integración; CI/CD para backend y móvil.
- Caché en tres niveles (base de datos, backend, frontend).
- Logout real en backend y móvil; sesión siempre viva mientras no haya logout.
- Matriz de versionamiento definida **antes** de desarrollar y aplicada en cada
  despliegue.
- Nube con balanceador, mínimo 2 instancias, Terraform, salida HTTPS, costo cero.
- Base de datos NoSQL.
- Móvil: despliegue desde GitHub a Google Play, moderación de contenido antes
  de guardar, push, aviso de privacidad ligado a la landing, eliminar cuenta,
  manejo de permisos, SQLite con sincronización, al menos una función de ML y
  una de IA generativa.
- Entrega en `.rar` + script de la base de datos.
- **Fecha de evaluación: 02/12/2026.**

---

## 6. Bitácora

### 27/10/2026
- Arranca el proyecto en la organización **PETMATE-APP-S**; el repositorio del
  backend en ASP.NET Core ya existe.
- Se crea la estructura de **Spec Driven Development** de la app móvil:
  `constitution.md`, `AGENTS.md`, `memoria.md`, subagentes, skills y la unidad
  `specs/001-arquitectura-base/` con `spec.md`, `plan.md` y `task.md`.
- Las specs cubren **solo la arquitectura base**, sin funcionalidad de PetMate,
  por indicación del profesor.
- **No se ejecuta ninguna tarea**: el profesor dio instrucción explícita de
  esperar.
- Se verifica en PowerShell que la estructura está completa y que **0 de 42**
  tareas se ejecutaron (evidencia en `docs/evidencia-verificacion.md`).
- Se crea el repositorio **`app-petmate-kotlin`** (privado) en la organización
  **PETMATE-APP-S**, con ramas `main` y `develop`. Primer commit: la estructura
  SDD completa, sin código de las capas.
- No se pudo correr la auditoría en **OpenCode**: la cuenta devuelve
  `Free usage limit reached` incluso con un mensaje de una palabra y tras
  cambiar de modelo. El prompt queda listo en
  `docs/prompt-auditoria-opencode.md` para cuando se restablezca la cuota.
- Pendiente para la próxima clase: instrucciones sobre las specs de
  funcionalidad específica de backend y móvil.

### 29/10/2026
- Se rectifica la unidad `001-arquitectura-base` tras la auditoría contra los
  lineamientos del profesor. Se sube `constitution.md` a **v1.1.0**: se agrega
  el principio P13 (moderación antes de almacenar), se amplía P4 con las
  escrituras offline, se añaden al stack el consumo de Azure, la publicación en
  Play y la moderación, y se documentan en §3 las carpetas `session`, `cache`,
  `sync`, `moderation` y `push`.
- La spec 001 pasa de 15 a 22 capacidades y de 22 a 30 criterios de éxito. El
  plan pasa de 25 a 32 componentes y de 14 a 20 decisiones. `task.md` pasa de
  32 a **42 tareas** (nuevas T-033 a T-042), todas sin marcar.
- Nuevas áreas cubiertas: publicación en Google Play desde GitHub Actions
  (track interno → prueba cerrada), consumo del backend en Azure con
  reintentos e idempotencia, punto de intercepción de moderación, base de FCM,
  mecanismo de permisos y outbox de sincronización offline.
- Se corrigen desalineaciones menores: nombre de la organización
  (`PETMATE-APP-S`), nombre real del repositorio (`app-petmate-kotlin`), R-14
  condicionada a Q-01 y advertencia de autorización en T-010 por el manifiesto.
- La revisión del `git diff` detectó y corrigió cuatro textos que contradecían
  la rectificación: G-06 y D-12 aún decían que la subida a Play era de la spec
  de liberación, la nota de T-031 lo repetía, y D-10 citaba R-22 en lugar de
  R-25. También se alineó el árbol de `core/` del plan con las rutas
  `core/permissions` y `core/connectivity`.
- **Siguen sin ejecutarse tareas**: 0 / 42.
- Se crea `specs/MAPA-CAPACIDADES.md` con las **20 unidades** del producto, sus
  dependencias en una sola dirección, 13 olas de construcción y el camino
  crítico `001 → 002 → 007 → 008 → 010 → 011 → 012 → 013 → 014 → 015 → 016 →
  020`. Se anota en §4 el riesgo de calendario de 020: la prueba cerrada de
  Google Play exige 12 testers durante 14 días antes de producción, así que la
  cuenta, la ficha y el primer AAB deben arrancar en paralelo desde ya.
- Se redactan las unidades **002 a 010** completas (`spec.md`, `plan.md` y
  `task.md`), todas sin tareas marcadas: 002 (18 tareas), 003 (10), 004 (12),
  005 (12), 006 (11), 007 (13), 008 (14), 009 (11) y 010 (14). Total de las
  nueve unidades: **115 tareas**, 0 ejecutadas (más las 42 de 001).
- Decisiones cerradas con el humano en esta sesión:
  - `010`: "lo más próximo" es el próximo **evento de salud** (vacuna o
    desparasitación), no el negocio más cercano; rojo ≤ 3 días, amarillo ≤ 14.
    La tarjeta depende de 008 y por eso 010 va después en el mapa.
  - `009`: el selector de negocios es una lista de **solo lectura** del
    directorio del backend, con la opción "cualquier veterinaria"; no crea
    negocios ni toca el mapa.
  - `004`: la app llama al **backend** y el backend llama a Azure; la llave
    nunca vive en el dispositivo. El texto se guarda offline y se modera al
    sincronizar (si se rechaza queda marcado); imágenes y videos no se suben
    sin moderar.
  - `005`: la política de sincronización por entidad se fija por adelantado,
    incluidas mascota y carnet; una entidad nueva se agrega ampliando 005 con
    aprobación. Conflictos: en datos clínicos gana el servidor con aviso; en
    datos de la mascota gana el más reciente.
  - `008`: los registros de origen veterinario no se editan ni se borran desde
    el carnet.
  - `003` y `006`: ningún permiso se pide al arrancar; el payload push no lleva
    datos clínicos.
- Dependencias que quedan bloqueadas por preguntas abiertas, no por falta de
  diseño: el SDK de mapas (`010/Q-01`), `coil-compose` en 007 (autorización
  pendiente) y el contrato de red de cada unidad con el backend.

---

## 7. Deudas y defectos conocidos

| # | Qué | Severidad | Nota |
|---|---|---|---|
| D-01 | `app/src/main/java/.../` sigue en `com.example.petmateedel` | Media | Debería ser un `applicationId` real (p. ej. `mx.petmate.app`) antes de publicar en Google Play. Requiere permiso para cambiarlo. |
| D-02 | Quedaron en el repositorio `hs_err_pid26380.log` y `replay_pid26380.log` | Baja | Logs de un crash de la JVM de Gradle; se pueden borrar. |
| D-03 | `minSdk` 24 vs. Android 8+ (API 26) de los lineamientos | Media | Confirmar con el profesor (ver §3). |
| D-04 | No hay matriz de versionamiento escrita todavía | Alta | Los lineamientos la exigen **antes** de desarrollar. Es la tarea T-001. |
