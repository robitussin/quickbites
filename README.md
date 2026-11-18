# QuickBite - Simple Food Ordering System (sample project)

A small Java Swing + JDBC + MySQL application used as a worked example.

## Setup
1. Install JDK 21, Maven 3.9, and MySQL 8.0.16 or later.
2. Create the database and user:
   ```sql
   CREATE DATABASE quickbite;
   CREATE USER 'qb_user'@'localhost' IDENTIFIED BY 'change_me';
   GRANT ALL PRIVILEGES ON quickbite.* TO 'qb_user'@'localhost';
   ```
3. Load the tables and (optionally) starter data:
   ```
   mysql -u qb_user -p quickbite < db/schema.sql
   mysql -u qb_user -p quickbite < db/seed_data.sql
   ```
4. Copy `config/config.properties.example` to `config/config.properties` and edit it.
5. Build and run **from the project folder** (the app reads `config/config.properties` from the current folder):
   ```
   mvn clean package
   java -jar target/quickbite.jar
   ```

## Project layout
```
src/main/java/com/quickbite/
  Main.java
  ui/         MainFrame, MenuPanel, OrderPanel, OrdersPanel, BackgroundTask
  service/    MenuService, OrderService
  model/      MenuItem, Order, OrderItem, Status
  dao/        MenuItemDao, JdbcMenuItemDao, OrderDao, JdbcOrderDao, DbConnection
  exception/  ValidationException, DomainException, NotFoundException, DataAccessException
  report/     SalesReport
db/           schema.sql, seed_data.sql
```

## Business rules
| ID | Rule | Where |
| --- | --- | --- |
| BR-01 | Item name unique and not blank | MenuService, UNIQUE constraint |
| BR-02 | Price greater than zero | MenuService, CHECK constraint |
| BR-03 | Quantity per line 1 to 10 | OrderService, CHECK constraint |
| BR-04 | Only available items can be ordered | OrderService.placeOrder() |
| BR-05 | Status moves PENDING, PREPARING, READY, COMPLETED | Status.canTransitionTo() |
| BR-06 | Only PENDING orders can be cancelled | OrderService.cancel() |
