# Plan 015 — Seguimiento del tratamiento

> Plan técnico de la unidad `015-seguimiento-tratamiento`
> Estado: **redactado, pendiente de aprobación**
> Depende de [`spec.md`](spec.md) aprobada

Este documento responde **cómo**. El **qué** está en la spec.

---

## 1. Principios aplicados

- `ui → domain ← data`; la máquina de estados vive en el dominio.
- Los umbrales de color se importan de la constante compartida; no se
  redefinen (R-12).
- El reloj se inyecta para todo cálculo de fechas.
- La autorización de quién cambia el estado la valida el backend.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| SG-01 | `FollowUp` | domain/model | Seguimiento: estado, revisión y bitácora |
| SG-02 | `FollowUpStatus` | domain/model | En tratamiento, Mejorando, Dado de alta |
| SG-03 | `FollowUpEntry` | domain/model | Entrada de bitácora: estado, fecha, autor, nota |
| SG-04 | `FollowUpTransition` | domain/usecase | Máquina de estados: qué transición es válida |
| SG-05 | `ClassifyReviewDateUseCase` | domain/usecase | Atrasada, próxima o normal, con reloj |
| SG-06 | `FollowUpRepository` | domain/repository | Contrato |
| SG-07 | `ChangeFollowUpStatusUseCase` | domain/usecase | Valida transición, modera nota y registra |
| SG-08 | DTOs y mappers | data/remote | Transporte |
| SG-09 | `FollowUpApiService` | data/remote | Llamadas al backend |
| SG-10 | Entidad y DAO de seguimiento | data/local | Lectura sin conexión |
| SG-11 | `FollowUpRepositoryImpl` | data/repository | Red → Room → UI |
| SG-12 | `FollowUpViewModel` | ui/seguimiento | Estado de la pantalla |
| SG-13 | `FollowUpScreen` | ui/seguimiento | Vista con permisos por rol |
| SG-14 | `StatusTimeline` | ui/seguimiento | Bitácora de cambios |
| SG-15 | `ReviewDateBadge` | ui/seguimiento | Fecha con su color |

**Se reutiliza sin duplicar:** umbrales y clasificación de 012; moderación de
004; disparo de alertas de 006; red, Room y reloj de 001.

---

## 3. Orden de construcción

| Fase | Componentes | Entrega |
|---|---|---|
| F1 Máquina de estados | SG-01 … SG-05 | Modelos y transiciones probadas |
| F2 Dominio | SG-06, SG-07 | Contrato y cambio de estado |
| F3 Datos | SG-08, SG-09, SG-10 | Transporte y lectura local |
| F4 Repositorio | SG-11 | Offline-first y moderación |
| F5 Presentación | SG-12 … SG-15 | Vista por rol y bitácora |
| F6 Cierre | — | Integración y verificación |

---

## 4. Decisiones

### D-01 — Máquina de estados explícita, no condicionales dispersos
**Decisión:** `FollowUpTransition` declara qué transiciones son válidas en una
tabla: En tratamiento → Mejorando, En tratamiento → Dado de alta,
Mejorando → En tratamiento, Mejorando → Dado de alta. Desde Dado de alta no
sale ninguna.
**Por qué:** G-01. Si la regla vive repartida en `if` dentro de la pantalla,
una ruta de navegación distinta la salta y el seguimiento queda en un estado
imposible.
**Consecuencia:** C-03, C-04 y C-05 prueban la tabla completa, no solo el
camino feliz.

### D-02 — Los umbrales se importan, no se redefinen
**Decisión:** `ClassifyReviewDateUseCase` usa la misma constante y el mismo
cálculo que 012.
**Por qué:** G-03 y R-12. Es la cuarta unidad que usa rojo ≤3 y amarillo ≤14.
Cuatro definiciones se desincronizan con certeza.
**Consecuencia:** C-15 compara el resultado de ambos cálculos con las mismas
entradas.

### D-03 — La alerta la dispara el backend al registrar el cambio
**Decisión:** la app registra el cambio de estado; el backend decide y envía
la notificación.
**Por qué:** G-05. Si la app enviara la alerta después de guardar, un cierre
de la app entre ambos pasos dejaría al dueño sin aviso. Un solo paso del lado
del servidor no se parte.
**Pendiente:** Q-02 confirma que el backend lo hace así.

### D-04 — El rol se valida en el backend, no solo en la pantalla
**Decisión:** la pantalla oculta los controles al dueño, y además el backend
rechaza el cambio si quien lo pide no es la veterinaria que atiende.
**Por qué:** G-04. Ocultar un botón no es autorización. Esto es la misma
lección de 013/D-01.

### D-05 — El seguimiento se lee de Room; cambiarlo requiere red
**Decisión:** la lectura es offline-first; el cambio de estado no se encola.
**Por qué:** R-10 permite consultar sin conexión, que es el caso del dueño
revisando cómo va su mascota. Pero un cambio de estado notifica a alguien y
debe quedar del lado del servidor; encolarlo mostraría "Dado de alta" al
veterinario sin que el dueño se entere.

### D-06 — El seguimiento guarda estado y notas, no el expediente clínico
**Decisión:** la entidad local guarda estado, fechas, autor y notas; el
diagnóstico y la receta siguen en 014.
**Por qué:** G-06. Evita duplicar contenido clínico en el dispositivo del
negocio y mantiene un solo origen del dato.

---

## 5. Dependencias nuevas

Ninguna.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | Una transición inválida llega por una ruta no prevista | D-01: la validación está en el caso de uso, no en la pantalla |
| RT-02 | El backend acepta un cambio que la app considera inválido | La app valida antes de enviar y el backend valida también; se documenta el contrato |
| RT-03 | La bitácora crece sin límite | Paginación de la bitácora si supera un umbral; Q-06 define retención |
| RT-04 | El dueño ve controles de edición por un error de estado | D-04 y prueba C-11 con sesión de dueño |
| RT-05 | Dos cambios simultáneos dejan la bitácora desordenada | El orden lo fija la fecha del servidor, no la del dispositivo |

---

## 7. Puntos de verificación

| Después de | Se verifica |
|---|---|
| F1 | Tabla de transiciones completa (C-02 a C-05); clasificación por fecha (C-08, C-09) |
| F2 | Cambio de estado valida y modera (C-13) |
| F3 | Transporte y lectura local correctos |
| F4 | Lectura sin conexión; cambio sin red explicado (C-14) |
| F5 | Vista por rol y bitácora (C-06, C-10, C-11) |
| F6 | Alerta disparada (C-07); umbrales coinciden con 012 (C-15) |

---

## 8. Lo que este plan NO hace

- No registra la consulta ni el cobro (014).
- No implementa el chat (016), solo lo cierra al dar de alta.
- No envía las notificaciones: las dispara el backend (006).
- No permite que el dueño cambie el estado.
