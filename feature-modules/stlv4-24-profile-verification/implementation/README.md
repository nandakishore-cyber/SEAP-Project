# Implementation map

- Admin API: `alumni-portal/src/main/java/com/alumni/portal/controller/AlumniController.java`
- Verification service: `.../service/AlumniService.java`
- Status model: `.../entity/enums/VerificationStatus.java`
- Request DTO: `.../dto/request/VerificationRequest.java`
- Security rules: `.../config/SecurityConfig.java`
- Admin dashboard: `alumni-portal/src/main/resources/static/dashboard.html`
- Admin actions: `.../static/js/dashboard.js`

Acceptance criteria:

1. Only administrators can list pending profiles or verify them.
2. Approve changes status to `VERIFIED`.
3. Reject changes status to `REJECTED`.
4. Remarks and verification time are retained.
