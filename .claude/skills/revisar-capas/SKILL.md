---
name: revisar-capas
description: Audita que la arquitectura limpia no se haya degradado — verifica la dirección de dependencias entre ui, domain y data, que domain siga siendo Kotlin puro y que se respeten las convenciones de nombres y ubicación. Úsala antes de cerrar una tarea, antes de un PR y cuando algo "ya no se sabe dónde va".
---

# Revisar las capas

Verificación mecánica del principio P3 de la constitución.

```
ui  ──→  domain  ←──  data
```

## Regla 1 — `domain` es Kotlin puro

Ningún archivo de `domain` puede importar framework:

```bash
grep -rnE "^import (android|androidx|kotlinx\.coroutines\.android)\." \
  app/src/main/java/com/example/petmateedel/domain/

grep -rnE "^import .*(room|retrofit2|okhttp3|compose|sqlite)" \
  app/src/main/java/com/example/petmateedel/domain/
```

**Cualquier resultado es una violación.** Se corrige, no se discute.

Excepción única y deliberada: `kotlinx.coroutines.flow` y
`kotlinx.serialization` como anotación de modelo se permiten si la
constitución lo dice. Hoy: `Flow` sí, serialización **no** (eso pertenece a
los DTOs de `data`).

## Regla 2 — `ui` nunca importa de `data`

```bash
grep -rn "import com.example.petmateedel.data" \
  app/src/main/java/com/example/petmateedel/ui/
```

**Cualquier resultado es una violación.** La UI habla con `domain` (casos de
uso o interfaces de repositorio), nunca con repositorios concretos, DAOs,
entidades de Room ni DTOs.

## Regla 3 — `domain` no importa de `data` ni de `ui`

```bash
grep -rnE "import com\.example\.petmateedel\.(data|ui)\." \
  app/src/main/java/com/example/petmateedel/domain/
```

`domain` está en el centro: **no conoce a nadie**. Declara interfaces; `data`
las implementa.

## Regla 4 — Nombres en su lugar

| Sufijo | Debe vivir en |
|---|---|
| `*Entity` | `data/local/` |
| `*Dto` | `data/remote/` |
| `*Dao` | `data/local/` |
| `*RepositoryImpl` | `data/repository/` |
| `*Repository` (interfaz) | `domain/repository/` |
| `*UseCase` | `domain/usecase/` |
| `*ViewModel`, `*UiState` | `ui/<feature>/` |
| `*Module` (Hilt) | `di/` |

```bash
# Entidades o DTOs filtrados fuera de data/
grep -rln --include=*.kt -E "class [A-Za-z]+(Entity|Dto)\b" \
  app/src/main/java/com/example/petmateedel/ | grep -v "/data/"
```

## Regla 5 — Fugas comunes

```bash
# println en producción (prohibido)
grep -rn "println(" app/src/main/java/

# Dispatchers escritos a mano en lugar de inyectados
grep -rn "Dispatchers\.\(IO\|Default\)" app/src/main/java/

# Versiones literales fuera de libs.versions.toml
grep -rnE "\"[0-9]+\.[0-9]+\.[0-9]+\"" app/build.gradle.kts

# Supresiones sospechosas
grep -rn -E "@Suppress|@Ignore|TODO\(\)" app/src/
```

## Regla 6 — Un ViewModel, un estado

Cada `ViewModel` expone **un** `StateFlow` inmutable:

```bash
grep -rn "MutableStateFlow\|StateFlow" app/src/main/java/com/example/petmateedel/ui/
```

Señales de problema: más de un `StateFlow` público por `ViewModel`, un
`MutableStateFlow` expuesto sin `asStateFlow()`, un `ViewModel` que inyecta un
repositorio de `data` en lugar de un caso de uso.

## Cómo reportar

Por violación: regla rota · archivo y línea · por qué importa · corrección
concreta. Si todo está limpio, dilo y enumera qué reglas corriste.

**No "arregles" una violación moviendo el problema de lugar.** Si una pieza no
cabe en ninguna capa, la clasificación está mal: escala al agente
`arquitecto`.
