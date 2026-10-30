# Plan 014 — Consulta veterinaria y cobro

> Plan técnico de la unidad `014-consulta-veterinaria`
> Estado: **redactado, pendiente de aprobación**
> Depende de [`spec.md`](spec.md) aprobada

Este documento responde **cómo**. El **qué** está en la spec.

---

## 1. Principios aplicados

- `ui → domain ← data`; el cálculo del cobro vive en el dominio, en Kotlin
  puro, y se prueba sin dispositivo.
- El dinero se representa con el `Money` de 011: enteros, nunca decimales de
  punto flotante.
- El borrador de la consulta es local y temporal; el contenido clínico se
  borra al cerrar la consulta.
- La autorización la valida el backend; la app no decide si puede escribir.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| CV-01 | `Consultation` | domain/model | Consulta completa: clínica y cobro |
| CV-02 | `Prescription`, `PrescriptionItem` | domain/model | Receta con sus medicamentos |
| CV-03 | `ChargeLine` | domain/model | Renglón del cobro: concepto, tipo y `Money` |
| CV-04 | `ChargeSummary` | domain/model | Subtotal, IVA y total |
| CV-05 | `PaymentMethod` | domain/model | Forma de pago |
| CV-06 | `TaxRate` | domain/model | Tasa configurable, no constante en código |
| CV-07 | `CalculateChargeUseCase` | domain/usecase | Suma renglones, aplica IVA y redondea |
| CV-08 | `ConsultationDraft` | domain/model | Borrador en curso |
| CV-09 | `ConsultationRepository` | domain/repository | Contrato: guardar consulta, crear servicio |
| CV-10 | `ValidateConsultationUseCase` | domain/usecase | Reglas de guardado |
| CV-11 | `SaveConsultationUseCase` | domain/usecase | Guarda y propaga el peso al carnet |
| CV-12 | DTOs y mappers | data/remote | Transporte y traducción |
| CV-13 | `ConsultationApiService` | data/remote | Llamadas al backend |
| CV-14 | `ConsultationDraftEntity` y DAO | data/local | Borrador persistente |
| CV-15 | `ConsultationRepositoryImpl` | data/repository | Orquestación |
| CV-16 | `ConsultationViewModel` | ui/consulta | Estado de la consulta |
| CV-17 | `ClinicalSection` | ui/consulta | Motivo, diagnóstico y peso |
| CV-18 | `PrescriptionSection` | ui/consulta | Receta con varios medicamentos |
| CV-19 | `ChargeSection` | ui/consulta | Renglones, total en vivo y pago |
| CV-20 | `NewServiceDialog` | ui/consulta | Alta de servicio durante la consulta |

**Se reutiliza sin duplicar:** `Money` de 011; carnet y peso de 008; ficha y
token de 013; red, errores y componentes de 001.

---

## 3. Orden de construcción

| Fase | Componentes | Entrega |
|---|---|---|
| F1 Cálculo | CV-03 … CV-07 | El cobro calculado y probado, sin interfaz |
| F2 Dominio | CV-01, CV-02, CV-08 … CV-11 | Modelos, contrato y reglas |
| F3 Datos | CV-12, CV-13, CV-14 | Transporte y borrador |
| F4 Repositorio | CV-15 | Guardado y propagación del peso |
| F5 Clínica | CV-16, CV-17, CV-18 | Motivo, diagnóstico, peso y receta |
| F6 Cobro | CV-19, CV-20 | Renglones, total en vivo y cierre |

**F1 va primero a propósito:** el cálculo del dinero es la parte donde un
error cuesta caro y es la más fácil de probar aislada. Se deja correcta antes
de construir nada encima.

---

## 4. Decisiones

### D-01 — El cálculo del cobro es dominio puro y se prueba primero
**Decisión:** `CalculateChargeUseCase` recibe los renglones y la tasa, y
devuelve `ChargeSummary`. No conoce la pantalla ni la red.
**Por qué:** G-01. Un error de redondeo en un cobro real produce reclamaciones
y desconfianza. Aislado en el dominio se prueba con decenas de combinaciones
en milisegundos, sin emulador.
**Alternativa descartada:** calcular en el `ViewModel` o en el Composable —
imposible de probar exhaustivamente y se duplica al guardar.

### D-02 — Redondeo explícito y documentado
**Decisión:** el IVA se calcula sobre el subtotal en enteros y se redondea
**al centavo más cercano, con medio hacia arriba**, una sola vez al final.
**Por qué:** redondear cada renglón y luego sumar da un total distinto a sumar
y luego redondear. Sin una regla escrita, el total de la app y el de la
calculadora del veterinario no coinciden.
**Consecuencia:** C-08 prueba montos que producen fracciones de centavo.

### D-03 — La tasa de IVA es configurable
**Decisión:** `TaxRate` llega desde la configuración del negocio o del
backend, no como constante en el código.
**Por qué:** G-06 y Q-03. Una tasa escrita en el código obliga a publicar una
versión nueva si cambia.

### D-04 — El peso tiene un solo origen
**Decisión:** `SaveConsultationUseCase` escribe el peso en el carnet de 008
como parte de la misma operación, no como una llamada aparte desde la
pantalla.
**Por qué:** G-08. Si la pantalla guarda la consulta y además registra el
peso, un reintento duplica el registro.

### D-05 — El borrador se borra al cerrar la consulta
**Decisión:** `ConsultationDraftEntity` se elimina al guardar con éxito y al
descartar; nunca sobrevive a la consulta.
**Por qué:** G-03 y R-14. El negocio no debe acumular en su dispositivo
diagnósticos y recetas de mascotas que no son suyas. El borrador existe para
no perder el trabajo durante la consulta, no para archivarlo.

### D-06 — La autorización la valida el backend al guardar
**Decisión:** la app envía la consulta con el token del escaneo; el backend
decide si el permiso sigue vigente.
**Por qué:** G-04. Validar en la app con una fecha local es falsificable y se
desincroniza. El dueño pudo revocar el QR mientras duraba la consulta.

### D-07 — Guardar requiere conexión
**Decisión:** la consulta no entra al outbox de 005.
**Por qué:** escribe en el historial clínico de una mascota ajena, con una
autorización que puede haber expirado. Encolarla daría por hecha una
autorización que quizá ya no existe.
**Consecuencia:** C-14 comprueba que sin red se explica y no se pierde nada.

### D-08 — Secciones plegables en una sola pantalla
**Decisión:** clínica, receta y cobro son secciones de una misma pantalla, no
un asistente de varios pasos.
**Por qué:** G-05. El veterinario tiene al dueño enfrente y necesita moverse
entre diagnóstico y cobro sin navegar hacia atrás y hacia adelante.

---

## 5. Dependencias nuevas

Ninguna. `Money` viene de 011 y el resto de la infraestructura de 001.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El total mostrado difiere del guardado | Un solo `CalculateChargeUseCase` para pantalla y guardado |
| RT-02 | Redondeo inconsistente entre app y backend | D-02 documentada y comunicada al backend (Q-03) |
| RT-03 | El borrador sobrevive y acumula datos clínicos | D-05 y verificación C-15 en el cierre |
| RT-04 | Un reintento duplica la consulta o el peso | Clave de idempotencia en el guardado; D-04 |
| RT-05 | La pantalla se vuelve pesada con muchos renglones | Lista perezosa y recálculo solo del resumen |
| RT-06 | El servicio nuevo se crea pero la consulta falla | El servicio se crea antes de guardar y es independiente; se documenta |

---

## 7. Puntos de verificación

| Después de | Se verifica |
|---|---|
| F1 | **Cálculo del cobro correcto** (C-05, C-06, C-07, C-08) |
| F2 | Validaciones de guardado probadas (C-10, C-11) |
| F3 | Borrador persiste y se borra al cerrar (C-12) |
| F4 | Peso propagado al carnet una sola vez (C-03); permiso expirado rechazado (C-13) |
| F5 | Clínica y receta completas (C-02, C-04) |
| F6 | Cobro en vivo, servicio nuevo y seguimiento opcional (C-09, C-16); borrador limpio (C-15) |

---

## 8. Lo que este plan NO hace

- No procesa pagos: registra lo cobrado (supuesto 3 de la spec).
- No emite facturas ni CFDI.
- No implementa el seguimiento (015) ni la explicación de la receta (019).
- No edita consultas guardadas (Q-07).
