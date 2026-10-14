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

Create a local database and a dedicated MySQL account. Replace the placeholders
with values chosen for your machine; do not put real passwords in source files
or commit them:

```sql
CREATE DATABASE spendwise;
CREATE USER 'spendwise_user'@'localhost' IDENTIFIED BY '<choose-a-local-password>';
GRANT ALL PRIVILEGES ON spendwise.* TO 'spendwise_user'@'localhost';
```

Configure the connection in the PowerShell window where you will run the app.
Set the password to the local MySQL account password you chose:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/spendwise"
$env:DB_USERNAME = "spendwise_user"
$env:DB_PASSWORD = "<your-local-password>"
```

The environment variables are read by `application.properties`. Do not replace
them with real credentials in that file. The application uses Hibernate's
`update` mode for this local MVP, so MySQL must be running and the database must
already exist.

## Build and test

From the project root:

```powershell
mvn clean test
```

## Run

Set the MySQL environment variables above in the current PowerShell window, then
run:

```powershell
mvn spring-boot:run
```

Open [http://localhost:8082](http://localhost:8082). The page should display
“SpendWise” and “Your personal finance tracker starts here.” Stop the app with
`Ctrl+C`.
