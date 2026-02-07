package com.ambermoon.lootgame.save;

/**
 * Functional interface for cloud sync operation results.
 * Single-method so lambdas can be used instead of named inner classes,
 * which crash the D8 dex compiler (R8 8.2.2).
 */
public interface SyncCallback {
    void onResult(boolean success, String message);
}
