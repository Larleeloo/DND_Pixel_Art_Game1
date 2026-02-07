package com.ambermoon.lootgame.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.ambermoon.lootgame.graphics.AssetLoader;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.entity.RecipeManager;
import com.ambermoon.lootgame.save.SaveManager;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize core systems
        GamePreferences.init(this);
        AssetLoader.init(this);
        SaveManager.init(this);
        RecipeManager.initialize(this);
        ItemRegistry.initialize();

        // Go straight to the game
        startActivity(new Intent(this, TabActivity.class));
        finish();
    }
}
