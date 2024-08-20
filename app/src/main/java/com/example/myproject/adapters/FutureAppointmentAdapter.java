package com.example.myproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.models.Appointment;
import com.example.myproject.R;

import java.util.List;

public class FutureAppointmentAdapter extends RecyclerView.Adapter<FutureAppointmentAdapter.AppointmentViewHolder> {
    private static final int VIEW_TYPE_APPOINTMENT = 0;
    private static final int VIEW_TYPE_EMPTY = 1;

    private final Context context;
    private final List<Appointment> appointmentList;
    private final OnAppointmentClickListener listener;

    // Interface for handling appointment cancellations
    public interface OnAppointmentClickListener {
        void onCancelAppointment(Appointment appointment);
    }

    // Constructor
    public FutureAppointmentAdapter(Context context, List<Appointment> appointmentList, OnAppointmentClickListener listener) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_EMPTY) {
            view = LayoutInflater.from(context).inflate(R.layout.item_empty_view, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_future_appointment, parent, false);
        }
        return new AppointmentViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_EMPTY) {
            holder.bindEmptyView();
        } else {
            holder.bindAppointmentView(appointmentList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return appointmentList.isEmpty() ? 1 : appointmentList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return appointmentList.isEmpty() ? VIEW_TYPE_EMPTY : VIEW_TYPE_APPOINTMENT;
    }

    // ViewHolder class to hold references to each item's views
    public class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private TextView text_view_title;
        private TextView date_text;
        private TextView time_text;
        private Button button_cancel;
        private TextView emptyMessage;

        public AppointmentViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == VIEW_TYPE_EMPTY) {
                emptyMessage = itemView.findViewById(R.id.empty_message);
            } else {
                text_view_title = itemView.findViewById(R.id.text_view_title);
                date_text = itemView.findViewById(R.id.date_text);
                time_text = itemView.findViewById(R.id.time_text);
                button_cancel = itemView.findViewById(R.id.button_cancel);
            }
        }

        public void bindEmptyView() {
            emptyMessage.setText("No future appointments. Please choose one.");
            Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
            emptyMessage.startAnimation(fadeInAnimation);
        }

        public void bindAppointmentView(Appointment appointment) {
            text_view_title.setText(appointment.getTreatment());
            date_text.setText(appointment.getDate());
            time_text.setText(appointment.getTime());
            button_cancel.setOnClickListener(v -> listener.onCancelAppointment(appointment));
        }
    }
}
