# STLV4-78 — User Login & Authentication

Student and Alumni pages use the same JWT authentication API while sending
their selected role. The API rejects a valid account used through the wrong
role page, and the frontend stores the token and opens the dashboard.

## Folder contents

- `implementation/` — source map and acceptance criteria.
- `testing/` — unit/integration test map and commands.
