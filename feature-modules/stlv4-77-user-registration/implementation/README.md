# Implementation map

- API: `alumni-portal/src/main/java/com/alumni/portal/controller/AuthController.java`
- Service: `alumni-portal/src/main/java/com/alumni/portal/service/AuthService.java`
- Request/response DTOs: `.../dto/request/RegisterRequest.java`, `.../dto/response/AuthResponse.java`
- User model: `.../entity/User.java`
- Student UI: `.../resources/static/student.html`
- Alumni UI: `.../resources/static/alumni.html`
- Registration execution: `.../resources/static/js/app.js`

Acceptance criteria:

1. Valid Student and Alumni registrations return `201`.
2. Passwords are stored encoded and duplicate email is rejected.
3. Invalid input returns field-level validation errors.
4. The role-specific profile details are saved after account creation.
