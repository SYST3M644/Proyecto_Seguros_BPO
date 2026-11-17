# Microservicios Java de Seguros BPO

Tres aplicaciones independientes Spring Boot con Java 21 y Maven. Un API Gateway con Spring Cloud Gateway centraliza las rutas y la documentación Swagger/OpenAPI.

| Servicio | Puerto | Responsabilidad |
| --- | --- | --- |
| api-gateway | 8080 | Entrada única REST y Swagger UI unificado |
| catalogos-service | 8081 | Compañías activas e inactivas, líneas de seguro, tipos de siniestro y documentos habituales |
| siniestros-service | 8082 | Denuncia, seguimiento, asignación, historial, cambios de etapa, documentos y avisos persistentes |

El catálogo contiene exclusivamente los datos ficticios de los anexos. Los siniestros se almacenan en H2 (archivo local `datos/siniestros.mv.db`), con JPA. No hay conexiones a aseguradoras reales.

## Ejecutar todo con Docker (recomendado)

Instala **Docker Desktop** con Docker Compose v2. Para levantar los tres servicios sin instalar Java/Maven localmente, desde la carpeta `backend-java`:

```bash
docker compose up --build -d --wait
```

Accesos desde el computador:
- **Swagger UI unificado:** http://localhost:8080/swagger-ui.html
- **Gateway REST:** http://localhost:8080/api/v1/
- **Estado del Gateway:** http://localhost:8080/api/v1/health

Los servicios internos escuchan en los puertos 8081 (Catálogos) y 8082 (Siniestros), sin exponerlos al computador. Docker Compose utiliza red interna entre los tres procesos. El puerto del Gateway se publica solo en `127.0.0.1` y puede cambiarse con `GATEWAY_PORT` en un fichero `.env` creado a partir de `.env.example`. Si cambias el puerto publicado, cambia también las URL del navegador.

Comandos para administrar el entorno:

```bash
docker compose ps
docker compose logs -f api-gateway
docker compose down
```

La base H2 reside en el volumen nombrado `siniestros-data`, y **`docker compose down` no lo elimina**. No utilices `docker compose down -v` si necesitas conservar los siniestros. Los Dockerfiles emplean build Maven multietapa con Java 21, imagen JRE para ejecución y usuario no privilegiado. El Gateway mantiene la URL Android `http://10.0.2.2:8080/api/v1/` cuando se ejecuta en un emulador Android.

Pruebas automáticas: `mvn clean verify` ejecuta los tests Java y el workflow **Backend Java Docker Compose CI** construye los contenedores, espera el estado saludable y ejecuta pruebas REST y Swagger a través del Gateway. Para repetir localmente las verificaciones HTTP de CI necesitas `curl`, `jq` y Bash:

```bash
bash scripts/docker-smoke.sh
bash scripts/docker-e2e.sh
```

El backend académico usa encabezados de rol simulados, **no tiene autenticación segura**, por lo que no se debe publicar el Gateway hacia Internet ni utilizar datos reales. Los contenedores no constituyen un despliegue productivo.

## Ejecutar sin Docker

Necesitas JDK 21 y Maven 3.6.3 o superior.

Desde `backend-java`:

```bash
mvn clean verify
mvn -pl catalogos-service spring-boot:run
```

En una segunda terminal, también dentro de `backend-java`:

```bash
mvn -pl siniestros-service spring-boot:run
```

En una tercera terminal:

```bash
mvn -pl api-gateway spring-boot:run
```

El API Gateway ofrece la única URL pública de desarrollo: `http://localhost:8080/api/v1/`. Los servicios internos ejecutan en `http://localhost:8081` (catálogos) y `http://localhost:8082` (siniestros). En un emulador Android, el host local se alcanza mediante `http://10.0.2.2:8080/api/v1/`, manteniendo el contrato existente de Retrofit.

## Swagger / OpenAPI

Con los tres procesos ejecutándose, abre **http://localhost:8080/swagger-ui.html** y selecciona **Siniestros** o **Catalogos** en el desplegable de especificaciones.

| URL | Función |
| --- | --- |
| `http://localhost:8080/swagger-ui.html` | Swagger UI unificado |
| `http://localhost:8080/openapi/siniestros` | Especificación OpenAPI del servicio de siniestros a través del Gateway |
| `http://localhost:8080/openapi/catalogos` | Especificación OpenAPI del servicio de catálogos a través del Gateway |
| `http://localhost:8082/swagger-ui.html` | Swagger del servicio de siniestros directamente |
| `http://localhost:8081/swagger-ui.html` | Swagger del servicio de catálogos directamente |

Desde Swagger, los endpoints de siniestros requieren los encabezados **`X-Actor-Id` y `X-Role`** para la identidad simulada del MVP. Por ejemplo, `X-Actor-Id: 15.842.371-5` y `X-Role: ASEGURADO`. Estos encabezados no constituyen autenticación y pueden ser suplantados.

El Gateway reenvía las rutas `/api/v1/siniestros/**` y `/api/v1/avisos/**` al servicio de siniestros, y `/api/v1/companias/**` y `/api/v1/tipos-siniestro/**` al servicio de catálogos. La ruta `/api/v1/health` indica el estado del propio Gateway. Las URLs internas pueden cambiarse mediante `SINIESTROS_BASE_URL` y `CATALOGOS_BASE_URL` en el entorno del Gateway.

Las pruebas de integración incluyen contratos OpenAPI generados y enrutamiento HTTP a servidores simulados, sin llamadas reales ni dependencia de los sistemas de una aseguradora.

## Flujo de ejemplo

El encabezado `X-Role` puede ser `ASEGURADO`, `ANALISTA` o `LIQUIDADOR`. El encabezado `X-Actor-Id` identifica el RUT ficticio o el colaborador. Son identidades simuladas **solo para la demostración académica**, no autenticación de producción.

```bash
curl -X POST http://localhost:8080/api/v1/siniestros \
  -H 'Content-Type: application/json' \
  -H 'X-Role: ASEGURADO' -H 'X-Actor-Id: 15.842.371-5' \
  -d '{"compania":"ASG-01","tipo":"TS-01","fechaOcurrencia":"2026-09-02","comuna":"Maipú","descripcion":"Choque de prueba"}'
```

La respuesta devuelve un identificador `SIN-AA-NNNNNN`. Reemplaza `{numero}` por ese identificador en los siguientes endpoints:

| Método | Endpoint | Perfil |
| --- | --- | --- |
| POST | `/api/v1/siniestros` | Asegurado |
| GET | `/api/v1/siniestros` | Los tres perfiles, con filtros por propietario/asignación |
| GET | `/api/v1/siniestros/{numero}` | Titular, liquidador asignado o analista |
| GET | `/api/v1/siniestros/{numero}/historial` | Los tres perfiles; sin gestiones internas para el asegurado |
| POST | `/api/v1/siniestros/{numero}/asignacion` | Analista |
| POST | `/api/v1/siniestros/{numero}/gestiones` | Liquidador asignado |
| POST | `/api/v1/siniestros/{numero}/solicitudes` | Liquidador asignado |
| GET | `/api/v1/siniestros/{numero}/solicitudes` | Titular y colaboradores autorizados |
| POST | `/api/v1/siniestros/{numero}/documentos` | Titular o liquidador; multipart `archivo`, `tipo`, `solicitudId` opcional |
| GET | `/api/v1/siniestros/{numero}/documentos` | Titular y colaboradores autorizados |
| GET | `/api/v1/siniestros/{numero}/documentos/{documentoId}` | Titular y colaboradores autorizados |
| POST | `/api/v1/siniestros/{numero}/etapa` | Liquidador asignado |
| POST | `/api/v1/siniestros/{numero}/desistimiento` | Asegurado |
| GET | `/api/v1/avisos` | Asegurado |
| PATCH | `/api/v1/avisos/{avisoId}/leido` | Asegurado |

La respuesta de detalle conserva los campos ya utilizados por Retrofit: `id`, `tipo`, `fechaOcurrencia`, `fechaReporte`, `estado`, `equipoAsignado`, `ultimaActualizacion`. Para conectarlo al cliente Android, el cliente HTTP deberá incluir los dos encabezados de identidad simulada y disponer de las nuevas funciones REST.

## Reglas incluidas

- RUT válido; compañía activa y compatible con el siniestro; fecha de ocurrencia no futura; reportes de más de 30 días identificados.
- Numeración única; siniestros nuevos en `Recibido`; asignación compatible con Vehículos/Hogar.
- Avance ordenado: `Recibido` → `En evaluación` → `En liquidación` → `Cerrado`, con cierres anticipados permitidos por el caso.
- Bloqueo del paso a liquidación con solicitudes pendientes; cierre con resultado y mensaje.
- Documentos en PDF/PNG/JPEG, hasta 5 MiB y hasta 15 por siniestro; no admite adjuntos en casos cerrados.
- Gestiones internas ocultas; avisos persistidos ante asignación, cambios de etapa, solicitud y cierre.

## Alcance y límites

Es un backend académico con almacenamiento local H2 y un Gateway de desarrollo que no incorpora autenticación. No incluye inicio de sesión real, control de permisos criptográficamente seguro, notificaciones push externas ni sincronización offline del móvil. Los encabezados de identidad pueden suplantarse, por lo que **no debe exponerse públicamente**. El frontend del ZIP se conserva intacto y su integración y pruebas instrumentadas deben verificarse por separado. Los datos de demostración deben ser exclusivamente ficticios. No se realizan pagos ni operaciones con aseguradoras.
