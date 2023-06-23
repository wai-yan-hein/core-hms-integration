package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "ot_his")
public class OTHis implements java.io.Serializable {
    @Id
    @Column(name = "ot_inv_id")
    private String vouNo;
    @Column(name = "ot_date",columnDefinition = "TIMESTAMP")
    private LocalDateTime vouDate;
    @Column(name = "vou_total")
    private Double vouTotal;
    @Column(name = "vou_balance")
    private Double vouBalance;
    @Column(name = "disc_a")
    private Double vouDiscount;
    @Column(name = "paid")
    private double vouPaid;
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

    public OTHis() {
    }
}
