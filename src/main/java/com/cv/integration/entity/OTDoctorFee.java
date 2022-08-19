package com.cv.integration.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ot_doctor_fee")
public class OTDoctorFee {
    @Id
    @Column(name = "dr_fee_id")
    private String drFeeId;
    @Column(name = "ot_detail_id")
    private String otDetailId;
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}
