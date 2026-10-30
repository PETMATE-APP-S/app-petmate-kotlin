# Plan 012 — Panel del afiliado

> Plan técnico de la unidad `012-panel-afiliado`
> Estado: **redactado, pendiente de aprobación**
> Depende de [`spec.md`](spec.md) aprobada

Este documento responde **cómo**. El **qué** está en la spec.

---

## 1. Principios aplicados

- `ui → domain ← data`; el dominio del panel es Kotlin puro.
- Room es la fuente de verdad: el panel observa la base local y la red la
  refresca (P4).
- El reloj se inyecta: ningún cálculo de atrasado o próximo lee la fecha del
  dispositivo a mano.
- Cada bloque del panel es independiente: si uno falla o está vacío, los
  demás siguen funcionando.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| PA-01 | `BusinessDashboard` | domain/model | Resumen: contadores y bloques |
| PA-02 | `FollowUpAlert` | domain/model | Alerta con su fecha de revisión y estado |
| PA-03 | `AttendedPet` | domain/model | Mascota atendida con fecha y consulta |
| PA-04 | `AlertSeverity` | domain/model | Atrasada, próxima o normal |
| PA-05 | `DashboardRepository` | domain/repository | Contrato del panel |
| PA-06 | `GetDashboardUseCase` | domain/usecase | Observa el panel desde Room |
| PA-07 | `ClassifyAlertUseCase` | domain/usecase | Decide el color según la fecha y el reloj |
| PA-08 | `GetAttendedPetsUseCase` | domain/usecase | Lista filtrada por rango de días |
| PA-09 | `DayFilter` | domain/model | Hoy, Ayer, Semana, fecha concreta |
| PA-10 | DTOs y mappers del panel | data/remote | Transporte y traducción |
| PA-11 | `DashboardApiService` | data/remote | Llamadas al backend |
| PA-12 | Entidades y DAOs del panel | data/local | Caché local del resumen |
| PA-13 | `DashboardRepositoryImpl` | data/repository | Red → Room → UI |
| PA-14 | `DashboardViewModel` | ui/panel | Estado del panel |
| PA-15 | `DashboardScreen` | ui/panel | Pantalla con los tres bloques |
| PA-16 | `BusinessHeader` | ui/panel | Portada del negocio |
| PA-17 | `AlertList` | ui/panel | Lista de alertas con color |
| PA-18 | `AttendedPetList` | ui/panel | Lista con filtro por día |

**Se reutiliza sin duplicar:** cliente HTTP, Room, caché, reloj, navegación y
componentes de estado vacío de 001; sesión de 002; negocio de 011; el cálculo
de severidad por fecha que ya usa 008/010.

---

## 3. Orden de construcción

| Fase | Componentes | Entrega |
|---|---|---|
| F1 Dominio | PA-01 … PA-09 | Modelos, contrato y reglas de clasificación |
| F2 Datos | PA-10, PA-11, PA-12 | Transporte y caché local |
| F3 Repositorio | PA-13 | Red → Room → UI con caché |
| F4 Presentación | PA-14, PA-15, PA-16 | Panel con portada y resumen |
| F5 Bloques | PA-17, PA-18 | Alertas y mascotas atendidas con filtros |
| F6 Cierre | — | Integración, sin conexión y cierre |

F1 y F2 pueden ir en paralelo una vez fijado el contrato de PA-05.

---

## 4. Decisiones

### D-01 — La severidad se calcula en el dominio con reloj inyectado
**Decisión:** `ClassifyAlertUseCase` recibe la fecha de revisión y el reloj, y
devuelve `AlertSeverity`. Los umbrales son constantes nombradas.
**Por qué:** G-03. Leer la fecha del dispositivo dentro de un Composable hace
la prueba imposible de escribir y el resultado depende del reloj del usuario.
**Consecuencia:** C-03 a C-05 se prueban adelantando el reloj, sin esperar.

### D-02 — Los umbrales se comparten con 008 y 010, no se redefinen
**Decisión:** rojo ≤ 3 días, amarillo ≤ 14, en una sola constante del dominio
que también usan el carnet y la tarjeta "lo más próximo".
**Por qué:** G-06. Tres definiciones del mismo umbral se desincronizan y el
usuario ve un evento en rojo en una pantalla y en amarillo en otra.

### D-03 — Cada bloque tiene su propio estado
**Decisión:** el `UiState` del panel contiene un estado por bloque (portada,
alertas, atendidas), no un único `isLoading` global.
**Por qué:** G-02. Si 015 todavía no existe, el bloque de alertas debe poder
mostrarse vacío mientras la portada y las atendidas funcionan. Un estado
global obligaría a que todo el panel espere al bloque más lento.

### D-04 — El panel abre con datos locales y refresca después
**Decisión:** `GetDashboardUseCase` emite primero lo que hay en Room y luego
el refresco de red actualiza la base.
**Por qué:** R-08 y R-10. Es el patrón offline-first de 001; el panel no es
excepción.
**Alternativa descartada:** esperar la red antes de pintar — rompe el
requisito de abrir sin conexión y hace lento el arranque.

### D-05 — El acceso clínico se valida contra el permiso vigente, no la caché
**Decisión:** la lista de mascotas atendidas se puede mostrar desde caché,
pero el detalle clínico de una mascota se pide al backend, que valida si el
permiso de compartir sigue vigente.
**Por qué:** G-05, que es el riesgo más serio de esta unidad. Si el negocio
conserva en caché el historial de una mascota cuyo permiso expiró, el límite
de duración de 009 deja de significar nada.
**Consecuencia:** la caché del panel guarda identificadores y fechas, no
contenido clínico.

### D-06 — Paginación en las mascotas atendidas
**Decisión:** la lista se carga por páginas; el tamaño queda en Q-03.
**Por qué:** G-04. Un negocio con meses de operación acumula cientos de
registros y cargarlos todos congela la pantalla.

### D-07 — El filtro por día se resuelve en el dominio
**Decisión:** `DayFilter` se traduce a un rango de fechas en el dominio, con
el reloj inyectado, y el repositorio consulta por ese rango.
**Por qué:** "Hoy" depende de la zona horaria y del momento. Resolverlo en la
consulta de Room dispersaría la lógica en SQL imposible de probar en la JVM.

---

## 5. Dependencias nuevas

Ninguna propia. Si se adopta una librería de paginación, se pide con la skill
`agregar-dependencia`; el plan no la presupone.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El contrato del backend cambia | Mappers aislados; el dominio no cambia |
| RT-02 | 015 y 016 no existen y el panel no compila | D-03: bloques independientes con contratos propios y dobles vacíos |
| RT-03 | Caché clínica obsoleta tras expirar el permiso | D-05: la caché no guarda contenido clínico |
| RT-04 | El filtro por día falla en cambios de zona horaria | D-07 y pruebas con zonas distintas |
| RT-05 | La paginación duplica elementos al refrescar | Clave estable por elemento y prueba de refresco |

---

## 7. Puntos de verificación

| Después de | Se verifica |
|---|---|
| F1 | Clasificación por fecha probada con reloj inyectado (C-03 a C-05) |
| F2 | Mapeo en ambos sentidos; caché local escribe y lee |
| F3 | Panel emite desde Room y refresca por red (C-11) |
| F4 | Portada y resumen correctos (C-01, C-02, C-06) |
| F5 | Alertas navegan y filtros devuelven lo esperado (C-07 a C-09) |
| F6 | Sin conexión, estados vacíos y cambio de sesión (C-10, C-13) |

---

## 8. Lo que este plan NO hace

- No crea ni edita seguimientos (015) ni consultas (014).
- No implementa el chat (016), solo consume su contador.
- No edita los datos del negocio (011, Q-08).
- No calcula estadísticas ni ingresos.
