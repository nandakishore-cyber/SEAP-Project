# Testing map

- Unit: `alumni-portal/src/test/java/com/alumni/portal/unit/service/AuthServiceTest.java`
- JWT unit tests: `.../unit/security/JwtTokenProviderTest.java`
- API integration: `.../integration/controller/AuthControllerIntegrationTest.java`

Run:

```text
mvn -Dtest=AuthServiceTest,JwtTokenProviderTest test
mvn -Dit.test=AuthControllerIntegrationTest verify
```
