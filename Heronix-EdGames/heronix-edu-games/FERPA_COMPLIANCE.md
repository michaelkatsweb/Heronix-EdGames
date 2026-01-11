# FERPA Compliance Guide for Heronix Educational Games Platform

## Overview

This document outlines how the Heronix Educational Games Platform complies with the Family Educational Rights and Privacy Act (FERPA), a federal law that protects the privacy of student education records.

## What is FERPA?

FERPA (20 U.S.C. § 1232g) gives parents/eligible students rights regarding education records:
- Right to inspect and review records
- Right to seek amendment of records
- Right to consent to disclosures
- Right to file complaints with the U.S. Department of Education

## Compliance Requirements

### 1. Definition of Education Records

Under FERPA, "education records" include records that are:
1. Directly related to a student
2. Maintained by an educational agency or institution

**In Heronix Platform**:
- Game scores and progress are education records
- Student performance data is protected
- Attendance/participation logs are records

### 2. What We Collect (Data Minimization)

✅ **Collected**:
- Student ID (internal school ID, NOT SSN)
- First name and last initial
- Grade level
- Class/teacher assignment
- Game scores and completion status
- Play time and frequency
- Device identifier (for approved devices)
- Timestamps of activity

❌ **NOT Collected**:
- Full names (last name is initial only)
- Social Security Numbers
- Birth dates
- Home addresses
- Parent/guardian information
- Demographic data (race, ethnicity, religion)
- Financial information
- Medical/health information
- Disciplinary records
- Photos or biometric data

### 3. Consent Requirements

#### For Students Under 13 (COPPA)
- Verifiable parental consent required
- Consent form includes:
  - Purpose of data collection
  - Types of data collected
  - How data is used
  - Data retention period
  - Parent rights
  - How to revoke consent

#### For Students 13+ (FERPA)
- School can provide consent as educational official
- Or parental consent if school policy requires
- Students 18+ can consent for themselves

#### Implementation in Platform:
```java
public class ConsentManager {
    // Track consent status per student
    // Prevent data collection without consent
    // Allow consent withdrawal
    // Generate consent reports for compliance
}
```

### 4. Access Controls

#### Who Can Access Student Data:

✅ **School Officials with Legitimate Educational Interest**:
- Assigned teachers
- School administrators
- IT support staff (limited access)

✅ **Parents/Eligible Students**:
- View their own records
- Request corrections
- Request deletion

❌ **Cannot Access**:
- Other students
- Teachers not assigned to student
- External parties without consent
- Marketing companies
- Third-party services

#### Technical Implementation:
```sql
-- Role-based access control
CREATE TABLE user_roles (
    user_id VARCHAR(50),
    role VARCHAR(20),
    school_id VARCHAR(50),
    class_id VARCHAR(50),
    PRIMARY KEY (user_id, role)
);

-- Teacher can only access their assigned students
SELECT s.* FROM students s
JOIN class_enrollment ce ON s.student_id = ce.student_id
JOIN classes c ON ce.class_id = c.class_id
WHERE c.teacher_id = ?
  AND s.student_id = ?;
```

### 5. Data Security

#### Technical Safeguards:
- **Encryption at Rest**: Local databases encrypted with AES-256
- **Encryption in Transit**: TLS 1.3 for all network communication
- **Access Logging**: All data access logged with timestamp and user
- **Device Authentication**: Only approved devices can sync
- **Session Management**: Timeout after inactivity
- **Password Requirements**: Strong password policy for teacher accounts

#### Physical Safeguards:
- Server in secure location (school IT room)
- Backup media secured
- Disposal procedures for old devices

#### Administrative Safeguards:
- Staff training on FERPA requirements
- Documented policies and procedures
- Regular security audits
- Incident response plan

### 6. Data Retention and Disposal

#### Retention Policy:
```
Active Data:     Current school year
Archive:         Current year + 1 year
Purge:           After 2 years from creation
```

#### Automatic Purge:
```java
@Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
public void purgeExpiredRecords() {
    LocalDate cutoffDate = LocalDate.now().minusYears(2);
    scoreRepository.deleteByDateBefore(cutoffDate);
    auditLog.log("Automatic purge of records before " + cutoffDate);
}
```

#### Secure Disposal:
- Database records: Secure deletion with overwrite
- Backup media: Physical destruction or secure wipe
- Paper records: Shredding
- Decommissioned devices: Data wipe per NIST guidelines

### 7. Parent/Student Rights

#### Right to Inspect and Review:
```java
// Export student data
public StudentDataExport exportStudentData(String studentId) {
    return StudentDataExport.builder()
        .studentInfo(getStudentInfo(studentId))
        .gameScores(getGameScores(studentId))
        .activityLog(getActivityLog(studentId))
        .deviceInfo(getDeviceInfo(studentId))
        .build();
}
```

#### Right to Request Amendment:
- Process for requesting corrections
- Response within 45 days
- Hearing process if request denied

#### Right to Opt-Out:
```java
public void optOutStudent(String studentId, String reason) {
    student.setOptOut(true);
    student.setOptOutDate(LocalDate.now());
    student.setOptOutReason(reason);
    
    // Stop collecting new data
    // Archive existing data
    // Notify relevant teachers
}
```

#### Right to Delete:
```java
public void deleteStudentData(String studentId, boolean verified) {
    if (!verified) {
        throw new SecurityException("Parent/student identity not verified");
    }
    
    // Delete all student records
    scoreRepository.deleteByStudentId(studentId);
    activityRepository.deleteByStudentId(studentId);
    deviceRepository.deleteByStudentId(studentId);
    
    // Log deletion for audit
    auditLog.log("Student data deleted: " + studentId);
}
```

### 8. Directory Information

Some information can be disclosed without consent if designated as "directory information":
- Student name
- Participation in activities
- Honors and awards

**In Heronix Platform**:
- NO automatic disclosure of any information
- Leaderboards use pseudonyms or require opt-in
- Public displays require explicit consent

### 9. Disclosure Without Consent (Exceptions)

FERPA allows disclosure without consent to:
- School officials with legitimate educational interest ✅
- Schools to which student is transferring ✅
- For audit or evaluation purposes ✅
- In health/safety emergencies ✅
- Pursuant to court order ✅

**Platform Implementation**:
- Document all disclosures
- Notify parent when possible
- Limit information to minimum necessary

### 10. Audit Trail

#### Required Logging:
```java
public class AuditLog {
    @Data
    public static class AuditEntry {
        private String timestamp;
        private String userId;
        private String action;
        private String studentId;
        private String ipAddress;
        private String result;
    }
    
    // Log every access to student data
    public void logAccess(String userId, String action, 
                         String studentId, String result);
    
    // Log every modification
    public void logModification(String userId, String action,
                               String studentId, String before, String after);
    
    // Log every disclosure
    public void logDisclosure(String userId, String reason,
                             String studentId, String recipient);
}
```

#### Audit Log Retention:
- Keep for minimum 3 years
- Protected from modification
- Available for review by education officials

### 11. Third-Party Service Providers

**Current Status**: 
- Heronix platform does NOT use third-party services
- All data stored on school's local network
- No cloud services
- No analytics services
- No advertising networks

**If Future Third-Party Integration**:
- Must have written agreement (FERPA § 99.31(a)(1))
- Agreement must specify:
  - Purpose of disclosure
  - That data cannot be re-disclosed
  - Data must be destroyed when no longer needed
  - Compliance with FERPA requirements

### 12. Teacher Training Requirements

All teachers and staff must be trained on:
- FERPA basics and student rights
- What constitutes an education record
- Proper handling of student data
- Access controls and authentication
- Incident reporting procedures
- Annual refresher training

### 13. Incident Response

In case of data breach:
1. **Immediate Response** (0-24 hours):
   - Contain the breach
   - Secure systems
   - Document the incident

2. **Assessment** (1-3 days):
   - Determine scope of breach
   - Identify affected students
   - Assess risk to students

3. **Notification** (Within 30 days):
   - Notify affected parents/students
   - Notify school administration
   - File report with state/federal authorities if required
   - Provide credit monitoring if SSNs exposed (N/A for Heronix)

4. **Remediation**:
   - Implement security improvements
   - Review and update policies
   - Additional staff training

### 14. State-Specific Requirements

Many states have additional requirements beyond FERPA:
- **California**: SOPIPA (Student Online Personal Information Protection Act)
- **New York**: Education Law §2-d
- **Illinois**: Student Online Personal Protection Act
- **Connecticut**: Data Privacy Act

**Platform Approach**:
- Comply with strictest requirements
- Configurable based on state
- Regular review of state laws

### 15. Documentation Requirements

Maintain documentation of:
- ✅ Privacy policies
- ✅ Consent forms
- ✅ Data handling procedures
- ✅ Access control policies
- ✅ Training records
- ✅ Audit logs
- ✅ Incident reports
- ✅ Data sharing agreements (if any)

### 16. Annual Review

Conduct annual review of:
- Privacy policies
- Security measures
- Access controls
- Consent status
- Data retention compliance
- Training completeness
- Audit log review

## Implementation Checklist

### Before Launch:
- [ ] Privacy policy created and approved by school legal counsel
- [ ] Consent forms prepared and translated if needed
- [ ] Staff training completed
- [ ] Access controls implemented and tested
- [ ] Encryption verified for data at rest and in transit
- [ ] Audit logging functional
- [ ] Incident response plan documented
- [ ] Data retention policy configured
- [ ] Parent notification prepared

### After Launch:
- [ ] Collect and track consent
- [ ] Regular security audits
- [ ] Monitor audit logs
- [ ] Process parent requests promptly
- [ ] Annual policy review
- [ ] Staff refresher training
- [ ] Test incident response plan

## Resources

- **FERPA Statute**: 20 U.S.C. § 1232g
- **FERPA Regulations**: 34 CFR Part 99
- **U.S. Department of Education FERPA Office**: 
  - Phone: 1-800-USA-LEARN
  - Email: FERPA@ed.gov
  - Website: https://studentprivacy.ed.gov

## Contact for Compliance Questions

For questions about FERPA compliance in the Heronix platform:
- School Privacy Officer: [Contact Info]
- District Legal Counsel: [Contact Info]
- Platform Support: [Contact Info]

---

**Note**: This guide provides general information about FERPA compliance. Schools should consult with their own legal counsel to ensure compliance with all applicable laws and regulations.
