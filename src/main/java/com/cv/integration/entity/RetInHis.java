/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cv.integration.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author WSwe
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "ret_in_his")
public class RetInHis implements java.io.Serializable {
    @Id
    @Column(name = "ret_in_id")
    private String vouNo;
    @Column(name = "ret_in_date",columnDefinition = "TIMESTAMP")
    private LocalDateTime vouDate;
    @Column(name = "vou_total")
    private Double vouTotal;
    @Column(name = "balance")
    private Double vouBalance;
    @Column(name = "paid")
    private double vouPaid;
    @Column(name = "deleted")
    private boolean deleted;
    @ManyToOne
    @JoinColumn(name = "reg_no")
    private Patient patient;
    @ManyToOne
    @JoinColumn(name = "cus_id")
    private Trader trader;
    @ManyToOne
    @JoinColumn(name = "currency")
    private Currency currency;
    @Column(name = "admission_no")
    private String admissionNo;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;
    @ManyToOne
    @JoinColumn(name = "location")
    private Location location;
}
