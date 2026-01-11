# Heronix Educational Games Platform

A comprehensive offline-first educational gaming platform designed for K-12 schools, following FERPA compliance requirements and the Heronix philosophy of local network-only operation with teacher control.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Quick Start](#quick-start)
- [Building the Project](#building-the-project)
- [Deployment](#deployment)
- [Creating New Games](#creating-new-games)
- [FERPA Compliance](#ferpa-compliance)
- [Documentation](#documentation)
- [License](#license)

## 🎯 Overview

The Heronix Educational Games Platform enables students to play educational games on their personal devices (computers, tablets, phones) both inside and outside the school network. When students return to school, their scores and progress automatically sync with the central server.

### Core Principles

1. **Offline-First**: Games work without internet connection
2. **Teacher-Controlled**: Teachers manage students, assignments, and device approvals
3. **Device Registration**: Only approved devices can sync
4. **Privacy-Focused**: FERPA compliant with minimal PII collection
5. **Local Network**: Operates within school's network infrastructure

## ✨ Features

### For Students
- ✅ Play games offline on any approved device
- ✅ Automatic progress synchronization
- ✅ Track personal scores and improvements
- ✅ Multiple difficulty levels
- ✅ Age-appropriate content

### For Teachers
- ✅ Assign games to classes or individual students
- ✅ Monitor student progress and performance
- ✅ Approve/revoke device registrations
- ✅ Generate performance reports
- ✅ Manage classes and enrollments

### For Administrators
- ✅ Multi-school support
- ✅ User and role management
- ✅ Audit logging for compliance
- ✅ Data retention policies
- ✅ System health monitoring

## 🏗️ Architecture

The platform consists of four main modules:

```
heronix-edu-games/
├── heronix-common/      # Shared models and utilities
├── heronix-server/      # Server application for schools
├── heronix-client/      # Student application
└── heronix-games/       # Educational game implementations
```

### Technology Stack

- **Language**: Java 17+
- **UI Framework**: JavaFX 21
- **Build Tool**: Maven 3.9+
- **Database**: H2 (embedded)
- **JSON**: Jackson
- **Logging**: SLF4J

## 📦 Requirements

### Server (School)
- Java 17 or higher
- 2GB RAM minimum (4GB recommended)
- 10GB storage minimum
- Windows 10+, macOS 11+, or Linux
- Local network with static IP or hostname

### Client (Student Devices)
- Java 17 or higher (or bundled JRE)
- 500MB storage minimum
- Windows 10+, macOS 11+, or Linux
- Optional: Android 8+ (with Gluon Mobile)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/yourorg/heronix-edu-games.git
cd heronix-edu-games
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Initialize the Database

```bash
# The database will be created automatically on first run
# Or manually initialize with:
java -cp heronix-server/target/heronix-server-1.0.0-SNAPSHOT.jar \
     com.heronix.edu.server.DatabaseInitializer
```

### 4. Start the Server

```bash
cd heronix-server
mvn javafx:run
```

Or run the packaged JAR:

```bash
java -jar heronix-server/target/heronix-server-1.0.0-SNAPSHOT.jar
```

### 5. Start the Client

```bash
cd heronix-client
mvn javafx:run
```

### 6. Register a Device

1. Open the client application
2. Click "Register Device"
3. Enter the registration code provided by your teacher
4. Wait for teacher approval
5. Start playing!

## 🔨 Building the Project

### Full Build

```bash
mvn clean install
```

### Build Individual Modules

```bash
# Common module
cd heronix-common
mvn clean install

# Server
cd heronix-server
mvn clean package

# Client
cd heronix-client
mvn clean package

# Games
cd heronix-games
mvn clean install
```

### Create Distributable Packages

```bash
# Server package with dependencies
cd heronix-server
mvn clean package assembly:single

# Client package with dependencies
cd heronix-client
mvn clean package assembly:single
```

### Run Tests

```bash
mvn clean test
```

## 📱 Deployment

### Server Deployment

1. **Install Java 17+** on the school server

2. **Copy the server JAR**:
   ```bash
   scp heronix-server/target/heronix-server-1.0.0-SNAPSHOT.jar server:/opt/heronix/
   ```

3. **Create configuration file** (`/opt/heronix/config.properties`):
   ```properties
   # Server Configuration
   server.port=8080
   server.host=0.0.0.0
   
   # Database Configuration
   db.path=./data/heronix.db
   db.username=admin
   db.password=CHANGE_ME
   
   # School Configuration
   school.id=SCHOOL001
   school.name=Your School Name
   
   # Security
   auth.token.expiry.days=90
   session.timeout.minutes=60
   ```

4. **Start the server**:
   ```bash
   java -jar /opt/heronix/heronix-server-1.0.0-SNAPSHOT.jar
   ```

5. **Setup as system service** (Linux):
   ```bash
   sudo cp heronix-server.service /etc/systemd/system/
   sudo systemctl enable heronix-server
   sudo systemctl start heronix-server
   ```

### Client Deployment

#### Option 1: Direct Download
Host the client JAR on the school network for students to download:
```
http://school-server:8080/downloads/heronix-client.jar
```

#### Option 2: USB Distribution
Copy to USB drives for distribution to students

#### Option 3: Network Share
Place on a shared network drive accessible to students

### Mobile Deployment (Android)

Using Gluon Mobile:

```bash
# Install Gluon Mobile plugin
mvn gluonfx:build

# Generate APK
mvn gluonfx:package -Pandroid
```

## 🎮 Creating New Games

### Step 1: Create Game Class

Create a new class extending `AbstractGame`:

```java
package com.heronix.edu.games.custom;

import com.heronix.edu.common.game.*;

public class MyCustomGame extends AbstractGame {
    
    @Override
    public String getGameId() {
        return "my-custom-game";
    }
    
    @Override
    public String getName() {
        return "My Custom Game";
    }
    
    @Override
    public String getDescription() {
        return "Description of your game";
    }
    
    @Override
    public GameSubject getSubject() {
        return GameSubject.MATHEMATICS; // or other subject
    }
    
    @Override
    public GradeLevel[] getTargetGrades() {
        return new GradeLevel[]{
            GradeLevel.THIRD,
            GradeLevel.FOURTH,
            GradeLevel.FIFTH
        };
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    protected void onInitialize() {
        // Setup game components
    }
    
    @Override
    protected void onStart() {
        // Start game logic
    }
    
    @Override
    protected void onPause() {
        // Pause game
    }
    
    @Override
    protected void onResume() {
        // Resume game
    }
    
    @Override
    protected void onStop() {
        // Clean up and finalize score
    }
    
    @Override
    protected void saveStateData(GameState state) {
        // Save game state for pause/resume
    }
    
    @Override
    protected void loadStateData(GameState state) {
        // Restore game state
    }
}
```

### Step 2: Implement Game Logic

See `MathSprintGame.java` for a complete example implementation.

### Step 3: Add to Game Registry

Register your game in the game registry so it's available in the platform.

### Step 4: Test Your Game

```bash
cd heronix-games
mvn test -Dtest=MyCustomGameTest
```

## 🔒 FERPA Compliance

This platform is designed with FERPA compliance in mind. Key features:

### Data Minimization
- Only collects essential educational data
- No SSN or sensitive personal information
- Student names: first name + last initial only

### Access Controls
- Role-based access (Teacher, Admin, IT)
- Teachers only see their assigned students
- Audit logging of all data access

### Parental Rights
- Consent management system
- Data export capability
- Right to review and delete

### Security
- Encrypted data at rest (AES-256)
- TLS encryption in transit
- Device authentication
- Session management

See [FERPA_COMPLIANCE.md](FERPA_COMPLIANCE.md) for detailed compliance information.

## 📚 Documentation

- [Architecture Documentation](ARCHITECTURE.md) - Detailed system architecture
- [FERPA Compliance Guide](FERPA_COMPLIANCE.md) - Privacy and compliance information
- [Database Schema](database-schema.sql) - Complete database structure
- [API Documentation](docs/API.md) - REST API endpoints (coming soon)
- [Teacher Guide](docs/TEACHER_GUIDE.md) - Guide for teachers (coming soon)
- [Student Guide](docs/STUDENT_GUIDE.md) - Guide for students (coming soon)

## 🎯 Roadmap

### Current Games
- ✅ Math Sprint - Arithmetic practice
- 🚧 Word Builder - Vocabulary building
- 🚧 Science Quest - Science concepts
- 🚧 Geography Challenge - World geography
- 🚧 History Timeline - Historical events

### Future Features
- [ ] Multiplayer games on local network
- [ ] Advanced teacher analytics dashboard
- [ ] Custom game builder for teachers
- [ ] Parent portal (with consent)
- [ ] Achievement and badge system
- [ ] Accessibility features (screen reader, high contrast)
- [ ] More games across all subjects

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes
4. Write/update tests
5. Ensure all tests pass: `mvn test`
6. Commit your changes: `git commit -am 'Add new feature'`
7. Push to the branch: `git push origin feature/my-feature`
8. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 💬 Support

For support and questions:
- Email: support@heronix-edu.example.com
- Documentation: https://docs.heronix-edu.example.com
- Issue Tracker: https://github.com/yourorg/heronix-edu-games/issues

## 🙏 Acknowledgments

- Developed with educational privacy and student safety as top priorities
- Designed in consultation with K-12 educators
- Built to support diverse learning needs

---

**Note**: This is an educational platform designed for use in schools. Please ensure compliance with your local regulations and obtain proper consent before deployment.
