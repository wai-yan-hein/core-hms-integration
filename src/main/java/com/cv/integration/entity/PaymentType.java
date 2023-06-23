package com.cv.integration.entity;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "trader_pay_account")
public class PaymentType implements java.io.Serializable {
    @Id
    @Column(name = "pay_id")
    private String payId;
    @Column(name = "desp")
    private String desp;
    @Column(name = "acc_id")
    private String account;
}
