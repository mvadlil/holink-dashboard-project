# HoLink

HoLink is a simplified link-in-bio fullstack prototype built for local evaluation. It includes a dashboard for profile and link management, a public profile page by username, click tracking for public link visits, and a simple analytics view that shows total clicks per link.

The project is intentionally scoped for a take-home style review: it uses mocked authentication, H2 as the default database, and a small but realistic backend/frontend structure.

## Project Overview

HoLink supports two main experiences:

- `/dashboard` for managing a single public profile, links, and basic analytics
- `/u/:username` for a visitor-facing public profile page that shows active links only

Core implemented behavior:

- Create and update one profile per user
- Normalize and enforce unique usernames
- Create, update, delete, and reorder links using a numeric `position`
- Toggle links active/inactive
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
- Bean Validation
- Lombok

### Frontend

- Vue 3
- Vite
- Composition API
- Vue Router
- Axios

### Database

- H2 in-memory database by default

### Mock Auth

- Frontend sends `X-User-Id`
- Backend reads `X-User-Id` and defaults to `user_001` if missing

## Features Implemented

- Profile create/update
- Username normalization and uniqueness
- Link create/update/delete
- Active/inactive links
- Link ordering using `position`
- Public profile page by username
- Click tracking
- Basic analytics: total clicks per link
- Backend validation for username, display name, bio, avatar URL, title, and link URL
- Unsafe URL rejection for dangerous schemes such as `javascript:` and `data:`
- Ownership checks for profile, link, and analytics access
- Avatar fallback on the frontend when the public avatar image is missing or fails to load

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

- `http://localhost:5173/dashboard`
- `http://localhost:5173/u/:username`

Examples:

- `http://localhost:5173/dashboard`
- `http://localhost:5173/u/kevincreator`

## Mock Authentication

This prototype does not implement real login or registration. Instead:

- The frontend uses the header `X-User-Id`
- If the header is missing, the backend defaults to `user_001`
- Seeded users are:
  - `user_001` / Kevin / `kevin@example.com`
  - `user_002` / Sarah / `sarah@example.com`

This keeps the project focused on profile ownership, link ownership, public routing, click tracking, and validation rather than auth flows.

Even with mocked auth, ownership logic is still enforced:

- A user cannot edit another user's profile
- A user cannot create links under another user's profile
- A user cannot edit/delete another user's links
- A user cannot view another user's private analytics

## Architecture Overview

### Backend

The backend follows a layered Spring Boot structure:

- `controller/`: REST endpoints
- `service/`: business logic for profiles, links, clicks, and analytics
- `repository/`: JPA access for entities
- `validation/`: reusable validation and normalization helpers
- `exception/`: custom exceptions and a global JSON error handler
- `security/`: mocked current-user lookup via `X-User-Id`
- `config/`: CORS and seed data

### Frontend

The frontend uses a small Vue structure:

- `pages/`: route-level pages (`DashboardPage.vue`, `PublicProfilePage.vue`)
- `components/`: reusable UI blocks (`ProfileForm`, `LinkEditor`, `LinkList`, `AnalyticsSummary`, etc.)
- `api/`: centralized Axios-based API helpers
- `router/`: Vue Router setup

### Data Flow

Typical flow:

1. Dashboard fetches current user profile from `GET /api/me/profile`
2. Dashboard submits profile and link changes to backend APIs
3. Public page fetches active profile/link data from `GET /api/public/{username}`
4. Public link click posts to `POST /api/links/{linkId}/click`
5. Dashboard analytics reads totals from `GET /api/analytics/links`

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

- `Profile.username` is unique
- A `Profile` belongs to exactly one `User`
- A `Link` belongs to exactly one `Profile`
- A `ClickEvent` belongs to exactly one `Link`
- `Profile`, `Link`, and `ClickEvent` use generated UUID string IDs

Entity summary:

- `User`: seeded mock user identity
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

### POST `/api/profiles`

Purpose:

- Create a profile for the current mocked user

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
- `404` current mocked user does not exist
- `409` username already exists
- `409` current user already has a profile

### PUT `/api/profiles/{profileId}`

Purpose:

- Update the current user's existing profile

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
- `403` current user does not own the profile
- `404` profile not found
- `409` username already belongs to another profile

### GET `/api/me/profile`

Purpose:

- Get the current user's dashboard profile and links

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

- Create a new link under the current user's profile

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
- `403` current user does not own the profile
- `404` profile not found

### PUT `/api/links/{linkId}`

Purpose:

- Update an existing link owned by the current user

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
- `403` current user does not own the link's profile
- `404` link not found

### DELETE `/api/links/{linkId}`

Purpose:

- Delete an existing link owned by the current user

Request body:

- None

Response example:

```text
204 No Content
```

Important error cases:

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

- The backend also reads `Referer` and `User-Agent` headers when available
- If persistence of the click record fails but the destination URL is already known, the backend is designed to still return the redirect URL

### GET `/api/analytics/links`

Purpose:

- Return total clicks per link for the current user

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

## Security Notes

### XSS Prevention

- Frontend does not use `v-html` for user-generated content
- Public profile, dashboard text, and link titles are rendered as plain text
- Backend rejects obvious script-like input in display name, bio, and title

### Dangerous URL Rejection

- Backend validates stored link URLs
- Only `http` and `https` are allowed
- Unsafe redirect schemes such as `javascript:` and `data:` are rejected before persistence

### Ownership Checks

- Profile updates are restricted to the owner
- Link create/update/delete actions are restricted to the owner of the parent profile
- Analytics are returned only for the current mocked user

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
- [ ] Create a profile from `/dashboard`
- [ ] Update the profile from `/dashboard`
- [ ] Verify username normalization and uniqueness
- [ ] Add a link
- [ ] Edit a link
- [ ] Delete a link
- [ ] Toggle active/inactive status
- [ ] Confirm dangerous URLs are rejected
- [ ] Open `/u/:username` and verify only active links are shown
- [ ] Click a public link and verify redirect occurs
- [ ] Verify click rows appear in `CLICK_EVENTS`
- [ ] Verify analytics count updates on `/dashboard`
- [ ] Switch mocked user to `user_002` and verify ownership checks block access to `user_001` resources

## Known Limitations

- Mock auth only; there is no real login or registration
- H2 in-memory data resets when the backend restarts
- No production deployment setup yet
- No advanced anti-spam or anti-bot protection for click tracking
- No advanced SEO or Open Graph image generation
- No drag-and-drop reorder UI; ordering uses the numeric `position` field
- No advanced automated test coverage has been added yet

## Future Improvements

- JWT or session-based authentication
- Optional PostgreSQL support with Docker
- Flyway migrations
- Real deployment setup
- Rate limiting
- Unique click tracking
- Daily analytics aggregation
- Bot filtering
- Open Graph metadata and image generation
- Avatar upload
- Automated tests

## Trade-offs

- H2 was chosen for quick local review and minimal setup
- Mock auth was chosen to keep the scope focused on ownership logic rather than auth flows
- Analytics is intentionally simple so the core product flow can be reviewed quickly
- The dashboard uses inline create/edit flows for faster UX and simpler implementation

## Suggested Review Flow

For the fastest evaluation path:

1. Start the backend and verify `/api/health`
2. Start the frontend and open `/dashboard`
3. Create or update a profile
4. Add a few links and toggle one inactive
5. Open `/u/:username` and confirm only active links render
6. Click the public links to create click events
7. Return to `/dashboard` and verify analytics totals
8. Optionally test ownership by changing the mocked user header to `user_002`
