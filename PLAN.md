# SpendWise MVP Plan

## Purpose and starting point

SpendWise is a beginner-sized personal finance tracker for one local user, built with Java, Spring Boot, Spring MVC, Thymeleaf, Spring Data JPA, Hibernate, MySQL, Maven, JUnit 5, and Mockito. The MVP will provide transaction CRUD, category organization, monthly totals, category budgets, and a simple spending report.

The inspected `SpendWise` project folder currently contains no project files (no `pom.xml`, Java sources, configuration, or documentation). This plan therefore assumes a new application rather than adapting existing code.

## MVP scope

### Included

- Create, view, edit, and delete income and expense transactions.
- Assign each transaction to one category.
- Show a monthly summary of income, expenses, and balance.
- Create and maintain a monthly spending budget for an expense category.
- View a simple spending report for a selected month, including per-category expense totals compared with budgets.
- Use server-rendered Thymeleaf pages and conventional Spring MVC form submissions.
- Validate input and show understandable validation, not-found, and database/configuration errors.
- Use `BigDecimal` for every monetary value and persist data in MySQL.
- Keep the application single-user, local/demo oriented, and small enough to explain in an exam.

### Explicitly excluded

- Authentication, multiple users, roles, or account sharing.
- Bank/payment integrations, imports, scheduled transactions, recurring payments, or currency conversion.
- REST API, JavaScript-heavy client, mobile app, charts beyond simple tables/totals, exports, notifications, and forecasting.
- Microservices, Docker/platform deployment work, and additional infrastructure not required for the local exam demonstration.
- An elaborate category hierarchy or soft-delete/audit framework.

## Architecture and package layout

Use a standard Maven Spring Boot application with a single root package, for example `com.example.spendwise`, organized by layer:

- `controller`: `TransactionController`, `CategoryController`, `BudgetController`, and `ReportController`. Bind web forms, invoke services, and select Thymeleaf views; do not calculate totals or implement business rules here.
- `service`: `TransactionService`, `CategoryService`, `BudgetService`, and `ReportService`. Own validation beyond simple field constraints, CRUD orchestration, monthly/date filtering, budget uniqueness, and financial calculations.
- `repository`: Spring Data JPA repositories for entities. Keep database access and query methods here.
- `entity`: JPA entities and enums.
- `dto` or `form` (only where useful): web form inputs and report/summary view models. Avoid exposing persistence entities as complicated page models if a small dedicated model is clearer.
- `src/main/resources/templates`: Thymeleaf pages and reusable fragments.
- `src/main/resources/static`: minimal CSS.
- `src/main/resources/application.properties`: local MySQL connection settings provided through environment variables or local configuration, not committed credentials.
- `src/test/java`: service unit tests (JUnit 5 + Mockito) and focused MVC/persistence tests as appropriate.

Controllers handle HTTP concerns, services own business rules, repositories perform persistence, and templates render results and validation messages. Use constructor injection. Use Spring validation annotations for basic input constraints and service-level checks for rules involving existing data or multiple records. Prefer clear user feedback and centralized handling of expected not-found cases over silent fallbacks.

## Proposed entities and relationships

### `Category`

- `id`: generated identifier.
- `name`: required, trimmed, case-insensitively unique category name; enforce duplicate detection in service logic and a database constraint where portable.
- `type`: `INCOME` or `EXPENSE` enum, required.
- `description`: optional, length-limited.
- Relationship: one category has many transactions; one expense category may have many monthly budgets.

Seed a small set of common income and expense categories for an easy first run, while allowing the user to add categories. Category type must match the transaction type. Do not allow deleting a category that is still referenced by transactions or budgets; explain the constraint in the UI.

### `Transaction`

- `id`: generated identifier.
- `type`: `INCOME` or `EXPENSE` enum.
- `amount`: required `BigDecimal`, greater than zero, with a fixed two-decimal scale for this MVP.
- `transactionDate`: required `LocalDate`.
- `description`: required short text with a reasonable maximum length so each row is identifiable.
- `category`: required many-to-one relationship to `Category`.
- Optional `createdAt`/`updatedAt` timestamps only if they remain low effort; they are not needed for core acceptance.

Store amount as a decimal column with adequate precision, not floating point. Disallow zero and negative transaction amounts; represent direction with the type rather than a signed amount. Category selection must be limited to categories of the matching type.

### `MonthlyBudget`

- `id`: generated identifier.
- `month`: first-of-month `LocalDate` as the JPA representation of the budget month; normalize to the first day when creating or updating.
- `amount`: required positive `BigDecimal`, scale two.
- `category`: required many-to-one relationship to an expense `Category`.
- Unique rule: at most one budget for a category in a month, enforced in service logic and preferably by a database unique constraint over category and month.

Budget usage is derived from expense transactions belonging to the same category and month; it is not persisted separately. A report labels usage as under budget, at budget, or over budget without imposing blocking transaction rules.

### Relationships at a glance

- `Category 1 -> many Transaction`
- `Category 1 -> many MonthlyBudget`
- Every `Transaction` has exactly one `Category`.
- Every `MonthlyBudget` has exactly one expense `Category`.
- No user entity or user relationship is included in this single-user MVP.

## Key behavior and edge cases

- Interpret month boundaries from the first date of the month inclusive to the first date of the next month exclusive; handle December/year rollover.
- Calculate monthly income and expenses as sums by type, then balance = income - expenses. Empty months display zero totals.
- Calculate category spending from expense transactions only. Income transactions must never reduce budget usage.
- An expense category budget may be absent; show “No budget set” rather than implying a zero budget or failing the report.
- Reject negative, zero, malformed, missing, or excessive-precision monetary input with a field-level message. Do not use `double`/`float` or silently round user input.
- Allow future transaction dates in this MVP; date range and category/type rules still apply.
- Reject budgets attached to income categories and transactions assigned to a category of the other type.
- Prevent duplicate category names and duplicate category/month budgets with clear messages.
- When editing/deleting missing IDs, return a not-found response/page rather than silently succeeding.
- A category referenced by records cannot be deleted; do not cascade data loss.
- Database credentials must be externalized. A missing/unavailable MySQL instance should fail visibly with actionable setup information, not be represented as an empty successful application.
- For decimal aggregation, preserve consistent scale and handle no-row results as `BigDecimal.ZERO`.
- Use a selected month query parameter with the current month as the default; reject invalid month input with a clear message.

## Seven-day implementation stages

Target workload: approximately 1–2 hours per day, with the simplest complete demonstration prioritized over polish. If a stage runs long, defer visual refinements rather than core validation or tests.

### Day 1 — Project foundation and domain outline

- Generate the Maven Spring Boot project with only necessary starters: Spring Web, Thymeleaf, Spring Data JPA, Validation, MySQL driver, and Spring Boot Test.
- Set package structure, application entry point, basic configuration conventions, and README setup notes.
- Create the three enums/entities and repository interfaces; define relationships and decimal/date column mappings.
- Configure local MySQL connection via environment variables and document schema/database creation.
- Check the application starts against a local MySQL database.

### Day 2 — Categories and transaction CRUD

- Implement category and transaction services, repositories, and MVC controllers.
- Add list/create/edit/delete pages and forms for categories and transactions.
- Validate required fields, positive amounts, category/type compatibility, and references.
- Add seed data or a small documented setup step for useful initial categories.

### Day 3 — Monthly summary

- Add repository queries for transaction totals filtered by date range and type.
- Implement a service-owned monthly summary calculation.
- Add a home/dashboard page with month selection, income, expenses, and balance.
- Verify empty month and year-boundary behavior.

### Day 4 — Budgets

- Implement budget create/edit/list flows with one budget per expense category and month.
- Validate positive amounts, expense category type, and duplicate month/category combinations.
- Add service methods that compute actual monthly expense per budget category.

### Day 5 — Spending report and usability

- Implement selected-month category expense breakdown, budget amount, used amount, remaining amount, and status.
- Handle categories without budgets and months without transactions.
- Improve navigation, validation display, confirmation for delete, and concise styling.

### Day 6 — Tests and error paths

- Add focused service unit tests with JUnit 5 and Mockito for transaction/category/budget rules and calculations.
- Add MVC tests for form validation, page responses, and not-found/error behavior where useful.
- Add a small persistence/integration test for mappings and uniqueness if the local test setup permits; otherwise clearly document a manual MySQL verification checklist.
- Run the Maven test suite and fix regressions.

### Day 7 — Acceptance pass and exam evidence

- Run through every acceptance criterion using the UI and a clean database.
- Verify no monetary values use floating point and no secrets are tracked.
- Update README with setup/run/test steps, screenshots or concise demonstration steps, design decisions, and known limitations.
- Complete the AI-assisted development log and prepare a short exam walkthrough.

## Acceptance criteria

1. The application starts locally with documented MySQL configuration and no committed credentials.
2. A user can create, view, edit, and delete income and expense transactions through server-rendered pages.
3. Transactions require a positive `BigDecimal` amount, a date, a valid category, and the correct category type; invalid form submissions show understandable messages and do not persist invalid data.
4. Categories can be created/listed and cannot be deleted while referenced by transactions or budgets.
5. For a chosen month, the dashboard shows income total, expense total, and `income - expenses` balance; empty months show zero.
6. A user can create and update one positive monthly budget per expense category and month.
7. The spending report lists expense totals by category and compares them with any budget for that category/month, including missing-budget, under-budget, equal-budget, and over-budget states.
8. Month filtering is correct across month/year boundaries, including December to January.
9. Missing record IDs and duplicate/invalid data result in explicit, user-friendly errors rather than silent success.
10. Automated tests cover core calculation and validation rules, and the documented test command passes.
11. The project remains a single beginner-level Spring Boot application with the requested layered architecture and no excluded integrations.

## Testing strategy

- **Service unit tests (primary):** Mockito repositories; verify CRUD orchestration, invalid category/type rejection, positive amount/budget checks, duplicate budgets, monthly sums, balance, category spending, no-data months, and budget status thresholds.
- **Controller/MVC tests:** verify GET pages and form submissions, field validation errors, redirect/success flow, selected month handling, and expected not-found responses. Mock services for focused controller behavior.
- **Persistence/integration checks:** verify JPA mappings, decimal precision, relationships, and uniqueness using a test database if feasible. Keep MySQL production/local settings separate from test settings; avoid requiring a developer's personal database for ordinary unit tests.
- **Manual acceptance test:** create categories, create/edit/delete each transaction type, test invalid amounts and mismatched category types, create budgets, inspect under/equal/over budget reports, inspect an empty month, and check a December/January boundary.
- **Commands:** document `mvn test` and `mvn spring-boot:run` with the required local database environment variables. Run the narrow relevant tests while implementing and the full test suite before the exam demonstration.

## Risks and mitigations

- **MySQL setup consumes limited time:** keep the schema simple, document exact local setup, externalize connection settings, and write business-rule unit tests that do not need MySQL.
- **Date/month mapping ambiguity in JPA:** map a budget month to the first day of the month as `LocalDate`, normalize and validate at the service boundary, and use date ranges for transaction queries.
- **Money precision errors:** use `BigDecimal` end to end, decimal database columns, positive-value validation, and tests around scale and aggregation.
- **Scope creep:** defer charts, authentication, REST APIs, advanced styling, imports, exports, and deployment; deliver a table-based report and a polished happy path.
- **Relationship deletion causing data loss:** block deletion of referenced categories and explain why.
- **Business logic leaking into controllers/templates:** keep calculations and cross-record validation in services; templates render only supplied values.
- **Tests coupled to local MySQL:** make service tests the required baseline and isolate optional integration tests from the normal run where needed.
- **Insufficient exam evidence:** maintain the AI development log from the first session and retain exact test outcomes and decision rationale.

## AI-assisted development exam documentation

Maintain a concise `AI-LOG.md` in the project root during implementation, plus any required exam form. For each work session, record:

- Date/session and the feature or problem being worked on.
- The prompt sent to the AI (or a faithful, redacted summary if the exam forbids storing full prompts); include the initial prompt and meaningful follow-ups.
- What the AI suggested and what was accepted, changed, or rejected, with the reason.
- Human decisions made, alternatives considered, and resulting design choice.
- Files changed and a short explanation of the changes.
- Tests/commands run, expected and actual outcomes, and any failures fixed.
- Challenges, debugging evidence, remaining limitations, and the next small step.

Keep the log factual and contemporaneous; do not claim AI-generated code was independently authored. Never record passwords, database credentials, personal financial data, or other secrets. Include a final README section describing AI use and linking to the log if permitted by the exam rules. Preserve failed test output or a brief reproducible summary when it demonstrates a challenge and its resolution.

Suggested per-session log template:

```text
## Session N — YYYY-MM-DD
- Goal:
- Prompt(s):
- AI suggestions:
- Decisions and rationale:
- Changes made:
- Tests/commands and results:
- Challenge/resolution:
- Next small step:
```

## Next smallest step

After reviewing this plan, create the root Maven project skeleton and README setup instructions only; do not begin implementing domain behavior until the plan is approved.
