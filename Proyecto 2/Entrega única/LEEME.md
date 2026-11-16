# Proyecto 2 — Entrega única

## Ejecución

Requiere Java 17. Abra una terminal dentro de `ClubDeportivo`:

- `sh ejecutar.sh administrador`
- `sh ejecutar.sh socio`
- `sh ejecutar.sh empleado`
- `sh ejecutar.sh pruebas`

En el primer inicio se crean automáticamente los datos de demostración si no existe `datos/club-ejemplo.json`. En los siguientes inicios se carga ese archivo y se guarda al salir. Los programas comparten el archivo y deben ejecutarse uno a la vez.

Cuentas de demostración: `admin`, `socio1` a `socio16`, `entrenador`, `entrenador2` y `fisio`. Contraseña de demostración: `demo123`. Son datos ficticios de prueba.

## Eclipse y JUnit

Importe `ClubDeportivo` mediante File → Import → Existing Projects into Workspace. Seleccione Java 17 y JUnit 5 en Build Path. Ejecute `AplicacionAdministrador`, `AplicacionSocio` o `AplicacionEmpleado` como Java Application.

Para JUnit, seleccione el paquete `pruebas` de `srctest` y Run As → JUnit Test. `sh ejecutar.sh pruebas` ejecuta las 150 verificaciones con main; la suite JUnit se ejecuta por separado. No se suman las verificaciones de integración ejecutadas por ambas vías como si fueran casos distintos.

## Acceso a funciones

Socios: opción 8 para servicios deportivos; opción 9 para torneos y compras. Entrenadores y fisioterapeutas: opción 8 para operaciones de su rol y opción 9 para torneos y compras. Administrador: opciones 2 a 10 para torneos, encuentros, premios y participantes externos; opción 11 para administración del club.

Los externos y estudiantes registrados por el administrador se autentican desde la aplicación de socios y acceden al menú de torneos y compras.

## Documentación

`Documentacion/Documento_Proyecto_2.docx`: historias nuevas, banco oficial, decisiones, diseño, pruebas y registro de IA. Las fuentes editables de los diagramas están en `Documentacion/Diagramas`; las imágenes SVG pueden abrirse en cualquier navegador. `modelo_completo.puml` contiene las clases y métodos detectados en el código.

La documentación distingue requisitos del enunciado de decisiones adoptadas para resolver reglas no fijadas, como reparto del recaudo y empates. Complete los integrantes, códigos, sección y profesor antes de entregar. La importación gráfica en Eclipse y la sustentación del grupo quedan a cargo de los integrantes.
