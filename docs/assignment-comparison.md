# Comparison with the supplied assignment

Source: `D:\Downloads\MMS301_Assignment 02_Watch Management_Saga Pattern.docx`, read directly from its Word XML.

| Requirement | Existing project | Changes / verification |
|---|---|---|
| Four named microservices and API gateway | Present, HE190091 suffix | Retained; downstream access now requires a service key |
| SQL Server, code-first, database per service | Separate database URLs and entities present | Fixed Compose initialization: mounting SQL did not execute it |
| Exact mandatory seed data | Accounts, brands, categories and watches present | Corrected F-91W water resistance from 300 to 30 meters |
| Public watch list with all fields and CategoryName | Present; descending WatchID | Retained and regression-tested |
| Administrator login for add/update/delete | Gateway JWT checks present | Prevent direct downstream bypass and external counter manipulation |
| All fields required; price >= 100; name > 10; date today | Required fields and business checks present | Added database length/precision limits and positive foreign IDs; retained trimmed-name validation |
| New item at top | Descending WatchID | Retained; Postman verifies |
| CRUD updates related microservices using Saga | Happy paths present | Catch local transaction commit failures; reverse both brand changes on failed update; preserve deleted watch identity on failure |
| Saga audit and consistency | Logs shared rollback scope; silent compensation failures | Independent repository commits; explicit compensation-failure status; counter locks and watch versioning |
| Brand relationship integrity | Referenced brands could be deleted | Reject deletion with HTTP 409 when counter is nonzero |
| Postman scripts/tests | Existing collection | Added required-field, length, precision, brand integrity and internal-endpoint regressions; authenticated audit lookup; local-date calculation |

The document uses both `WaterpMaterial` and `WaterResistance`. The database/response field remains `waterpMaterial` for compatibility; requests accept either spelling. Its historical seed rows intentionally include short names, prices below 100 and past release dates. Those values remain unchanged; validation applies to new and updated items. The document assigns the IWC watch to BrandID 4, so that association is preserved.

The database-per-service rule means BrandID is a logical remote reference, not a cross-database SQL foreign key. CategoryID has a local JPA relationship. TotalWatches, SagaLogs and the watch Version column are implementation extensions supporting consistency.

Scope limits: synchronous compensation is not durable crash recovery. A crash or uncertain network response can require reconciliation. No automatic retry is enabled for counter mutations. SQL Server needs a separate runtime check; H2 test success does not establish SQL Server compatibility. Existing seeded data is not migrated automatically.

## Verification performed

On 2026-11-07, Java 21 and Maven `verify` successfully built all four applications. All 15 Java tests passed (9 watch/Saga, 2 brand integrity, 4 gateway). With all four packaged applications running and the three database services using the H2 profile, Newman completed 36 requests and 56 assertions with zero failures. A separate direct request to port 8083 returned HTTP 403. `git diff --check` passed.

Docker is unavailable in the verification environment, so the Compose initialization and SQL Server runtime have not been exercised. The four temporary H2 test processes were stopped after verification.


## Frontend and SQL Server 2019 follow-up

The project now defaults to SQL Server 2019 with sa/sa and includes a React JavaScript frontend with hooks and Bootstrap. The installed engine version is confirmed; SQL authentication remains pending user configuration. See [current verification notes](frontend-verification.md) for the tested frontend flows and the database login limitation.
