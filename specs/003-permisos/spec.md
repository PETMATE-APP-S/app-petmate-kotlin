# Spec 003 — Permisos

> Unidad de trabajo: `003-permisos`
> Estado: **redactada, pendiente de aprobación**
> Fecha: 29/10/2026 · Autor: Adrián Adonai García Jiménez
> Alineada con: [`constitution.md`](../../constitution.md) v1.1.0
> Depende de: [`001-arquitectura-base`](../001-arquitectura-base/spec.md)

---

## Supuestos

```
SUPUESTOS QUE ESTOY HACIENDO:

1. Los permisos que cubre esta unidad son cuatro: ubicación (mapa y "lo más
   próximo"), cámara (escáner QR y foto de mascota), fotos/almacenamiento
   (elegir una imagen) y notificaciones (Android 13 en adelante). Micrófono,
   contactos, teléfono y bluetooth quedan fuera.

2. El mecanismo ya existe en 001/K-27. Esta unidad define el momento de uso,
   el texto de justificación previa y la degradación por pantalla; no
   reimplementa el pedido ni el flujo del sistema.

3. Siempre hay una explicación propia ANTES del diálogo del sistema. Nunca se
   lanza el diálogo en frío.

4. Si el usuario niega dos veces, no se insiste con un tercer diálogo: se
   ofrece abrir los ajustes de la app.

5. La degradación es concreta y visible: sin ubicación el mapa abre con centro
   por defecto y lista; sin cámara se puede escribir el código del QR a mano;
   sin fotos la mascota usa iniciales y se puede reintentar; sin
   notificaciones la app avisa dentro de la interfaz.

6. No se pide ningún permiso al arrancar ni en el login. Ubicación solo al
   abrir el mapa o pedir cercanía; cámara solo al escanear o fotografiar;
   notificaciones solo al activarlas en 006 o 017.

7. No se agrega ninguna librería. Se usan las APIs del sistema y, cuando esté
   disponible, el selector de fotos del sistema (Photo Picker) sin pedir
   permiso de almacenamiento.

8. Tocar `AndroidManifest.xml` para declarar permisos requiere autorización
   explícita del humano; las tareas que lo hagan lo indican.

9. Las pantallas reales que usan permisos llegan en 006, 007, 010, 013 y 016.
   Aquí se entregan los contratos, los componentes y una pantalla de prueba
   que demuestra cada degradación.

10. Fuera de alcance: la política de qué se notifica (006), el contenido que
    se fotografía (007, 016), el escaneo (013) y el texto legal (017).

→ Corrígeme ahora o avanzo con esto.
```

---

## 1. Objetivo

Que la app **pida cada permiso en el momento en que lo usa, explique para qué
antes de pedirlo y siga siendo usable si el usuario lo niega**, sin bloquear
ninguna función esencial y sin pedir permisos en frío al arrancar.

Esta unidad convierte el mecanismo genérico de 001 en una experiencia de
producto repetible: un catálogo de permisos con su momento, su justificación y
su alternativa, que las demás unidades consumen sin volver a decidir.

**Para quién:** cualquier usuario de la app, desde la primera pantalla que
necesita ubicación, cámara, fotos o notificaciones.

---

## 2. Alcance

| ID | Capacidad |
|---|---|
| A-01 | Catálogo de permisos con su momento de uso y su justificación |
| A-02 | Explicación previa propia antes del diálogo del sistema |
| A-03 | Flujo de negativa, negativa permanente y apertura de ajustes |
| A-04 | Degradación de ubicación: centro por defecto, lista y búsqueda manual |
| A-05 | Degradación de cámara: entrada manual del código del QR |
| A-06 | Degradación de fotos: avatar con iniciales y reintento |
| A-07 | Degradación de notificaciones: avisos dentro de la app |
| A-08 | Contratos, componentes y pruebas reutilizables por las demás unidades |

---

## 3. Fuera de alcance

| Qué | Dónde se atenderá |
|---|---|
| Política y contenido de las notificaciones | Spec 006 |
| Subida y moderación de fotos | Specs 004 y 007 |
| Mapa, marcadores y cercanía | Spec 010 |
| Escaneo de QR y entrada manual del código en su pantalla final | Spec 013 |
| Fotos del chat | Spec 016 |
| Texto legal y aviso de privacidad | Spec 017 |
| Permisos de micrófono, contactos, teléfono o bluetooth | No están en los lineamientos ni en la maqueta |
| Permisos especiales de fabricante (autostart, batería) | No están en los lineamientos |

**Ninguna regla de negocio de mascotas, mapa o afiliación se escribe aquí.**

---

## 4. Requisitos

### Funcionales de permisos

| ID | Requisito | Trazabilidad |
|---|---|---|
| R-01 | Cada permiso debe solicitarse en el momento en que la pantalla lo necesita, nunca al arrancar ni al iniciar sesión | Constitución P10; RNF-17, RNF-28 |
| R-02 | Antes del diálogo del sistema debe mostrarse una explicación propia del motivo y del uso del permiso | Constitución P10; RNF-28 |
| R-03 | El sistema debe distinguir tres estados: concedido, negado y negado de forma permanente | RNF-17 |
| R-04 | Tras dos negativas no se debe insistir con el diálogo del sistema; debe ofrecerse abrir los ajustes de la app | Constitución P10; RNF-16 |
| R-05 | Sin ubicación, el mapa debe abrir con un centro por defecto y ofrecer lista y búsqueda manual | RNF-16, RNF-28 |
| R-06 | Sin cámara, debe existir una entrada manual para el código que se iba a escanear | RNF-16 |
| R-07 | Sin permiso de fotos, la mascota debe mostrarse con iniciales y debe poder reintentarse la selección | RNF-16 |
| R-08 | Sin permiso de notificaciones, la app debe seguir siendo usable y mostrar los avisos dentro de la interfaz | RNF-16 |
| R-09 | Ninguna negativa puede impedir el uso de una función esencial ni cerrar la app | Constitución P10; RNF-16 |
| R-10 | El estado de la solicitud debe sobrevivir a la rotación y a la muerte del proceso | RNF-16 |

### No funcionales de ingeniería

| ID | Requisito | Trazabilidad |
|---|---|---|
| R-11 | Los textos de justificación y de degradación deben vivir en `strings.xml`, sin literales en Composables | Constitución §4; RNF-16 |
| R-12 | Ninguna consulta de permiso ni apertura de ajustes puede ejecutarse en el hilo principal de forma bloqueante | RNF-15 |
| R-13 | El pedido de permisos debe pasar por un contrato de dominio, no por llamadas directas desde la UI a las APIs de Android | Constitución P3 |
| R-14 | La app no debe declarar en el manifiesto ningún permiso que ninguna pantalla use | RNF-01, RNF-17 |

---

## 5. Criterios de éxito

| # | Criterio | Cómo se comprueba |
|---|---|---|
| C-01 | Al arrancar la app no se muestra ningún diálogo de permiso | Prueba de interfaz del arranque y revisión del manifiesto |
| C-02 | Cada permiso muestra primero la explicación propia y solo después el diálogo del sistema | Prueba de interfaz del componente de justificación |
| C-03 | Negar ubicación deja el mapa usable con centro por defecto, lista y búsqueda manual | Prueba de la pantalla de prueba con ubicación denegada |
| C-04 | Negar cámara deja disponible la entrada manual del código | Prueba de la pantalla de prueba con cámara denegada |
| C-05 | Negar fotos muestra las iniciales de la mascota y permite reintentar | Prueba de la pantalla de prueba con fotos denegadas |
| C-06 | Negar notificaciones no bloquea la app y el aviso aparece dentro de la interfaz | Prueba con notificaciones denegadas |
| C-07 | Tras dos negativas aparece la opción de abrir ajustes y no un tercer diálogo del sistema | Prueba del flujo de negativa permanente |
| C-08 | Al conceder el permiso desde ajustes y volver, la función se activa sin reiniciar la app | Prueba instrumentada del retorno desde ajustes |
| C-09 | El estado de la solicitud se conserva al rotar la pantalla | Prueba instrumentada de rotación |
| C-10 | Cero cadenas literales de permisos en los Composables | `grep` sobre `ui/` y revisión de `strings.xml` |

---

## 6. Riesgos

| # | Riesgo | Impacto | Mitigación |
|---|---|---|---|
| G-01 | Algunos fabricantes no informan correctamente la negativa permanente | Medio | Se detecta por comportamiento: si el sistema ya no muestra el diálogo, se ofrece ajustes; prueba en al menos dos versiones de Android |
| G-02 | El selector de fotos del sistema no está disponible en API 24 | Medio | Respaldo con el permiso clásico solo en las versiones que lo requieran (Q-05) |
| G-03 | Un permiso se pide en el momento equivocado y molesta al usuario | Alto | Catálogo con momento explícito por permiso y prueba C-01 |
| G-04 | La negativa bloquea un flujo que el usuario necesita | Alto | Degradación definida por permiso y probada con C-03 a C-06 |
| G-05 | Los textos de justificación no explican el beneficio y el usuario niega por desconfianza | Medio | Texto revisado, en recursos y con lenguaje sin jerga; Q-04 |
| G-06 | El manifiesto declara permisos que ninguna pantalla usa | Medio | Revisión en T-010 y regla R-14 |

---

## 7. Dependencias

**De esta unidad:**

- `001-arquitectura-base`: `001/K-27` (mecanismo de permisos), `001/K-20`
  (componentes visuales), `001/K-18` (navegación), `001/K-05` (`UiText`) y
  `001/K-21` (pruebas).

**Externas:**

- Versión mínima de Android confirmada (001 Q-01) para decidir el respaldo del
  selector de fotos.
- Aprobación de los textos de justificación (Q-04).

**Unidades posteriores que dependen de esta:**

- `006` (permiso de notificaciones), `007` (fotos de mascota), `010`
  (ubicación del mapa), `013` (cámara del escáner), `016` (fotos del chat) y
  `017` (interruptores de notificaciones).

---

## 8. Preguntas abiertas

| # | Pregunta | Para quién | Bloquea |
|---|---|---|---|
| Q-01 | ¿La ubicación se pide precisa o aproximada? ¿Basta la aproximada para ordenar los negocios por cercanía? | Equipo / maqueta | Degradación de ubicación |
| Q-02 | ¿Qué formato tiene el código manual que sustituye al escaneo (numérico, alfanumérico, longitud)? | Equipo / 009 y 013 | Degradación de cámara |
| Q-03 | ¿El permiso de notificaciones se pide al activar la primera categoría en 006 o al abrir los ajustes en 017? | Equipo | Permiso de notificaciones |
| Q-04 | ¿Quién aprueba los textos de justificación y en qué idioma se muestran? | Equipo / profesor | Textos |
| Q-05 | ¿Se usa el selector de fotos del sistema en todas las versiones o se declara el permiso clásico como respaldo en API 24–32? | Equipo | Degradación de fotos |
| Q-06 | ¿Se registra la decisión de permiso (concedido o negado) en analítica? Si sí, ¿con qué fin y con qué consentimiento? | Equipo / privacidad | Instrumentación |

> Las preguntas abiertas **no se responden inventando**. Cada una bloquea la
> tarea indicada hasta que se resuelva.

---

## 9. Puerta de aprobación

- [ ] El alcance y el fuera de alcance están revisados.
- [ ] Los supuestos aprobados siguen vigentes.
- [ ] Los requisitos y los criterios de éxito son suficientes para planificar.
- [ ] Las preguntas abiertas están identificadas y se sabe a quién preguntar.
- [ ] **Aprobada para pasar a `plan.md`.**
