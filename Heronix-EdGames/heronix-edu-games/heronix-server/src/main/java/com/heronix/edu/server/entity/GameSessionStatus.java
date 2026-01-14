package com.heronix.edu.server.entity;

/**
 * Status of a multiplayer game session.
 */
public enum GameSessionStatus {
    WAITING,    // Session created, waiting for players to join
    ACTIVE,     // Game is running
    PAUSED,     // Game temporarily paused by teacher
    ENDED       // Game has finished
}
