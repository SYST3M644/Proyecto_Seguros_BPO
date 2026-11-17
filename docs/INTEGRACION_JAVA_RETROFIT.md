# Seguros BPO · integración para repositorio oficial

Esta entrega complementa, sin sustituir, la aplicación Android Kotlin de Abel y Nicolás en `SYST3M644/Proyecto_Seguros_BPO`.

## Qué contiene

- Aplicación Kotlin Compose y SQLite original del repositorio oficial.
- Cliente Retrofit que consume el API Gateway Java desde un emulador Android.
- Dos microservicios Java Spring Boot: Catálogos y Siniestros.
- Pruebas unitarias Kotlin y Java, smoke Docker, y prueba instrumentada de Android contra el servidor.

## Historial organizado

El procedimiento crea cuatro commits reales y separados, registrados bajo la identidad de Git configurada en el equipo que lo ejecuta:

1. `feat(backend): integrar microservicios Java, gateway y Docker`
2. `feat(android): agregar Retrofit y configuracion de red`
3. `feat(android): conectar reporte, consulta e historial con Java`
4. `test(ci): certificar Android, Java y comunicacion REST`

Los commits no se atribuyen a Abel ni a otros colaboradores por el simple hecho de copiar su trabajo. Se mantiene todo el historial preexistente del repositorio oficial.

## Alcance comprobable

La conexión Retrofit se ha probado antes en `Nicricht/DesarrolloMovil`, pero es necesario ejecutar nuevamente la CI sobre el repositorio oficial después de aplicar los cambios. El backend usa identificaciones simuladas y datos académicos ficticios.

Las demás pantallas Android siguen usando SQLite local; aún no existe sincronización bidireccional completa de documentos, notificaciones y cambios de etapa entre teléfonos.

## Ejecución académica

Backend en el computador: `cd backend-java` y `docker compose up --build -d --wait`.
Android Studio: abrir la raíz del repositorio oficial, donde está `settings.gradle.kts`, y ejecutar en emulador Android. Retrofit usa `10.0.2.2:8080`, que solo apunta al host del emulador. Un teléfono físico requeriría una dirección accesible distinta.

## Fuente de los cambios

Código Retrofit y backend de `Nicricht/DesarrolloMovil`, rama `Nicricht-mainOtro`, PR #29. Adaptación destinada a preservar la versión oficial actual y no reemplazar archivos de Abel.
