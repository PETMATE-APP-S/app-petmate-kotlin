# Spec 018 — ML: identificar la raza por foto

> Unidad de trabajo: `018-ml-raza-por-foto`
> Estado: **redactada, pendiente de aprobación**
> Fecha: 29/10/2026
> Alineada con [`constitution.md`](../../constitution.md) y
> [`MAPA-CAPACIDADES.md`](../MAPA-CAPACIDADES.md)

---

## Supuestos

```
SUPUESTOS QUE ESTOY HACIENDO:
1. Esta unidad cumple el requisito del punto 16: "al menos una función de ML"
   dentro del proyecto móvil.
2. La función es: al registrar una mascota (007), sugerir la raza a partir de
   la foto. Es una SUGERENCIA, no una determinación.
3. SUPUESTO CRÍTICO: el usuario siempre puede ignorar, corregir o escribir la
   raza a mano. El modelo nunca decide por él.
4. La inferencia ocurre EN EL TELÉFONO, no en el servidor. La foto no sale del
   dispositivo para esto.
5. Si el modelo no reconoce nada con confianza suficiente, no inventa: dice
   que no pudo identificarla y ofrece la captura manual.
6. El modelo concreto no está decidido. La spec define el comportamiento
   esperado, no la tecnología.
7. La función es opcional: si el modelo no está disponible o falla, registrar
   una mascota sigue funcionando igual.
8. Se muestran las tres razas más probables con su nivel de confianza, no una
   sola respuesta.
9. Solo se clasifican perros y gatos. Otras especies caen en "no identificada".
10. El tamaño del modelo no debe inflar la app más allá de lo razonable; si
    pesa demasiado, se descarga bajo demanda.
→ Corríjanme antes de aprobar esta spec.
```

---

## 1. Objetivo

**Sugerir la raza de una mascota a partir de su foto**, para que el dueño no
tenga que buscarla en una lista de cientos de opciones al registrarla.

**Por qué:** los lineamientos exigen al menos una función de aprendizaje
automático. Entre las opciones posibles, ésta es la que de verdad ayuda:
mucha gente no sabe con certeza la raza de su perro, y elegir de una lista
larga es la parte más tediosa del registro. Además la foto ya existe en ese
momento, así que no se le pide nada extra al usuario.

**Para quién:** el dueño que está registrando a su mascota por primera vez.

**Cómo se ve el éxito:** el dueño sube la foto de su perro, la app le propone
"Labrador (87%)", él confirma con un toque y sigue adelante.

---

## 2. Alcance

| ID | Capacidad |
|---|---|
| A-01 | Inferencia de raza a partir de la foto de la mascota |
| A-02 | Ejecución en el dispositivo, sin enviar la foto a un servidor |
| A-03 | Presentación de las tres razas más probables con su confianza |
| A-04 | Confirmación de una sugerencia con un toque |
| A-05 | Corrección o captura manual de la raza |
| A-06 | Resultado "no identificada" cuando la confianza es insuficiente |
| A-07 | Funcionamiento sin conexión |
| A-08 | Degradación limpia si el modelo no está disponible |
| A-09 | Indicación clara de que es una sugerencia, no un diagnóstico |

---

## 3. Fuera de alcance

| Qué | Dónde se atenderá |
|---|---|
| Registro de la mascota | Spec 007 |
| Captura y moderación de la foto | Specs 007 y 004 |
| Diagnóstico de salud a partir de la foto | **Fuera del producto**: sería consejo veterinario |
| Identificar la especie (perro o gato) automáticamente | Pregunta abierta Q-04 |
| Estimar edad, peso o condición física | No está en los lineamientos |
| Entrenar o reentrenar el modelo | Fuera del proyecto móvil |
| IA generativa | Spec 019 |

---

## 4. Requisitos

### Funcionales

| ID | Requisito | Trazabilidad |
|---|---|---|
| R-01 | El sistema debe sugerir razas probables a partir de la foto | **Punto 16 (obligatorio)** |
| R-02 | El sistema debe mostrar varias opciones con su nivel de confianza | Usabilidad |
| R-03 | El usuario debe poder aceptar una sugerencia con un toque | Usabilidad |
| R-04 | El usuario debe poder ignorar las sugerencias y escribir la raza | **Supuesto 3** |
| R-05 | El sistema debe indicar que no pudo identificar, en vez de inventar | Honestidad |
| R-06 | El sistema debe advertir que es una sugerencia | Transparencia |

### No funcionales

| ID | Requisito | Trazabilidad |
|---|---|---|
| R-07 | La inferencia debe ejecutarse en el dispositivo | RNF-01; privacidad |
| R-08 | La función debe operar sin conexión | Punto 16 |
| R-09 | La inferencia debe tardar menos de 3 s en un dispositivo de gama media | RNF-15 |
| R-10 | La inferencia no debe bloquear el hilo principal | RNF-15 |
| R-11 | El registro de mascota debe funcionar aunque el modelo falle | Robustez |
| R-12 | El modelo no debe aumentar el tamaño de la app más de lo acordado | Q-03 |

---

## 5. Criterios de éxito

| # | Criterio | Cómo se comprueba |
|---|---|---|
| C-01 | Con la foto de un perro de raza reconocible, la sugerencia correcta aparece entre las tres primeras | Prueba con un conjunto de fotos etiquetadas |
| C-02 | Se muestran hasta tres opciones, cada una con su confianza | Prueba de interfaz |
| C-03 | Tocar una sugerencia la escribe en el campo de raza | Prueba de interfaz |
| C-04 | El usuario puede borrar la sugerencia y escribir otra raza | Prueba de interfaz |
| C-05 | Con una foto que no es de una mascota, el resultado es "no identificada" | Prueba con fotos de objetos y paisajes |
| C-06 | Con confianza por debajo del umbral, no se sugiere nada | Prueba con umbral forzado |
| C-07 | La inferencia tarda menos de 3 s en el dispositivo de referencia | Medición en dispositivo |
| C-08 | La inferencia no bloquea la interfaz: la pantalla sigue respondiendo | Prueba de hilo principal |
| C-09 | **La foto no sale del dispositivo durante la inferencia** | Inspección del tráfico de red durante la operación |
| C-10 | Sin conexión, la sugerencia funciona igual | Prueba con red ausente |
| C-11 | Si el modelo no está disponible, el registro de mascota continúa sin él | Prueba con el modelo deshabilitado |
| C-12 | La pantalla indica que la raza es una sugerencia | Prueba de interfaz |
| C-13 | El aumento del tamaño de la app está dentro de lo acordado | Comparación del artefacto con y sin el modelo |

---

## 6. Riesgos

| # | Riesgo | Impacto | Mitigación |
|---|---|---|---|
| G-01 | El usuario toma la sugerencia como un hecho y registra una raza incorrecta | Medio | R-06 y C-12: se presenta como sugerencia con su confianza; siempre editable |
| G-02 | El modelo infla el tamaño de la app y afecta la descarga | Medio | Q-03 fija el límite; si se excede, descarga bajo demanda (supuesto 10) |
| G-03 | La inferencia es lenta en dispositivos modestos | Medio | R-09 y C-07; si no se cumple, se ejecuta en segundo plano con indicador |
| G-04 | El modelo tiene sesgos y falla con razas mestizas o poco comunes | Medio | Se presentan tres opciones, no una; "no identificada" es un resultado válido |
| G-05 | Alguien espera que la foto diagnostique problemas de salud | **Alto** | Fuera de alcance explícito; la interfaz solo habla de raza |
| G-06 | El modelo elegido exige servicios que no están en todos los dispositivos | Medio | R-11 y C-11: degradación limpia; la decisión va a Q-01 |
| G-07 | La foto se envía a un servicio externo por una implementación descuidada | **Alto** | R-07 y C-09: se inspecciona el tráfico durante la inferencia |

---

## 7. Dependencias

**De esta unidad:**

- `001-arquitectura-base`: dispatchers, errores, componentes.
- `003-permisos`: cámara y fotos.
- `007-mis-mascotas`: la pantalla donde se sugiere la raza.

**Externas:**

- Decisión del modelo y su disponibilidad (Q-01).
- Conjunto de fotos etiquetadas para verificar C-01 (Q-05).

**Unidades posteriores que dependen de esta:**

- Ninguna. Es una función opcional sobre 007.

---

## 8. Preguntas abiertas

| # | Pregunta | Para quién | Bloquea |
|---|---|---|---|
| Q-01 | ¿Qué modelo se usa y funciona sin servicios de Google? | Equipo | Todas las tareas de inferencia |
| Q-02 | ¿Cuál es el umbral mínimo de confianza para mostrar una sugerencia? | Equipo | Lógica de sugerencia |
| Q-03 | ¿Cuánto puede crecer el tamaño de la app por el modelo? ¿Se empaqueta o se descarga? | Equipo / profesor | Empaquetado |
| Q-04 | ¿El modelo distingue perro de gato, o se le indica la especie antes? | Equipo | Alcance de la inferencia |
| Q-05 | ¿De dónde salen las fotos etiquetadas para verificar la precisión? | Equipo | Prueba C-01 |
| Q-06 | ¿Qué precisión mínima se considera aceptable para la entrega? | Profesor / equipo | Criterio de aceptación |
| Q-07 | ¿El catálogo de razas del modelo coincide con el de 007 (su Q-06)? | Equipo | Integración con 007 |

---

## 9. Puerta de aprobación

- [ ] Alcance y fuera de alcance revisados.
- [ ] **Supuestos 3 y 4 confirmados**: es sugerencia y corre en el dispositivo.
- [ ] Preguntas abiertas con dueño, en especial Q-01.
- [ ] **Aprobada para pasar a `plan.md`.**
