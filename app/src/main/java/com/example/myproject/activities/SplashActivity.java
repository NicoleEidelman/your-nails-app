package com.example.myproject.activities;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myproject.manager.MyDbManager;
import com.example.myproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private ImageView splashLogo;
    private static final int SPLASH_TIME_OUT = 5000;
    private FirebaseAuth mAuth;
    private MyDbManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbManager = new MyDbManager();
        splashLogo = findViewById(R.id.splashLogo);
        animate(splashLogo);
        dbManager.initializeTreatments(res -> {
            // After treatments are initialized, initialize the slots for the year
            dbManager.initializeSlotsForYear(unused -> {
                // After slots are initialized, check user authentication state
              //  checkAuthenticationState();
            });
        });

    }




    private void animate(View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setY(-displayMetrics.heightPixels / 2 - view.getHeight());
        view.setScaleX(0.0f);
        view.setScaleY(0.0f);
        view
                .animate()
                .scaleY(1.0f)
                .scaleX(1.0f)
                .translationY(0)
                .setDuration(SPLASH_TIME_OUT)
                .setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        checkAuthenticationState();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
    }


    private void checkAuthenticationState() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        } else {
            proceedToProfileActivity();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, Activity_Login.class);
        startActivity(intent);
        finish();
    }

    private void proceedToProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

}
