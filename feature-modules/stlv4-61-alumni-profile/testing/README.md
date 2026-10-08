# Testing map

- Unit: `alumni-portal/src/test/java/com/alumni/portal/unit/service/AlumniServiceTest.java`
- Integration: `alumni-portal/src/test/java/com/alumni/portal/integration/controller/FullJourneyIntegrationTest.java`
- Persistence: `.../integration/repository/AlumniProfileRepositoryIntegrationTest.java`

Run:

```text
mvn -Dtest=AlumniServiceTest test
mvn -Dit.test=FullJourneyIntegrationTest,AlumniProfileRepositoryIntegrationTest verify
```
