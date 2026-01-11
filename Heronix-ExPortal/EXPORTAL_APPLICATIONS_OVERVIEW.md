# Heronix ExPortal - Applications Overview

**Version:** 1.0.0
**Date:** December 27, 2025
**Purpose:** Overview of all external-facing applications in the Heronix ecosystem

---

## Folder Structure

```
H:\Heronix\Heronix-ExPortal\
│
├── Heronix-Mobile\              # Mobile app for teachers (school devices, on-campus)
├── Heronix-Portal\              # Web app for parents/students (public access)
└── Heronix-TeacherPortal\       # Web app for teachers (off-campus access)
```

---

## Application Comparison Matrix

| Application | Target Users | Access Location | Network | Device Type | Data Flow |
|------------|--------------|-----------------|---------|-------------|-----------|
| **Heronix-SIS** | School staff | School only | Internal network | School computers | Direct to SIS DB |
| **Heronix-Mobile** | Teachers | School only | Internal network | School-provided tablets | Direct to SIS DB |
| **Heronix-TeacherPortal** | Teachers | Anywhere | Internet | Personal devices | Staging → SIS |
| **Heronix-Portal** | Parents/Students | Anywhere | Internet | Personal devices | Staging → SIS |
| **Online Registration** | Public | Anywhere | Internet | Any device | Staging → SIS |

---

## 1. Heronix-SIS (Student Information System)

### Description
Desktop application (JavaFX) for comprehensive school administration.

### Users
- School administrators
- Registrar staff
- Guidance counselors
- Principals

### Access
- **Location**: School campus ONLY
- **Network**: Internal network (no internet access)
- **Security**: Windows Active Directory authentication
- **Devices**: School-provided desktop computers

### Features
- Complete student records management
- Enrollment and scheduling
- Grade management (direct entry)
- Attendance tracking
- Report generation
- Admin review dashboard (staging imports)

### Data Flow
```
SIS Desktop App → SIS Database (Direct Connection)
```

---

## 2. Heronix-Mobile

### Description
Mobile app for teachers using school-provided tablets/devices on campus.

### Users
- Teachers
- Substitute teachers
- Teacher assistants

### Access
- **Location**: School campus ONLY (on-premises)
- **Network**: School internal network (WiFi or wired)
- **Security**: Same credentials as SIS, network firewall
- **Devices**: School-provided iPads/Android tablets

### Technology
- React Native or Flutter
- Offline-first architecture (local SQLite)
- Biometric authentication (fingerprint/face unlock)
- Optimized for tablets (10-12 inch screens)

### Features
- Take attendance in classroom (real-time)
- Record grades during class
- View class rosters
- Quick access to student info
- Works offline (syncs when back online)
- Optimized for speed and ease of use

### Data Flow
```
Mobile App (on campus) → School WiFi → SIS Database (Direct Connection)
```

### Use Cases
- **In-Classroom Attendance**: Teacher opens app on tablet, marks attendance during roll call
- **Quick Grade Entry**: Enter quiz scores on tablet while students are working
- **Student Lookup**: Check student info during parent-teacher conference
- **Field Trip**: Take attendance offline, syncs when back at school

### Why Separate from TeacherPortal?
- **Speed**: Direct database connection is faster than going through staging server
- **Reliability**: Works even if internet is down (school network still up)
- **Real-Time**: Attendance/grades appear immediately in SIS
- **Security**: No internet exposure, protected by school firewall
- **Simplicity**: Doesn't need review/approval process for in-school work

---

## 3. Heronix-TeacherPortal

### Description
Web application for teachers to submit grades and manage assignments from home or off-campus.

### Users
- Teachers working from home
- Teachers during off-hours
- Remote/hybrid teachers

### Access
- **Location**: ANYWHERE with internet (home, coffee shop, library)
- **Network**: Public internet
- **Security**: JWT authentication, staging server validation
- **Devices**: Personal laptops, home computers, any web browser

### Technology
- React 18+ with TypeScript
- Progressive Web App (PWA)
- Responsive design (desktop-optimized)
- Tailwind CSS

### Features
- Submit grades (assignments, tests, projects)
- Record attendance (for makeup/corrections)
- Create and manage assignments
- View submission history
- Reports and analytics
- Offline support (PWA)

### Data Flow
```
Teacher at Home → TeacherPortal Web App → Staging Server → Admin Review → SIS Database
```

### Use Cases
- **Weekend Grading**: Teacher grades essays at home on Sunday, submits for review
- **Evening Planning**: Create assignments for next week from home computer
- **Remote Work**: Teacher working from home submits attendance corrections
- **After Hours**: Review student grades and analytics after school day

### Why Separate from Heronix-Mobile?
- **Different Environment**: Home network vs school network
- **Security Model**: Must go through staging server (not direct to SIS)
- **Device Type**: Desktop/laptop optimized (larger screen, keyboard/mouse)
- **Workflow**: Batch submissions with review process (not real-time)
- **Access Control**: Available from any internet connection (not restricted to school)

---

## 4. Heronix-Portal

### Description
Web application for parents and students to view information and submit updates.

### Users
- Parents/Guardians
- Students
- Family members

### Access
- **Location**: Anywhere with internet
- **Network**: Public internet
- **Security**: JWT authentication, staging server validation
- **Devices**: Phones, tablets, laptops, any browser

### Technology
- React or Vue 3 with TypeScript
- Mobile-first responsive design
- Progressive Web App (PWA)
- Push notifications

### Features

**For Parents:**
- View student grades and attendance
- Update contact information
- View upcoming assignments
- Communicate with teachers (messaging)
- View report cards
- Update emergency contacts

**For Students:**
- View their own grades
- See assignment due dates
- Check attendance record
- View class schedule
- Access resources (links to learning materials)

### Data Flow
```
Parent at Home → Portal Web App → Staging Server → Admin Review → SIS Database
```

### Use Cases
- **Grade Checking**: Parent logs in to check student's progress
- **Contact Update**: Parent moves, updates home address (pending admin approval)
- **Student Planning**: Student checks upcoming assignments and due dates
- **Report Cards**: View semester grades and GPA

---

## 5. Online Registration (Public Website)

### Description
Public-facing website for new student registration.

### Users
- Prospective parents
- New families
- General public

### Access
- **Location**: Anywhere
- **Network**: Public internet
- **Security**: reCAPTCHA, staging server validation
- **Devices**: Any device with browser

### Technology
- Next.js or Nuxt.js (Server-Side Rendering)
- SEO-optimized
- Multi-step form wizard
- Document upload capability

### Features
- Multi-step registration form
- Document upload (birth certificate, immunization records)
- Email verification
- Submission confirmation
- Track application status

### Data Flow
```
Public User → Registration Website → Staging Server → Admin Review → SIS Database
```

---

## Security Architecture Comparison

### Internal Network Applications (Direct SIS Access)

```
┌─────────────────────────────────────────────────────┐
│          SCHOOL INTERNAL NETWORK                     │
│          (Behind School Firewall)                    │
│                                                       │
│  ┌──────────────┐         ┌──────────────┐          │
│  │ Heronix-SIS  │────────▶│              │          │
│  │ (Desktop)    │         │              │          │
│  └──────────────┘         │              │          │
│                            │   SIS        │          │
│  ┌──────────────┐         │   Database   │          │
│  │ Heronix-     │────────▶│              │          │
│  │ Mobile       │         │  (Direct     │          │
│  │ (Tablets)    │         │   Access)    │          │
│  └──────────────┘         │              │          │
│                            └──────────────┘          │
│  ✅ Fast (direct connection)                         │
│  ✅ Real-time updates                                │
│  ✅ No review process needed                         │
│  ✅ Offline capable (school network always up)       │
└─────────────────────────────────────────────────────┘
```

### External Internet Applications (Via Staging Server)

```
┌─────────────────────────────────────────────────────┐
│                 PUBLIC INTERNET                      │
│                                                       │
│  ┌────────────────┐    ┌────────────────┐           │
│  │ Teacher        │    │ Parent/Student │           │
│  │ Portal         │    │ Portal         │           │
│  │ (Web App)      │    │ (Web App)      │           │
│  └────────┬───────┘    └───────┬────────┘           │
│           │                     │                    │
│           └──────────┬──────────┘                    │
│                      │                               │
└──────────────────────┼───────────────────────────────┘
                       │
                       │ HTTPS
                       ▼
┌─────────────────────────────────────────────────────┐
│           STAGING SERVER (DMZ)                       │
│                                                       │
│  • Validate inputs                                   │
│  • Scan for viruses                                  │
│  • Rate limiting                                     │
│  • Store in staging DB                               │
│  • Status: PENDING_REVIEW                            │
└──────────────────────┬──────────────────────────────┘
                       │
                       │ Internal Network Only
                       ▼
┌─────────────────────────────────────────────────────┐
│           SIS (SECURE NETWORK)                       │
│                                                       │
│  Admin Reviews → Approves → Imports to SIS Database  │
│                                                       │
│  ✅ Maximum security (isolated from internet)        │
│  ✅ Human review step                                │
│  ✅ Complete audit trail                             │
│  ⏱ Slower (review process: 1-2 days)                 │
└─────────────────────────────────────────────────────┘
```

---

## Data Flow Scenarios

### Scenario 1: Teacher Takes Attendance During Class (Heronix-Mobile)

```
8:00 AM - Teacher in classroom with school-provided iPad
    ↓
Opens Heronix-Mobile app
    ↓
Authenticates with fingerprint (biometric)
    ↓
Selects Period 2 - Math 101
    ↓
Marks students present/absent/tardy
    ↓
Clicks "Submit Attendance"
    ↓
App sends to SIS via school WiFi (direct connection)
    ↓
✅ Attendance recorded IMMEDIATELY in SIS database
    ↓
8:05 AM - Office staff can see attendance in SIS
    ↓
Parents receive absence notification at 8:10 AM (automated)
```

**Time from submission to SIS database: ~1 second**

---

### Scenario 2: Teacher Grades Essays at Home (Heronix-TeacherPortal)

```
Sunday 2:00 PM - Teacher at home on personal laptop
    ↓
Opens https://teachers.heronix.edu in Chrome
    ↓
Logs in with SIS credentials
    ↓
Navigates to "Submit Grades"
    ↓
Selects course: English 101, Assignment: Essay 1
    ↓
Enters grades for 28 students (scores 0-100)
    ↓
Clicks "Submit for Review"
    ↓
TeacherPortal sends to Staging Server via HTTPS
    ↓
Staging Server validates:
  • Teacher is assigned to English 101 ✓
  • All scores are 0-100 ✓
  • All students enrolled in course ✓
    ↓
Stores in staging database with status: PENDING_REVIEW
    ↓
Teacher receives confirmation: "Submission received (ID: SUB-12345)"
    ↓
Monday 8:00 AM - Admin opens SIS Review Dashboard
    ↓
Sees teacher's grade submission in "Pending" tab
    ↓
Reviews grades, approves
    ↓
Clicks "Import All Approved"
    ↓
SIS imports grades into production database
    ↓
Monday 8:15 AM - Grades visible in SIS
    ↓
Teacher receives email: "Your submission has been imported"
```

**Time from submission to SIS database: 18 hours (next business day)**

---

### Scenario 3: Parent Updates Phone Number (Heronix-Portal)

```
Wednesday 7:00 PM - Parent at home on phone
    ↓
Opens https://portal.heronix.edu in mobile browser
    ↓
Logs in with parent account credentials
    ↓
Navigates to "My Profile"
    ↓
Updates phone number: (555) 123-4567 → (555) 987-6543
    ↓
Clicks "Submit Changes"
    ↓
Portal sends update to Staging Server
    ↓
Staging Server:
  • Validates phone format ✓
  • Checks parent has access to student ✓
  • Stores in staging DB: parent_updates table
  • Status: PENDING_REVIEW
    ↓
Parent receives confirmation: "Update submitted for review"
    ↓
Thursday 9:00 AM - Admin reviews update in SIS
    ↓
Verifies change looks legitimate
    ↓
Approves update
    ↓
SIS imports: Updates Parent.phoneNumber in database
    ↓
Thursday 9:05 AM - New phone number active in SIS
    ↓
Parent receives email: "Your information has been updated"
```

**Time from submission to SIS database: 14 hours**

---

## Development Priority

### Phase 1 (Foundation): Staging Server
**Timeline**: Weeks 1-8
- Set up staging server infrastructure
- Implement security features
- Create admin review API
- Database schema and migrations

### Phase 2 (Teacher Access): Heronix-TeacherPortal
**Timeline**: Weeks 9-16
- Build web application (React + TypeScript)
- Implement grade submission
- Attendance recording
- Assignment management
- Integration with staging server

### Phase 3 (Parent Access): Heronix-Portal
**Timeline**: Weeks 17-24
- Build parent/student portal
- View grades and attendance
- Update contact information
- Messaging system
- Mobile-responsive design

### Phase 4 (On-Campus Mobile): Heronix-Mobile
**Timeline**: Weeks 25-32
- Build mobile app (React Native/Flutter)
- Optimize for tablets
- Offline support
- Biometric authentication
- Direct SIS integration

### Phase 5 (Public Registration): Online Registration
**Timeline**: Weeks 33-40
- Build public registration site
- Multi-step form wizard
- Document upload
- reCAPTCHA integration
- SEO optimization

---

## Summary: When to Use Each Application

| Situation | Use This Application | Why |
|-----------|---------------------|-----|
| Taking attendance during class | **Heronix-Mobile** | Real-time, on school tablet, direct to SIS |
| Grading papers at home on weekend | **Heronix-TeacherPortal** | Remote access, goes through review |
| Parent checking student grades | **Heronix-Portal** | Public internet access, secure portal |
| Admin entering new student | **Heronix-SIS** | Full control, direct database access |
| New family registering student | **Online Registration** | Public website, no login required |
| Teacher recording quiz scores in class | **Heronix-Mobile** | Fast, immediate, on school device |
| Teacher planning assignments from home | **Heronix-TeacherPortal** | Remote access, web browser |
| Student viewing homework assignments | **Heronix-Portal** | Student login, view-only access |

---

**End of ExPortal Applications Overview**
