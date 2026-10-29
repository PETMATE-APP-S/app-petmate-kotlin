# Mapa de capacidades — App móvil PetMate

> Documento de alcance y orden. Fija el **id estable** de cada unidad de trabajo.
> Estado: **redactado, pendiente de aprobación; numeración cerrada.**
> Fecha: 29/10/2026
> Alineado con [`constitution.md`](../constitution.md) v1.1.0

Este mapa no sustituye a las specs: solo fija qué unidades existen, de qué
dependen y en qué orden se construyen. Una vez asignado un id, **no se
renombra**: las tareas, los commits y los PRs se refieren a él.

`001` y `002` ya tienen documentos. El resto está pendiente de abrir.

---

## 1. Unidades

| id | Responsabilidad | Depende de | Punto del profesor / maqueta |
|---|---|---|---|
| `001-arquitectura-base` | Capas, inyección, red, Room, sesión base, caché, calidad, CI/CD y publicación base | — | 5, 7, 8, 9, 11 |
| `002-identidad-y-sesion` | Bienvenida, alta, verificación por correo, login, sesión viva, refresh, logout y recuperación | 001 | 4, 10, 16 |
| `003-permisos` | Mecanismo y UX de permisos en el momento de uso (ubicación, cámara, fotos, notificaciones) y degradación si se niegan | 001 | 4, 16 |
| `004-moderacion-contenido` | Política, proveedor y umbrales de moderación de texto, imagen y video antes de almacenar | 001, 002 | 4, 16 |
| `005-sincronizacion-offline` | Política de escrituras sin conexión por entidad, outbox, reconciliación y conflictos | 001, 002 | 4, 9, 16 |
| `006-notificaciones-push` | Contenido, segmentación, deep links y preferencias de las notificaciones | 001, 002 | 4, 16 |
| `007-mis-mascotas` | Alta, edición, foto y tarjetas de mascotas | 002, 003, 004, 005 | 4, 16 |
| `008-carnet-salud` | Vacunas, desparasitación, peso, alergias, condiciones e historial médico | 007 | 4 |
| `009-compartir-historial` | Selección de negocio, secciones y duración; QR regenerable | 008, 002 | 4 |
| `010-mapa-e-inicio` | Mapa de negocios, marcadores, filtros, carrusel, "lo más próximo" y demo | 002, 003, 007, 008 | 4, 16 |
| `011-afiliacion` | Alta de negocio en 3 pasos: datos y tipos, servicios con precio y pin en el mapa | 002, 003, 010 | 4 |
| `012-panel-afiliado` | Portada, alertas de seguimiento, mensajes sin leer y mascotas atendidas | 011 | 4 |
| `013-escaner-qr` | Escaneo de QR y ficha del negocio según su tipo | 009, 012, 003 | 4 |
| `014-consulta-veterinaria` | Consulta, diagnóstico, peso, receta, cobro desglosado y forma de pago | 013, 008, 012 | 4 |
| `015-seguimiento-tratamiento` | Estados del tratamiento, alertas y cierre del seguimiento | 014 | 4 |
| `016-chat-seguimiento` | Chat tipo WhatsApp con fotos entre dueño y negocio | 015, 004, 006 | 4, 16 |
| `017-ajustes-privacidad` | Ajustes de notificaciones, aviso de privacidad, enlace a la landing y eliminar cuenta | 002, 003, 006 | 4, 16 |
| `018-ml-raza-por-foto` | Función de ML: identificar la raza por foto | 007, 003 | 4, 16 |
| `019-ia-generativa` | Función de IA: explicar la receta o resumir el historial | 008, 014 | 4, 16 |
| `020-liberacion-play` | Ficha, data safety, política de privacidad, AAB, track cerrado y producción | 001, 002, 017 y el resto del producto | 1, 11, 16, 18 |

**Puntos del profesor:** 1 descarga desde Play · 4 maqueta · 5 SDD/DevSecOps ·
7 pruebas · 8 CI-CD · 9 caché · 10 logout · 11 versionamiento · 16 móvil ·
18 Git. Los puntos 13, 14, 15 y 17 viven en otros repositorios; 001 consume la
nube y el backend, y 017 enlaza la landing.

---

## 2. Orden de construcción

Las unidades de una misma ola pueden ir en paralelo.

| Ola | Unidades | Nota |
|---|---|---|
| 0 | 001 | Base obligatoria |
| 1 | 002, 003 | Identidad y permisos no se estorban |
| 2 | 004, 005, 006 | Moderación, offline y push sobre la base de 001/002 |
| 3 | 007 | Mascotas: primera feature que carga imágenes |
| 4 | 008, 017, 018 | Carnet, ajustes y ML en paralelo |
| 5 | 009, 010 | Compartir historial y mapa; el demo necesita 007 y 008 |
| 6 | 011 | Afiliación: el pin necesita el mapa |
| 7 | 012 | Panel del afiliado |
| 8 | 013 | Escáner QR y ficha |
| 9 | 014 | Consulta y cobro |
| 10 | 015, 019 | Seguimiento e IA generativa |
| 11 | 016 | Chat de seguimiento |
| 12 | 020 | Liberación en Play |

### Camino crítico

```
001 → 002 → 007 → 008 → 010 → 011 → 012 → 013 → 014 → 015 → 016 → 020
```

Fuera del camino crítico: 003, 004, 005 y 006 habilitan a 007; 017 y 018
pueden ir en paralelo con 008; 019 espera a 014.

---

## 3. Reglas del mapa

- El id no se renombra ni se renumera después de asignado.
- Las dependencias van en una sola dirección y no forman ciclos. Si dos
  unidades se necesitan mutuamente, son una sola.
- Ninguna unidad depende de un id mayor, salvo `020`, que cierra el producto.
- `018`, `019` y `020` **no son recortables**: los exigen los lineamientos.
- Si el calendario aprieta, se reduce el alcance **dentro** de una unidad; no
  se elimina la unidad ni se saltan sus pruebas.
- Una unidad solo se abre cuando su spec, su plan y sus tareas están aprobados.

---

## 4. Riesgo de calendario anotado para 020

Google Play exige una **prueba cerrada con al menos 12 testers durante 14
días** antes de poder pedir producción. Con evaluación el **02/12/2026**, la
cuenta de Play, la ficha de la tienda y el primer AAB al track interno deben
arrancar **en paralelo desde ya**, no al final. Si 020 espera a que todo el
producto esté listo, no se llega.

Esta nota no cambia el orden del mapa, pero debe recogerse como riesgo propio
en la spec 020 cuando se abra.
