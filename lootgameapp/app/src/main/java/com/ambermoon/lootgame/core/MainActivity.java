package com.ambermoon.lootgame.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.ambermoon.lootgame.graphics.AssetLoader;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.entity.RecipeManager;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize core systems (SaveManager is initialized after username is chosen)
        GamePreferences.init(this);
        AssetLoader.init(this);
        RecipeManager.initialize(this);
        ItemRegistry.initialize();

        // Show username screen — SaveManager.init() is called there after user picks a name
        startActivity(new Intent(this, UsernameActivity.class));
        finish();
    }
}
