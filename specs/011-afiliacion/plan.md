# Plan 011 — Afiliación de negocios

> Plan técnico de la unidad `011-afiliacion`
> Estado: **redactado, pendiente de aprobación**
> Depende de [`spec.md`](spec.md) aprobada

Este documento responde **cómo**. El **qué** está en la spec.

---

## 1. Principios aplicados

- `ui → domain ← data`; el dominio de negocios es Kotlin puro.
- Un `ViewModel` por paso, más uno que gobierna el flujo completo.
- El borrador vive en Room, no en memoria: sobrevive a la muerte del proceso.
- Se reutiliza el mapa de 010 y el punto de moderación de 004; no se duplican.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| AF-01 | `Business`, `BusinessType`, `Service` | domain/model | Modelos del negocio, sus tipos y sus servicios |
| AF-02 | `AffiliationDraft` | domain/model | Borrador con el avance de los tres pasos |
| AF-03 | `BusinessRepository` | domain/repository | Contrato: crear negocio, consultar el propio, guardar borrador |
| AF-04 | `SaveAffiliationDraftUseCase` | domain/usecase | Guarda el avance de un paso |
| AF-05 | `ValidateAffiliationStepUseCase` | domain/usecase | Reglas de validación por paso |
| AF-06 | `SubmitAffiliationUseCase` | domain/usecase | Envía la afiliación completa |
| AF-07 | `BusinessDto`, `ServiceDto` y mappers | data/remote | Transporte y traducción |
| AF-08 | `BusinessApiService` | data/remote | Llamadas al backend de negocios |
| AF-09 | `AffiliationDraftEntity` y su DAO | data/local | Persistencia del borrador |
| AF-10 | `BusinessRepositoryImpl` | data/repository | Orquesta borrador local y envío remoto |
| AF-11 | `AffiliationFlowViewModel` | ui/afiliacion | Estado del flujo y paso activo |
| AF-12 | `AffiliationStep1Screen` | ui/afiliacion | Datos y tipos |
| AF-13 | `AffiliationStep2Screen` | ui/afiliacion | Servicios con precio |
| AF-14 | `AffiliationStep3Screen` | ui/afiliacion | Pin en el mapa |
| AF-15 | `ModeSwitcher` | ui/navigation | Alternancia entre modo dueño y modo negocio |
| AF-16 | `Money` | domain/model | Representación del precio, compartida con 014 |

**Se reutiliza sin duplicar:** cliente HTTP y errores de 001; sesión de 002;
permiso de ubicación de 003; moderación de 004; componente de mapa de 010.

---

## 3. Orden de construcción

| Fase | Componentes | Entrega |
|---|---|---|
| F1 Dominio | AF-01, AF-02, AF-16, AF-03, AF-05 | Modelos, contratos y validación |
| F2 Datos | AF-07, AF-08, AF-09 | Transporte y persistencia del borrador |
| F3 Repositorio | AF-10, AF-04, AF-06 | Orquestación completa |
| F4 Pasos | AF-11, AF-12, AF-13 | Flujo con los dos primeros pasos |
| F5 Mapa | AF-14 | Paso 3 sobre el componente de 010 |
| F6 Modos | AF-15 | Alternancia y cierre de la unidad |

F1 y F2 pueden ir en paralelo una vez fijados los contratos de AF-03.

---

## 4. Decisiones

### D-01 — `Money` como tipo propio, no `Double`
**Decisión:** el precio se representa con un tipo `Money` que guarda el monto
en la unidad mínima (centavos) como entero, más la moneda.
**Por qué:** `Double` pierde precisión al sumar, y 014 va a sumar servicios,
vacunas, medicinas e IVA para el cobro. Un error de redondeo en un cobro real
es un problema serio, no estético.
**Alternativa descartada:** `Double` o `Float` — fallan en sumas de decimales.
**Consecuencia:** 014 reutiliza este tipo; no define el suyo.

### D-02 — Borrador en Room, no en el estado del ViewModel
**Decisión:** cada paso guarda su avance en una tabla de borrador.
**Por qué:** R-05 exige que el avance sobreviva a salir de la app. Un
`SavedStateHandle` sobrevive a la muerte del proceso pero no a cerrar la app.
**Alternativa descartada:** solo memoria — pierde todo al salir.

### D-03 — Un ViewModel de flujo, tres pantallas sin estado propio
**Decisión:** `AffiliationFlowViewModel` gobierna el paso activo y el borrador
completo; cada pantalla recibe su porción y emite eventos.
**Por qué:** los tres pasos comparten un solo objeto en construcción. Tres
ViewModels independientes tendrían que sincronizarse entre sí.

### D-04 — El borrador se borra al cambiar de sesión
**Decisión:** el borrador se asocia al identificador del usuario y se limpia
en el logout.
**Por qué:** R-13. Un borrador con los datos del negocio de otra persona
visible tras cambiar de cuenta es una fuga.

### D-05 — Validación en el dominio, no en la pantalla
**Decisión:** `ValidateAffiliationStepUseCase` concentra las reglas.
**Por qué:** las mismas reglas se aplican al avanzar y al enviar. Duplicarlas
en los Composables garantiza que se desincronicen.

### D-06 — La afiliación requiere conexión
**Decisión:** crear el negocio no se encola en el outbox de 005.
**Por qué:** el negocio necesita identificador del servidor para aparecer en
el mapa y recibir escaneos. Encolarlo daría la ilusión de estar afiliado sin
estarlo. El borrador sí es local.
**Consecuencia:** C-14 comprueba que sin red se explica y no se pierde nada.

### D-07 — El pin reutiliza el mapa de 010
**Decisión:** el paso 3 monta el componente de mapa de 010 en modo selección.
**Por qué:** dos implementaciones de mapa significan dos SDK, dos llaves y dos
comportamientos distintos.
**Riesgo:** 010 depende del proveedor sin decidir (su Q-07). Si 010 se retrasa,
el paso 3 se bloquea. Se asume porque la alternativa es peor.

---

## 5. Dependencias nuevas

Ninguna propia. El SDK de mapas lo introduce 010; esta unidad lo consume.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El contrato del backend cambia y rompe los DTOs | Mappers aislados; el dominio no cambia |
| RT-02 | El borrador crece y se desincroniza del formulario | Una sola fuente: el borrador en Room; la pantalla lo observa |
| RT-03 | El proveedor de mapas sin decidir bloquea el paso 3 | F5 es la última fase; F1–F4 avanzan sin el mapa |
| RT-04 | Sumas de precios con error de redondeo | D-01: `Money` en enteros; pruebas de suma |
| RT-05 | La moderación rechaza y el usuario pierde lo capturado | El borrador no se borra ante rechazo; solo se marca |

---

## 7. Puntos de verificación

| Después de | Se verifica |
|---|---|
| F1 | Validación por paso probada (C-02, C-04, C-06, C-07) |
| F2 | Borrador persiste al cerrar la app (C-08) |
| F3 | Envío con doble de backend y manejo de error (C-09, C-12) |
| F4 | Pasos 1 y 2 completos (C-03, C-05) |
| F5 | Pin colocado y confirmado (C-07) |
| F6 | Modos alternan sin perder mascotas (C-10); cierre de unidad |

---

## 8. Lo que este plan NO hace

- No implementa el panel del negocio (012) ni el escáner (013).
- No define el cobro (014), aunque sí el tipo `Money` que usará.
- No valida la identidad legal del negocio.
- No edita el negocio después de afiliado (Q-08).
