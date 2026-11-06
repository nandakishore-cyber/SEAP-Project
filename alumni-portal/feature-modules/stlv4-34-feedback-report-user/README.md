# STLV4-34 — Feedback and Report User

Users can submit general portal feedback or report another account from the
alumni directory with a category and details, then track the report status. Administrators can review reports and
move them through Open, Reviewing, Resolved, and Dismissed.

The dashboard is integrated at `src/main/resources/static/dashboard.html` and
`src/main/resources/static/js/dashboard.js`. The backend provides `POST
/api/reports`, `GET /api/reports/me`, administrator-only `GET /api/reports`,
and administrator-only `PUT /api/reports/{id}/status?status=...`.

## Implementation

The standalone Spring source handoff is under `implementation/src/main/java`.
The canonical implementation is integrated into `src/main/java/com/alumni/portal`.
