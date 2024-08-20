package com.example.myproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.R;
import com.example.myproject.models.Treatment;
import com.example.myproject.activities.BookingActivity;

import java.util.List;

public class TreatmentAdapter extends RecyclerView.Adapter<TreatmentAdapter.TreatmentViewHolder> {
    private List<Treatment> treatments;
    private List<String> treatmentKeys;
    private Context context;

    public TreatmentAdapter(Context context, List<Treatment> treatments, List<String> treatmentKeys) {
        this.context = context;
        this.treatments = treatments;
        this.treatmentKeys = treatmentKeys;
    }

    @NonNull
    @Override
    public TreatmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        return new TreatmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreatmentViewHolder holder, int position) {
        Treatment treatment = treatments.get(position);
        String key = treatmentKeys.get(position);

        holder.titleTextView.setText(treatment.getTitle());
        holder.descriptionTextView.setText(treatment.getDescription());

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        holder.continueButton.setOnClickListener(v -> {
            // Vibrate when the continue button is clicked
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));

            // Start the BookingActivity with the treatment title
            Intent intent = new Intent(context, BookingActivity.class);
            intent.putExtra("TREATMENT_TITLE", key);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return treatments.size();
    }

    static class TreatmentViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        Button continueButton;

        public TreatmentViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_view_title);
            descriptionTextView = itemView.findViewById(R.id.text_view_description);
            continueButton = itemView.findViewById(R.id.button_continue);
        }
    }
}
