# Product Requirements Document (PRD): GMart Full Stack

## 1. Product Overview
**GMart Full Stack** is a comprehensive Inventory and Billing Management System designed to handle retail or wholesale store operations. It provides role-based access to manage stock, suppliers, inward/outward transactions, and billing. The application is designed as a hybrid full-stack system, featuring a Web-based interface (HTML/CSS/JS) backed by a Java Spring Boot REST API, alongside a robust Model-View-Presenter (MVP) architecture for console-based operations.

## 2. Target Audience & User Roles
The system supports multiple user roles, each with specific permissions and access levels to ensure security and operational efficiency:
- **ADMIN**: Full access to all modules, including User Management to add/remove staff accounts.
- **BILLING_STAFF**: Restricted access strictly to the Billing System module to process customer checkouts.
- **WAREHOUSE**: Access to Stock Management, Inventory Tracking, and Inward/Outward transactions.
- **PURCHASING_MANAGER**: Access to Supplier Management and Reports.

## 3. Technology Stack
* **Backend Framework**: Java 17, Spring Boot 3.2.5 (Spring Web, Spring Data JPA)
  * *Note: Requires Java compiler `-parameters` flag for path variable reflection name resolution.*
* **Database**: MySQL (via `mysql-connector-j`)
* **Frontend**: Vanilla HTML5, CSS3, JavaScript (Fetch API for REST integration)
  * *Note: System currency is formatted in Indian Rupees (₹).*
* **Architecture Patterns**: 
  * Backend API: standard N-Tier (Controller, Service, Repository)
  * Console/Legacy UI: Model-View-Presenter (MVP)

## 4. Core Features & Modules

### 4.1 Authentication & Authorization
* Secure login system (`index.html`, `auth.js`) validating credentials against the MySQL `users` table.
* Role-based redirection to specific dashboards or console menus.

### 4.2 Dashboard (`dashboard.html`, `DashboardView.java`)
* Central hub routing users to their authorized modules based on their `Role`.

### 4.3 Billing System
* Process customer orders and generate bills in Indian Rupees (₹).
* Tracks `Bill` and `BillItem` entities, calculating total amounts, saving the sold product names in the bill items list, updating stock, and generating Java-side transaction/billing timestamps in real-time.

### 4.4 Stock & Product Management
* Manage product catalog (`Product` entity) including SKU, name, price, category, and associated Supplier ID.
* REST API endpoints (e.g., `GET /api/products`, `POST /api/products`) for frontend integration.

### 4.5 Supplier Management
* Maintain a directory of suppliers (`Supplier` entity) for procurement.
* Features to add, view, and manage supplier details (with alteration/edit capabilities enabled for Admins).

### 4.6 Inward and Outward Transactions
* Log stock entering the warehouse (Inward) from suppliers.
* Log stock leaving the warehouse (Outward) for sales or returns.
* Tracked via the `InventoryTransaction` entity.

### 4.7 Inventory Tracking & Reports
* Real-time tracking of current stock levels.
* Generate reports on sales, low stock alerts, and transaction history.

### 4.8 User Management (Admin Only)
* Create, read, update, and delete system users.
* Assign roles (ADMIN, BILLING_STAFF, etc.) to control access.

## 5. Data Models (Entities)
The application relies on the following core data entities managed via JPA:
1. **User**: Authentication details (`username`, `password`, `role`).
2. **Product**: Catalog items (`sku`, `name`, `price`, `categoryId`, `supplierId`).
3. **Category**: Enum/Table for product classification.
4. **Supplier**: Vendor details (`name`, `contactInfo`).
5. **Bill & BillItem**: Transactional data for customer purchases. `Bill` contains `billDate` (initialized to current date). `BillItem` logs `productName` alongside product and transaction detail fields.
6. **InventoryTransaction**: Audit log for stock movements, tracking `transactionDate` (initialized to current date).

## 6. API Endpoints (Subset)
- `GET /api/products` - Retrieve all products.
- `GET /api/products/{sku}` - Retrieve a single product by SKU.
- `POST /api/products` - Add a new product.
*(Other endpoints follow standard REST conventions for Users, Suppliers, Bills, etc., managed by their respective controllers).*

## 7. Future Enhancements
* Transitioning all legacy MVP console features fully into the responsive web dashboard.
* Implementing JWT-based authentication for the REST APIs to secure frontend-to-backend communication.
* Adding export functionality (PDF/Excel) for reports and bills.
