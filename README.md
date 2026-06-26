# Paint and Petals

Multi-vendor e-commerce platform for artisan paints, art supplies, and floral arrangements.

## Stack

- **Backend:** Java 17, Spring Boot 3.3, Spring Security JWT, Spring Data JPA
- **Frontend:** React 18, Vite, Tailwind CSS, Axios, React Router
- **Database:** PostgreSQL 16
- **Payments:** Razorpay (sandbox)
- **Deployment:** Docker Compose

## Quick Start

```bash
cp .env.example .env
# Edit .env with your Razorpay sandbox keys

docker compose up --build
```

| Service  | URL                        |
|----------|----------------------------|
| Frontend | http://localhost:3000      |
| API      | http://localhost:8080/api  |

### Default Admin (seeded on first run)

- Email: `admin@paintandpetals.com` (or `ADMIN_EMAIL`)
- Password: value of `ADMIN_PASSWORD` in `.env`

## Local Development (without Docker)

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Requires PostgreSQL running locally. Configure `application-dev.yml` or env vars.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Database Schema](docs/DATABASE_SCHEMA.md)

## API Overview

| Endpoint prefix        | Access    |
|------------------------|-----------|
| `/api/auth/**`         | Public    |
| `/api/products/**`     | Public    |
| `/api/categories/**`   | Public    |
| `/api/cart/**`         | Customer  |
| `/api/checkout/**`     | Customer  |
| `/api/orders/**`       | Customer  |
| `/api/vendor/**`       | Vendor    |
| `/api/admin/**`        | Admin     |
| `/api/payments/**`     | Mixed     |

## License

MIT
