/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * @author winswe
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "pur_his")
public class PurHis implements java.io.Serializable {

    @Id
    @Column(name = "pur_inv_id")
    private String vouNo;
    @Column(name = "pur_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vouDate;
    @Column(name = "vou_total")
    private Double vouTotal;
    @Column(name = "balance")
    private Double vouBalance;
    @Column(name = "discount")
    private String vouDiscount;
    @Column(name = "paid")
    private String vouPaid;
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
}
