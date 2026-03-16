## Spring JWT Todo API

A Spring Boot 4.x REST API that secures a multi-user Todo application using **Spring Security**, **JWT (access + refresh tokens)**, and **Spring Data JPA** with PostgreSQL.  
The project demonstrates a stateless authentication flow where users register, log in to obtain a JWT access token and refresh token, and then access protected Todo endpoints with role-based access control.

---

## Tech Stack

- **Backend**: Spring Boot 4.0.x (Java 21)
- **Security**: Spring Security, custom `SecurityFilterChain`, `OncePerRequestFilter`
- **Authentication**: JWT (JJWT 0.12.5 – `jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- **Persistence**: Spring Data JPA, Hibernate
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Utilities**: Lombok, Jakarta Validation

---

## Prerequisites & Database Setup

- **Java**: 21+
- **Maven**: 3.9+
- **PostgreSQL**: Any recent version

### Database configuration

The datasource and JPA configuration are defined in `application.yaml`:

- **JDBC URL**: configured via `DB_URL` env var (with a Supabase/PostgreSQL default)
- **Username**: `DB_USER`
- **Password**: `DB_PASSWORD`
- **Driver**: `org.postgresql.Driver`
- **Hibernate**:
  - `ddl-auto: update` (schema auto-migration in dev)
  - `show-sql: true`, formatted SQL with comments

To run locally with your own Postgres:

1. Create a database, e.g. `spring_jwt_todos`.
2. Set the following environment variables (PowerShell example):

   ```powershell
   $env:DB_URL="jdbc:postgresql://localhost:5432/spring_jwt_todos"
   $env:DB_USER="your_db_user"
   $env:DB_PASSWORD="your_db_password"
   ```

3. Start the app; Hibernate will create/update the tables automatically.

The server listens on `PORT` (default `8080`), configurable via environment variable.

---

## Installation & Running the Application

1. **Clone the repository**

   ```bash
   git clone <this-repo-url>
   cd spring-jwt
   ```

2. **Build the project**

   ```bash
   mvn clean install
   ```

3. **Run the application**

   ```bash
   mvn spring-boot:run
   ```

4. The API will be available at:

   - Base URL: `http://localhost:8080`
   - API root: `/api/todos/v1`

---

## Security Architecture

This project uses **stateless JWT-based authentication** with Spring Security.

### Security configuration

Defined in `SecurityConfig`:

- **CSRF**: disabled for stateless REST API.
- **Session management**: `SessionCreationPolicy.STATELESS` – no HTTP sessions.
- **Endpoint security rules**:
  - `/api/todos/v1/auth/**` → **permit all** (register, login, refresh, logout)
  - Any other request → **authenticated**
- **Filter chain**:
  - Custom `JwtAuthenticationFilter` is added **before** `UsernamePasswordAuthenticationFilter`.

### Authentication flow

1. **Registration**
   - Client calls `POST /api/todos/v1/auth/register` with username, email, password, and (optional) role.
   - `AuthService`:
     - Encodes the password using `BCryptPasswordEncoder` (`ApplicationConfig` bean).
     - Saves a `User` entity with a `Role` (`USER` default if not provided).
   - Returns basic user information (without tokens).

2. **Login**
   - Client calls `POST /api/todos/v1/auth/login` with username and password.
   - `AuthenticationManager` authenticates via `UsernamePasswordAuthenticationToken`.
   - On success:
     - `JwtService.generateToken(userId, username)` creates a **short-lived access token**:
       - Claims: `userId`, `sub` (username)
       - Signed with HMAC using a shared secret key.
       - Expiration: **15 minutes**.
     - `RefreshTokenService.createRefreshToken(userId)` creates a **refresh token**:
       - Random UUID string.
       - Stored in `refresh_tokens` table with expiry **7 days**.
   - Response (`AuthResponse`): `accessToken`, `refreshToken`, `userId`, `username`, `role`.

3. **Requesting protected resources**
   - Client sends `Authorization: Bearer <accessToken>` with each request to protected endpoints (e.g. Todos).
   - `JwtAuthenticationFilter`:
     - Runs once per request (`OncePerRequestFilter`).
     - Extracts `Authorization` header; if missing or not `Bearer ...`, passes through without authentication.
     - Parses the JWT via `JwtService.extractUsername(token)` using JJWT.
     - Loads `UserDetails` via `CustomUserDetailsService`.
     - Builds a `UsernamePasswordAuthenticationToken` with the user’s authorities (roles) and sets it in `SecurityContextHolder`.
   - Downstream code (`TodoService`) can access the **current user** via `SecurityContextHolder.getContext().getAuthentication()`.

4. **Token refresh**
   - Client calls `POST /api/todos/v1/auth/refresh` with the **refresh token** (plain string body).
   - `AuthService.refreshToken`:
     - Looks up the token in `RefreshTokenRepository`.
     - Validates expiry via `RefreshTokenService.verifyExpiration`.
     - If valid, generates a new **access token** (15 minutes) while reusing the same refresh token.

5. **Logout**
   - Client calls `POST /api/todos/v1/auth/logout` with the refresh token.
   - `AuthService.logout` deletes the refresh token record, effectively revoking it.

### Password encoding & role-based access control

- **Password encoding**:
  - `ApplicationConfig` exposes a `PasswordEncoder` bean using `BCryptPasswordEncoder`.
  - `AuthService.register` encodes the plain password before persisting the `User`.
- **Roles**:
  - `Role` enum: `USER`, `ADMIN`.
  - Mapped to authorities via `CustomUserDetailsService`:
    - `UserDetails` is built with `.roles(user.getRole().name())`.
  - Currently, all non-auth endpoints require authentication but are not further split by role; role-based authorization can be extended in `SecurityConfig` or method-level security as needed.

---

## Database Design (JPA Entities & Relationships)

This project uses **Spring Data JPA** to map entities to relational tables.

### User

- Class: `com.github.sonjaemark.spring_jwt.user.User`
- Table: `users`
- Fields:
  - `id` (PK, `Long`, auto-generated)
  - `username` (`String`)
  - `email` (`String`)
  - `password` (`String`, **BCrypt-hashed**)
  - `role` (`Enum` → `Role.USER`/`Role.ADMIN`, `EnumType.STRING`)
- Relationships:
  - Referenced by `Todo` and `RefreshToken` via `@ManyToOne`.

### Todo

- Class: `com.github.sonjaemark.spring_jwt.todo.Todo`
- Table: `todos`
- Fields:
  - `id` (PK, `Long`, auto-generated)
  - `task` (`String`)
  - `isDone` (`Boolean`, default `false`)
  - `createdAt` (`LocalDateTime`)
  - `user` (`@ManyToOne` → `User`, `@JoinColumn(name = "user_id")`)
- Behavior:
  - `TodoService` ensures a user can only CRUD their own todos by comparing `todo.user.id` with the current authenticated user’s id.

### RefreshToken

- Class: `com.github.sonjaemark.spring_jwt.token.RefreshToken`
- Table: `refresh_tokens`
- Fields:
  - `id` (PK, `Long`, auto-generated)
  - `token` (`String`, random UUID)
  - `expiryDate` (`LocalDateTime`)
  - `user` (`@ManyToOne` → `User`, `@JoinColumn(name = "user_id")`)
- Usage:
  - Managed via `RefreshTokenService` and `RefreshTokenRepository`.
  - Supports refresh-token rotation and expiry checks.

### Repositories

- `UserRepository` (not shown here, but used in services):
  - Typical `JpaRepository<User, Long>` with `Optional<User> findByUsername(String username)`.
- `TodoRepository`:
  - Extends `JpaRepository<Todo, Long>`.
  - Custom query: `List<Todo> findByUser(User user)`.
- `RefreshTokenRepository`:
  - Extends `JpaRepository<RefreshToken, Long>`.
  - Custom queries:
    - `Optional<RefreshToken> findByToken(String token)`
    - `void deleteByUser(User user)`

JPA and Hibernate dialect are inferred from the PostgreSQL driver; no explicit dialect is set because Boot autoconfigures it based on the JDBC URL.

---

## API Endpoints

Base path for the API: `/api/todos/v1`

### Authentication Endpoints (Public)

All under `/api/todos/v1/auth/**` are **public** (no JWT required).

- **POST** `/api/todos/v1/auth/register`
  - Description: Create a new user.
  - Body:
    ```json
    {
      "username": "john",
      "email": "john@example.com",
      "password": "password123",
      "role": "USER"   // optional, defaults to USER
    }
    ```
- **POST** `/api/todos/v1/auth/login`
  - Description: Authenticate and obtain access + refresh tokens.
  - Body:
    ```json
    {
      "username": "john",
      "password": "password123"
    }
    ```
  - Response (`AuthResponse`):
    ```json
    {
      "accessToken": "<jwt-access-token>",
      "refreshToken": "<refresh-token>",
      "userId": 1,
      "username": "john",
      "role": "USER"
    }
    ```
- **POST** `/api/todos/v1/auth/refresh`
  - Description: Exchange a valid refresh token for a new access token.
  - Body: raw string containing the refresh token (e.g. `"f9d3-uuid..."`).
- **POST** `/api/todos/v1/auth/logout`
  - Description: Invalidate a refresh token (logout).
  - Body: raw string containing the refresh token to revoke.

### Todo Endpoints (Secured)

All Todo endpoints **require a valid JWT access token**.  
Include header:

```http
Authorization: Bearer <jwt-access-token>
```

- **POST** `/api/todos/v1/create`
  - Description: Create a new todo for the current user.
  - Body:
    ```json
    {
      "task": "Write documentation"
    }
    ```
- **GET** `/api/todos/v1`
  - Description: Get all todos for the current authenticated user.
- **PUT** `/api/todos/v1/update/{id}`
  - Description: Update the task text for a specific todo (only if owned by the current user).
  - Body:
    ```json
    {
      "task": "Update documentation"
    }
    ```
- **PUT** `/api/todos/v1/done/{id}`
  - Description: Mark a todo as done (only if owned by the current user).
- **DELETE** `/api/todos/v1/delete/{id}`
  - Description: Delete a todo (only if owned by the current user).

If the authenticated user is not the owner of the target todo, `TodoService` throws an `Unauthorized` runtime exception.

---

## How JWT Is Passed and Validated

1. After logging in, store `accessToken` and `refreshToken` on the client (e.g. in memory or secure storage).
2. For **every** call to secured endpoints:
   - Add HTTP header: `Authorization: Bearer <accessToken>`.
3. The `JwtAuthenticationFilter`:
   - Reads the header, extracts the token string.
   - Validates the signature and expiration via `JwtService`:
     - `extractUsername` uses the same signing key to verify integrity.
   - If valid, sets an authenticated `SecurityContext`, which is then used by services.
4. When the access token expires (after ~15 minutes):
   - Call `POST /api/todos/v1/auth/refresh` with the refresh token to obtain a new access token.

---

## How to Run Tests

The project includes a basic Spring Boot context test in `SpringJwtApplicationTests`:

```bash
mvn test
```

This will:

- Start an application context.
- Validate that the wiring and configuration are correct.

You can add additional unit and integration tests under `src/test/java` for services, repositories, and the security layer as needed.
