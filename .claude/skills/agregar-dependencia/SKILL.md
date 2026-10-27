---
name: agregar-dependencia
description: Procedimiento para evaluar, autorizar y declarar correctamente una dependencia nueva en gradle/libs.versions.toml. Úsala siempre que haga falta una librería que el proyecto aún no tiene — agregar una dependencia requiere autorización del humano.
---

# Agregar una dependencia

> **Agregar una dependencia está en la lista "Preguntar primero" de la
> constitución (§4). Nunca la agregues por tu cuenta.**

## Paso 1 — ¿De verdad hace falta?

Antes de proponer nada, responde:

1. ¿Ya hay en el proyecto algo que resuelve esto? Revisa
   `gradle/libs.versions.toml` completo.
2. ¿Lo resuelve la biblioteca estándar de Kotlin, AndroidX o Compose?
3. ¿Cuánto código propio ahorra realmente? Si son 30 líneas, escríbelas.
4. ¿La librería está viva? Último release, issues abiertos, mantenedores.
5. ¿Qué tanto pesa y qué se arrastra con ella (dependencias transitivas)?
6. ¿Qué licencia tiene? (Apache 2.0 / MIT sin problema; GPL es un veto.)
7. ¿Tiene vulnerabilidades conocidas?

Muchas dependencias se resuelven con un archivo de 50 líneas. Prefiere eso.

## Paso 2 — Propón, no agregues

Preséntale al humano:

```
PROPUESTA DE DEPENDENCIA

Librería:      <grupo:artefacto>
Versión:       <x.y.z>   (última estable, publicada el <fecha>)
Para qué:      <qué tarea del task.md la necesita>
Licencia:      <licencia>
Peso:          <tamaño aproximado / impacto en el APK>
Transitivas:   <qué arrastra>
Alternativas:  1) <otra librería> — descartada porque ...
               2) escribirlo a mano — ~<N> líneas, descartado porque ...
Riesgos:       <mantenimiento, vulnerabilidades, acoplamiento>

→ ¿Autorizas agregarla?
```

**Espera el "sí". No avances.**

## Paso 3 — Declárala bien

Todo va en `gradle/libs.versions.toml`. **Jamás** una versión literal en un
`build.gradle.kts`.

```toml
[versions]
hilt = "2.52"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

Y en `app/build.gradle.kts`, por alias:

```kotlin
dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

### Elige bien la configuración

| Configuración | Cuándo |
|---|---|
| `implementation` | Por omisión, casi siempre |
| `api` | Solo si el tipo aparece en la API pública del módulo |
| `ksp` / `kapt` | Procesadores de anotaciones (prefiere `ksp`) |
| `testImplementation` | Solo pruebas unitarias |
| `androidTestImplementation` | Solo pruebas instrumentadas |
| `debugImplementation` | Solo en depuración (herramientas, inspectores) |

Usa un BOM cuando exista (Compose, Firebase) y no fijes versiones de los
artefactos que el BOM ya administra.

## Paso 4 — Verifica

```bash
./gradlew build
./gradlew app:dependencies --configuration releaseRuntimeClasspath
./gradlew test
```

Revisa: que no haya conflictos de versión, que no se duplique funcionalidad
existente y que el `release` siga compilando.

## Paso 5 — Registra

Anota en `memoria.md`: qué se agregó, para qué tarea y quién lo autorizó.

## Reglas duras
- Nada de versiones `latest.release`, `+` ni rangos. Versión exacta, siempre.
- Nada de dependencias en estado `alpha` en código de producción sin
  autorización explícita y motivo escrito.
- Una dependencia nueva **no** entra en el mismo commit que una feature:
  va en su propio commit `chore:`.
- Si la librería mete framework en `domain`, está descartada por diseño.
