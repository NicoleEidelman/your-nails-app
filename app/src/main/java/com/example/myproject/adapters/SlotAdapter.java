package com.example.myproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.R;

import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    private final List<String> slotTimes;
    private final SlotClickListener listener;

    public interface SlotClickListener {
        void onSlotClick(String time);
    }

    public SlotAdapter(List<String> slotTimes, SlotClickListener listener) {
        this.slotTimes = slotTimes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        String time = slotTimes.get(position);
        holder.bind(time, listener);
    }

    @Override
    public int getItemCount() {
        return slotTimes.size();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        private final Button slotButton;

        SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            slotButton = itemView.findViewById(R.id.slotButton);
        }

        void bind(final String time, final SlotClickListener listener) {
            slotButton.setText(time);
            slotButton.setOnClickListener(v -> listener.onSlotClick(time));
        }
    }
}
