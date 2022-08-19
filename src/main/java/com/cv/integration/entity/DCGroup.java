package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "inp_category")
public class DCGroup implements java.io.Serializable {
    @Id
    @Column(name = "cat_id")
    private Integer groupId;
    @Column(name = "cat_name")
    private String groupName;
    @Column(name = "account_id")
    private String accountCode;
    @Column(name = "dep_code")
    private String deptCode;
    @Column(name = "srvf2_acc_id")
    private String nurseFeeAcc;
    @Column(name = "srvf3_acc_id")
    private String techFeeAcc;
    @Column(name = "srvf4_acc_id")
    private String moFeeAcc;
    @Column(name = "payable_acc_id")
    private String payableAcc;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;


    public DCGroup() {
    }
}
