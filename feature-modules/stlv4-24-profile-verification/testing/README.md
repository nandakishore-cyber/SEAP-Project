# Testing map

- Unit tests: `alumni-portal/src/test/java/com/alumni/portal/unit/service/AlumniServiceTest.java`
- Authorization and journey tests: `alumni-portal/src/test/java/com/alumni/portal/integration/controller/FullJourneyIntegrationTest.java`

Run from `alumni-portal/`:

```text
mvn -Dtest=AlumniServiceTest test
mvn -Dit.test=FullJourneyIntegrationTest verify
```
