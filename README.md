# 🛍️ Store — Full-Stack E-Commerce Practice Project

A full-stack online store built to practice backend engineering with **Spring Boot**, paired with a **React** storefront and admin panel. The backend was written and designed from scratch as a hands-on Java/Spring Security exercise; the frontend UI was **generated with the help of Claude AI** to give the API a real, usable interface without spending the project's learning budget on frontend work.

> This is a **learning project**, not a production system. Real payment processing is intentionally out of scope — checkout collects a delivery address and stops at a decoy "Pay" button — and a few tradeoffs below are called out on purpose so reviewers can see what was considered, not just what was built.

---

## ✨ Features

**Customer-facing**
- Registration protected by SMS verification: creating an account sends a 6-digit code (2-minute expiry) that must be confirmed before the account is enabled and a login token is issued
- Login with JWT-based authentication
- Product catalog with search, availability filtering, and pagination
- A dedicated product detail page with full description and image
- Persistent shopping cart (mapped to a `PENDING` order under the hood), with quantity adjustable directly from the product grid, the product detail page, or the cart itself
- **Stock / inventory management**: each product has a real stock count. Adding items to the cart and checking out both verify available quantity. If another user completes checkout and reduces stock, other users' pending carts are automatically synced — excess quantity is reduced or the item is removed, and the cart total is recalculated
- Checkout: collects a delivery address and 10-digit postal code, adds a fixed shipping fee, decreases product stock, and moves the order to `PROCESSING` — followed by a **decoy "Pay" button**, since no real payment gateway is wired in
- **Cancel order**: users can cancel `PROCESSING` orders. Cancelling a `PROCESSING` order restores the reserved stock and re-syncs other pending carts
- Order history (paginated), showing delivery info once an order has been checked out
- Profile management (edit info, delete account)
- "Forgot password" flow with a time-limited reset code (delivered via a mock terminal-based SMS service)
- An "About Us" footer with store description, contact numbers, and an address that opens in Google Maps

**Admin**
- Product management (create, edit, delete, toggle availability, set stock quantity, description field), paginated
- View all orders (paginated), including delivery info when an order has been checked out
- View and manage all users (paginated)

**Security**
- JWT authentication with per-request ownership checks (users can only access their own cart/orders; admins can access all)
- Rate limiting on `/login` and `/forgot-password`: 5 attempts, then a 10-minute lockout per IP (via Bucket4j)
- All writes go through request DTOs — no JPA entity is ever bound directly from a client request

**Frontend**
- Custom blue theme, light/dark mode, RTL/Persian UI
- Built with React + Vite, no UI framework — hand-written design system
- Cancel button available on eligible orders in the order history / cart views

---

## 🧱 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot 4.1.0, Spring Web, Spring Data JPA, Spring Security |
| Auth | JWT (`jjwt`) |
| Rate limiting | Bucket4j |
| Database | MySQL |
| Docs | springdoc-openapi (Swagger UI) |
| Validation | Jakarta Bean Validation, request DTOs (no entities exposed directly on write) |
| Frontend | React 18, Vite, React Router — no CSS framework, hand-rolled design system |
| Testing | JUnit / Spring Boot Test (service, controller, and security layers) |

---

## 📁 Project Structure

```
Store/                          # Backend (Spring Boot)
├── src/main/java/org/store/store/
│   ├── config/                 # DatabaseSeeder, app-level config
│   ├── controller/             # REST controllers
│   ├── dto/                    # Request DTOs (RegisterRequest, VerifyRegistrationRequest,
│   │                           #   ProductRequest, CreateOrderRequest, CheckoutRequest, ...) + ErrorResponse
│   ├── exception/              # Global exception handling
│   ├── model/                  # JPA entities (User, Product, Order, OrderItem, Checkout, ...)
│   ├── repository/             # Spring Data repositories
│   ├── security/               # JWT filter, SecurityConfig, JWT utils, rate limiting
│   └── service/                # Business logic
├── src/main/resources/
│   ├── static/images/          # Product images served at /images/**
│   └── application.properties
└── src/test/java/...           # Unit & integration tests

store-frontend/                 # Frontend (React + Vite)
├── src/
│   ├── api/                    # Typed fetch client for the backend (incl. Page<T> normalization)
│   ├── context/                # Auth, Cart, Toast contexts
│   ├── pages/                  # Route-level pages (+ pages/admin), incl. ProductDetail, Checkout
│   ├── components/             # Navbar, Footer, Pagination, route guards, icons
│   └── index.css               # Design system (tokens, components)
└── package.json
```

---

## 🚀 Getting Started

### Prerequisites
- JDK 21+ (developed and tested against JDK 26)
- MySQL Server, running locally on port `3306`
- Node.js 18+ and npm

### 1. Backend setup

1. Make sure MySQL is running. The app auto-creates the `store` database on first run (`createDatabaseIfNotExist=true`).
2. Update the datasource credentials in `src/main/resources/application.properties` to match your local MySQL setup:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
4. On first run, `DatabaseSeeder` seeds a test admin, a test user (both pre-verified, so they can log in without an SMS code), and a handful of products (see [Test accounts](#-test-accounts) below).
5. Confirm it's up: `http://localhost:8080/store/welcome`. Full API docs: `http://localhost:8080/swagger-ui.html`.

### 2. Frontend setup

```bash
cd store-frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The backend's CORS config already whitelists this origin — no extra setup needed as long as both are running on their default ports.

If your backend runs elsewhere, copy `.env.example` to `.env` and set `VITE_API_URL`.

### A note on new registrations

Signing up through the UI requires a verification code. Since the SMS service is simulated, the code is printed to the **backend's console output**, not sent to an actual phone — check the terminal running `StoreApplication` after registering.

---

## 🔑 Test Accounts

Seeded automatically on first backend run (pre-verified — no SMS code needed):

| Role  | Number        | Password     |
|-------|---------------|--------------|
| Admin | `09111111111` | `1234` |
| User  | `09222222222` | `5678` |

---

## 📌 API Overview

All endpoints are prefixed with `/store`. Full interactive docs are available via Swagger UI once the backend is running. List endpoints return a Spring Data `Page<T>` and accept `page`, `size`, `sortBy`, and `sortDir` query parameters.

| Area | Endpoints |
|---|---|
| Auth | `POST /register` (sends verification code), `POST /register/verify` (confirms code, returns a token), `POST /login`, `POST /forgot-password/request`, `POST /forgot-password/reset` |
| Users | `GET/PUT/DELETE /users/me`, `GET /users` *(admin, paginated)*, `GET /users/by-number` *(admin)*, `DELETE /users/{id}` *(admin)* |
| Products | `GET /products` *(paginated)*, `GET /products/available` *(paginated)*, `GET /products/search?name=` *(paginated)*, `GET /products/{id}`, `POST/PUT/DELETE /products/{id}` *(admin)* |
| Orders (cart) | `GET /orders/user/me` *(paginated)*, `POST /orders/user/me`, `POST /orders/{id}` (add item), `PUT /orders/{orderId}/{itemId}`, `DELETE /orders/{orderId}/{itemId}`, `DELETE /orders/{id}` |
| Checkout | `POST /orders/user/me/checkout/{orderId}` — takes a delivery address + 10-digit postal code, decreases product stock, adds a fixed shipping fee, and moves the order to `PROCESSING` |
| Cancel | `POST /orders/{id}/cancel` (or equivalent) — cancels a `PROCESSING` order; restores stock when cancelling a `PROCESSING` order and re-syncs other pending carts |

**How the cart works:** there's no separate "cart" entity — the shopping cart *is* an `Order` in `PENDING` status. Adding the first item creates it; adding more items updates the same order. Stock is checked on every add/edit; the quantity cannot exceed the product's current stock.

**How stock & concurrent carts work:** stock is only decreased at checkout (not when items are merely added to a cart). After a successful checkout, any other `PENDING` carts that contain the same product are automatically synced: if stock is now zero the item is removed from those carts; if the requested quantity exceeds remaining stock the quantity is reduced. Cart totals are recalculated accordingly. Empty carts are deleted.

**How checkout works:** once an order is checked out, a `Checkout` record (address, postal code, final total including shipping) is attached to it, product stock is decreased, and its status moves to `PROCESSING`. From that point the order no longer counts as the active cart and can no longer be edited — item mutation endpoints only operate on `PENDING` orders.

**How cancel works:** users can cancel their own `PROCESSING` orders (admins can cancel any). Cancelling a `PROCESSING` order restores the previously reserved stock and triggers a re-sync of other pending carts. Cancelled orders keep their history for the user.

**Request bodies are DTOs, not entities.** Writes go through purpose-built request DTOs (`RegisterRequest`, `ProductRequest`, `CreateOrderRequest`, `OrderItemRequest`, `CheckoutRequest`, ...) instead of accepting the JPA entities directly, so clients can't set fields like `id` or `role` that they shouldn't control.

---

## ⚠️ Known Limitations (by design)

- **No real payment gateway.** The checkout flow collects a real delivery address and postal code, but the final "Pay" button is a decoy — this was out of scope for the exercise.
- **Shipping is a flat fee**, not calculated from address, weight, or carrier rates.
- **SMS is simulated.** Both the registration code and the password-reset code are printed to the backend console instead of being sent via a real SMS provider.

---

## 🔒 Security Notes

Building this project (and its frontend) surfaced several real bugs, which is part of what made it worth doing. Documenting them here on purpose — walking through *why* something was broken and how it was fixed is more valuable to a reviewer than pretending the first version was flawless.

Issues found and fixed during development:
- **IDOR on order endpoints** — order/cart endpoints originally had no ownership check, letting any authenticated user view or modify another user's cart by guessing an order ID. Fixed by verifying the requesting user owns the order (or is an admin) before allowing access.
- **Client-trusted product price** — the cart originally recalculated order totals using the `Product` object sent by the client instead of re-fetching it from the database, meaning an unvalidated price (or a default of `0`) could be used. Fixed by always resolving products server-side from their ID.
- **CORS configured but never wired in** — a `CorsConfigurationSource` bean existed but was never attached to the security filter chain, so cross-origin requests from the frontend were silently blocked.
- **Inconsistent authorization on `DELETE /orders/{id}`** — originally admin-only, which broke normal users clearing their own cart; fixed by opening it to authenticated users with an ownership check instead of a blanket role restriction.
- **Role prefix mismatch** — `CustomUserDetailsService` built authorities without the `ROLE_` prefix Spring Security's `hasRole()` expects by default, which caused real admin accounts to be denied on admin-only endpoints. Tests were passing anyway because `@WithMockUser(roles = "ADMIN")` adds the prefix automatically, which had been masking the issue. Fixed by adding the `ROLE_` prefix when building authorities.
- **Unverified accounts could get permanently stuck** — after adding SMS-verified registration, re-using a phone number whose verification code had expired (or was mistyped) was rejected as "already taken," even though the account was never actually enabled. Fixed by allowing registration to update and re-issue a code for existing-but-unverified accounts, instead of blocking on any existing row for that number.
- **Incomplete cart collection when syncing stock** — the query that found pending carts containing a product used a filtered `JOIN FETCH` on `items`, which caused Hibernate to load only the matching item instead of the full cart. Other items disappeared and totals were recalculated incorrectly. Fixed by removing the filtered fetch so the full item collection is loaded before adjusting quantities or removing items.

Hardening added proactively (not a bug fix, but worth calling out):
- **Rate limiting on `/login` and `/forgot-password`** — capped at 5 attempts per IP within a 10-minute window (via Bucket4j), to slow down brute-force attempts against passwords and reset codes.

---

## 🧪 Running Tests

```bash
./mvnw test
```

---

## 🙋 About This Project

The backend — data model, security, business logic, and tests — was hand-written as a Spring Boot / Spring Security learning exercise. The frontend was built with **Claude AI** to give the API a complete, working interface to demo and click through, so the focus could stay on the backend engineering.

---

## 📄 License

This project is for educational/portfolio purposes.
