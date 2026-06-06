package com.example.bubblepopmaster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnPlay, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnPlay = findViewById(R.id.btnPlay);
        btnExit = findViewById(R.id.btnExit);

        btnPlay.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        });

        btnExit.setOnClickListener(v -> finish());
    }
}