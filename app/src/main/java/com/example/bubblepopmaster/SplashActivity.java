package com.example.bubblepopmaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView splashLogo = findViewById(R.id.splashLogo);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim);
        splashLogo.startAnimation(animation);

        new Handler().postDelayed(() -> {

            Intent intent =
                    new Intent(SplashActivity.this,
                            HomeActivity.class);

            startActivity(intent);
            finish();

        }, 3000);
    }
}