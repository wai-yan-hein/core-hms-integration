package com.cv.integration.mongo.model;

import lombok.Data;

@Data
public class Doctor {
    private String id;
    private String doctorName;

    public Doctor(String doctorName) {
        this.doctorName = doctorName;
    }

    public Doctor() {
    }
}
