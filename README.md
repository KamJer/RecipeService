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
| `dev` | `application-dev.properties` | Flyway on, JPA `ddl-auto=validate` |
| `prod` | `application-prod.properties` | Flyway on, JPA `ddl-auto=none` |

Activate a profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

```bash
java -jar target/ShoppingListRecipeService-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

| Variable | Description |
|----------|-------------|
| `DB_USERNAME` | MariaDB username |
| `DB_PASSWORD` | MariaDB password |

### Default application settings (`application.properties`)

| Setting | Default | Notes |
|---------|---------|-------|
| `spring.application.name` | `ShoppingListRecipeService` | |
| `server.port` | `6443` | |
| `spring.datasource.url` | `jdbc:mariadb://localhost:3306/recipe_db` | Adjust per environment |
| `user.service.base-url` | `http://localhost:4443/user` | Must point at the security service's user API |

---

## Database and Flyway

- SQL migrations: `src/main/resources/db/migration/` (e.g. `V1__create_tables.sql`).
- Tables include `recipe`, `ingredient`, `step`, `tag`, `recipe_tag`, and `recipe_user` (links recipes to user identifiers from the security service).

Ensure the target database exists and matches the configured URL before starting the application with Flyway enabled.

---

## Build and run

You need **JDK 21**, **Maven 3.8+**, and a running **MariaDB** instance with database `recipe_db`.

```bash
# Set required environment variables
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password

# Build
mvn clean package

# Run with dev profile
java -jar target/ShoppingListRecipeService-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Run with prod profile
java -jar target/ShoppingListRecipeService-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Development:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## API overview

Base path: **`/recipe`** and **`/tags`** (no global servlet context path in default config).

Authentication is via `Authorization: Bearer <JWT>` header. Write endpoints (PUT, POST, DELETE) and `GET /recipe/user` require a valid JWT. All other GET endpoints are public.

### Recipes (`/recipe`)

| Method | Path | Auth | Request | Response | Description |
|--------|------|------|---------|----------|-------------|
| `PUT` | `/recipe` | JWT | `RecipeDto` JSON | `RecipeDto` | Insert a new recipe |
| `POST` | `/recipe` | JWT | `RecipeDto` JSON | `RecipeDto` | Update an existing recipe |
| `DELETE` | `/recipe/{recipeId}` | JWT | — | `200 OK` | Delete recipe (owner only) |
| `GET` | `/recipe/id/{id}` | Public | — | `RecipeDto` | Get recipe by id |
| `GET` | `/recipe/name/{query}` | Public | — | `Page<RecipeDto>` | Search by name (pageable) |
| `POST` | `/recipe/products` | Public | `RecipeRequestDto` | `Page<RecipeDto>` | Recipes matching given products (paged) |
| `POST` | `/recipe/products/required` | Public | `RecipeRequestDto` | `Page<RecipeDto>` | Recipes requiring **all** given products (paged) |
| `POST` | `/recipe/tag` | Public | list of tag names | `Page<RecipeDto>` | Recipes matching **any** given tag (paged) |
| `POST` | `/recipe/tag/required` | Public | list of tag names | `Page<RecipeDto>` | Recipes requiring **all** given tags (paged) |
| `GET` | `/recipe/user` | **JWT** | — | `Page<RecipeDto>` | Paged recipes owned by the authenticated user |
| `GET` | `/recipe` | Public | — | `Page<RecipeDto>` | All recipes (paged) |

### Tags (`/tags`)

| Method | Path | Auth | Response | Description |
|--------|------|------|----------|-------------|
| `GET` | `/tags` | Public | `List<String>` | All available tags |

### Data types – JSON schemas

#### `RecipeDto`

```json
{
  "recipeId": null,
  "name": "Spaghetti Carbonara",
  "description": "Klasyczny włoski przepis",
  "ingredients": [
    {
      "ingredientId": null,
      "name": "Makaron spaghetti",
      "amount": "200",
      "unit": "g"
    },
    {
      "ingredientId": null,
      "name": "Jajka",
      "amount": "3",
      "unit": "szt."
    }
  ],
  "steps": [
    {
      "stepId": null,
      "stepNumber": 1,
      "description": "Ugotuj makaron al dente"
    },
    {
      "stepId": null,
      "stepNumber": 2,
      "description": "Wymieszaj jajka z serem"
    }
  ],
  "tags": ["włoskie", "makaron"],
  "userName": null,
  "source": "https://przepisy.pl/carbonara",
  "published": true
}
```

| Field | Type | Description |
|-------|------|-------------|
| `recipeId` | number (nullable) | `null` for new recipes; set for updates |
| `name` | string (max 255) | **Required.** Recipe name |
| `description` | string (nullable) | Free‑text description |
| `ingredients` | array of `IngredientDto` | List of ingredients |
| `steps` | array of `StepDto` | Preparation steps |
| `tags` | array of string | Tag names (created on the fly if new) |
| `userName` | string (nullable) | Ignored on create — set from JWT |
| `source` | string (max 255, nullable) | Optional URL or attribution |
| `published` | boolean (nullable) | `true` = visible in public search |

#### `IngredientDto`

```json
{
  "ingredientId": null,
  "name": "Makaron spaghetti",
  "amount": "200",
  "unit": "g"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `ingredientId` | number (nullable) | `null` for new ingredients |
| `name` | string | Ingredient name |
| `amount` | string | Quantity as text (e.g. "200", "1", "do smaku") |
| `unit` | string | Unit (e.g. "g", "ml", "szt.") |

#### `StepDto`

```json
{
  "stepId": null,
  "stepNumber": 1,
  "description": "Ugotuj makaron al dente"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `stepId` | number (nullable) | `null` for new steps |
| `stepNumber` | number | Order of the step (1‑based) |
| `description` | string | Step instruction |

#### `RecipeRequestDto` (used in product searches)

```json
{
  "products": ["jajka", "makaron"],
  "maxMissing": 1,
  "page": 0,
  "size": 20
}
```

| Field | Type | Description |
|-------|------|-------------|
| `products` | array of string | Product names to match against ingredient names |
| `maxMissing` | number (optional) | Tolerance — how many products may be missing (default `0`) |
| `page` | number | Page index (0‑based, default `0`) |
| `size` | number | Page size (default `20`) |

### Pagination

All `GET` endpoints that return multiple recipes use Spring Data pagination. Add query parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Page index (0‑based) |
| `size` | `20` | Number of items per page |
| `sort` | — | Sort property, e.g. `sort=name,asc` |

**Response format:**
```json
{
  "content": [ ... RecipeDto ... ],
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

### Security notes

- **CSRF is disabled** (typical for stateless JWT APIs).
- **`GET /recipe/user`** requires an authenticated principal; the service validates the JWT with the **Shopping Security Service** using `user.service.base-url`.
- Other **GET** requests are permitted without authentication; review `SecurityConfig` if you need to lock down writes or additional routes.

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