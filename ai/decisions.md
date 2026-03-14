# Decisiones e Intervención Humana

- El archivo openapi.yaml generado fue ajustado para soportar el servicio: GET /payment-initiation/payment-orders/{id}, dado que generaba únicamente 2 servicios.
- Se incorporaron validaciones de campos para `debtorIban` y `creditorIban` y `currency` alineados con BIAN.
- Se corrigieron tipos de datos del OpenAPI (amount definido como BigDecimal).
- La estructura hexagonal propuesta fue reorganizada para cumplir el estándar interno (application/service separado de ports).
- Las pruebas unitarias generadas por IA fueron ajustadas y requirieron varias interaciones para obtener el nivel de cobertura deseado.
- Las pruebas de integración generadas por IA fueron ajustadas y requirieron varias interaciones para obtener el nivel de cobertura deseado.
- Revisión de código para alineación real con arquitectura hexagonal.
