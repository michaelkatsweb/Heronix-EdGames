# Heronix Platform Architecture

## Overview

Heronix uses a **hybrid offline-first architecture** with:
- Local H2 databases on desktop clients for offline resilience
- Multiple backend servers for different domains (Games, SIS, etc.)
- PostgreSQL for enterprise production scalability

## Multi-Server Architecture Diagram

```
                        ┌─────────────────────────────────────────┐
                        │           TEACHER PORTAL                │
                        │         (JavaFX Desktop Hub)            │
                        │                                         │
                        │  ┌───────────────────────────────────┐  │
                        │  │         H2 Embedded DB            │  │
                        │  │  - Grades, Attendance, Students   │  │
                        │  │  - Game sessions, Questions       │  │
                        │  │  - Cached data from all servers   │  │
                        │  └───────────────┬───────────────────┘  │
                        │                  │                      │
                        │         AutoSyncService (15s)           │
                        │    ┌─────────────┼─────────────┐        │
                        └────┼─────────────┼─────────────┼────────┘
                             │             │             │
           ┌─────────────────┘             │             └─────────────────┐
           │                               │                               │
           ▼                               ▼                               ▼
┌─────────────────────┐       ┌─────────────────────┐       ┌─────────────────────┐
│   SIS Server        │       │   EdGames Server    │       │   Future Servers    │
│ (EduScheduler-Pro)  │       │  (heronix-server)   │       │                     │
│                     │       │                     │       │  - Assessment       │
│  - Students         │       │  - Game Sessions    │       │  - Communication    │
│  - Classes          │       │  - Questions        │       │  - Analytics        │
│  - Schedules        │       │  - Scores           │       │  - Integrations     │
│  - Grades           │       │  - Devices          │       │                     │
│  - Attendance       │       │  - Multiplayer      │       │                     │
│                     │       │                     │       │                     │
│  ┌───────────────┐  │       │  ┌───────────────┐  │       │  ┌───────────────┐  │
│  │  PostgreSQL   │  │       │  │  PostgreSQL   │  │       │  │  PostgreSQL   │  │
│  └───────────────┘  │       │  └───────────────┘  │       │  └───────────────┘  │
└─────────────────────┘       └──────────┬──────────┘       └─────────────────────┘
                                         │
                                         │
                              ┌──────────┴──────────┐
                              │                     │
                              ▼                     ▼
               ┌─────────────────────┐  ┌─────────────────────┐
               │   Student Client    │  │   Student Client    │
               │  (JavaFX Desktop)   │  │  (JavaFX Desktop)   │
               │                     │  │                     │
               │  ┌───────────────┐  │  │  ┌───────────────┐  │
               │  │ H2 Embedded   │  │  │  │ H2 Embedded   │  │
               │  │ - Scores      │  │  │  │ - Scores      │  │
               │  │ - Games       │  │  │  │ - Games       │  │
               │  │ - Device      │  │  │  │ - Device      │  │
               │  └───────────────┘  │  │  └───────────────┘  │
               └─────────────────────┘  └─────────────────────┘
```

## Teacher Portal - Multi-Server Hub

The Teacher Portal connects to **multiple backend servers**, acting as a unified interface:

### Current Server Connections

| Server | API Client | Purpose | Data Synced |
|--------|------------|---------|-------------|
| **SIS Server** | `AdminApiClient.java` | Student Information System | Students, Grades, Attendance, Schedules |
| **EdGames Server** | `EdGamesApiClient.java` | Educational Games Platform | Game Sessions, Questions, Device Approvals, Scores |

### API Clients in Teacher Portal

```
Heronix-Teacher/src/main/java/com/heronix/teacher/service/
├── AdminApiClient.java      # SIS Server (EduScheduler-Pro)
├── EdGamesApiClient.java    # Game Server (heronix-server)
├── AutoSyncService.java     # Background sync to SIS
├── SyncManagerService.java  # Bidirectional sync orchestration
└── NetworkMonitorService.java # Connectivity detection
```

### Future Server Integrations (Planned)

| Server | Purpose | Potential Data |
|--------|---------|----------------|
| **Assessment Server** | Standardized testing | Test scores, benchmarks |
| **Communication Hub** | Parent/Student messaging | Messages, announcements |
| **Analytics Server** | Advanced reporting | Dashboards, predictions |
| **LMS Integration** | Learning Management | Course content, assignments |
| **SSO Provider** | Single Sign-On | Authentication federation |

## Database Responsibilities

### Student Client - H2 (Embedded)

**Location:** `~/.heronix/client.db`

**Stores:**
| Table | Purpose | Sync Direction |
|-------|---------|----------------|
| `local_device` | Device registration, JWT token cache | Bidirectional |
| `local_game_scores` | Game results pending sync | Client → Server |
| `installed_games` | Downloaded game metadata | Server → Client |
| `game_progress` | Checkpoint saves for long games | Client → Server |

**Already Implemented:** ✅
- `LocalDevice` entity
- `LocalGameScore` entity
- `InstalledGame` entity
- `SyncService` for score upload

### Teacher Portal - H2 (Embedded) ✅ ALREADY IMPLEMENTED

**Location:** `./data/eduproteacher.mv.db` (in Heronix-Teacher project)

**Stores:**
| Table | Purpose | Sync Direction |
|-------|---------|----------------|
| `students` | Student roster | Server → Client |
| `assignments` | Assignment definitions | Bidirectional |
| `assignment_categories` | Grading categories | Bidirectional |
| `grades` | Student grades | Client → Server |
| `attendance` | Attendance records | Client → Server |
| `hall_passes` | Hall pass records | Client → Server |
| `teachers` | Teacher profile/auth | Bidirectional |
| `clubs` | Club data | Bidirectional |
| `class_wallets` | Class reward system | Bidirectional |

**Implementation Status:** ✅ Complete

Key Files:
- `DatabaseConfig.java` - H2 file-based configuration
- `AutoSyncService.java` - Background sync every 15 seconds
- `SyncManagerService.java` - Bidirectional sync orchestration
- `NetworkMonitorService.java` - Network connectivity detection
- All entities have `syncStatus` field for tracking

**Sync Features:**
- Auto-sync every 15 seconds (configurable via `sync.interval.seconds`)
- Network disruption resilient (continues offline)
- Power failure protection (saves locally first)
- Batch sync for efficiency (`sync.batch-size`)
- Automatic retry on failure (`sync.retry.max-attempts`)
- Manual sync trigger via `syncNow()`
- Sync statistics tracking

### Server - PostgreSQL (Production)

**Purpose:** Central source of truth for all data

**Tables:**
| Category | Tables |
|----------|--------|
| **Auth** | `users`, `roles`, `teachers`, `students` |
| **Schools** | `schools`, `classes`, `school_licenses` |
| **Devices** | `devices`, `device_registrations` |
| **Games** | `games`, `game_bundles`, `installed_games` |
| **Gameplay** | `game_sessions`, `game_players`, `game_scores` |
| **Content** | `question_sets`, `questions` |
| **Analytics** | `play_time_logs`, `score_aggregates` |

**Current:** H2 (development only)
**To Implement:** ⏳ PostgreSQL migration

## Sync Strategy

### Conflict Resolution

**Strategy:** Last-Write-Wins with Server Authority

```
1. Client saves data locally with timestamp
2. SyncService uploads when online
3. Server validates and stores
4. Server returns canonical version
5. Client updates local copy
```

### Sync Priority

| Data Type | Priority | Retry Policy |
|-----------|----------|--------------|
| Game scores | High | Retry until success |
| Device auth | High | Retry with backoff |
| Session drafts | Medium | Retry 3 times |
| Analytics cache | Low | Best effort |

### Offline Capabilities

**Student Client:**
- Play downloaded single-player games ✅
- Store scores locally for later sync ✅
- View cached leaderboards (read-only)
- Join multiplayer requires connection

**Teacher Portal:**
- Create/edit session drafts offline
- View cached analytics
- Queue device approvals
- Publish sessions requires connection

## Implementation Plan

### Phase 1: PostgreSQL Server Migration (Priority: High) ⏳ TODO
1. Add PostgreSQL driver to `heronix-server/pom.xml`
2. Create `application-prod.yml` with PostgreSQL config
3. Review JPA entities for PostgreSQL compatibility
4. Create Flyway/Liquibase migration scripts
5. Set up connection pooling (HikariCP)
6. Test with Docker PostgreSQL instance

### Phase 2: Student Client Local DB ✅ COMPLETE
- `LocalDevice` entity with JWT caching
- `LocalGameScore` entity with sync tracking
- `InstalledGame` entity for offline play
- `SyncService` for background score upload
- `DeviceService` with token restoration

### Phase 3: Teacher Portal Local DB ✅ COMPLETE
- H2 embedded database at `./data/eduproteacher.mv.db`
- `AutoSyncService` - 15-second interval sync
- `SyncManagerService` - Bidirectional sync
- `NetworkMonitorService` - Connectivity detection
- All entities support `syncStatus` tracking
- Sync for: students, assignments, grades, attendance, hall passes

### Phase 4: Enhanced Sync (Priority: Low) ⏳ TODO
1. Implement delta sync (only changed records)
2. Add sync conflict UI for teachers
3. Implement sync queue prioritization
4. Add sync progress indicators

## Configuration

### Development (H2)
```yaml
# application.yml (server)
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:file:./data/heronix
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
```

### Production (PostgreSQL)
```yaml
# application-prod.yml (server)
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/heronix
    username: ${DB_USER:heronix}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
```

## Capacity Planning

### PostgreSQL Requirements (Enterprise)

| Metric | Estimate | Configuration |
|--------|----------|---------------|
| Concurrent connections | 1,100+ | `max_connections: 200` + PgBouncer |
| Students | 1,000+ | Indexed queries |
| Teachers | 100+ | Connection pooling |
| Games per session | 30+ simultaneous | WebSocket + async writes |
| Scores per day | 10,000+ | Partitioned tables |
| Storage (1 year) | ~50GB | Regular archival |

### Recommended PostgreSQL Settings
```
# postgresql.conf
max_connections = 200
shared_buffers = 256MB
effective_cache_size = 768MB
work_mem = 4MB
maintenance_work_mem = 64MB
```

## Security Considerations

1. **Client H2 databases** are encrypted at rest (planned)
2. **JWT tokens** stored locally expire after 24 hours
3. **PostgreSQL** uses SSL connections in production
4. **Sync requests** are authenticated with JWT
5. **Sensitive data** (passwords) never stored on clients

## Related Files

### Student Client (heronix-edu-games/heronix-client)
- DB Manager: `src/main/java/com/heronix/edu/client/db/DatabaseManager.java`
- Entities: `src/main/java/com/heronix/edu/client/db/entity/`
- Repositories: `src/main/java/com/heronix/edu/client/db/repository/`
- Sync Service: `src/main/java/com/heronix/edu/client/service/SyncService.java`
- Device Service: `src/main/java/com/heronix/edu/client/service/DeviceService.java`

### Teacher Portal (Heronix-Teacher)
- DB Config: `src/main/java/com/heronix/teacher/config/DatabaseConfig.java`
- Entities: `src/main/java/com/heronix/teacher/model/domain/`
- Repositories: `src/main/java/com/heronix/teacher/repository/`
- Auto Sync: `src/main/java/com/heronix/teacher/service/AutoSyncService.java`
- Sync Manager: `src/main/java/com/heronix/teacher/service/SyncManagerService.java`
- Network Monitor: `src/main/java/com/heronix/teacher/service/NetworkMonitorService.java`

### Server (heronix-edu-games/heronix-server)
- Entities: `src/main/java/com/heronix/edu/server/entity/`
- Repositories: `src/main/java/com/heronix/edu/server/repository/`
- Config: `src/main/resources/application.yml`
