#  Desk Booking System

A **web-based application** designed to manage desk reservations within an organization. 
The system supports both **regular users (employees)** and **administrators**, providing a clear workflow for booking, managing availability, and organizing workspace resources.

---

## 1️⃣ Project Overview

The Desk Booking System allows users to:  
-  Register and authenticate  
-  Browse desks and check their real-time availability on map, mark as Favourites  
-  Create, update, or cancel bookings  
-  View personal weekly/monthly bookings  

Administrators can do all the user functionalities and additionally:  
-  Manage desks (create, edit, delete, enable/disable availability)  
-  Manage users and assign roles  
-  Manage users’ bookings and their limitations  
-  Monitor booking activity, users activity, and space usage  
-  Override availability when needed  
-  Manage map setup  

**Tech Stack:**  
- **Backend:** Java 17, Spring Boot, IntelliJ IDEA Ultimate, Spring Security, JPA/Hibernate, Bcrypt, MailService, Mockito  
- **Frontend:** Vue 3, Vue Router, Axios, Visual Studio Code, GridLayout  
- **Database:** PostgreSQL, FlyWay  
- **Build tools:** Maven, Node/Yarn  
- **Deployment:** Windows Server + YAJSW (runs as a Windows Service)  

---

## 2️⃣ System Architecture

**Modules included:**  
-  **Auth Module:** registration, login, JWT, LDAP, role-based access  
-  **User Module:** user management, profile  
-  **Desk Module:** desk CRUD, coordinates, temporary availability  
-  **Booking Module:** create/update/cancel bookings, validations  
-  **Admin Module:** advanced actions, dashboards, statistics  

---

##  Project Structure
```
internship-desk-booking-system/
│
├── 📁 backend/                                 # Spring Boot Backend
│   ├── .mvn/wrapper/                          # Maven wrapper files
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/project/internship_desk_booking_system/
│   │   │   │   ├── command/                   # Command pattern implementations
│   │   │   │   ├── config/                    # Configuration classes
│   │   │   │   ├── controller/                # REST API controllers
│   │   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   │   ├── entity/                    # JPA entities
│   │   │   │   ├── enums/                     # Enumeration types
│   │   │   │   ├── errors/                    # Custom error classes
│   │   │   │   ├── handlers/                  # Exception handlers
│   │   │   │   ├── jwt/                       # JWT authentication
│   │   │   │   ├── mapper/                    # Entity-DTO mappers
│   │   │   │   ├── repository/                # JPA repositories
│   │   │   │   ├── service/                   # Business logic
│   │   │   │   └── InternshipDeskBookingSystemApplication.java
│   │   │   └── resources/
│   │   │       ├── db.migration/              # Flyway SQL migrations
│   │   │       ├── static/                    # Vue frontend build (dist)
│   │   │       └── application.properties     # App configuration
│   │   └── test/                              # Unit & integration tests
│   │       └── java/com/project/internship_desk_booking_system/
│   ├── docker-compose.yml                     # Docker PostgreSQL setup
│   └── pom.xml                                # Maven dependencies
│
├── 📁 frontend/                                # Vue.js Frontend
│   ├── src/
│   │   ├── assets/                            # Images, styles, fonts
│   │   ├── components/                        # Vue components
│   │   ├── plugins/                           # Axios, etc.
│   │   ├── router/                            # Vue Router
│   │   ├── stores/                            # Pinia state management
│   │   ├── utils/                             # Helper functions
│   │   ├── App.vue                            # Root component
│   │   └── main.js                            # App entry point
│   ├── public/                                # Static assets
│   ├── index.html                             # HTML template
│   ├── jsconfig.json                          # JavaScript config
│   ├── package.json                           # NPM dependencies
│   ├── pnpm-lock.yaml                         # PNPM lock file
│   ├── tsconfig.json                          # TypeScript config
│   └── vite.config.js                         # Vite bundler config
│
├── .gitattributes                             # Git attributes
├── .gitignore                                 # Git ignore rules
├── LICENSE                                    # Project license
└── README.md                                  # Project documentation
```

## 3️⃣ Features

**User Features**  
-  Create account & log in (LDAP/JWT)  
-  View desk availability on map 
-  Book a desk for a specific date and time  
-  Edit or cancel existing bookings  
-  Add/Remove Favourite Desks  
-  Receive email notifications for booking confirmation/cancellation  

**Admin Features**  
-  Create and manage desks  
-  Create/edit and delete user’s bookings
-  Manage users and assign roles  
-  Update desk coordinates and metadata  
-  View booking history and user activity  
-  Manage validations  

---

## 4️⃣ Requirements

**Software Requirements**  
- Java 17+  
- Maven 3.9+  
- Node.js 21+  
- Yarn or npm  
- PostgreSQL 17+  
- YAJSW (for Windows Server deployment)  

**System Requirements**  
- Windows Server machine  
- Database access credentials  
- Port availability for backend  

---

## 5️⃣ Local Development Setup

### Backend Setup

1. **Clone repository**
2. **Import as Maven project**
3. **Configure environment variables or `application.properties`:**
```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/desk_booking_system
       username: your_user
       password: your_password
     jpa:
       hibernate:
         ddl-auto: update
```

4. **Run with IntelliJ:**
   - Simply select the "dev" run profile in IntelliJ
   - No manual frontend build required — Maven handles frontend build during backend startup

---

## 6️⃣ Production Deployment (Windows Server + YAJSW)

### a. Database Setup

### b. Build the Full Application
```bash
mvn clean package -DskipTests
```

### c. YAJSW Configuration

1. **Place the jar inside the YAJSW `/app` folder**

2. **Generate YAJSW config:**
```bash
   bin\genConfig.bat C:\path\to\desk-booking-system.jar
```

3. **Edit the generated `conf/wrapper.conf` and adjust:**
   - Java path
   - JAR path
   - Working directory
   - Log path
   - Active Spring profile:
```properties
     wrapper.java.additional.1=-Dspring.profiles.active=prod
```

4. **Install & Start as Windows Service:**
```
   bin\installService.bat
   bin\startService.bat
```

---

## 7️⃣ API Documentation

The project uses **Swagger (OpenAPI 3)** for automatic REST API documentation.

**Swagger UI is available at:**  
👉 `http://localhost:8080/swagger-ui/index.html`

### Main Endpoints

- **`/api/v1/auth/*`** – Register, login
- **`/api/v1/booking/*`** – My bookings, all, upcoming, by date, booking CRUD
- **`/api/v1/desk/*`** – Coordinates, `{deskId}/availability`
- **`/api/v1/admin/*`** – Admin-specific operations for managing desks, zones, bookings, users
- **`/api/statistics/*`** – Get statistics for range period of time

### JavaDoc Documentation

The backend also includes **JavaDoc documentation** for services, controllers, and domain logic.

**Generate JavaDoc with:**
```bash
mvn javadoc:javadoc
```

---

## 8️⃣ Security

✅ **JWT Authentication**  
✅ **LDAP Authentication**  
✅ **Role-based authorization:** `USER`, `ADMIN`  
✅ **Password hashing with Bcrypt**  
✅ **Password encryption & decryption on frontend**  
✅ **Token refresh endpoints**  
✅ **CORS configured for frontend development**

---

## 9️⃣ Testing

### Unit Tests

- Booking logic
- Validation rules
- User and desk services
- Admin specific functionalities and validation

**Tests executed via Mockito:**
```bash
mvn test
```

---

## 1️⃣0️⃣ Contributors

👩‍💻 **Fantaziu Irina**  
👨‍💻 **Gherta Lilian**  
👨‍💻 **Iachim Vlad**  
👨‍💻 **Rijenco Vladimir**  
👨‍💻 **Vlasitchi Stefan**

---

