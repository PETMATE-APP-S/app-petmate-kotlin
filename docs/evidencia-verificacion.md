# Evidencia de verificación — Estructura SDD

> Comprobación corrida en PowerShell el **27/10/2026** sobre
> `C:\Users\lobop\Desktop\MemoriasClaude\edel\PetMateEdel`

## Qué pidió el profesor y qué se comprobó

| # | Pedido | Resultado | Evidencia |
|---|---|---|---|
| 1 | Archivos de la metodología SDD | ✅ Cumplido | `constitution.md` (10,692 b), `AGENTS.md` (8,526 b), `memoria.md` (6,532 b) |
| 2 | Subagentes | ✅ **5** | `arquitecto`, `dev-android`, `seguridad`, `spec-writer`, `tester` |
| 3 | Skills | ✅ **4** | `agregar-dependencia`, `crear-spec`, `nueva-pantalla`, `revisar-capas` |
| 4 | `spec.md`, `plan.md`, `task.md` de la arquitectura base | ✅ Cumplido | `specs/001-arquitectura-base/` — spec (14,812 b), plan (17,670 b), task (20,960 b) |
| 5 | Sin funcionalidad específica | ✅ Cumplido | La spec lista 17 exclusiones explícitas en "Fuera de alcance" |
| 6 | **No ejecutar las tareas** | ✅ Cumplido | **0** tareas marcadas `[x]` de **32** definidas |
| 7 | Código sin tocar | ✅ Cumplido | **6** archivos `.kt` = el andamio original de Android Studio |
| 8 | Todo alineado al `constitution.md` | ✅ Cumplido | Trazabilidad spec → plan → task; 22 criterios de éxito verificables |

## Comandos y salida

```powershell
PS> cd "C:\Users\lobop\Desktop\MemoriasClaude\edel\PetMateEdel"

PS> ls constitution.md, AGENTS.md, memoria.md
  constitution.md    10692
  AGENTS.md           8526
  memoria.md          6532

PS> ls .claude\agents\, .claude\skills\
  agents/   arquitecto.md  dev-android.md  seguridad.md  spec-writer.md  tester.md
  skills/   agregar-dependencia/  crear-spec/  nueva-pantalla/  revisar-capas/

PS> ls specs\001-arquitectura-base\
  plan.md   17670
  spec.md   14812
  task.md   20960

PS> (Select-String -Path specs\001-arquitectura-base\task.md -Pattern '^- \[ \] \*\*T-0').Count
32

PS> (Select-String -Path specs\001-arquitectura-base\task.md -Pattern '^- \[x\]').Count
0

PS> (Get-ChildItem app\src -Recurse -Filter *.kt).Count
6
```

## Lectura de los números

- **32 / 0** — hay 32 tareas definidas y ninguna ejecutada. Es justo lo que pidió
  el profesor: dejar las tareas listas pero no correrlas.
- **6 archivos .kt** — son los que genera Android Studio al crear el proyecto
  (`MainActivity`, `Color`, `Theme`, `Type` y las dos pruebas de ejemplo). No
  existen todavía las carpetas `domain/`, `data/` ni `ui/` implementadas, lo que
  confirma que no se escribió código de la arquitectura.

## Nota sobre OpenCode

Se intentó correr esta auditoría dentro de **OpenCode** (la herramienta que
prefiere el profesor), pero la cuenta devolvió
`Error: Free usage limit reached` incluso con un mensaje de una palabra y
después de cambiar de modelo (Big Pickle → DeepSeek V4.1 Flash Free). El
bloqueo es de la cuenta, no del modelo ni del proyecto.

La verificación se hizo entonces en PowerShell, que da la misma evidencia sin
depender de ningún servicio externo. El prompt para OpenCode queda listo en
[`prompt-auditoria-opencode.md`](prompt-auditoria-opencode.md) para cuando la
cuota se restablezca.
