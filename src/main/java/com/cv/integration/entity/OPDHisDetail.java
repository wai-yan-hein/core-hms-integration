package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "opd_details_his")
public class OPDHisDetail implements java.io.Serializable {
    @Id
    @Column(name = "opd_detail_id")
    private String id;
    @Column(name = "vou_no")
    private String vouNo;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private OPDService service;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "srv_fees2")
    private Double moFeeAmt;
    @Column(name = "srv_fees3")
    private Double staffFeeAmt;
    @Column(name = "srv_fees4")
    private Double techFeeAmt;
    @Column(name = "srv_fees5")
    private Double referFeeAmt;
    @Column(name = "srv_fees6")
    private Double readFeeAmt;
    @Column(name = "charge_type")
    private Integer chargeType;
    @ManyToOne
    @JoinColumn(name = "reader_doctor_id")
    private Doctor reader;
    @ManyToOne
    @JoinColumn(name = "refer_doctor_id")
    private Doctor refer;
    @ManyToOne
    @JoinColumn(name = "tech_id")
    private Doctor technician;
    @Column(name = "qty")
    private Double qty;

    public OPDHisDetail() {
    }
}
