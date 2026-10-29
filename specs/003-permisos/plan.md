# Plan 003 — Permisos

> Plan técnico de la unidad `003-permisos`
> Estado: **redactado, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) aprobada
> Alineado con: [`constitution.md`](../../constitution.md) v1.1.0
> Fecha: 29/10/2026

Este documento responde **cómo**. El **qué** y el **por qué** están en la spec.

---

## 1. Principios de diseño

1. **Un solo contrato de permisos.** La UI nunca llama a las APIs de Android
   directamente; habla con un contrato de dominio que se puede probar.
2. **Explicar antes de pedir.** El diálogo del sistema siempre va precedido de
   una explicación propia.
3. **La negativa no es un error.** Es un estado más del `UiState`, con su
   alternativa visible.
4. **Cada permiso tiene un dueño.** Solo la pantalla que lo necesita lo pide;
   ninguna pantalla pide permisos de otra.
5. **Nada al arranque.** El inicio de la app no solicita permisos.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| PM-01 | Catálogo de permisos | `domain/permissions` | Tipo de permiso, momento de uso, justificación y alternativa |
| PM-02 | Contrato `PermissionGateway` | `domain/permissions` | Consultar, solicitar y observar el estado de un permiso |
| PM-03 | Implementación Android | `core/permissions` | Traduce el contrato a las APIs del sistema y al manifiesto |
| PM-04 | Flujo de decisión | `core/permissions` | Cuenta negativas, detecta negativa permanente y decide si abrir ajustes |
| PM-05 | Justificación previa | `ui/components/permissions` | Hoja o diálogo con el motivo antes del diálogo del sistema |
| PM-06 | Componentes de degradación | `ui/components/permissions` | Ubicación, cámara, fotos y notificaciones en su estado negado |
| PM-07 | Lanzador de ajustes | `core/permissions` | Abre los ajustes de la app y informa al volver |
| PM-08 | Pruebas y pantalla de prueba | `test` + `androidTest` | Dobles del gateway y pantalla que demuestra cada degradación |

### Lo que se reutiliza de 001

- `001/K-27` `PermissionController`: se extiende, no se duplica.
- `001/K-20` componentes base (botón, estado vacío, estado de error).
- `001/K-05` `UiText`, `001/K-18` navegación y `001/K-21` infraestructura de
  pruebas.
- `001/K-04` `AppResult` / `AppError` para el resultado de una solicitud.

---

## 3. Dependencias y orden de construcción

```
PM-01 catálogo ──→ PM-02 contrato ──→ PM-03 Android ──→ PM-04 flujo
                          │                                   │
                          └──────────→ PM-08 dobles           ▼
                                                      PM-05 justificación
                                                              │
                                                              ▼
                                                      PM-06 degradación
                                                              │
                                                              ▼
                                                      PM-07 ajustes

PM-08 acompaña desde PM-02 (no va al final)
```

### Fases

| Fase | Componentes | Puede ir en paralelo |
|---|---|---|
| **F1 — Contrato y gateway** | PM-01, PM-02, PM-03, PM-07 | PM-08 con PM-02 |
| **F2 — Flujo** | PM-04, PM-05 | PM-04 con PM-05 |
| **F3 — Degradación** | PM-06 | — (depende de PM-04 y PM-05) |
| **F4 — Cierre** | PM-08 | — |

---

## 4. Decisiones técnicas

### D-01 — Contrato de dominio, no llamadas directas
**Decisión:** `PermissionGateway` declara consultar, solicitar y observar; la
implementación Android vive en `core/permissions` y se inyecta con Hilt.
**Por qué:** permite probar cada degradación con un doble, sin dispositivo, y
cumple P3. Una llamada directa a `ActivityCompat` desde un Composable no se
puede probar ni sustituir.
**Alternativa descartada:** usar las APIs del sistema desde la UI — más corto
de escribir, imposible de probar y repetido en cada pantalla.

### D-02 — Explicación propia antes del diálogo
**Decisión:** `PermissionRationale` se muestra siempre antes de invocar al
sistema, con el motivo y el beneficio concretos. No se muestra si el permiso
ya está concedido.
**Por qué:** cumple R-02 y P10. El diálogo del sistema no permite explicar el
contexto y el usuario tiende a negar.
**Alternativa descartada:** lanzar el diálogo directamente y explicar solo si
el usuario niega — llega tarde y se siente como una excusa.

### D-03 — Selector de fotos del sistema cuando exista
**Decisión:** en las versiones con Photo Picker se usa ese selector y no se
pide permiso de almacenamiento; en las versiones sin él se usa el permiso
clásico como respaldo, solo cuando la pantalla lo necesita.
**Por qué:** menos permisos y menos fricción; es además lo que Google Play
recomienda. El respaldo cubre API 24.
**Alternativa descartada:** pedir siempre el permiso de almacenamiento — pide
más de lo necesario y en Android 13+ ya no aplica igual.
**Pendiente:** Q-05 confirma el corte de versiones.

### D-04 — La degradación es un estado explícito, no un silencio
**Decisión:** cada pantalla que usa un permiso declara en su `UiState` el caso
negado y muestra el componente de degradación correspondiente. Nunca se
oculta la función sin explicar.
**Por qué:** cumple R-05 a R-09. Un botón que desaparece sin explicación se
lee como un defecto.
**Alternativa descartada:** deshabilitar la función y dejar un texto genérico —
no ofrece salida y el usuario no sabe qué hacer.

### D-05 — Dos negativas y luego ajustes
**Decisión:** el flujo cuenta las negativas por permiso. A la segunda, el
siguiente intento abre los ajustes en lugar del diálogo del sistema.
**Por qué:** cumple R-04 y evita el bucle de diálogos que Android ya no
muestra. También cubre a los fabricantes que no reportan bien la negativa
permanente (G-01).
**Alternativa descartada:** confiar solo en `shouldShowRequestPermissionRationale`
— no es fiable en todos los fabricantes.

### D-06 — Ningún permiso en el arranque
**Decisión:** `MainActivity` no solicita permisos. El catálogo declara el
momento de cada uno y solo la pantalla dueña lo dispara.
**Por qué:** cumple R-01 y la revisión de Google Play. Un diálogo al abrir la
app es la causa más común de negativa y de rechazo en la tienda.
**Alternativa descartada:** pedir todo al iniciar — cómodo para el
desarrollador, hostil para el usuario.

### D-07 — Pantalla de prueba para las degradaciones
**Decisión:** se entrega una pantalla de prueba (no de producto) que permite
simular los cuatro permisos concedidos o negados y ver cada alternativa. Las
pantallas reales la consumen después.
**Por qué:** 003 no puede esperar a 007, 010 ni 013 para demostrar que la
degradación funciona; la pantalla de prueba permite verificarlo hoy.
**Alternativa descartada:** probar solo con dobles — no demuestra el
comportamiento real del sistema operativo.

---

## 5. Dependencias a agregar

**Ninguna.** Se usan las APIs del sistema y los componentes ya declarados por
001. Si apareciera una necesidad nueva, se aplica la skill
`agregar-dependencia` y se pide autorización.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El contrato de dominio se queda corto y una pantalla necesita una API que no expone | El contrato cubre consultar, solicitar, observar y abrir ajustes; se amplía con aprobación si falta algo |
| RT-02 | La negativa permanente se detecta mal en algún fabricante | D-05 cuenta negativas y no depende solo de la API del sistema |
| RT-03 | El respaldo sin Photo Picker duplica lógica | Un solo punto decide qué selector usar; las pantallas no lo saben |
| RT-04 | La pantalla de prueba se queda en producción | T-010 la revisa: no se registra en el grafo de navegación de producto |
| RT-05 | Los textos se escriben en los Composables | R-11 y C-10 con `grep` en la puerta de calidad |
| RT-06 | El manifiesto declara permisos sin uso | T-010 compara manifiesto contra catálogo |

---

## 7. Puntos de verificación

| Fase | Verificación |
|---|---|
| F1 | Pruebas del catálogo y del contrato con doble; gateway instrumentado; `grep` de Android en `domain/permissions` sin resultados |
| F2 | Prueba de interfaz: la justificación aparece antes del diálogo; dos negativas llevan a ajustes y los ajustes se abren |
| F3 | Matriz de degradación: los cuatro permisos negados dejan la pantalla usable (C-03…C-06) |
| F4 | `./gradlew ktlintCheck detekt test`; manifiesto sin permisos sin uso; pantalla de prueba fuera del grafo de producto |

---

## 8. Lo que este plan no incluye

- No implementa el mapa (010), el escáner (013), la foto de mascota (007) ni
  el chat (016).
- No define qué se notifica ni cuándo (006).
- No modera contenido (004).
- No incluye el texto legal ni los ajustes de privacidad (017).
- No agrega permisos de micrófono, contactos ni ubicación en segundo plano.

---

## 9. Puerta de aprobación

- [ ] Los componentes y su orden son aceptables.
- [ ] Las decisiones D-01 a D-07 están revisadas.
- [ ] Se confirma que no se agregan dependencias nuevas.
- [ ] Los riesgos técnicos y sus mitigaciones son suficientes.
- [ ] **Aprobado para pasar a `task.md`.**
