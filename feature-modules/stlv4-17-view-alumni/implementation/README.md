# Implementation map

- API: `alumni-portal/src/main/java/com/alumni/portal/controller/AlumniController.java`
- Service: `.../service/AlumniService.java`
- Repository/search queries: `.../repository/AlumniProfileRepository.java`
- Response DTO: `.../dto/response/AlumniProfileResponse.java`
- Directory UI: `alumni-portal/src/main/resources/static/dashboard.html`
- Search and modal UI: `.../static/js/dashboard.js`

Acceptance criteria:

1. Directory search supports department, graduation year, and company filters.
2. Only verified profiles are returned by directory search.
3. A profile can be opened by ID with academic and professional details.
4. Missing profiles return a clear not-found response.
