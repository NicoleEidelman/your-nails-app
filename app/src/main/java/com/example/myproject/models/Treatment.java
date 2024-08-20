package com.example.myproject.models;

public class Treatment {
    private String title;
    private String description;

    // Default constructor required for calls to DataSnapshot.getValue(Treatment.class)
    public Treatment() {}

    public Treatment(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

   /* public void setTitle(String title) {
        this.title = title;
    }*/

    public String getDescription() {
        return description;
    }

    /*public void setDescription(String description) {
        this.description = description;
    }*/
}
