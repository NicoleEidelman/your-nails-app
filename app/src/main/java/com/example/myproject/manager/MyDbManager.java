package com.example.myproject.manager;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myproject.models.Appointment;
import com.example.myproject.models.Treatment;
import com.example.myproject.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

public class MyDbManager {

    // Class Variables
    private FirebaseDatabase database;
    private DatabaseReference treatmentsRef;
    private DatabaseReference usersRef;
    private DatabaseReference slotsRef;

    // Constructor
    public MyDbManager() {
        this.database = FirebaseDatabase.getInstance();
        this.treatmentsRef = database.getReference("treatments");
        this.usersRef = database.getReference("users");
        this.slotsRef = database.getReference("slots");
    }

    // Callback Interface
    public interface CallBack<T> {
        void res(T res);
    }

    // User Management Methods
    public void loadUserData(String userId, CallBack<User> callback) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                callback.res(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.res(null);
            }
        });
    }

    public void updateUserData(User user, CallBack<Void> callback) {
        usersRef.child(user.getId()).setValue(user)
                .addOnSuccessListener(aVoid -> callback.res(null))
                .addOnFailureListener(e -> callback.res(null));
    }
    public void updateUserProfile(String userUid, String name, String phoneNumber, CallBack<Boolean> callback) {
        if (name != null && !name.isEmpty()) {
            updateUserName(userUid, name, nameResult -> {
                if (nameResult) {
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        updateUserPhoneNumber(userUid, phoneNumber, phoneResult -> {
                            if (phoneResult) {
                                callback.res(true);
                            } else {
                                callback.res(false);
                            }
                        });
                    } else {
                        callback.res(true); // Name updated, no phone update needed
                    }
                } else {
                    callback.res(false); // Name update failed
                }
            });
        } else if (phoneNumber != null && !phoneNumber.isEmpty()) {
            updateUserPhoneNumber(userUid, phoneNumber, phoneResult -> {
                if (phoneResult) {
                    callback.res(true);
                } else {
                    callback.res(false);
                }
            });
        } else {
            callback.res(false); // Neither name nor phone number provided
        }
    }








    public static void updateUserImage(String userUid, String imageUrl, Runnable onSuccess) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);
        userRef.child("imageUrl").setValue(imageUrl)
                .addOnSuccessListener(aVoid -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(e -> Log.e("MyDbManager", "Failed to update image URL: " + e.getMessage()));
    }

    public static void getUserImage(String userUid, Consumer<String> callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);
        userRef.child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageUrl = snapshot.getValue(String.class);
                if (callback != null) {
                    callback.accept(imageUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MyDbManager", "Failed to get image URL: " + error.getMessage());
            }
        });
    }
    public void updateUserName(String userUid, String name, CallBack<Boolean> callback) {
        usersRef.child(userUid).child("name").setValue(name)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.res(true);
                    } else {
                        callback.res(false);
                    }
                });
    }


    public void updateUserPhoneNumber(String userUid, String phoneNumber, CallBack<Boolean> callback) {
        usersRef.child(userUid).child("phone").setValue(phoneNumber)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.res(true);
                    } else {
                        callback.res(false);
                    }
                });
    }

    // Treatment Management Methods
    public void initializeTreatments(CallBack<Void> callback) {
        treatmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Map<String, Treatment> newTreatments = new HashMap<>();
                    newTreatments.put("Hand Manicure", new Treatment("Hand Manicure", "Revitalize your hands with our specialized manicure service."));
                    newTreatments.put("Foot Manicure", new Treatment("Foot Manicure", "Pamper your feet with our relaxing pedicure service."));
                    newTreatments.put("Regular Polish", new Treatment("Regular Polish", "Choose from a variety of nail polish colors for a fresh look."));
                    newTreatments.put("Nail Building", new Treatment("Nail Building", "Enhance the length and strength of your nails with our nail building service."));
                    newTreatments.put("Gel Hand Polish", new Treatment("Gel Hand Polish", "Get long-lasting, chip-free color for your nails with our gel polish service."));
                    newTreatments.put("Gel Foot Polish", new Treatment("Gel Foot Polish", "Achieve durable and vibrant color for your toenails with our gel polish treatment."));

                    treatmentsRef.setValue(newTreatments);
                }
                callback.res(null); // Callback when done
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.res(null);
            }
        });
    }

    public void loadTreatments(CallBack<Map<String, Treatment>> callback) {
        treatmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Treatment> treatmentsMap = new HashMap<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Treatment treatment = postSnapshot.getValue(Treatment.class);
                    if (treatment != null) {
                        treatmentsMap.put(postSnapshot.getKey(), treatment);
                    }
                }
                callback.res(treatmentsMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.res(null);
            }
        });
    }

    // Appointment Management Methods
    public void loadFutureAppointments(String userId, CallBack<List<Appointment>> callback) {
        DatabaseReference bookingsRef = usersRef.child(userId).child("bookings");
        bookingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Appointment> appointments = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Appointment appointment = postSnapshot.getValue(Appointment.class);
                    if (appointment != null && isFutureAppointment(appointment)) {
                        appointments.add(appointment);
                    }
                }
                callback.res(appointments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.res(null);
            }
        });
    }

    public void cancelAppointment(String userId, Appointment appointment, Runnable onSuccess, Context context) {
        DatabaseReference bookingsRef = usersRef.child(userId).child("bookings");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));

        try {
            String appointmentDateTimeStr = appointment.getDate() + " " + appointment.getTime();
            Date appointmentDateTime = dateFormat.parse(appointmentDateTimeStr);

            if (appointmentDateTime == null) {
                Toast.makeText(context, "Failed to parse appointment time.", Toast.LENGTH_LONG).show();
                return;
            }

            Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
            Date currentDateTime = currentCalendar.getTime();

            long differenceInMillis = appointmentDateTime.getTime() - currentDateTime.getTime();

            if (differenceInMillis < (24 * 60 * 60 * 1000)) { // Less than 24 hours
                Toast.makeText(context, "Cancellation is only allowed up to 24 hours before the appointment.", Toast.LENGTH_LONG).show();
                return;
            }

        } catch (ParseException e) {
            Toast.makeText(context, "Failed to parse appointment time.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        bookingsRef.orderByChild("date").equalTo(appointment.getDate())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            String bookedTime = postSnapshot.child("time").getValue(String.class);
                            String bookedTreatment = postSnapshot.child("treatment").getValue(String.class);

                            if (appointment.getTime().equals(bookedTime) &&
                                    (appointment.getTreatment() == null || appointment.getTreatment().equals(bookedTreatment))) {
                                postSnapshot.getRef().removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("MyDbManager", "Appointment cancelled successfully.");
                                            retrieveSlot(appointment.getDate(), appointment.getTime());
                                            Toast.makeText(context, "Appointment was successfully deleted.", Toast.LENGTH_SHORT).show();
                                            if (onSuccess != null) {
                                                onSuccess.run();
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("MyDbManager", "Failed to cancel appointment: " + e.getMessage()));
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MyDbManager", "Error canceling appointment: " + error.getMessage());
                    }
                });
    }
    private void retrieveSlot(String date, String time) {
        slotsRef.child(date).child(time).setValue(true)
                .addOnSuccessListener(aVoid -> Log.d("MyDbManager", "Slot " + time + " on " + date + " retrieved successfully."))
                .addOnFailureListener(e -> Log.e("MyDbManager", "Failed to retrieve slot: " + e.getMessage()));
    }



    public boolean bookSlot(String userId, String date, String time, String treatmentTitle) {
        DatabaseReference userBookingsRef = usersRef.child(userId).child("bookings");

        Map<String, String> bookingDetails = new HashMap<>();
        bookingDetails.put("date", date);
        bookingDetails.put("time", time);
        bookingDetails.put("treatment", treatmentTitle);

        String bookingId = userBookingsRef.push().getKey();

        if (bookingId != null) {
            userBookingsRef.child(bookingId).setValue(bookingDetails)
                    .addOnSuccessListener(aVoid -> {
                        // Mark the time slot as unavailable in Firebase
                        slotsRef.child(date).child(time).setValue(false);
                    })
                    .addOnFailureListener(e -> Log.e("MyDbManager", "Failed to book slot: " + e.getMessage()));

            return true;  // Slot booking process initiated successfully
        } else {
            return false;  // Failed to initiate slot booking
        }
    }

    // Slot Management Methods
    public void initializeSlotsForYear(CallBack<Void> callback) {
        DatabaseReference initializationFlagRef = database.getReference("slots_initialized");

        initializationFlagRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Set the start date to today
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    // Define the time slots
                    String[] times = {"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"};

                    // Loop through the entire year
                    for (int i = 0; i < 365; i++) { // Number of days in a year
                        String date = dateFormat.format(calendar.getTime());
                        addAvailableSlots(date, times);
                        calendar.add(Calendar.DAY_OF_YEAR, 1); // Move to the next day
                    }

                    initializationFlagRef.setValue(true);
                    callback.res(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.res(null);
            }
        });
    }

    private void addAvailableSlots(String date, String[] times) {
        DatabaseReference slotsRef = database.getReference("slots").child(date);

        // Create a map to hold the slots
        Map<String, Boolean> slots = new HashMap<>();
        for (String time : times) {
            slots.put(time, true); // All slots are initially available
        }

        // Set the slots for the specified date
        slotsRef.setValue(slots)
                .addOnSuccessListener(aVoid -> {
                    // Slots added successfully
                    System.out.println("Slots added for " + date);
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    System.out.println("Failed to add slots for " + date + ": " + e.getMessage());
                });
    }

    public void loadAvailableSlots(String date, CallBack<Map<String, Boolean>> callback) {
        slotsRef.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Boolean> availableSlots = new HashMap<>();
                for (DataSnapshot slotSnapshot : snapshot.getChildren()) {
                    String time = slotSnapshot.getKey();
                    Boolean isAvailable = slotSnapshot.getValue(Boolean.class);
                    availableSlots.put(time, isAvailable != null && isAvailable);
                }
                callback.res(availableSlots);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.res(null);
            }
        });
    }

    // Utility Methods
    private boolean isFutureAppointment(Appointment appointment) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem")); // Set to Israel timezone

        try {
            String appointmentDateTimeStr = appointment.getDate() + " " + appointment.getTime();
            Date appointmentDateTime = dateFormat.parse(appointmentDateTimeStr);

            // Get current date and time in Israel timezone
            Calendar current = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));

            // Return true if the appointment is in the future
            return appointmentDateTime != null && appointmentDateTime.after(current.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
