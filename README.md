# Microservicios de Pagos


Resumen
------------------------
`microservicio_hpayments` es un microservicio Spring Boot (reactivo, R2DBC) que gestiona órdenes de pago y registra auditoría persistente por petición HTTP. Incluye:
- Endpoints REST para crear/consultar órdenes de pago.
- Persistencia reactiva con R2DBC hacia MySQL.
- Auditoría por petición en la tabla `audit_log` (requestId, path, method, user, timestamps, status, payload restringido).
- Actuator para monitoreo (endpoint `/actuator/health`).


Requerimientos
--------------
- JDK 17 (solo si compilas/ejecutas localmente)
- Maven (se incluye `mvnw`/`mvnw.cmd` para facilitar builds)
- Docker & Docker Compose (para ejecutar con contenedores)
- Opcional: MySQL 8.0 si ejecutas la aplicación contra una instancia local en vez de Docker


Instalación y Ejecución
-----------------------
1) Clona el repositorio y entra al directorio del proyecto:

```cmd
cd D:\desarrollo\workspace\microservicio_hpayments
```

2) Copia el `.env.example` a `.env` y ajusta las variables si lo deseas:

```cmd
copy .env.example .env
```

3) Levanta con Docker Compose (la primera vez ejecutará los scripts de inicialización `docker/mysql/init`):

```cmd
docker compose up --build -d
```


Compilación local
-----------------
Para compilar y generar el JAR localmente (Windows):

```cmd
D:\desarrollo\workspace\microservicio_hpayments\mvnw.cmd -DskipTests package
```

El JAR resultante se encontrará en `target/`.


Generación de cobertura de pruebas
----------------------------------
El reporte de cobertura se realiza con JaCoCo. Ejemplo:

```cmd
D:\desarrollo\workspace\microservicio_hpayments\mvnw.cmd test jacoco:report
```

El reporte se genera en `target/site/jacoco/index.html`.


Pruebas de Integración
----------------------
Este proyecto incluye pruebas de integración R2DBC que se ejecutan en memoria usando H2 (modo MySQL) y pruebas end-to-end con Testcontainers / MySQL

Tests incluidos y rutas relevantes:
- `src/test/java/com/example/hpayments/AuditLogRepositoryIntegrationTest.java` — inserta y consulta `audit_log` usando `Spring Data R2DBC`.
- `src/test/java/com/example/hpayments/PaymentOrderRepositoryIntegrationTest.java` — crea la tabla `payment_orders`, prueba `SpringDataPaymentOrderRepository` y `PaymentOrderRepositoryAdapter`.

Ejecutar las pruebas R2DBC (H2 en memoria):


```cmd
mvnw.cmd -Dtest=AuditLogRepositoryIntegrationTest,PaymentOrderRepositoryIntegrationTest test
```


Servicio de monitoreo `actuator/health`
---------------------------------------
- El proyecto expone los endpoints de Actuator `health` e `info` (configurado en `src/main/resources/application.properties`).
- Endpoint de salud:
  - `http://localhost:8080/actuator/health`
  - Respuesta esperada cuando la app está OK: JSON con status `UP`.

Hay un helper para esperar hasta que la app esté `UP`.
- Windows:
  - `scripts\check-health.bat`
- Unix:
  - `scripts/check-health.sh`

También puedes ejecutar manualmente:

```cmd
docker compose run --rm healthcheck
```


Servicio de auditoría
---------------------
- El proyecto incluye auditoría persistente reactiva en base de datos (R2DBC). 
- La auditoría registra una fila por petición HTTP con datos clave: requestId (X-Request-ID), path, method, usuario, timestamps (start/end), elapsed time, status, error
- Los logs también incluyen el requestId/traceId en JSON.
- Las tablas y esquemas necesarios están en `src/main/resources/schema.sql` 
- La tabla `audit_log` registra una fila por petición con campos indexados para búsqueda: `request_id`, `created_at`, `path`.
- Para evitar duplicados en desarrollo se añadió `docker/mysql/init/00-cleanup.sql` que elimina las tablas antes de crear el esquema cuando el volumen es nuevo.
- Utilidad que aplica reglas básicas para enmascarar números tipo tarjeta, CVV y tokens.



Servicios expuestos
-------------------

  - POST /payment-initiation/payment-orders
  
  	Request:
  
  ```cmd
	  {
	    "externalId": "ext-1314",
	    "debtorIban": "ES7921000813610123456789",
	    "creditorIban": "ES7921000813610123456789",
	    "amount": 100.00,
	    "currency": "EUR",
	    "remittanceInfo": "Pago factura 2026-03",
	    "requestedExecutionDate": "2026-03-15"
	}
  ```
  
  	Response:
  
  ```cmd
	{
	    "paymentOrderId": "ext-1314",
	    "status": "PENDING"
	}
  ```
  - GET /payment-initiation/payment-orders/{id}
  
  	Response:

  ```cmd
	{
	    "paymentOrderId": "ext-1314",
	    "status": "PENDING"
	}
  ```
  - GET /payment-initiation/payment-orders/{id}/status
  
  	Response:

  ```cmd
	{
	    "paymentOrderId": "ext-1314",
	    "status": "PENDING",
	    "lastUpdate": "2026-03-13T22:41:17Z"
	}
  ```



Comandos útiles
---------------
- Ver tablas creadas en la DB:

```cmd
docker exec payments-db mysql -uroot -p%DB_ROOT_PASSWORD% -e "SHOW TABLES IN payments;"
```

- Vaciar tablas en ejecución (si necesitas limpiar estado sin recrear volúmenes):

```cmd
docker exec payments-db mysql -uroot -p%DB_ROOT_PASSWORD% -e "TRUNCATE TABLE payments.payment_orders; TRUNCATE TABLE payments.audit_log;"
```

- Ejecutar manualmente los scripts de init contra la DB en ejecución:

```cmd
docker exec -i payments-db mysql -uroot -p%DB_ROOT_PASSWORD% payments < docker\mysql\init\00-cleanup.sql
```