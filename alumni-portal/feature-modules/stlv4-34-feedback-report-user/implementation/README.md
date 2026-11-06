# Implementation

Spring Boot implementation for report submission, reporter history, and
administrator review. The dashboard form is under `src/main/resources/static`.
Entities are persisted as `user_reports` and all endpoints require JWT auth;
administrator endpoints use the existing role checks.
