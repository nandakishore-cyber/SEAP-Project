# Implementation map

- API: `alumni-portal/src/main/java/com/alumni/portal/controller/AlumniController.java`
- Service: `.../service/AlumniService.java`
- Entity: `.../entity/AlumniProfile.java`
- DTOs: `.../dto/request/AlumniProfileRequest.java`, `.../dto/response/AlumniProfileResponse.java`
- Dashboard form: `alumni-portal/src/main/resources/static/dashboard.html`
- Dashboard execution: `.../static/js/dashboard.js`

Acceptance criteria:

1. An authenticated user can submit one alumni profile.
2. Required academic fields are validated.
3. Duplicate profile creation is rejected.
4. New profiles are marked `PENDING`.
