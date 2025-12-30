Minimal Angular frontend scaffold for Ega Bank API.

Prerequisites
- Node.js (16+)
- npm
- (optional) Angular CLI globally: `npm i -g @angular/cli`

Install and run

```bash
cd frontend
npm install
# using npx/Angular CLI
npx ng serve --open
```

If you prefer to run without `ng` installed globally, use `npx ng serve`.

Configuration
- The frontend expects the backend at `http://localhost:8080` by default. Edit the `apiBase` constant in `src/app/account.service.ts` and `src/app/auth.service.ts` if needed.

What is included
- Basic login component storing JWT in `localStorage`.
- `AccountService` to call backend endpoints for accounts and transactions.
- Simple accounts list component to display accounts.

This is a starter scaffold — adapt components and styles for your project.
