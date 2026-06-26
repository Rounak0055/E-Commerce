# Backend Project Structure

```
backend/
├── pom.xml
├── Dockerfile
└── src/main/
    ├── java/com/paintandpetals/
    │   ├── PaintAndPetalsApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java          # JWT + RBAC filter chain
    │   │   ├── JwtProperties.java
    │   │   ├── RazorpayProperties.java
    │   │   ├── AdminProperties.java
    │   │   └── DataInitializer.java         # Admin seed + categories
    │   ├── security/
    │   │   ├── JwtTokenProvider.java
    │   │   ├── JwtAuthenticationFilter.java
    │   │   ├── CustomUserDetailsService.java
    │   │   └── SecurityUtils.java
    │   ├── entity/                          # JPA entities
    │   ├── repository/                      # Spring Data JPA
    │   ├── dto/request|response/
    │   ├── mapper/DtoMapper.java
    │   ├── service/                         # Business logic
    │   ├── controller/                      # REST endpoints
    │   └── exception/                       # @ControllerAdvice
    └── resources/
        └── application.yml
```

## Key Services

| Service          | Responsibility                                      |
|------------------|-----------------------------------------------------|
| AuthService      | Customer/vendor registration, JWT login             |
| CartService      | Persistent cart CRUD                                |
| CheckoutService  | Multi-vendor split, Razorpay, payment verify        |
| InventoryService | Pessimistic lock + stock decrement                  |
| OrderService     | Status workflow transitions                         |
| AdminService     | KPIs, vendor approval, customer CRM                 |
| VendorService    | Fulfillment, analytics, customer insights           |
