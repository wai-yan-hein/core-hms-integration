package com.cv.integration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "acc_setting")
public class AccountSetting {
    @Id
    private String type;
    @Column(name = "source_acc")
    private String sourceAcc;
    @Column(name = "pay_acc")
    private String payAcc;
    @Column(name = "dis_acc")
    private String discountAcc;
    @Column(name = "bal_acc")
    private String balanceAcc;
    @Column(name = "tax_acc")
    private String taxAcc;
    @Column(name = "dep_code")
    private String deptCode;
    @Column(name = "source_acc_1")
    private String ipdSource;
    @Column(name = "pay_acc_out")
    private String payAccOut;
}
