# SEAP Project

SEAP is a college alumni networking portal that connects current students with
verified alumni. The application provides role-specific onboarding, secure
authentication, profile management, alumni discovery, and administrator
verification workflows.

## Features

- **Student access** — Create a student account, maintain academic and career
  interests, and discover alumni mentors.
- **Alumni access** — Create an alumni account, submit academic and professional
  details, and participate in the verified alumni directory.
- **Secure authentication** — JWT-based login with Student and Alumni role
  separation.
- **Basic profiles** — Create and update contact, location, biography, and
  student-specific profile information.
- **Alumni directory** — Search verified alumni by department, graduation year,
  or company and view detailed profiles.
- **Administrator verification** — Review pending alumni profiles and approve or
  reject submissions with remarks.

## Technology stack

- Java 17+
- Spring Boot 3.2
- Spring Security and JWT
- Spring Data JPA and Hibernate
- H2 for development and tests
- Maven
- HTML, CSS, and JavaScript for the frontend

## Project structure

```text
alumni-portal/
├── src/main/java/                 # Spring Boot backend
├── src/main/resources/static/     # Frontend pages, styles, and scripts
├── src/test/java/                 # Unit and integration tests
├── feature-modules/               # Task-specific implementation/test maps
└── pom.xml
```

## Run locally

From the repository root:

```powershell
Set-Location .\alumni-portal
mvn spring-boot:run
```

Open <http://localhost:8080/>.

The landing page provides separate Student and Alumni authentication paths.
The default development database uses H2 and is created under
`alumni-portal/data/`.

## Test

Run the complete unit and integration test suite from `alumni-portal/`:

```powershell
mvn verify
```

The suite covers authentication, JWT handling, basic profiles, alumni profile
creation, alumni search/viewing, verification, repository behavior, and the
full controller journey.

## Feature task maps

The implementation and test references for the three profile-related tasks are
under `alumni-portal/feature-modules/`:

- `stlv4-79-basic-profile`
- `stlv4-17-view-alumni`
- `stlv4-24-profile-verification`

