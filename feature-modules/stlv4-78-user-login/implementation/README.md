# Implementation map

- API: `alumni-portal/src/main/java/com/alumni/portal/controller/AuthController.java`
- Authentication service: `.../service/AuthService.java`
- JWT provider/filter: `.../security/JwtTokenProvider.java`, `.../config/JwtAuthenticationFilter.java`
- Security rules: `.../config/SecurityConfig.java`
- Login execution: `alumni-portal/src/main/resources/static/js/app.js`
- Role-specific entry pages: `.../static/student.html`, `.../static/alumni.html`

Acceptance criteria:

1. Correct credentials return a Bearer JWT.
2. Wrong password returns `401`.
3. Wrong Student/Alumni role selection returns `401`.
4. Protected endpoints require a valid JWT.
