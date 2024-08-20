package com.example.myproject.activities;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myproject.R;
import com.example.myproject.adapters.FutureAppointmentAdapter;
import com.example.myproject.manager.MyDbManager;
import com.example.myproject.models.Appointment;
import com.example.myproject.utils.AnimationUtils;
import com.example.myproject.utils.MyBackgroundMusic;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_CODE = 1;
    private static final int IMAGE_REQUEST = 7;
    private static final int EDIT_PROFILE_REQUEST_CODE = 100;

    private ImageView profile_menu_icon;
    private TextView text_name;
    private TextView text_phone_label;
    private ImageView myMenuIcon;
    private ImageView myProfileIcon;
    private RecyclerView recyclerView;
    private ShapeableImageView profile_IMG_avatar;
    private View loadingAnimation;
    private ImageView polishBottle;
    private AnimatorSet animatorSet;

    private FutureAppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();
    private MyDbManager dbManager;

    private Uri imageLocationPath;
    private StorageReference objectStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupRecyclerView();
        checkAndRequestPermissions();
        setupListeners();

        dbManager = new MyDbManager();
        objectStorageReference = FirebaseStorage.getInstance().getReference("USERS");

        updateUI();
    }

    private void initViews() {
        profile_menu_icon = findViewById(R.id.profile_menu_icon);
        text_name = findViewById(R.id.text_name);
        text_phone_label = findViewById(R.id.text_phone);
        myMenuIcon = findViewById(R.id.myMenuIcon);
        myProfileIcon = findViewById(R.id.myProfileIcon);
        profile_IMG_avatar = findViewById(R.id.profile_IMG_avatar);
        recyclerView = findViewById(R.id.recycler_view);
        loadingAnimation = findViewById(R.id.loadingAnimation);
        polishBottle = findViewById(R.id.polishBottle);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FutureAppointmentAdapter(this, appointmentList, this::cancelAppointment);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        profile_menu_icon.setOnClickListener(this::showPopupMenu);
        myMenuIcon.setOnClickListener(v -> {
            v.setSelected(true);
            navigateTo(MenuActivity.class);
        });

        myProfileIcon.setOnClickListener(v -> {
            if (!v.isSelected()) {
                v.setSelected(true);
                myMenuIcon.setSelected(false);
            }
        });
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(ProfileActivity.this, cls);
        startActivity(intent);
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit) {
                editImage();
            } else if (id == R.id.menu_delete) {
                removeProfileImage();
            } else if (id == R.id.menu_play_music) {
                startService(new Intent(this, MyBackgroundMusic.class));
            } else if (id == R.id.menu_stop_music) {
                stopService(new Intent(this, MyBackgroundMusic.class));
            } else if (id == R.id.menu_edit_profile) {
                navigateToEditProfile();
            }
             else if (id == R.id.menu_edit_logout) {
                showLogoutConfirmationDialog();

            }
            return true;
        });

        popup.show();
    }
    private void showLogoutConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Toast.makeText(ProfileActivity.this, "Goodbye", Toast.LENGTH_SHORT).show();
                    logout();  // Perform the logout operation
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Toast.makeText(ProfileActivity.this, "You chose to stay!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ProfileActivity.this, Activity_Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToEditProfile() {
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);

        startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
    }

    private void editImage() {
        try {
            Intent objectIntent = new Intent();
            objectIntent.setType("image/*");
            objectIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(objectIntent, IMAGE_REQUEST);
        } catch (Exception e) {
            showToast("Failed: " + e.getMessage());
        }
    }

    private void removeProfileImage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userUid = currentUser.getUid();
        profile_IMG_avatar.setImageResource(R.drawable.img_user_avater4);
        MyDbManager.updateUserImage(userUid, "", () -> showToast("Profile picture removed."));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            handleImageSelection(data.getData());
        } else if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh UI with updated profile data
            loadUserData();
        }
    }

    private void handleImageSelection(Uri uri) {
        try {
            imageLocationPath = uri;
            Bitmap objectBitmap = loadBitmapFromUri(uri);
            profile_IMG_avatar.setImageBitmap(objectBitmap);
            showToast("Image uploaded successfully.");
            uploadImage(imageLocationPath);
        } catch (Exception e) {
            showToast("Failed: " + e.getMessage());
        }
    }

    private Bitmap loadBitmapFromUri(Uri uri) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), uri));
        } else {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        }
    }

    private void uploadImage(Uri uri) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userUid = currentUser.getUid();
        String extension = getExtension(uri);
        if (extension == null) {
            showToast("Unable to determine file type");
            return;
        }

        StorageReference imageRef = objectStorageReference.child(userUid + "." + extension);
        uploadImageToFirebase(imageRef, uri, userUid);
    }

    private void uploadImageToFirebase(StorageReference imageRef, Uri uri, String userUid) {
        imageRef.putFile(uri).addOnFailureListener(exception ->
                        showToast("Upload failed: " + exception.getMessage()))
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        MyDbManager.updateUserImage(userUid, task.getResult().toString(), null);
                    }
                }));
    }

    private String getExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.INTERNET);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    Log.d("Permissions", "Permission granted: " + permissions[i]);
                } else {
                    showToast("Permission denied: " + permissions[i]);
                }
            }
        }
    }

    private void updateUI() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        MyDbManager.getUserImage(currentUser.getUid(), imageUrl -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                loadImage(imageUrl);
            } else {
                profile_IMG_avatar.setImageResource(R.drawable.img_user_avater4); // Fallback image
            }
        });
    }

    private void loadImage(String imageUrl) {
        Glide.with(ProfileActivity.this)
                .load(imageUrl)
                .placeholder(R.drawable.img_white)
                .centerCrop()
                .into(profile_IMG_avatar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadFutureAppointments();
    }

    private void loadUserData() {
        showLoadingAnimation();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        dbManager.loadUserData(currentUser.getUid(), user -> {
            hideLoadingAnimation();

            if (user != null) {
                updateUserInfo(user.getName(), user.getPhone());
            } else {
                handleDeletedUser();
            }
        });
    }

    private void updateUserInfo(String userName, String userPhone) {
        if (userName == null || userPhone == null) {
            redirectToLogin();
            return;
        }

        text_name.setText(userName.isEmpty() ? "No name provided" : userName);
        text_phone_label.setText(userPhone.isEmpty() ? "No phone provided" : userPhone);
    }

    private void handleDeletedUser() {
        FirebaseAuth.getInstance().signOut();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, Activity_Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadFutureAppointments() {
        showLoadingAnimation();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        dbManager.loadFutureAppointments(currentUser.getUid(), appointments -> {
            hideLoadingAnimation();

            appointmentList.clear();
            if (appointments != null) {
                appointmentList.addAll(appointments);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void cancelAppointment(Appointment appointment) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        dbManager.cancelAppointment(currentUser.getUid(), appointment, this::loadFutureAppointments,  this);
    }

    private void showLoadingAnimation() {
        loadingAnimation.setVisibility(View.VISIBLE);
        animatorSet = AnimationUtils.startShiningEffect(polishBottle);
    }

    private void hideLoadingAnimation() {
        loadingAnimation.setVisibility(View.GONE);
        AnimationUtils.stopShiningEffect(animatorSet);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
