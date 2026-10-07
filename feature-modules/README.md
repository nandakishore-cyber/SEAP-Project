# SEAP feature modules

This directory separates the six board tasks into reviewable feature folders.
The application remains a single Spring Boot module under `alumni-portal/`;
the task folders deliberately reference the source files and tests instead of
duplicating code.

| Folder | Board task | Status | Main test coverage |
| --- | --- | --- | --- |
| `stlv4-77-user-registration` | User Registration | Complete | `AuthControllerIntegrationTest`, `AuthServiceTest` |
| `stlv4-78-user-login` | User Login & Authentication | Complete | `AuthControllerIntegrationTest`, `AuthServiceTest`, `JwtTokenProviderTest` |
| `stlv4-79-basic-profile` | Basic Profile Creation | Complete | `ProfileServiceTest`, `FullJourneyIntegrationTest` |
| `stlv4-61-alumni-profile` | Create Alumni Profile | Complete | `AlumniServiceTest`, `FullJourneyIntegrationTest` |
| `stlv4-24-profile-verification` | Alumni Profile Verification | Complete | `AlumniServiceTest`, `FullJourneyIntegrationTest` |
| `stlv4-17-view-alumni` | View Alumni Profile | Complete | `AlumniServiceTest`, repository tests, `FullJourneyIntegrationTest` |

Run all unit and integration tests from `alumni-portal/` with:

```text
mvn verify
```
