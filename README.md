# SEAP Project

<p align="center">
  <strong>College Alumni Tracking and Networking Portal</strong>
</p>

<p align="center">
  A secure platform that connects students with verified alumni, supports meaningful mentorship, and preserves the college community.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk" alt="Java 17+">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot 3.2">
  <img src="https://img.shields.io/badge/Security-JWT-blue?style=for-the-badge&logo=jsonwebtokens" alt="JWT Security">
  <img src="https://img.shields.io/badge/Tests-Maven%20%2B%20JUnit-red?style=for-the-badge&logo=apachemaven" alt="Maven and JUnit">
</p>

## About SEAP

SEAP provides a single, role-aware experience for current students, alumni, and administrators. Students can build an academic and career profile, alumni can share their professional journey, and administrators can verify alumni submissions before they appear in the directory.

## Key features

- **Student onboarding** — Role-specific registration, academic details, interests, skills, and career goals.
- **Alumni onboarding** — Academic history, graduation details, professional experience, company, designation, and skills.
- **Secure authentication** — JWT-based login with separate Student and Alumni access paths.
- **Basic profile management** — Contact, location, biography, date of birth, LinkedIn, and role-specific details.
- **Verified alumni directory** — Search by department, graduation year, or company and view complete alumni profiles.
- **Admin verification workflow** — Review pending profiles, approve or reject submissions, and record verification remarks.
- **Dynamic dashboards** — Student, Alumni, and Administrator views show the information and actions relevant to each role.

## Technology stack

| Layer | Technologies |
| --- | --- |
| Backend | Java 17+, Spring Boot 3.2, Spring MVC |
| Security | Spring Security, JWT |
| Persistence | Spring Data JPA, Hibernate, H2 for development/testing |
| Frontend | HTML, CSS, JavaScript |
| Testing | JUnit 5, Mockito, Spring Boot Test, MockMvc |
| Build | Maven |

## Project structure

```text
SEAP-Project/
├── alumni-portal/
│   ├── src/main/java/              # Spring Boot backend
│   ├── src/main/resources/static/  # Student, Alumni, and Admin web UI
│   ├── src/test/java/              # Unit and integration tests
│   └── pom.xml
└── .gitignore
```

## Run locally

Requirements: Java 17 or newer and Maven.

```powershell
Set-Location .\alumni-portal
mvn spring-boot:run
```

Then open [http://localhost:8080/](http://localhost:8080/). The landing page provides separate Student and Alumni login and registration paths.

## Run tests

From `alumni-portal/`:

```powershell
mvn verify
```

The test suite covers registration, login and JWT handling, basic profile creation, alumni profile creation, alumni profile viewing/search, verification authorization, repository behavior, and full controller journeys.

## Feature ownership

| Contributor | Features |
| --- | --- |
| **Krishnakumar E** | User Registration; User Login & Authentication; Create Alumni Profile |
| **Nanda Kishore K S** | Basic Profile Creation; View Alumni Profile; Alumni Profile Verification |

## Contributors

- **Krishnakumar E** — Authentication and alumni profile creation
- **Nanda Kishore K S** — Profile management, alumni discovery, and verification workflow

## License

This project is developed for academic and educational use.