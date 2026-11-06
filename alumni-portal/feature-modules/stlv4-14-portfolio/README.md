# STLV4-14 / US-2 — Alumni Professional Portfolio

Alumni can publish a professional headline, summary, experience, selected
projects, certifications, and a website link. They can save and update their
portfolio from the dashboard. Other signed-in users can open an alumni
portfolio from the directory.

The dashboard integration is at `src/main/resources/static/dashboard.html`
and `src/main/resources/static/js/dashboard.js`; the feature's client code is
also included in `frontend/portfolio.js`. Alumni can preview the completed
portfolio before saving. The API provides `GET
/api/portfolio/me`, `PUT /api/portfolio/me`, and `GET /api/portfolio/{userId}`.

## Implementation

The standalone Spring source handoff is under `implementation/src/main/java`.
The canonical implementation is integrated into `src/main/java/com/alumni/portal`.
