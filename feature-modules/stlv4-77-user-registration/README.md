# STLV4-77 — User Registration

Users can create accounts from the role-specific Student or Alumni page.
Registration validates names, email, password, and role, hashes the password,
and returns an authentication response. Student and Alumni registration then
collects and saves the matching profile details.

## Folder contents

- `implementation/` — source map and acceptance criteria.
- `testing/` — unit/integration test map and commands.
