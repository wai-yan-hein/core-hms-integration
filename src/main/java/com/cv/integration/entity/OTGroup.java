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
@Table(name = "ot_group")
public class OTGroup implements java.io.Serializable {
    @Id
    @Column(name = "group_id")
    private Integer groupId;
    @Column(name = "group_name")
    private String groupName;
    @Column(name = "opd_acc_code")
    private String opdAcc;
    @Column(name = "ipd_acc_code")
    private String ipdAcc;
    @Column(name = "dep_code")
    private String deptCode;
    @Column(name = "srvf2_acc_id")
    private String staffFeeAcc;
    @Column(name = "srvf3_acc_id")
    private String nurseFeeAcc;
    @Column(name = "srvf4_acc_id")
    private String moFeeAcc;
    @Column(name = "payable_acc_id")
    private String payableAcc;

    public OTGroup() {
    }
}
