# STLV4-25 / US-13 — Alumni Success Stories

Alumni can submit a title, achievement, and story for review. Administrators
can publish or reject submissions. Published stories are visible to signed-in
portal users, while alumni can see and delete their own submissions.

The dashboard is integrated at `src/main/resources/static/dashboard.html` and
`src/main/resources/static/js/dashboard.js`. The API provides `GET
/api/stories`, `GET /api/stories/me`, `POST /api/stories`, administrator-only
`GET /api/stories/pending` and `PUT /api/stories/{id}/moderation?status=...`,
and author/admin `DELETE /api/stories/{id}`.

## Implementation

The standalone Spring source handoff is under `implementation/src/main/java`.
The canonical implementation is integrated into `src/main/java/com/alumni/portal`.
