# HoLink

HoLink is a simplified link-in-bio fullstack prototype built for local evaluation. It includes account registration and login, a JWT-protected dashboard for profile and link management, a public profile page by username, click tracking for public link visits, and a simple analytics view that shows total clicks per link.

The project is intentionally scoped for a take-home style review: it uses H2 as the default database, keeps authentication simple for local prototype use, and aims for a clean backend/frontend structure without overengineering.

## Project Overview

HoLink supports four main routes:

- `/login` for signing in
- `/register` for creating a new account
- `/dashboard` for managing a single public profile, links, and basic analytics
- `/u/:username` for a visitor-facing public profile page that remains accessible without login

Core implemented behavior:

- Register a new user account
- Login with JWT authentication
- Logout and clear local auth state
- Protect dashboard APIs and the `/dashboard` route
- Create and update one profile per user
- Normalize and enforce unique usernames
- Create, update, delete, and reorder links using a numeric `position`
- Toggle links active/inactive
- Copy the current user's public profile URL from the dashboard
- Show public profiles by username
- Track clicks before redirecting visitors to the destination URL
- Show total click counts per link in the dashboard

## Tech Stack

### Backend

- Java 17
- Spring Boot 3
- Maven
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- BCrypt password hashing
- Lombok

### Frontend

- Vue 3
- Vite
- Composition API
- Vue Router
- Axios

### Authentication

- JWT-based authentication for protected APIs
- Stateless session handling
- Frontend token storage in `localStorage` for prototype simplicity

### Database

- H2 in-memory database by default

## Features Implemented

- Create account
- Login
- Logout
- JWT-protected dashboard APIs
- Frontend route guard for `/dashboard`
- Password hashing with BCrypt
- Profile create/update
- Username normalization and uniqueness
- Link create/update/delete
- Active/inactive links
- Link ordering using `position`
- Link position uniqueness validation per profile
- Public profile page by username
- Click tracking
- Basic analytics: total clicks per link
- Backend validation for username, display name, bio, avatar URL, title, and link URL
- Unsafe URL rejection for dangerous schemes such as `javascript:` and `data:`
- Ownership checks for profile, link, and analytics access
- Avatar fallback on the frontend when the public avatar image is missing or fails to load
- Share Profile action in the dashboard that uses the signed-in user's current username
- Responsive UI refresh for login, register, dashboard, and public profile pages

## How To Run Backend

Requirements:

- Java 17
- Maven

Run:

```bash
cd backend
mvn spring-boot:run
```

Default backend URL:

```text
http://localhost:8080
```

Health check:

```text
GET http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "OK"
}
```

Seeded users for local testing:

- `kevin@example.com` / `password123`
- `sarah@example.com` / `password123`

H2 console:

```text
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:holinkdb;DB_CLOSE_DELAY=-1
Username: sa
Password: (empty)
```

Note: H2 is configured as an in-memory database, so data will reset when the backend application restarts.

## How To Run Frontend

Requirements:

- Node.js
- npm

Run:

```bash
cd frontend
npm install
npm run dev
```

Default frontend URL:

```text
http://localhost:5173
```

Important routes:

- `http://localhost:5173/login`
- `http://localhost:5173/register`
- `http://localhost:5173/dashboard`
- `http://localhost:5173/u/:username`

Examples:

- `http://localhost:5173/login`
- `http://localhost:5173/register`
- `http://localhost:5173/dashboard`
- `http://localhost:5173/u/kevincreator`

## Authentication

This prototype now uses simple JWT authentication for local evaluation.

How it works:

- Users can register with `POST /api/auth/register`
- Users can login with `POST /api/auth/login`
- The backend returns a JWT token plus basic user data
- The frontend stores the token in `localStorage` under `holink_auth_token`
- The frontend stores user info in `localStorage` under `holink_auth_user`
- The frontend sends `Authorization: Bearer <token>` automatically for protected API calls
- The `/dashboard` route requires login
- Public profile and public click tracking remain accessible without login
- Passwords are stored as BCrypt hashes, not plain text

Prototype note:

- This is a simple JWT implementation for local prototype use
- It is not presented as a production-complete authentication system
- There is no refresh token flow, email verification, or password reset yet

## Public vs Protected Endpoints

### Public Endpoints

- `GET /api/health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/public/{username}`
- `POST /api/links/{linkId}/click`

### Protected Endpoints

- `GET /api/auth/me`
- `POST /api/profiles`
- `PUT /api/profiles/{profileId}`
- `GET /api/me/profile`
- `POST /api/links`
- `PUT /api/links/{linkId}`
- `DELETE /api/links/{linkId}`
- `GET /api/analytics/links`

## Architecture Overview

### Backend

The backend follows a layered Spring Boot structure:

- `controller/`: REST endpoints
- `service/`: business logic for auth, profiles, links, clicks, and analytics
- `repository/`: JPA access for entities
- `validation/`: reusable validation and normalization helpers
- `exception/`: custom exceptions and a global JSON error handler
- `security/`: JWT creation, JWT filter, current-user lookup, and auth entry point
- `config/`: CORS, security, and seed data

### Frontend

The frontend uses a small Vue structure:

- `pages/`: route-level pages (`LoginPage.vue`, `RegisterPage.vue`, `DashboardPage.vue`, `PublicProfilePage.vue`)
- `components/`: reusable UI blocks (`ProfileForm`, `LinkEditor`, `LinkList`, `AnalyticsSummary`, etc.)
- `api/`: centralized Axios-based API helpers
- `router/`: Vue Router setup with auth guard
- `utils/`: token storage helpers and auth route helpers
- `/dashboard`: one protected page with in-page sections for profile, links, and analytics

### Data Flow

Typical flow:

1. Register or login to receive a JWT token
2. Frontend stores the token and user info in `localStorage`
3. Dashboard fetches the current user profile from `GET /api/me/profile`
4. Dashboard submits profile and link changes to protected backend APIs
5. Public page fetches active profile/link data from `GET /api/public/{username}`
6. Public link click posts to `POST /api/links/{linkId}/click`
7. Dashboard analytics reads totals from `GET /api/analytics/links`

## Database Design

Entities:

- `User`
- `Profile`
- `Link`
- `ClickEvent`

Relationships:

```text
User 1 - 1 Profile
Profile 1 - many Links
Link 1 - many ClickEvents
```

Important constraints and rules:

- `User.email` is unique
- `Profile.username` is unique
- A `Profile` belongs to exactly one `User`
- A `Link` belongs to exactly one `Profile`
- A `ClickEvent` belongs to exactly one `Link`
- `Profile`, `Link`, and `ClickEvent` use generated UUID string IDs

Entity summary:

- `User`: account identity with `name`, `email`, and `passwordHash`
- `Profile`: public-facing identity (`username`, `displayName`, `bio`, `avatarUrl`)
- `Link`: ordered public destinations with `isActive` and `position`
- `ClickEvent`: stored click record with referrer, user agent, and optional UTM params

## API Documentation

### GET `/api/health`

Purpose:

- Quick backend health check

Response example:

```json
{
  "status": "OK"
}
```

Important error cases:

- None expected in normal local startup

### POST `/api/auth/register`

Purpose:

- Create a new user account and immediately return a JWT token

Request body:

```json
{
  "name": "Muhammad Fadlil",
  "email": "fadlil@example.com",
  "password": "password123"
}
```

Response example:

```json
{
  "token": "jwt-token-here",
  "user": {
    "id": "user-id",
    "name": "Muhammad Fadlil",
    "email": "fadlil@example.com"
  }
}
```

Important error cases:

- `400` invalid request body, invalid email format, or short password
- `409` email already registered

### POST `/api/auth/login`

Purpose:

- Authenticate an existing user and return a JWT token

Request body:

```json
{
  "email": "kevin@example.com",
  "password": "password123"
}
```

Response example:

```json
{
  "token": "jwt-token-here",
  "user": {
    "id": "user_001",
    "name": "Kevin",
    "email": "kevin@example.com"
  }
}
```

Important error cases:

- `400` invalid request body
- `401` invalid email or password

### GET `/api/auth/me`

Purpose:

- Validate the current JWT token and return the authenticated user

Headers:

```http
Authorization: Bearer <token>
```

Response example:

```json
{
  "id": "user_001",
  "name": "Kevin",
  "email": "kevin@example.com"
}
```

Important error cases:

- `401` missing token
- `401` invalid token
- `401` expired token

### POST `/api/profiles`

Purpose:

- Create a profile for the current authenticated user

Request body:

```json
{
  "username": "Kevin Creator",
  "displayName": "Kevin Kennedy Kie",
  "bio": "Tech, sports, and creator page",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

Response example:

```json
{
  "id": "profile-id",
  "username": "kevincreator",
  "displayName": "Kevin Kennedy Kie",
  "bio": "Tech, sports, and creator page",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

Important error cases:

- `400` invalid username, display name, bio, or avatar URL
- `401` missing or invalid JWT
- `409` username already exists
- `409` current user already has a profile

### PUT `/api/profiles/{profileId}`

Purpose:

- Update the current authenticated user's existing profile

Request body:

```json
{
  "username": "kevin",
  "displayName": "Kevin Kie",
  "bio": "Updated bio",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

Response example:

```json
{
  "id": "profile-id",
  "username": "kevin",
  "displayName": "Kevin Kie",
  "bio": "Updated bio",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

Important error cases:

- `400` invalid username or invalid text/URL fields
- `401` missing or invalid JWT
- `403` current user does not own the profile
- `404` profile not found
- `409` username already belongs to another profile

### GET `/api/me/profile`

Purpose:

- Get the current authenticated user's dashboard profile and links

Response example:

```json
{
  "profile": {
    "id": "profile-id",
    "username": "kevin",
    "displayName": "Kevin Kie",
    "bio": "Updated bio",
    "avatarUrl": "https://example.com/new-avatar.jpg"
  },
  "links": [
    {
      "id": "link-id",
      "profileId": "profile-id",
      "title": "Instagram",
      "url": "https://instagram.com/example",
      "isActive": true,
      "position": 1
    }
  ]
}
```

Important error cases:

- `401` missing or invalid JWT
- `404` current user has no profile yet

### GET `/api/public/{username}`

Purpose:

- Get a public profile by username with active links only

Response example:

```json
{
  "profile": {
    "username": "kevin",
    "displayName": "Kevin Kie",
    "bio": "Updated bio",
    "avatarUrl": "https://example.com/new-avatar.jpg"
  },
  "links": [
    {
      "id": "link-id",
      "profileId": "profile-id",
      "title": "Instagram",
      "url": "https://instagram.com/example",
      "isActive": true,
      "position": 1
    }
  ]
}
```

Important error cases:

- `404` username not found

### POST `/api/links`

Purpose:

- Create a new link under the current authenticated user's profile

Request body:

```json
{
  "profileId": "profile-id",
  "title": "Instagram",
  "url": "https://instagram.com/example",
  "isActive": true,
  "position": 1
}
```

Response example:

```json
{
  "id": "link-id",
  "profileId": "profile-id",
  "title": "Instagram",
  "url": "https://instagram.com/example",
  "isActive": true,
  "position": 1
}
```

Important error cases:

- `400` blank title, invalid URL, dangerous URL scheme
- `401` missing or invalid JWT
- `403` current user does not own the profile
- `404` profile not found

### PUT `/api/links/{linkId}`

Purpose:

- Update an existing link owned by the current authenticated user

Request body:

```json
{
  "title": "My Instagram",
  "url": "https://instagram.com/example",
  "isActive": true,
  "position": 1
}
```

Response example:

```json
{
  "id": "link-id",
  "profileId": "profile-id",
  "title": "My Instagram",
  "url": "https://instagram.com/example",
  "isActive": true,
  "position": 1
}
```

Important error cases:

- `400` blank title, invalid URL, dangerous URL scheme
- `401` missing or invalid JWT
- `403` current user does not own the link's profile
- `404` link not found

### DELETE `/api/links/{linkId}`

Purpose:

- Delete an existing link owned by the current authenticated user

Request body:

- None

Response example:

```text
204 No Content
```

Important error cases:

- `401` missing or invalid JWT
- `403` current user does not own the link's profile
- `404` link not found

### POST `/api/links/{linkId}/click`

Purpose:

- Record a public click and return the redirect URL

Request body:

```json
{
  "utmSource": "instagram",
  "utmMedium": "social",
  "utmCampaign": "creator_launch"
}
```

Response example:

```json
{
  "redirectUrl": "https://instagram.com/example"
}
```

Important error cases:

- `404` link not found
- `400` inactive link click rejected

Notes:

- This endpoint is intentionally public so visitors can open shared links without login
- The backend also reads `Referer` and `User-Agent` headers when available
- If persistence of the click record fails but the destination URL is already known, the backend is designed to still return the redirect URL

### GET `/api/analytics/links`

Purpose:

- Return total clicks per link for the current authenticated user

Response example:

```json
{
  "items": [
    {
      "linkId": "link-id",
      "title": "Instagram",
      "url": "https://instagram.com/example",
      "isActive": true,
      "totalClicks": 10
    }
  ]
}
```

Important error cases:

- `401` missing or invalid JWT
- `404` current user profile not found
- `403` analytics access blocked by ownership rules

## Validation Rules

### Username

- Required
- Trimmed
- Lowercased
- Spaces removed
- Allowed characters: `a-z`, `0-9`, `_`, `-`
- Length must be between `3` and `30`
- Must be unique

Example:

```text
"Kevin Creator" -> "kevincreator"
```

### Display Name

- Required
- Max `120` characters
- Obvious script-like input such as `<script` is rejected

### Bio

- Optional
- Max `500` characters
- Obvious script-like input such as `<script` is rejected

### Avatar URL

- Optional
- If present, must be a valid URL
- Only `http` and `https` are allowed
- Max `2048` characters

### Link Title

- Required
- Max `120` characters
- Obvious script-like input such as `<script` is rejected

### Link URL

- Required
- Must be a valid URI
- Must include protocol
- Only `http` and `https` are allowed
- Max `2048` characters
- Dangerous schemes are rejected

Rejected examples:

```text
javascript:alert(1)
data:text/html,<script>alert(1)</script>
file:///etc/passwd
ftp://example.com/file
example.com/no-protocol
```

### Link Position

- Required
- Must be an integer value of at least `1`
- Must be unique within the same profile
- Duplicate positions for the same profile are rejected

## Security Notes

### Password Handling

- Passwords are stored as BCrypt hashes
- Plain-text passwords are never persisted

### JWT Protection

- Protected APIs require a valid Bearer token
- The backend uses a stateless security setup
- Ownership checks are still enforced server-side after authentication

### Frontend Token Storage Trade-off

- The frontend stores the JWT in `localStorage` for prototype simplicity
- This is convenient for local evaluation, but not the most secure production approach
- Production systems should prefer more defensive approaches such as secure HttpOnly cookies where appropriate

### XSS Prevention

- Frontend does not use `v-html` for user-generated content
- Public profile, dashboard text, and link titles are rendered as plain text
- Backend rejects obvious script-like input in display name, bio, and title

### Dangerous URL Rejection

- Backend validates stored link URLs
- Only `http` and `https` are allowed
- Unsafe redirect schemes such as `javascript:` and `data:` are rejected before persistence

### Public Click Endpoint

- `POST /api/links/{linkId}/click` remains public intentionally
- Visitors should be able to open shared links without signing in

### Privacy and Logging Considerations

- Click tracking stores referrer, user agent, and UTM fields when available
- This is acceptable for a prototype, but production systems should define retention and privacy handling clearly

## Click Tracking Design

Current behavior:

- Public page calls `POST /api/links/{linkId}/click`
- Backend looks up the link and validates it
- Backend records the click before returning `redirectUrl`
- Stored fields include:
  - `linkId`
  - `profileUsername`
  - `createdAt`
  - `referrer`
  - `userAgent`
  - `utmSource`
  - `utmMedium`
  - `utmCampaign`

Failure behavior:

- If click persistence fails but the link exists and the URL is already known, redirect should still continue

Production improvement:

- Move click persistence to async event processing or a queue so redirect flow is even more resilient under load

## Analytics Notes

- Dashboard analytics currently shows total clicks per link
- Inactive links may still appear in analytics if the backend returns them
- Duplicate clicks and spam clicks can inflate totals

Possible future improvements:

- Unique click counting
- Daily aggregation
- Bot filtering
- Trend/history reporting

## Manual Testing Checklist

- [ ] Start backend successfully and verify `GET /api/health`
- [ ] Open H2 console and confirm seeded users exist
- [ ] Register a new account
- [ ] Login with seeded user `kevin@example.com / password123`
- [ ] Login with a newly registered user
- [ ] Access `/dashboard` without a token and verify redirect to `/login`
- [ ] Set an invalid token and verify session is cleared
- [ ] Logout and verify token plus stored user are removed
- [ ] Create a profile from `/dashboard`
- [ ] Update the profile from `/dashboard`
- [ ] Use Share Profile and verify it copies the current profile URL, or shows an error if no profile exists yet
- [ ] Verify username normalization and uniqueness
- [ ] Add a link
- [ ] Edit a link
- [ ] Delete a link
- [ ] Toggle active/inactive status
- [ ] Try creating or updating two links with the same `position` in one profile and verify the request is rejected
- [ ] Confirm dangerous URLs are rejected
- [ ] Open `/u/:username` without login and verify only active links are shown
- [ ] Verify the public page shows fallback initials if the avatar image is missing or fails to load
- [ ] Click a public link and verify redirect occurs
- [ ] Verify click rows appear in `CLICK_EVENTS`
- [ ] Verify analytics count updates on `/dashboard`
- [ ] Call a protected API without a token and verify `401`
- [ ] Verify user A cannot edit or delete user B resources

## Known Limitations

- Auth is a simple JWT prototype, not a production-complete auth platform
- No refresh token flow yet
- No email verification
- No password reset
- Token is stored in `localStorage` for prototype simplicity
- H2 in-memory data resets when the backend restarts
- No production deployment setup yet
- No advanced anti-spam or anti-bot protection for click tracking
- No advanced SEO or Open Graph image generation
- No drag-and-drop reorder UI; ordering uses the numeric `position` field
- Dashboard "Dashboard / Analytics / Settings" navigation is currently a single-page section switcher, not separate routes
- No advanced automated test coverage has been added yet

## Future Improvements

- Refresh token rotation
- HttpOnly secure cookies
- Email verification
- Password reset
- Account settings
- Production secret management
- Optional PostgreSQL support with Docker
- Flyway migrations
- Rate limiting
- Unique click tracking
- Daily analytics aggregation
- Bot filtering
- Open Graph metadata and image generation
- Avatar upload
- Automated tests

## Trade-offs

- H2 was chosen for quick local review and minimal setup
- JWT auth was implemented in a simple form so the evaluator can test real login flows without adding too much product complexity
- Analytics is intentionally simple so the core product flow can be reviewed quickly
- The dashboard uses inline create/edit flows for faster UX and simpler implementation
- `localStorage` token storage is acceptable for a local prototype but not ideal for hardened production security

## Suggested Review Flow

For the fastest evaluation path:

1. Start the backend and verify `/api/health`
2. Start the frontend and open `/register` or `/login`
3. Login with `kevin@example.com / password123`
4. Confirm `/dashboard` is accessible only after authentication
5. Create or update a profile
6. Add a few links and toggle one inactive
7. Open `/u/:username` and confirm only active links render without login
8. Click the public links to create click events
9. Return to `/dashboard` and verify analytics totals
10. Optionally register a second user and confirm ownership checks still block access to another user's resources
