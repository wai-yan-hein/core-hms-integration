package com.cv.integration.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "dc_his")
public class DCHis {
    @Id
    @Column(name = "dc_inv_id")
    private String vouNo;
    @Column(name = "dc_date",columnDefinition = "TIMESTAMP")
    private LocalDateTime vouDate;
    @Column(name = "vou_total")
    private Double vouTotal;
    @Column(name = "vou_balance")
    private Double vouBalance;
    @Column(name = "disc_a")
    private Double vouDiscount;
    @Column(name = "paid")
    private Double vouPaid;
    @Column(name = "deleted")
    private boolean deleted;
    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;
    @Column(name = "admission_no")
    private String admissionNo;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;
    @ManyToOne()
    @JoinColumn(name = "patient_id")
    private Patient patient;
    @Column(name = "payment_id")
    private Integer paymentId;
    @Column(name = "doctor_id")
    private String doctorId;
}
