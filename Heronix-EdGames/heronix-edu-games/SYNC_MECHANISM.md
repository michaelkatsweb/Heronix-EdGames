# Sync Mechanism Design

## Overview

The Heronix platform implements an offline-first architecture where students can play games without network connectivity. When devices reconnect to the school network, a synchronization process updates the central server with student progress and downloads any new assignments or updates.

## Design Goals

1. **Reliable**: Handle network interruptions gracefully
2. **Conflict-Free**: Resolve conflicts automatically without data loss
3. **Efficient**: Minimize bandwidth and processing time
4. **Transparent**: Sync happens automatically in background
5. **Secure**: Authenticate and encrypt all communications

## Sync Architecture

### Components

```
┌─────────────────┐         ┌──────────────────┐
│  Client Device  │◄────────►│  School Server   │
│                 │  HTTPS   │                  │
│  ┌──────────┐  │          │   ┌──────────┐   │
│  │ Local DB │  │          │   │ Central  │   │
│  └──────────┘  │          │   │ Database │   │
│  ┌──────────┐  │          │   └──────────┘   │
│  │  Sync    │  │          │   ┌──────────┐   │
│  │  Queue   │  │          │   │  Sync    │   │
│  └──────────┘  │          │   │ Service  │   │
└─────────────────┘          │   └──────────┘   │
                             └──────────────────┘
```

### Data Flow

1. **Offline Play**: Student plays game → Score saved to local DB → Added to sync queue
2. **Network Detection**: App detects school network connection
3. **Authentication**: Device authenticates with server using auth token
4. **Upload Phase**: Client uploads pending scores and progress
5. **Download Phase**: Client downloads new assignments and game updates
6. **Conflict Resolution**: Server resolves any conflicts
7. **Completion**: Client updates local database and clears sync queue

## Sync Protocol

### 1. Connection Detection

The client continuously monitors network status:

```java
public class NetworkMonitor {
    private static final String SERVER_URL = "http://school-server:8080";
    private static final int CHECK_INTERVAL_MS = 30000; // 30 seconds
    
    public boolean isSchoolNetworkAvailable() {
        try {
            HttpURLConnection connection = 
                (HttpURLConnection) new URL(SERVER_URL + "/api/ping").openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (IOException e) {
            return false;
        }
    }
}
```

### 2. Authentication

Before syncing, device must authenticate:

```
POST /api/auth/device
Headers:
  Content-Type: application/json
Body:
{
  "deviceId": "uuid-device-id",
  "studentId": "STUDENT001",
  "authToken": "encrypted-token"
}

Response:
{
  "success": true,
  "sessionToken": "session-xyz",
  "expiresAt": "2025-01-09T18:00:00Z"
}
```

### 3. Upload Phase

#### A. Get Server's Last Sync Timestamp

```
GET /api/sync/last-sync?deviceId={deviceId}

Response:
{
  "lastSyncAt": "2025-01-09T10:00:00Z",
  "serverTimestamp": "2025-01-09T15:30:00Z"
}
```

#### B. Upload Pending Scores

Client sends all scores created/modified since last sync:

```
POST /api/sync/upload
Headers:
  Content-Type: application/json
  Authorization: Bearer session-xyz
Body:
{
  "deviceId": "uuid-device-id",
  "uploadTimestamp": "2025-01-09T15:30:00Z",
  "scores": [
    {
      "scoreId": "score-uuid-1",
      "studentId": "STUDENT001",
      "gameId": "math-sprint",
      "score": 85,
      "maxScore": 100,
      "correctAnswers": 17,
      "incorrectAnswers": 3,
      "timeSeconds": 120,
      "completionPercentage": 100,
      "completed": true,
      "difficultyLevel": "MEDIUM",
      "playedAt": "2025-01-09T14:00:00Z",
      "deviceId": "uuid-device-id",
      "metadata": "{}"
    },
    // ... more scores
  ]
}

Response:
{
  "success": true,
  "scoresProcessed": 5,
  "conflicts": [],
  "syncId": "sync-uuid"
}
```

### 4. Conflict Resolution

#### Conflict Types and Resolution

**Type 1: Duplicate Score** (same scoreId exists on server)
- Resolution: Server wins (score already synced)
- Action: Mark client score as synced, don't overwrite

**Type 2: Multiple Scores for Same Game Session** (unlikely but possible)
- Resolution: Keep highest score
- Action: Flag lower scores as duplicates

**Type 3: Student Data Changed** (name, grade, etc.)
- Resolution: Server version wins
- Action: Update client with server data

**Example Conflict Response:**
```json
{
  "success": true,
  "scoresProcessed": 5,
  "conflicts": [
    {
      "type": "DUPLICATE_SCORE",
      "scoreId": "score-uuid-1",
      "resolution": "SERVER_KEPT",
      "message": "Score already exists on server"
    }
  ],
  "syncId": "sync-uuid"
}
```

### 5. Download Phase

#### A. Get Assignments

```
GET /api/sync/assignments?studentId={studentId}&since={lastSyncTimestamp}

Response:
{
  "assignments": [
    {
      "assignmentId": "assign-uuid-1",
      "gameId": "word-builder",
      "assignedDate": "2025-01-09T08:00:00Z",
      "dueDate": "2025-01-16T23:59:59Z",
      "minScore": 70,
      "requiredCompletion": true,
      "status": "ASSIGNED"
    }
  ],
  "removedAssignments": [] // Assignments that were deleted
}
```

#### B. Get Game Updates

```
GET /api/sync/games?deviceVersion={currentVersion}

Response:
{
  "updates": [
    {
      "gameId": "math-sprint",
      "version": "1.1.0",
      "updateType": "PATCH",
      "downloadUrl": "/api/games/math-sprint/1.1.0/download",
      "size": 1048576,
      "releaseNotes": "Bug fixes and performance improvements"
    }
  ]
}
```

#### C. Get Student Data Updates

```
GET /api/sync/student?studentId={studentId}

Response:
{
  "student": {
    "studentId": "STUDENT001",
    "firstName": "Alex",
    "lastInitial": "J",
    "gradeLevel": "4", // Updated from 3 to 4
    "active": true,
    "consentGiven": true
  },
  "classes": [
    {
      "classId": "CLASS001",
      "className": "Math 4A",
      "teacherName": "Ms. Smith"
    }
  ]
}
```

### 6. Completion

After successful sync:

```
POST /api/sync/complete
Body:
{
  "syncId": "sync-uuid",
  "deviceId": "uuid-device-id",
  "success": true,
  "uploadedScores": 5,
  "downloadedAssignments": 1,
  "errors": []
}

Response:
{
  "success": true,
  "nextSyncRecommended": "2025-01-09T16:30:00Z"
}
```

## Client-Side Implementation

### Sync Queue

```java
public class SyncQueue {
    private final LocalDatabase db;
    
    public void addScore(GameScore score) {
        // Add score to local database
        db.insertScore(score);
        
        // Add to sync queue
        SyncQueueItem item = new SyncQueueItem();
        item.setType(SyncItemType.SCORE);
        item.setEntityId(score.getScoreId());
        item.setData(toJson(score));
        item.setPriority(SyncPriority.NORMAL);
        item.setCreatedAt(LocalDateTime.now());
        
        db.insertSyncQueueItem(item);
    }
    
    public List<SyncQueueItem> getPendingItems() {
        return db.getSyncQueueItems()
            .stream()
            .filter(item -> !item.isSynced())
            .sorted(Comparator.comparing(SyncQueueItem::getPriority)
                             .thenComparing(SyncQueueItem::getCreatedAt))
            .collect(Collectors.toList());
    }
    
    public void markSynced(String itemId) {
        db.updateSyncQueueItem(itemId, true, LocalDateTime.now());
    }
}
```

### Sync Manager

```java
public class SyncManager {
    private final NetworkMonitor networkMonitor;
    private final AuthenticationManager authManager;
    private final SyncQueue syncQueue;
    private final SyncService syncService;
    
    private boolean isSyncing = false;
    private LocalDateTime lastSyncAt;
    
    public void startAutoSync() {
        // Schedule periodic sync checks
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(
            this::attemptSync,
            0,
            30,
            TimeUnit.SECONDS
        );
    }
    
    private void attemptSync() {
        if (isSyncing) {
            return; // Already syncing
        }
        
        if (!networkMonitor.isSchoolNetworkAvailable()) {
            return; // Not on school network
        }
        
        if (syncQueue.getPendingItems().isEmpty()) {
            return; // Nothing to sync
        }
        
        // Perform sync
        performSync();
    }
    
    public void performSync() {
        isSyncing = true;
        
        try {
            // 1. Authenticate
            String sessionToken = authManager.authenticate();
            
            // 2. Get pending items
            List<SyncQueueItem> pending = syncQueue.getPendingItems();
            
            // 3. Upload
            SyncUploadRequest request = buildUploadRequest(pending);
            SyncUploadResponse uploadResponse = syncService.upload(request, sessionToken);
            
            // 4. Handle conflicts
            handleConflicts(uploadResponse.getConflicts());
            
            // 5. Mark uploaded items as synced
            for (SyncQueueItem item : pending) {
                if (!hasConflict(item, uploadResponse.getConflicts())) {
                    syncQueue.markSynced(item.getId());
                }
            }
            
            // 6. Download updates
            downloadAssignments(sessionToken);
            downloadGameUpdates(sessionToken);
            downloadStudentDataUpdates(sessionToken);
            
            // 7. Complete sync
            syncService.completeSync(uploadResponse.getSyncId(), sessionToken);
            
            lastSyncAt = LocalDateTime.now();
            
            // Notify UI
            notifySyncComplete(true, null);
            
        } catch (Exception e) {
            logger.error("Sync failed", e);
            notifySyncComplete(false, e.getMessage());
        } finally {
            isSyncing = false;
        }
    }
}
```

## Server-Side Implementation

### Sync Service

```java
@Service
public class SyncService {
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private GameScoreRepository scoreRepository;
    
    @Transactional
    public SyncUploadResponse processUpload(SyncUploadRequest request, String deviceId) {
        SyncUploadResponse response = new SyncUploadResponse();
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        // Verify device is authorized
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new UnauthorizedException("Device not found"));
            
        if (!device.isAuthorized()) {
            throw new UnauthorizedException("Device not authorized");
        }
        
        // Process each score
        for (GameScore score : request.getScores()) {
            try {
                // Check for duplicates
                if (scoreRepository.existsById(score.getScoreId())) {
                    conflicts.add(new ConflictInfo(
                        ConflictType.DUPLICATE_SCORE,
                        score.getScoreId(),
                        "Score already exists on server"
                    ));
                    continue;
                }
                
                // Validate score
                validateScore(score);
                
                // Save to database
                score.setSynced(true);
                score.setSyncedAt(LocalDateTime.now());
                scoreRepository.save(score);
                
                response.incrementScoresProcessed();
                
            } catch (ValidationException e) {
                conflicts.add(new ConflictInfo(
                    ConflictType.VALIDATION_ERROR,
                    score.getScoreId(),
                    e.getMessage()
                ));
            }
        }
        
        // Update device last sync time
        device.setLastSyncAt(LocalDateTime.now());
        deviceRepository.save(device);
        
        // Log the sync
        auditLog.log(
            device.getStudentId(),
            "SYNC_UPLOAD",
            "Uploaded " + response.getScoresProcessed() + " scores"
        );
        
        response.setConflicts(conflicts);
        response.setSyncId(UUID.randomUUID().toString());
        response.setSuccess(true);
        
        return response;
    }
}
```

## Performance Optimization

### Batch Processing

Process multiple scores in a single transaction:

```java
@Transactional
public void batchInsertScores(List<GameScore> scores) {
    final int BATCH_SIZE = 100;
    
    for (int i = 0; i < scores.size(); i += BATCH_SIZE) {
        int end = Math.min(i + BATCH_SIZE, scores.size());
        List<GameScore> batch = scores.subList(i, end);
        
        scoreRepository.saveAll(batch);
        scoreRepository.flush();
    }
}
```

### Compression

Compress large payloads:

```java
public byte[] compressData(String jsonData) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
        gzos.write(jsonData.getBytes(StandardCharsets.UTF_8));
    }
    return baos.toByteArray();
}
```

### Delta Sync

Only sync what's changed:

```sql
SELECT * FROM game_scores
WHERE student_id = ?
  AND (
    created_at > ? OR
    updated_at > ?
  )
  AND synced = false
ORDER BY created_at ASC;
```

## Error Handling

### Retry Logic

```java
public class RetryableSyncOperation {
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 5000;
    
    public <T> T executeWithRetry(Supplier<T> operation) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (TransientException e) {
                lastException = e;
                attempts++;
                
                if (attempts < MAX_RETRIES) {
                    sleep(RETRY_DELAY_MS * attempts); // Exponential backoff
                }
            }
        }
        
        throw new SyncException("Failed after " + MAX_RETRIES + " attempts", lastException);
    }
}
```

### Partial Sync Success

If some scores upload successfully but others fail, mark successful ones as synced and retry failures later.

## Security Considerations

### Token Refresh

Auth tokens expire after 90 days:

```java
public String refreshTokenIfNeeded(String currentToken) {
    if (tokenExpiresWithin(currentToken, Duration.ofDays(7))) {
        return authService.refreshToken(currentToken);
    }
    return currentToken;
}
```

### Data Encryption

Encrypt sensitive data in transit:

```java
public String encryptPayload(String data, PublicKey publicKey) {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
}
```

## Monitoring and Logging

### Sync Metrics

Track key metrics:

```java
public class SyncMetrics {
    private long totalSyncs;
    private long successfulSyncs;
    private long failedSyncs;
    private long averageUploadSize;
    private long averageSyncDuration;
    
    public void recordSync(SyncResult result) {
        totalSyncs++;
        
        if (result.isSuccess()) {
            successfulSyncs++;
        } else {
            failedSyncs++;
        }
        
        averageUploadSize = calculateRunningAverage(
            averageUploadSize, 
            result.getUploadSizeBytes(), 
            totalSyncs
        );
        
        averageSyncDuration = calculateRunningAverage(
            averageSyncDuration, 
            result.getDurationMs(), 
            totalSyncs
        );
    }
}
```

### Audit Trail

Log all sync operations:

```sql
INSERT INTO audit_log (
    timestamp, user_id, action, entity_type, 
    entity_id, details, result
) VALUES (
    CURRENT_TIMESTAMP, ?, 'SYNC_UPLOAD', 'SCORE',
    ?, ?, 'SUCCESS'
);
```

## Testing

### Unit Tests

```java
@Test
public void testSyncQueueAddsScoreCorrectly() {
    GameScore score = new GameScore("STUDENT001", "math-sprint", 85);
    syncQueue.addScore(score);
    
    List<SyncQueueItem> pending = syncQueue.getPendingItems();
    assertEquals(1, pending.size());
    assertEquals(score.getScoreId(), pending.get(0).getEntityId());
}
```

### Integration Tests

```java
@Test
public void testFullSyncCycle() {
    // Setup
    GameScore score = createTestScore();
    syncQueue.addScore(score);
    
    // Execute
    syncManager.performSync();
    
    // Verify
    assertTrue(syncQueue.getPendingItems().isEmpty());
    assertTrue(scoreRepository.existsById(score.getScoreId()));
}
```

## Troubleshooting

### Common Issues

**Issue**: Scores not syncing
- **Check**: Network connectivity
- **Check**: Device approval status
- **Check**: Auth token validity

**Issue**: Duplicate scores
- **Check**: Score IDs are unique
- **Check**: Sync queue properly managing synced items

**Issue**: Slow sync
- **Check**: Network bandwidth
- **Check**: Batch size configuration
- **Check**: Database indexes

## Future Enhancements

1. **Partial Uploads**: Resume interrupted uploads
2. **Predictive Sync**: Sync during off-peak hours
3. **Peer-to-Peer**: Share game updates between devices
4. **Progressive Download**: Download critical data first
5. **Smart Conflict Resolution**: ML-based conflict detection
