# Plan 009 — Compartir historial

> Plan técnico de la unidad `009-compartir-historial`
> Estado: **redactado, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) aprobada
> Alineado con: [`constitution.md`](../../constitution.md) v1.1.0
> Fecha: 29/10/2026

Este documento responde **cómo**. El **qué** y el **por qué** están en la spec.

---

## 1. Principios de diseño

1. **El QR es una llave, no un expediente.** Lleva un token opaco; el contenido
   se resuelve en el servidor.
2. **El servidor manda en el tiempo.** La expiración y la invalidación las
   decide el backend.
3. **Las alergias no se eligen.** Vienen del caso de uso de 008 y se inyectan
   siempre.
4. **El directorio es de solo lectura.** 009 no crea negocios ni los pinta en
   un mapa.
5. **Sin red no se emite, pero se puede mostrar lo vigente.**

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| CH-01 | Modelo de compartición | `domain/sharing` | Selección, duración, token y estado del QR |
| CH-02 | Contratos y casos de uso | `domain/sharing` | Emitir, regenerar, consultar y mostrar |
| CH-03 | Cliente de emisión y resolución | `data/sharing/remote` | Endpoints, DTOs y mapeo |
| CH-04 | Directorio de negocios | `data/sharing` + `ui` | Lista de solo lectura y opción "cualquier veterinaria" |
| CH-05 | Regla de secciones | `domain/sharing` | Secciones elegibles y alergias obligatorias |
| CH-06 | Duración y expiración | `domain/sharing` | Tres opciones y cuenta regresiva con hora del servidor |
| CH-07 | Generación y caché del QR | `ui/sharing` + `data` | Dibuja el QR y conserva el vigente |
| CH-08 | Auditoría de emisión | `data/sharing` | Registro sin datos clínicos |
| CH-09 | Pruebas y dobles | `test` | Directorio falso, reloj fijo y decodificador de QR |

### Lo que se reutiliza de 001, 002 y 008

- `001/K-11` red, `001/K-17` caché, `001/K-18` navegación y `001/K-20`.
- `002/ID-06` sesión y `002/ID-08` errores.
- `008/CS-07` `GetCriticalAllergiesUseCase`: se consume tal cual.

---

## 3. Dependencias y orden de construcción

```
CH-01 modelo ──→ CH-02 contratos ──→ CH-03 cliente ──→ CH-07 QR
      │                 │                  │
      ├──→ CH-05 secciones ────────────────┤
      ├──→ CH-06 duración ─────────────────┤
      └──→ CH-04 directorio ───────────────┘
                              │
                              ▼
                        CH-08 auditoría

CH-09 acompaña desde CH-01 (no va al final)
```

### Fases

| Fase | Componentes | Puede ir en paralelo |
|---|---|---|
| **F1 — Dominio** | CH-01, CH-02, CH-05 | CH-09 con CH-02 |
| **F2 — Red** | CH-03, CH-04 | CH-03 con CH-04 |
| **F3 — Tiempo** | CH-06 | — |
| **F4 — QR y pantalla** | CH-07, CH-08 | CH-07 con CH-08 |
| **F5 — Cierre** | CH-09 | — |

---

## 4. Decisiones técnicas

### D-01 — Token opaco resuelto en el servidor
**Decisión:** el QR contiene un token aleatorio; el contenido se pide al
backend con ese token. Ni el nombre de la mascota ni el diagnóstico viajan en
el código.
**Por qué:** cumple R-06, R-13 y G-01. Un QR con datos clínicos es una fuga
permanente: cualquiera lo fotografía y lo guarda.
**Alternativa descartada:** codificar un JSON con el historial — cómodo y
catastrófico para la privacidad.

### D-02 — Las alergias se inyectan, no se seleccionan
**Decisión:** el caso de uso de emisión agrega las alergias de
`008/CS-07` después de la selección del usuario, sin consultarlo.
**Por qué:** cumple R-04 y G-03. Si la UI decidiera, un cambio de pantalla
podría omitirlas.
**Alternativa descartada:** marcar la alergia como obligatoria en la UI — la
regla quedaría en la presentación.

### D-03 — Tres duraciones cerradas
**Decisión:** `ShareDuration` tiene 30 minutos, 1 día y 7 días; no admite
valores libres.
**Por qué:** cumple R-05. Un campo libre permite compartir "para siempre" por
error.
**Alternativa descartada:** fecha de expiración elegida por el usuario — más
compleja y sin respaldo en la maqueta.

### D-04 — El servidor es la autoridad del tiempo
**Decisión:** la respuesta de emisión trae la expiración; la cuenta regresiva
se calcula contra esa marca y no contra el reloj del dispositivo.
**Por qué:** cumple R-08, R-16 y G-04. Un dispositivo con la hora mal puede
mostrar un QR vencido como vigente.
**Alternativa descartada:** calcular la expiración en local — se desincroniza.

### D-05 — Regenerar invalida el anterior
**Decisión:** cada emisión invalida el token anterior de la misma mascota y
negocio, o el servidor lo marca como caducado.
**Por qué:** cumple R-07 y G-02. Dos QR vigentes duplican la superficie de
acceso.
**Alternativa descartada:** permitir varios vigentes — más difícil de revocar.
**Pendiente:** Q-05 confirma si la invalidación es inmediata o por caducidad.

### D-06 — Caché solo para mostrar, no para emitir
**Decisión:** sin red no se emite; un QR vigente se muestra desde la caché
hasta su expiración y luego se borra.
**Por qué:** cumple R-10 y G-05. El usuario puede seguir mostrando lo que ya
tenía, pero no puede generar acceso nuevo sin validar.
**Alternativa descartada:** emitir offline y sincronizar después — el servidor
no puede garantizar la expiración ni la invalidación.

### D-07 — Directorio de solo lectura y "cualquier veterinaria"
**Decisión:** el selector consume el directorio del backend y agrega una opción
explícita que no apunta a un negocio.
**Por qué:** cumple R-02 y G-06. El directorio existe antes que el mapa (010) y
que el alta de negocios (011).
**Alternativa descartada:** esperar a 010 para tener la lista — retrasa 009 sin
necesidad.

### D-08 — Sin enlace en la primera versión
**Decisión:** solo QR. Compartir por enlace queda en Q-10 y no se implementa.
**Por qué:** la maqueta pide QR; un enlace añade otra superficie de acceso y
otra forma de expirar.
**Alternativa descartada:** incluir ambos — alcance no pedido.

### D-09 — Auditoría sin contenido
**Decisión:** se registra mascota, negocio, secciones y expiración; no se
registra el contenido clínico.
**Por qué:** cumple R-12 y R-09 de 008. El registro no debe ser una copia del
historial.
**Alternativa descartada:** guardar el payload compartido — duplica datos
sensibles.

---

## 5. Dependencias a agregar

**Ninguna.** El QR se dibuja con una librería que debe autorizarse si no está
en el catálogo; se evalúa con la skill `agregar-dependencia` antes de tocar
`libs.versions.toml`. Si el equipo prefiere no agregarla, se usa un generador
propio validado por pruebas.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El contrato de emisión no está y se inventa | Q-01 antes de la primera tarea de red |
| RT-02 | El QR se decodifica y expone datos | Token opaco y prueba C-04 |
| RT-03 | La expiración local difiere del servidor | D-04 y prueba C-07 |
| RT-04 | Un QR viejo sigue vigente | D-05 y prueba C-05 |
| RT-05 | Las alergias se pierden al cambiar la selección | D-02 y prueba C-03 |
| RT-06 | Sin red la pantalla queda bloqueada | D-06 y mensaje claro; prueba C-08 |
| RT-07 | El directorio no responde y no hay opción | Estado vacío y "cualquier veterinaria"; prueba C-02 |

---

## 7. Puntos de verificación

| Fase | Verificación |
|---|---|
| F1 | Dominio puro; secciones y alergias obligatorias (C-03) |
| F2 | Contrato con servidor local; directorio y opción "cualquier veterinaria" (C-02) |
| F3 | Expiración y cuenta regresiva con reloj fijo (C-06, C-07) |
| F4 | QR sin datos, regeneración y caché (C-04, C-05, C-09) |
| F5 | `./gradlew ktlintCheck detekt test`; auditoría y secciones (C-10, C-11) |

---

## 8. Lo que este plan no incluye

- No implementa el escáner ni la ficha del negocio (013).
- No implementa el alta de negocios (011) ni el panel (012).
- No comparte por enlace ni por correo.
- No edita el contenido clínico (008).
- No muestra al dueño el registro de accesos (Q-09).

---

## 9. Puerta de aprobación

- [ ] Los componentes y su orden son aceptables.
- [ ] Las decisiones D-01 a D-09 están revisadas.
- [ ] Se confirma que no se agregan dependencias sin autorización.
- [ ] Los riesgos técnicos y sus mitigaciones son suficientes.
- [ ] **Aprobado para pasar a `task.md`.**
