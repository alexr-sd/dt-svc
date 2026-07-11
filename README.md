# Delivery Tracker

REST API for tracking driver deliveries. Three features: driver ingestion via CSV, delivery event recording, and metric-based statistics.

---

## How to Run

**Prerequisites**: Java 21, Maven 3.8+

```bash
./mvnw spring-boot:run
```

App starts on `http://localhost:8080`.  
H2 console (dev only): `http://localhost:8080/h2-console`  
- JDBC URL: `jdbc:h2:file:./data/delivery-tracker`
- Username: `sa`, Password: *(empty)*

Swagger UI: `http://localhost:8080/swagger-ui.html`


**Aggregated test report**:
```bash
./mvnw verify
```

Report: [`target/reports/surefire.html`](target/reports/surefire.html)  
Raw XML: [`target/surefire-reports/`](target/surefire-reports)

---

## API

### POST /api/v1/drivers/upload

Upload drivers via CSV. Upsert on `driverId` — safe to re-upload.

```bash
curl -X POST http://localhost:8080/api/v1/drivers/upload \
  -F "file=@src/test/resources/sample-drivers.csv"
```

CSV format:
```
driverId,name,phone,email,region
D001,Alice Martin,+1-555-0101,alice@example.com,NORTH
```

`phone` and `email` are optional. `region`: `NORTH | SOUTH | EAST | WEST` (case-insensitive).

Response `201`:
```json
{ "imported": 5, "skipped": 1, "errors": ["Row 3: invalid region 'nowhere'"] }
```

---

### POST /api/v1/events

Record a delivery status event for a package.

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "packageId": "PKG-001",
    "driverId": "D001",
    "status": "DELIVERED",
    "timestamp": "2024-05-01T14:30:00"
  }'
```

`status`: `PICKED_UP | IN_TRANSIT | DELIVERED | FAILED | RETURNED`

Response `201`: the created event.

---

### GET /api/v1/stats

Query delivery metrics for a time window.

```bash
# Total packages today
curl "http://localhost:8080/api/v1/stats?metric=total_packages"

# Delivery rate for NORTH region, last 7 days
curl "http://localhost:8080/api/v1/stats?metric=delivery_rate&regions=NORTH&from=2024-05-01&to=2024-05-07"

# Failure rate for specific drivers
curl "http://localhost:8080/api/v1/stats?metric=failure_rate&driverIds=D001&driverIds=D002"
```

| Param | Required | Description |
|---|---|---|
| `metric` | yes | `total_packages`, `delivery_rate`, `failure_rate`, `avg_per_day` |
| `from` | no | ISO date, default today |
| `to` | no | ISO date, default today |
| `driverIds` | no | Repeated param, omit = all drivers |
| `regions` | no | Repeated param, omit = all regions |

Date range max: 31 days. `from` must not be after `to`.

Response `200`:
```json
{
  "metric": "delivery_rate",
  "value": 0.857,
  "filters": { "driverIds": [], "regions": ["north"] },
  "dateRange": { "from": "2024-05-01", "to": "2024-05-07" }
}
```

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

## Extensibility

| What to add | Where |
|---|---|
| New metric | New class implementing `MetricCalculator`, annotate `@Component` — auto-discovered |
| New import format | New class implementing `DriverImporter`, annotate `@Component` |
| FAILED delivery alert | New `@EventListener` on `DeliveryEventCreated` |
| PostgreSQL | Change datasource in `application.yaml`, add indexes |
| Auth | Add Spring Security dependency + filter chain |
