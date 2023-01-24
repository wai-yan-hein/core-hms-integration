/*
 * To change this template, choose Tools | Templates
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
 * @author WSwe
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "ret_out_his")
public class RetOutHis implements java.io.Serializable {
    @Id
    @Column(name = "ret_out_id")
    private String vouNo;
    @Column(name = "ret_out_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vouDate;
    @Column(name = "vou_total")
    private Double vouTotal;
    @Column(name = "balance")
    private Double vouBalance;
    @Column(name = "paid")
    private double vouPaid;
    @Column(name = "deleted")
    private boolean deleted;
    @ManyToOne
    @JoinColumn(name = "cus_id")
    private Trader trader;
    @ManyToOne
    @JoinColumn(name = "currency")
    private Currency currency;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;
    @ManyToOne
    @JoinColumn(name = "location")
    private Location location;
    @Column(name = "remark")
    private String remark;
}
