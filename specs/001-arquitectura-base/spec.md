# Spec 001 — Arquitectura base de la app móvil

> Unidad de trabajo: `001-arquitectura-base`
> Estado: **redactada, pendiente de aprobación**
> Fecha: 27/10/2026 · Autor: Adrián Adonai García Jiménez
> Alineada con: [`constitution.md`](../../constitution.md) v1.0.0

---

## Supuestos

```
SUPUESTOS QUE ESTOY HACIENDO:
1. La app se construye en Kotlin nativo con Jetpack Compose (el repositorio
   ya existe así; queda descartado Flutter para este proyecto).
2. El backend expone una API REST en /api/v1 con autenticación JWT
   (token de acceso + refresh), según el repositorio hermano en ASP.NET Core.
3. Esta unidad entrega SOLO el andamio arquitectónico: ninguna pantalla,
   modelo ni regla de negocio de PetMate.
4. El contrato exacto de los endpoints aún no está cerrado, por lo que la
   capa de red se valida contra un endpoint de salud y un doble de prueba,
   no contra endpoints reales de negocio.
5. minSdk queda en 24 hasta que el profesor confirme si los lineamientos
   obligan API 26.
→ Corríjanme antes de aprobar esta spec.
```

---

## 1. Objetivo

Dejar lista la **base técnica** sobre la que después se construirá toda la
funcionalidad de PetMate: estructura de capas, inyección de dependencias,
persistencia local, acceso a red, navegación, manejo de errores, sesión,
caché de frontend, tema visual, infraestructura de pruebas y pipeline de
CI/CD.

**Por qué ahora:** la metodología del proyecto es Spec Driven Development y
los lineamientos exigen DevSecOps, caché en tres niveles, pruebas en tres
niveles, CI/CD y una matriz de versionamiento definida *antes* de desarrollar.
Si la funcionalidad se construye sobre un andamio improvisado, cada feature
reinventa su forma de manejar errores, estado, caché y pruebas, y nada de eso
se puede corregir después sin reescribir todo.

**Para quién:** para el equipo de desarrollo y para los agentes de IA que
implementarán las features. El usuario final no ve esta unidad de forma
directa; la percibe como una app que abre sin conexión, no se cae y no pierde
la sesión.

**Cómo se ve el éxito:** un agente de IA puede tomar la spec de una feature
cualquiera y construirla sin tener que decidir nada de infraestructura, porque
todas esas decisiones ya están tomadas, implementadas y probadas.

---

## 2. Alcance

| # | Capacidad |
|---|---|
| A-01 | Matriz de versionamiento escrita y aplicada al build |
| A-02 | Estructura de paquetes de las tres capas (`ui`, `domain`, `data`) y sus auxiliares (`core`, `di`) |
| A-03 | Inyección de dependencias en toda la app |
| A-04 | Base de datos local operativa y verificable, como fuente de verdad de la UI |
| A-05 | Cliente de red operativo contra `/api/v1`, con serialización, tiempos de espera, reintentos y registro |
| A-06 | Modelo único de resultado y de error, del repositorio hasta la pantalla |
| A-07 | Manejo de sesión: guardado cifrado de credenciales, renovación automática y cierre de sesión real |
| A-08 | Caché de frontend (memoria + disco) con política de expiración explícita |
| A-09 | Navegación con rutas tipadas y un solo grafo |
| A-10 | Tema visual (colores, tipografía, modo claro/oscuro) y componentes base reutilizables |
| A-11 | Infraestructura de pruebas en los tres niveles, con dobles y utilidades |
| A-12 | Puerta de calidad automática (estilo y análisis estático) |
| A-13 | Pipeline de CI/CD que compila, prueba, revisa seguridad y genera el artefacto firmable |
| A-14 | Configuración de build por entorno y endurecimiento del `release` |
| A-15 | Observabilidad mínima: registro de eventos y errores con un logger propio |

### Pantalla de verificación
Se permite **una sola** pantalla mínima y desechable, sin valor de producto,
cuyo único fin es demostrar que el andamio funciona de extremo a extremo
(pantalla → ViewModel → caso de uso → repositorio → base local + red). Se
elimina al llegar la primera feature real.

---

## 3. Fuera de alcance

Queda **explícitamente excluido** de esta unidad:

| Qué | Dónde se atenderá |
|---|---|
| Mapa, marcadores y filtros de negocios | spec de funcionalidad |
| Mis mascotas, carnet, vacunas, alergias, historial | spec de funcionalidad |
| QR, compartir historial, escáner | spec de funcionalidad |
| Registro, verificación por código, login, recuperación | spec de funcionalidad |
| Afiliación, panel del negocio, consultas, cobro, seguimiento | spec de funcionalidad |
| Chat de seguimiento | spec de funcionalidad |
| Función de ML (raza por foto) | spec de funcionalidad |
| Función de IA generativa | spec de funcionalidad |
| Moderación de contenido | spec de funcionalidad (la arquitectura solo deja el punto de extensión) |
| Notificaciones push | spec propia, posterior |
| Entidades y tablas de negocio | spec de funcionalidad (aquí solo se prueba el mecanismo de persistencia) |
| Endpoints de negocio | spec de funcionalidad (aquí solo el cliente y el endpoint de salud) |
| Landing page y Google Analytics | otro repositorio |
| Infraestructura de nube y Terraform | otro repositorio |
| Backend | repositorio `backend-petmate-nosdd-aspnetcore` |
| Publicación en producción de Google Play | spec de liberación, posterior |

**Ninguna regla de negocio de PetMate se escribe en esta unidad.**

---

## 4. Requisitos

### Funcionales de la plataforma

| ID | Requisito | Trazabilidad |
|---|---|---|
| R-01 | La app debe abrir y mostrar datos previamente guardados sin conexión a internet | RF-53, RNF-28 |
| R-02 | La información que ve la UI debe provenir de la base local; la red actualiza la base local | RF-53, RNF-28 |
| R-03 | La app debe conservar la sesión del usuario indefinidamente sin reautenticación | RF-57, RNF-31 |
| R-04 | La app debe renovar el token de acceso de forma automática y transparente al expirar | RNF-31 |
| R-05 | El cierre de sesión debe invalidar las credenciales en el servidor y borrar todo dato de sesión del dispositivo | RNF-31 |
| R-06 | La app debe guardar credenciales de sesión de forma cifrada | RNF-01, RNF-02 |
| R-07 | Toda comunicación con el backend debe ir sobre HTTPS; el tráfico en claro debe estar prohibido | RNF-01, RNF-29 |
| R-08 | La app debe cachear respuestas en memoria y en disco, con expiración explícita por tipo de dato | RNF-28 |
| R-09 | La app debe presentar al usuario un mensaje comprensible ante cualquier falla de red, servidor o datos, sin cerrarse | RNF-16 |
| R-10 | La app debe registrar eventos y errores de forma consultable, sin incluir datos sensibles ni credenciales | RNF-32 |
| R-11 | La app debe permitir navegar entre pantallas conservando el estado al rotar y al volver atrás | RNF-16 |
| R-12 | La app debe ofrecer modo claro y oscuro | RNF-16 |
| R-13 | El número de versión de cada compilación debe provenir de la matriz de versionamiento | RNF-34 |
| R-14 | La app debe funcionar en Android 7.0 (API 24) o superior | RNF-17 |

### No funcionales de ingeniería

| ID | Requisito | Trazabilidad |
|---|---|---|
| R-15 | El código debe estar organizado en tres capas con dependencias en una sola dirección | RNF-20, constitución P3 |
| R-16 | La capa de dominio no debe depender de ningún framework ni de Android | RNF-20, constitución P3 |
| R-17 | Toda dependencia debe resolverse por inyección, sin construcción manual ni singletons globales | RNF-20 |
| R-18 | Ninguna operación de entrada/salida debe ejecutarse en el hilo principal | RNF-15 |
| R-19 | No debe existir ningún secreto, llave ni credencial en archivos versionados | RNF-01, constitución P6 |
| R-20 | Debe existir infraestructura para pruebas unitarias, de integración y de interfaz | RNF-33 |
| R-21 | El pipeline debe compilar, correr pruebas, revisar estilo, análisis estático y seguridad en cada cambio propuesto | RNF-33 |
| R-22 | El artefacto de `release` debe estar minificado, sin depuración y sin registro verboso | RNF-01, RNF-15 |
| R-23 | Todas las versiones de dependencias deben estar centralizadas en un único catálogo | RNF-20 |

---

## 5. Criterios de éxito

Cada criterio es verificable por alguien que no escribió esta spec.

| # | Criterio | Cómo se comprueba |
|---|---|---|
| C-01 | Existe un documento de matriz de versionamiento y el `versionName` de la app coincide con él | Inspección del documento y de la salida del build |
| C-02 | Las tres capas existen y sus dependencias van en una sola dirección | La skill `revisar-capas` corre sin violaciones |
| C-03 | Ningún archivo de la capa de dominio importa Android, Room, Retrofit ni Compose | `grep` sobre `domain/` no devuelve resultados |
| C-04 | La app arranca con el contenedor de dependencias activo y ninguna dependencia construida a mano | La app abre; no existen llamadas a constructores de repositorios en `ui` |
| C-05 | La base de datos local se crea, escribe, lee y emite cambios | Prueba instrumentada de la base en memoria, en verde |
| C-06 | El cliente de red consulta el endpoint de salud del backend y deserializa su respuesta | Prueba de integración contra un servidor local de prueba, en verde |
| C-07 | Un error de red se convierte en un mensaje en pantalla y la app no se cierra | Prueba del ViewModel con el doble de red fallando, en verde |
| C-08 | Un token expirado se renueva automáticamente y la petición original se reintenta una sola vez | Prueba del interceptor de renovación, en verde |
| C-09 | El cierre de sesión deja el almacenamiento de credenciales vacío | Prueba del repositorio de sesión, en verde |
| C-10 | Las credenciales no son legibles en texto claro en el almacenamiento del dispositivo | Inspección del almacenamiento en un dispositivo de depuración |
| C-11 | Un dato cacheado se sirve desde la caché antes de su expiración y se renueva después | Prueba de la política de caché, en verde |
| C-12 | La app abre y muestra datos guardados con el modo avión activado | Comprobación manual en dispositivo |
| C-13 | La navegación entre dos destinos conserva el estado al rotar la pantalla | Prueba instrumentada de navegación, en verde |
| C-14 | La app se ve correctamente en modo claro y en modo oscuro | Comprobación manual y capturas |
| C-15 | `./gradlew ktlintCheck detekt test` termina sin errores ni advertencias | Ejecución del comando |
| C-16 | El pipeline de CI corre en un cambio propuesto y todas sus etapas quedan en verde | Ejecución visible en GitHub Actions |
| C-17 | El pipeline falla a propósito si se introduce una prueba que no pasa | Prueba del pipeline con un cambio deliberado |
| C-18 | El `release` compila, está minificado y no tiene depuración activa | `./gradlew bundleRelease` y revisión de la configuración |
| C-19 | No hay secretos en el repositorio ni en su historial | Revisión del agente `seguridad` |
| C-20 | Ninguna versión literal de dependencia aparece fuera del catálogo | `grep` sobre los archivos de build |
| C-21 | La pantalla de verificación recorre pantalla → ViewModel → caso de uso → repositorio → base local y red | Comprobación manual en dispositivo |
| C-22 | Las coberturas mínimas de la constitución §5 se cumplen | Reporte de cobertura |

---

## 6. Riesgos

| # | Riesgo | Impacto | Mitigación |
|---|---|---|---|
| G-01 | El contrato de la API no está cerrado y la capa de red se diseña a ciegas | Alto | Esta unidad solo valida el mecanismo (endpoint de salud + dobles). Los modelos de negocio llegan con sus specs. Se acuerda el contrato con el responsable del backend antes de la primera feature |
| G-02 | Sobre-ingeniería: construir abstracciones que ninguna feature usará | Medio | El alcance está limitado a las 15 capacidades listadas. Toda pieza que no sirva a un criterio de éxito se elimina |
| G-03 | Conflicto entre `minSdk` 24 del andamio y el Android 8+ de los lineamientos | Medio | Pregunta abierta Q-01; se resuelve antes de ejecutar la tarea T-002 |
| G-04 | La renovación automática de token con varias peticiones en paralelo provoca renovaciones duplicadas o un ciclo infinito | Alto | Renovación serializada con un solo reintento por petición; prueba específica (C-08) |
| G-05 | Las versiones muy recientes del catálogo (AGP 9.x, Compose BOM 2026.02) pueden ser incompatibles entre sí o con Hilt | Medio | Verificar la compatibilidad en la tarea T-003 antes de agregar el resto; fijar versiones estables |
| G-06 | El crédito y los límites de la cuenta de Google Play retrasan la verificación del despliegue | Medio | El pipeline deja el artefacto firmable listo; la subida se valida en la spec de liberación |
| G-07 | Varios integrantes tocan el andamio al mismo tiempo y se pisan | Medio | El andamio lo construye una sola persona; las features entran después, en ramas separadas |
| G-08 | Cifrar credenciales con la API estándar falla en algunos dispositivos y deja la app sin sesión | Medio | Degradación controlada y registrada; prueba en dispositivo físico |

---

## 7. Dependencias

**Externas a esta unidad:**
- Acuerdo con el responsable del backend sobre la URL base, el endpoint de
  salud, el formato de error y el flujo de renovación de token.
- Repositorio creado en la organización **PETMATE-APP'S** con ramas `main` y
  `develop` protegidas.
- Secretos de CI/CD configurados en GitHub (credenciales de firma).

**Internas (esta unidad habilita a):**
- Todas las specs de funcionalidad posteriores dependen de que 001 esté
  terminada y verificada.

---

## 8. Preguntas abiertas

| # | Pregunta | Para quién | Bloquea |
|---|---|---|---|
| Q-01 | ¿`minSdk` se queda en 24 o sube a 26 por los lineamientos? | Profesor | T-002 |
| Q-02 | ¿Cuál es la URL base del backend en desarrollo y la ruta exacta del endpoint de salud? | Miguel (backend) | T-005 |
| Q-03 | ¿Cuál es el formato de la respuesta de error del backend (campos y códigos)? | Miguel (backend) | T-013 |
| Q-04 | ¿El refresh token rotativo se envía en el cuerpo o en una cabecera, y en qué ruta? | Miguel (backend) | T-017 |
| Q-05 | ¿Se cambia el `applicationId` de `com.example.petmateedel` a uno definitivo antes de publicar? | Equipo | T-002 |
| Q-06 | ¿Qué duración de caché se quiere por tipo de dato? (la arquitectura propone valores por omisión) | Equipo | T-018 |
| Q-07 | ¿La matriz de versionamiento es un documento compartido para los tres repositorios o uno por repositorio? | Profesor / equipo | T-001 |

> Las preguntas abiertas **no se responden inventando**. Cada una bloquea la
> tarea indicada hasta que se resuelva.

---

## 9. Puerta de aprobación

- [ ] El profesor / responsable revisó el alcance y el fuera de alcance.
- [ ] Los supuestos están corregidos o confirmados.
- [ ] Las preguntas abiertas tienen dueño.
- [ ] **Aprobada para pasar a `plan.md`.**

Siguiente: [`plan.md`](plan.md) → [`task.md`](task.md)
