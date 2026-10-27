# memoria.md — Estado del proyecto PetMate Móvil

> Memoria viva del repositorio. Los agentes la leen al empezar y la actualizan
> al terminar cada tarea. Si algo se decidió, está aquí.
>
> Última actualización: 27/10/2026

---

## 1. Dónde vamos

| Campo | Estado |
|---|---|
| Fase SDD actual | **SPECIFY / PLAN / TASK de la arquitectura base — redactados, pendientes de aprobación** |
| Código implementado | Ninguno más allá del andamio que generó Android Studio |
| Tareas ejecutadas | **0 — por instrucción del profesor, aún no se ejecutan** |
| Siguiente paso | Que el profesor revise los archivos SDD. Las specs de funcionalidad se definen en la próxima clase. |

### Documentos listos
- [x] `constitution.md` (v1.0.0)
- [x] `AGENTS.md`
- [x] `memoria.md` (este archivo)
- [x] `.claude/agents/` — 5 subagentes
- [x] `.claude/skills/` — 4 skills
- [x] `specs/001-arquitectura-base/spec.md`
- [x] `specs/001-arquitectura-base/plan.md`
- [x] `specs/001-arquitectura-base/task.md`
- [ ] `specs/002-*` funcionalidad específica — **no empezar todavía**

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
- [ ] Proveedor de mapas (Google Maps vs. alternativa) — se define en la spec
      de la feature del mapa, no en la arquitectura base.
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
- Arranca el proyecto en la organización **PETMATE-APP'S**; el repositorio del
  backend en ASP.NET Core ya existe.
- Se crea la estructura de **Spec Driven Development** de la app móvil:
  `constitution.md`, `AGENTS.md`, `memoria.md`, subagentes, skills y la unidad
  `specs/001-arquitectura-base/` con `spec.md`, `plan.md` y `task.md`.
- Las specs cubren **solo la arquitectura base**, sin funcionalidad de PetMate,
  por indicación del profesor.
- **No se ejecuta ninguna tarea**: el profesor dio instrucción explícita de
  esperar.
- Se verifica en PowerShell que la estructura está completa y que **0 de 32**
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

---

## 7. Deudas y defectos conocidos

| # | Qué | Severidad | Nota |
|---|---|---|---|
| D-01 | `app/src/main/java/.../` sigue en `com.example.petmateedel` | Media | Debería ser un `applicationId` real (p. ej. `mx.petmate.app`) antes de publicar en Google Play. Requiere permiso para cambiarlo. |
| D-02 | Quedaron en el repositorio `hs_err_pid26380.log` y `replay_pid26380.log` | Baja | Logs de un crash de la JVM de Gradle; se pueden borrar. |
| D-03 | `minSdk` 24 vs. Android 8+ (API 26) de los lineamientos | Media | Confirmar con el profesor (ver §3). |
| D-04 | No hay matriz de versionamiento escrita todavía | Alta | Los lineamientos la exigen **antes** de desarrollar. Es la tarea T-001. |
