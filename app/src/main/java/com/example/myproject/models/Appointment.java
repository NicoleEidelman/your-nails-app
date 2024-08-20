package com.example.myproject.models;

import java.util.Objects;

public class Appointment {
    private String date;
    private String time;
    private String treatment;


    public Appointment() {}


    public Appointment(String date, String time, String treatment) {
        this.date = date;
        this.time = time;
        this.treatment = treatment;
    }


    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTreatment() {
        return treatment;
    }


    @Override
    public String toString() {
        return "Appointment{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", treatment='" + treatment + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(date, that.date) &&
                Objects.equals(time, that.time) &&
                Objects.equals(treatment, that.treatment);
    }


    @Override
    public int hashCode() {
        return Objects.hash(date, time, treatment);
    }
}
