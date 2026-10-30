# Plan 013 — Escáner de QR y ficha de la mascota

> Plan técnico de la unidad `013-escaner-qr`
> Estado: **redactado, pendiente de aprobación**
> Depende de [`spec.md`](spec.md) aprobada

Este documento responde **cómo**. El **qué** está en la spec.

---

## 1. Principios aplicados

- `ui → domain ← data`; el dominio de la ficha es Kotlin puro.
- **El contenido clínico no toca Room.** Esta unidad es la excepción
  deliberada a offline-first, y está justificada en D-02.
- El filtrado por tipo de negocio es del servidor; la app no implementa
  reglas de autorización.
- El permiso de cámara se pide según 003, con alternativa real si se niega.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| QR-01 | `ScannedPetCard` | domain/model | Ficha recibida: básicos, alergias y secciones autorizadas |
| QR-02 | `ScanToken` | domain/model | Token opaco; no se registra ni se serializa en claro |
| QR-03 | `ScanError` | domain/model | Expirado, inválido, revocado, sin red, no autorizado |
| QR-04 | `ScanRepository` | domain/repository | Contrato: canjear token por ficha |
| QR-05 | `RedeemScanTokenUseCase` | domain/usecase | Canje y traducción de errores |
| QR-06 | `ScannedPetCardDto` y mapper | data/remote | Transporte y traducción |
| QR-07 | `ScanApiService` | data/remote | Llamada de canje |
| QR-08 | `ScanRepositoryImpl` | data/repository | Canje sin persistencia local |
| QR-09 | `QrScannerViewModel` | ui/escaner | Estado del escáner |
| QR-10 | `QrScannerScreen` | ui/escaner | Cámara, guía visual y linterna |
| QR-11 | `ManualCodeEntry` | ui/escaner | Entrada del código corto |
| QR-12 | `PetCardViewModel` | ui/ficha | Estado de la ficha |
| QR-13 | `PetCardScreen` | ui/ficha | Ficha de solo lectura |
| QR-14 | `AllergyBanner` | ui/ficha | Alergias destacadas, siempre visibles |

**Se reutiliza sin duplicar:** cliente HTTP y errores de 001; sesión de 002;
permiso de cámara de 003; componentes de estado de 001.

---

## 3. Orden de construcción

| Fase | Componentes | Entrega |
|---|---|---|
| F1 Dominio | QR-01 … QR-05 | Modelos, contrato y canje |
| F2 Datos | QR-06, QR-07, QR-08 | Transporte sin persistencia |
| F3 Escáner | QR-09, QR-10 | Cámara y lectura |
| F4 Manual | QR-11 | Alternativa sin cámara |
| F5 Ficha | QR-12, QR-13, QR-14 | Presentación por tipo de negocio |
| F6 Cierre | — | Integración, privacidad y cierre |

---

## 4. Decisiones

### D-01 — El filtrado por tipo de negocio es del servidor
**Decisión:** la app envía el token y su credencial de sesión; el backend
devuelve **solo** lo que ese negocio puede ver. La app pinta lo que recibe.
**Por qué:** G-01, el riesgo crítico de esta unidad. Si el backend devolviera
todo el historial y la app lo ocultara, cualquiera que inspeccione el tráfico
de la app vería el historial clínico completo de una mascota en una tienda de
alimento. Ocultar en pantalla no es proteger.
**Alternativa descartada:** filtrar en la app — es seguridad aparente.
**Consecuencia:** si el backend no lo hace así (Q-02), hay que cambiarlo antes
de implementar. La prueba C-04 inspecciona la respuesta recibida, no la
pantalla.

### D-02 — La ficha no se persiste
**Decisión:** `ScanRepositoryImpl` devuelve la ficha y **no** la escribe en
Room ni en ningún almacenamiento. Vive en memoria mientras la pantalla esté
abierta.
**Por qué:** R-11 y G-02. El negocio tiene acceso temporal y limitado; si la
ficha quedara en caché, el límite de duración que el dueño fijó en 009 dejaría
de significar algo. Esta es la excepción justificada al offline-first de P4.
**Consecuencia:** sin red no se abre ficha nueva (C-11), y eso es correcto.

### D-03 — El token nunca se registra
**Decisión:** `ScanToken` sobrescribe su representación textual para no
exponer el valor, y el interceptor de registro lo omite.
**Por qué:** R-12 y G-03. Un token en un registro es un acceso regalado.

### D-04 — La entrada manual no es un extra, es obligatoria
**Decisión:** `ManualCodeEntry` se construye en su propia fase y es un camino
de primera clase, no un respaldo escondido.
**Por qué:** R-02 y G-05. Hay dispositivos sin servicios de Google donde la
librería de escaneo puede no funcionar, y hay usuarios que niegan la cámara.
Sin este camino, la unidad falla en esos casos.

### D-05 — Las alergias se pintan fuera de las secciones
**Decisión:** `AllergyBanner` es un componente propio, por encima del resto
de la ficha, y no depende de que las secciones clínicas estén autorizadas.
**Por qué:** regla de 008.3. Una alergia desconocida puede matar a un animal
en una estética que le aplica un producto. Es el dato que siempre viaja.

### D-06 — La librería de escaneo se decide en la tarea, no aquí
**Decisión:** el plan define la interfaz `QrCodeReader` en el dominio y deja
la implementación concreta a la tarea correspondiente, previa autorización.
**Por qué:** Q-06 sigue abierta y la elección cambia según si se exige
funcionar sin servicios de Google. La abstracción permite cambiarla sin tocar
la pantalla.

---

## 5. Dependencias nuevas

Una librería de lectura de códigos QR, **pendiente de decidir** (Q-06). Se
pide con la skill `agregar-dependencia` antes de tocar el catálogo. El plan no
presupone cuál.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El backend devuelve más de lo que el negocio puede ver | D-01 y prueba C-04 sobre la respuesta; si falla, se bloquea la unidad |
| RT-02 | La ficha se filtra a Room por un refactor posterior | D-02 explícita en el plan y verificación en el cierre (C-12) |
| RT-03 | La librería de escaneo no funciona sin servicios de Google | D-04: la entrada manual cubre el caso; D-06: la interfaz permite cambiarla |
| RT-04 | El token aparece en un registro de depuración | D-03 y prueba C-13 |
| RT-05 | La cámara consume batería al quedarse abierta | La cámara se libera al salir de la pantalla; se verifica en el cierre |

---

## 7. Puntos de verificación

| Después de | Se verifica |
|---|---|
| F1 | Canje y traducción de errores probados (C-06, C-07, C-08) |
| F2 | **La respuesta de tienda/estética no contiene historial** (C-04) |
| F3 | Escaneo abre la ficha correcta (C-01, C-02) |
| F4 | Entrada manual funciona sin permiso de cámara (C-09, C-10) |
| F5 | Alergias siempre visibles; la consulta solo para veterinaria (C-05, C-15) |
| F6 | Nada clínico persistido; token fuera de registros (C-12, C-13) |

---

## 8. Lo que este plan NO hace

- No genera el QR (009) ni registra la consulta (014).
- No implementa reglas de autorización: las aplica el backend.
- No persiste nada clínico, por diseño.
- No elige todavía la librería de escaneo (Q-06).
