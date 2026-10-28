# AGENTS.md - Instructions for Future AI Coding Agents

## 1. Project Purpose & Scope

The **Smart Parking Management System** is a full-stack Java web application built on the Jakarta EE 10 / Servlet 6.0 and Apache Tomcat 10.1 platform, using pure JDBC and MySQL 8.0.

It connects drivers (Customers) who discover and reserve parking slots with parking operators (Providers) who manage facilities, bays, and customer bookings. A critical mandatory feature is the automated background scheduler (`ExpiryScheduler`) that detects expired bookings and triggers 4 notifications (Customer Email, Customer SMS, Provider Email, Provider SMS).

---

## 2. Mandatory Tech Stack & Architecture Constraints

* **Backend**: Java 17+ (JDK 25.0.2 installed), Jakarta Servlet 6.0, Jakarta JSP 3.1, JDBC, MySQL 8.0.
* **Web Container**: Apache Tomcat 10.1.x (located at `C:\apache-tomcat-10.1.59`).
* **Frontend**: HTML5, CSS3, JavaScript, Bootstrap 5.3.3, Bootstrap Icons 1.11.3.
* **Build System**: Maven (`pom.xml`) and native build script (`build.ps1`).
* **DO NOT introduce**: Spring Boot, React, Angular, Vue, Hibernate, Redis, Kafka, Docker, or Kubernetes. The project must remain simple, lightweight, and suitable for a university B.Tech demonstration.

---

## 3. Directory Layout & Key Files

```
smart_parking_system/
├── src/main/java/com/smartparking/
│   ├── config/AppConfig.java           # Central property and environment manager
│   ├── config/DatabaseConnection.java  # Thread-safe JDBC Connection provider
│   ├── controller/                     # Servlets handling HTTP routing & view dispatch
│   ├── dao/                            # Data Access Objects using PreparedStatement
│   ├── filter/                         # AuthFilter (RBAC), CsrfFilter, EncodingFilter
│   ├── model/                          # Domain POJOs (User, ParkingArea, ParkingSlot, Booking, Notification)
│   ├── notification/                   # EmailService, SmsService, DispatchResult
│   ├── scheduler/                      # ExpiryScheduler (60s timer), AppLifecycleListener
│   ├── service/                        # Business logic, transactions & overlap prevention
│   └── util/                           # PasswordUtil (BCrypt), FeeCalculator, ValidationUtil
├── src/main/webapp/
│   ├── assets/css/style.css            # Custom responsive stylesheet
│   ├── assets/js/main.js               # Client fee estimator & confirmation helpers
│   └── WEB-INF/
│       ├── web.xml                     # Servlet 6.0 deployment descriptor
│       ├── lib/                        # Runtime JARs (MySQL, BCrypt, Mail, JSTL)
│       └── views/                      # JSP presentation templates
├── database/
│   ├── schema.sql                      # DDL schema definition
│   └── sample_data.sql                 # Sample test data (Password: Password@123)
├── pom.xml                             # Maven POM
├── build.ps1                           # Automated build, test, and WAR packaging
└── tests/ (src/test/java/...)          # 22 automated JUnit 5 tests
```

---

## 4. Coding & Architecture Conventions

1. **MVC Separation**:
   * Controller Servlets must only parse HTTP parameters, invoke Service methods, and forward to JSP views.
   * Business logic, fee calculations, and transaction management belong strictly in the Service layer.
   * SQL queries belong strictly in DAO classes.
   * Presentation and formatting belong in JSP views and `DateTimeUtil`.

2. **Database Access & Concurrency Control**:
   * **Always** use `PreparedStatement` with parameterized placeholders (`?`). String concatenation in SQL is strictly forbidden.
   * Concurrency & Overlap Prevention: Booking creation must occur in an explicit database transaction (`conn.setAutoCommit(false)`) with a re-verification query (`SELECT ... FOR UPDATE`) before saving.
   * If a conflict occurs, roll back the transaction and return an informative error message.

3. **Authentication & Authorization**:
   * Passwords must **never** be stored in plain text. Always use `PasswordUtil.hashPassword(...)` and `PasswordUtil.checkPassword(...)`.
   * Sessions must be invalidated on logout and regenerated upon login to prevent session fixation.
   * Role-based access is enforced by `AuthFilter`:
     * `/customer/*` requires `CUSTOMER` or `ADMIN`.
     * `/provider/*` requires `PROVIDER` or `ADMIN`.
   * **Provider Isolation**: A provider must never be able to view or edit another provider's facilities, slots, or bookings. Always include `provider_id = ?` checks on updates and deletes.

4. **Background Expiry Processing**:
   * Managed by `ExpiryScheduler` using `ScheduledExecutorService`.
   * Runs every 60 seconds (configurable via `scheduler.intervalSeconds`).
   * Must identify bookings where `booking_status = 'CONFIRMED'` and `end_time <= NOW()`.
   * Transitions status to `EXPIRED` and dispatches 4 notifications.
   * Uses unique composite key in `notifications` (`uk_notification_idempotency`) to ensure duplicate notifications are prevented across restarts.

5. **Notification System & Development Mode**:
   * If real SMTP or Twilio SMS credentials are not set, **do not fail or fake delivery**.
   * Mark the record as `SIMULATED` and log the message to standard output.
   * Never mark a simulated message as `SENT`.

---

## 5. How to Build, Test, and Verify

To compile sources, execute all 22 automated JUnit 5 tests, and produce `target/smart-parking.war`:
```powershell
.\build.ps1
```

To run only the unit test suite:
```powershell
$jars = (Get-ChildItem -Path "lib/*.jar", "src/main/webapp/WEB-INF/lib/*.jar" | Select-Object -ExpandProperty FullName) -join ';'
$cp = "target/classes;target/test-classes;$jars;C:\apache-tomcat-10.1.59\lib\servlet-api.jar;C:\apache-tomcat-10.1.59\lib\jsp-api.jar"
& "C:\Program Files\Java\jdk-25.0.2\bin\java.exe" -jar "lib/junit-platform-console-standalone-1.10.2.jar" execute --class-path $cp --scan-class-path --fail-if-no-tests
```

---

## 6. Guidelines for Future Agent Modifications

1. **Inspect Before Modifying**: Read the target file thoroughly before making edits. Do not assume or rewrite files blindly.
2. **Preserve Existing Functionality**: Maintain backward compatibility with the existing database schema and sample data.
3. **No Unnecessary Dependencies**: Do not download external JARs or inject frameworks unless explicitly required by the user.
4. **Run Tests After Any Edit**: Always run `.\build.ps1` to ensure all 22 automated tests continue passing.
5. **Update Documentation**: Whenever models, endpoints, or configuration keys change, update `README.md`, `HELPING_ME.md`, and `docs/PROJECT_OVERVIEW.md` synchronously.
