# Heronix Educational Games Platform - Architecture Documentation

## Overview

The Heronix Educational Games Platform is an offline-first educational gaming system that allows students to play educational games on their devices (phone/computer) both inside and outside the school network, with automatic synchronization of progress and scores when reconnected to the school network.

## Core Philosophy: Heronix Principles

1. **Offline-First**: All games function fully without internet connectivity
2. **Teacher-Controlled**: Teachers manage student access, game assignments, and progress monitoring
3. **Local Network**: Primary operation within school's local network
4. **Device Registration**: Only approved devices can access the platform
5. **Privacy-Focused**: FERPA compliant with minimal PII collection

## System Architecture

### Three-Tier Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Applications                      │
│  (Student devices - Desktop/Mobile with JavaFX)             │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Game UI    │  │  Local DB    │  │ Sync Engine  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↕ (Sync when on network)
┌─────────────────────────────────────────────────────────────┐
│                      Server Application                      │
│         (School network - Java application)                  │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   REST API   │  │ Device Mgmt  │  │ Teacher UI   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                      Database Layer                          │
│              (H2 Database - embedded)                        │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Students   │  │    Scores    │  │   Devices    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

## Component Details

### 1. Client Application (heronix-client)

**Purpose**: Student-facing application for playing games

**Key Features**:
- JavaFX-based UI (cross-platform desktop support)
- Local SQLite/H2 database for offline storage
- Game engine and renderer
- Sync engine with conflict resolution
- Device identification and registration

**Offline Capabilities**:
- Full game functionality without network
- Local score tracking
- Progress saving
- Queue sync operations for when online

**Data Storage**:
```
~/.heronix/
  ├── config.json          # Device config
  ├── client.db            # Local H2 database
  ├── games/               # Game assets
  └── pending_sync.json    # Pending operations
```

### 2. Server Application (heronix-server)

**Purpose**: Central management system for teachers and sync

**Key Features**:
- REST API for client synchronization
- Teacher dashboard (JavaFX or web-based)
- Device approval/management system
- Student and class management
- Progress tracking and reporting
- Game content distribution

**Endpoints**:
```
POST   /api/device/register      - Device registration request
GET    /api/device/status         - Check device approval status
POST   /api/sync/upload           - Upload game scores/progress
GET    /api/sync/download         - Download assignments/updates
GET    /api/games/list            - List available games
GET    /api/games/{id}/download   - Download game package
POST   /api/auth/login            - Student authentication
```

### 3. Common Module (heronix-common)

**Purpose**: Shared code between client and server

**Contains**:
- Data models (Student, Score, Game, Device, etc.)
- Network protocols and DTOs
- Encryption utilities
- Constants and configurations
- Validation logic

### 4. Games Module (heronix-games)

**Purpose**: Collection of educational games

**Game Interface**:
```java
public interface EducationalGame {
    String getGameId();
    String getName();
    String getDescription();
    String getSubject();
    GradeLevel getTargetGrade();
    void start(GameContext context);
    void pause();
    void resume();
    void stop();
    GameResult getResult();
}
```

**Initial Games**:
1. **Math Sprint** - Timed arithmetic practice
2. **Word Builder** - Vocabulary and spelling
3. **Science Quest** - Science facts and concepts
4. **History Timeline** - Historical events ordering
5. **Geography Challenge** - World geography quiz

## Data Flow

### Device Registration Flow

```
1. Student installs app on device
2. App generates unique device ID
3. Student enters registration code (from teacher)
4. App sends registration request to server
5. Teacher reviews and approves device
6. Device receives approval token
7. App can now sync with server
```

### Game Play and Sync Flow

```
Offline Play:
1. Student opens app (no network required)
2. Selects game from locally cached games
3. Plays game
4. Scores saved to local database
5. Sync queued for next network connection

When Connected to School Network:
1. App detects school network
2. Authenticates with server
3. Uploads pending scores/progress
4. Downloads new assignments/games
5. Resolves any conflicts
6. Updates local cache
```

## Security Considerations

### Device Security
- Each device has unique identifier (UUID)
- Registration requires teacher-provided code
- Device approval before any data sync
- Revocation capability for lost/stolen devices

### Data Transmission
- TLS encryption for all network communication
- Device tokens with expiration
- Rate limiting on API endpoints
- Input validation and sanitization

### Local Storage
- Encrypted local database
- Secure storage of device credentials
- No plaintext storage of sensitive data

## FERPA Compliance

### Data Minimization
- Only collect essential educational data
- Student ID (internal, not SSN)
- First name and last initial only
- Game scores and progress
- No demographic data collected

### Access Control
- Teachers only see their assigned classes
- Students only see their own data
- Admin role for school IT
- Audit logging of data access

### Data Retention
- Scores retained for current school year + 1
- Automatic archival and purging
- Configurable retention policies
- Parent/student right to access and delete

### Consent Management
- Parental consent for students under 13
- Opt-out capability
- Clear privacy policy
- No third-party data sharing

## Scalability Considerations

### Client-Side
- Efficient local caching
- Progressive game loading
- Background sync processing
- Memory management for mobile devices

### Server-Side
- Connection pooling
- Batch processing for syncs
- Database indexing
- Horizontal scaling capability

## Deployment

### Server Deployment
```
School Server:
- Windows/Linux/Mac support
- Java 17+ runtime
- Minimum 2GB RAM, 10GB storage
- Local network accessibility
- Optional: SSL certificate for HTTPS
```

### Client Deployment
```
Student Devices:
- Desktop: Windows 10+, macOS 11+, Linux
- Mobile: Android 8+ (via Gluon Mobile)
- Java 17+ runtime or bundled JRE
- 500MB storage minimum
- Works offline after initial setup
```

## Monitoring and Maintenance

### Health Checks
- Server uptime monitoring
- Database integrity checks
- Client version tracking
- Sync failure alerts

### Updates
- Over-the-air game updates
- Client version checking
- Backward compatibility
- Rollback capability

## Future Enhancements

1. **Multiplayer Games**: Collaborative/competitive games on local network
2. **Teacher Analytics**: Advanced reporting and insights
3. **Custom Game Builder**: Teachers create custom content
4. **Parent Portal**: View student progress (with consent)
5. **Achievement System**: Badges and rewards
6. **Accessibility**: Screen reader support, customizable UI

## Technology Stack Summary

- **Language**: Java 17+
- **UI Framework**: JavaFX 21
- **Build Tool**: Maven 3.9+
- **Database**: H2 (embedded)
- **JSON**: Jackson
- **Logging**: SLF4J
- **Testing**: JUnit 5
- **Mobile**: Gluon Mobile (optional)

## Development Guidelines

### Code Standards
- Follow Java naming conventions
- Comprehensive JavaDoc
- Unit tests for business logic
- Integration tests for sync
- Code coverage > 70%

### Git Workflow
- Feature branches
- Pull request reviews
- Semantic versioning
- Tagged releases

### Documentation
- API documentation (OpenAPI/Swagger)
- User manuals (teacher and student)
- Installation guides
- Troubleshooting guides
