# SpendWise AI

**A Personal Finance and Budget Management Application**

SpendWise AI is a single-user, server-rendered personal finance application built with Java, Spring Boot, Thymeleaf, Spring Data JPA, Bean Validation, and MySQL.

The application helps users organize income and expenses, manage categories, monitor monthly financial summaries, and track spending against monthly budgets. The project is developed using an AI-assisted development workflow.

## Features

### Dashboard
- Displays total income, total expenses, and balance for the current month.
- Uses monthly transaction data to calculate financial summaries.

### Transaction Management
- Create, list, edit, and delete income and expense transactions.
- Each transaction has a positive decimal amount, date, description, type, and category.
- The selected category must match the transaction type.
- Transaction deletion requires confirmation and a POST submission.

### Category Management
- Create and list income and expense categories.
- Prevent duplicate category names according to the application's validation rules.
- Categories referenced by transactions or budgets are protected by database relationships.

### Monthly Budgets and Reports
- Create, edit, and delete monthly budgets for expense categories.
- Maintain one budget per category per month.
- View budgeted amounts, actual spending, and remaining amounts for a selected month.
- Identify categories that are over budget and expenses that are not covered by a budget.

### User Interface
- Shared layout and navigation.
- Responsive styling for the dashboard, transactions, categories, and budgets.
- Consistent typography, buttons, forms, tables, and validation messages.

## Technology Stack

- Java 25
- Spring Boot 3.5.6
- Apache Maven
- Spring Data JPA and Hibernate
- Bean Validation
- Thymeleaf
- MySQL
- HTML and CSS
- JUnit 5 and Mockito
- H2 for isolated persistence tests

## Prerequisites

- JDK 25 or later
- Apache Maven 3.9 or later
- MySQL Server 8.0 or later

Check the installed tools:

```powershell
java -version
mvn -version
```

## Configure MySQL

Create the `spendwise` database using a MySQL administrator account, then create a dedicated application user:

```sql
CREATE DATABASE spendwise;
CREATE USER 'spendwise_user'@'localhost' IDENTIFIED BY '<choose-a-local-password>';
GRANT ALL PRIVILEGES ON spendwise.* TO 'spendwise_user'@'localhost';
```

Create a `.env` file in the project root with the following settings. Replace the example password with your local MySQL account password:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/spendwise?createDatabaseIfNotExist=true
DB_USERNAME=spendwise_user
DB_PASSWORD=your-local-password
```

The application imports `.env` as configuration. The file is excluded from Git; never commit real credentials or place passwords in source files.

The application listens on port `8082`.

Hibernate uses the configured non-dropping `update` schema mode. Back up an existing database before starting the application after schema changes. If the database is not created manually, the JDBC URL option may create it automatically; in that case, the MySQL account needs the appropriate database-creation permissions.

## Build and Test

Run the full Maven verification lifecycle from the project root:

```powershell
mvn clean verify
```

JPA tests use an isolated in-memory H2 database and do not connect to or modify the MySQL database.

## Run the Application

Make sure MySQL is running and the project-root `.env` file is configured.

Start the application:

```powershell
mvn spring-boot:run
```

Open the application in your browser:

[http://localhost:8082](http://localhost:8082)

Use the navigation to access the dashboard, transactions, categories, and monthly budgets. Create the required expense categories before assigning budgets, and create categories matching the transaction type before adding transactions.

Stop the application with `Ctrl+C` in the terminal running the server.

## Project Architecture

The application follows a layered Spring Boot architecture:

- **Controllers** — handle HTTP requests and page navigation.
- **Services** — implement business logic and calculations.
- **Repositories** — access persisted data through Spring Data JPA.
- **Entities and models** — represent transactions, categories, budgets, and reporting data.
- **Thymeleaf templates** — render server-side HTML pages.
- **CSS** — provides shared styling and responsive layouts.
- **Tests** — verify application behavior and business rules.

## AI-Assisted Development

AI development tools support implementation, code review, testing, and iterative UI improvements. AI-generated changes are reviewed and verified as part of the development workflow.

Potential future enhancements include AI-powered spending insights, personalized budgeting suggestions, and automated expense analysis. These are future possibilities, not current features.

## Current Scope and Limitations

- The application is designed for a single user and does not currently provide authentication or multi-user support.
- It does not currently include account management, savings goals, transaction imports, recurring transactions, or external financial integrations.
- AI-powered financial analysis and recommendations are not currently implemented.

## License

No license has been specified yet.