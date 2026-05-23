# GMart Full Stack - Inventory & Billing Management System

**GMart Full Stack** is a comprehensive, enterprise-ready Inventory and Billing Management System. It features a modern, responsive Single Page Application (SPA) web frontend alongside a robust Java Spring Boot REST API backend, coupled with a legacy Command Line Interface (CLI) utilizing a Model-View-Presenter (MVP) architecture.

---

## 🚀 Key Features

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

## 🛠️ Technology Stack

* **Backend**: Java 17, Spring Boot 3.2.5 (Spring Web, Spring Data JPA)
* **Database**: MySQL (configured for cloud Clever Cloud database / local MySQL connectivity)
* **Frontend**: Vanilla HTML5, CSS3, JavaScript (Fetch API integration)
* **Design & Icons**: Inter & Outfit typography, Font Awesome 6.4.0, custom HSL-based color tokens

---

## 📂 Project Structure

```
Gmart_Full_Stack/
├── pom.xml                   # Maven Dependency Configurations
├── .gitignore                # Restricts git to tracking src/ and pom.xml
└── src/
    ├── main/
    │   ├── java/com/gokulrajvel/gmart/
    │   │   ├── GmartApplication.java   # Spring Boot Application Entry Point
    │   │   ├── controller/             # REST Endpoints (Auth, Product, User, etc.)
    │   │   ├── service/                # Business & Validation logic layers
    │   │   ├── repository/             # Spring Data JPA Repository Interfaces
    │   │   ├── data/                   # Data DTOs, Enums, and JPA Entity Classes
    │   │   └── features/               # Legacy MVP Console CLI Feature Subpackages
    │   └── resources/
    │       ├── application.properties  # Database & Spring Boot Configuration
    │       └── static/                 # Front-End Web Application Assets
    │           ├── index.html          # Secure Login Page
    │           ├── dashboard.html      # Main SPA Dashboard
    │           ├── css/                # Glassmorphic Styling Sheets
    │           └── js/                 # Web API & Interface Controllers
    └── test/                           # Service and Feature Test suites
```

---

## ⚙️ Running Locally

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

---

## 🐳 Running with Docker

You can package and run the application inside a lightweight container using the provided [Dockerfile](file:///home/pain/IdeaProjects/Gmart_Full_Stack/Dockerfile):

### 1. Build the Docker Image
Run the following command in the project root:
```bash
docker build -t gmart-app .
```

### 2. Run the Container
Launch the container and map the web port:
```bash
docker run -d -p 8080:8080 --name gmart-app gmart-app
```
Access the application at `http://localhost:8080`.

---

## 🚀 Step-by-Step Online Deployment

To deploy GMart online so it is accessible from anywhere in the world:

### 1. Push to GitHub
1. Create a repository on GitHub.
2. Push your local `Gmart_Full_Stack` project folder to your new GitHub repository:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin <your-github-repo-url>
   git push -u origin main
   ```

### 2. Deploy Using Render (Recommended & Free)
1. Go to [Render.com](https://render.com/) and log in/register.
2. Click **New** -> **Web Service**.
3. Connect your GitHub account and select your `Gmart_Full_Stack` repository.
4. Configure the service:
   - **Name**: `gmart-billing` (or any custom name)
   - **Region**: Choose a region closest to you
   - **Branch**: `main`
   - **Runtime**: `Docker` (Render automatically detects the [Dockerfile](file:///home/pain/IdeaProjects/Gmart_Full_Stack/Dockerfile) in the root)
   - **Instance Type**: `Free`
5. Click **Deploy Web Service**.
6. Render will pull the code, build the Docker container, and start the app. Once finished, you will receive a public URL (e.g., `https://gmart-billing.onrender.com`).

> [!NOTE]
> GMart is pre-configured to connect to a cloud Clever Cloud MySQL database by default. Hence, you do not need to configure any environment variables for the database connection when deploying online. It will connect and synchronize stock counts automatically.

### 3. Deploy Using Railway (Alternative)
1. Go to [Railway.app](https://railway.app/) and log in/register.
2. Click **New Project** -> **Deploy from GitHub repo**.
3. Select your `Gmart_Full_Stack` repository.
4. Railway will auto-detect the `Dockerfile` and start the deploy build.
5. Once deployed, go to **Settings** -> **Public Networking** -> **Generate Domain** to retrieve the public URL.

