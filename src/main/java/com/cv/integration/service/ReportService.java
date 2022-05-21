package com.cv.integration.service;

import com.cv.integration.common.Voucher;

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


}
