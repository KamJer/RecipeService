# Shopping List Recipe Service

Backend microservice that **stores and serves recipes** for a shopping-list ecosystem. It manages recipe metadata, ingredients, preparation steps, tags, and per-user recipe associations, exposes a REST API for search and CRUD-style operations, and uses the **user/security service** to validate JWT access tokens when callers need authenticated endpoints.

---

## What it does

- Persists **recipes** with **ingredients**, **steps**, **tags**, and optional publication/source fields.
- Supports **full-text and paginated** recipe search (by name, by product lists with optional “missing ingredient” tolerance, by required products, by tags).
- Maintains **user-specific recipe lists** via a `recipe_user` link table (resolved using the authenticated user from the security service).
- Exposes a read-only **tag catalog** endpoint.

## What it is for

This service is the **recipe domain** of the shopping-list platform: mobile or web clients can browse and search recipes, associate recipes with the logged-in user, and integrate ingredient data with shopping flows. It keeps **recipe data in its own database** while **identity and JWT issuance** stay in a separate service (see below).

---

## Related service: user database & authentication

Recipe endpoints that need the current user call the **Shopping Security Service**, which owns the **user database**, registration, login, JWT access/refresh tokens, and token validation.

- Repository: [KamJer/Shopping-security-service](https://github.com/KamJer/Shopping-security-service)

Configure the base URL of that API in this project (default in `application.properties`):

| Property | Default (local) | Purpose |
|----------|------------------|---------|
| `user.service.base-url` | `http://localhost:4443/user` | REST prefix for validating tokens and resolving users (must match how the security service exposes `/user`). |

Run the security service (typically on port **4443**) before relying on authenticated recipe features such as `GET /recipe/user`.

---

## Features

- Recipe **create/update/delete** and **paged reads** with multiple search strategies.
- **Tags** and **recipe–tag** relationships.
- **JWT-based authentication** integrated with the security service (stateless sessions).
- **MariaDB/MySQL** persistence with **Flyway** migrations.
- **Virtual threads** enabled (`spring.threads.virtual.enabled=true`).

---

## Technology stack

| Area | Technology |
|------|------------|
| Runtime | Java **21** (virtual threads) |
| Framework | **Spring Boot 3.5.4** |
| Web | Spring Web (REST), embedded Tomcat |
| Security | Spring Security, custom JWT filter & provider calling the user service |
| HTTP client | **Spring `RestClient`** (WebFlux on classpath for stack compatibility) |
| Persistence | Spring Data JPA, **Hibernate** 6.5.x |
| Database | **MariaDB** (MySQL connector also on classpath) |
| Migrations | **Flyway** (`flyway-mysql`) |
| Validation | Jakarta Validation (`spring-boot-starter-validation`) |
| Build | **Maven** |
| Other | Lombok, Spring Actuator, WebSocket starter, H2 (tests) |

---

## Prerequisites

- **JDK 21**
- **Maven 3.8+**
- **MariaDB** (or compatible MySQL) with a database for this service (default local name in config: `recipe_db`)
- **[Shopping-security-service](https://github.com/KamJer/Shopping-security-service)** running and reachable when using authenticated endpoints

---

## Configuration

### Profiles

| Profile | Main config | Behavior |
|---------|-------------|----------|
| Default | `application.properties` | Local defaults; datasource credentials should be supplied (see secrets) |
| `dev` | `application-dev.properties` | Imports `application-secret-dev.properties`, Flyway on, JPA `ddl-auto=validate` |
| `prod` | `application-prod.properties` | Imports `application-secret-prod.properties`, Flyway on, JPA `ddl-auto=none` |

Activate a profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

```bash
java -jar target/ShoppingListRecipeService-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Secret properties (for `dev` / `prod`)

Files `application-secret-dev.properties` and `application-secret-prod.properties` are **gitignored**. Create them under `src/main/resources/` and define at least your datasource (and any other overrides), for example:

- `spring.datasource.username`
- `spring.datasource.password`

**Security:** do not commit real credentials; use environment-specific values.

### Default application settings (`application.properties`)

| Setting | Default | Notes |
|---------|---------|--------|
| `spring.application.name` | `ShoppingListRecipeService` | |
| `server.port` | `6443` | |
| `spring.datasource.url` | `jdbc:mariadb://localhost:3306/recipe_db` | Adjust per environment |
| `user.service.base-url` | `http://localhost:4443/user` | Must point at the security service’s user API |

---

## Database and Flyway

- SQL migrations: `src/main/resources/db/migration/` (e.g. `V1__create_tables.sql`).
- Tables include `recipe`, `ingredient`, `step`, `tag`, `recipe_tag`, and `recipe_user` (links recipes to user identifiers from the security service).

Ensure the target database exists and matches the configured URL before starting the application with Flyway enabled.

---

## Build and run

```bash
mvn clean package -DskipTests
java -jar target/ShoppingListRecipeService-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

Development:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## API overview

Base path: **`/recipe`** and **`/tags`** (no global servlet context path in default config).

### Recipes (`/recipe`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `PUT` | `/recipe` | Per `SecurityConfig` | Insert recipe (body: `RecipeDto`) |
| `POST` | `/recipe` | Per `SecurityConfig` | Update recipe |
| `DELETE` | `/recipe/{recipeId}` | Per `SecurityConfig` | Delete recipe |
| `GET` | `/recipe/id/{id}` | Public | Recipe by id |
| `POST` | `/recipe/products` | Public | Recipes matching products (`RecipeRequestDto`, pageable) |
| `GET` | `/recipe/name/{query}` | Public | Search by name (pageable) |
| `POST` | `/recipe/products/required` | Public | Recipes requiring given products (pageable) |
| `POST` | `/recipe/tag` | Public | Recipes by tags (pageable) |
| `POST` | `/recipe/tag/required` | Public | Recipes requiring all given tags (pageable) |
| `GET` | `/recipe/user` | **Authenticated** | Paged recipes for the current user |
| `GET` | `/recipe` | Public | All recipes (pageable) |

### Tags (`/tags`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/tags` | Public | All tags |

### Security notes

- **CSRF is disabled** (typical for stateless JWT APIs).
- **`GET /recipe/user`** requires an authenticated principal; the service validates the JWT with the **Shopping Security Service** using `user.service.base-url`.
- Other **GET** requests are permitted without authentication in the current configuration; review `SecurityConfig` if you need to lock down writes or additional routes.

---

## Testing

```bash
mvn test
```

H2 is available in the **test** scope for isolated tests when configured.

---

## Project layout (high level)

```
src/main/java/pl/kamjer/ShoppingListRecipeService/
├── client/           # REST client to the user/security service
├── config/           # Security, JWT, RestClient
├── controller/       # REST API
├── exceptions/
├── model/            # JPA entities and DTOs
├── repository/
└── services/

src/main/resources/
├── application*.properties
└── db/migration/     # Flyway SQL
```

---