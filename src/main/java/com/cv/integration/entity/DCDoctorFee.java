package com.cv.integration.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "dc_doctor_fee")
public class DCDoctorFee {
    @Id
    @Column(name = "dr_fee_id")
    private String drFeeId;
    @Column(name = "dc_detail_id")
    private String dcDetailId;
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}
