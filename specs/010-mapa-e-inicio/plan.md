# Plan 010 — Mapa e inicio

> Plan técnico de la unidad `010-mapa-e-inicio`
> Estado: **redactado, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) aprobada
> Alineado con: [`constitution.md`](../../constitution.md) v1.1.0
> Fecha: 29/10/2026

Este documento responde **cómo**. El **qué** y el **por qué** están en la spec.

---

## 1. Principios de diseño

1. **El mapa es una vista, no la app.** La pantalla observa un estado; el
   proveedor de mapas es un detalle sustituible.
2. **El inicio no depende de la red.** El directorio se cachea y el mapa abre
   con lo último visto.
3. **"Lo más próximo" es una fecha, no una distancia.** La regla vive en
   `domain` y se prueba con reloj fijo.
4. **Una sola selección.** Mapa y carrusel comparten el mismo estado; ninguno
   tiene su propia copia.
5. **El demo es un modo, no un atajo.** Usa datos locales y no escribe en el
   backend.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| MI-01 | Modelo de negocio y tipo | `domain/business` | Negocio, tipo cerrado, coordenadas y datos de tarjeta |
| MI-02 | Contrato del directorio | `domain/business` | Observar y refrescar el directorio |
| MI-03 | Próximo evento de salud | `domain/health` | Próxima dosis por mascota y umbral rojo/amarillo con reloj inyectable |
| MI-04 | Cliente del directorio | `data/business/remote` | Endpoints, DTOs y mapeo |
| MI-05 | Caché del directorio | `data/business` | Persistencia local, expiración y aviso de desactualización |
| MI-06 | Repositorio del directorio | `data/repository` | Caché primero, refresco en segundo plano |
| MI-07 | Abstracción del mapa | `ui/map` | Contrato de marcadores, cámara y selección, sin SDK |
| MI-08 | Adaptador del proveedor | `ui/map` + `core/maps` | Implementa el contrato con el SDK elegido en Q-01 |
| MI-09 | Pantalla de inicio | `ui/home` | Mapa, filtros, carrusel, tarjeta de negocio y estados |
| MI-10 | Tarjeta "lo más próximo" | `ui/home` | Pinta el próximo evento y navega al carnet |
| MI-11 | Modo demo | `data/demo` + `di` | Datos locales de demostración sin escrituras |
| MI-12 | Pruebas y dobles | `test` | Directorio falso, mapa falso, reloj fijo y contador de escrituras |

### Lo que se reutiliza de 001 a 008

- `001/K-11` red, `001/K-17` caché, `001/K-18` navegación, `001/K-20`
  componentes y `001/K-21` pruebas.
- `002/ID-06` sesión y `002/ID-08` errores cerrados.
- `003` `PermissionGateway` y `LocationFallback`.
- `007` mascotas del usuario.
- `008/CS-07` alergias y el carnet al que navega la tarjeta; `008` aporta la
  próxima dosis.

---

## 3. Dependencias y orden de construcción

```
MI-01 modelo ──→ MI-02 contrato ──→ MI-06 repositorio ──→ MI-09 pantalla
      │                │                    │                  │
      ├──→ MI-03 salud │                    ├──→ MI-07 mapa ───┤
      ├──→ MI-04 cliente                    ├──→ MI-08 adaptador
      └──→ MI-05 caché ┘                    └──→ MI-10 tarjeta
                                                       │
                                                  MI-11 demo
                                                       │
                                                  MI-12 pruebas
```

### Fases

| Fase | Componentes | Puede ir en paralelo |
|---|---|---|
| **F1 — Dominio** | MI-01, MI-02, MI-03 | MI-12 con MI-03 |
| **F2 — Datos** | MI-04, MI-05, MI-06 | MI-04 con MI-05 |
| **F3 — Mapa** | MI-07, MI-08 | MI-08 depende de Q-01 |
| **F4 — Pantalla** | MI-09, MI-10 | MI-09 con MI-10 |
| **F5 — Demo y cierre** | MI-11, MI-12 | MI-11 con MI-12 |

---

## 4. Decisiones técnicas

### D-01 — El proveedor de mapas queda detrás de una abstracción
**Decisión:** `MapView` recibe marcadores, cámara y selección mediante un
contrato propio; un adaptador traduce al SDK que se elija en Q-01.
**Por qué:** cumple R-14 y desbloquea G-01. El proveedor no está decidido y la
pantalla no puede esperar; con la abstracción se avanza con un mapa falso y se
cambia el adaptador sin tocar la UI.
**Alternativa descartada:** llamar al SDK directamente desde el Composable —
acopla la pantalla a un proveedor y a su licencia.

### D-02 — "Lo más próximo" es una fecha de salud
**Decisión:** `NextHealthEvent` toma la próxima dosis de cada mascota desde
008, elige la de menos días y clasifica el color: rojo si faltan 3 días o
menos, amarillo si faltan 14 o menos, neutro en otro caso.
**Por qué:** cumple R-06, R-07 y G-04. La maqueta usa el mismo patrón
rojo/amarillo para alertas de seguimiento, y esas alertas siempre son fechas.
**Alternativa descartada:** ordenar por distancia al negocio más cercano — no
es lo que muestra la maqueta y confunde dos conceptos distintos.
**Umbrales:** 3 y 14 días son constantes nombradas en `domain`, no números
sueltos en la UI.

### D-03 — El inicio no depende de la red
**Decisión:** el repositorio devuelve la caché de inmediato y refresca en
segundo plano; si el refresco falla, se conserva la caché y se marca
"desactualizado".
**Por qué:** cumple R-09, C-10 y C-11. Un mapa en blanco en la primera pantalla
es el peor primer contacto posible.
**Alternativa descartada:** esperar la respuesta del servidor para pintar — sin
red, la app abre vacía.

### D-04 — Una sola selección entre mapa y carrusel
**Decisión:** el `ViewModel` de inicio guarda `selectedBusinessId`; el mapa y
el carrusel lo leen y lo escriben.
**Por qué:** cumple R-05 y G-06. Dos estados sincronizados a mano se
desincronizan en cuanto alguien olvida una llamada.
**Alternativa descartada:** cada componente con su selección y un observador
cruzado — más código y más formas de fallar.

### D-05 — Ubicación a demanda, con centro por defecto
**Decisión:** el mapa abre con un centro por defecto y no pide nada; "cerca de
mí" dispara el flujo de 003. Si se niega, se usa el centro por defecto y se
ofrece la lista.
**Por qué:** cumple R-08, R-18 y C-09. Pedir ubicación al arrancar es la causa
más común de negativa permanente.
**Alternativa descartada:** pedirla al abrir el mapa sin que el usuario lo
pida — mismo problema, un paso más tarde.

### D-06 — Llave del proveedor por configuración, nunca en el repositorio
**Decisión:** la llave se inyecta por configuración de entorno de 001 y se
restringe por aplicación y por huella del certificado en la consola del
proveedor.
**Por qué:** cumple R-13 y C-14. Una llave en el repositorio es una llave
pública, aunque el repositorio sea privado.
**Alternativa descartada:** dejarla en `BuildConfig` con valor por defecto —
termina versionada en el primer descuido.

### D-07 — Tipos de negocio cerrados, con respaldo genérico
**Decisión:** `BusinessType` es un conjunto cerrado con icono y color; un tipo
desconocido se pinta con el marcador genérico y no rompe el mapa.
**Por qué:** cumple R-02 y R-03, y evita que un tipo nuevo en el backend deje
marcadores invisibles.
**Alternativa descartada:** tipo como texto libre — la UI no sabe qué icono
poner.

### D-08 — La tarjeta de salud consume 008, no recalcula
**Decisión:** `NextHealthEvent` recibe la próxima dosis ya calculada por 008 y
solo elige la más urgente y su color.
**Por qué:** cumple R-06 y evita dos cálculos clínicos distintos. 008 es la
autoridad de la próxima dosis.
**Alternativa descartada:** recalcular la dosis en 010 — dos reglas clínicas y
dos resultados posibles.

### D-09 — Modo demo con datos locales
**Decisión:** un proveedor de datos de demostración alimenta el mapa, la
mascota y el carnet cuando el modo demo está activo; ninguna operación sale
hacia el backend.
**Por qué:** cumple R-11, C-12 y G-05. El demo no debe depender de la red del
aula ni ensuciar datos reales.
**Alternativa descartada:** sembrar datos en producción — irreversible y
visible para otros usuarios.

### D-10 — Sin búsqueda por texto en esta unidad
**Decisión:** solo filtros por tipo y el carrusel. La búsqueda y la ficha
completa son de 013.
**Por qué:** mantiene el alcance y evita duplicar la pantalla de negocio.
**Alternativa descartada:** adelantar la búsqueda — alcance no pedido y otra
pantalla que mantener.

---

## 5. Dependencias a agregar

| Dependencia | Para qué | Autorización |
|---|---|---|
| SDK del proveedor de mapas | Pintar el mapa real | **Bloqueada por Q-01**; se aplica `agregar-dependencia` cuando se decida |

Ninguna otra. Mientras Q-01 siga abierta, las tareas de mapa real usan el
adaptador falso y las pruebas corren sin SDK.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El proveedor se elige tarde y bloquea la entrega | D-01 permite avanzar con el adaptador falso; Q-01 con fecha límite |
| RT-02 | La llave se filtra | D-06 y prueba C-14 |
| RT-03 | El mapa se queda en blanco sin red | D-03 y estados C-10, C-11 |
| RT-04 | La tarjeta de salud muestra un color equivocado por zona horaria | Reloj inyectable y fechas normalizadas a fecha local; C-07 |
| RT-05 | El carrusel y el mapa se desincronizan | D-04 y prueba C-05 |
| RT-06 | El demo escribe en el backend | Proveedor de demo sin escrituras; prueba C-12 con contador en cero |
| RT-07 | El directorio devuelve demasiados negocios y el mapa se traba | Límite y filtros; Q-05 |
| RT-08 | La pantalla de inicio depende de 008 y 008 no está lista | Dependencia explícita en la spec; la tarjeta se construye después de 008 |

---

## 7. Puntos de verificación

| Fase | Verificación |
|---|---|
| F1 | Dominio puro; próxima dosis y umbrales con reloj fijo (C-06, C-07) |
| F2 | Directorio con contrato local; caché y refresco (C-10, C-11) |
| F3 | Adaptador falso del mapa; marcadores y selección (C-02, C-05) |
| F4 | Inicio, filtros, tarjeta de negocio y tarjeta de salud (C-01, C-03, C-04, C-13) |
| F5 | `./gradlew ktlintCheck detekt test`; modo demo y llaves (C-12, C-14) |

---

## 8. Lo que este plan no incluye

- No implementa el alta de negocios ni el pin del afiliado (011).
- No implementa el panel del afiliado (012).
- No implementa la ficha completa del negocio ni el escaneo (013).
- No calcula clínicamente la próxima dosis: la consume de 008.
- No envía notificaciones (006).
- No incluye búsqueda por texto ni navegación giro a giro.

---

## 9. Puerta de aprobación

- [ ] Los componentes y su orden son aceptables.
- [ ] Las decisiones D-01 a D-10 están revisadas.
- [ ] Se confirma que el SDK de mapas queda bloqueado hasta Q-01.
- [ ] Los riesgos técnicos y sus mitigaciones son suficientes.
- [ ] **Aprobado para pasar a `task.md`.**
