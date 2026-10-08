# Testing map

- Unit: `alumni-portal/src/test/java/com/alumni/portal/unit/service/AuthServiceTest.java`
- Integration: `alumni-portal/src/test/java/com/alumni/portal/integration/controller/AuthControllerIntegrationTest.java`
- End-to-end journey: `.../integration/controller/FullJourneyIntegrationTest.java`

Run:

```text
mvn -Dtest=AuthServiceTest test
mvn -Dit.test=AuthControllerIntegrationTest,FullJourneyIntegrationTest verify
```
