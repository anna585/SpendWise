# SpendWise

SpendWise is a small, server-rendered personal finance tracker. This initial
project foundation contains a Spring Boot application and a placeholder home
page; finance features will be added in later steps.

## Prerequisites

- Java Development Kit (JDK) 25 or later
- Apache Maven 3.9 or later
- MySQL Server 8.0 or later

Check the installed Java and Maven versions:

```powershell
java -version
mvn -version
```

## MySQL setup

The application connects to the MySQL database named `spendwise`. If it does
not exist, the JDBC connection creates it; if it already exists, the
application reuses it. The MySQL account must have permission to create the
database on first startup. To use a dedicated account, create it in MySQL and
grant it the required permissions. Do not put real passwords in source files
or commit them:

```sql
CREATE USER 'spendwise_user'@'localhost' IDENTIFIED BY '<choose-a-local-password>';
GRANT CREATE ON *.* TO 'spendwise_user'@'localhost';
GRANT ALL PRIVILEGES ON spendwise.* TO 'spendwise_user'@'localhost';
```

Set `DB_USERNAME` and `DB_PASSWORD` in the project-root `.env` file. It is
excluded from Git, and Spring Boot loads it when the app starts. Replace the
example values with the credentials for your local MySQL account:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/spendwise?createDatabaseIfNotExist=true
DB_USERNAME=spendwise_user
DB_PASSWORD=your-local-password
```

Do not commit `.env` or put real credentials in `application.properties`.
MySQL must be running. The
`createDatabaseIfNotExist=true` JDBC option creates the database when needed,
and Hibernate's `update` mode creates or updates tables for JPA entities
without using a drop-and-recreate mode. Back up the database before applying
schema changes to a database containing valuable data.

The current persistence model contains categories, transactions, and monthly
category budgets. Transaction and budget amounts use `DECIMAL(19,2)`. A
category cannot be deleted while referenced by a transaction or budget, and a
category can have at most one budget per month. Database foreign keys enforce
these references; finance records are not cascade-deleted. The project is a
single-user MVP and does not currently persist users, accounts, or savings
goals.

## Build and test

From the project root:

```powershell
mvn clean verify
```

Automated JPA schema tests use an isolated in-memory H2 database; they do not
connect to or modify the configured MySQL database.

## Run

Set the MySQL environment variables above in the current PowerShell window, then
run:

```powershell
mvn spring-boot:run
```

Open [http://localhost:8082](http://localhost:8082). The page should display
“SpendWise” and “Your personal finance tracker starts here.” Stop the app with
`Ctrl+C`.
