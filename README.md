# FinTax — Financial & Income Tax Assistance Platform

A full-stack learning and assistance platform for personal finance and income tax, built with
React, Spring Boot, and MySQL.

## Project Structure

```
fintax-platform/
├── frontend/     React (Vite) — UI
├── backend/      Spring Boot — REST API
├── database/     MySQL schema + seed/demo data
└── README.md
```

## Tech Stack

- **Frontend**: React 18, React Router, Tailwind CSS, Axios
- **Backend**: Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA
- **Database**: MySQL 8

## 1. Database Setup

```bash
mysql -u root -p < database/schema.sql
```

This creates the `fintax_platform` database, all 15 tables, and seed data (roles, categories,
CA specializations, a few demo CA profiles, and two sample articles with FAQs).

## 2. Backend Setup

```bash
cd backend
cp .env.example .env    # fill in your DB credentials and a real JWT secret
```

Export the variables in `.env` (or configure them in your IDE run config / hosting provider),
then run:

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Test it:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","password":"password123"}'
```

**To create an admin user for testing the admin module**: register normally, then in MySQL:
```sql
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN') WHERE email = 'test@example.com';
INSERT INTO admin_users (user_id) SELECT id FROM users WHERE email = 'test@example.com';
```
Log in again afterward — the new JWT will carry the ADMIN role.

## 3. Frontend Setup

```bash
cd frontend
cp .env.example .env    # points at your backend, defaults to http://localhost:8080/api
npm install
npm run dev
```

The app starts on `http://localhost:5173`.

## API Overview

| Module | Base path |
|---|---|
| Auth | `/api/auth` |
| Categories | `/api/categories` |
| Articles | `/api/articles` |
| FAQs | `/api/faqs` |
| CA Directory | `/api/ca` |
| Reviews & Contact Requests | `/api/ca/{id}/reviews`, `/api/ca/{id}/contact-requests` |
| Calculators | `/api/calculators` |
| Saved Articles | `/api/users/me/saved-articles` |
| Notifications | `/api/notifications` |
| Search | `/api/search` |
| Admin | `/api/admin` (requires ADMIN role) |

## Testing

- **Backend**: import the endpoints above into Postman/Insomnia; most GETs are public, most
  mutations require a `Bearer <JWT>` header from `/api/auth/login`.
- **Frontend**: manual QA — register a user, browse Financial Learning and Income Tax pages,
  run a calculator, search for a CA, submit a consultation request, check the dashboard.

## Deployment

| Layer | Where | Config included |
|---|---|---|
| Frontend | Vercel or Netlify | `frontend/vercel.json`, `frontend/netlify.toml` |
| Backend | Render or Railway | `backend/render.yaml`, `backend/Procfile` |
| Database | Any managed MySQL (PlanetScale, Railway, AWS RDS) | run `database/schema.sql` against it |

Steps:
1. Push this repo to GitHub.
2. Deploy the database first; note its host/port/credentials.
3. Deploy `backend/` to Render/Railway, setting the env vars from `backend/.env.example`
   (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`).
4. Deploy `frontend/` to Vercel/Netlify, setting `VITE_API_BASE_URL` to your deployed backend's
   `/api` URL.
5. Update the backend's CORS allowed origins in `SecurityConfig.java` to include your deployed
   frontend URL (currently set to `localhost` for local development).

## Disclaimer

Financial and tax information on this platform is provided for educational purposes only and
is not a substitute for professional tax or legal advice. Verify current rules with official
government sources or a qualified professional.
