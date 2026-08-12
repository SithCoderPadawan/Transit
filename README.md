# Transit — School Transport System

Phase B build: extends the presentation prototype with CSV data import
(requirement 1a) and role-scoped reporting (requirement 3f), on top of
the existing login, role-based access, route/pupil management, and
school contact functionality.

## What's implemented

- **Subtype polymorphism** — abstract `User` with `Admin`, `LEAOfficer`,
  `SchoolStaff`, `Parent` siblings (`model/`)
- **Creational pattern (Factory Method)** — `UserFactory` (`factory/`)
- **Structural pattern (Facade)** — `AuthService` (`auth/`)
- **Structural pattern (Composite)** — `Route` composed of `Stop`s
- **Behavioural pattern (Strategy)** — `PermissionStrategy`, four role-specific implementations
- **Requirement 2c fix** — `LEAPermissionStrategy` explicitly reuses
  `SchoolPermissionStrategy`'s set (genuine superset)
- **CSV import (requirement 1a)** — `CsvImportService`,
  reads pupil records from a CSV file, validates each row, assigns
  pupils to existing routes
- **Reporting (requirement 3f)** — `ReportService`,
  one shared `generateReport(User)` entry point matching the "Generate
  report" use case generalization, dispatching to four role-scoped
  variants: Full System Report (Admin), Route & Contract Report (LEA),
  School Pupil Report (School Staff, scoped to their school), Child's
  Route Report (Parent, scoped to their linked `childIds`)
- **Optimistic locking (requirement 3e, new in Phase B)** — `Pupil`
  carries a `version` field, incremented on every successful update.
  `PupilService.updatePupilRecord()` requires the caller to supply the
  version they read; a mismatch (someone else edited the record since)
  throws `StaleDataException` rather than silently overwriting the
  other user's change. See "Locking Mechanism Justification" below.
- **Bus contract management (Phase B, new)** — `ContractService`,
  LEA-only, implements the "Manage bus contracts" use case: reviewing
  and updating a contractor's performance rating (1-5), corresponding
  to the case study's June contractor review cycle
- **Custom checked exceptions** — `InvalidCredentialsException`,
  `AccountLockedException`, `InvalidRoleException`, `RouteCapacityException`,
  `InvalidRouteException`, `UnauthorizedScopeException`, `InvalidSchoolException`,
  `PupilNotFoundException`, `CsvFormatException` (new), `InvalidContractorException`,
  `StaleDataException` (reserved for the locking mechanism, iteration 3)
- **API exception handling** — `IOException` from CSV file reading,
  `NumberFormatException` from bad menu input, `NoSuchElementException`
  from a closed input stream
- **Console menu** — role-scoped, now includes "Import pupil data (CSV)" for Admin
- **Unit tests** — `AuthServiceTest`, `RouteManagerTest`, `PupilServiceTest`, `CsvImportServiceTest`

## CSV format

```
pupilId,name,emergencyContact,schoolId,routeId
```

No header row. Example:
```
P-100,Grace Bishop,07700-555555,SCH-01,R-102
```

Sample files are in `sample-data/` — `pupils_valid.csv` (imports cleanly),
`pupils_malformed.csv` (blank name, triggers `CsvFormatException`),
`pupils_bad_route.csv` (unknown route, triggers `CsvFormatException`).

## Running it

```bash
mvn compile exec:java
```

## Running the tests

```bash
mvn test
```

## Demo accounts

| Username     | Password    | Role         | School  |
|--------------|-------------|--------------|---------|
| admin1       | admin123    | Admin        | --      |
| j.matthews   | lea123      | LEA Officer  | --      |
| school1      | school123   | School Staff | SCH-01  |
| parent1      | parent123   | Parent       | --      |

## Requirements coverage

| Requirement | Status |
|---|---|
| 1a: CSV data entry | Done |
| 1b: school staff contacts | Done |
| 2a-2c: role-based access | Done |
| 3c/3d: multi-user access | Not started |
| 3e: locking mechanism | Done (this iteration) |
| 3f: reporting per role | Done |
| Bus contract management | Done (this iteration) |
| Swing GUI | Not started (optional) |

## Locking Mechanism Justification

Requirement 3e asks for multi-user access to be safe, with data integrity
preserved, and for the chosen locking strategy to be justified.

**Chosen strategy: optimistic locking**, implemented via a `version`
integer field on `Pupil`, incremented on every successful update.
Every write must supply the version number the caller last read; if
the stored version has since moved on, the write is rejected with
`StaleDataException` rather than silently overwriting another user's
change.

**Why optimistic over pessimistic:**

- Contention is expected to be low. Pupils are edited occasionally
  (a parent's contact details change, a pupil moves house), not
  continuously, and different School Staff users are almost always
  editing *different* pupils, not the same one at the same moment.
  Pessimistic locking (row-locking a pupil record for the duration of
  an edit) would add real complexity -- lock acquisition, timeout
  handling, deadlock avoidance -- to guard against a conflict that will
  rarely actually happen.
- Optimistic locking has a graceful failure mode. If a genuine
  conflict does occur, the second user is told clearly and can simply
  re-read the current record and retry, rather than being blocked
  waiting for a lock to release.
- It fits a console-driven, largely single-user-per-session usage
  pattern better than pessimistic locking, which is more suited to
  systems with sustained concurrent editing of the same records (e.g.
  collaborative document editors).

**Known limitation:** because the console menu always re-reads a
record immediately before presenting it for editing, a genuine
conflict cannot be demonstrated through the interactive console itself
within a single session -- the console UI has no way to simulate two
users editing at once. The conflict *is* proven, but through unit
tests (`staleVersionIsRejectedWithoutOverwritingTheFirstEditorsChange`
in `PupilServiceTest`) that simulate two callers holding different
version snapshots of the same record, which is the same underlying
mechanism a real multi-user (e.g. Swing or web) front end would
trigger naturally.

## GUI (Java Swing + FlatLaf)

A Swing GUI front end is included (`gui/LoginFrame.java`,
`gui/DashboardFrame.java`), using FlatLaf for a modern flat look and
feel. It's a thin presentation layer exactly like `ConsoleMenu` --
every action calls the same service classes (`AuthService`,
`RouteManager`, `PupilService`, `ReportService`, `SchoolService`,
`CsvImportService`, `ContractService`), so no business logic is
duplicated between the console and GUI front ends. The sidebar and
available actions are permission-gated per role, identically to the
console menu.

Run it with:
```bash
mvn compile exec:java -Dexec.mainClass="com.transit.gui.LoginFrame"
```

FlatLaf is declared as a Maven dependency in `pom.xml` and will be
downloaded automatically.

## Note

`src/main/java/com/transit/VerifyHarness.java` is a temporary,
dependency-free stand-in used only to verify logic in environments
without Maven Central access. Delete it once `mvn test` has been
confirmed to run normally on your machine.

`src/main/java/com/transit/gui/DashboardPreview.java` is a test-only
launcher used to visually verify `DashboardFrame` without going
through the login flow. Not part of the deliverable -- delete once
the real login-to-dashboard flow has been confirmed working.
