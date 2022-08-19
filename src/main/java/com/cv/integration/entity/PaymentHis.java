package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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
    @Temporal(TemporalType.DATE)
    @Column(name = "pay_date")
    private Date payDate;
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
