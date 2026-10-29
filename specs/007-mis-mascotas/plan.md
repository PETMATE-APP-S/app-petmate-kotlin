# Plan 007 — Mis mascotas

> Plan técnico de la unidad `007-mis-mascotas`
> Estado: **redactado, pendiente de aprobación**
> Depende de: [`spec.md`](spec.md) aprobada
> Alineado con: [`constitution.md`](../../constitution.md) v1.1.0
> Fecha: 29/10/2026

Este documento responde **cómo**. El **qué** y el **por qué** están en la spec.

---

## 1. Principios de diseño

1. **Room es la lista.** La UI observa la base local; la red solo la actualiza.
2. **La foto no bloquea.** La mascota se guarda primero y la imagen se sube
   cuando la moderación la aprueba.
3. **El dominio calcula.** La edad y las reglas viven en `domain`, no en el
   Composable.
4. **Cada quien ve lo suyo.** El repositorio filtra por la sesión activa.
5. **La primera feature retira el andamio.** La pantalla de verificación de 001
   desaparece.

---

## 2. Componentes

| # | Componente | Capa | Responsabilidad |
|---|---|---|---|
| MS-01 | Modelo de mascota | `domain/pets` | Datos, edad calculada y validación mínima |
| MS-02 | Contratos y casos de uso | `domain/pets` | Observar, crear, editar, borrar y subir foto |
| MS-03 | Entidad y DAO de Room | `data/local` | Persistencia local y `Flow` de la lista |
| MS-04 | DTOs y mapeo | `data/remote` + `data/mapper` | Contrato de red y traducción |
| MS-05 | Repositorio | `data/repository` | Orquesta Room, red, sesión y sincronización |
| MS-06 | Flujo de foto | `data/pets` | Permiso, moderación, subida diferida y caché |
| MS-07 | Pantalla de lista | `ui/pets` | Tarjetas, estado vacío y navegación |
| MS-08 | Pantalla de formulario | `ui/pets` | Alta y edición con un solo estado |
| MS-09 | Borrado | `ui/pets` + `data` | Confirmación y borrado lógico |
| MS-10 | Navegación | `ui/navigation` | Rutas de lista, alta y edición |
| MS-11 | Pruebas y dobles | `test` | Repositorio falso, reloj fijo y API falsa |

### Lo que se reutiliza de 001 a 005

- `001/K-10` Room, `001/K-11` red, `001/K-17` caché y `001/K-20`
  componentes.
- `002/ID-06` sesión y `002/ID-08` errores.
- `003` permiso de cámara y fotos, con su degradación.
- `004` moderación de imagen.
- `005` política de creación y edición sin conexión.

---

## 3. Dependencias y orden de construcción

```
MS-01 modelo ──→ MS-02 contratos ──→ MS-05 repositorio ──→ MS-07 lista
      │                 │                    │                  │
      ├──→ MS-03 Room ──┤                    ├──→ MS-08 formulario
      ├──→ MS-04 DTOs ──┤                    ├──→ MS-09 borrado
      └──→ MS-06 foto ──┘                    └──→ MS-10 navegación

MS-11 acompaña desde MS-01 (no va al final)
```

### Fases

| Fase | Componentes | Puede ir en paralelo |
|---|---|---|
| **F1 — Dominio** | MS-01, MS-02 | MS-11 con MS-01 |
| **F2 — Datos** | MS-03, MS-04, MS-05 | MS-03 con MS-04 |
| **F3 — Foto** | MS-06 | — |
| **F4 — Presentación** | MS-07, MS-08, MS-09, MS-10 | MS-07 con MS-08 |
| **F5 — Cierre** | MS-11 | — |

---

## 4. Decisiones técnicas

### D-01 — La edad se calcula en `domain` con reloj inyectable
**Decisión:** `Pet.age(today)` recibe la fecha actual; el ViewModel no la lee
del sistema.
**Por qué:** cumple R-07 y G-04. Dos pantallas con cálculos distintos muestran
edades distintas para la misma mascota.
**Alternativa descartada:** calcular en el Composable con `LocalDate.now()` —
no se puede probar y cambia según la zona horaria.

### D-02 — Room es la fuente de verdad de la lista
**Decisión:** el DAO expone `Flow<List<PetEntity>>`; el repositorio escribe y
la UI observa. La red actualiza Room.
**Por qué:** cumple R-12 y P4. La lista se actualiza sola y funciona sin red.
**Alternativa descartada:** devolver la respuesta de red a la UI — dos fuentes
de verdad.

### D-03 — Foto diferida
**Decisión:** la mascota se guarda con o sin foto; si hay foto, se modera y se
sube en segundo plano. Mientras tanto se muestra la imagen local en caché.
**Por qué:** cumple R-13 y G-06. Bloquear el guardado por la subida hace que el
usuario pierda el alta si la red falla.
**Alternativa descartada:** esperar la subida para guardar — frágil y lento.

### D-04 — Borrado lógico con confirmación
**Decisión:** borrar marca la mascota como inactiva y encola la operación; no
se borra la fila de inmediato. El servidor decide el alcance final.
**Por qué:** cumple R-09 y G-03. Un borrado físico local deja el carnet huérfano
y no se puede deshacer.
**Alternativa descartada:** borrar en local y en red a la vez — sin red, el
estado queda inconsistente.

### D-05 — Coil para la foto
**Decisión:** se agrega `coil-compose` al catálogo de versiones, con
autorización previa, y se configura una caché de disco con expiración.
**Por qué:** cumple R-14. 001 ya había diferido Coil a esta unidad, la primera
que carga imágenes.
**Alternativa descartada:** cargar mapas de bits a mano — repite trabajo y no
cachea bien.
**Autorización:** requerida antes de tocar `gradle/libs.versions.toml`.

### D-06 — Un solo formulario para alta y edición
**Decisión:** la misma pantalla y el mismo `ViewModel` reciben un id opcional;
sin id es alta, con id es edición.
**Por qué:** los campos y la validación son idénticos; duplicarlos garantiza
que se desincronicen.
**Alternativa descartada:** dos pantallas — más código y dos lugares donde
arreglar un campo.

### D-07 — Aislamiento por sesión en el repositorio
**Decisión:** toda consulta filtra por el id de usuario de la sesión activa; el
`ViewModel` no recibe el id como parámetro.
**Por qué:** cumple R-10 y G-05. Si la UI tuviera que recordar el filtro, un
olvido muestra mascotas ajenas.
**Alternativa descartada:** filtrar en el `ViewModel` — repetido y fácil de
omitir.

### D-08 — Retiro de la pantalla de verificación
**Decisión:** se elimina la pantalla y la ruta de `001/K-25` del grafo de
producto al entrar la lista de mascotas.
**Por qué:** cumple R-15. 001 la declaró desechable y su plan previó retirarla
en la primera feature real.
**Alternativa descartada:** dejarla oculta — queda código muerto y una ruta que
nadie mantiene.

### D-09 — El cliente no borra el carnet
**Decisión:** al borrar una mascota, el cliente no elimina los registros del
carnet; el servidor aplica la política que confirme Q-04.
**Por qué:** el carnet es información clínica y su conservación es una decisión
de negocio, no del dispositivo.
**Alternativa descartada:** borrar en cascada en local — irreversible y sin
respaldo.

---

## 5. Dependencias a agregar

| Dependencia | Para qué | Autorización |
|---|---|---|
| `coil-compose` | Cargar y cachear la foto de la mascota | **Requerida** antes de tocar el catálogo |

Ninguna otra. Si aparece una necesidad nueva, se aplica la skill
`agregar-dependencia` y se pide autorización.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | El contrato de mascotas no está y se inventa | DTOs aislados y Q-01 antes de la primera tarea de red |
| RT-02 | El esquema de Room cambia sin migración | T-002 exige migración y prueba de actualización; cambio de esquema requiere autorización |
| RT-03 | La foto queda en caché tras un rechazo | 004 borra el temporal; prueba C-06 |
| RT-04 | El formulario pierde datos al rotar | Estado en el `ViewModel` y prueba de rotación |
| RT-05 | El borrado lógico deja la mascota visible | El DAO filtra por activo; prueba C-08 |
| RT-06 | Coil se agrega sin autorización | T-002/T-006 se detienen si no hay aprobación |
| RT-07 | La pantalla de verificación sigue accesible | T-010 revisa el grafo; C-11 |

---

## 7. Puntos de verificación

| Fase | Verificación |
|---|---|
| F1 | Dominio puro; edad con reloj fijo (C-04) |
| F2 | Room con migración; lista desde `Flow`; aislamiento (C-09, C-10) |
| F3 | Foto moderada, rechazada y sin permiso (C-06, C-07) |
| F4 | Alta, edición, borrado y estado vacío (C-01, C-02, C-03, C-08) |
| F5 | `./gradlew ktlintCheck detekt test`; sin pantalla de verificación (C-11) |

---

## 8. Lo que este plan no incluye

- No implementa el carnet ni sus secciones (008).
- No implementa el QR (009) ni el mapa (010).
- No calcula la próxima vacuna: solo reserva el espacio (008).
- No identifica la raza por foto (018).
- No implementa el chat (016) ni el perfil del dueño (017).

---

## 9. Puerta de aprobación

- [ ] Los componentes y su orden son aceptables.
- [ ] Las decisiones D-01 a D-09 están revisadas.
- [ ] La dependencia `coil-compose` está autorizada.
- [ ] Los riesgos técnicos y sus mitigaciones son suficientes.
- [ ] **Aprobado para pasar a `task.md`.**
