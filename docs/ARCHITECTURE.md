# Paint and Petals — System Architecture

## Overview

Paint and Petals is a multi-vendor e-commerce platform for artisan paints, art supplies, and floral arrangements. The system uses a **monorepo** with a Spring Boot API, React SPA, and PostgreSQL database, orchestrated via Docker Compose.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (Browser)                          │
│              React 18 + Vite + Tailwind + Axios                  │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTPS / REST (JWT Bearer)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Spring Boot 3.x API (Java 17)                  │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────────┐ │
│  │ Spring       │ │ Spring       │ │ Spring Data JPA          │ │
│  │ Security JWT │ │ Web MVC      │ │ (Hibernate + PostgreSQL) │ │
│  └──────────────┘ └──────────────┘ └──────────────────────────┘ │
│  ┌──────────────┐ ┌──────────────┐                                │
│  │ Razorpay SDK │ │ @Controller  │                                │
│  │ (Payments)   │ │ Advice       │                                │
│  └──────────────┘ └──────────────┘                                │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   PostgreSQL    │
                    └─────────────────┘
```

## User Roles & RBAC

| Role     | Registration              | Capabilities                                      |
|----------|---------------------------|---------------------------------------------------|
| CUSTOMER | Public self-register      | Browse, cart, checkout, order history             |
| VENDOR   | Request → Admin approval  | Inventory, fulfillment, sales analytics           |
| ADMIN    | Seeded at startup (1 only)| Vendor approval, platform KPIs, customer CRM      |

## Multi-Vendor Order Flow

1. Customer adds products from Vendor A and Vendor B to cart.
2. Checkout creates an **OrderGroup** (parent) with shared shipping address.
3. Backend splits into **VendorOrder** sub-orders (one per vendor).
4. Single Razorpay payment covers the OrderGroup total.
5. Webhook/signature verification moves all sub-orders from `PENDING_PAYMENT` → `PLACED`.
6. Each vendor sees only their sub-orders in the fulfillment center.

## Inventory Concurrency

- Products use `@Version` (optimistic locking) on stock quantity.
- Checkout uses pessimistic write lock (`PESSIMISTIC_WRITE`) when decrementing stock.
- Failed lock/version conflict returns HTTP 409 with a clear message.

## Order Status Workflow

```
PENDING_PAYMENT → PLACED → PROCESSING → SHIPPED → DELIVERED
                      └──────────────────→ CANCELLED
```

## Deployment

```bash
docker compose up --build
```

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080/api`
- PostgreSQL: `localhost:5432`

## Environment Variables

See root `.env.example` and `backend/src/main/resources/application.yml`.
