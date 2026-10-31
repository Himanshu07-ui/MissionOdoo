# Brightloop Billing – Full Stack Recurring Billing Platform

A modern, full-stack recurring subscription and billing management platform designed for SaaS, software vendors, and recurring service providers.

Built for the Hackathon MVP using **HTML / CSS / Vanilla JavaScript**, **Java Spring Boot 3**, **Spring Data JPA / Hibernate**, and **MySQL**.

---

## 🚀 Key Highlights & Architecture

- **Visual Fidelity**: Preserved Brightloop's original typography (`Bricolage Grotesque` + `Instrument Sans`), light/dark themes, responsive sidebar, animations, and modals.
- **Server-Side Truth**: Invoice subtotal, taxes, and totals are **strictly calculated on the backend** — client totals are never trusted.
- **Duplicate Prevention**: Unique `billing_key` (`subscriptionId@billingDate`) constraint ensures billing runs never generate duplicate invoices for the same cycle.
- **Role-Based Access Control**:
  - `ADMIN`: Full access (products, plans, subscriptions, invoices, payments, taxes, users, dashboard).
  - `STAFF`: Subscriptions, invoices, payments, product/plan catalog, dashboard.
  - `CUSTOMER`: Self-service portal (view subscriptions, view/print invoices, online payment).
- **Stateless Authentication**: Custom HMAC-SHA256 JWT tokens with BCrypt password hashing.
- **Multi-Method Payments**: Support for UPI, Card, Cash, Bank Transfer, and Cheque with dynamic balance tracking.

---

## 📂 Project Structure

```
MissionOdoo/
├── frontend/
│   ├── index.html                 # Complete SPA frontend connected to REST APIs
│   └── index_original_backup.html # Backup of initial Claude-exported frontend
├── backend/
│   ├── pom.xml                    # Maven configuration (Java 21+, Spring Boot 3.5.7)
│   ├── src/main/java/com/brightloop/billing/
│   │   ├── BillingApplication.java
│   │   ├── config/                # JWT utility, JWT filter, CORS & Web filter registration
│   │   ├── entity/                # JPA entities (User, Tax, Product, Variant, Plan, Subscription, Line, Invoice, Payment)
│   │   ├── dto/                   # Request & Response DTOs
│   │   ├── repository/            # Spring Data JPA repositories
│   │   ├── service/               # Core business logic (Auth, Invoicing, Billing, Dashboard, Payments)
│   │   └── controller/            # REST API endpoints (/api/auth, /api/dashboard, /api/invoices, etc.)
│   └── src/main/resources/
│       └── application.properties # Spring Boot & MySQL configuration
├── database/
│   ├── schema.sql                 # MySQL schema with foreign keys and unique constraints
│   └── seed.sql                   # Initial demo data (products, plans, taxes, subscriptions)
├── index.html                     # Root entry point
└── README.md                      # Project documentation and runbook
```

---

## 🛠️ Prerequisites

- **Java**: JDK 21 or higher
- **Maven**: 3.9+
- **MySQL Server**: 8.0 or higher running on port `3306`

---

## 🗄️ Database Setup

1. Log into your MySQL instance:
   ```bash
   mysql -u root -p
   ```
2. Execute the schema script:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
3. Load the seed/demo data:
   ```bash
   mysql -u root -p < database/seed.sql
   ```

*Note: Default database name is `brightloop_billing`.*

---

## ⚙️ Backend Setup & Execution

### 1. Configure Environment Variables (or edit `application.properties`)

```bash
export DB_URL="jdbc:mysql://localhost:3306/brightloop_billing?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"
export DB_USER="root"
export DB_PASS="YourPasswordHere"
export JWT_SECRET="BrightloopBillingSecretKey2026HackathonMVPDontUseInProd!"
```

### 2. Build & Package

```bash
cd backend
mvn clean package -Dmaven.test.skip=true
```

### 3. Run the Application

```bash
java -jar target/billing-1.0.0.jar
```

The Spring Boot backend will start on **`http://localhost:8080`**.

---

## 🌐 Frontend Setup & Execution

You can run the frontend in either of two ways:

### Option A: Local Static Server (Recommended)
```bash
# From the repository root
python3 -m http.server 3000
```
Open **`http://localhost:3000/frontend/`** (or `http://localhost:3000/`) in your browser.

### Option B: Open directly in Browser
Open `frontend/index.html` directly in Chrome, Safari, or Firefox (CORS is configured to allow `file://` and local origins).

---

## 🔑 Demo Credentials

| Role | Email | Password | Access Capabilities |
|------|-------|----------|---------------------|
| **Admin** | `admin@brightloop.io` | `Admin@1234` | Full system access, users, taxes, product/plan CRUD |
| **Staff** | `staff@brightloop.io` | `Staff@1234` | Subscriptions, billing, invoices, payments |
| **Customer** | `cust@acme.com` | `Cust@12345` | Customer portal, view invoices, self-service pay |
| **Customer** | `ops@globex.com` | `Globex@1234` | Customer portal |

---

## 🧪 End-to-End Verification Workflow

### 1. Authentication
- Go to the Sign In page.
- Log in with `admin@brightloop.io` / `Admin@1234`.
- A valid JWT token is returned and stored in `localStorage`.

### 2. Dashboard KPIs
- View real-time **Active Subscriptions**, **Monthly Recurring Revenue (MRR)**, **Revenue Collected**, and **Outstanding Invoices**.
- Filter dashboard metrics by date range.

### 3. Products & Plans
- Navigate to **Products**: View existing products (Brightloop CRM, Analytics Add-on, Priority Support) with variants and tax rates. Click **Add product** to create new software/add-on products.
- Navigate to **Plans**: View daily, weekly, monthly, and yearly recurring plans with pause/close/renew rules.

### 4. Subscription Management
- Navigate to **Subscriptions**: View current subscriptions (`SUB-0001`, `SUB-0002`, `SUB-0003`).
- Click **New subscription**: Select customer, plan, primary product, quantity, and billing cycle.
- Manage status using transition buttons: **Confirm**, **Activate**, **Pause**, **Resume**, **Close**, and **Renew**.

### 5. Automated Invoice Generation & Duplicate Prevention
- Navigate to **Invoices**.
- Click **Run billing**: The backend evaluates active subscriptions with `nextBillDate <= today`, calculates subtotal, taxes (GST 18% / 5%), and totals, and assigns unique sequence numbers (`INV-00001`).
- Click **Run billing** a second time: The unique `billing_key` prevents duplicates and reports `0 invoices created`.
- Click **Confirm** to lock an invoice.
- Click **View / Print** to preview or print a formatted invoice.

### 6. Payment Recording & Dynamic Balance
- On an invoice, click **Record payment** (or from the **Payments** tab).
- Select payment method (**UPI**, **Card**, **Cash**, **Bank Transfer**, or **Cheque**).
- Enter partial or full amount:
  - If partially paid, remaining balance updates immediately.
  - If fully paid, status changes automatically to **PAID**.
- The Dashboard reflects the updated collected revenue and reduced outstanding balance in real time.

### 7. Customer Portal
- Sign out and log in as `cust@acme.com` / `Cust@12345`.
- Customer lands on **My account**, seeing only their active subscriptions and invoices.
- Click **Pay now** to simulate online payment with immediate invoice clearance.

---

## 📡 Core REST API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/login` | Authenticate user and obtain JWT token |
| `POST` | `/api/auth/register` | Register a new customer |
| `GET` | `/api/dashboard` | Retrieve real-time MRR and financial KPIs |
| `GET` | `/api/products` | List all products with variants |
| `POST` | `/api/products` | Create a product with variants & tax |
| `GET` | `/api/plans` | List recurring billing plans |
| `POST` | `/api/plans` | Create a new billing plan |
| `GET` | `/api/subscriptions` | List subscriptions (filtered by user role) |
| `POST` | `/api/subscriptions` | Create a subscription with line items |
| `PUT` | `/api/subscriptions/{id}/action` | Trigger subscription action (`CONFIRM`, `ACTIVATE`, `PAUSE`, `RESUME`, `CLOSE`, `RENEW`) |
| `POST` | `/api/invoices/billing/run` | Execute automated billing cycle |
| `GET` | `/api/invoices` | List invoices with lines, balance, and status |
| `PUT` | `/api/invoices/{id}/status` | Update invoice status (`CONFIRMED`, `CANCELLED`, `PAID`) |
| `GET` | `/api/payments` | List payment ledger records |
| `POST` | `/api/payments` | Record payment against invoice |
| `GET` | `/api/taxes` | List tax configurations |
| `POST` | `/api/taxes` | Create a tax rate |
| `GET` | `/api/users` | List all users (admin view) |
| `POST` | `/api/users` | Create staff member or customer |
