# Implementation

The complete handoff source is under
`implementation/src/main/java/com/alumni/portal/`.

It includes the mentorship controller, service, entity, status enum, request
and response DTOs, repositories, and the shared alumni/profile classes needed
to understand the verified-mentor integration.

Primary endpoints:

- `GET /api/mentorship/mentors`
- `POST /api/mentorship/requests`
- `GET /api/mentorship/requests/me`
- `GET /api/mentorship/requests/received`
- `PUT /api/mentorship/requests/{id}`
