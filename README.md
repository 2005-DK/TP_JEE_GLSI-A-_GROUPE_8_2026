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
 
Environment variables
---------------------

The application reads some settings from `application.properties` and from environment variables. Important ones:

- `JWT_SECRET` (recommended): the secret used to sign JWT tokens. You can also set `jwt.secret` in `application.properties` for development. If no secret is provided the application will generate an ephemeral key (development only).
- `jwt.expiration-ms` (optional): token validity in milliseconds. Default is set in `application.properties`.

Set the `JWT_SECRET` in PowerShell (session):

```powershell
$env:JWT_SECRET = "your-very-secret-value"
```

Or on Unix/macOS:

```bash
export JWT_SECRET="your-very-secret-value"
```

Security note: prefer injecting secrets using your CI/CD secrets store or a secrets manager (Vault, GitHub Secrets, etc.).

Run & tests
-----------

Run application (development):

Windows (PowerShell):

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Unix/macOS:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

Run unit & integration tests:

```bash
.\mvnw.cmd test
```

Run Postman collection locally with Newman (requires Node.js):

```bash
npm install -g newman
newman run docs/postman_collection.json --env-var "baseUrl=http://localhost:8080" --timeout-request 30000
```

Submission & checklist
----------------------

Please follow the `SUBMISSION.md` instructions in the repository root. Quick checklist:

- Ensure the repository name follows the required format: `TP_JEE_GLSI<A|B>_GROUPE_<NumGroupe>_2026`.
- Verify each member has at least one commit associated with their GitHub account.
- Add the three GitHub usernames in `README.md` (Authors section).
- Create a Git tag for the final submission, e.g. `v1.0-submission` and push it.
- Optionally create a release and attach a ZIP of the project (CI can produce artefact).

Next steps I can help with:

- Add detailed request/response examples or OpenAPI/Swagger.
- Finalize `SUBMISSION.md` with a compact checklist and tag/release commands.
- Scaffold a minimal Angular frontend that calls the APIs.

If you want me to finalize `SUBMISSION.md` now, choose that option and I will update the file with a submission checklist and commands.
