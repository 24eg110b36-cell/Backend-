# Sales Forecast - Backend (Spring Boot + MySQL)

Requirements: JDK 17+, Maven 3.8+, MySQL 8 running on localhost:3306.

1. Open `src/main/resources/application.properties` and set your MySQL username/password.
   (The database `sales_forecast_db` is created automatically.)
2. Run:  `mvn spring-boot:run`
3. API runs on http://localhost:8081  (sample data is inserted on first start)

Endpoints
- GET/POST        /api/products        PUT/DELETE /api/products/{id}
- GET/POST        /api/sales           PUT/DELETE /api/sales/{id}
- GET             /api/dashboard/summary
- GET             /api/forecast?productId=1&months=6   (productId optional = all products)

You can also import this folder as a Maven project in IntelliJ / Eclipse / STS and run SalesForecastApplication.
