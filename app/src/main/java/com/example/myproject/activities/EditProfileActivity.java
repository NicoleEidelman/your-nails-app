package com.example.myproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myproject.manager.MyDbManager;
import com.example.myproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editProfileName, editProfilePhone;
    private MaterialButton editProfileSaveBtn;
    private MyDbManager dbManager;
    private ImageView backIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();

        dbManager = new MyDbManager();

        // Fill the EditTexts with the current user data
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            dbManager.loadUserData(currentUser.getUid(), user -> {
                if (user != null) {
                    editProfileName.setText(user.getName());
                    editProfilePhone.setText(user.getPhone());
                }
            });
        }
        setupListeners();
        editProfileSaveBtn.setOnClickListener(v -> saveProfileDetails());
    }

    private void initViews() {
        editProfileName = findViewById(R.id.edit_profile_EDT_name);
        editProfilePhone = findViewById(R.id.edit_profile_EDT_phone);
        editProfileSaveBtn = findViewById(R.id.edit_profile_BTN_save);
        backIcon = findViewById(R.id.icon_top_left);
    }
    private void setupListeners() {
        backIcon.setOnClickListener(v -> navigateToProfile());
    }
    private void navigateToProfile() {
        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish(); // Optionally finish the current activity if you don't want users to return to it
    }
    private void saveProfileDetails() {
        String newName = editProfileName.getText().toString().trim();
        String newPhone = editProfilePhone.getText().toString().trim();

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            dbManager.updateUserProfile(currentUser.getUid(), newName, newPhone, result -> {
                if (result) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    navigateToProfile();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}
