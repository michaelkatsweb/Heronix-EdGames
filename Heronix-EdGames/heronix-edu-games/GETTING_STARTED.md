# Getting Started with Heronix Educational Games

This guide will help you set up and start using the Heronix Educational Games platform.

## For Teachers

### Initial Setup

#### 1. Access the Teacher Dashboard

Once your school's IT department has installed the server:

1. Open your web browser or the teacher application
2. Navigate to `http://school-server:8080` (or the address provided by IT)
3. Log in with your credentials

#### 2. Set Up Your First Class

1. Click **"Classes"** in the navigation menu
2. Click **"Create New Class"**
3. Fill in the class information:
   - Class Name (e.g., "Math 3A")
   - Grade Level
   - Subject
   - School Year
4. Click **"Create"**

#### 3. Add Students to Your Class

1. Go to your class page
2. Click **"Add Students"**
3. Either:
   - **Import from CSV**: Upload a CSV file with student information
   - **Add Manually**: Enter each student's information
   
**Important**: Only collect necessary information:
- First name
- Last initial (not full last name)
- Student ID (internal school ID, never SSN)
- Grade level

#### 4. Generate Device Registration Codes

Students need a registration code to connect their devices:

1. Go to your class page
2. Click **"Device Registration"**
3. Click **"Generate New Code"**
4. Set options:
   - Maximum uses (usually set to number of students)
   - Expiration date (optional)
5. Share this code with your students

**Example Code**: `ABC-DEF-123`

### Managing Students

#### Approve Device Registrations

When a student registers a device:

1. Go to **"Device Management"**
2. Review pending registrations
3. Verify the student and device information
4. Click **"Approve"** or **"Reject"**

#### Assign Games

1. Go to your class page
2. Click **"Assignments"**
3. Click **"Create Assignment"**
4. Select:
   - Game to assign
   - Due date (optional)
   - Minimum score requirement (optional)
   - Whether completion is required
5. Click **"Assign"**

#### Monitor Progress

1. Go to your class page
2. Click **"Performance"** tab
3. View:
   - Individual student progress
   - Class averages
   - Time spent on games
   - Accuracy and completion rates

### Working with Reports

#### Generate Reports

1. Go to **"Reports"**
2. Select report type:
   - Class Performance Summary
   - Individual Student Progress
   - Game Completion Status
   - Time Analysis
3. Set date range
4. Click **"Generate Report"**
5. Export as PDF or CSV

## For Students

### Getting Started

#### 1. Install the Heronix Client

**Option A: Download from School Network**
1. On the school network, visit: `http://school-server:8080/downloads`
2. Download `heronix-client.jar`
3. Save to your computer

**Option B: USB Drive**
1. Get the USB drive from your teacher
2. Copy `heronix-client.jar` to your computer

**Option C: Already Installed**
1. Look for the Heronix icon on your computer

#### 2. First Time Setup - Register Your Device

1. **Launch the Application**:
   - Double-click `heronix-client.jar`, or
   - Right-click and choose "Open with Java", or
   - From command line: `java -jar heronix-client.jar`

2. **Register Your Device**:
   - Click **"Register New Device"**
   - Give your device a name (e.g., "Emma's Laptop")
   - Enter the registration code from your teacher
   - Click **"Submit Registration"**

3. **Wait for Approval**:
   - Your teacher will review your registration
   - You'll see "Pending Approval" status
   - Once approved, you'll see "Device Approved!"

4. **Initial Sync**:
   - Click **"Sync Now"** to download your assigned games
   - Wait for the download to complete

#### 3. Playing Games

1. **Select a Game**:
   - Browse available games in the main screen
   - Games are organized by subject
   - Look for assigned games (marked with a star ⭐)

2. **Choose Difficulty**:
   - Easy: For learning new concepts
   - Medium: For regular practice
   - Hard: For challenge
   - Expert: For advanced students

3. **Play**:
   - Click **"Start Game"**
   - Follow the game instructions
   - Try to get the best score!

4. **Pause and Resume**:
   - Click **"Pause"** if you need a break
   - Your progress is saved automatically
   - Click **"Resume"** to continue

#### 4. Playing at Home (Offline)

The best part about Heronix is that you can play anywhere!

**At Home**:
1. Open the Heronix application
2. Your games and progress are already there
3. Play games normally (no internet needed!)
4. Your scores are saved locally

**Back at School**:
1. Connect to the school network
2. Open Heronix
3. It will automatically sync your scores
4. Your teacher can now see your progress!

### Tips for Students

#### Getting Better Scores
- ⏰ Don't rush - accuracy is more important than speed
- 📚 Practice regularly for improvement
- 🎯 Start with easier difficulty and work your way up
- 🔄 Review incorrect answers to learn from mistakes

#### If Something Goes Wrong

**Game Won't Start**:
- Make sure you completed the registration
- Check that you're logged in
- Try restarting the application

**Can't Sync**:
- Make sure you're on the school network
- Check your device approval status
- Ask your teacher if your device needs re-approval

**Lost Progress**:
- Don't worry! Your scores are saved locally
- They'll sync next time you're at school
- If you deleted the app, previous scores may be lost

## For IT Administrators

### Server Installation

#### Prerequisites

1. **Java Runtime Environment**:
   ```bash
   # Check Java version
   java -version
   # Should be 17 or higher
   ```

2. **Server Requirements**:
   - OS: Windows Server 2016+, Ubuntu 20.04+, or equivalent
   - RAM: 4GB minimum, 8GB recommended
   - Storage: 50GB minimum for small school
   - Network: Static IP or hostname on local network

#### Installation Steps

1. **Create Installation Directory**:
   ```bash
   # Linux/Mac
   sudo mkdir -p /opt/heronix
   cd /opt/heronix
   
   # Windows
   md C:\Heronix
   cd C:\Heronix
   ```

2. **Copy Server Files**:
   ```bash
   # Copy the server JAR
   cp heronix-server-1.0.0-SNAPSHOT.jar /opt/heronix/
   
   # Copy configuration template
   cp config.properties.template /opt/heronix/config.properties
   ```

3. **Configure the Server**:
   Edit `/opt/heronix/config.properties`:
   ```properties
   # Server Settings
   server.port=8080
   server.host=0.0.0.0
   
   # Database Settings
   db.path=./data/heronix.db
   db.username=admin
   db.password=CHANGE_THIS_PASSWORD
   
   # School Settings
   school.id=YOUR_SCHOOL_ID
   school.name=Your School Name
   school.district=Your District
   school.state=CA
   
   # Security Settings
   auth.token.expiry.days=90
   session.timeout.minutes=60
   max.device.per.student=3
   
   # Backup Settings
   backup.enabled=true
   backup.path=./backups
   backup.schedule.cron=0 0 2 * * *  # Daily at 2 AM
   
   # Data Retention (FERPA Compliance)
   data.retention.years=2
   data.purge.enabled=true
   data.purge.schedule.cron=0 0 3 * * *  # Daily at 3 AM
   ```

4. **Initialize Database**:
   ```bash
   java -jar heronix-server-1.0.0-SNAPSHOT.jar --init-db
   ```

5. **Start the Server**:
   ```bash
   java -jar heronix-server-1.0.0-SNAPSHOT.jar
   ```

#### Setup as System Service (Linux)

1. Create service file `/etc/systemd/system/heronix.service`:
   ```ini
   [Unit]
   Description=Heronix Educational Games Server
   After=network.target
   
   [Service]
   Type=simple
   User=heronix
   WorkingDirectory=/opt/heronix
   ExecStart=/usr/bin/java -jar /opt/heronix/heronix-server-1.0.0-SNAPSHOT.jar
   Restart=on-failure
   RestartSec=10
   
   [Install]
   WantedBy=multi-user.target
   ```

2. Enable and start:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable heronix
   sudo systemctl start heronix
   ```

3. Check status:
   ```bash
   sudo systemctl status heronix
   ```

### Network Configuration

#### Firewall Rules

Allow incoming connections on port 8080:

```bash
# Linux (UFW)
sudo ufw allow 8080/tcp

# Linux (firewalld)
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload

# Windows Firewall
netsh advfirewall firewall add rule name="Heronix Server" dir=in action=allow protocol=TCP localport=8080
```

#### DNS/Hostname Setup

Set up a hostname for easy access:
- Internal DNS: `heronix.school.local` → server IP
- Or use server's hostname

### Backup and Recovery

#### Automatic Backups

Backups are configured in `config.properties`:
```properties
backup.enabled=true
backup.path=./backups
backup.schedule.cron=0 0 2 * * *  # Daily at 2 AM
backup.retention.days=30
```

#### Manual Backup

```bash
# Stop the server
systemctl stop heronix

# Backup database
cp -r /opt/heronix/data /backup/location/heronix-$(date +%Y%m%d)

# Start the server
systemctl start heronix
```

#### Restore from Backup

```bash
# Stop the server
systemctl stop heronix

# Restore database
cp -r /backup/location/heronix-20250109/data /opt/heronix/data

# Start the server
systemctl start heronix
```

### Monitoring

#### Check Server Health

```bash
# View logs
tail -f /opt/heronix/logs/heronix.log

# Check service status
systemctl status heronix

# Check database size
du -h /opt/heronix/data
```

#### Monitor Performance

Access the admin dashboard:
- URL: `http://server:8080/admin`
- View:
  - Active users
  - Database size
  - Recent syncs
  - System resources

### Troubleshooting

#### Server Won't Start

1. Check Java version: `java -version`
2. Check port availability: `netstat -an | grep 8080`
3. Check logs: `cat /opt/heronix/logs/heronix.log`
4. Verify permissions: `ls -la /opt/heronix`

#### Clients Can't Connect

1. Verify server is running: `systemctl status heronix`
2. Check firewall rules
3. Test connectivity: `telnet server-ip 8080`
4. Check server logs for connection attempts

#### Database Issues

1. Check disk space: `df -h`
2. Verify database file exists: `ls -la /opt/heronix/data`
3. Run integrity check:
   ```bash
   java -jar heronix-server-1.0.0-SNAPSHOT.jar --check-db
   ```

## Support Resources

- **Documentation**: Full documentation in `/docs` folder
- **Email Support**: it-support@school.edu
- **Issue Tracker**: Report bugs on GitHub
- **Community Forum**: https://community.heronix-edu.example.com

## Quick Reference

### Teacher Quick Commands
- Generate registration code: Class → Device Registration → Generate Code
- Approve device: Device Management → Pending → Approve
- Assign game: Class → Assignments → Create Assignment
- View progress: Class → Performance

### Student Quick Commands
- Register device: Register New Device → Enter Code
- Sync progress: Main Menu → Sync Now
- Play game: Games → Select Game → Start
- View scores: Profile → My Scores

### Admin Quick Commands
- Check status: `systemctl status heronix`
- View logs: `tail -f /opt/heronix/logs/heronix.log`
- Backup: `systemctl stop heronix && cp -r data backup/`
- Restart: `systemctl restart heronix`
