/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cv.integration.common;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Lenovo
 */
@Data
public class Voucher {

    private String vouNo;
    private String tranId;
    private Date vouDate;
    private String currency;
    private String compCode;
    private String description;
    private String srcAcc;
    private String disAcc;
    private String payAcc;
    private String balAcc;
    private Double vouBal;
    private Double ttlAmt;
    private Double disAmt;
    private Double taxAmt;
    private Double paidAmt;
    private Boolean deleted;
    private String patientCode;
    private String patientName;
    private String defaultPatient;
    private String depCode;
    private boolean admission;
    private String reference;
    private String traderCode;
    private String traderName;
    private String appType;
    private List<VoucherList> listVoucher;

}
