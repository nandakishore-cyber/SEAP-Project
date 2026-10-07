# Testing map

- Unit: `alumni-portal/src/test/java/com/alumni/portal/unit/service/AlumniServiceTest.java`
- Repository: `alumni-portal/src/test/java/com/alumni/portal/integration/repository/AlumniProfileRepositoryIntegrationTest.java`
- Journey: `alumni-portal/src/test/java/com/alumni/portal/integration/controller/FullJourneyIntegrationTest.java`

Run:

```text
mvn -Dtest=AlumniServiceTest test
mvn -Dit.test=AlumniProfileRepositoryIntegrationTest,FullJourneyIntegrationTest verify
```
