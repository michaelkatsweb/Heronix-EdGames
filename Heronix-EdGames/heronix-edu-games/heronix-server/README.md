# Heronix Educational Games Server

REST API server for the Heronix Educational Games Platform with FERPA-compliant device management, score synchronization, and teacher controls.

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.9+

### Running the Server

```bash
cd heronix-server
mvn spring-boot:run
```

Server starts on **http://localhost:8080**

### Default Test Accounts

**Teacher Account:**
- Username: `teacher1`
- Password: `teacher123`

**Admin Account:**
- Username: `admin1`
- Password: `admin123`

**Registration Code:** `TEST1234` (valid for 10 devices)

**Test Students:** STU001 (Alice J.), STU002 (Bob S.), STU003 (Charlie M.)

## API Endpoints

### Public Endpoints (No Authentication)

#### Health Check
```
GET /api/ping
```
Returns server status and timestamp.

#### Device Registration
```
POST /api/device/register
Content-Type: application/json

{
  "deviceId": "unique-device-uuid",
  "studentId": "STU001",
  "registrationCode": "TEST1234",
  "deviceName": "Alice's iPad",
  "deviceType": "TABLET",
  "operatingSystem": "iOS",
  "osVersion": "16.0",
  "appVersion": "1.0.0"
}
```
Registers a device with PENDING status.

#### Device Authentication
```
POST /api/auth/device
Content-Type: application/json

{
  "deviceId": "unique-device-uuid",
  "studentId": "STU001"
}
```
Returns JWT token for approved devices.

**Response:**
```json
{
  "token": "eyJhbGci...",
  "tokenType": "Bearer",
  "expiresAt": "2026-04-09T20:00:00",
  "deviceId": "unique-device-uuid",
  "studentId": "STU001"
}
```

#### Teacher Login
```
POST /api/teacher/login
Content-Type: application/json

{
  "username": "teacher1",
  "password": "teacher123"
}
```
Returns JWT token for teachers.

### Protected Endpoints (Require JWT)

#### Sync Operations (Device JWT)

**Get Last Sync Time:**
```
GET /api/sync/last-sync?deviceId=unique-device-uuid
Authorization: Bearer {device-jwt-token}
```

**Upload Scores:**
```
POST /api/sync/upload
Authorization: Bearer {device-jwt-token}
Content-Type: application/json

{
  "scores": [
    {
      "scoreId": "score-uuid-1",
      "studentId": "STU001",
      "gameId": "math-sprint",
      "score": 85,
      "maxScore": 100,
      "timeSeconds": 120,
      "correctAnswers": 17,
      "incorrectAnswers": 3,
      "completed": true,
      "difficultyLevel": "MEDIUM",
      "playedAt": "2026-01-09T15:30:00",
      "deviceId": "unique-device-uuid"
    }
  ]
}
```

**Mark Sync Complete:**
```
POST /api/sync/complete
Authorization: Bearer {device-jwt-token}
```

#### Teacher Operations (Teacher JWT)

**Get Pending Devices:**
```
GET /api/teacher/devices/pending
Authorization: Bearer {teacher-jwt-token}
```

**Approve Device:**
```
POST /api/teacher/devices/approve
Authorization: Bearer {teacher-jwt-token}
Content-Type: application/json

{
  "deviceId": "unique-device-uuid"
}
```

**Reject Device:**
```
POST /api/teacher/devices/reject
Authorization: Bearer {teacher-jwt-token}
Content-Type: application/json

{
  "deviceId": "unique-device-uuid",
  "reason": "Device not recognized"
}
```

**Revoke Device Access:**
```
POST /api/teacher/devices/revoke
Authorization: Bearer {teacher-jwt-token}
Content-Type: application/json

{
  "deviceId": "unique-device-uuid",
  "reason": "Device lost or stolen"
}
```

**Generate Registration Code:**
```
POST /api/teacher/codes/generate
Authorization: Bearer {teacher-jwt-token}
Content-Type: application/json

{
  "classId": 1,
  "maxUses": 30,
  "validDays": 30
}
```

**Get All Registration Codes:**
```
GET /api/teacher/codes
Authorization: Bearer {teacher-jwt-token}
```

**Deactivate Registration Code:**
```
DELETE /api/teacher/codes/{code}
Authorization: Bearer {teacher-jwt-token}
```

**Get Student's Devices:**
```
GET /api/teacher/students/{studentId}/devices
Authorization: Bearer {teacher-jwt-token}
```

## Database Access

### H2 Console

Access at: **http://localhost:8080/h2-console**

**Connection Settings:**
- JDBC URL: `jdbc:h2:file:./data/heronix`
- Username: `sa`
- Password: (leave blank)

### Database Tables

- `users` - Teachers and administrators
- `students` - Student records (FERPA-compliant)
- `devices` - Registered devices
- `game_scores` - Educational performance data
- `classes` - Teacher classes
- `registration_codes` - Device registration codes
- `audit_log` - FERPA compliance audit trail

## API Documentation

**Swagger UI:** http://localhost:8080/swagger-ui.html

**OpenAPI Spec:** http://localhost:8080/v3/api-docs

## Security

### JWT Tokens

- **Expiration:** 90 days
- **Algorithm:** HS256
- **Secret:** Configured in `application.yml` (change for production!)

### Password Hashing

- **Algorithm:** BCrypt
- **Strength:** 10 rounds

### FERPA Compliance

- Minimal PII collection (first name + last initial only)
- Consent tracking required
- All data access logged in `audit_log` table
- Device approval workflow

## Configuration

### Profiles

- **Default:** Development settings
- **dev:** Development with verbose logging
- **prod:** Production with security hardening

### Key Settings (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/heronix
  h2:
    console:
      enabled: true  # Disable in production!

jwt:
  secret: ${JWT_SECRET}  # Use environment variable in production
  expiration: 7776000000  # 90 days

heronix:
  sync:
    batch-size: 100
    max-retries: 3
  audit:
    enabled: true
```

## Development

### Building

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Packaging

```bash
mvn clean package
```

Creates executable JAR: `target/heronix-server-1.0.0-SNAPSHOT.jar`

### Running Standalone

```bash
java -jar target/heronix-server-1.0.0-SNAPSHOT.jar
```

## Troubleshooting

### Port Already in Use

Change port in `application.yml`:
```yaml
server:
  port: 8081
```

### Database Locked

Stop all running instances and delete `./data/heronix.lock.db`

### Authentication Failures

Check JWT token expiration and ensure device is APPROVED status.

## Production Deployment

1. **Set JWT Secret:**
   ```bash
   export JWT_SECRET=your-secure-256-bit-secret
   ```

2. **Disable H2 Console:**
   Edit `application-prod.yml`:
   ```yaml
   spring:
     h2:
       console:
         enabled: false
   ```

3. **Run with Production Profile:**
   ```bash
   java -jar heronix-server.jar --spring.profiles.active=prod
   ```

## Support

For issues, see the main project documentation or check the audit logs in the H2 console.
