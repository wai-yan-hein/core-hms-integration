package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "dc_details_his")
public class DCHisDetail {
    @Id
    @Column(name = "dc_detail_id")
    private String id;
    @Column(name = "vou_no")
    private String vouNo;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private DCService service;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "srv_fee1")
    private Double hospitalAmt;
    @Column(name = "srv_fee2")
    private Double nurseFeeAmt;
    @Column(name = "srv_fee3")
    private Double techFeeAmt;
    @Column(name = "srv_fee4")
    private Double moFeeAmt;
    @Column(name = "qty")
    private Double qty;
    public DCHisDetail() {
    }
}
