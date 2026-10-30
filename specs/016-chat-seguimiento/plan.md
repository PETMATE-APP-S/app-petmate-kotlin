# Plan 016 — Chat de seguimiento

> Plan técnico de la unidad `016-chat-seguimiento`
> Estado: **redactado, pendiente de aprobación**
> Depende de [`spec.md`](spec.md) aprobada

Este documento responde **cómo**. El **qué** está en la spec.

---

## 1. Principios aplicados

- `ui → domain ← data`; el dominio del chat es Kotlin puro.
- Room es la fuente de verdad del historial; la red lo refresca (P4).
- Los envíos sin conexión usan la cola de 005; no se construye una propia.
- La autorización por participante la valida el backend.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| CH-01 | `Message` | domain/model | Mensaje: autor, contenido, fecha, estado |
| CH-02 | `MessageContent` | domain/model | Texto o imagen, como tipo cerrado |
| CH-03 | `MessageStatus` | domain/model | Pendiente, enviado, entregado, fallido |
| CH-04 | `Conversation` | domain/model | Conversación de un seguimiento, con no leídos |
| CH-05 | `ChatRepository` | domain/repository | Contrato |
| CH-06 | `SendMessageUseCase` | domain/usecase | Modera, encola o envía |
| CH-07 | `ObserveConversationUseCase` | domain/usecase | Observa el historial desde Room |
| CH-08 | `MarkAsReadUseCase` | domain/usecase | Marca leídos y actualiza el contador |
| CH-09 | DTOs y mappers | data/remote | Transporte |
| CH-10 | `ChatApiService` | data/remote | Listar y enviar mensajes |
| CH-11 | `MessageEntity` y DAO | data/local | Historial local paginado |
| CH-12 | `ChatRepositoryImpl` | data/repository | Red → Room → UI y cola |
| CH-13 | `ChatViewModel` | ui/chat | Estado de la conversación |
| CH-14 | `ChatScreen` | ui/chat | Lista de mensajes y composición |
| CH-15 | `MessageBubble` | ui/chat | Burbuja con su estado |
| CH-16 | `UnreadBadge` | ui/chat | Botón flotante con el contador |
| CH-17 | `ChatDisclaimer` | ui/chat | Advertencia de alcance del chat |

**Se reutiliza sin duplicar:** cola y reconciliación de 005; moderación de
004; push de 006; caché de imágenes y red de 001; permisos de 003.

---

## 3. Orden de construcción

| Fase | Componentes | Entrega |
|---|---|---|
| F1 Dominio | CH-01 … CH-05 | Modelos y contrato |
| F2 Datos | CH-09, CH-10, CH-11 | Transporte e historial local |
| F3 Repositorio | CH-12, CH-06 … CH-08 | Envío, cola y lectura |
| F4 Conversación | CH-13, CH-14, CH-15 | Historial y envío de texto |
| F5 Fotos | — | Adjuntos moderados |
| F6 Integración | CH-16, CH-17 | Contador, push y cierre |

---

## 4. Decisiones

### D-01 — Sin conexión persistente: push más refresco
**Decisión:** no se usan websockets ni conexión larga. El chat se refresca al
abrirse y cuando llega un push de 006.
**Por qué:** una conexión persistente consume batería, exige infraestructura
del lado del backend que no está planeada y complica el despliegue en Azure
con dos instancias. Para un chat de seguimiento, donde las respuestas tardan
horas y no segundos, el push es suficiente.
**Alternativa descartada:** websockets — desproporcionado para el caso de uso
y agrega un punto de falla.
**Consecuencia:** G-07; la expectativa de latencia se documenta en Q-03.

### D-02 — El envío usa la cola de 005, no una propia
**Decisión:** un mensaje sin conexión se encola con el mecanismo de 005, con
clave de idempotencia.
**Por qué:** 005 ya resolvió orden, reintento con espera creciente,
deduplicación y errores permanentes. Una segunda cola duplicaría esa lógica y
se comportaría distinto.
**Consecuencia:** C-11 y C-12 se apoyan en el comportamiento ya probado de 005.

### D-03 — La moderación ocurre antes de encolar
**Decisión:** `SendMessageUseCase` modera el contenido antes de meterlo en la
cola; si lo rechaza, no se encola.
**Por qué:** encolar primero y moderar al sincronizar dejaría al usuario
creyendo que su mensaje salió. Para texto, 004 permite guardar y moderar al
sincronizar; aquí se modera antes porque el mensaje se publica a otra persona
en cuanto sale, no es una nota privada.
**Diferencia con 004/D-03:** en el chat el destinatario es otra persona, así
que el criterio es más estricto.

### D-04 — Historial paginado desde Room
**Decisión:** la pantalla observa una página del historial local; al
desplazarse hacia arriba se cargan más mensajes.
**Por qué:** R-12 y C-16. Cargar doscientos mensajes y sus imágenes de golpe
congela la lista.

### D-05 — Las imágenes se sirven del CDN y se cachean con límite
**Decisión:** el mensaje guarda la referencia a la imagen, no la imagen; la
caché de 001 tiene un tope de tamaño.
**Por qué:** G-06. Un chat con decenas de fotos llenaría el dispositivo.

### D-06 — El cierre del chat lo decide el seguimiento
**Decisión:** el estado de 015 determina si se puede escribir; además el
backend rechaza escrituras en un chat cerrado.
**Por qué:** G-08 y R-06. Si solo la pantalla deshabilitara el campo, una
petición directa seguiría escribiendo.

### D-07 — La advertencia es un componente propio y persistente
**Decisión:** `ChatDisclaimer` aparece al abrir la conversación y se puede
contraer, pero no desaparece del todo.
**Por qué:** G-01, el riesgo más serio de esta unidad. Una persona que actúa
sobre un consejo médico mal entendido puede dañar a su animal.

---

## 5. Dependencias nuevas

Ninguna. La carga de imágenes usa la librería ya introducida en 007.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | Mensajes duplicados al reintentar | D-02: clave de idempotencia de 005; C-12 |
| RT-02 | El historial local crece sin control | D-04 con paginación; retención ligada a 015/Q-06 |
| RT-03 | El push no llega y el chat parece muerto | D-01 con refresco al abrir; Q-03 define el intervalo |
| RT-04 | El contador de no leídos difiere del de 012 | Un solo origen del conteo, consumido por ambos; C-07 |
| RT-05 | Una foto grande falla al subirse y bloquea la cola | Límite de tamaño (Q-04) y error permanente que no reintenta |
| RT-06 | El contenido del mensaje viaja en el push | Regla de 006: payload genérico; C-08 verifica |

---

## 7. Puntos de verificación

| Después de | Se verifica |
|---|---|
| F1 | Modelos y contrato; estados del mensaje definidos |
| F2 | Historial local paginado; transporte correcto |
| F3 | Envío con y sin conexión; sin duplicados (C-11, C-12) |
| F4 | Conversación ordenada y legible (C-02, C-05, C-16) |
| F5 | Foto rechazada no se envía (C-03, C-04) |
| F6 | Contador coincide con 012 (C-07); cierre al dar de alta (C-09) |

---

## 8. Lo que este plan NO hace

- No implementa websockets ni conexión persistente.
- No envía las notificaciones: las dispara el backend (006).
- No permite audio, video ni archivos.
- No implementa cifrado de extremo a extremo (Q-06 abierta).
