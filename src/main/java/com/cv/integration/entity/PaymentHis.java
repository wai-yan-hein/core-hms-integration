package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "payment_his")
public class PaymentHis implements java.io.Serializable {
    @Id
    @Column(name = "payment_id")
    private Integer payId;
    @Column(name = "deleted")
    private boolean deleted;
    @Column(name = "paid_amtc")
    private Double payAmt;
    @Column(name = "pay_dt",columnDefinition = "TIMESTAMP")
    private LocalDateTime payDate;
    @Column(name = "remark")
    private String remark;
    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;
    @ManyToOne
    @JoinColumn(name = "trader_id")
    private Trader trader;
    @Column(name = "discount")
    private Double discount;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;
    @ManyToOne
    @JoinColumn(name = "pay_id")
    private PaymentType paymentType;
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    public PaymentHis() {
    }
}
