package com.cv.integration.mongo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PatientInfo {
    private String id;
    private String patientNo;
    private String admNo;
    private String patientName;
    private String nrc;
    private Date regDate;
    private Date dob;
    private String gender;
    private String fatherName;
    private Region region;
    private Doctor doctor;
    private String address;
    private String phoneNo;
    private Integer age;
    private Integer month;
    private Integer day;
    private PatientGroup patientGroup;
}
