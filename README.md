# GMart Full Stack - Inventory & Billing Management System

**GMart Full Stack** is a comprehensive, enterprise-ready Inventory and Billing Management System. It features a modern, responsive Single Page Application (SPA) web frontend alongside a robust Java Spring Boot REST API backend, coupled with a legacy Command Line Interface (CLI) utilizing a Model-View-Presenter (MVP) architecture.

---

## ? Key Features

* **Role-Based Access Control (RBAC)**: Secure access restricted to authorized personnel:
  * `ADMIN`: Full system management, employee registration, and data control.
  * `BILLING_STAFF`: Limited access to the Point of Sale (POS) billing interface.
  * `WAREHOUSE`: Access to stock catalogs, transaction records, and inventory flows.
  * `PURCHASING_MANAGER`: Access to suppliers/vendors directories and analytical reports.
* **Point of Sale (POS) & Billing**: Interactive checkout counter supporting cart management, auto-calculated tax, itemized invoices, and real-time inventory deduction.
* **Inventory Tracking**: Log inward/outward transactions, audit stock flows, and receive visual alerts for low-stock products.
* **Supplier Directory**: Registry of active product vendors with quick editing options for Admins.
* **Theme Support**: Adaptive Light/Dark mode toggles with synchronized system badges and styled color themes.
* **Premium UX Overlay**: Smooth glassmorphic loading screens that appear during async REST API operations to indicate work in progress.

---

## ?? Technology Stack

* **Backend**: Java 17, Spring Boot 3.2.5 (Spring Web, Spring Data JPA)
* **Database**: MySQL (configured for cloud Clever Cloud database / local MySQL connectivity)
* **Frontend**: Vanilla HTML5, CSS3, JavaScript (Fetch API integration)
* **Design & Icons**: Inter & Outfit typography, Font Awesome 6.4.0, custom HSL-based color tokens

---

## ? Project Structure

```
Gmart_Full_Stack/
??? pom.xml                   # Maven Dependency Configurations
??? .gitignore                # Restricts git to tracking src/ and pom.xml
??? src/
    ??? main/
    ?   ??? java/com/gokulrajvel/gmart/
    ?   ?   ??? GmartApplication.java   # Spring Boot Application Entry Point
    ?   ?   ??? controller/             # REST Endpoints (Auth, Product, User, etc.)
    ?   ?   ??? service/                # Business & Validation logic layers
    ?   ?   ??? repository/             # Spring Data JPA Repository Interfaces
    ?   ?   ??? data/                   # Data DTOs, Enums, and JPA Entity Classes
    ?   ?   ??? features/               # Legacy MVP Console CLI Feature Subpackages
    ?   ??? resources/
    ?       ??? application.properties  # Database & Spring Boot Configuration
    ?       ??? static/                 # Front-End Web Application Assets
    ?           ??? index.html          # Secure Login Page
    ?           ??? dashboard.html      # Main SPA Dashboard
    ?           ??? css/                # Glassmorphic Styling Sheets
    ?           ??? js/                 # Web API & Interface Controllers
    ??? test/                           # Service and Feature Test suites
```

---

## ?? Running Locally

### 1. Database Setup
The system is configured to connect to a remote Clever Cloud MySQL server by default in `src/main/resources/application.properties`. 

To run it against your **local MySQL** instance for maximum speed (< 1ms query times):
1. Open [application.properties](file:///home/pain/IdeaProjects/Gmart_Full_Stack/src/main/resources/application.properties).
2. Uncomment the local connection blocks and supply your credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/gmart_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

### 2. Compilation and Launch
Build and execute the project using Maven:

```bash
# Compile and run the Spring Boot app
mvn spring-boot:run
```

Once started:
* **Web Portal**: Navigate to `http://localhost:8080` in your web browser.
* **Default Admin Account**:
  * **Username**: `admin`
  * **Password**: `admin123`
