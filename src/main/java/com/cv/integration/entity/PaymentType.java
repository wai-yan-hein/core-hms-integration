package com.cv.integration.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "trader_pay_account")
public class PaymentType implements java.io.Serializable {
    @Id
    @Column(name = "pay_id")
    private String payId;
    @Column(name = "desp")
    private String desp;
}
