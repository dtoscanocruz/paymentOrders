# Registro de Prompts IA

Prompt 01 - Generar openapi.yaml y mapping BIAN
------------------------
Generar el archivo openapi.yaml basado en el PaymentOrderService.wsdl  con el fin de generar  un contrato REST (OpenAPI 3.0) alineado al Service Domain BIAN Payment Initiation (BQ:  PaymentOrder).

	• Leer el WSDL PaymentOrderService.wsdl (operaciones: SubmitPaymentOrder, GetPaymentOrderStatus; tipos XSD).
	• Generar openapi.yaml con paths REST alineados a BIAN bajo /payment-initiation/payment-orders.
	• Incluir esquemas JSON/XML (basados en los elementos XSD), ejemplos y respuestas HTTP (201/200/404).
	• Agregar security scheme (bearer JWT) y metadatos BIAN.
	• Guardar el archivo en src/main/resources/openapi.yaml del proyecto microservicio_payments.


Prompt 02 - Mapping BIAN
------------------------
Añadir patterns para IBAN y currency

	• Añadir pattern para IBAN en debtorIban y creditorIban.
	• Añadir pattern para currency (ISO 4217) en currency.
	• Añadir ejemplos para los campos.
	
	
Prompt 03 - Validación de openapi.yaml
------------------------
Validación formal con una herramienta (openapi-generator)

	• Validación exitosa
	

Prompt 04 - Generar las interfaces y DTOs basados en el archivo openapi.yaml
------------------------
Agregar un pluggin openapi-generator-maven-plugin en el pom.xml para generar las interfaces y DTOs basados en el archivo openapi.yaml

	• Agregar plugin org.openapitools
	• Definir <apiPackage> </apiPackage>
	• Definir <modelPackage> </modelPackage>

	
Prompt 05 - Arquitectura Hexagonal
------------------------
Diseñar la arquitectura hexagonal completa basada en el archivo openapi.xml y en los interfacces/DTOs generados con el plugin openapi-generator-maven-plugin, a continuación crear un mircorservicio en Spring Boot 4 con Spring reactive web, lombok, Spring Data R2DBC, Spring Boot Dev Tools

	• Actualizar pom.xml añadiendo dependencias (Lombok, R2DBC Postgres, validation) y configuración del compiler para Lombok; 
	• Crear los paquetes y clases del diseño hexagonal (application service, ports, adapter, entity, mapper, controller impl, Application) 
	• Compilar con Maven
	

Prompt 06 - Control de errores globales
------------------------	
Crear un Exception en el paquete com.example.hpayments.adapters.rest que capture los excepciones y devuelva un error controlado

	• Crear un manejador global de excepciones reactivo en com.example.hpayments.adapters.rest que capture errores comunes (validación, IllegalArgumentException y Exception) y 
	• Retornar un JSON controlado usando el DTO generado com.example.hpayments.rest.adapter.dto.ErrorResponse. 
	• Crear el archivo GlobalExceptionHandler.java.
	
	
Prompt 06 - Completar servicios en openapi.yaml
------------------------		
Agregar al archivo openapi.yaml un mapeo /payment-initiation/payment-orders/{paymentOrderId} de tipo get para obtener un SubmitPaymentOrderResponse recibiendo como parámetro el paymentOrderId de tipo String, agrega las respuestas para 200 con el schema SubmitPaymentOrderResponse y 404 con el esquema ErrorResponse

	• Agregar servicio /payment-initiation/payment-orders/{paymentOrderId}
	• Validar openapi.yaml generado con herramienta (openapi-generator)

	
Prompt 07 - Generación de pruebas unitarias
------------------------		
Generar pruebas unitarias en todas las capas utilizando JUnit5 y Mockito

	• Crear pruebas unitarias para paquetes y clases (application service, ports, adapter, entity, mapper, controller impl, Application) 
	

Prompt 08 - Implementar auditoría
------------------------		
Implementar auditoría persistente reactiva en DB (R2DBC) con MySql
- Crear una tabla  "audit_log" con columnas indexadas: requestId, timestamp, path
- Capturar: requestId, path, method, user, timestamp start/end, elapsetime, status, error
- Integrar correlationId (X-Request-ID) 
- Configurar Logback para JSON con campos: timestamp, level, logger, requestId, path, user, message y añadir el requestId en cada registro.
- Enmascarar datos sensibles
- Adoptar las clases en el esquema de arquitectura hexagonal

	• Se creó la tabla `audit_log` (ver `src/main/resources/schema.sql`). Columnas indexadas para búsquedas rápidas: `request_id`, `created_at` (timestamp) y `path`.	
	• La aplicación lee `X-Request-ID` si existe; si no, genera un `requestId` y lo añade al header de respuesta `X-Request-ID` para permitir correlación desde el cliente.
	• La auditoría se realiza por medio del `WebFilter` reactivo `com.example.hpayments.adapter.inbound.AuditWebFilter` y persiste entradas en forma reactiva usando Spring Data R2DBC.
	• Campos auditados
		
		- requestId (X-Request-ID)
		- traceId (extraído de headers comunes: `traceparent`, `b3`, `X-B3-TraceId`, `X-Cloud-Trace-Context`)
		- path (URI)
		- method (HTTP method)
		- username (si el principal está presente, de lo contrario `anonymous`)
		- started_at, ended_at (timestamps)
		- elapsed_ms (duración en ms)
		- status (HTTP status)
		- error_message (si aplica)
		- payload (opcional; actualmente no capturado por defecto)
		
	• Mascara de datos sensibles
	
	
Prompt 09 - Implementar mecanismo para health
------------------------			
Añadir Endpoint de salud /actuator/health con Spring Boot Actuator

	• Añadir dependencia spring-boot-starter-actuator a pom.xml.
	• Configurar application.properties para exponer health (y info) y mostrar detalles en dev.
	• Ejecutar mvn test y verificar BUILD SUCCESS.
	

Prompt 10 - Generar pruebas de integración
------------------------		
Generar pruebas de integración con WebTestClient y con base de datos MySQL en memoria

	•  Modificado: pom.xml
	•  Añadida dependencia test io.r2dbc:r2dbc-h2:1.0.0.RELEASE.
	•  Añadido: src/test/resources/schema-test.sql
	•  Contiene DDL para la tabla audit_log (H2 compatible).
	•  Añadido: src/test/java/com/example/hpayments/AuditLogRepositoryIntegrationTest.java
	•  Test que usa @DataR2dbcTest, importa R2dbcConfiguration y configura spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;MODE=MySQL.	