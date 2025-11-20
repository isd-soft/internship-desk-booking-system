**Desk Booking System**

A web-based application designed to manage desk reservations within an organization. The system supports both regular users (employees) and administrators, 
providing a clear workflow for booking, managing availability, and organizing workspace resources.

**1.	Project Overview**
The Desk Booking System allows users to:
•	Register and authenticate
•	Browse desks and check their real-time availability, mark as Favourites
•	Create, update, or cancel bookings
•	View personal weekly/monthly bookings
Administrators can do all the user’s functionalities and additionally:
•	Manage desks (create, edit, delete, enable/disable availability)
•	Manage users and assign roles
•	Manage users’s bookings and their limitations
•	Monitor booking activity, users activity and space usage
•	Override availability when needed
•	Manage map setup

**Tech Stack:**
•	Backend: Java 17, Spring Boot, Intellije Idea Ultimate, Spring Security, JPA/Hibernate, Bcrypt, MailService, Mockito
•	Frontend: Vue 3, Vue Router, Axios, VisualCode, GridLayout
•	Database: PostgreSQL, FlyWay, 
•	Build tools: Maven, Node/Yarn
•	Deployment: Windows Server + YAJSW (runs as a Windows Service)

**2.	System Architecture**
Modules included:
•	Auth Module (registration, login, JWT, LDAP, role-based access)
•	User Module (user management, profile)
•	Desk Module (desk CRUD, coordinates, temp availability)
•	Booking Module (create/update/cancel bookings, validations)
•	Admin Module (advanced actions, dashboards, statistics)

**Project Structure**
internship-desk-booking-system/
│
├── backend/  
│   ├── .mvn/wrapper 
│   ├── src/main/java/com/project/internship_desk_booking_system/
│   │   ├── command/
│   │   ├── config/  
│   │   ├── controller/
│   │   ├── dto/  
│   │   ├── entity/
│   │   ├── enums/  
│   │   ├── errors/
│   │   ├── handlers/
│   │   ├── jwt/
│   │   ├── mapper/
│   │   ├── repository/
│   │   ├── service/
│   │   └── InternshipDeskBookingSystemApplication.java
│   ├── src/main/resources/
│   │   │   ├── db.migration/ migration.sql files
│   │   ├── application.properties
│   │   └── static/ dist (vue frontend build)
│   ├── src/test/java/com/project/internship_desk_booking_system/
│   ├── docker-compose.yml
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── assets
│   │   ├── components
│   │   ├── plugins
│   │   ├── router
│   │   ├── stores
│   │   ├── utils
│   │   ├── App.vue
│   │   └── main.js
│   ├── public/
│   ├── index.html
│   ├── jsconfig.json
│   ├── package-lock.json
│   ├── package.json
│   ├── pnpm-lock.yaml
│   ├── tsconfig.json
│   └── vite.config.js
│
├── .gitattributes
├── .gitignore
├── LICENSE
└── README

**3.	Features**

User Features
•	Create account & log in (LDAP/JWT)
•	View desk availability calendar
•	Book a desk for a specific date and time
•	Edit or cancel existing bookings
•	Add/Remove to Favourite Desks
•	Weekly booking limit validations
•	Receive email notification for booking confirmation/cancelation

Admin Features
•	Create and manage desks
•	Adjust desk temporary availability
•	Manage users and assign roles
•	Update desk coordinates and metadata
•	View booking history and user activity
•	Manage booking validation

**4.	Requirements**
Software Requirements
•	Java 17+
•	Maven 3.9+
•	Node.js 18+
•	Yarn or npm
•	PostgreSQL 15+
•	YAJSW (for Windows Server deployment)
System Requirements
•	Windows Server machine
•	Database access credentials
•	Port availability for backend 
5.	Local Development Setup
Backend Setup
1.	Clone repository
2.	Import as Maven project
3.	Configure environment variables or application.properties:
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/desk_booking
    username: your_user
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
4.	Run with:
mvn spring-boot:run
Frontend Setup
cd frontend
npm install    
npm run dev    
Build Frontend
npm run build

**6.	Production Deployment (Windows Server + YAJSW)**
a.	Database Setup
Create a new PostgreSQL database:
CREATE DATABASE desk_booking;
CREATE USER desk_user WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE desk_booking TO desk_user;
a)	Build Backend
mvn clean package -DskipTests
Output file:
/target/desk-booking-system.jar
b)	Frontend Production Build
The project uses frontend-maven-plugin and maven-resources-plugin, so the entire Vue frontend build process is fully automated when building backend.
c)	YAJSW Configuration
1.	Place the jar inside the YAJSW /app folder
2.	Generate YAJSW config:
bin\genConfig.bat C:\path\to\desk-booking-system.jar
3.	Edit the generated conf/wrapper.conf
4.	Install as Windows Service:
bin\installService.bat

**7.	API Documentation**
The project uses Swagger (OpenAPI 3) for automatic REST API documentation. Swagger UI is available at: http://localhost:8080/swagger-ui/index.html 
Main endpoints:
•	/api/v1/auth/* – register, login;
•	/api/v1/booking/* – my, all, upcoming, byDate, my/byDate, booking CRUD;
•	/api/v1/desk/* – coordinates, {deskId}/availability;
•	/api/v1/admin/* – admin-specific operations for managing desks, zones, bookings, users;
•	/api/statistics/* – get statistics for range period of time.
The backend also includes JavaDoc documentation for services, controllers, and domain logic.
JavaDoc can be generated with:
mvn javadoc:javadoc

**8.	Security**
•	JWT Authentication
•	LDAP Authentication
•	Role-based authorization: USER, ADMIN
•	Password hashing with Bcrypt
•	Password encryption & descryption on front-end
•	Token refresh endpoints
•	CORS configured for front-end development

**9.	 Testing**
Unit tests
•	Booking logic
•	Validation rules
•	User and desk services
•	Admin specific functionalities and validation
Tests executed via Mockito:
mvn test

**10.	Contributors:**
	Fantaziu Irina
	Gherta Lilian
	Iachim Vlad
	Stefan
	Vladimir 

