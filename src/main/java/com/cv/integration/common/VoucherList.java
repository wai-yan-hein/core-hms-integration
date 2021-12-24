/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cv.integration.common;

import lombok.Data;

import java.util.Date;

/**
 * @author Lenovo
 */
@Data
public class VoucherList {

    private Date vouDate;
    private Double ttlAmt;
    private Double srvFee1;
    private Double srvFee2;
    private Double srvFee3;
    private Double srvFee4;
    private Double srvFee5;
    private Double srvFee6;
    private String srvAccCode1;
    private String srvAccCode2;
    private String srvAccCode3;
    private String srvAccCode4;
    private String srvAccCode5;
    private String sourceAcc;
    private String depCode;
    private String currency;
    private String serviceName;
    private boolean deposit;
    private boolean paid;
    private boolean refund;
    private boolean discount;
    private String payableAccId;
}
