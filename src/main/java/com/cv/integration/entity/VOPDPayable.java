package com.cv.integration.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
@Data
@Entity
@Table(name = "v_opd_payable")
public class VOPDPayable implements java.io.Serializable {
    @Id
    @Column(name = "opd_inv_id")
    private String vouNo;
    @Column(name = "tran_date")
    @Temporal(TemporalType.DATE)
    private Date tranDate;
    @Column(name = "expense_name")
    private String expenseName;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private OPDService opdService;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private OPDGroup opdGroup;
    @Column(name = "cat_id")
    private String catId;
    @Column(name = "doctor_id")
    private String drCode;
    @Column(name = "amount")
    private Double amount;
    @ManyToOne
    @JoinColumn(name = "reg_no")
    private Patient patient;

}
