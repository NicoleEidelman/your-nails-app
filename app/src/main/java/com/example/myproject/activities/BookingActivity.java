package com.example.myproject.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.manager.MyDbManager;
import com.example.myproject.R;
import com.example.myproject.adapters.SlotAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class BookingActivity extends AppCompatActivity {

    private MyDbManager dbManager;
    private CalendarView calendarView;
    private RecyclerView slotRecyclerView;
    private SlotAdapter slotAdapter;
    private List<String> slotTimes;
    private String selectedDate;
    private ImageView myProfileIcon;
    private ImageView myMenuIcon;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        String treatmentTitle = getIntent().getStringExtra("TREATMENT_TITLE");
        findViews();
        initialize();

        setupRecyclerView();
        setupCalendarView(treatmentTitle);
        setupIconClickListeners();
    }

    private void findViews() {
        calendarView = findViewById(R.id.calendarView);
        myProfileIcon = findViewById(R.id.myProfileIcon);
        myMenuIcon = findViewById(R.id.myMenuIcon);
        slotRecyclerView = findViewById(R.id.slotRecyclerView);
    }

    private void initialize() {
        dbManager = new MyDbManager();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        slotTimes = new ArrayList<>();
        dbManager.initializeSlotsForYear(res -> {
            // Slots initialized, no further action required
        });
    }

    private void setupRecyclerView() {
        slotAdapter = new SlotAdapter(slotTimes, time -> bookSlot(selectedDate, time));
        slotRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        slotRecyclerView.setAdapter(slotAdapter);
    }


    private void setupCalendarView(String treatmentTitle) {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadAvailableSlots(selectedDate, treatmentTitle);
        });
    }

    private void setupIconClickListeners() {
        myProfileIcon.setOnClickListener(v -> navigateToActivity(ProfileActivity.class));
        myMenuIcon.setOnClickListener(v -> navigateToActivity(MenuActivity.class));
        myProfileIcon.setSelected(false);
        myMenuIcon.setSelected(false);
    }

    private void loadAvailableSlots(String date, String treatmentTitle) {
        if (isDateValid(date)) {
            dbManager.loadAvailableSlots(date, availableSlots -> {
                slotTimes.clear();
                if (availableSlots != null && !availableSlots.isEmpty()) {
                    populateSlotTimes(availableSlots);
                    slotAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(BookingActivity.this, "No slots available for this date.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            clearSlotTimes();
        }
    }

    private boolean isDateValid(String date) {
        TimeZone israelTimeZone = TimeZone.getTimeZone("Asia/Jerusalem");
        Calendar currentCalendar = Calendar.getInstance(israelTimeZone);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setTimeZone(israelTimeZone);
        String currentDate = dateFormat.format(currentCalendar.getTime());
        return date.compareTo(currentDate) > 0;
    }

    private void populateSlotTimes(Map<String, Boolean> availableSlots) {
        List<String> sortedTimes = new ArrayList<>(availableSlots.keySet());
        Collections.sort(sortedTimes, (time1, time2) -> compareTimes(time1, time2));

        for (String time : sortedTimes) {
            if (Boolean.TRUE.equals(availableSlots.get(time))) {
                slotTimes.add(time);
            }
        }
    }

    private int compareTimes(String time1, String time2) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        try {
            return sdf.parse(time1).compareTo(sdf.parse(time2));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void clearSlotTimes() {
        slotTimes.clear();
        slotAdapter.notifyDataSetChanged();
    }

    private void bookSlot(String date, String time) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(BookingActivity.this, "Please log in to book a slot.", Toast.LENGTH_SHORT).show();
            return;
        }

        vibrateDevice();
        String treatmentTitle = getIntent().getStringExtra("TREATMENT_TITLE");
        boolean success = dbManager.bookSlot(currentUser.getUid(), date, time, treatmentTitle);
        if (success) {
            // Remove the booked slot from the list
            slotTimes.remove(time);
            slotAdapter.notifyDataSetChanged();  // Update the RecyclerView to reflect the change
            Toast.makeText(BookingActivity.this, "Slot booked successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(BookingActivity.this, "Failed to book slot.", Toast.LENGTH_SHORT).show();
        }
    }



    private void vibrateDevice() {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(BookingActivity.this, activityClass);
        startActivity(intent);
    }
}