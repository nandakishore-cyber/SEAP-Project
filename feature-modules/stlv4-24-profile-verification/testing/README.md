# Testing map

- Unit: `alumni-portal/src/test/java/com/alumni/portal/unit/service/AlumniServiceTest.java`
- Authorization and journey: `alumni-portal/src/test/java/com/alumni/portal/integration/controller/FullJourneyIntegrationTest.java`

Run:

```text
mvn -Dtest=AlumniServiceTest test
mvn -Dit.test=FullJourneyIntegrationTest verify
```
