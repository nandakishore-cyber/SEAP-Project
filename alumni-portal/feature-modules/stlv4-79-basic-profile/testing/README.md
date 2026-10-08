# Testing

The copied test modules are:

- `testing/src/test/java/com/alumni/portal/unit/service/ProfileServiceTest.java`
- `testing/integration/FullJourneyIntegrationTest.java`

Run the canonical application tests from `alumni-portal/`:

```text
mvn -Dtest=ProfileServiceTest test
mvn -Dit.test=FullJourneyIntegrationTest verify
```
