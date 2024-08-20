package com.example.myproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myproject.utils.AnimationUtils;
import com.example.myproject.manager.MyDbManager;
import com.example.myproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DetailsActivity extends AppCompatActivity {

    private EditText details_EDT_phone;
    private MaterialButton details_BTN_save;
    private TextView middle_TXT_message;
    private MyDbManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initViews();

        dbManager = new MyDbManager();
        animateMiddleMessage();


        details_BTN_save.setOnClickListener(v -> saveDetails());
    }

    private void initViews() {
        details_EDT_phone = findViewById(R.id.details_EDT_phone);
        details_BTN_save = findViewById(R.id.details_BTN_save);
        middle_TXT_message = findViewById(R.id.middle_TXT_message);
    }

    private void animateMiddleMessage() {
        AnimationUtils.fadeIn(middle_TXT_message, 2000); // Fade-in animation for 4 seconds
    }




    private void saveDetails() {
        String phoneNumber = details_EDT_phone.getText().toString().trim();

        if (!validatePhoneNumber(phoneNumber)) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            dbManager.updateUserPhoneNumber(currentUser.getUid(), phoneNumber, success -> {
                if (success) {
                    navigateToProfile();
                } else {
                    Toast.makeText(DetailsActivity.this, "Failed to save details", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber.isEmpty()) {
            showError(details_EDT_phone, "Phone number is required");
            return false;
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            showError(details_EDT_phone, "Invalid phone number");
            return false;
        }

        return true;
    }

    private void showError(EditText editText, String error) {
        editText.setError(error);
        editText.requestFocus();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 10 ; // Adjust as necessary
    }

    private void navigateToProfile() {
        Intent intent = new Intent(DetailsActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}
