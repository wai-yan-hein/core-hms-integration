package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "opd_category")
public class OPDCategory implements java.io.Serializable {
    @Id
    @Column(name = "cat_id")
    private Integer catId;
    @Column(name = "cat_name")
    private String catName;
    @Column(name = "dep_code")
    private String deptCode;
    @Column(name = "opd_acc_code")
    private String opdAcc;
    @Column(name = "ipd_acc_code")
    private String ipdAcc;
    @Column(name = "srvf1_acc_id")
    private String moFeeAcc;
    @Column(name = "srvf2_acc_id")
    private String staffFeeAcc;
    @Column(name = "srvf3_acc_id")
    private String techFeeAcc;
    @Column(name = "srvf4_acc_id")
    private String referFeeAcc;
    @Column(name = "srvf5_acc_id")
    private String readFeeAcc;
    @Column(name = "payable_acc_id")
    private String payableAcc;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private OPDGroup group;

    public OPDCategory() {
    }
}
