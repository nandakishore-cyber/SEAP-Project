# Implementation map

- API: `alumni-portal/src/main/java/com/alumni/portal/controller/ProfileController.java`
- Service: `.../service/ProfileService.java`
- Entity: `.../entity/Profile.java`
- DTOs: `.../dto/request/ProfileCreateRequest.java`, `.../dto/response/ProfileResponse.java`
- Dashboard UI: `alumni-portal/src/main/resources/static/dashboard.html`
- Dashboard execution: `.../static/js/dashboard.js`

Acceptance criteria:

1. A user can create a profile once and update it thereafter.
2. Duplicate creation is rejected.
3. Student fields are persisted and returned.
4. Unauthenticated requests cannot access profile endpoints.
