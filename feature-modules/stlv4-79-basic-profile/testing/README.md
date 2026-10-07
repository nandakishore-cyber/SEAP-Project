# Testing map

- Unit: `alumni-portal/src/test/java/com/alumni/portal/unit/service/ProfileServiceTest.java`
- Journey: `alumni-portal/src/test/java/com/alumni/portal/integration/controller/FullJourneyIntegrationTest.java`

Run:

```text
mvn -Dtest=ProfileServiceTest test
mvn -Dit.test=FullJourneyIntegrationTest verify
```
