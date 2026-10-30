# Plan 017 — Ajustes, privacidad y eliminar cuenta

> Plan técnico de la unidad `017-ajustes-privacidad`
> Estado: **redactado, pendiente de aprobación**
> Depende de [`spec.md`](spec.md) aprobada

Este documento responde **cómo**. El **qué** está en la spec.

---

## 1. Principios aplicados

- `ui → domain ← data`; el dominio de ajustes es Kotlin puro.
- El modelo de notificaciones se importa de 006; no se redefine.
- La eliminación de cuenta es una operación irreversible: se trata con el
  mismo cuidado que una credencial.
- Las URLs legales son configuración, no constantes en el código.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| AJ-01 | `NotificationPreferences` | domain/model | Importado de 006, no redefinido |
| AJ-02 | `UserProfile` | domain/model | Datos del perfil y si es afiliado |
| AJ-03 | `LegalLinks` | domain/model | URLs de privacidad y términos |
| AJ-04 | `SettingsRepository` | domain/repository | Contrato de ajustes |
| AJ-05 | `AccountRepository` | domain/repository | Contrato de eliminación |
| AJ-06 | `UpdateNotificationPreferenceUseCase` | domain/usecase | Cambia una categoría |
| AJ-07 | `DeleteAccountUseCase` | domain/usecase | Reautentica, elimina y limpia |
| AJ-08 | `WipeLocalDataUseCase` | domain/usecase | Borra todo rastro local |
| AJ-09 | DTOs y mappers | data/remote | Transporte |
| AJ-10 | `SettingsApiService` | data/remote | Ajustes y eliminación |
| AJ-11 | Entidad y DAO de ajustes | data/local | Lectura sin conexión |
| AJ-12 | `SettingsRepositoryImpl` | data/repository | Offline para leer |
| AJ-13 | `AccountRepositoryImpl` | data/repository | Eliminación y limpieza |
| AJ-14 | `SettingsViewModel` | ui/ajustes | Estado de los ajustes |
| AJ-15 | `SettingsScreen` | ui/ajustes | Pantalla principal |
| AJ-16 | `DeleteAccountFlow` | ui/ajustes | Explicación, confirmación y reautenticación |
| AJ-17 | `ProfileScreen` | ui/perfil | Perfil y visitas para afiliados |

**Se reutiliza sin duplicar:** categorías de 006; logout y reautenticación de
002; permiso de notificaciones de 003; red, Room y componentes de 001.

---

## 3. Orden de construcción

| Fase | Componentes | Entrega |
|---|---|---|
| F1 Dominio | AJ-01 … AJ-06 | Modelos y contratos |
| F2 Datos | AJ-09, AJ-10, AJ-11, AJ-12 | Ajustes con lectura local |
| F3 Ajustes | AJ-14, AJ-15 | Interruptores y enlaces legales |
| F4 Eliminación | AJ-07, AJ-08, AJ-13, AJ-16 | Flujo completo de borrado |
| F5 Perfil | AJ-17 | Perfil y visitas |
| F6 Cierre | — | Verificación de residuos y cierre |

**La eliminación va en su propia fase (F4)** porque es la parte irreversible y
merece atención aislada, no mezclada con los interruptores.

---

## 4. Decisiones

### D-01 — Eliminar cuenta exige reautenticación
**Decisión:** antes de ejecutar el borrado, el usuario vuelve a autenticarse
con sus credenciales, reutilizando el flujo de 002.
**Por qué:** G-01. Un teléfono desbloqueado y prestado basta para destruir el
historial médico de las mascotas de alguien. La reautenticación convierte un
accidente en una acción deliberada.
**Alternativa descartada:** solo un diálogo de confirmación — se acepta por
reflejo.

### D-02 — Se explica qué se borra antes de pedir confirmación
**Decisión:** la pantalla enumera lo que desaparece: mascotas, carnets,
historial, QRs vigentes y, si es afiliado, su negocio del mapa.
**Por qué:** R-06 y G-03. Google Play revisa que el usuario entienda el
alcance, y es lo correcto aunque no lo revisara.

### D-03 — La limpieza local es una operación propia y verificable
**Decisión:** `WipeLocalDataUseCase` borra Room, DataStore y las credenciales
cifradas, y se puede probar de forma aislada.
**Por qué:** G-06 y R-11. Si la limpieza quedara repartida entre varios
repositorios, cualquier tabla nueva se olvidaría y dejaría residuos.
**Consecuencia:** C-10 inspecciona el almacenamiento tras eliminar.

### D-04 — La eliminación nunca se encola
**Decisión:** sin conexión, eliminar cuenta se rechaza con un mensaje; no
entra al outbox de 005.
**Por qué:** una eliminación encolada que se ejecuta días después, cuando el
usuario ya cambió de opinión, es un daño irreversible por un detalle técnico.

### D-05 — Las URLs legales son configuración
**Decisión:** `LegalLinks` llega desde la configuración del build o del
backend, no escrita en el código.
**Por qué:** G-04 y Q-02. La landing todavía no existe; cuando exista, la URL
no debe obligar a publicar una versión nueva de la app.

### D-06 — El aviso de privacidad se alcanza sin sesión
**Decisión:** el enlace también está disponible desde la bienvenida de 002.
**Por qué:** R-12 y C-05. Google Play exige que la política sea accesible, y
alguien que no puede entrar a su cuenta igual tiene derecho a leerla.

### D-07 — Los ajustes se leen de Room y se escriben en el servidor
**Decisión:** lectura offline-first; el cambio requiere red porque el servidor
segmenta los envíos.
**Por qué:** R-10 y la decisión de 006. Un interruptor que solo cambia
localmente mentiría: el servidor seguiría enviando.

---

## 5. Dependencias nuevas

Ninguna. Para abrir el aviso de privacidad se usa el navegador del sistema o
una vista integrada ya disponible; si se opta por una librería, se pide con la
skill `agregar-dependencia`.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | Una tabla nueva de Room queda sin limpiar al eliminar la cuenta | D-03: una sola operación de limpieza, con prueba que la verifica |
| RT-02 | La URL legal apunta a una página inexistente | D-05: configurable; se valida antes de publicar (020) |
| RT-03 | El interruptor cambia en pantalla pero no en el servidor | D-07: el estado refleja la respuesta del servidor, no el toque |
| RT-04 | La eliminación falla a medias y deja la sesión rota | La limpieza local ocurre **después** de la confirmación del servidor |
| RT-05 | El usuario pierde acceso al aviso de privacidad sin sesión | D-06 y prueba C-05 |

---

## 7. Puntos de verificación

| Después de | Se verifica |
|---|---|
| F1 | Contratos definidos; categorías importadas de 006 |
| F2 | Ajustes se leen sin conexión (C-11) |
| F3 | Interruptores guardan y reflejan; seguridad no se apaga (C-02, C-03) |
| F4 | Reautenticación exigida y cancelable (C-07, C-08); sin residuos (C-10) |
| F5 | Visitas solo para afiliados (C-13) |
| F6 | Enlaces legales funcionan con y sin sesión (C-04, C-05) |

---

## 8. Lo que este plan NO hace

- No define las categorías de notificación: las importa de 006.
- No implementa el logout: lo invoca de 002.
- No escribe el texto legal: vive en la landing.
- No exporta datos del usuario (Q-05 abierta).
