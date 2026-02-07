package com.ambermoon.lootgame.save;

/**
 * Callback interface for cloud sync operations.
 * Top-level interface (not nested) to avoid D8 dex compiler crashes
 * when inner classes from other packages implement nested interfaces.
 */
public interface SyncCallback {
    void onSuccess(String message);
    void onError(String error);
}
