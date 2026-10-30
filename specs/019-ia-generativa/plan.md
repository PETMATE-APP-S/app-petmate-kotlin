# Plan 019 — IA generativa: explicar la receta y resumir el historial

> Plan técnico de la unidad `019-ia-generativa`
> Estado: **redactado, pendiente de aprobación**
> Depende de [`spec.md`](spec.md) aprobada

Este documento responde **cómo**. El **qué** está en la spec.

---

## 1. Principios aplicados

- `ui → domain ← data`; el dominio de la generación es Kotlin puro.
- La llamada al proveedor la hace el backend; la app solo habla con el backend
  (constitución P6).
- El contenido original del veterinario es el dato primario; lo generado es
  derivado y desechable.
- La función degrada sin romper las pantallas que la hospedan.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| IA-01 | `GeneratedText` | domain/model | Texto generado con su origen y fecha |
| IA-02 | `GenerationRequest` | domain/model | Qué se pide: explicar receta o resumir historial |
| IA-03 | `GenerationResult` | domain/model | Generado, no disponible o fallido |
| IA-04 | `AiRepository` | domain/repository | Contrato |
| IA-05 | `ExplainPrescriptionUseCase` | domain/usecase | Explicación con caché |
| IA-06 | `SummarizeHistoryUseCase` | domain/usecase | Resumen con caché |
| IA-07 | DTOs y mappers | data/remote | Transporte |
| IA-08 | `AiApiService` | data/remote | Llamada **al backend**, no al proveedor |
| IA-09 | `GeneratedTextEntity` y DAO | data/local | Caché del resultado |
| IA-10 | `AiRepositoryImpl` | data/repository | Caché primero, red después |
| IA-11 | `AiExplanationCard` | ui/ia | Tarjeta con el texto generado |
| IA-12 | `AiDisclaimer` | ui/ia | Marca de IA y advertencia |
| IA-13 | `OriginalTextSection` | ui/ia | Texto del veterinario, siempre visible |

**Se reutiliza sin duplicar:** red, caché y errores de 001; receta de 014;
historial de 008; el aviso de privacidad de 017.

---

## 3. Orden de construcción

| Fase | Componentes | Entrega |
|---|---|---|
| F1 Dominio | IA-01 … IA-04 | Modelos y contrato |
| F2 Datos | IA-07, IA-08, IA-09, IA-10 | Transporte y caché |
| F3 Casos de uso | IA-05, IA-06 | Explicación y resumen con caché |
| F4 Interfaz | IA-11, IA-12, IA-13 | Presentación con original y advertencia |
| F5 Verificación | — | Privacidad, degradación y contenido |

---

## 4. Decisiones

### D-01 — La app llama al backend, nunca al proveedor
**Decisión:** `AiApiService` apunta a un endpoint propio del backend. No
existe ningún cliente del proveedor de IA en la app, ni ninguna llave.
**Por qué:** G-03 y la constitución P6. Una llave de proveedor en un APK se
extrae en minutos y alguien más gasta la cuota del proyecto. Es la misma
decisión que tomó 004 para la moderación, por la misma razón.
**Consecuencia:** C-08 y C-09 lo verifican con revisión de código y de
tráfico.

### D-02 — El texto original es el dato primario
**Decisión:** `OriginalTextSection` se pinta siempre; la tarjeta generada se
monta debajo y se puede contraer.
**Por qué:** G-01, el riesgo crítico. Si la IA altera una dosis y el original
no está a la vista, nadie puede detectar el error. El generado es una ayuda de
lectura, no la fuente.
**Alternativa descartada:** sustituir el original por la explicación — una
alucinación se volvería indetectable.

### D-03 — La advertencia no se puede descartar
**Decisión:** `AiDisclaimer` acompaña a todo texto generado y no tiene cierre
permanente.
**Por qué:** G-02 y R-04. El usuario debe saber siempre que lo está leyendo de
una máquina y que ante dudas pregunta al veterinario.

### D-04 — Caché por contenido, no por tiempo
**Decisión:** el resultado se guarda asociado al identificador y a una huella
del contenido original. Si la receta no cambió, no se regenera.
**Por qué:** G-04 y R-09. Una caché por tiempo regeneraría lo mismo cada
cierto rato y gastaría cuota sin motivo. Si el veterinario corrige la receta,
la huella cambia y se regenera sola.
**Consecuencia:** C-06 cuenta las llamadas; C-07 comprueba que regenerar
manualmente sí fuerza una nueva.

### D-05 — Las alergias quedan fuera del texto generado
**Decisión:** el resumen no incluye las alergias; éstas se siguen mostrando
con el componente destacado de 008.
**Por qué:** G-08. Si el resumen omitiera o suavizara una alergia, el daño
sería grave. El dato crítico se muestra tal cual, sin pasar por la IA.

### D-06 — La interfaz no ofrece preguntas abiertas
**Decisión:** solo hay dos acciones: explicar esta receta y resumir este
historial. No hay campo de texto libre hacia la IA.
**Por qué:** G-02 y C-14. Un campo abierto convierte la función en una
consulta veterinaria, que es justo lo que no debe ser.

### D-07 — Degradación al original
**Decisión:** si la generación falla o no hay red, se muestra el original con
un aviso discreto; la pantalla no muestra error bloqueante.
**Por qué:** R-10 y G-06. La receta se debe poder leer siempre, aunque el
proveedor esté caído.

---

## 5. Dependencias nuevas

Ninguna en la app. El proveedor de IA lo integra el backend.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | Alguien agrega un cliente del proveedor en la app | D-01 y revisión del agente `seguridad` en el cierre |
| RT-02 | La caché devuelve una explicación de una receta ya corregida | D-04: huella del contenido, no solo identificador |
| RT-03 | El texto generado se guarda y se confunde con el original | El modelo marca el origen; la interfaz los separa visualmente |
| RT-04 | Datos clínicos viajan a un tercero sin declararlo | Q-04 y declaración en el aviso de privacidad de 017 |
| RT-05 | La generación lenta deja la pantalla colgada | D-07 y tiempo límite con cancelación (C-13) |
| RT-06 | La caché crece sin límite | Se limpia junto con los datos de la mascota y en el borrado de cuenta (017) |

---

## 7. Puntos de verificación

| Después de | Se verifica |
|---|---|
| F1 | Modelos distinguen generado, no disponible y fallido |
| F2 | **Solo hay llamadas al backend** (C-09); caché por huella (C-06) |
| F3 | Regenerar fuerza una nueva llamada (C-07) |
| F4 | Original visible, marca de IA y advertencia (C-02, C-03, C-04) |
| F5 | Sin llaves en el repositorio (C-08); degradación (C-10, C-11) |

---

## 8. Lo que este plan NO hace

- No integra ningún proveedor de IA en la app: eso es del backend.
- No diagnostica, no recomienda tratamientos ni ajusta dosis.
- No ofrece un chat ni preguntas abiertas.
- No sustituye el texto del veterinario.
