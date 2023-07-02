package com.cv.integration.service;

import com.cv.integration.common.Voucher;
import com.cv.integration.entity.SaleHis;
import com.cv.integration.model.VoucherInfo;
import com.cv.integration.mongo.model.PatientInfo;

import java.sql.SQLException;
import java.util.List;

public interface ReportService {
    List<Voucher> getSaleVoucher(String vouNo) throws SQLException;

    List<Voucher> getPurchaseVoucher(String vouNo) throws SQLException;

    List<Voucher> getReturnInVoucher(String vouNo) throws SQLException;

    List<Voucher> getReturnOutVoucher(String vouNo) throws SQLException;

    List<Voucher> getOPDVoucher(String vouNo) throws SQLException;

    List<Voucher> getOTVoucher(String vouNo) throws SQLException;

    List<Voucher> getDCVoucher(String vouNo) throws SQLException;

    List<PatientInfo> getPatient() throws SQLException;

    boolean isAdmission(String date, String regNo, Integer payId);

    List<VoucherInfo> getSaleList(String fromDate, String toDate);

    List<VoucherInfo> getPurchaseList(String fromDate, String toDate);

    List<VoucherInfo> getReturnInList(String fromDate, String toDate);

    List<VoucherInfo> getReturnOutList(String fromDate, String toDate);

    List<VoucherInfo> getOPDList(String fromDate, String toDate);

    List<VoucherInfo> getOTList(String fromDate, String toDate);

    List<VoucherInfo> getDCList(String fromDate, String toDate);

    List<VoucherInfo> getPaymentList(String fromDate, String toDate);


}
