package com.example.myproject.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myproject.utils.AnimationUtils;
import com.example.myproject.manager.MyDbManager;
import com.example.myproject.R;
import com.example.myproject.models.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;

public class Activity_Login extends AppCompatActivity {
    private MaterialButton loginButton;
    private MyDbManager dbManager;
    private ImageView logoImageView;
    private TextView welcomeTextView, instructionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViews();

        dbManager = new MyDbManager();
        setupWindowInsets();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setupShineEffectOnButton();
        animateWelcomeText();

        loginButton.setOnClickListener(v -> {
            // Vibrate when the login button is clicked
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            login(); // Call the login method
        });
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginScreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupShineEffectOnButton() {
        loginButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                loginButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                AnimationUtils.addShineEffectToButton(loginButton, getResources().getDrawable(R.drawable.button_shine, null));
            }
        });
    }

    private void animateWelcomeText() {
        AnimationUtils.fadeIn(welcomeTextView, 2000);
        AnimationUtils.fadeIn(instructionTextView, 2000);
    }

    private void login() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()
                ))
                .build();

        signInLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            (result) -> {
                if (result.getResultCode() == RESULT_OK) {
                    checkForNewUser();
                } else {
                    loginButton.setText("Login Failed");
                }
            });

    private void checkForNewUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        dbManager.loadUserData(firebaseUser.getUid(), user -> {
            if (user == null) {
                createNewUser(firebaseUser);
            } else {
                navigateToDetails();
            }
        });
    }

    private void createNewUser(FirebaseUser firebaseUser) {
        User user = new User();
        user.setId(firebaseUser.getUid())
                .setName(firebaseUser.getDisplayName())
                .setEmail(firebaseUser.getEmail());

        dbManager.updateUserData(user, aVoid -> navigateToDetails());
    }

    private void navigateToDetails() {
        Intent intent = new Intent(Activity_Login.this, DetailsActivity.class);
        startActivity(intent);
        finish();
    }

    private void findViews() {
        loginButton = findViewById(R.id.main_BTN_login);
        welcomeTextView = findViewById(R.id.welcomeMessage);
        instructionTextView = findViewById(R.id.instructionMessage);
        logoImageView = findViewById(R.id.imageViewLogo);
    }
}
