# Prompts para generar las 18 specs restantes

> Las unidades 001 y 002 ya están. Faltan 003 a 020, según
> [`specs/MAPA-CAPACIDADES.md`](../specs/MAPA-CAPACIDADES.md).
>
> Dos formas de pedirlo. La **A** es la recomendada.

---

## OPCIÓN A — Por olas (recomendada)

Seis tandas, siguiendo las olas del mapa. Cada tanda agrupa unidades que no
dependen entre sí, así que el agente las puede escribir con el mismo contexto
sin contradecirse.

**Por qué así:** 54 documentos de corrido hacen que las últimas unidades salgan
genéricas, y como las unidades dependen unas de otras, un error temprano se
propaga hacia abajo. Revisar entre olas corta esa cadena.

### Tanda 1 — Unidades 003, 004, 005, 006 (las transversales)

```
Genera las unidades 003-permisos, 004-moderacion-contenido,
005-sincronizacion-offline y 006-notificaciones-push, siguiendo la skill
crear-spec y el formato de las unidades 001 y 002.

Para CADA unidad escribe spec.md, plan.md y task.md en specs/<id>/.

PASO 1 — Antes de escribir, enumera los supuestos de LAS CUATRO juntas, en un
solo bloque, agrupados por unidad. Espera mi respuesta.

PASO 2 — Cuando apruebe, escribe las cuatro unidades completas (los tres
archivos de cada una). En esta tanda no te detengas entre spec, plan y task:
ya validamos el formato en 002, y las cuatro son transversales y pequeñas.

REGLAS
  - Coherentes con constitution.md y con el mapa de capacidades.
  - Respeta las dependencias del mapa: 003 y 004 dependen de 001/002; 005 y
    006 también. Ninguna depende de 007 en adelante.
  - Capas: ui → domain ← data. El domain es Kotlin puro.
  - Criterios de éxito MEDIBLES. Nada de "debe funcionar bien".
  - Lo que no sepas va a Preguntas abiertas. NO inventes rutas de endpoints,
    nombres de campos, umbrales de moderación ni proveedores.
  - Las tareas NO se ejecutan, solo se redactan. Todas sin marcar.
  - No dupliques lo que ya resuelve 001: cítalo y reutilízalo.
  - Al terminar las cuatro, actualiza memoria.md y el mapa si hiciera falta.

Al final, dime en una tabla: unidad | nº de tareas | nº de preguntas abiertas |
de qué otras unidades depende.
```

**→ Revisa antes de la tanda 2.**

### Tanda 2 — Unidades 007, 008 (mascotas y carnet)

```
Genera 007-mis-mascotas y 008-carnet-salud, mismo procedimiento: supuestos de
las dos primero, espera mi respuesta, luego los tres archivos de cada una.

Son el corazón del producto, así que pon especial cuidado en:
  - 007 es la primera unidad que sube fotos: debe usar el punto de moderación
    de 004 y los permisos de 003, sin reimplementarlos.
  - 008 incluye vacunas, desparasitación, peso, alergias y condiciones, más el
    historial médico en línea de tiempo con filtros.
  - Las alergias son información crítica: en compartir historial (009) siempre
    van, aunque el usuario limite el resto. Déjalo asentado desde 008.
  - El carnet debe verse sin conexión (es el caso de uso real: en la
    veterinaria puede no haber señal).
```

**→ Revisa antes de la tanda 3.**

### Tanda 3 — Unidades 009, 010, 017, 018

```
Genera 009-compartir-historial, 010-mapa-e-inicio, 017-ajustes-privacidad y
018-ml-raza-por-foto. Supuestos de las cuatro primero.

Cuidado especial en:
  - 009: QR regenerable, elegir negocio, secciones y duración (30 min, 1 día,
    7 días); las alergias siempre se incluyen.
  - 010: aquí entra el demo ("probadita") que sacamos de 002. El mapa es la
    pantalla de inicio: marcadores por tipo, filtros, carrusel y tarjeta "lo
    más próximo" (rojo ≤3 días, amarillo ≤14). El proveedor de mapas NO está
    decidido: va a preguntas abiertas.
  - 017: ajustes de notificaciones, aviso de privacidad enlazado a la landing
    y eliminar cuenta (que es borrado en cascada del lado del API).
  - 018: función de ML exigida por los lineamientos. El modelo concreto no está
    decidido; plantea opciones en el plan y deja la elección como pregunta
    abierta.
```

**→ Revisa antes de la tanda 4.**

### Tanda 4 — Unidades 011, 012, 013

```
Genera 011-afiliacion, 012-panel-afiliado y 013-escaner-qr. Supuestos primero.

Cuidado en:
  - 011: afiliación en 3 pasos (datos y tipos → servicios con precio, mínimo
    uno → pin en el mapa). Un usuario puede ser varios tipos a la vez
    (veterinaria, tienda, estética) y NO pierde "Mis mascotas" al afiliarse.
  - 012: portada del negocio, alertas de seguimiento (atrasadas en rojo,
    próximas en amarillo), mensajes sin leer, y mascotas atendidas con filtro
    por día. "Personas que vieron tu perfil" va en el Perfil, no aquí.
  - 013: la ficha que se ve al escanear DEPENDE DEL TIPO de negocio. La
    veterinaria ve todo el historial; tienda y estética solo lo básico más
    alergias. Esto es una regla de privacidad, no una preferencia de interfaz.
```

**→ Revisa antes de la tanda 5.**

### Tanda 5 — Unidades 014, 015, 016, 019

```
Genera 014-consulta-veterinaria, 015-seguimiento-tratamiento,
016-chat-seguimiento y 019-ia-generativa. Supuestos primero.

Cuidado en:
  - 014: motivo, diagnóstico, peso, receta, y cobro en vivo desglosado
    (servicios, vacunas, medicinas, IVA, forma de pago), con opción de agregar
    un servicio nuevo con su costo.
  - 015: estados En tratamiento / Mejorando / Dado de alta, próxima revisión,
    notas, y alerta en cada cambio de estado.
  - 016: chat tipo WhatsApp con fotos, botón flotante con no leídos; se cierra
    al dar de alta. Las fotos pasan por la moderación de 004.
  - 019: función de IA generativa exigida por los lineamientos. Candidatas:
    "explícamelo fácil" de la receta, resumen del historial. El proveedor no
    está decidido: pregunta abierta. Advierte en los riesgos que la IA NO debe
    dar consejo médico veterinario, solo explicar lo que el veterinario ya
    escribió.
```

**→ Revisa antes de la tanda 6.**

### Tanda 6 — Unidad 020 (liberación)

```
Genera 020-liberacion-play. Supuestos primero.

Esta unidad es especial porque es la que más riesgo de calendario tiene:
  - Google Play exige prueba cerrada con 12 testers durante 14 días antes de
    poder pedir producción.
  - La evaluación es el 02/12/2026 y el punto 1 de los lineamientos dice que
    la evaluación INICIA descargando la app de Google Play.
  - Por tanto, la cuenta de desarrollador, la ficha de tienda, el formulario de
    seguridad de datos y el primer AAB al track interno NO pueden esperar a que
    el producto esté terminado.

Incluye en el plan un calendario hacia atrás desde el 02/12/2026 que muestre
cuál es la última fecha posible para cada hito. Si las cuentas no dan, dilo
explícitamente en los riesgos en lugar de maquillarlo.
```

---

## OPCIÓN B — Todas de golpe

Si de plano quieres las 18 en una sola corrida:

```
Genera TODAS las unidades restantes del mapa de capacidades: de 003 a 020.

Para cada una, los tres archivos (spec.md, plan.md, task.md) en specs/<id>/,
siguiendo la skill crear-spec y el formato de las unidades 001 y 002.

Procede en el orden de las olas del mapa, sin detenerte entre unidades.

REGLAS
  - Coherentes con constitution.md y con specs/MAPA-CAPACIDADES.md.
  - Respeta las dependencias del mapa; ninguna unidad depende de un id mayor.
  - Capas: ui → domain ← data. El domain es Kotlin puro.
  - Criterios de éxito MEDIBLES, nunca "debe funcionar bien".
  - Lo que no sepas va a Preguntas abiertas. NO inventes rutas, campos,
    proveedores ni umbrales.
  - Las tareas NO se ejecutan. Todas sin marcar.
  - No dupliques lo que ya resuelven 001 y 002: cítalo.
  - Actualiza memoria.md al final.

Si en algún punto detectas que el mapa tiene un error (una dependencia mal
puesta, una unidad mal cortada), DETENTE y repórtalo en vez de continuar
sobre un cimiento malo.

Al terminar, entrega una tabla: unidad | nº de tareas | nº de preguntas
abiertas | dependencias, y el total de tareas de todo el proyecto.
```

**Advertencia:** esto son ~54 documentos y ~13,000 líneas. Es probable que las
últimas unidades salgan más flojas que las primeras, y que haya que rehacer
algunas. Si el modelo se queda sin contexto a la mitad, pídele que continúe
desde la unidad donde se quedó.

---

## Qué revisar en cada unidad que salga

Los cuatro errores que hay que cazar, en orden de frecuencia:

1. **Criterios vagos.** "Debe cargar rápido" no sirve; "< 2.5 s en 4G" sí.
2. **Endpoints inventados.** Si el contrato del backend no está definido, la
   spec debe decirlo en Preguntas abiertas, no suponer rutas ni campos.
3. **Duplicación.** Si una unidad reimplementa algo de 001 o 002 en vez de
   citarlo, está mal.
4. **Capas rotas.** Que `ui` dependa de `data`, o que `domain` conozca Android,
   Room, Retrofit o Compose.

Comprobaciones rápidas después de cada tanda:

```bash
# Cuántas tareas hay en total y cuántas marcadas (deben ser 0)
grep -rcE "^- \[ \] \*\*T-0" specs/*/task.md
grep -rcE "^- \[x\]" specs/*/task.md

# Unidades creadas
ls specs/

# Encoding sano (no debe imprimir nada)
grep -rl "Ã\|â€" specs/
```

Y haz **commit después de cada tanda**. Si una sale mal, vuelves atrás sin
perder las anteriores.
