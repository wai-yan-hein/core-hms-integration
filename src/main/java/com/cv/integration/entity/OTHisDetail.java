package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "ot_details_his")
public class OTHisDetail implements java.io.Serializable {
    @Id
    @Column(name = "ot_detail_id")
    private String id;
    @Column(name = "vou_no")
    private String vouNo;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private OTService service;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "srv_fee1")
    private Double hospitalAmt;
    @Column(name = "srv_fee2")
    private Double staffFeeAmt;
    @Column(name = "srv_fee3")
    private Double nurseFeeAmt;
    @Column(name = "srv_fee4")
    private Double moFeeAmt;
    @Column(name = "qty")
    private Double qty;
    @Column(name = "charge_type")
    private Integer chargeType;
}
