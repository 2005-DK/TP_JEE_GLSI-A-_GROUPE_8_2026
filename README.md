# Ega Bank API

Backend Spring Boot application for managing clients, accounts and transactions.

Run (maven):

```bash
./mvnw clean package
./mvnw spring-boot:run
```

Quick curl examples:

- Register user:

```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username":"user","password":"pass"}'
```

- Login to receive token:

```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"user","password":"pass"}'
```

- Use token in `Authorization: Bearer <token>` to call protected endpoints.

Postman collection is included in `docs/postman_collection.json`.
Import the collection into Postman and use the built-in tests; the collection extracts the `token`, `clientId` and `accountNumber` into environment variables automatically.

Run the collection from the command line with Newman (CI-friendly):

1. Install newman (requires Node.js/npm):

```bash
npm install -g newman
```

2. Run the collection (use `--env-var` to provide base URL if needed):

```bash
newman run docs/postman_collection.json --env-var "baseUrl=http://localhost:8080" \
	--env-var "token=" --timeout-request 30000
```

Tips:
- Start the API before running the collection:

```bash
./mvnw spring-boot:run
```

- The collection sets `token` as `Bearer <jwt>` in the environment after a successful login; Postman tests use that value for protected requests.
- To run only specific requests or to integrate into CI, use Newman options and CI job steps.

Quick sequence to test manually:

1. Register a user:

```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username":"user","password":"pass"}'
```

2. Login and copy the token from the response:

```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"user","password":"pass"}'
```

3. Use the token to create a client, account and perform deposit/withdraw/transfer as shown in the Postman collection.

Notes:
- Endpoints are protected; include header `Authorization: Bearer <token>` for protected calls.
- The statement endpoint returns CSV suitable for printing/saving.

API Endpoints
-------------

Most endpoints are under `/api`. Summary:

- Auth:
	- `POST /api/auth/register` : body `{ "username","password" }` → register user
	- `POST /api/auth/login` : body `{ "username","password" }` → returns `{ "token":"<jwt>" }`

- Clients:
	- `POST /api/clients` : create client (protected)
	- `GET /api/clients/{id}` : get client (protected)

- Accounts:
	- `POST /api/accounts` : create account. Body `CreateAccountRequest { clientId, type }` (protected)
	- `GET /api/accounts/{accountNumber}` : get account (protected)

- Transactions (deposit/withdraw/transfer):
	- `POST /api/accounts/{accountNumber}/deposit` : body `TransactionRequest { amount }` (protected)
	- `POST /api/accounts/{accountNumber}/withdraw` : body `TransactionRequest { amount }` (protected)
	- `POST /api/accounts/transfer` : body `TransferRequest { fromAccount,toAccount,amount }` (protected)

- Transactions listing & statement:
	- `GET /api/accounts/{accountNumber}/transactions?start=<ISO>&end=<ISO>` : returns all transactions between `start` and `end` (protected)
	- Optional pagination: add `page` (0-based) and `size` params to get a paged response sorted by `timestamp` desc, e.g. `?start=...&end=...&page=0&size=20`.
	- `GET /api/accounts/{accountNumber}/statement` : CSV statement (protected)
	- `GET /api/accounts/{accountNumber}/statement.pdf` : PDF statement (protected)

Examples:

Fetch first page (20) of transactions:

```bash
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/accounts/FR001234.../transactions?start=2025-01-01T00:00:00&end=2025-12-31T23:59:59&page=0&size=20"
```

Fetch PDF statement:

```bash
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/accounts/FR001234.../statement.pdf" --output statement.pdf
```

CI notes:
- The repository includes `docs/postman_collection.json` and a `.github/workflows/newman-ci.yml` workflow that runs the collection with Newman after building the app.

If you want, I can add detailed request/response examples for each endpoint or include OpenAPI/Swagger generation next.
