# GMart Decoupled - Inventory & Billing Management System

**GMart** is a comprehensive, enterprise-ready Inventory and Billing Management System. The project is separated into a standalone Java Spring Boot REST API backend and a modern vanilla JS web frontend.

---

## 📂 Project Structure

```
Gmart_Full_Stack/
├── backend/                  # Java Spring Boot Backend Service
│   ├── pom.xml               # Maven configurations
│   ├── src/                  # REST APIs, Controllers, Services & Entities
│   └── Dockerfile            # Container build for the Java service
├── frontend/                 # Decoupled Web Frontend
│   ├── index.html            # User login portal
│   ├── dashboard.html        # Main dashboard Single Page Application (SPA)
│   ├── css/                  # Styling & themes
│   ├── js/                   # Javascript logic & Fetch API controllers
│   ├── package.json          # Node configuration for local Vite development
│   └── Dockerfile            # Multi-stage container build with Nginx
└── README.md                 # Root instructions (this file)
```

---

## ⚙️ Running Locally

### 1. Spring Boot Backend (Port 8080)
The backend project connects to a remote MySQL database by default (configured in `backend/src/main/resources/application.properties`).

To run the backend locally:
```bash
cd backend
mvn spring-boot:run
```
The REST API will launch at `http://localhost:8080`.

* **Default Administrator Credentials**:
  - **Username**: `admin`
  - **Password**: `admin123`

---

### 2. Frontend Development Server (Port 5173 / dynamic)
The frontend uses Vite to serve the assets and compile dependencies.
In development mode, `api.js` is preconfigured to automatically route API calls to the local Spring Boot server running on port `8080` (CORS is enabled on the backend).

To run the frontend dev environment:
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` (or the URL outputted by Vite) in your browser.

---

## 🐳 Running with Docker Compose

You can spin up both services at the same time using Docker. Create a `docker-compose.yml` in the root folder:

```yaml
version: '3.8'

services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    restart: always

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: always
```

Run the stack using:
```bash
docker compose up --build -d
```
Access the application by navigating to the frontend web server at `http://localhost`.
