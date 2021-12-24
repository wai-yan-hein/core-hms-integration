/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cv.integration.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Wai Yan
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "sale_his")
public class SaleHis implements java.io.Serializable {
    @Id
    @Column(name = "sale_inv_id")
    private String vouNo;
    @Column(name = "sale_date")
    @Temporal(TemporalType.DATE)
    private Date vouDate;
    @Column(name = "vou_total")
    private Double vouTotal;
    @Column(name = "balance")
    private Double vouBalance;
    @Column(name = "discount")
    private Double vouDiscount;
    @Column(name = "paid_amount")
    private String vouPaid;
    @Column(name = "deleted")
    private boolean deleted;
    @ManyToOne
    @JoinColumn(name = "reg_no")
    private Patient patient;
    @ManyToOne
    @JoinColumn(name = "cus_id")
    private Trader trader;
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;
    @Column(name = "admission_no")
    private String admissionNo;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;
}
