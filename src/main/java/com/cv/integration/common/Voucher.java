/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cv.integration.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Lenovo
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Voucher {

    private String vouNo;
    private String tranId;
    private Date vouDate;
    private String vouDateStr;
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
    private String admissionNo;
    private String patientName;
    private String defaultPatient;
    private String depCode;
    private boolean admission;
    private String reference;
    private String traderCode;
    private String traderName;
    private String appType;
    private List<VoucherList> listVoucher;
    private String locationName;
    private String doctorName;
    private String vouTypeName;
    private Double amount;
    private Double qty;
    private String qtyStr;
    private Double price;
    private String unit;
    private String stockName;
    private String discount;
    private String focQty;
    private String createdBy;
    private String remark;
    private String expireDate;

}
