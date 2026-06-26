# Paint and Petals — Database Schema

## ER Diagram

```mermaid
erDiagram
    USERS ||--o| VENDOR_PROFILES : has
    USERS ||--o{ CARTS : owns
    USERS ||--o{ ORDER_GROUPS : places
    USERS ||--o{ VENDOR_ORDERS : fulfills

    CATEGORIES ||--o{ PRODUCTS : contains
    VENDOR_PROFILES ||--o{ PRODUCTS : sells

    CARTS ||--o{ CART_ITEMS : contains
    PRODUCTS ||--o{ CART_ITEMS : in

    ORDER_GROUPS ||--o{ VENDOR_ORDERS : splits_into
    ORDER_GROUPS ||--o| PAYMENTS : has
    VENDOR_ORDERS ||--o{ ORDER_ITEMS : contains
    PRODUCTS ||--o{ ORDER_ITEMS : references

    USERS {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar first_name
        varchar last_name
        varchar phone
        enum role
        enum vendor_status
        timestamp created_at
    }

    VENDOR_PROFILES {
        bigint id PK
        bigint user_id FK UK
        varchar business_name
        text description
        timestamp approved_at
    }

    CATEGORIES {
        bigint id PK
        varchar name UK
        varchar slug UK
    }

    PRODUCTS {
        bigint id PK
        bigint vendor_id FK
        bigint category_id FK
        varchar name
        text description
        decimal price
        int stock_quantity
        int version
        varchar image_url
        boolean active
    }

    ORDER_GROUPS {
        bigint id PK
        bigint customer_id FK
        enum status
        decimal total_amount
        varchar shipping_street
        varchar shipping_city
        varchar shipping_state
        varchar shipping_zip
        varchar shipping_country
        varchar shipping_phone
        timestamp created_at
    }

    VENDOR_ORDERS {
        bigint id PK
        bigint order_group_id FK
        bigint vendor_id FK
        enum status
        decimal subtotal
    }

    ORDER_ITEMS {
        bigint id PK
        bigint vendor_order_id FK
        bigint product_id FK
        varchar product_name
        int quantity
        decimal unit_price
    }

    PAYMENTS {
        bigint id PK
        bigint order_group_id FK UK
        varchar razorpay_order_id
        varchar razorpay_payment_id
        varchar razorpay_signature
        decimal amount
        enum status
        timestamp verified_at
    }
```

## Tables Summary

| Table            | Purpose                                              |
|------------------|------------------------------------------------------|
| `users`          | All accounts (Customer, Vendor, Admin)               |
| `vendor_profiles`| Business details for approved vendors                |
| `categories`     | Paints, Petals, etc.                                 |
| `products`       | Vendor inventory with optimistic lock version        |
| `carts`          | One cart per authenticated customer                  |
| `cart_items`     | Line items in cart                                   |
| `order_groups`   | Parent checkout (multi-vendor split point)           |
| `vendor_orders`  | Per-vendor sub-order                                 |
| `order_items`    | Snapshot of products at purchase time                |
| `payments`       | Razorpay payment records and verification            |

## Indexes

- `users(email)` — unique login lookup
- `products(vendor_id, active)` — vendor catalog queries
- `products(category_id)` — category filtering
- `vendor_orders(vendor_id, status)` — fulfillment center
- `order_groups(customer_id, created_at)` — order history

## Admin Seed

A single admin account is created on first startup via `AdminDataInitializer`:
- Email from `ADMIN_EMAIL` env var (default: `admin@paintandpetals.com`)
- Password from `ADMIN_PASSWORD` env var (must be set in production)
