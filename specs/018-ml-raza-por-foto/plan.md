# Plan 018 — ML: identificar la raza por foto

> Plan técnico de la unidad `018-ml-raza-por-foto`
> Estado: **redactado, pendiente de aprobación**
> Depende de [`spec.md`](spec.md) aprobada

Este documento responde **cómo**. El **qué** está en la spec.

---

## 1. Principios aplicados

- `ui → domain ← data`; el contrato del clasificador se declara en el dominio
  y la implementación concreta vive en `data`.
- La inferencia ocurre en el dispositivo: la foto no viaja.
- La función es un añadido opcional sobre 007: si falla, 007 sigue completo.
- El modelo concreto se decide en la tarea, no en el plan (Q-01 abierta).

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| ML-01 | `BreedSuggestion` | domain/model | Raza propuesta con su confianza |
| ML-02 | `BreedClassifier` | domain/classifier | **Interfaz**: foto → sugerencias |
| ML-03 | `ClassificationResult` | domain/model | Sugerencias, no identificada o no disponible |
| ML-04 | `SuggestBreedUseCase` | domain/usecase | Aplica el umbral y ordena |
| ML-05 | `BreedClassifierImpl` | data/classifier | Implementación concreta con el modelo |
| ML-06 | `ModelAvailability` | data/classifier | Comprueba si el modelo está listo |
| ML-07 | `BreedSuggestionChips` | ui/mascotas | Sugerencias tocables en 007 |
| ML-08 | `SuggestionDisclaimer` | ui/mascotas | Aviso de que es sugerencia |

**Se reutiliza sin duplicar:** dispatchers y errores de 001; permisos de 003;
la pantalla y el campo de raza de 007.

---

## 3. Orden de construcción

| Fase | Componentes | Entrega |
|---|---|---|
| F1 Contrato | ML-01, ML-02, ML-03, ML-04 | Interfaz y reglas, sin modelo |
| F2 Modelo | ML-05, ML-06 | Implementación concreta |
| F3 Interfaz | ML-07, ML-08 | Integración en 007 |
| F4 Verificación | — | Precisión, rendimiento y privacidad |

**F1 se construye sin el modelo a propósito:** con un clasificador doble se
prueban el umbral, el orden y la degradación antes de pelear con la
tecnología.

---

## 4. Decisiones

### D-01 — El clasificador es una interfaz del dominio
**Decisión:** `BreedClassifier` se declara en `domain` y recibe la imagen como
un tipo propio, no como un `Bitmap` de Android.
**Por qué:** Q-01 sigue abierta. Si el modelo se decide después o se cambia,
la pantalla y las reglas no se tocan. Además permite probar F1 con un doble.
**Consecuencia:** el dominio sigue siendo Kotlin puro pese a ser una unidad de
aprendizaje automático.

### D-02 — La inferencia corre en el dispositivo
**Decisión:** la implementación usa un modelo local; no existe ninguna llamada
de red en el camino de la inferencia.
**Por qué:** R-07 y G-07. Mandar la foto de la mascota de alguien a un
servicio externo para adivinar la raza es un costo de privacidad que esta
función no justifica. Además R-08 exige que funcione sin conexión.
**Consecuencia:** C-09 inspecciona el tráfico durante la operación para
comprobar que nada sale.

### D-03 — "No identificada" es un resultado de primera clase
**Decisión:** `ClassificationResult` distingue tres casos: sugerencias,
no identificada y modelo no disponible. No se devuelve una lista vacía.
**Por qué:** R-05 y G-04. Los tres casos se le explican distinto al usuario:
"no la reconocí", "esta función no está disponible" y "aquí van tres
opciones" no son lo mismo.

### D-04 — El umbral se aplica en el dominio
**Decisión:** `SuggestBreedUseCase` descarta lo que no llega al umbral y
limita a tres resultados.
**Por qué:** Q-02 sigue abierta y el umbral se va a ajustar con pruebas. En el
dominio se cambia en un lugar y se prueba sin dispositivo.

### D-05 — La función degrada sin romper 007
**Decisión:** si el modelo no está, `ModelAvailability` lo reporta y la
pantalla de 007 oculta las sugerencias; el campo de raza sigue funcionando.
**Por qué:** R-11 y G-06. El registro de mascota es obligatorio para el
producto; la sugerencia de raza es un añadido. El añadido no puede tumbar lo
obligatorio.

### D-06 — Inferencia fuera del hilo principal, con indicador
**Decisión:** la inferencia corre en el dispatcher inyectado y la pantalla
muestra que está pensando.
**Por qué:** R-10 y G-03. En un dispositivo modesto la inferencia puede tardar
segundos; sin indicador parece que la app se congeló.

### D-07 — El empaquetado del modelo se decide con una medición
**Decisión:** se mide el tamaño del artefacto con y sin el modelo antes de
decidir si se empaqueta o se descarga bajo demanda.
**Por qué:** G-02 y Q-03. Decidirlo sin el número es adivinar.

---

## 5. Dependencias nuevas

Una librería de inferencia en el dispositivo, **pendiente de decidir**
(Q-01). Se pide con la skill `agregar-dependencia` antes de tocar el catálogo.
El plan no presupone cuál, y la interfaz de D-01 permite cambiarla.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | La librería elegida exige servicios no disponibles en todos los dispositivos | D-01 y D-05: interfaz intercambiable y degradación limpia |
| RT-02 | La foto viaja a un servicio externo sin notarlo | D-02 y prueba C-09 sobre el tráfico |
| RT-03 | El modelo infla el artefacto | D-07: medición antes de decidir; C-13 |
| RT-04 | El catálogo de razas del modelo no coincide con el de 007 | Q-07; se mapea o se acepta texto libre |
| RT-05 | La precisión real es menor que la esperada | C-01 con fotos etiquetadas; Q-06 fija el mínimo aceptable |
| RT-06 | La inferencia agota la batería si se repite | Se infiere una vez por foto, no de forma continua |

---

## 7. Puntos de verificación

| Después de | Se verifica |
|---|---|
| F1 | Umbral, orden y tres resultados probados con un clasificador doble (C-02, C-06) |
| F2 | El modelo responde; "no identificada" funciona (C-05); tamaño medido (C-13) |
| F3 | Sugerencias tocables y editables en 007 (C-03, C-04, C-12) |
| F4 | **Nada sale del dispositivo** (C-09); tiempo y fluidez (C-07, C-08); degradación (C-11) |

---

## 8. Lo que este plan NO hace

- No entrena ni reentrena ningún modelo.
- No diagnostica salud a partir de la foto.
- No identifica la especie automáticamente (Q-04).
- No elige todavía la tecnología de inferencia (Q-01).
