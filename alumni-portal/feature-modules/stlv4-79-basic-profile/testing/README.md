# Testing map

- Unit tests: `alumni-portal/src/test/java/com/alumni/portal/unit/service/ProfileServiceTest.java`
- Journey tests: `alumni-portal/src/test/java/com/alumni/portal/integration/controller/FullJourneyIntegrationTest.java`

Run from `alumni-portal/`:

```text
mvn -Dtest=ProfileServiceTest test
mvn -Dit.test=FullJourneyIntegrationTest verify
```
