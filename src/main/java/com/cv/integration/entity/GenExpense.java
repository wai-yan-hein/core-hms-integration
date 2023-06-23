package com.cv.integration.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "gen_expense")
public class GenExpense {
    @Id
    @Column(name = "gene_id")
    private Integer genId;
    @Column(name = "desp")
    private String description;
    @Column(name = "exp_date")
    private Date expDate;
    @Column(name = "remark")
    private String remark;
    @Column(name = "vou_no")
    private String vouNo;
    @Column(name = "source_acc_id")
    private String srcAcc;
    @Column(name = "acc_id")
    private String account;
    @Column(name = "dept_id")
    private String deptCode;
    @Column(name = "exp_amount")
    private Double expAmt;
    @Column(name = "expense_option")
    private String expOption;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;
    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;
    @Column(name = "deleted")
    private boolean deleted;
    @Column(name = "un_paid_status")
    private boolean unpaid;
    public GenExpense() {
    }
}
