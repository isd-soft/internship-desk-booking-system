# Desk Booking System

The Desk Booking System is a **web-based application** designed to manage workspace reservations within an organization.
It supports both **regular users** and **administrators**, offering functionality for booking desks, managing availability, and monitoring workspace usage.

---

## 1. Project Overview

**User capabilities:**

* Register and authenticate
* Browse desks and view real-time availability
* Create, update, or cancel bookings
* Mark desks as favourites
* View weekly and monthly booking history

**Administrator capabilities:**

* All user-level functionality
* Manage desks (create, edit, disable/enable availability)
* Manage users and assign roles
* View booking history, user activity, and statistics
* Override availability and limitations
* Configure map layout and desk coordinates

**Technology Stack**

* **Backend:** Java 17, Spring Boot, Spring Security, JPA/Hibernate, BCrypt, MailService, Mockito, JUnit 
* **Frontend:** Vue 3, Vue Router, Axios, GridLayout
* **Database:** PostgreSQL (Flyway for migrations)
* **Build Tools:** Maven, Node/Yarn
* **Deployment:** Windows Server using YAJSW as a Windows Service

---

## 2. System Architecture

**Modules**

* **Auth Module:** registration, login, JWT, LDAP, role management
* **User Module:** user profiles and management
* **Desk Module:** CRUD operations, coordinates, temporary availability
* **Booking Module:** booking creation/modification, constraints and validations
* **Admin Module:** advanced administration tools, analytics, statistics

---

## 3. Project Structure

```
internship-desk-booking-system/
│
├── backend/                                 # Spring Boot Backend
│   ├── .mvn/wrapper/                         # Maven wrapper files
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/project/internship_desk_booking_system/
│   │   │   │   ├── command/                  # Command pattern implementations
│   │   │   │   ├── config/                   # Configuration classes
│   │   │   │   ├── controller/               # REST API controllers
│   │   │   │   ├── dto/                      # Data Transfer Objects
│   │   │   │   ├── entity/                   # JPA entities
│   │   │   │   ├── enums/                    # Enumeration types
│   │   │   │   ├── errors/                   # Custom error classes
│   │   │   │   ├── handlers/                 # Exception handlers
│   │   │   │   ├── jwt/                      # JWT authentication utilities
│   │   │   │   ├── mapper/                   # Entity-DTO mappers
│   │   │   │   ├── repository/               # JPA repositories
│   │   │   │   ├── service/                  # Business logic
│   │   │   │   └── InternshipDeskBookingSystemApplication.java
│   │   │   └── resources/
│   │   │       ├── db.migration/             # Flyway SQL migrations
│   │   │       ├── static/                   # Vue frontend build output
│   │   │       └── application.properties    # Configuration file
│   │   └── test/                             # Unit and integration tests
│   ├── docker-compose.yml                    # PostgreSQL Docker config
│   └── pom.xml                               # Maven dependencies
│
├── frontend/                                 # Vue.js Frontend
│   ├── src/
│   │   ├── assets/                           # Static assets
│   │   ├── components/                       # Vue components
│   │   ├── plugins/                          # Axios configuration
│   │   ├── router/                           # Vue Router
│   │   ├── stores/                           # Pinia state store
│   │   ├── utils/                            # Helper utilities
│   │   ├── App.vue                           # Root component
│   │   └── main.js                           # Application entry point
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── .gitattributes
├── .gitignore
├── LICENSE
└── README.md
```

---

## 4. Features

### User Features

* Account creation and authentication (LDAP/JWT)
* View desk availability calendar
* Book, edit, or cancel reservations
* Mark desks as favourites
* Weekly booking limit validations
* Email notifications (confirmation/cancellation)

### Administrator Features

* Desk creation, editing, and availability controls
* User management and role assignment
* Desk metadata and coordinate management
* Access to booking history and activity logs
* Administrative booking validation and override tools

---

## 5. Requirements

**Software Requirements**

* Java 17+
* Maven 3.9+
* Node.js 18+
* Yarn or npm
* PostgreSQL 17+
* YAJSW (for Windows deployments)

**System Requirements**

* Windows Server machine
* PostgreSQL database access
* Network port availability (8081, 5432, 3000(for local development), 389)

---

## 6. Local Development Setup

### Backend Setup

1.  **Clone the repository**

2.  **Start Infrastructure (Database & LDAP)**
    The project uses Docker to run PostgreSQL and the LDAP server locally.
    
    ```bash
    docker-compose up -d
    ```
    *This command will start the PostgreSQL database on port 5432 and the LDAP server on port 389/10389.*

3.  **Import as a Maven project** in your IDE

4.  **Configure `application.properties`**
    Ensure your properties match the ports exposed by Docker.

    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/desk_booking
    spring.datasource.username=your_user
    spring.datasource.password=your_password
    spring.jpa.hibernate.ddl-auto=update
    
    # Mail & Security configs...
    spring.mail.username=your@example.com
    # ... other properties
    ```

    *Or follow the guide in `application-dev_example.properties`.*

5.  **Run the Application:**

    a. Select run config: `Run backend (Dev profile)`
    
    b. Run it

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

   *This command will start the Vite server on port 3000*

### Frontend Build

```bash
npm run build
```

---

# 7. Production Deployment (Windows Server + YAJSW)

## 7.1 Database Setup

```sql
CREATE DATABASE desk_booking;
CREATE USER desk_user WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE desk_booking TO desk_user;
```

---

## 7.2 Build the Backend

* Select 1 of 2 run confgis `Build executable jar` or `Build executable jar (no tests)` and just run it 
---
* `Build executable jar (no tests)` is same to

```bash
mvn clean package -DskipTests -Pprod
```

* `Build executable jar` is same to

```bash
mvn clean package -Pprod
```

Output:

```
/target/internship-desk-booking-system-0.0.1-SNAPSHOT.jar
```

---

## 7.3 Frontend Production Build

The Vue.js frontend is automatically built via:

* `frontend-maven-plugin`
* `maven-resources-plugin`

No manual steps are required.

---

## 7.4 YAJSW Configuration (Windows Service)

1. Place the JAR inside `yajsw/app/`.

2. Generate configuration:

   ```bash
   bin\genConfig.bat C:\path\to\internship-desk-booking-system-0.0.1-SNAPSHOT.jar
   ```

3. Edit:

   ```
   conf/wrapper.conf
   ```

4. Edit: 
   ```
   ${wrapper.working.dir}/secret.conf
   ```

4. Install as a Windows service:

   ```bash
   bin\installService.bat
   ```
   
---

# 8. API Documentation

Swagger UI:

```
http://localhost:8081/swagger-ui/index.html
```

### Main API Categories

* `/api/v1/auth/*` — authentication
* `/api/v1/booking/*` — booking operations
* `/api/v1/desk/*` — desk availability and coordinates
* `/api/v1/admin/*` — administrative actions
* `/api/statistics/*` — reporting and analytics

### JavaDoc Generation

```bash
mvn javadoc:javadoc
```

---

# 9. Security

The system implements several security measures to ensure safe user access, data protection, and secure communication between the frontend and backend.

### JWT Authentication

The backend uses JSON Web Tokens to authenticate users. Each request to protected endpoints must include a valid token in the `Authorization` header.

### LDAP Authentication

Integration with LDAP allows login through the organization’s directory service when required.

### Role-Based Authorization

Two access levels are supported:

* **USER** – regular employees
* **ADMIN** – users with extended privileges and administrative rights

Access is enforced with Spring Security.

### Password Hashing

All passwords are stored securely using **BCrypt hashing**.

### Frontend Encryption

Sensitive inputs such as passwords are encrypted on the frontend before being sent to the server.

### Token Refresh

The backend exposes endpoints for refreshing expired access tokens without requiring a full login.

### CORS Configuration

Cross-Origin Resource Sharing (CORS) is configured to allow the frontend and backend to communicate during development and production.

---

# 10. Testing

### 10.1 Covered Areas

* Booking rules and validation
* User and desk services
* Admin operations and constraints

### 10.2 Execute Tests

```bash
mvn test
```
---

# 11. Contributors

- Fantaziu Irina
- Gherta Lilian
- Iachim Vlad
- Rijenco Vladimir
- Vlasitchi Stefan

---

