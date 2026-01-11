# Heronix Educational Games - Client Application

A JavaFX desktop application for students to play educational games with offline-first architecture and automatic score synchronization.

## Overview

The Heronix Client is a cross-platform desktop application (Windows/Mac/Linux) that allows students to:
- Play educational games installed on their device
- Track game scores and progress locally
- Automatically sync scores to the server when online
- Work completely offline with background synchronization

## Features

### Core Functionality
- **Device Registration**: Simple setup wizard using registration codes provided by teachers
- **Offline-First**: All games and scores work without internet connection
- **Background Sync**: Automatic score upload every 5 minutes when online
- **Game Launcher**: Clean interface to browse and launch installed games
- **Settings Panel**: View device status, test connectivity, and force sync

### Security & Privacy
- **Device-Based Authentication**: One device = one student (no passwords)
- **JWT Token Security**: Secure API authentication with automatic token refresh
- **FERPA Compliant**: Minimal PII storage, audit logging
- **Local Encryption**: Sensitive data encrypted in local H2 database

### Architecture
- **Offline Queue**: Scores saved locally and synced when connection restored
- **Server Wins**: Conflict resolution strategy (server data takes precedence)
- **Network Resilience**: Automatic retry with exponential backoff
- **Dynamic Game Loading**: Games downloaded as JARs and loaded via custom ClassLoader

## Prerequisites

- **Java**: JDK 17 or higher
- **JavaFX**: 21.0.1 (included via Maven)
- **Disk Space**: ~100MB for application + games
- **Network**: Internet connection for initial setup and sync (optional for gameplay)

## Installation

### Building from Source

```bash
cd heronix-edu-games
mvn clean package -pl heronix-client -am
```

The executable JAR will be created at:
```
heronix-client/target/heronix-client-1.0.0-SNAPSHOT.jar
```

### Running the Application

```bash
java -jar heronix-client/target/heronix-client-1.0.0-SNAPSHOT.jar
```

Or using Maven:
```bash
mvn javafx:run -pl heronix-client
```

## First-Time Setup

### 1. Launch Application
When launched for the first time, the setup wizard will appear.

### 2. Get Registration Code
Ask your teacher for a registration code. The teacher generates these codes from the teacher portal.

### 3. Register Device
- Enter a device name (e.g., "Alice's Laptop")
- Enter the registration code
- Click "Register"

### 4. Wait for Approval
The device status will show "PENDING" while waiting for teacher approval. The application will automatically poll the server every 5 seconds.

### 5. Auto-Authentication
Once approved, the application will:
- Automatically authenticate and obtain a security token
- Load the main launcher
- Start background sync service

## Using the Application

### Main Launcher

The main launcher displays:
- **Student Information**: Device name and student ID
- **Games Grid**: Installed games with play buttons
- **Sync Status**: Online/offline indicator and pending score count
- **Settings Button**: Access configuration and diagnostics

### Playing Games

1. Click a game card in the launcher
2. Game opens in a new window
3. Play the game to completion
4. Score automatically saved locally
5. Score synced to server when online

### Offline Mode

The client works completely offline:
- Games can be played without internet
- Scores queue locally in the database
- Automatic sync when connection restored
- Visual indicator shows pending sync count

### Settings Panel

Access via the ⚙ button in the top-right:
- **Device Info**: View device ID, status, student assignment
- **App Settings**: Server URL, sync interval, network status
- **Actions**: Test connection, force sync now

## Configuration

### Application Settings

Default configuration in `~/.heronix/`:
```
~/.heronix/
├── client.db          # H2 database (scores, device info)
├── games/             # Downloaded game JAR files
└── logs/              # Application logs
```

### Server Configuration

Edit `heronix-client/src/main/resources/application.properties`:

```properties
# Server URL
heronix.server.url=http://localhost:8080

# Sync settings
heronix.sync.interval.minutes=5
heronix.sync.batch.size=100

# Database
heronix.db.path=${user.home}/.heronix/client.db

# Games
heronix.games.directory=${user.home}/.heronix/games
```

## Architecture Details

### Database Schema

**device**: Single row storing device registration and token
**game_score**: Queue of game scores (synced/unsynced)
**installed_game**: Metadata for downloaded games
**sync_log**: Audit trail of sync operations

### Services

- **DeviceService**: Device lifecycle (registration, auth, approval)
- **GameManager**: Game download and loading
- **ScoreService**: Score persistence and querying
- **SyncService**: Background score synchronization
- **NetworkMonitor**: Connection state monitoring

### UI Controllers

- **SetupWizardController**: First-run registration wizard
- **WaitingForApprovalController**: Polling screen during approval
- **MainLauncherController**: Main game launcher UI
- **SettingsController**: Configuration and diagnostics
- **GameContainerController**: Game window wrapper

## Troubleshooting

### Device Not Registering

**Problem**: Registration fails with error message

**Solutions**:
- Check internet connection
- Verify registration code is correct and not expired
- Ensure server is running at configured URL
- Check firewall settings

### Scores Not Syncing

**Problem**: Pending scores remain unsynced

**Solutions**:
- Check network status indicator (should be green "Online")
- Click "Sync Now" in settings to force manual sync
- Verify device is still approved (Settings → Device Status)
- Check logs: `~/.heronix/logs/client.log`

### Games Not Loading

**Problem**: Game fails to launch or crashes

**Solutions**:
- Ensure game JAR exists in `~/.heronix/games/`
- Verify game checksum matches server
- Re-download game (delete JAR and reinstall)
- Check game compatibility with Java version

### Token Expired

**Problem**: "Token expired or invalid" error

**Solutions**:
- Application automatically re-authenticates
- If persists, re-register device
- Contact teacher to verify device approval status

## Development

### Project Structure

```
heronix-client/
├── src/main/java/com/heronix/edu/client/
│   ├── HeronixClientApplication.java    # Main entry point
│   ├── api/                             # REST API client
│   │   ├── HeronixApiClient.java
│   │   └── dto/                         # Data transfer objects
│   ├── config/                          # Configuration
│   │   ├── AppConfig.java
│   │   └── HttpClientConfig.java
│   ├── db/                              # Database layer
│   │   ├── DatabaseManager.java
│   │   ├── entity/                      # JPA-style entities
│   │   └── repository/                  # Data access
│   ├── service/                         # Business logic
│   │   ├── DeviceService.java
│   │   ├── GameManager.java
│   │   ├── ScoreService.java
│   │   └── SyncService.java
│   ├── game/                            # Game loading
│   │   ├── GameClassLoader.java
│   │   └── GameLauncher.java
│   ├── ui/                              # JavaFX UI
│   │   ├── controller/
│   │   └── component/
│   ├── security/                        # Auth & encryption
│   │   ├── TokenManager.java
│   │   └── DeviceIdentifier.java
│   └── util/                            # Utilities
└── src/main/resources/
    ├── view/                            # FXML files
    ├── schema.sql                       # Database schema
    ├── application.properties           # Configuration
    └── logback.xml                      # Logging config
```

### Adding New Features

1. **New Service**: Add to `service/` package and inject via constructor
2. **New UI Screen**: Create FXML in `view/` and controller in `ui/controller/`
3. **Database Changes**: Update `schema.sql` and entity classes
4. **New API Endpoint**: Add method to `HeronixApiClient`

### Running Tests

```bash
mvn test -pl heronix-client
```

### Building Distribution

```bash
mvn clean package -pl heronix-client -am
```

Creates standalone JAR with all dependencies.

## API Integration

The client communicates with the server via REST API:

### Endpoints Used

- `POST /api/device/register` - Register new device
- `GET /api/device/status` - Check approval status
- `POST /api/auth/device` - Authenticate and get JWT
- `POST /api/sync/upload` - Upload game scores
- `GET /api/games/list` - List available games
- `GET /api/games/{id}/download` - Download game JAR
- `GET /api/ping` - Test connectivity

### Authentication Flow

```
1. Device Registration → Server assigns student ID
2. Teacher Approval → Device status = APPROVED
3. Device Authentication → Server issues JWT token
4. Token Storage → Encrypted in local database
5. API Requests → Include Bearer token in headers
6. Token Refresh → Auto re-auth when expired
```

## Security Considerations

### Data Storage
- Tokens encrypted in local H2 database
- Database file permissions restricted to user
- No plaintext passwords or sensitive data

### Network Security
- All API calls use HTTPS in production
- JWT tokens with 90-day expiration
- Automatic token refresh before expiration

### Privacy Compliance
- Minimal PII: First name, last initial only
- No student passwords (device-based auth)
- Audit logging for FERPA compliance
- Parent opt-out support

## Support

For issues or questions:
- Check logs: `~/.heronix/logs/client.log`
- Contact your teacher for registration issues
- Report bugs: [GitHub Issues](https://github.com/heronix/heronix-edu-games/issues)

## License

Copyright © 2025 Heronix Educational Games
All rights reserved.
