# Spec 020 — Liberación en Google Play

> Unidad de trabajo: `020-liberacion-play`
> Estado: **redactada, pendiente de aprobación**
> Fecha: 29/10/2026
> Alineada con [`constitution.md`](../../constitution.md) y
> [`MAPA-CAPACIDADES.md`](../MAPA-CAPACIDADES.md)

---

## Supuestos

```
SUPUESTOS QUE ESTOY HACIENDO:
1. El punto 1 de los lineamientos dice que la evaluación INICIA descargando
   la app de Google Play. Si la app no está publicada el 02/12/2026, no hay
   nada que evaluar.
2. SUPUESTO CRÍTICO DE CALENDARIO: Google exige prueba cerrada con al menos
   12 testers durante 14 días continuos antes de poder solicitar producción
   para cuentas personales nuevas. Ese plazo no se negocia ni se acelera.
3. Por tanto esta unidad NO espera a que el producto esté terminado. La
   cuenta, la ficha y el primer artefacto al track interno arrancan en
   paralelo desde el inicio del desarrollo.
4. El despliegue se hace desde GitHub Actions, como sugiere el punto 16.
5. La firma usa una llave de subida almacenada en secretos de GitHub, nunca
   en el repositorio.
6. El formulario de seguridad de datos debe declarar todo lo que la app
   recolecta, incluidos los datos clínicos y el uso de IA de 019.
7. La política de privacidad vive en la landing y debe estar publicada y
   accesible antes de enviar a revisión.
8. La revisión de Google puede tardar días y puede rechazar. Se contemplan al
   menos dos ciclos de rechazo y corrección.
9. El versionado sale de la matriz de versionamiento de 001/T-001.
10. Esta unidad coordina, no construye funcionalidad.
→ Corríjanme antes de aprobar esta spec.
```

---

## 1. Objetivo

Llevar la app a **Google Play, descargable y funcionando**, antes de la fecha
de evaluación, cumpliendo los requisitos de publicación de Google y los de los
lineamientos.

**Por qué:** es el único entregable del que depende todo lo demás. El punto 1
de los lineamientos condiciona la evaluación entera a poder descargar la app
de la tienda. Un proyecto perfecto que no está publicado vale cero en esa
revisión.

**Para quién:** el profesor que va a evaluar, y los 12 testers de la prueba
cerrada.

**Cómo se ve el éxito:** el 02/12/2026 el profesor busca PetMate en Google
Play, la instala y la usa.

---

## 2. Alcance

| ID | Capacidad |
|---|---|
| A-01 | Cuenta de desarrollador de Google Play activa |
| A-02 | Llave de subida generada y resguardada en secretos de GitHub |
| A-03 | Ficha de la tienda: título, descripciones, capturas e icono |
| A-04 | Formulario de seguridad de datos completo y veraz |
| A-05 | Política de privacidad publicada y enlazada |
| A-06 | Clasificación de contenido respondida |
| A-07 | Flujo de GitHub Actions que sube al track interno |
| A-08 | Prueba cerrada con al menos 12 testers durante 14 días |
| A-09 | Promoción a producción |
| A-10 | Calendario hacia atrás desde la fecha de evaluación |
| A-11 | Procedimiento de reversión si una versión sale defectuosa |

---

## 3. Fuera de alcance

| Qué | Dónde se atenderá |
|---|---|
| Funcionalidad de la app | Specs 002 a 019 |
| El pipeline de compilación y pruebas | Spec 001 (esta unidad lo extiende) |
| El texto legal de la privacidad | Landing page, otro repositorio |
| La ruta de eliminar cuenta en la app | Spec 017 (requisito de Google) |
| Publicación en otras tiendas | No está en los lineamientos |
| Monetización, suscripciones o compras | No está en el producto |
| Marketing o campañas de descarga | No está en los lineamientos |

---

## 4. Requisitos

### Funcionales

| ID | Requisito | Trazabilidad |
|---|---|---|
| R-01 | La app debe estar descargable desde Google Play antes de la evaluación | **Punto 1 (obligatorio)** |
| R-02 | El despliegue debe hacerse desde GitHub | **Punto 16** |
| R-03 | La ficha debe incluir título, descripciones, capturas e icono | Google Play |
| R-04 | El formulario de seguridad de datos debe declarar todo lo recolectado | Google Play |
| R-05 | La política de privacidad debe estar publicada y enlazada | **Punto 16**; Google Play |
| R-06 | La app debe pasar prueba cerrada con 12 testers durante 14 días | Google Play |
| R-07 | El versionado debe salir de la matriz de versionamiento | **Punto 11** |
| R-08 | Debe existir un procedimiento de reversión | DevSecOps |

### No funcionales

| ID | Requisito | Trazabilidad |
|---|---|---|
| R-09 | La llave de firma no debe existir en el repositorio | **Constitución P6** |
| R-10 | El artefacto de producción debe estar minificado y sin depuración | Spec 001 |
| R-11 | Cada subida debe quedar registrada con su versión | Punto 11 |
| R-12 | La app debe cumplir los requisitos de Google para datos de salud | Google Play |

---

## 5. Criterios de éxito

| # | Criterio | Cómo se comprueba |
|---|---|---|
| C-01 | La cuenta de desarrollador está activa y verificada | Acceso a la consola |
| C-02 | La llave de subida está en secretos de GitHub y **no** en el repositorio | Revisión del repositorio y su historial |
| C-03 | Un cambio en la rama principal genera un artefacto firmado y lo sube al track interno | Ejecución del flujo visible en verde |
| C-04 | El `versionCode` del artefacto coincide con la matriz de versionamiento | Comparación con el documento |
| C-05 | La ficha de la tienda está completa y aprobada | Estado en la consola |
| C-06 | El formulario de seguridad de datos declara los datos clínicos y el uso de IA | Revisión del formulario contra las specs 008, 014 y 019 |
| C-07 | La política de privacidad está publicada y el enlace funciona | Apertura del enlace desde la ficha y desde la app |
| C-08 | La ruta de eliminar cuenta es accesible y funciona | Prueba en la app publicada (requisito de 017) |
| C-09 | Hay al menos 12 testers inscritos en la prueba cerrada | Lista en la consola |
| C-10 | La prueba cerrada completó 14 días continuos | Fechas en la consola |
| C-11 | La app está disponible en producción y se puede descargar | Descarga desde un dispositivo ajeno al proyecto |
| C-12 | La app descargada de la tienda abre y permite registrarse | Prueba en dispositivo limpio |
| C-13 | Existe y se probó el procedimiento de reversión | Ensayo documentado |
| C-14 | El calendario hacia atrás está escrito y las fechas límite se cumplieron | Revisión del documento |

---

## 6. Riesgos

| # | Riesgo | Impacto | Mitigación |
|---|---|---|---|
| G-01 | **No se llega al 02/12/2026 por los 14 días de prueba cerrada** | **Crítico** | A-10: calendario hacia atrás; la unidad arranca en paralelo desde el inicio (supuesto 3), no al final |
| G-02 | No se consiguen 12 testers | **Alto** | Reclutar desde el inicio: equipo, familiares, compañeros de clase; meta de 15 para tener margen |
| G-03 | Google rechaza la app y hay que corregir y reenviar | **Alto** | Supuesto 8: se contemplan dos ciclos en el calendario; enviar temprano para tener margen |
| G-04 | La app maneja datos de salud y Google aplica revisión más estricta | Alto | Declaración veraz y completa en el formulario; política de privacidad clara sobre datos clínicos |
| G-05 | La verificación de identidad del desarrollador tarda | **Alto** | Abrir la cuenta y verificarla como primera tarea de todas |
| G-06 | La política de privacidad de la landing no está lista a tiempo | Alto | Dependencia explícita; se escala si la landing se retrasa |
| G-07 | Se pierde la llave de firma y no se puede actualizar la app | **Crítico** | Resguardo de la llave en un lugar seguro y documentado, además del secreto de GitHub |
| G-08 | Una versión defectuosa llega a producción | Medio | R-08: procedimiento de reversión probado (C-13) |
| G-09 | La cuenta personal nueva tiene requisitos adicionales | Alto | Confirmar los requisitos vigentes al abrir la cuenta (Q-01) |

---

## 7. Dependencias

**De esta unidad:**

- `001-arquitectura-base`: pipeline, firma, matriz de versionamiento y
  artefacto endurecido (T-040, T-041).
- `017-ajustes-privacidad`: eliminar cuenta y enlace a la privacidad, ambos
  requisitos de Google.
- El resto del producto: para promover a producción debe haber algo que valga
  la pena descargar.

**Externas:**

- Cuenta de Google Play con su cuota de registro.
- Política de privacidad publicada en la landing (otro repositorio).
- 12 testers reales con cuenta de Google.

**Unidades posteriores que dependen de esta:**

- Ninguna. Es el cierre del proyecto.

---

## 8. Preguntas abiertas

| # | Pregunta | Para quién | Bloquea |
|---|---|---|---|
| Q-01 | ¿Los requisitos vigentes de Google para cuentas personales nuevas siguen siendo 12 testers por 14 días? Confirmar al abrir la cuenta | Adrián | Calendario completo |
| Q-02 | ¿Quién paga la cuota de registro de desarrollador y cuándo? | Equipo | Tarea de cuenta |
| Q-03 | ¿Quiénes son los 12 testers y ya tienen cuenta de Google? | Equipo | Prueba cerrada |
| Q-04 | ¿La landing estará publicada con la política de privacidad y en qué fecha? | Equipo / landing | Envío a revisión |
| Q-05 | ¿El nombre "PetMate" está disponible en Google Play? | Adrián | Ficha de la tienda |
| Q-06 | ¿Quién resguarda la copia de seguridad de la llave de firma? | Equipo | Riesgo G-07 |
| Q-07 | ¿Se publica en producción abierta o con disponibilidad limitada a México? | Equipo / profesor | Configuración de la ficha |

---

## 9. Puerta de aprobación

- [ ] **El calendario hacia atrás está revisado y las fechas son realistas.**
- [ ] Supuesto 3 confirmado: esta unidad arranca en paralelo, no al final.
- [ ] Preguntas abiertas con dueño, en especial Q-01 y Q-03.
- [ ] **Aprobada para pasar a `plan.md`.**
