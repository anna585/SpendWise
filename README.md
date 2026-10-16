# SpendWise

SpendWise is a single-user, server-rendered personal finance tracker built
with Spring Boot, Thymeleaf, Spring Data JPA, Bean Validation, and MySQL. The
current web application lets you create and list income/expense categories
and create, list, edit, and delete income/expense transactions.

Transactions have a positive decimal amount, date, description, type, and one
category of the same type. Amounts are stored as `DECIMAL(19,2)`. Transaction
deletion requires a confirmation page and a POST submission. Category
management currently supports listing and creation only; categories referenced
by transactions or budgets cannot be deleted by the database.

The persistence model also includes monthly category budgets, with one budget
per category per month. Budget management, monthly summaries, and spending
reports are not currently available in the web UI.

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

Create the `spendwise` database using an administrator account, then create a
dedicated application user with access limited to that database:

```sql
CREATE DATABASE spendwise;
CREATE USER 'spendwise_user'@'localhost' IDENTIFIED BY '<choose-a-local-password>';
GRANT ALL PRIVILEGES ON spendwise.* TO 'spendwise_user'@'localhost';
```

Set the following values in the project-root `.env` file. Replace the example
username and password with the local MySQL account credentials:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/spendwise?createDatabaseIfNotExist=true
DB_USERNAME=spendwise_user
DB_PASSWORD=your-local-password
```

Spring Boot imports this file as configuration. `.env` is excluded from Git;
do not commit it or place real credentials in source files. `DB_URL` may be
changed for a different local MySQL host or port.

The application listens on port `8082`. Hibernate uses the configured
non-dropping `update` schema mode; it does not use `create` or `create-drop`.
Back up an existing database before starting the application after schema
changes. A missing database may be created by the JDBC URL option, so the
MySQL account needs suitable database-creation rights if you rely on that
behavior instead of creating the database first.

## Build and test

Run all tests and Maven verification from the project root:

```powershell
mvn clean verify
```

Tests requiring JPA use an isolated in-memory H2 database and do not connect
to or modify MySQL.

## Run the application

With MySQL running and `.env` configured, start the server:

```powershell
mvn spring-boot:run
```

Open [http://localhost:8082](http://localhost:8082). Use the home page links
to manage categories and transactions. Create at least one income and/or
expense category before adding transactions; the selected category must match
the transaction type. Stop the application with `Ctrl+C`.

## Current scope

SpendWise currently has no authentication or multi-user support. It does not
include account management, savings goals, transaction imports, recurring
transactions, budget/report pages, or external financial integrations.
