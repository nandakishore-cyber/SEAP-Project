# Testing

The copied test modules are:

- `testing/src/test/java/com/alumni/portal/unit/service/AlumniServiceTest.java`
- `testing/integration/AlumniProfileRepositoryIntegrationTest.java`
- `testing/integration/FullJourneyIntegrationTest.java`

Run the canonical application tests from `alumni-portal/`:

```text
mvn -Dtest=AlumniServiceTest test
mvn -Dit.test=AlumniProfileRepositoryIntegrationTest,FullJourneyIntegrationTest verify
```
