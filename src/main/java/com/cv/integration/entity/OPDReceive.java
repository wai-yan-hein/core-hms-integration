package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "opd_patient_bill_payment")
public class OPDReceive implements java.io.Serializable {
    @Id
    @Column(name = "id")
    private Integer billId;
    @ManyToOne
    @JoinColumn(name = "reg_no")
    private Patient patient;
    @Column(name = "currency_id")
    private String curCode;
    @Column(name = "pay_date")
    @Temporal(TemporalType.DATE)
    private Date payDate;
    @Column(name = "pay_amt")
    private Double payAmt;
    @Column(name = "remark")
    private String remark;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;

    public OPDReceive() {
    }
}
