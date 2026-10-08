# SevaTrack — Civic Complaint & SLA Escalation Platform

SevaTrack is a Java/J2EE-based civic complaint management platform designed to manage citizen complaints, route them to appropriate officers, monitor SLA deadlines, and automatically escalate overdue complaints through L1 → L2 → L3.

The project demonstrates backend application development using Servlets, JSP, JDBC, MariaDB, SQL procedures/triggers, object-oriented routing strategies, automated SLA monitoring, and automated testing.

## ✨ Key Features

- 📝 Citizen complaint registration
- 🎫 Unique complaint ticket generation
- 🔎 Complaint tracking with status and SLA information
- 🏢 Department-based complaint routing
- 👤 Officer assignment based on routing rules
- 🧩 Strategy-pattern based routing engine
- ⏱️ SLA deadline calculation and monitoring
- 🔼 Automatic L1 → L2 → L3 escalation
- 📋 Complete complaint status/audit history
- 📊 Department-level reporting
- 📄 JSON and XML report generation
- 🗄️ MariaDB stored procedures and triggers
- 🧪 JUnit 5 and Mockito tests
- 🐳 Docker and Docker Compose support
- ❤️ Health checks for the application and database

---

## 🛠️ Technology Stack

### Backend
- Java 17
- Java Servlets
- JSP
- JSTL
- JDBC
- Apache Tomcat 9

### Database
- MariaDB 10.11
- SQL
- Stored Procedures
- Triggers

### Testing
- JUnit 5
- Mockito

### Build & DevOps
- Maven
- Docker
- Docker Compose
- Git / GitHub

---

## 🏗️ Architecture

SevaTrack follows a layered backend architecture:

```text
Browser
   │
   ▼
JSP / Web Layer
   │
   ▼
Servlets
   │
   ▼
Service Layer
   │
   ├── Complaint Service
   ├── Escalation Service
   ├── SLA Monitor
   └── Report Service
   │
   ▼
DAO Layer
   │
   ▼
JDBC
   │
   ▼
MariaDB
