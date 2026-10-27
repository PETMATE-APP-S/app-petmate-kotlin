---
name: nueva-pantalla
description: Crea una pantalla nueva completa respetando la arquitectura del proyecto — UiState, ViewModel con Hilt, Composable sin estado, ruta en el NavHost y pruebas. Úsala cada vez que haya que agregar una pantalla a la app.
---

# Crear una pantalla nueva

Receta para agregar una pantalla sin romper las capas.

**Requisito:** la pantalla debe estar en una spec aprobada. Si no lo está,
detente.

## Archivos que vas a crear

```
ui/<feature>/
├── <Feature>UiState.kt      ← estado inmutable
├── <Feature>ViewModel.kt    ← lógica de presentación
└── <Feature>Screen.kt       ← Composable sin estado de negocio

ui/navigation/
└── (editar) rutas y NavHost

test/.../ui/<feature>/
└── <Feature>ViewModelTest.kt
```

## 1. El estado

Inmutable, con valores por omisión, y representa **todos** los estados
posibles de la pantalla, incluidos carga y error:

```kotlin
data class PetsUiState(
    val isLoading: Boolean = false,
    val pets: List<Pet> = emptyList(),
    val error: UiText? = null,
)
```

Reglas: `data class` con `val`; nada de `var`; sin tipos de framework dentro
(no `Context`, no `Painter`); los textos de error como `UiText`/id de recurso,
no como `String` ya formateado.

## 2. El ViewModel

```kotlin
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
```

Reglas:
- `@HiltViewModel` + `@Inject constructor`.
- Inyecta **casos de uso o interfaces de `domain`**, nunca un repositorio de `data`.
- Un solo `StateFlow` público, expuesto con `asStateFlow()`.
- Métodos `onXxx...()` para los eventos de la UI.
- Cero referencias a `Context`, `View`, `Navigator` o Compose.
- El dispatcher se inyecta; no se escribe `Dispatchers.IO` dentro.

## 3. El Composable

Dos capas: una con estado (lee el ViewModel) y una sin estado (pura, se puede
previsualizar y probar).

```kotlin
@Composable
fun PetsScreen(
    onPetClick: (String) -> Unit,
    viewModel: PetsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PetsContent(
        uiState = uiState,
        onPetClick = onPetClick,
        onRetryClick = viewModel::onRetryClicked,
    )
}

@Composable
private fun PetsContent(
    uiState: PetsUiState,
    onPetClick: (String) -> Unit,
    onRetryClick: () -> Unit,
) { /* ... */ }
```

Reglas:
- La navegación **sube** como lambda; el Composable no conoce el `NavController`.
- Textos desde `stringResource(...)`. Cero literales.
- `collectAsStateWithLifecycle()`, no `collectAsState()`.
- Cubre los tres estados: cargando, vacío, error.
- Un `@Preview` del Composable sin estado.
- Elementos interactivos con `contentDescription` y área táctil ≥ 48 dp.

## 4. La ruta
Agrega la ruta tipada y su entrada en el `NavHost` único. No crees un segundo
`NavHost`.

## 5. Las pruebas
`<Feature>ViewModelTest` es **obligatoria** (constitución §5: ViewModels al
100 %). Con Turbine, cubre: estado inicial, camino feliz, error y reintento.

## 6. Cierre
```bash
./gradlew ktlintCheck detekt test
```
Y corre la skill `revisar-capas`.

## Lista de verificación
- [ ] La pantalla está en una spec aprobada.
- [ ] `UiState` inmutable y cubre carga, vacío y error.
- [ ] El `ViewModel` inyecta `domain`, no `data`.
- [ ] Un solo `StateFlow` expuesto con `asStateFlow()`.
- [ ] Composable sin estado separado, con `@Preview`.
- [ ] Navegación como lambda hacia arriba.
- [ ] Cero literales de texto; todo en `strings.xml`.
- [ ] `ViewModelTest` escrita y pasando.
- [ ] `ktlintCheck detekt test` en verde.
