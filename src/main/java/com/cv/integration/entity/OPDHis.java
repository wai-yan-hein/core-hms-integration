package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "opd_his")
public class OPDHis implements java.io.Serializable {
    @Id
    @Column(name = "opd_inv_id")
    private String vouNo;
    @Column(name = "opd_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vouDate;
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
    @Column(name = "patient_name")
    private String patientName;
    @Column(name = "payment_id")
    private Integer paymentId;

    public OPDHis() {
    }
}
