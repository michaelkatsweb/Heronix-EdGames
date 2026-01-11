# Heronix-TeacherPortal - Updated Features List

**Version:** 1.1.0
**Date:** December 27, 2025
**Update:** Removed attendance features, added communication system

---

## Core Features

### ❌ REMOVED: Attendance Recording
**Attendance can ONLY be taken using Heronix-Mobile on school-provided devices within the school network.**

Reasons:
- Real-time attendance reporting required by law
- Prevents remote attendance fraud
- Ensures attendance taken during actual class time
- Maintains data integrity with direct SIS connection

---

### ✅ INCLUDED Features

## 1. Grade Submission & Management
- Submit grades for assignments, quizzes, tests, projects
- Batch grade entry for entire class
- Draft mode (save and continue later)
- Submit for admin review
- View submission status (Pending, Approved, Rejected, Imported)
- Resubmit rejected grades with corrections
- Grade statistics and analytics

## 2. Assignment Management
- Create new assignments with due dates
- Edit existing assignments
- Set point values and grade weights
- Publish assignments to students
- Save as draft (not visible to students yet)
- View assignment submission stats
- Attach files/documents to assignments

## 3. Communication System (NEW)

### Message Parents
- Send message to individual parent
- Send message to all parents in a class
- Reply to parent messages
- View message history
- Attach files (permission slips, study guides)
- Message templates (progress reports, concerns, congratulations)
- Read receipts

### Message Students
- Send announcements to entire class
- Direct message individual students
- Remind students about upcoming assignments
- Share resources and links
- Set message priority (normal, important, urgent)

### Message Other Teachers
- Collaborate with department members
- Discuss student concerns (with permissions)
- Share teaching resources
- Coordinate cross-curricular projects
- Department announcements

### Message Administrators
- Report student concerns to guidance counselors
- Communicate with principal/vice principal
- Request meetings
- Submit absence requests
- Ask policy questions

### District Communication
- Receive district-wide announcements
- Access district resources
- Submit reports to district staff
- Professional development notifications
- District calendar events

## 4. Class Roster & Student Info
- View class rosters (read-only)
- See student photos
- View student contact information
- View parent/guardian contacts
- See student schedules
- View current grades (read-only for other teachers' classes)

## 5. Course Management
- View assigned courses
- See course schedules
- Access course materials
- View semester calendars
- Track course statistics

## 6. Reports & Analytics
- Grade distribution charts
- Class performance trends
- Assignment completion rates
- Student progress tracking
- Export data to PDF/Excel
- Generate progress reports

## 7. Submission History
- View all grade submissions
- Filter by course, date, status
- Track approval status
- See admin review notes
- Resubmit corrections

## 8. Notifications
- Push notifications for message replies
- Email notifications for grade approvals/rejections
- Assignment due date reminders
- District announcements alerts
- Configurable notification preferences

## 9. Offline Support (PWA)
- Work offline on grades
- Queue submissions for when online
- Cached student rosters
- Sync when connection restored
- Install as desktop/mobile app

---

## Feature Comparison: Heronix-Mobile vs Heronix-TeacherPortal

| Feature | Heronix-Mobile (On-Campus) | Heronix-TeacherPortal (Remote) |
|---------|---------------------------|--------------------------------|
| **Take Attendance** | ✅ YES (real-time) | ❌ NO (security/integrity) |
| **Submit Grades** | ✅ YES (immediate) | ✅ YES (with review) |
| **Create Assignments** | ✅ YES | ✅ YES |
| **View Rosters** | ✅ YES | ✅ YES (read-only) |
| **Message Parents** | ⚠️ Limited | ✅ FULL messaging system |
| **Message Students** | ⚠️ Limited | ✅ FULL messaging system |
| **Message Teachers** | ⚠️ Limited | ✅ FULL messaging system |
| **District Communication** | ❌ NO | ✅ YES |
| **Work Offline** | ✅ YES (local sync) | ✅ YES (PWA) |
| **Access Location** | School only | Anywhere |
| **Device Type** | School tablets | Personal devices |

---

## Updated Workflow Example

### Teacher's Typical Week Using Both Applications

**Monday Morning (8:00 AM) - At School**
- Uses **Heronix-Mobile** on school tablet
- Takes attendance for Period 2 - Math 101
- Marks 2 students absent, 1 tardy
- Attendance instantly recorded in SIS
- Parents automatically notified of absences

**Monday Evening (7:00 PM) - At Home**
- Opens **Heronix-TeacherPortal** on personal laptop
- Grades weekend essays (28 students)
- Submits grades for admin review
- Creates assignment for next week
- Sends message to parents about upcoming field trip

**Tuesday Morning (8:00 AM) - At School**
- Uses **Heronix-Mobile** on school tablet
- Takes attendance for all periods
- Quick grade entry for pop quiz given during class

**Tuesday Evening (6:00 PM) - At Home**
- Opens **Heronix-TeacherPortal**
- Receives message from parent about student absence
- Replies to parent message
- Checks that Monday's grade submission was approved
- Prepares next assignment

**Wednesday Morning - District Meeting (Remote)**
- Uses **Heronix-TeacherPortal** from district office
- Receives district announcement about new policy
- Messages department head about curriculum change
- Cannot take attendance (not at school)

---

## Communication Feature Details

### Messaging Interface

```
┌──────────────────────────────────────────────────────────────┐
│  Messages                                         [Compose]   │
├──────────────────────────────────────────────────────────────┤
│  Inbox (12) | Sent | Drafts (2) | Starred                    │
│                                                                │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ 📧 From: Sarah Johnson (Parent)              Unread    │  │
│  │    Re: Math 101 - Extra Help                           │  │
│  │    "Can my son stay after school for..."               │  │
│  │    2 hours ago                                         │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                                │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ 💬 From: John Adams (Student)                Read      │  │
│  │    Question about Assignment 5                         │  │
│  │    "I don't understand problem #12..."                 │  │
│  │    Yesterday at 4:30 PM                                │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                                │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ 📢 From: District Office                     Read      │  │
│  │    Professional Development Day - Feb 15               │  │
│  │    "All teachers are required to attend..."            │  │
│  │    Monday at 8:00 AM                                   │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### Compose Message

```
┌──────────────────────────────────────────────────────────────┐
│  Compose New Message                                   [×]   │
├──────────────────────────────────────────────────────────────┤
│  To: [Select Recipients ▼]                                   │
│      ○ Individual Parent                                      │
│      ○ All Parents in Class                                   │
│      ● Individual Student                                     │
│      ○ All Students in Class                                  │
│      ○ Another Teacher                                        │
│      ○ Administrator                                          │
│                                                                │
│  Course: [MATH 101 - Algebra I ▼]                            │
│                                                                │
│  Student: [Select student... ▼]                              │
│                                                                │
│  Subject: *                                                   │
│  [Great progress this week!                           ]      │
│                                                                │
│  Message: *                                                   │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Hi John,                                               │  │
│  │                                                        │  │
│  │ I wanted to let you know that you've made great       │  │
│  │ progress this week. Your quiz score improved          │  │
│  │ significantly!                                         │  │
│  │                                                        │  │
│  │ Keep up the good work!                                 │  │
│  │                                                        │  │
│  │ Mr. Johnson                                            │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                                │
│  Attachments: [+ Add File]                                   │
│                                                                │
│  Priority: ○ Normal  ● Important  ○ Urgent                   │
│                                                                │
│  ☐ Send copy to parent/guardian                              │
│  ☐ Request read receipt                                       │
│                                                                │
│  [Save Draft] [Send Message]                                 │
└──────────────────────────────────────────────────────────────┘
```

### Message Templates

```
┌──────────────────────────────────────────────────────────────┐
│  Message Templates                                            │
├──────────────────────────────────────────────────────────────┤
│  Quick messages for common scenarios                          │
│                                                                │
│  📝 Congratulations on Improvement                            │
│     "I wanted to let you know that [student name] has..."     │
│     [Use Template]                                            │
│                                                                │
│  ⚠️ Concerns About Performance                                │
│     "I'm writing to discuss some concerns about..."           │
│     [Use Template]                                            │
│                                                                │
│  📚 Missing Assignment Reminder                               │
│     "This is a friendly reminder that [assignment]..."        │
│     [Use Template]                                            │
│                                                                │
│  🎉 Achievement Recognition                                   │
│     "Congratulations! [Student name] achieved..."             │
│     [Use Template]                                            │
│                                                                │
│  📅 Upcoming Test Notification                                │
│     "This is to inform you that we will have a test on..."    │
│     [Use Template]                                            │
│                                                                │
│  [+ Create Custom Template]                                   │
└──────────────────────────────────────────────────────────────┘
```

---

## Updated API Endpoints (Staging Server)

### Communication Endpoints

**Send Message to Parent:**
```
POST /api/teacher/messages/send
{
  "recipientType": "PARENT",
  "studentId": 2025001,
  "subject": "Great progress this week",
  "message": "Hi, I wanted to let you know...",
  "priority": "NORMAL",
  "attachments": []
}
```

**Send Message to Student:**
```
POST /api/teacher/messages/send
{
  "recipientType": "STUDENT",
  "studentId": 2025001,
  "subject": "Reminder: Assignment due tomorrow",
  "message": "Don't forget that Assignment 5...",
  "priority": "IMPORTANT"
}
```

**Send Message to Class (Broadcast):**
```
POST /api/teacher/messages/broadcast
{
  "courseId": 101,
  "recipientType": "ALL_STUDENTS",
  "subject": "Field trip next week",
  "message": "Don't forget permission slips...",
  "attachments": ["permission_slip.pdf"]
}
```

**Get Messages:**
```
GET /api/teacher/messages?folder=inbox&unread=true
```

**Reply to Message:**
```
POST /api/teacher/messages/{messageId}/reply
{
  "message": "Thank you for reaching out..."
}
```

---

## Updated Project Structure

```
src/
├── pages/
│   ├── DashboardPage.tsx
│   ├── CoursesPage.tsx
│   ├── SubmitGradesPage.tsx
│   ├── AssignmentsPage.tsx
│   ├── MessagesPage.tsx          # NEW - Messaging interface
│   ├── ComposeMessagePage.tsx     # NEW - Compose message
│   └── SubmissionsPage.tsx
│
├── components/
│   ├── messaging/                 # NEW - Messaging components
│   │   ├── MessageList.tsx
│   │   ├── MessageCard.tsx
│   │   ├── ComposeForm.tsx
│   │   ├── RecipientSelector.tsx
│   │   ├── MessageThread.tsx
│   │   └── TemplateSelector.tsx
│   │
│   ├── grades/
│   │   ├── GradeEntryForm.tsx
│   │   └── GradeSummary.tsx
│   │
│   └── assignments/
│       ├── AssignmentForm.tsx
│       └── AssignmentList.tsx
```

---

## Security Considerations for Messaging

### Message Validation
- All messages sanitized for XSS prevention
- Attachment file types whitelisted (PDF, DOCX, XLSX only)
- Max attachment size: 10MB
- Rate limiting: Max 100 messages per day per teacher
- Profanity filter for student messages
- Admin can review flagged messages

### Privacy Protection
- Messages stored encrypted at rest
- Teachers can only message students in their courses
- Parents must be linked to student
- No cross-course messaging without admin approval
- FERPA compliance for student data
- Audit log of all messages

### Message Routing
- Messages go through staging server
- Admin can moderate flagged messages
- District messages bypass teacher portal (read-only)
- Emergency messages (lockdown, etc.) prioritized

---

**End of Feature Update**
