# DT-SVC

[![CI](https://github.com/alexr-sd/dt-svc/actions/workflows/ci.yml/badge.svg)](https://github.com/alexr-sd/dt-svc/actions/workflows/ci.yml)
[![CodeQL](https://github.com/alexr-sd/dt-svc/actions/workflows/codeql.yml/badge.svg)](https://github.com/alexr-sd/dt-svc/actions/workflows/codeql.yml)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)

REST API implementing three features: driver ingestion via CSV, delivery event recording, and metric-based statistics.

Standard Spring Boot layered architecture: 
- controllers → services → repositories (Spring Data JPA), backed by H2 (file-based DB);
- import formats and stat metrics are pluggable via Spring-discovered beans;
- delivery events use Spring application events for decoupled side-effects.

---

## How to Run

**Prerequisites**: Java 21 

Maven wrapper (`mvnw`) is bundled, no install needed

```bash
./mvnw spring-boot:run
```

App starts on: `http://localhost:8080`  
H2 console: [`http://localhost:8080/h2-console`](http://localhost:8080/h2-console)  
- JDBC URL: `jdbc:h2:file:./data/dt-svc`
- Username: `sa`, Password: *(empty)*

Swagger UI: [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html)


**Build & run artifact**:
```bash
./mvnw package            
java -jar target/dt-svc-*.jar
```

**Test report**:
```bash
./mvnw verify
```

Report: [`target/reports/surefire.html`](target/reports/surefire.html)

---

## Extensibility

| What to add | Where |
|---|---|
| New metric | New class implementing `MetricCalculator`, annotate `@Component` — auto-discovered |
| New import format | New class implementing `DriverImporter`, annotate `@Component` |
| FAILED delivery alert | New `@EventListener` on `DeliveryEventCreated` |
| PostgreSQL | Change datasource in `application.yaml`, add indexes |
| Auth | Add Spring Security dependency + filter chain |

---

## Quick Start (end-to-end)

```bash
# 1. Upload drivers
curl -X POST http://localhost:8080/api/v1/drivers/upload \
  -F "file=@src/test/resources/sample-drivers.csv"

# 2. Record some events
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"packageId":"PKG-001","driverId":"D001","status":"PICKED_UP","timestamp":"2024-05-01T08:00:00"}'

curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"packageId":"PKG-001","driverId":"D001","status":"DELIVERED","timestamp":"2024-05-01T14:00:00"}'

curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"packageId":"PKG-002","driverId":"D002","status":"FAILED","timestamp":"2024-05-01T11:00:00"}'

# 3. Query stats
curl "http://localhost:8080/api/v1/stats?metric=delivery_rate&from=2024-05-01&to=2024-05-01"
# → { "metric": "delivery_rate", "value": 0.5, ... }
# PKG-001 terminal = DELIVERED, PKG-002 terminal = FAILED → 1 of 2 delivered
```

