package com.ambermoon.lootgame.save;

public interface SyncCallback {
    void onSuccess(String message);
    void onError(String error);
}
