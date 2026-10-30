# Plan 020 — Liberación en Google Play

> Plan técnico de la unidad `020-liberacion-play`
> Estado: **redactado, pendiente de aprobación**
> Depende de [`spec.md`](spec.md) aprobada

Este documento responde **cómo**. El **qué** está en la spec.

---

## 1. El calendario manda

Esta unidad no se ordena por dependencias técnicas sino **por fechas**. El
plazo de Google no se puede acelerar con esfuerzo.

### Calendario hacia atrás desde el 02/12/2026

| Fecha límite | Hito | Por qué esa fecha |
|---|---|---|
| **29/10/2026** | Cuenta de desarrollador creada y en verificación | La verificación de identidad puede tardar días |
| **03/11/2026** | Ficha, seguridad de datos y clasificación completas | Deben estar antes de subir el primer artefacto |
| **05/11/2026** | Primer artefacto firmado en el track cerrado | 3 días de margen para resolver problemas de subida |
| **08/11/2026** | **INICIO de la prueba cerrada con 12 testers** | **Fecha más crítica del proyecto** |
| **22/11/2026** | Fin de los 14 días y envío a producción | 14 días continuos, sin interrupciones |
| **29/11/2026** | App disponible en producción | Hasta 7 días de revisión de Google |
| **02/12/2026** | Evaluación | 3 días de margen |

> **Quedan 10 días hasta el 8 de noviembre.** Si esa fecha se pasa, no se
> llega a la evaluación con la app publicada, por muy terminado que esté el
> código.

### Lo que esto implica

La app que entra a la prueba cerrada **no necesita estar completa**. Necesita
compilar, abrir, permitir registrarse y no cerrarse sola. Las funciones se
siguen subiendo durante los 14 días: cada actualización dentro del mismo track
no reinicia el contador.

---

## 2. Componentes

| # | Componente | Dónde | Responsabilidad |
|---|---|---|---|
| LP-01 | Cuenta de desarrollador | Google Play Console | Identidad verificada |
| LP-02 | Llave de subida | Secretos de GitHub | Firma del artefacto |
| LP-03 | `release.yml` extendido | `.github/workflows` | Construye, firma y sube |
| LP-04 | Cuenta de servicio de Google | Google Cloud + Play | Permite subir desde el flujo |
| LP-05 | Ficha de la tienda | Play Console | Título, textos, capturas, icono |
| LP-06 | Formulario de seguridad de datos | Play Console | Declaración de lo recolectado |
| LP-07 | Clasificación de contenido | Play Console | Cuestionario |
| LP-08 | Lista de testers | Play Console | 12 o más cuentas de Google |
| LP-09 | Procedimiento de reversión | `docs/` | Qué hacer si una versión sale mal |
| LP-10 | Bitácora de versiones | `docs/` | Qué versión se subió y cuándo |

---

## 3. Orden de ejecución

| Fase | Hasta | Entrega |
|---|---|---|
| **F1 Cuenta** | 29/10 | LP-01 en verificación, LP-02 generada y resguardada |
| **F2 Ficha** | 03/11 | LP-05, LP-06, LP-07 completas |
| **F3 Automatización** | 05/11 | LP-03, LP-04: subida automática funcionando |
| **F4 Prueba cerrada** | 08/11 | LP-08 con 12 testers; prueba iniciada |
| **F5 Iteración** | 08/11–22/11 | Subidas sucesivas con las funciones que vayan saliendo |
| **F6 Producción** | 22/11–29/11 | Envío, revisión y publicación |

F1 y F2 pueden avanzar en paralelo con todo el desarrollo. No dependen de que
ninguna spec esté implementada.

---

## 4. Decisiones

### D-01 — La publicación arranca antes que el producto
**Decisión:** la cuenta, la ficha y el primer artefacto se preparan desde ya,
con la app en el estado en que esté.
**Por qué:** G-01. Los 14 días de prueba cerrada corren en calendario, no en
esfuerzo. Esperar a terminar el producto para empezar a publicar garantiza no
llegar.
**Alternativa descartada:** publicar al final — matemáticamente imposible con
la fecha de evaluación.

### D-02 — La llave de firma se resguarda en dos lugares
**Decisión:** la llave vive en los secretos de GitHub **y** en un resguardo
seguro fuera del repositorio, con responsable nombrado.
**Por qué:** G-07. Perder la llave de subida significa no poder actualizar
nunca más esa app; habría que publicar una aplicación nueva, con otro
identificador. Es el único error verdaderamente irreversible del proyecto.

### D-03 — La subida se automatiza desde GitHub Actions
**Decisión:** `release.yml` construye el artefacto firmado y lo sube al track
correspondiente mediante una cuenta de servicio.
**Por qué:** R-02 lo pide el punto 16, y además una subida manual se olvida,
se hace con la versión equivocada o se hace desde la máquina de alguien que
ese día no está.

### D-04 — El formulario de seguridad de datos se llena contra las specs
**Decisión:** se recorren las specs 002, 007, 008, 014 y 019 y se declara cada
dato que la app recolecta, incluidos los clínicos y el envío a la IA.
**Por qué:** G-04 y C-06. Una declaración incompleta sobre datos de salud es
motivo de suspensión, no solo de rechazo. Llenarlo "a ojo" es el error típico.

### D-05 — Se contemplan dos ciclos de rechazo
**Decisión:** el calendario reserva margen para dos rechazos y correcciones.
**Por qué:** G-03. Una app de salud de primera publicación con cuenta nueva
tiene alta probabilidad de observaciones. Planear sin margen es planear mal.

### D-06 — Los testers se reclutan antes de necesitarlos
**Decisión:** se busca una lista de 15 personas con cuenta de Google, para
cubrir el mínimo de 12 con margen.
**Por qué:** G-02. Conseguir 12 personas que efectivamente acepten la
invitación e instalen la app toma más tiempo del que parece.

### D-07 — Reversión probada, no solo documentada
**Decisión:** se ensaya una reversión real antes de llegar a producción.
**Por qué:** G-08 y C-13. Un procedimiento escrito que nunca se ejecutó no es
un procedimiento, es una intención.

---

## 5. Dependencias nuevas

Ninguna en código. Sí hay costos y trámites externos: cuota de registro de
desarrollador (Q-02) y la cuenta de servicio de Google Cloud.

---

## 6. Riesgos técnicos

| # | Riesgo | Mitigación |
|---|---|---|
| RT-01 | La verificación de identidad retrasa todo el calendario | F1 es la primera tarea de todas; si se atrasa, se escala de inmediato |
| RT-02 | La subida automática falla por permisos de la cuenta de servicio | Se prueba con el track interno antes del cerrado |
| RT-03 | El `versionCode` se repite y Google rechaza la subida | Se toma de la matriz de versionamiento y se registra en LP-10 |
| RT-04 | La política de privacidad no está publicada a tiempo | G-06: dependencia de la landing; se escala si se retrasa |
| RT-05 | La app que entra a prueba cerrada se cierra al abrir | Se exige el recorrido mínimo probado antes de subir |
| RT-06 | Una actualización durante los 14 días reinicia el contador | Se verifica el comportamiento en la consola antes de actualizar |

---

## 7. Puntos de verificación

| Después de | Se verifica |
|---|---|
| F1 | Cuenta activa; llave resguardada y **no** en el repositorio (C-01, C-02) |
| F2 | Ficha completa; seguridad de datos declara lo clínico y la IA (C-05, C-06) |
| F3 | Un cambio en la rama principal sube artefacto firmado (C-03, C-04) |
| F4 | 12 testers inscritos; prueba iniciada el 08/11 (C-09) |
| F5 | La app instalada desde la tienda abre y permite registrarse (C-12) |
| F6 | Disponible en producción y descargable (C-11); reversión ensayada (C-13) |

---

## 8. Lo que este plan NO hace

- No construye funcionalidad: la coordina y la publica.
- No escribe la política de privacidad: la enlaza.
- No garantiza la aprobación de Google: reserva margen para rechazos.
