package com.cv.integration.service;

import com.cv.integration.common.Util1;
import com.cv.integration.common.Voucher;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional("hibernate")
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private SessionFactory sessionFactory;
    private ResultSet rs = null;

    @Override
    public List<Voucher> getSaleVoucher(String vouNo) throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        String sql = "select v.*,l.location_name,d.doctor_name,vs.status_desp,a.user_name,\n" +
                "item_discount_p,foc_qty,foc_unit\n" +
                "from v_sale v \n" +
                "join location l on v.location_id = l.location_id\n" +
                "left join doctor d on v.doctor_id = d.doctor_id\n" +
                "join vou_status vs on v.vou_status = vs.vou_status_id\n" +
                "join appuser a on v.created_by = a.user_id\n" +
                "where sale_inv_id = '" + vouNo + "'";
        ResultSet rs = exeSql(sql);
        if (!Objects.isNull(rs)) {
            while (rs.next()) {
                Voucher v = new Voucher();
                v.setVouNo(rs.getString("sale_inv_id"));
                v.setVouBal(rs.getDouble("balance"));
                v.setDisAmt(rs.getDouble("discount"));
                v.setPaidAmt(rs.getDouble("paid_amount"));
                v.setVouDateStr(Util1.toDateStr(rs.getDate("sale_date"), "dd/MM/yyyy hh:mm aa"));
                v.setTtlAmt(rs.getDouble("vou_total"));
                v.setCurrency(rs.getString("currency_id"));
                v.setPatientCode(rs.getString("reg_no"));
                v.setPatientName(rs.getString("patient_name"));
                v.setAdmissionNo(rs.getString("admission_no"));
                v.setAmount(rs.getDouble("sale_amount"));
                v.setPrice(rs.getDouble("sale_price"));
                v.setQtyStr(rs.getDouble("sale_qty") + rs.getString("item_unit"));
                v.setStockName(rs.getString("med_name"));
                v.setLocationName(rs.getString("location_name"));
                v.setDoctorName(rs.getString("doctor_name"));
                v.setVouTypeName(rs.getString("status_desp"));
                v.setCreatedBy(rs.getString("user_name"));
                v.setDiscount(rs.getString("item_discount_p"));
                double focQty = rs.getDouble("foc_qty");
                v.setFocQty(focQty > 0 ? focQty + rs.getString("foc_unit") : null);
                v.setExpireDate(Util1.toDateStr(rs.getDate("expire_date"), "dd/MM/yyyy"));
                v.setRemark(rs.getString("remark"));
                vouchers.add(v);
            }
        }
        return vouchers;
    }

    @Override
    public List<Voucher> getPurchaseVoucher(String vouNo) throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        String sql = "select v.*,a.user_name,l.location_name\n" +
                "from v_purchase v join appuser a on v.created_by = a.user_id\n" +
                "join location l on v.location = l.location_id\n" +
                "where pur_inv_id = '" + vouNo + "'";
        ResultSet rs = exeSql(sql);
        if (!Objects.isNull(rs)) {
            while (rs.next()) {
                Voucher v = new Voucher();
                v.setVouNo(rs.getString("pur_inv_id"));
                v.setVouBal(rs.getDouble("balance"));
                v.setDisAmt(rs.getDouble("discount"));
                v.setPaidAmt(rs.getDouble("paid"));
                v.setVouDate(rs.getDate("pur_date"));
                v.setTtlAmt(rs.getDouble("vou_total"));
                v.setCurrency(rs.getString("currency"));
                v.setTraderCode(rs.getString("cus_id"));
                v.setTraderName(rs.getString("trader_name"));
                v.setAmount(rs.getDouble("pur_amount"));
                v.setPrice(rs.getDouble("pur_price"));
                v.setQtyStr(rs.getDouble("pur_qty") + rs.getString("pur_unit"));
                v.setStockName(rs.getString("med_name"));
                v.setLocationName(rs.getString("location_name"));
                v.setCreatedBy(rs.getString("user_name"));
                vouchers.add(v);
            }
        }
        return vouchers;
    }

    @Override
    public List<Voucher> getReturnInVoucher(String vouNo) throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        String sql = "select v.*,a.user_name,l.location_name,pd.patient_name\n" +
                "from v_return_in v join appuser a on v.created_by = a.user_id\n" +
                "join location l on v.location = l.location_id\n" +
                "join patient_detail pd on v.reg_no = pd.reg_no\n" +
                "where ret_in_id = '" + vouNo + "'";
        ResultSet rs = exeSql(sql);
        if (!Objects.isNull(rs)) {
            while (rs.next()) {
                Voucher v = new Voucher();
                v.setVouNo(rs.getString("ret_in_id"));
                v.setVouBal(rs.getDouble("balance"));
                v.setPaidAmt(rs.getDouble("paid"));
                v.setVouDate(rs.getDate("ret_in_date"));
                v.setTtlAmt(rs.getDouble("vou_total"));
                v.setCurrency(rs.getString("currency"));
                v.setPatientCode(rs.getString("reg_no"));
                v.setPatientName(rs.getString("patient_name"));
                v.setAdmissionNo(rs.getString("admission_no"));
                v.setAmount(rs.getDouble("ret_in_amount"));
                v.setPrice(rs.getDouble("ret_in_price"));
                v.setQtyStr(rs.getDouble("ret_in_qty") + rs.getString("item_unit"));
                v.setStockName(rs.getString("med_name"));
                v.setLocationName(rs.getString("location_name"));
                v.setCreatedBy(rs.getString("user_name"));
                vouchers.add(v);
            }
        }
        return vouchers;
    }

    @Override
    public List<Voucher> getReturnOutVoucher(String vouNo) throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        String sql = "select v.*,a.user_name,l.location_name\n" +
                "from v_return_out v join appuser a on v.created_by = a.user_id\n" +
                "join location l on v.location = l.location_id\n" +
                "where ret_out_id = '" + vouNo + "'";
        ResultSet rs = exeSql(sql);
        if (!Objects.isNull(rs)) {
            while (rs.next()) {
                Voucher v = new Voucher();
                v.setVouNo(rs.getString("ret_out_id"));
                v.setVouBal(rs.getDouble("balance"));
                v.setPaidAmt(rs.getDouble("paid"));
                v.setVouDate(rs.getDate("ret_out_date"));
                v.setTtlAmt(rs.getDouble("vou_total"));
                v.setCurrency(rs.getString("currency"));
                v.setTraderCode(rs.getString("cus_id"));
                v.setTraderName(rs.getString("trader_name"));
                v.setAmount(rs.getDouble("ret_out_amount"));
                v.setPrice(rs.getDouble("ret_out_price"));
                v.setQtyStr(rs.getDouble("ret_out_qty") + rs.getString("item_unit"));
                v.setStockName(rs.getString("med_name"));
                v.setLocationName(rs.getString("location_name"));
                v.setCreatedBy(rs.getString("user_name"));
                vouchers.add(v);
            }
        }
        return vouchers;
    }

    @Override
    public List<Voucher> getOPDVoucher(String vouNo) throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        String sql = "select v.*,a.user_name\n" +
                "from v_opd v join appuser a on v.created_by = a.user_id\n" +
                "where opd_inv_id = '" + vouNo + "'";
        ResultSet rs = exeSql(sql);
        if (!Objects.isNull(rs)) {
            while (rs.next()) {
                Voucher v = new Voucher();
                v.setVouNo(rs.getString("opd_inv_id"));
                v.setVouBal(rs.getDouble("vou_balance"));
                v.setPaidAmt(rs.getDouble("paid"));
                v.setVouDate(rs.getDate("opd_date"));
                v.setTtlAmt(rs.getDouble("vou_total"));
                v.setDisAmt(rs.getDouble("disc_a"));
                v.setCurrency(rs.getString("currency_id"));
                v.setPatientCode(rs.getString("patient_id"));
                v.setPatientName(rs.getString("patient_name"));
                v.setAdmissionNo(rs.getString("admission_no"));
                v.setAmount(rs.getDouble("amount"));
                v.setPrice(rs.getDouble("price"));
                v.setQty(rs.getDouble("qty"));
                v.setDescription(rs.getString("service_name"));
                v.setCreatedBy(rs.getString("user_name"));
                v.setDoctorName(rs.getString("doctor_name"));
                vouchers.add(v);
            }
        }
        return vouchers;
    }

    @Override
    public List<Voucher> getOTVoucher(String vouNo) throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        String sql = "select v.*,a.user_name\n" +
                "from v_ot v join appuser a on v.created_by = a.user_id\n" +
                "where ot_inv_id = '" + vouNo + "'";
        ResultSet rs = exeSql(sql);
        if (!Objects.isNull(rs)) {
            while (rs.next()) {
                Voucher v = new Voucher();
                v.setVouNo(rs.getString("ot_inv_id"));
                v.setVouBal(rs.getDouble("vou_balance"));
                v.setPaidAmt(rs.getDouble("paid"));
                v.setVouDate(rs.getDate("ot_date"));
                v.setTtlAmt(rs.getDouble("vou_total"));
                v.setDisAmt(rs.getDouble("disc_a"));
                v.setCurrency(rs.getString("currency_id"));
                v.setPatientCode(rs.getString("patient_id"));
                v.setPatientName(rs.getString("patient_name"));
                v.setAdmissionNo(rs.getString("admission_no"));
                v.setAmount(rs.getDouble("amount"));
                v.setPrice(rs.getDouble("price"));
                v.setQty(rs.getDouble("qty"));
                v.setDescription(rs.getString("service_name"));
                v.setCreatedBy(rs.getString("user_name"));
                v.setDoctorName(rs.getString("doctor_name"));
                vouchers.add(v);
            }
        }
        return vouchers;
    }

    @Override
    public List<Voucher> getDCVoucher(String vouNo) throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        String sql = "select v.*,a.user_name\n" +
                "from v_dc v join appuser a on v.created_by = a.user_id\n" +
                "where dc_inv_id = '" + vouNo + "'";
        ResultSet rs = exeSql(sql);
        if (!Objects.isNull(rs)) {
            while (rs.next()) {
                Voucher v = new Voucher();
                v.setVouNo(rs.getString("dc_inv_id"));
                v.setVouBal(rs.getDouble("vou_balance"));
                v.setPaidAmt(rs.getDouble("paid"));
                v.setVouDate(rs.getDate("dc_date"));
                v.setTtlAmt(rs.getDouble("vou_total"));
                v.setDisAmt(rs.getDouble("disc_a"));
                v.setCurrency(rs.getString("currency_id"));
                v.setPatientCode(rs.getString("patient_id"));
                v.setPatientName(rs.getString("patient_name"));
                v.setAdmissionNo(rs.getString("admission_no"));
                v.setAmount(rs.getDouble("amount"));
                v.setPrice(rs.getDouble("price"));
                v.setQty(rs.getDouble("qty"));
                v.setDescription(rs.getString("service_name"));
                v.setCreatedBy(rs.getString("user_name"));
                v.setDoctorName(rs.getString("doctor_name"));
                vouchers.add(v);
            }
        }
        return vouchers;
    }

    public ResultSet exeSql(final String sql) {
        Work work = (Connection con) -> {
            try {
                rs = null;
                rs = con.prepareStatement(sql).executeQuery();
            } catch (SQLException ex) {
                log.error("getResultSet : " + sql + " : " + ex.getMessage());
            }
        };
        sessionFactory.getCurrentSession().doWork(work);
        return rs;
    }
}
