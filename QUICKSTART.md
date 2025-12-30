# 🏦 EGA Bank API - Quick Start Guide

## 30-Second Setup

### 1. Start Backend
```bash
cd ega-bank-api
mvn clean package -DskipTests
java -jar target/ega-bank-api-0.0.1-SNAPSHOT.jar
```
✅ Runs on http://localhost:8080

### 2. Start Frontend  
```bash
cd ega-bank-api/frontend
npm install
npm start
# or:
npx ng serve --project ega-bank-frontend --configuration development
```
✅ Runs on http://localhost:4200

### 3. Access the App
- Open http://localhost:4200 in your browser
- Click "Register" to create account
- Login with your credentials
- View accounts, deposit, withdraw, transfer, download statements

---

## Test with Postman

1. **Open Postman**
2. **Import** `docs/postman_collection.json`
3. **Run Collection** (executes all tests in sequence)
   - Auto-captures tokens & account numbers
   - Tests auth, CRUD, transactions, statements

---

## Key Endpoints

| Method | Endpoint | Auth Required | Purpose |
|--------|----------|---------------|---------|
| POST | `/api/auth/register` | ❌ | Create user account |
| POST | `/api/auth/login` | ❌ | Get JWT token |
| GET | `/api/clients` | ✅ | List all clients |
| POST | `/api/clients` | ✅ | Create new client |
| POST | `/api/accounts` | ✅ | Create account |
| POST | `/api/accounts/{id}/deposit` | ✅ | Deposit money |
| POST | `/api/accounts/{id}/withdraw` | ✅ | Withdraw money |
| POST | `/api/accounts/transfer` | ✅ | Transfer between accounts |
| GET | `/api/accounts/{id}/transactions` | ✅ | View transaction history |
| GET | `/api/accounts/{id}/statement` | ✅ | Download CSV statement |
| GET | `/api/accounts/{id}/statement.pdf` | ✅ | Download PDF statement |

---

## Test Workflow

### Register & Login
```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'

# 2. Login (capture token)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'

# Returns: { "token": "eyJhbGc..." }
```

### Use with Authentication
```bash
TOKEN="your_jwt_token_here"

# Create client
curl -X POST http://localhost:8080/api/clients \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"John",
    "lastName":"Doe",
    "birthDate":"1990-01-01",
    "gender":"M",
    "address":"123 Main St",
    "phone":"+1234567890",
    "email":"john@example.com",
    "nationality":"US"
  }'

# Create account
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"clientId":1,"type":"SAVINGS"}'

# Deposit
curl -X POST http://localhost:8080/api/accounts/FR1234567890/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":500.00}'

# View transactions
curl -X GET "http://localhost:8080/api/accounts/FR1234567890/transactions?start=2025-01-01T00:00:00&end=2026-12-31T23:59:59" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `mvn: command not found` | Use `.\mvnw.cmd` on Windows instead of `mvn` |
| `npm: command not found` | Install Node.js from nodejs.org |
| Port 8080 already in use | Change `server.port` in `application.properties` |
| Angular build fails | Run `npm install --legacy-peer-deps` |
| CORS errors in browser | Backend CORS is disabled; use same domain or configure in `SecurityConfig` |
| JWT token expired | Generate new token by logging in again |

---

## Features Checklist

- ✅ User Authentication (Register/Login with JWT)
- ✅ Client Management (CRUD)
- ✅ Account Management (CRUD)
- ✅ Deposits & Withdrawals (with balance checks)
- ✅ Transfers (between accounts)
- ✅ Transaction History (by date range)
- ✅ Statement Export (CSV & PDF)
- ✅ Global Exception Handling
- ✅ Input Validation
- ✅ Role-Based Access Control
- ✅ Responsive UI (Angular)
- ✅ Comprehensive Testing (Postman)

---

## Next Steps for Production

1. **Database**: Switch from H2 to PostgreSQL/MySQL
2. **CORS**: Configure CORS in `SecurityConfig` for cross-origin requests
3. **Email Notifications**: Add email verification on registration
4. **Rate Limiting**: Add rate limiting to auth endpoints
5. **Logging**: Configure centralized logging (ELK/Splunk)
6. **Monitoring**: Add health checks and metrics (Actuator)
7. **CI/CD**: Set up GitHub Actions or Jenkins pipeline
8. **Docker**: Create Dockerfile for containerization

---

**Everything is ready to go!** 🚀
