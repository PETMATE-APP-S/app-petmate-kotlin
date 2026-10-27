# Prompt de auditoría para OpenCode (terminal de Warp)

Este archivo guarda el prompt que se pega en OpenCode para que audite la
estructura de Spec Driven Development de este repositorio.

**Para qué sirve:** tener la revisión hecha en OpenCode (que es la herramienta
que prefiere el profe) y dejar la conversación como evidencia.

**Importante:** es un prompt de **auditoría**, no de implementación. Mientras el
profesor no autorice ejecutar las tareas de `task.md`, OpenCode no debe escribir
código.

---

## Cómo usarlo

1. Abre Warp en la carpeta del proyecto:
   ```
   cd C:\Users\lobop\Desktop\MemoriasClaude\edel\PetMateEdel
   ```
2. Inicia OpenCode:
   ```
   opencode
   ```
3. Pega el prompt de abajo completo (va entre las líneas de guiones).

---

## PROMPT 1 — Auditoría de la estructura SDD

```
Estás en el repositorio de la app móvil Android de PetMate, en la carpeta
C:\Users\lobop\Desktop\MemoriasClaude\edel\PetMateEdel

CONTEXTO
Este proyecto se desarrolla con la metodología Spec Driven Development (SDD) y
DevSecOps, usando agentes de IA. El profesor pidió para la siguiente clase:

1. Haber creado los archivos de la metodología SDD: constitution.md, AGENTS.md,
   memoria.md, subagentes y skills.
2. Haber creado spec.md, plan.md y task.md para la ARQUITECTURA BASE del
   proyecto, SIN incluir funcionalidad específica de lo que tratará el proyecto.
3. AÚN NO EJECUTAR LAS TAREAS.
4. Que todos los archivos estén alineados y coherentes con constitution.md.

TU TRABAJO: AUDITAR, NO IMPLEMENTAR
Eres un auditor. Verificas si lo anterior se cumplió y reportas. No escribes ni
modificas código, no creas archivos, no ejecutas ninguna tarea de task.md. Si
detectas algo que falta o está mal, lo REPORTAS; no lo arreglas por tu cuenta.

PASO 1 — Lee estos archivos en este orden
  constitution.md
  AGENTS.md
  memoria.md
  .claude/agents/          (los 5 archivos)
  .claude/skills/          (los 4 SKILL.md)
  specs/001-arquitectura-base/spec.md
  specs/001-arquitectura-base/plan.md
  specs/001-arquitectura-base/task.md

PASO 2 — Verifica que exista todo lo que pidió el profe
Haz una tabla: elemento pedido | existe sí/no | ruta | observación.

PASO 3 — Verifica la coherencia con constitution.md
Esto es lo que más pesa en la calificación. Revisa y da evidencia (cita el
archivo y la línea) de cada punto:

  a) El stack que declara constitution.md §2 es el mismo que usa plan.md.
     ¿Hay alguna tecnología en el plan que la constitución no autorice?
  b) La estructura de carpetas de constitution.md §3 es la misma que proponen
     plan.md y task.md.
  c) El principio P3 (dirección de dependencias ui -> domain <- data) se
     respeta en el diseño del plan. ¿El plan propone en algún punto que ui
     dependa de data, o que domain conozca algún framework?
  d) Los límites de constitution.md §4 (Siempre / Preguntar primero / Nunca)
     se respetan en task.md. Ejemplo: agregar dependencias y cambiar el
     esquema de Room están en "Preguntar primero"; ¿las tareas que hacen eso
     lo advierten?
  e) Los umbrales de calidad de constitution.md §5 (cobertura domain >= 80%,
     data >= 70%, ViewModels 100%) aparecen verificados en alguna tarea.
  f) AGENTS.md no contradice a constitution.md en ningún punto.
  g) Los 5 subagentes y las 4 skills son coherentes con la constitución y no
     se contradicen entre sí.

PASO 4 — Verifica la trazabilidad spec -> plan -> task
  - ¿Cada capacidad del alcance de spec.md tiene componentes en plan.md?
  - ¿Cada componente de plan.md tiene al menos una tarea en task.md?
  - ¿Cada criterio de éxito de spec.md (son 22, de C-01 a C-22) se verifica en
    alguna tarea de task.md? Lista los que NO estén cubiertos.
  - ¿Cada pregunta abierta de spec.md (Q-01 a Q-07) apunta a una tarea que de
    verdad existe y que de verdad depende de esa respuesta?
  - ¿Las dependencias entre tareas de task.md forman un orden válido, sin
    ciclos y sin que una tarea dependa de otra posterior?

PASO 5 — Verifica que NO haya funcionalidad específica
El profe fue explícito: la arquitectura base NO debe incluir funcionalidad del
producto. Busca en spec.md, plan.md y task.md si se colaron cosas como: mapa,
marcadores, mascotas, carnet, vacunas, alergias, QR, escáner, chat, consultas,
cobro, afiliación, seguimiento, login, registro, ML, IA generativa.

Ojo: mencionarlas en la sección "Fuera de alcance" es CORRECTO y deseable.
El problema sería que aparezcan como algo a construir en esta unidad.
Distingue claramente entre las dos cosas.

PASO 6 — Verifica que NO se haya ejecutado nada
  - ¿Hay tareas marcadas como [x] en task.md? (debe haber 0)
  - ¿El código en app/src/ sigue siendo solo el andamio que genera Android
    Studio, sin capas domain/data/ui implementadas?
  - Cuenta los archivos .kt que existen y di cuáles son.

PASO 7 — Reporta
Entrega:
  1. Veredicto: ¿se cumplió lo que pidió el profesor? Sí / No / Parcial.
  2. Tabla del PASO 2.
  3. Hallazgos de coherencia, ordenados por gravedad (Alta / Media / Baja),
     cada uno con archivo, línea y por qué importa.
  4. Huecos de trazabilidad, si hay.
  5. Lo que está bien hecho (sé específico, no genérico).
  6. Qué falta por hacer antes de la siguiente clase.

REGLAS
  - No modifiques ningún archivo.
  - No ejecutes ninguna tarea de task.md.
  - No agregues dependencias ni corras builds que cambien algo.
  - Cita evidencia (archivo y línea) en cada hallazgo. No opines sin evidencia.
  - Si no encuentras problemas en algo, dilo y di qué revisaste. No inventes
    hallazgos para parecer útil.
  - Si algo te parece ambiguo, dilo como pregunta abierta en lugar de suponer.
```

---

## PROMPT 2 — Si quieres que OpenCode explique la metodología

Útil si el profe pregunta en clase "¿qué es cada archivo?" y quieres la
explicación generada ahí mismo.

```
Lee constitution.md, AGENTS.md, memoria.md, los agentes de .claude/agents/, las
skills de .claude/skills/ y los tres archivos de
specs/001-arquitectura-base/.

Explícame, en español y en una página:
  1. Qué función cumple cada archivo dentro de Spec Driven Development y por
     qué el orden spec -> plan -> task importa.
  2. Qué significa que todo esté "alineado al constitution.md" y da dos
     ejemplos concretos de este repositorio.
  3. Para qué sirve cada uno de los 5 subagentes y de las 4 skills, y en qué
     momento del trabajo se usaría cada uno.
  4. Por qué esta unidad de trabajo no incluye funcionalidad del producto.

No modifiques nada. Es solo explicación.
```

---

## PROMPT 3 — Comprobaciones rápidas de terminal (sin agente)

Si solo quieres ver los números en pantalla, en Warp:

```bash
# Los archivos de la metodología existen
ls constitution.md AGENTS.md memoria.md
ls .claude/agents/ .claude/skills/
ls specs/001-arquitectura-base/

# 0 tareas ejecutadas (no debe imprimir nada)
grep "^- \[x\]" specs/001-arquitectura-base/task.md

# Cuántas tareas hay definidas (debe dar 32)
grep -c "^- \[ \] \*\*T-0" specs/001-arquitectura-base/task.md

# El código sigue siendo solo el andamio (debe dar 6)
find app/src -name "*.kt" | wc -l

# Las capas aún no se implementan (no debe existir)
ls app/src/main/java/com/example/petmateedel/domain 2>/dev/null || echo "domain/ no existe todavía: correcto, no se ha ejecutado nada"
```

---

## PROMPTS CORTOS — si se acaba la cuota del modelo

El PROMPT 1 completo hace leer ~2,300 líneas de una sola vez y consume mucho.
Si tu modelo tiene límite, usa estos tres por separado. Cada uno es barato y,
si se corta, no pierdes el avance de los anteriores.

### Corto A — ¿Existe todo? (el más barato)

```
Estás en el repo de la app móvil de PetMate. NO modifiques nada, solo verifica.

Lista qué existe de esto y en qué ruta:
  constitution.md, AGENTS.md, memoria.md
  .claude/agents/ (deben ser 5)
  .claude/skills/ (deben ser 4)
  specs/001-arquitectura-base/spec.md, plan.md, task.md

Hazlo con ls, sin abrir el contenido de los archivos.
Devuelve una tabla: elemento | existe | ruta.
```

### Corto B — ¿Está alineado al constitution?

```
Lee SOLO estos tres archivos: constitution.md, specs/001-arquitectura-base/plan.md
y specs/001-arquitectura-base/task.md. No leas los demás. No modifiques nada.

Verifica y cita archivo y línea en cada punto:
  a) El stack de constitution.md §2 es el mismo que usa plan.md.
  b) La estructura de carpetas de constitution.md §3 coincide con la del plan.
  c) El principio P3 (ui -> domain <- data) se respeta: el plan nunca hace que
     ui dependa de data, ni que domain conozca un framework.
  d) Lo que constitution.md §4 marca como "Preguntar primero" (agregar
     dependencias, cambiar esquema de Room, tocar el CI) está advertido en las
     tareas que lo hacen.

Reporta solo contradicciones reales, con evidencia. Si algo está bien, dilo.
No inventes hallazgos.
```

### Corto C — ¿Se respetó lo que pidió el profe?

```
Lee SOLO specs/001-arquitectura-base/spec.md y task.md. No modifiques nada.

1. El profe pidió arquitectura base SIN funcionalidad específica. Busca si se
   coló algo como mapa, mascotas, carnet, QR, chat, cobro, login, ML o IA
   generativa COMO ALGO A CONSTRUIR en esta unidad.
   OJO: si aparecen en la sección "Fuera de alcance", eso es CORRECTO y es lo
   que se busca. Distingue bien entre las dos cosas y no lo reportes como error.

2. El profe pidió NO ejecutar las tareas. Verifica:
   - ¿Cuántas tareas están marcadas [x]? (debe ser 0)
   - ¿Cuántos archivos .kt hay en app/src? (debe ser 6, el andamio de Android
     Studio; si hubiera carpetas domain/ data/ ui/ implementadas, sí se ejecutó)

Reporta: ¿se cumplió lo que pidió el profe? Sí / No / Parcial, con evidencia.
```

---

## Notas sobre OpenCode (verificadas en opencode.ai/docs, 27/10/2026)

| Qué | Cómo |
|---|---|
| Conectar un proveedor | `/connect` dentro de OpenCode (NO es `opencode auth login`) |
| Cambiar de modelo | `/models` |
| Ver qué credenciales hay | `opencode auth list` |
| Dónde se guardan | `~/.local/share/opencode/auth.json` |
| Modo Plan / Build | tecla `Tab` |

**La suscripción de Claude Pro/Max NO se puede usar en OpenCode.** Las docs
dicen textual: *"Anthropic explicitly prohibits this"*. Los plugins que lo
hacían se quitaron desde la versión 1.3.0. Solo sirve una API key de
console.anthropic.com.

**Si se acaba la cuota:** las docs dicen *"If you reach the usage limit, you can
continue using the free models."* Los gratuitos son **Beam Free** e **Iceman
Contributor Free** (ilimitados por tiempo limitado). Se eligen con `/models`.
Iceman Contributor Free es gratis a cambio de que usen tus prompts para
entrenar el modelo.

**OpenCode Go** cuesta $10/mes (Go) o $40/mes (Go Plus). No hace falta para
auditar documentos.
