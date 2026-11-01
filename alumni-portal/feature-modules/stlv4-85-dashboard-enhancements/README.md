# Dashboard Enhancements

This enhancement module extends the existing dashboard without replacing the existing feature flows.

It adds persisted notifications, alumni request accept/reject actions, alumni event/webinar creation with student announcements, and alumni bonafide certificate upload.

The canonical implementation is under `alumni-portal/src/main/java/com/alumni/portal`; the handoff copies in this folder cover the notification API and its tests. The certificate API is kept with the canonical source because it handles local file storage configuration.
