# Heronix TeacherPortal - Complete Specifications

**Version:** 1.0.0
**Date:** December 27, 2025
**Purpose:** Secure web application for teachers to manage grades, attendance, and assignments from off-campus

---

## Executive Summary

**Heronix-TeacherPortal** is a dedicated web application that allows teachers to submit grades, manage assignments, and communicate with parents, students, other teachers, and administrators **from anywhere with internet access** (home, coffee shops, library, etc.). This is SEPARATE from the Heronix-Mobile app which runs only on school-provided devices within the school network.

### Application Comparison

| Feature | Heronix-Mobile | Heronix-TeacherPortal |
|---------|----------------|----------------------|
| **Access** | School network ONLY | Internet (anywhere) |
| **Device** | School-provided tablets/devices | Personal computers, laptops |
| **Connection** | Direct to SIS (internal network) | Via Staging Server (external) |
| **Security** | Internal network firewall | Staging server validation |
| **Use Case** | During school hours, on campus | After hours, from home |
| **Data Flow** | Direct to SIS database | Staging → Review → SIS |
| **Attendance** | ✅ Can take attendance | ❌ Cannot take attendance (school network only) |
| **Grades** | ✅ Quick entry (real-time) | ✅ Batch submission (with review) |
| **Communication** | Limited | ✅ Full messaging system |

### Key Benefits
- **Remote Access**: Teachers can work from home, library, or anywhere with internet
- **Security**: SIS database remains protected behind staging server
- **Validation**: All grade submissions validated before import
- **Communication Hub**: Message parents, students, teachers, and administrators
- **District Integration**: Communicate with district-level staff
- **Audit Trail**: Complete history of who submitted what and when
- **Offline Capability**: Progressive Web App (PWA) with offline support
- **Same Credentials**: Teachers use existing SIS credentials via staging server authentication
- **No School VPN Required**: Works on any device with a web browser

### IMPORTANT: Attendance Restriction
**Attendance can ONLY be taken using Heronix-Mobile on school-provided devices within the school network.** This ensures:
- Real-time attendance reporting (required by law in many jurisdictions)
- Prevents remote attendance fraud
- Ensures attendance is taken during actual class time
- Maintains data integrity with direct SIS connection

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                    TEACHER WORKFLOW                                  │
└─────────────────────────────────────────────────────────────────────┘

Teacher at Home/Off-Campus
    ↓
Opens browser → https://teachers.heronix.edu
    ↓
┌─────────────────────────────────────────────────────────────────────┐
│              HERONIX-TEACHERPORTAL (React/Vue Web App)               │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ Features:                                                      │  │
│  │  • Login with SIS credentials                                 │  │
│  │  • View assigned courses                                      │  │
│  │  • View class rosters                                         │  │
│  │  • Submit grades (assignments, midterms, finals)              │  │
│  │  • Record attendance (present, absent, tardy, excused)        │  │
│  │  • Create/edit assignments                                    │  │
│  │  • View submission history                                    │  │
│  │  • Offline mode (PWA with local cache)                        │  │
│  └───────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                    HTTPS (JWT Authentication)
                                │
┌───────────────────────────────▼─────────────────────────────────────┐
│              HERONIX STAGING SERVER                                  │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ Teacher Authentication Endpoint:                              │  │
│  │  POST /api/teacher/login                                      │  │
│  │  • Validates against SIS teacher credentials                  │  │
│  │  • Returns JWT token (1-hour expiration)                      │  │
│  │  • Logs login attempt (IP, timestamp)                         │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ Teacher Data Endpoints:                                        │  │
│  │  GET  /api/teacher/courses (assigned courses)                 │  │
│  │  GET  /api/teacher/roster/{courseId} (class roster)           │  │
│  │  POST /api/teacher/grades (submit grades)                     │  │
│  │  POST /api/teacher/attendance (submit attendance)             │  │
│  │  GET  /api/teacher/assignments/{courseId}                     │  │
│  │  POST /api/teacher/assignments (create assignment)            │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ Security & Validation:                                         │  │
│  │  • Verify teacher is assigned to course                       │  │
│  │  • Validate grade values (0-100, A-F, etc.)                   │  │
│  │  • Check student enrollment in course                         │  │
│  │  • Prevent duplicate submissions                              │  │
│  │  • Sanitize all inputs (SQL injection prevention)             │  │
│  │  • Rate limiting (prevent abuse)                              │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ Staging Database (teacher_submissions table):                 │  │
│  │  • submission_uuid, teacher_user_id, submission_type          │  │
│  │  • course_id, submission_data (JSON)                          │  │
│  │  • status: PENDING_REVIEW → APPROVED → IMPORTED               │  │
│  │  • submitted_at, reviewed_at, imported_at                     │  │
│  └───────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                    Internal Network / VPN Only
                                │
┌───────────────────────────────▼─────────────────────────────────────┐
│              HERONIX SIS (SECURE NETWORK)                            │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ Admin Review Dashboard:                                        │  │
│  │  • View pending teacher submissions                           │  │
│  │  • See: Teacher name, course, submission type, date           │  │
│  │  • Review grades/attendance data                              │  │
│  │  • Approve or reject submissions                              │  │
│  │  • Bulk approve for trusted teachers                          │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ Import Process:                                                │  │
│  │  1. Admin clicks "Import All Approved"                        │  │
│  │  2. SIS fetches approved submissions from staging             │  │
│  │  3. Creates/updates Grade records in SIS database             │  │
│  │  4. Creates/updates Attendance records                        │  │
│  │  5. Recalculates GPAs if needed                               │  │
│  │  6. Marks as IMPORTED on staging server                       │  │
│  │  7. Sends confirmation email to teacher                       │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ SIS Production Database:                                       │  │
│  │  • grades (final imported grades)                             │  │
│  │  • attendance_records (daily attendance)                      │  │
│  │  • assignments (assignment metadata)                          │  │
│  │  • NEVER directly accessible from internet                    │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Application Features

### 1. Authentication & Authorization

**Login Process:**
```
1. Teacher opens https://teachers.heronix.edu
2. Enters SIS username/password
3. Portal sends credentials to staging server
4. Staging server validates against SIS teacher database
5. Returns JWT token (1-hour expiration, refresh token for 24 hours)
6. Portal stores token in secure HttpOnly cookie
7. All subsequent requests include JWT in Authorization header
```

**Session Management:**
- JWT tokens expire after 1 hour (configurable)
- Refresh tokens valid for 24 hours
- Auto-refresh before expiration (seamless UX)
- Logout clears all tokens and local storage
- "Remember Me" option for 7-day sessions

**Role-Based Access:**
- Teachers can only access their assigned courses
- Department heads can view all courses in department
- Admin users (principals) have read-only access to all

---

### 2. Dashboard (Home Page)

**Quick Stats Card:**
```
┌─────────────────────────────────────────────────────────────┐
│  Welcome back, Mr. Johnson!                                  │
│                                                               │
│  📚 Active Courses: 5                                        │
│  👥 Total Students: 142                                      │
│  📝 Pending Submissions: 3                                   │
│  ✅ Imported This Week: 12                                   │
└─────────────────────────────────────────────────────────────┘
```

**Recent Activity:**
- Last 10 submissions with status (Pending, Approved, Imported)
- Submission type (Grades, Assignment)
- Course name
- Date submitted
- Import date (if approved)

**Unread Messages:**
- 📧 5 new messages from parents
- 💬 2 new messages from students
- 📢 1 district announcement

**Quick Actions:**
- Submit Grades
- Create Assignment
- Send Message
- View My Courses

---

### 3. My Courses (Course List)

**Course Cards:**
```
┌──────────────────────────────────────────────────────────────┐
│  MATH 101 - Algebra I                                         │
│  Section A | Period 2 | Room 204                              │
│                                                                │
│  👥 28 Students                                               │
│  📅 Semester 1 (2025-2026)                                    │
│                                                                │
│  [View Roster] [Submit Grades] [Send Message to Class]       │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  MATH 102 - Geometry                                          │
│  Section B | Period 4 | Room 204                              │
│                                                                │
│  👥 25 Students                                               │
│  📅 Semester 1 (2025-2026)                                    │
│                                                                │
│  [View Roster] [Submit Grades] [Send Message to Class]       │
└──────────────────────────────────────────────────────────────┘
```

**Filters:**
- Current semester / All semesters
- Active courses / Archived courses
- Search by course code or name

---

### 4. Class Roster (Student List)

**Table View:**
| Photo | Student Name      | Student ID | Grade Level | Email                     | Actions        |
|-------|------------------|------------|-------------|---------------------------|----------------|
| 📷    | Adams, John      | 2025-001   | 9th         | john.adams@student.edu    | View Profile   |
| 📷    | Brown, Sarah     | 2025-002   | 9th         | sarah.brown@student.edu   | View Profile   |
| 📷    | Chen, Michael    | 2025-003   | 9th         | michael.chen@student.edu  | View Profile   |

**Student Profile Modal:**
```
┌──────────────────────────────────────────────────────────────┐
│  Student Profile: John Adams                                  │
├──────────────────────────────────────────────────────────────┤
│  📷 [Student Photo]                                           │
│                                                                │
│  Student ID: 2025-001                                         │
│  Grade Level: 9th                                             │
│  Email: john.adams@student.edu                                │
│  Phone: (555) 123-4567                                        │
│                                                                │
│  Current Grades (This Course):                                │
│  • Quiz 1: 85%                                                │
│  • Homework 1: 90%                                            │
│  • Midterm: 88%                                               │
│  • Current Average: 87.7%                                     │
│                                                                │
│  Attendance (This Course):                                    │
│  • Present: 42 days                                           │
│  • Absent: 2 days                                             │
│  • Tardy: 1 day                                               │
│  • Attendance Rate: 93.3%                                     │
│                                                                │
│  [Close]                                                      │
└──────────────────────────────────────────────────────────────┘
```

---

### 5. Submit Grades

**Grade Entry Form:**

**Step 1: Select Assignment Type**
```
┌──────────────────────────────────────────────────────────────┐
│  Submit Grades - MATH 101 Section A                          │
├──────────────────────────────────────────────────────────────┤
│  Assignment Type:                                             │
│  ○ Quiz                                                       │
│  ○ Homework                                                   │
│  ○ Test                                                       │
│  ● Midterm Exam                                               │
│  ○ Final Exam                                                 │
│  ○ Project                                                    │
│  ○ Class Participation                                        │
│                                                                │
│  Assignment Name: *                                           │
│  [Midterm Exam - Chapter 1-5                           ]     │
│                                                                │
│  Date Assigned: [2025-12-01]    Due Date: [2025-12-15]       │
│                                                                │
│  Total Points: [100]                                          │
│                                                                │
│  Weight: [20%] of final grade                                 │
│                                                                │
│  [Next Step →]                                                │
└──────────────────────────────────────────────────────────────┘
```

**Step 2: Enter Grades**
```
┌──────────────────────────────────────────────────────────────┐
│  Enter Grades - Midterm Exam                                  │
├──────────────────────────────────────────────────────────────┤
│  Student Name          | Score | Letter Grade | Status       │
│  ──────────────────────┼───────┼──────────────┼───────────   │
│  Adams, John           | [85]  | B            | ✓ Valid      │
│  Brown, Sarah          | [92]  | A            | ✓ Valid      │
│  Chen, Michael         | [78]  | C+           | ✓ Valid      │
│  Davis, Emily          | [--]  | --           | ⚠ Absent     │
│  Evans, Robert         | [88]  | B+           | ✓ Valid      │
│                                                                │
│  ☑ Auto-calculate letter grades based on scale               │
│  ☑ Allow excused absences (marked as '--')                   │
│                                                                │
│  Grading Scale:                                               │
│  A: 90-100 | B: 80-89 | C: 70-79 | D: 60-69 | F: 0-59        │
│                                                                │
│  [← Back] [Save Draft] [Submit for Review →]                 │
└──────────────────────────────────────────────────────────────┘
```

**Step 3: Review & Submit**
```
┌──────────────────────────────────────────────────────────────┐
│  Review Submission                                            │
├──────────────────────────────────────────────────────────────┤
│  Course: MATH 101 - Algebra I (Section A)                    │
│  Assignment: Midterm Exam - Chapter 1-5                       │
│  Type: Midterm Exam                                           │
│  Date Assigned: December 1, 2025                              │
│  Due Date: December 15, 2025                                  │
│  Total Points: 100                                            │
│  Weight: 20% of final grade                                   │
│                                                                │
│  Grades Entered: 27 students                                  │
│  Excused Absences: 1 student                                  │
│                                                                │
│  Class Statistics:                                            │
│  • Highest Score: 98                                          │
│  • Lowest Score: 62                                           │
│  • Average Score: 84.3                                        │
│  • Median Score: 85                                           │
│                                                                │
│  ⚠ WARNING: Once submitted, grades will be pending admin     │
│  review. You can edit draft submissions before submitting.    │
│                                                                │
│  [← Back] [Save as Draft] [Submit for Review]                │
└──────────────────────────────────────────────────────────────┘
```

**Confirmation:**
```
┌──────────────────────────────────────────────────────────────┐
│  ✓ Submission Successful!                                     │
├──────────────────────────────────────────────────────────────┤
│  Your grade submission has been sent for review.              │
│                                                                │
│  Submission ID: SUB-2025-12345                                │
│  Status: PENDING_REVIEW                                       │
│                                                                │
│  You will receive an email notification when your submission  │
│  is approved and imported into the SIS.                       │
│                                                                │
│  Estimated review time: 1-2 business days                     │
│                                                                │
│  [View Submission History] [Submit More Grades] [Dashboard]  │
└──────────────────────────────────────────────────────────────┘
```

---

### 6. Record Attendance

**Daily Attendance Entry:**
```
┌──────────────────────────────────────────────────────────────┐
│  Record Attendance - MATH 101 Section A                       │
├──────────────────────────────────────────────────────────────┤
│  Date: [2025-12-27 ▼]    Period: 2                           │
│                                                                │
│  Student Name          | Status                               │
│  ──────────────────────┼─────────────────────────────────     │
│  Adams, John           | ● Present  ○ Absent  ○ Tardy  ○ Exc │
│  Brown, Sarah          | ● Present  ○ Absent  ○ Tardy  ○ Exc │
│  Chen, Michael         | ○ Present  ● Absent  ○ Tardy  ○ Exc │
│  Davis, Emily          | ○ Present  ○ Absent  ● Tardy  ○ Exc │
│  Evans, Robert         | ● Present  ○ Absent  ○ Tardy  ○ Exc │
│                                                                │
│  Quick Actions:                                               │
│  [Mark All Present] [Mark All Absent]                         │
│                                                                │
│  Summary:                                                     │
│  Present: 23 | Absent: 1 | Tardy: 1 | Excused: 0             │
│                                                                │
│  Notes (optional):                                            │
│  [Fire drill during period - 10 min late start          ]    │
│                                                                │
│  [Save Draft] [Submit for Review]                            │
└──────────────────────────────────────────────────────────────┘
```

**Bulk Entry (Week View):**
```
┌──────────────────────────────────────────────────────────────────────┐
│  Attendance - Week of December 23-27, 2025                            │
├──────────────────────────────────────────────────────────────────────┤
│  Student Name     | Mon 12/23 | Tue 12/24 | Wed 12/25 | Thu 12/26 | │
│  ─────────────────┼───────────┼───────────┼───────────┼─────────── │ │
│  Adams, John      |     P     |     P     |  HOLIDAY  |     P     | │
│  Brown, Sarah     |     P     |     P     |  HOLIDAY  |     P     | │
│  Chen, Michael    |     A     |     P     |  HOLIDAY  |     P     | │
│  Davis, Emily     |     T     |     P     |  HOLIDAY  |     P     | │
│                                                                       │
│  Legend: P = Present, A = Absent, T = Tardy, E = Excused            │
│                                                                       │
│  [Submit Week's Attendance]                                          │
└──────────────────────────────────────────────────────────────────────┘
```

---

### 7. Manage Assignments

**Assignment List:**
```
┌──────────────────────────────────────────────────────────────┐
│  Assignments - MATH 101 Section A                            │
├──────────────────────────────────────────────────────────────┤
│  [+ Create New Assignment]                                    │
│                                                                │
│  Active Assignments:                                          │
│                                                                │
│  📝 Homework 1: Linear Equations                              │
│     Assigned: Dec 1, 2025 | Due: Dec 8, 2025                 │
│     Points: 20 | Weight: 5% | Submissions: 25/28             │
│     [Edit] [View Submissions] [Delete]                       │
│                                                                │
│  📝 Quiz 2: Graphing                                          │
│     Assigned: Dec 10, 2025 | Due: Dec 10, 2025               │
│     Points: 50 | Weight: 10% | Submissions: 28/28            │
│     [Edit] [View Submissions] [Delete]                       │
│                                                                │
│  📝 Midterm Exam - Chapter 1-5                                │
│     Assigned: Dec 1, 2025 | Due: Dec 15, 2025                │
│     Points: 100 | Weight: 20% | Status: Pending Review       │
│     [View Details]                                            │
│                                                                │
│  Upcoming Assignments:                                        │
│                                                                │
│  📅 Final Exam - Comprehensive                                │
│     Assigned: Jan 5, 2026 | Due: Jan 20, 2026                │
│     Points: 200 | Weight: 30% | Status: Draft                │
│     [Edit] [Publish]                                          │
└──────────────────────────────────────────────────────────────┘
```

**Create Assignment:**
```
┌──────────────────────────────────────────────────────────────┐
│  Create New Assignment                                        │
├──────────────────────────────────────────────────────────────┤
│  Assignment Name: *                                           │
│  [Chapter 6 Homework - Quadratic Equations            ]      │
│                                                                │
│  Type: *                                                      │
│  [Homework ▼]                                                 │
│                                                                │
│  Description:                                                 │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Complete problems 1-25 on pages 145-147.              │  │
│  │ Show all work for full credit.                        │  │
│  │                                                        │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                                │
│  Date Assigned: [2025-12-27]                                  │
│  Due Date: [2026-01-05]                                       │
│                                                                │
│  Total Points: [20]                                           │
│  Weight: [5]% of final grade                                  │
│                                                                │
│  ☑ Publish immediately                                        │
│  ☐ Save as draft                                              │
│                                                                │
│  [Cancel] [Create Assignment]                                │
└──────────────────────────────────────────────────────────────┘
```

---

### 8. Submission History

**Submission List:**
```
┌──────────────────────────────────────────────────────────────────────────┐
│  My Submission History                                                    │
├──────────────────────────────────────────────────────────────────────────┤
│  Filters: [All Types ▼] [All Status ▼] [This Semester ▼]                │
│                                                                           │
│  ID          | Type       | Course     | Date      | Status    | Action │
│  ───────────┼────────────┼────────────┼───────────┼───────────┼─────── │
│  SUB-12345  | Grades     | MATH 101   | Dec 27    | ✅ Imported | View  │
│  SUB-12344  | Attendance | MATH 101   | Dec 26    | ✅ Imported | View  │
│  SUB-12343  | Grades     | MATH 102   | Dec 25    | ⏳ Pending  | Edit  │
│  SUB-12342  | Attendance | MATH 102   | Dec 24    | ✅ Approved | View  │
│  SUB-12341  | Grades     | MATH 101   | Dec 20    | ❌ Rejected | View  │
│                                                                           │
│  Legend:                                                                  │
│  ⏳ Pending Review | ✅ Approved (awaiting import) | ✅ Imported          │
│  ❌ Rejected (see notes) | 💾 Draft (not submitted)                      │
└──────────────────────────────────────────────────────────────────────────┘
```

**Submission Details (Rejected Example):**
```
┌──────────────────────────────────────────────────────────────┐
│  Submission Details: SUB-12341                                │
├──────────────────────────────────────────────────────────────┤
│  Status: ❌ REJECTED                                          │
│                                                                │
│  Course: MATH 101 - Algebra I (Section A)                    │
│  Type: Grades - Quiz 3                                        │
│  Submitted: December 20, 2025 at 3:45 PM                      │
│  Reviewed: December 21, 2025 at 9:30 AM                       │
│  Reviewed By: Dr. Smith (Principal)                           │
│                                                                │
│  Rejection Reason:                                            │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Grade values out of range. Several students have      │  │
│  │ scores listed as "105/100" which exceeds maximum.     │  │
│  │ Please correct extra credit to be listed separately   │  │
│  │ and resubmit.                                          │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                                │
│  [Resubmit with Corrections] [Contact Admin] [Close]         │
└──────────────────────────────────────────────────────────────┘
```

---

### 9. Reports & Analytics

**Grade Distribution Chart:**
```
┌──────────────────────────────────────────────────────────────┐
│  Grade Distribution - MATH 101 Section A                      │
├──────────────────────────────────────────────────────────────┤
│  Current Averages:                                            │
│                                                                │
│  A (90-100):  ████████████████░░░░░░  45% (12 students)      │
│  B (80-89):   ██████████████░░░░░░░░  35% (9 students)       │
│  C (70-79):   ████░░░░░░░░░░░░░░░░░░  10% (3 students)       │
│  D (60-69):   ██░░░░░░░░░░░░░░░░░░░░   5% (1 student)        │
│  F (0-59):    ██░░░░░░░░░░░░░░░░░░░░   5% (1 student)        │
│                                                                │
│  Class Average: 84.7%                                         │
│  Median: 86%                                                   │
│  Highest: 98%                                                  │
│  Lowest: 52%                                                   │
│                                                                │
│  [Export to PDF] [Print Report]                              │
└──────────────────────────────────────────────────────────────┘
```

**Attendance Summary:**
```
┌──────────────────────────────────────────────────────────────┐
│  Attendance Summary - All Courses                             │
├──────────────────────────────────────────────────────────────┤
│  This Semester:                                               │
│                                                                │
│  MATH 101 Section A:                                          │
│    Present: 95% | Absent: 3% | Tardy: 2%                     │
│                                                                │
│  MATH 102 Section B:                                          │
│    Present: 93% | Absent: 5% | Tardy: 2%                     │
│                                                                │
│  Students with Attendance Concerns (>5% absent):              │
│  • Chen, Michael (MATH 101): 8% absent rate                   │
│  • Davis, Emily (MATH 102): 12% absent rate                   │
│                                                                │
│  [View Detailed Report] [Export Data]                        │
└──────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Frontend (Web Application)

**Framework Choice (Select One):**

#### Option 1: React + TypeScript (Recommended)
- **React 18+**: Component-based UI framework
- **TypeScript**: Type-safe development
- **Vite**: Fast build tool and dev server
- **React Router**: Client-side routing
- **TanStack Query (React Query)**: Server state management
- **Zustand**: Local state management (lightweight)
- **Tailwind CSS**: Utility-first CSS framework
- **shadcn/ui**: Pre-built accessible components
- **React Hook Form**: Form validation
- **Recharts**: Charts and graphs
- **Axios**: HTTP client
- **date-fns**: Date manipulation

#### Option 2: Vue 3 + TypeScript
- **Vue 3**: Progressive JavaScript framework
- **TypeScript**: Type-safe development
- **Vite**: Build tool
- **Vue Router**: Routing
- **Pinia**: State management
- **Tailwind CSS**: Styling
- **Headless UI**: Accessible components
- **VeeValidate**: Form validation
- **Chart.js**: Data visualization
- **Axios**: HTTP client

### PWA Features
- **Service Workers**: Offline support
- **IndexedDB**: Local data caching
- **Web App Manifest**: Install as app
- **Push Notifications**: Grade approval alerts
- **Background Sync**: Submit when back online

### Security
- **JWT Storage**: Secure HttpOnly cookies
- **CSRF Protection**: Token validation
- **XSS Prevention**: Input sanitization
- **Content Security Policy**: Header configuration
- **HTTPS Only**: Enforce secure connections

---

## Project Structure (React + TypeScript)

```
H:\Heronix\Heronix-ExPortal\Heronix-TeacherPortal\
│
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.js
├── .env.development
├── .env.production
├── README.md
│
├── public/
│   ├── manifest.json              # PWA manifest
│   ├── service-worker.js          # Offline support
│   ├── icons/                     # App icons (various sizes)
│   └── robots.txt
│
├── src/
│   ├── main.tsx                   # App entry point
│   ├── App.tsx                    # Root component
│   ├── vite-env.d.ts
│   │
│   ├── config/
│   │   ├── api.config.ts          # API base URLs
│   │   └── constants.ts           # App constants
│   │
│   ├── types/
│   │   ├── auth.types.ts          # Authentication types
│   │   ├── course.types.ts        # Course types
│   │   ├── grade.types.ts         # Grade types
│   │   ├── attendance.types.ts    # Attendance types
│   │   └── submission.types.ts    # Submission types
│   │
│   ├── api/
│   │   ├── client.ts              # Axios instance with interceptors
│   │   ├── auth.api.ts            # Login, logout, refresh token
│   │   ├── courses.api.ts         # Get courses, rosters
│   │   ├── grades.api.ts          # Submit grades
│   │   ├── attendance.api.ts      # Submit attendance
│   │   └── submissions.api.ts     # View history
│   │
│   ├── hooks/
│   │   ├── useAuth.ts             # Authentication hook
│   │   ├── useCourses.ts          # Courses data hook
│   │   ├── useGrades.ts           # Grades submission hook
│   │   └── useOfflineSync.ts      # Offline sync hook
│   │
│   ├── stores/
│   │   ├── authStore.ts           # Auth state (Zustand)
│   │   └── offlineStore.ts        # Offline queue (Zustand)
│   │
│   ├── components/
│   │   ├── layout/
│   │   │   ├── Header.tsx         # Top navigation
│   │   │   ├── Sidebar.tsx        # Side menu
│   │   │   ├── Footer.tsx
│   │   │   └── Layout.tsx         # Main layout wrapper
│   │   │
│   │   ├── auth/
│   │   │   ├── LoginForm.tsx      # Login component
│   │   │   └── PrivateRoute.tsx   # Protected route wrapper
│   │   │
│   │   ├── courses/
│   │   │   ├── CourseCard.tsx     # Course display card
│   │   │   ├── CourseList.tsx     # List of courses
│   │   │   └── RosterTable.tsx    # Student roster table
│   │   │
│   │   ├── grades/
│   │   │   ├── GradeEntryForm.tsx # Grade submission form
│   │   │   ├── GradeTable.tsx     # Table for grade entry
│   │   │   └── GradeSummary.tsx   # Stats display
│   │   │
│   │   ├── attendance/
│   │   │   ├── AttendanceForm.tsx # Daily attendance
│   │   │   └── WeekView.tsx       # Week-at-a-glance
│   │   │
│   │   ├── submissions/
│   │   │   ├── SubmissionList.tsx # History list
│   │   │   └── SubmissionDetail.tsx # Detail modal
│   │   │
│   │   └── ui/
│   │       ├── Button.tsx         # Reusable button
│   │       ├── Input.tsx          # Form input
│   │       ├── Modal.tsx          # Modal dialog
│   │       ├── Table.tsx          # Data table
│   │       ├── Card.tsx           # Card container
│   │       └── Badge.tsx          # Status badge
│   │
│   ├── pages/
│   │   ├── LoginPage.tsx          # /login
│   │   ├── DashboardPage.tsx      # /dashboard
│   │   ├── CoursesPage.tsx        # /courses
│   │   ├── CourseDetailPage.tsx   # /courses/:id
│   │   ├── SubmitGradesPage.tsx   # /grades/submit
│   │   ├── AttendancePage.tsx     # /attendance
│   │   ├── AssignmentsPage.tsx    # /assignments
│   │   ├── SubmissionsPage.tsx    # /submissions
│   │   └── NotFoundPage.tsx       # 404
│   │
│   ├── utils/
│   │   ├── formatters.ts          # Date, number formatters
│   │   ├── validators.ts          # Input validation
│   │   ├── storage.ts             # LocalStorage helpers
│   │   └── gradeCalculations.ts   # GPA, letter grade calc
│   │
│   └── styles/
│       └── globals.css            # Global styles + Tailwind
│
└── tests/
    ├── unit/
    │   └── gradeCalculations.test.ts
    └── integration/
        └── submission.test.tsx
```

---

## API Integration

### Authentication Flow

**Login Request:**
```typescript
POST https://staging.heronix.edu/api/teacher/login
Content-Type: application/json

{
  "username": "johnson@school.edu",
  "password": "securePassword123"
}
```

**Login Response:**
```typescript
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "teacher": {
    "id": 123,
    "firstName": "Robert",
    "lastName": "Johnson",
    "email": "johnson@school.edu",
    "employeeId": "TCH-001",
    "department": "Mathematics"
  }
}
```

### Fetching Courses

**Request:**
```typescript
GET https://staging.heronix.edu/api/teacher/courses
Authorization: Bearer <accessToken>
```

**Response:**
```typescript
{
  "courses": [
    {
      "id": 101,
      "courseCode": "MATH-101",
      "courseName": "Algebra I",
      "section": "A",
      "period": 2,
      "room": "204",
      "semester": "Fall 2025",
      "studentCount": 28,
      "schedule": {
        "days": ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"],
        "startTime": "09:00",
        "endTime": "09:50"
      }
    }
  ]
}
```

### Submitting Grades

**Request:**
```typescript
POST https://staging.heronix.edu/api/teacher/grades
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "courseId": 101,
  "assignmentName": "Midterm Exam - Chapter 1-5",
  "assignmentType": "MIDTERM",
  "dateAssigned": "2025-12-01",
  "dateDue": "2025-12-15",
  "totalPoints": 100,
  "weight": 20,
  "grades": [
    {
      "studentId": 2025001,
      "score": 85,
      "notes": ""
    },
    {
      "studentId": 2025002,
      "score": 92,
      "notes": ""
    },
    {
      "studentId": 2025003,
      "score": null,
      "notes": "Excused absence - makeup scheduled"
    }
  ]
}
```

**Response:**
```typescript
{
  "success": true,
  "submissionId": "SUB-2025-12345",
  "status": "PENDING_REVIEW",
  "message": "Grade submission received and pending admin review",
  "estimatedReviewTime": "1-2 business days"
}
```

---

## Offline Support (PWA)

### Service Worker Strategy

**Cache-First for Static Assets:**
- HTML, CSS, JavaScript files
- Images, fonts, icons
- Cached during installation

**Network-First for API Calls:**
- Always try network first
- Fall back to cache if offline
- Queue mutations for background sync

**Background Sync:**
```typescript
// When offline, queue grade submissions
if (!navigator.onLine) {
  await offlineQueue.add({
    type: 'SUBMIT_GRADES',
    data: gradeSubmission,
    timestamp: Date.now()
  });

  // Show user feedback
  toast.info('You are offline. Submission will be sent when connection is restored.');
}

// When back online, process queue
navigator.addEventListener('online', async () => {
  const queuedItems = await offlineQueue.getAll();

  for (const item of queuedItems) {
    await apiClient.post('/teacher/grades', item.data);
    await offlineQueue.remove(item.id);
  }
});
```

---

## Security Considerations

### Input Validation

**Client-Side (React Hook Form):**
```typescript
const gradeSchema = z.object({
  score: z.number()
    .min(0, 'Score cannot be negative')
    .max(100, 'Score cannot exceed 100')
    .nullable(),
  notes: z.string()
    .max(500, 'Notes cannot exceed 500 characters')
    .optional()
});
```

**Server-Side (Staging Server):**
```java
@PostMapping("/api/teacher/grades")
public ResponseEntity<?> submitGrades(
    @Valid @RequestBody GradeSubmissionRequest request,
    Authentication authentication) {

    // 1. Verify teacher is assigned to course
    // 2. Validate grade values (0-100 or null)
    // 3. Check students are enrolled
    // 4. Sanitize inputs
    // 5. Store in staging database
}
```

### Rate Limiting

**Prevent Abuse:**
- Max 100 grade submissions per day per teacher
- Max 50 API requests per minute per user
- Max 10 login attempts per hour per IP

---

## Deployment Strategy

### Development Environment
```bash
cd H:\Heronix\Heronix-ExPortal\Heronix-TeacherPortal
npm install
npm run dev
# Opens at http://localhost:5173
```

### Production Build
```bash
npm run build
# Outputs to dist/ folder
# Deploy to static hosting (Netlify, Vercel, AWS S3 + CloudFront)
```

### Docker Deployment
```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## Development Roadmap

### Phase 1: Foundation (Weeks 1-2)
- [ ] Project setup (Vite + React + TypeScript)
- [ ] Authentication system (login, JWT storage)
- [ ] Layout components (header, sidebar, footer)
- [ ] API client setup (Axios with interceptors)

### Phase 2: Core Features (Weeks 3-5)
- [ ] Dashboard page (stats, recent activity)
- [ ] Course list and detail pages
- [ ] Student roster view
- [ ] Grade submission form (single assignment)

### Phase 3: Advanced Features (Weeks 6-8)
- [ ] Attendance recording (daily + bulk)
- [ ] Assignment management
- [ ] Submission history view
- [ ] Reports and analytics

### Phase 4: PWA & Offline (Weeks 9-10)
- [ ] Service worker setup
- [ ] Offline caching strategy
- [ ] Background sync for submissions
- [ ] Push notifications

### Phase 5: Testing & Polish (Weeks 11-12)
- [ ] Unit tests (React Testing Library)
- [ ] Integration tests
- [ ] Accessibility audit (WCAG 2.1)
- [ ] Performance optimization
- [ ] Security audit

### Phase 6: Deployment (Week 13)
- [ ] Production build
- [ ] Docker containerization
- [ ] SSL certificate setup
- [ ] Monitoring integration (Sentry, Analytics)

---

**End of TeacherPortal Specifications**

**Next Steps:**
1. Create project folder structure
2. Initialize React + TypeScript project
3. Set up API integration with staging server
4. Begin UI development
