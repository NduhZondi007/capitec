# Transaction Aggregation API

This project implements a **Transaction Aggregation API** for a Capitec‐like retail banking system.  The service ingests
transaction data from multiple sources (in this example a CSV file), enriches each transaction with a spending
category and exposes a set of RESTful endpoints for retrieving aggregated information such as per‑customer spending,
top spenders and most popular categories.  The goal is to mirror real banking insights like those found in Capitec’s
app, where customers can see where their money goes【31†L163-L170】.

## Overview

* **Data ingestion:** On startup the application loads a CSV file located at `src/main/resources/data/transactions.csv`.
  Each row contains a transaction with fields such as customer, timestamp, merchant and amount.  If a
  category is not provided, the loader uses a simple keyword lookup to assign one of several broad categories (Food,
  Transport, Utilities, Entertainment, Shopping, Healthcare, Communication, Education, Travel, Income, Other).
* **Categorisation:**  Transaction enrichment is important for personal finance management【16†L231-L235】.  Keywords like
  “uber” or “gas” will map to the Transport category.  You can extend the `DataLoader`’s keyword map or supply your
  own categorised dataset.
* **Aggregation:**  The service aggregates transactions per customer or across all customers.  It calculates total
  spend, a breakdown by category and identifies the top category for the given period.  Top spenders and top
  categories endpoints help identify “most valuable” customers or spending trends.
* **Security:**  Endpoints are protected with JWT‑based authentication.  Users can register and log in to obtain a
  token.  Customers may only access their own data; administrators (role `ROLE_ADMIN`) can view global summaries and
  other users’ data.
* **Tech stack:**  Built with Java 17 and Spring Boot 3, using Spring Data JPA for persistence, Spring Security with
  JWT for authentication and an H2 in‑memory database.  The application is packaged with a multi‑stage Dockerfile for
  easy deployment.

## Getting Started

### Prerequisites

* JDK 17+
* Maven 3.8+
* Docker (optional if you want to run via container)

### Building and Running locally

Clone the repository (or copy the contents into a new git repo) and run:

```bash
mvn clean package
java -jar target/transaction-api-0.0.1-SNAPSHOT.jar
```

The service will start on port `8080` and automatically load the sample CSV file.  You can observe the logs to see
how many customers and transactions were loaded.  To test the API you first need to register a user:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}'

# note the returned token and use it in the Authorization header as "Bearer <token>"
```

Alternatively, register an admin by including the role:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin","role":"ROLE_ADMIN"}'
```

### Running with Docker

To build and run the container:

```bash
docker build -t transaction-api .
docker run -p 8080:8080 transaction-api
```

The Dockerfile uses a two‑stage build to produce a lightweight image containing only the compiled jar and a JRE.  By
default the H2 database is in memory; if you need persistence across restarts, switch the JDBC URL in
`application.properties` to a file or external database.

## API Endpoints

All endpoints are prefixed with `/api/v1` and return JSON.  Use the `Authorization: Bearer <token>` header for all
secured requests.

### Authentication

| Method | Endpoint               | Description                                      |
|-------:|-----------------------|--------------------------------------------------|
| POST   | `/auth/register`      | Register a new user.  Body: `{username,password,role?}` |
| POST   | `/auth/login`         | Authenticate and obtain a JWT token.  Body: `{username,password}` |

If `role` is omitted during registration the user will receive the default `ROLE_USER`.

### Customer APIs

| Method | Endpoint                                       | Access                         | Description |
|-------:|-------------------------------------------------|--------------------------------|-------------|
| GET    | `/customers/{id}/summary`                       | User or Admin                  | Returns total spend, per‑category breakdown and top category for a customer. Optional `from`/`to` query params (ISO date) limit the period. |
| GET    | `/customers/{id}/transactions`                  | User or Admin                  | Returns raw transactions for the customer.  Optional `from`/`to` to filter by date. |
| GET    | `/categories/top-categories?customerId={id}`    | User (own) or Admin            | Returns the top spending categories for a customer.  `count` query param controls the number of categories returned (default 5). |
| GET    | `/customers/top-spenders?count={n}`             | Admin only                     | Lists the top `n` customers by total spend. |
| GET    | `/summary/overall`                              | Admin only                     | Returns aggregated totals and top category across all customers. Optional date filters. |
| GET    | `/categories/top-categories`                    | Admin only                     | Returns top categories across all customers (without `customerId`). |

### Examples

Fetch the summary for customer 1 (assuming your token is stored in `$TOKEN`):

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/customers/1/summary
```

Fetch top three spenders between January and March 2025:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/customers/top-spenders?count=3&from=2025-01-01&to=2025-03-31"
```

You can also open the automatically generated Swagger UI at `http://localhost:8080/swagger-ui/index.html` to explore
the API interactively.

## Testing

Run the test suite with:

```bash
mvn test
```

Tests are written with JUnit 5 and Spring Boot’s test framework.  A sample integration test verifies that
aggregations work correctly on the preloaded dataset.

## Extending the project

* **Add more data sources:**  Replace or augment `transactions.csv` with other CSVs or integrate with external APIs.
* **Improve categorisation:**  Use merchant category codes (MCC) or third‑party enrichment services to assign
  categories more accurately【16†L231-L235】.  You could also persist merchant/category mappings in a database.
* **Persist to a real database:**  Swap H2 for PostgreSQL or MySQL by changing the datasource properties.  Spring Data
  JPA will automatically create the schema.
* **Metrics and monitoring:**  Enable Spring Boot Actuator to expose health and metrics endpoints【33†L260-L267】 and
  integrate with Prometheus/Grafana.

## License

This project is provided as a sample and does not carry a specific software license.  You are free to use and modify
it for educational or evaluation purposes.