# AGENTS.md — Reglas operativas para agentes de IA

> Este archivo le dice a cualquier agente de IA (Claude Code, OpenCode, Warp,
> Copilot…) cómo trabajar en este repositorio.
>
> **Lee primero [`constitution.md`](constitution.md).** Es la autoridad máxima.
> Este archivo explica *cómo operar*; la constitución define *qué es permitido*.
> Si los dos se contradicen, gana la constitución y este archivo se corrige.

---

## 1. Qué es este proyecto

Aplicación móvil Android de **PetMate**: carnet digital de salud de mascotas,
QR para compartir el historial y mapa de veterinarias, tiendas y estéticas
afiliadas. Consume la API REST del repositorio hermano
`backend-petmate-nosdd-aspnetcore` (ASP.NET Core) bajo `/api/v1`.

Se desarrolla con **Spec Driven Development**: nada se codifica sin spec
aprobada.

---

## 2. Antes de hacer cualquier cosa

Lee, en este orden:

1. `constitution.md` — principios, stack, límites.
2. `memoria.md` — en qué estado va el proyecto y qué se decidió.
3. La spec de la unidad de trabajo que te toca, en `specs/<id>/spec.md`.
4. Su `plan.md` y su `task.md`.

No empieces a escribir código basándote solo en el prompt del usuario. Si el
prompt pide algo que ninguna spec cubre, **detente y dilo**.

---

## 3. El ciclo de trabajo

```
   SPECIFY  ──→  PLAN  ──→  TASK  ──→  IMPLEMENT
      │           │         │            │
      ▼           ▼         ▼            ▼
   aprueba     aprueba   aprueba      verifica
   el humano   el humano el humano    el humano
```

Cada flecha es una **puerta cerrada**. No la cruces sin un "sí" explícito del
humano. Avanzar sin aprobación es el error más grave que puedes cometer aquí.

### Fase SPECIFY — `spec.md`
Define **qué** y **por qué**. Sin decisiones de implementación, sin nombres de
clases. Antes de escribirla, enumera en voz alta tus supuestos:

```
SUPUESTOS QUE ESTOY HACIENDO:
1. ...
2. ...
→ Corrígeme ahora o avanzo con esto.
```

Toda spec cubre: objetivo, alcance, fuera de alcance, requisitos
(trazables a `RF-xx` / `RNF-xx`), criterios de éxito verificables, riesgos y
preguntas abiertas.

### Fase PLAN — `plan.md`
Define **cómo**, en términos técnicos: componentes, dependencias entre ellos,
orden de construcción, decisiones de arquitectura con su justificación y
alternativas descartadas, riesgos con mitigación, puntos de verificación.

### Fase TASK — `task.md`
Tareas discretas y ordenadas por dependencia. Cada tarea trae:

```markdown
- [ ] T-00X — <descripción en una línea>
  - Depende de: T-00Y
  - Criterio de aceptación: <qué debe ser cierto al terminar>
  - Verificación: <comando exacto o comprobación manual>
  - Archivos: <rutas que se van a tocar>
```

Reglas de tamaño: una tarea se termina en una sesión enfocada y toca a lo más
~5 archivos. Si no cabe, pártela.

### Fase IMPLEMENT
Una tarea a la vez, en el orden de `task.md`. Por cada tarea:

1. Vuelve a leer la tarea y su criterio de aceptación.
2. Escribe la prueba primero cuando la tarea tenga lógica verificable.
3. Implementa lo mínimo que cumple el criterio. Nada extra.
4. Corre la verificación de la tarea.
5. Corre `./gradlew ktlintCheck detekt test` antes de darla por buena.
6. Marca `[x]` **solo si la verificación pasó de verdad**.
7. Anota lo relevante en `memoria.md`.
8. Detente y reporta. No sigas con la siguiente tarea por tu cuenta.

---

## 4. Reglas de código

### Dirección de dependencias (lo más importante)

```
ui  ──→  domain  ←──  data
```

- `domain` es Kotlin puro. Si un archivo en `domain` importa `android.*`,
  `androidx.*`, Room, Retrofit o Compose, está mal. Corrígelo.
- `ui` nunca importa de `data`. Habla con `domain`.
- `data` implementa interfaces declaradas en `domain`.

### Convenciones

| Elemento | Convención | Ejemplo |
|---|---|---|
| Clases, interfaces | `PascalCase` | `PetRepository` |
| Funciones, variables | `camelCase` | `loadPets()` |
| Composables | `PascalCase` | `PetCard()` |
| Constantes | `UPPER_SNAKE_CASE` | `MAX_RETRIES` |
| Entidades de Room | sufijo `Entity` | `PetEntity` |
| DTOs de red | sufijo `Dto` | `PetDto` |
| Modelos de dominio | sin sufijo | `Pet` |
| Casos de uso | verbo + `UseCase` | `GetPetsUseCase` |
| Implementaciones | prefijo del nombre | `PetRepositoryImpl` |
| Archivos de prueba | sufijo `Test` | `PetRepositoryTest` |

### Estilo esperado

```kotlin
// domain/repository/PetRepository.kt — Kotlin puro, sin framework
interface PetRepository {
    fun observePets(): Flow<List<Pet>>
    suspend fun refreshPets(): Result<Unit>
}

// ui/pets/PetsViewModel.kt — un StateFlow inmutable, eventos hacia adentro
@HiltViewModel
class PetsViewModel @Inject constructor(
    private val getPets: GetPetsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    fun onRetryClicked() {
        viewModelScope.launch { /* ... */ }
    }
}

data class PetsUiState(
    val isLoading: Boolean = false,
    val pets: List<Pet> = emptyList(),
    val error: UiText? = null,
)
```

### Obligatorio
- Errores modelados con un tipo `Result`/`sealed class` del proyecto. **Nunca**
  lances excepciones a la UI ni uses `try/catch` vacíos.
- I/O siempre fuera del hilo principal, con el dispatcher inyectado (nunca
  `Dispatchers.IO` escrito a mano dentro de una clase: imposible de probar).
- Textos de usuario en `strings.xml`. Cero cadenas literales en Composables.
- Todo lo público que no sea obvio lleva KDoc.
- Logging con el logger del proyecto. `println` está prohibido.

---

## 5. Comandos

```bash
./gradlew build                  # compila todo
./gradlew assembleDebug          # APK de depuración
./gradlew bundleRelease          # AAB para Google Play
./gradlew test                   # pruebas unitarias (JVM)
./gradlew connectedAndroidTest   # pruebas instrumentadas (requiere dispositivo)
./gradlew lint                   # Android Lint
./gradlew ktlintCheck            # estilo
./gradlew ktlintFormat           # corrige estilo
./gradlew detekt                 # análisis estático
./gradlew clean                  # limpia
```

En Windows usa `gradlew.bat`.

**Puerta de calidad antes de cerrar cualquier tarea:**
```bash
./gradlew ktlintCheck detekt test
```

---

## 6. Qué hacer cuando algo se atora

| Situación | Qué haces |
|---|---|
| La spec no cubre lo que te piden | Te detienes y lo dices. No improvisas. |
| La spec se contradice con la constitución | Gana la constitución. Lo reportas. |
| Necesitas una dependencia nueva | Preguntas. No la agregas. |
| Una prueba falla | Arreglas la causa. Jamás borras ni saltas la prueba. |
| No sabes el valor de una configuración | Preguntas. No inventas un valor plausible. |
| El build se rompe por algo ajeno a tu tarea | Lo reportas antes de seguir. |
| Encuentras un defecto fuera de tu tarea | Lo anotas en `memoria.md`. No lo arreglas sin permiso. |

**Regla general: ante la duda, pregunta. Un agente que pregunta cuesta un
minuto; un agente que supone cuesta un día.**

---

## 7. Subagentes y skills

- `.claude/agents/` — especialistas a los que delegar (arquitecto, desarrollador
  Android, tester, revisor de seguridad, escritor de specs).
- `.claude/skills/` — procedimientos reutilizables (crear una spec, crear una
  feature de UI, revisar capas, agregar una dependencia).

Antes de resolver una tarea de forma artesanal, revisa si ya existe una skill
que la cubre y úsala.

---

## 8. Reglas de Git

- Ramas: `feature/<id>-<descripción>` → `develop` → `main`.
- Commits convencionales: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`,
  `chore:`, `ci:`.
- **No agregues `Co-Authored-By: Claude` ni ninguna atribución a la IA.** Los
  commits van solo a nombre del integrante humano.
- **No hagas `git push` ni abras PRs sin permiso explícito.**
- Nunca uses `--no-verify` ni te brinques los hooks.

---

## 9. Checklist de cierre

Antes de reportar una tarea como terminada:

- [ ] La tarea está en un `task.md` aprobado.
- [ ] Se cumple su criterio de aceptación.
- [ ] Su comando de verificación pasó.
- [ ] `./gradlew ktlintCheck detekt test` pasó.
- [ ] No se agregaron dependencias sin permiso.
- [ ] No hay secretos en lo que escribiste.
- [ ] Se respetó la dirección de las capas (`ui` → `data` nunca ocurre).
- [ ] No borraste ni saltaste pruebas.
- [ ] `memoria.md` quedó actualizada.
- [ ] Reportaste al humano y te detuviste.
