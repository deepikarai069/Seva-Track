# SevaTrack - Civic Complaint & SLA Escalation Platform

Citizens report civic problems (water, roads, sanitation, street lights). Each complaint is routed to the right
officer by rules, carries an SLA deadline, and is escalated L1 -> L2 -> L3 automatically when the deadline is missed.
Every change is audited by database triggers.

**Stack:** Java 17, Servlets 4 / JSP + JSTL (Tomcat 9), JDBC, MySQL/MariaDB, Maven, JUnit 5, Mockito, Docker.

## Run it

```bash
docker compose up --build        # http://localhost:8080
```
Without Docker: load `db/01_schema.sql`, `02_routines.sql`, `03_seed.sql` into MariaDB, set `DB_URL`, `DB_USER`,
`DB_PASSWORD`, then `mvn package` and drop `target/sevatrack.war` into Tomcat 9 (not 10: this uses `javax.servlet`).

## Pages and endpoints

| URL | Purpose |
|---|---|
| `/register` | Citizen files a complaint, gets a ticket like `ST-261007-K4M2X` |
| `/track?ticket=` | Status, SLA deadline, L1/L2/L3 position, full audit history |
| `/officer` | Officer work-queue (pick an officer; demo has no login), move complaints along the lifecycle |
| `/admin` | Department stats, overdue complaints, officer workload, "Run SLA check now" |
| `/reports?format=json\|xml&from=&to=` | Department report (default last 30 days) |

## How it works

**Routing** (`com.sevatrack.routing`, strategy pattern, `RoutingEngine` runs them in order):
`CategoryRoutingStrategy` (hard: department owns the category) -> `CapacityRoutingStrategy` (hard: skip officers at max load)
-> `WardRoutingStrategy` (prefer ward owner, then floating officers) -> `PriorityRoutingStrategy` (HIGH/CRITICAL go to
emergency-certified officers, routine work avoids them) -> `WorkloadRoutingStrategy` (lowest open/max ratio wins).
Preference strategies fall back gracefully; hard ones can leave a complaint unassigned (it then escalates on SLA expiry).

**SLA and escalation** (`EscalationService`, run every `SLA_CHECK_INTERVAL_SECONDS` by `SlaMonitor`, or from `/admin`):
SLA hours per priority and level live in table `sla_policy`. When an open complaint passes its deadline it moves to the next
level (supervisor, then department head), gets a fresh deadline for that level, and an `escalations` row is written.
Missing the deadline at L3 sets `sla_breached` once. Resolved/closed complaints are never escalated.

**Database logic** (`db/02_routines.sql`):
- Triggers `trg_complaints_ai/au` write `status_history` on every status, officer, level or breach change (actor and note
  travel via session variables set by the procedures). `trg_complaints_bu` enforces the status transition table, stamps
  `resolved_at`, and forbids lowering the escalation level - so rules hold even if someone bypasses the Java code.
- Procedures `sp_assign_complaint`, `sp_update_status`, `sp_escalate_complaint` (transactional, with rollback),
  `sp_mark_breached`, `sp_department_report`; function `fn_open_load`.

**Reports:** `ReportFormatter` renders JSON (hand written, dependency-free) and XML (JDK DOM).

## Tests

```bash
mvn test                                   # 43 tests; DB test is skipped without DB_URL
DB_URL=jdbc:mariadb://localhost:3306/sevatrack DB_USER=sevatrack DB_PASSWORD=sevatrack mvn test
```
Unit tests (JUnit 5 + Mockito, DAOs mocked, fixed clock): routing rules, registration/validation, officer permissions,
escalation L1/L2/L3 and breach, SLA policy, JSON/XML output. `EndToEndDatabaseTest` runs the real stored procedures and
triggers (**it deletes complaints in the target DB**).

## Known limits (deliberate, for a portfolio-sized build)
No authentication (officer/admin pages are open), no CSRF tokens, plain `DriverManager` connections instead of a pool,
no email/SMS notifications. Natural next steps: login with roles, HikariCP, notification on escalation.

## Layout
```
db/                 schema, triggers + procedures, seed (Lucknow wards, 4 departments, 20 officers)
src/main/java/...   model, dao (interfaces + JDBC), routing, service, util, web (servlets, listener, filter)
src/main/webapp/    JSPs under WEB-INF/views, static/style.css
src/test/java/...   unit tests + EndToEndDatabaseTest
```
