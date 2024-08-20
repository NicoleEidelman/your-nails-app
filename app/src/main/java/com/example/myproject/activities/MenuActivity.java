package com.example.myproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.manager.MyDbManager;
import com.example.myproject.R;
import com.example.myproject.models.Treatment;
import com.example.myproject.adapters.TreatmentAdapter;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TreatmentAdapter adapter;
    private ImageView backIcon;
    private List<Treatment> treatmentList = new ArrayList<>();
    private List<String> treatmentKeys = new ArrayList<>(); // To store keys for the treatments
    private MyDbManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initViews();
        setupRecyclerView();
        setupListeners();

        dbManager = new MyDbManager();

        loadTreatments();
    }

    private void initViews() {
        backIcon = findViewById(R.id.icon_top_left);
        recyclerView = findViewById(R.id.recycler_view);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TreatmentAdapter(this, treatmentList, treatmentKeys);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        backIcon.setOnClickListener(v -> navigateToProfile());
    }

    private void loadTreatments() {
        dbManager.loadTreatments(treatmentsMap -> {
            treatmentList.clear();
            treatmentKeys.clear();
            if (treatmentsMap != null && !treatmentsMap.isEmpty()) {
                treatmentList.addAll(treatmentsMap.values());
                treatmentKeys.addAll(treatmentsMap.keySet());
            } else {
                Toast.makeText(MenuActivity.this, "No treatments available", Toast.LENGTH_SHORT).show();
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void navigateToProfile() {
        Intent intent = new Intent(MenuActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish(); // Optionally finish the current activity if you don't want users to return to it
    }
}
