package com.cv.integration.service;

import com.cv.integration.common.Util1;
import com.cv.integration.common.Voucher;
import com.cv.integration.mongo.model.Doctor;
import com.cv.integration.mongo.model.PatientInfo;
import com.cv.integration.mongo.model.Region;
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
import java.util.Date;
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
                v.setPatientCode(Util1.isNull(rs.getString("reg_no"), "-"));
                v.setPatientName(Util1.isNull(rs.getString("patient_name"), "-"));
                v.setAdmissionNo(rs.getString("admission_no"));
                v.setAmount(rs.getDouble("sale_amount"));
                v.setPrice(rs.getDouble("sale_price"));
                v.setQtyStr(rs.getDouble("sale_qty") + " " + rs.getString("item_unit"));
                v.setStockName(rs.getString("med_name"));
                v.setLocationName(rs.getString("location_name"));
                v.setDoctorName(rs.getString("doctor_name"));
                v.setVouTypeName(rs.getString("status_desp"));
                v.setCreatedBy(rs.getString("user_name"));
                v.setDiscount(rs.getString("item_discount_p"));
                double focQty = rs.getDouble("foc_qty");
                v.setFocQty(focQty > 0 ? focQty + rs.getString("foc_unit") : null);
                Date expDate = rs.getDate("expire_date");
                v.setExpireDate(expDate == null ? null : Util1.toDateStr(expDate, "dd/MM/yyyy"));
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
                v.setVouDateStr(Util1.toDateStr(rs.getDate("pur_date"), "dd/MM/yyyy hh:mm aa"));
                v.setTtlAmt(rs.getDouble("vou_total"));
                v.setCurrency(rs.getString("currency"));
                v.setTraderCode(rs.getString("cus_id"));
                v.setTraderName(rs.getString("trader_name"));
                v.setAmount(rs.getDouble("pur_amount"));
                v.setPrice(rs.getDouble("pur_price"));
                v.setQtyStr(rs.getDouble("pur_qty") + " " + rs.getString("pur_unit"));
                v.setStockName(rs.getString("med_name"));
                v.setLocationName(rs.getString("location_name"));
                v.setCreatedBy(rs.getString("user_name"));
                v.setRemark(rs.getString("remark"));
                v.setReference(rs.getString("ref_no"));
                double focQty = rs.getDouble("pur_foc_qty");
                v.setFocQty(focQty > 0 ? focQty + rs.getString("foc_unit") : null);
                Date expDate = rs.getDate("expire_date");
                v.setExpireDate(expDate == null ? null : Util1.toDateStr(expDate, "dd/MM/yyyy"));
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
                v.setVouDateStr(Util1.toDateStr(rs.getDate("ret_in_date"), "dd/MM/yyyy hh:mm aa"));
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
                v.setVouDateStr(Util1.toDateStr(rs.getDate("opd_date"), "dd/MM/yyyy hh:mm aa"));
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
                v.setVouDateStr(Util1.toDateStr(rs.getDate("ot_date"), "dd/MM/yyyy hh:mm aa"));
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
                v.setVouDateStr(Util1.toDateStr(rs.getDate("dc_date"), "dd/MM/yyyy hh:mm aa"));
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
    public List<PatientInfo> getPatient() throws SQLException {
        List<PatientInfo> infos = new ArrayList<>();
        String sql = "select p.*,c.city_name,d.doctor_name from patient_detail p left join city c on p.city_id = c.city_id\n" +
                "left join doctor d on p.doctor_id = d.doctor_id";
        ResultSet rs = exeSql(sql);
        //reg_no, reg_date, dob, sex, father_name, nirc, city_id, nationality, religion, doctor_id,
        // patient_name, address, contactno, created_by, age, admission_no, township_id,
        // pt_type, ot_id, age_str, month, day, regno_h2
        if (rs != null) {
            while (rs.next()) {
                PatientInfo info = new PatientInfo();
                info.setPatientNo(rs.getString("reg_no"));
                info.setRegDate(rs.getTimestamp("reg_date"));
                info.setDob(rs.getDate("dob"));
                info.setGender(rs.getString("sex"));
                info.setFatherName(rs.getString("father_name"));
                info.setNrc(rs.getString("nirc"));
                info.setRegion(new Region(rs.getString("city_name")));
                info.setDoctor(new Doctor(rs.getString("doctor_name")));
                info.setPatientName(rs.getString("patient_name"));
                info.setAddress(rs.getString("address"));
                info.setPhoneNo(rs.getString("contactno"));
                info.setAge(rs.getInt("age"));
                info.setMonth(rs.getInt("month"));
                info.setDay(rs.getInt("day"));
                info.setAdmNo(rs.getString("admission_no"));
                infos.add(info);
            }
        }
        return infos;
    }

    @Override
    public boolean isAdmission(String date, String regNo, Integer payId) {
        boolean admission = false;
        String sql = "\n" +
                "select reg_no,admission_no,currency_id,round(sum(amt) ,0)amt\n" +
                "from (\n" +
                "select reg_no,admission_no, currency_id,sum(balance) amt\n" +
                "from sale_his \n" +
                "where deleted = 0 \n" +
                "#and date(sale_date)<= '" + date + "' \n" +
                "and balance <>0\n" +
                "and reg_no ='" + regNo + "'\n" +
                "group by reg_no,admission_no,currency_id\n" +
                "\tunion all\n" +
                "select reg_no,admission_no, currency,sum(balance)*-1 amt\n" +
                "from ret_in_his \n" +
                "where deleted = 0 \n" +
                "and date(ret_in_date)<='" + date + "' \n" +
                "and balance <>0\n" +
                "and reg_no ='" + regNo + "'\n" +
                "group by reg_no,admission_no, currency\n" +
                "\tunion all\n" +
                "select patient_id,admission_no, currency_id,sum(vou_balance) amt\n" +
                "from opd_his \n" +
                "where deleted = 0 \n" +
                "and date(opd_date)<='" + date + "' \n" +
                "and vou_balance <>0\n" +
                "and patient_id ='" + regNo + "'\n" +
                "group by patient_id,admission_no,currency_id\n" +
                "\tunion all\n" +
                "select patient_id,admission_no, currency_id,sum(vou_total) amt\n" +
                "from ot_his \n" +
                "where deleted = 0 \n" +
                "and date(ot_date)<='" + date + "' \n" +
                "and patient_id ='" + regNo + "'\n" +
                "group by patient_id,admission_no,currency_id\n" +
                "\tunion all\n" +
                "select patient_id,admission_no, currency_id,sum(paid)*-1 amt\n" +
                "from ot_his \n" +
                "where deleted = 0 \n" +
                "and date(ot_date)<='" + date + "' \n" +
                "and patient_id ='" + regNo + "'\n" +
                "group by patient_id,admission_no,currency_id\n" +
                "\tunion all\n" +
                "select patient_id,admission_no, currency_id,sum(disc_a)*-1 amt\n" +
                "from ot_his \n" +
                "where deleted = 0 \n" +
                "and date(ot_date)<='" + date + "' \n" +
                "and patient_id ='" + regNo + "'\n" +
                "group by patient_id,admission_no,currency_id\n" +
                "\tunion all\n" +
                "select patient_id,admission_no, currency_id,sum(vou_total) amt\n" +
                "from dc_his \n" +
                "where deleted = 0 \n" +
                "and date(dc_date)<='" + date + "' \n" +
                "and patient_id ='" + regNo + "'\n" +
                "group by patient_id,admission_no,currency_id\n" +
                "\tunion all\n" +
                "select patient_id,admission_no, currency_id,sum(disc_a)*-1 amt\n" +
                "from dc_his \n" +
                "where deleted = 0 \n" +
                "and date(dc_date)<='" + date + "' \n" +
                "and patient_id ='" + regNo + "'\n" +
                "group by patient_id,admission_no,currency_id\n" +
                "\tunion all \n" +
                "select patient_id,admission_no, currency_id,sum(paid)*-1 amt\n" +
                "from dc_his \n" +
                "where deleted = 0 \n" +
                "and date(dc_date)<='" + date + "' \n" +
                "and patient_id ='" + regNo + "'\n" +
                "group by patient_id,admission_no,currency_id\n" +
                "\tunion all\n" +
                "select reg_no,admission_no, currency_id,sum(pay_amt)*-1 amt\n" +
                "from opd_patient_bill_payment \n" +
                "where  date(pay_date)<='" + date + "' \n" +
                "and reg_no ='" + regNo + "' and id <> " + payId + "\n" +
                "and deleted = 0\n" +
                "group by reg_no,admission_no,currency_id\n" +
                ")a\n" +
                "where amt <> 0\n" +
                "group by reg_no,currency_id\n";
        ResultSet rs = exeSql(sql);
        try {
            while (rs.next()) {
                String amsNo = rs.getString("admission_no");
                double amt = rs.getDouble("amt");
                if (!Util1.isNullOrEmpty(amsNo) && amt > 0) {
                    admission = true;
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        return admission;
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
