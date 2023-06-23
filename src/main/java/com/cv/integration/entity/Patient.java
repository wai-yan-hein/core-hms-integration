package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "patient_detail")
public class Patient implements java.io.Serializable {
    @Id
    @Column(name = "reg_no")
    private String patientNo;
    @Column(name = "patient_name")
    private String patientName;

    public Patient() {
    }
}
