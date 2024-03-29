package com.cv.integration.service;

import com.cv.integration.common.Util1;
import com.cv.integration.common.Voucher;
import com.cv.integration.model.ErrorMessage;
import com.cv.integration.model.SyncModel;
import com.cv.integration.model.VoucherInfo;
import com.cv.integration.mongo.model.Doctor;
import com.cv.integration.mongo.model.PatientInfo;
import com.cv.integration.mongo.model.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final JdbcTemplate jdbcTemplate;

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
        ResultSet rs = getResult(sql);
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
        ResultSet rs = getResult(sql);
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
        ResultSet rs = getResult(sql);
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
        ResultSet rs = getResult(sql);
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
        ResultSet rs = getResult(sql);
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
        ResultSet rs = getResult(sql);
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
        ResultSet rs = getResult(sql);
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
        ResultSet rs = getResult(sql);
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
        ResultSet rs = getResult(sql);
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

    @Override
    public List<VoucherInfo> getSaleList(String fromDate, String toDate) {
        List<VoucherInfo> list = new ArrayList<>();
        String sql = "select sale_inv_id,vou_total\n" +
                "from sale_his\n" +
                "where date(sale_date) between '" + fromDate + "' and '" + toDate + "'\n" +
                "and deleted = false\n" +
                "and vou_total <>0\n" +
                "order by sale_inv_id;";
        ResultSet rs = getResult(sql);
        try {
            while (rs.next()) {
                VoucherInfo info = VoucherInfo.builder()
                        .vouNo(rs.getString("sale_inv_id"))
                        .vouTotal(rs.getDouble("vou_total"))
                        .build();
                list.add(info);
            }

        } catch (Exception e) {
            log.error("getSaleList : " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<VoucherInfo> getPurchaseList(String fromDate, String toDate) {
        List<VoucherInfo> list = new ArrayList<>();
        String sql = "select pur_inv_id,vou_total\n" +
                "from pur_his\n" +
                "where date(pur_date) between '" + fromDate + "' and '" + toDate + "'\n" +
                "and deleted = false\n" +
                "and vou_total <>0\n" +
                "order by pur_inv_id;";
        ResultSet rs = getResult(sql);
        try {
            while (rs.next()) {
                VoucherInfo info = VoucherInfo.builder()
                        .vouNo(rs.getString("pur_inv_id"))
                        .vouTotal(rs.getDouble("vou_total"))
                        .build();
                list.add(info);
            }

        } catch (Exception e) {
            log.error("getPurchaseList : " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<VoucherInfo> getReturnInList(String fromDate, String toDate) {
        List<VoucherInfo> list = new ArrayList<>();
        String sql = "select ret_in_id,vou_total\n" +
                "from ret_in_his\n" +
                "where date(ret_in_date) between '" + fromDate + "' and '" + toDate + "'\n" +
                "and deleted = false\n" +
                "and vou_total <>0\n" +
                "order by ret_in_id;";
        ResultSet rs = getResult(sql);
        try {
            while (rs.next()) {
                VoucherInfo info = VoucherInfo.builder()
                        .vouNo(rs.getString("pur_inv_id"))
                        .vouTotal(rs.getDouble("vou_total"))
                        .build();
                list.add(info);
            }

        } catch (Exception e) {
            log.error("getReturnInList : " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<VoucherInfo> getReturnOutList(String fromDate, String toDate) {
        List<VoucherInfo> list = new ArrayList<>();
        String sql = "select ret_out_id,vou_total\n" +
                "from ret_out_his\n" +
                "where date(ret_out_date) between '" + fromDate + "' and '" + toDate + "'\n" +
                "and deleted = false\n" +
                "and vou_total <>0\n" +
                "order by ret_out_id;";
        ResultSet rs = getResult(sql);
        try {
            while (rs.next()) {
                VoucherInfo info = VoucherInfo.builder()
                        .vouNo(rs.getString("pur_inv_id"))
                        .vouTotal(rs.getDouble("vou_total"))
                        .build();
                list.add(info);
            }

        } catch (Exception e) {
            log.error("getReturnOutList : " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<VoucherInfo> getOPDList(String fromDate, String toDate) {
        List<VoucherInfo> list = new ArrayList<>();
        String sql = "select opd_inv_id,vou_total\n" +
                "from opd_his\n" +
                "where date(opd_date) between '" + fromDate + "' and '" + toDate + "'\n" +
                "and deleted = false\n" +
                "and vou_total <>0\n" +
                "order by opd_inv_id;";
        ResultSet rs = getResult(sql);
        try {
            while (rs.next()) {
                VoucherInfo info = VoucherInfo.builder()
                        .vouNo(rs.getString("opd_inv_id"))
                        .vouTotal(rs.getDouble("vou_total"))
                        .build();
                list.add(info);
            }

        } catch (Exception e) {
            log.error("getOPDList : " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<VoucherInfo> getOTList(String fromDate, String toDate) {
        List<VoucherInfo> list = new ArrayList<>();
        String sql = "select ot_inv_id,vou_total\n" +
                "from ot_his\n" +
                "where date(ot_date) between '" + fromDate + "' and '" + toDate + "'\n" +
                "and deleted = false\n" +
                "and vou_total <>0\n" +
                "order by ot_inv_id;";
        ResultSet rs = getResult(sql);
        try {
            while (rs.next()) {
                VoucherInfo info = VoucherInfo.builder()
                        .vouNo(rs.getString("ot_inv_id"))
                        .vouTotal(rs.getDouble("vou_total"))
                        .build();
                list.add(info);
            }

        } catch (Exception e) {
            log.error("getOTList : " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<VoucherInfo> getDCList(String fromDate, String toDate) {
        List<VoucherInfo> list = new ArrayList<>();
        String sql = "select dc_inv_id,vou_total\n" +
                "from dc_his\n" +
                "where date(dc_date) between '" + fromDate + "' and '" + toDate + "'\n" +
                "and deleted = false\n" +
                "and vou_total <>0\n" +
                "order by dc_inv_id;";
        ResultSet rs = getResult(sql);
        try {
            while (rs.next()) {
                VoucherInfo info = VoucherInfo.builder()
                        .vouNo(rs.getString("dc_inv_id"))
                        .vouTotal(rs.getDouble("vou_total"))
                        .build();
                list.add(info);
            }

        } catch (Exception e) {
            log.error("getDCList : " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<VoucherInfo> getPaymentList(String fromDate, String toDate) {
        List<VoucherInfo> list = new ArrayList<>();
        String sql = "select payment_id,paid_amtc\n" +
                "from payment_his\n" +
                "where date(pay_dt) between '" + fromDate + "' and '" + toDate + "'\n" +
                "and deleted = false\n" +
                "order by payment_id;";
        ResultSet rs = getResult(sql);
        try {
            while (rs.next()) {
                VoucherInfo info = VoucherInfo.builder()
                        .vouNo(rs.getString("payment_id"))
                        .vouTotal(rs.getDouble("paid_amtc"))
                        .build();
                list.add(info);
            }

        } catch (Exception e) {
            log.error("getDCList : " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ErrorMessage> getErrorMessage() {
        return null;
    }

    @Override
    public boolean syncData(List<SyncModel> list) {
        if (list != null && !list.isEmpty()) {
            list.forEach(model -> {
                String fromDate = model.getFromDate();
                String toDate = model.getToDate();
                String status = model.isAck() ? "'ACK'" : null;
                switch (model.getTranSource()) {
                    case "SALE" -> syncSale(fromDate, toDate, status);
                    case "PURCHASE" -> syncPurchase(fromDate, toDate, status);
                    case "RETURN_IN" -> syncReturnIn(fromDate, toDate, status);
                    case "RETURN_OUT" -> syncReturnOut(fromDate, toDate, status);
                    case "OPD" -> syncOPD(fromDate, toDate, status);
                    case "OT" -> syncOT(fromDate, toDate, status);
                    case "DC" -> syncDC(fromDate, toDate, status);
                    case "PAYMENT" -> syncPayment(fromDate, toDate, status);
                    case "BILL" -> syncOPDReceive(fromDate, toDate, status);
                    case "EXPENSE" -> syncExpense(fromDate, toDate, status);
                }
            });
            return true;
        }
        return false;
    }

    private void syncSale(String fromDate, String toDate,String status) {
        String sql = """
                update sale_his
                set intg_upd_status = %s
                where date(sale_date) between '%s' and '%s'
                """.formatted(status, fromDate, toDate);
        executeSql(sql);

    }

    private void syncPurchase(String fromDate, String toDate, String status) {
        String sql = """
                update pur_his
                set intg_upd_status = %s
                where date(pur_date) between '%s' and '%s'
                """.formatted(status, fromDate, toDate);
        executeSql(sql);
    }

    private void syncReturnIn(String fromDate, String toDate, String status) {
        String sql = """
                update ret_in_his
                set intg_upd_status = %s
                where date(ret_in_date) between '%s' and '%s'
                """.formatted(status, fromDate, toDate);
        executeSql(sql);
    }

    private void syncReturnOut(String fromDate, String toDate, String status) {
        String sql = """
                update ret_out_his
                set intg_upd_status = %s
                where date(ret_out_date) between '%s' and '%s'
                """.formatted(status, fromDate, toDate);
        executeSql(sql);
    }

    private void syncOPD(String fromDate, String toDate, String status) {
        String sql = """
                update opd_his
                set intg_upd_status = %s
                where date(opd_date) between '%s' and '%s'
                """.formatted(status, fromDate, toDate);
        executeSql(sql);
    }

    private void syncOT(String fromDate, String toDate, String status) {
        String sql = """
                update ot_his
                set intg_upd_status = %s
                where date(ot_date) between '%s' and '%s'
                """.formatted(status, fromDate, toDate);
        executeSql(sql);
    }

    private void syncDC(String fromDate, String toDate, String status) {
        String sql = """
                update dc_his
                set intg_upd_status = %s
                where date(dc_date) between '%s' and '%s'
                """.formatted(status, fromDate, toDate);
        executeSql(sql);
    }

    private void syncPayment(String fromDate, String toDate, String status) {
        String sql = """
                update payment_his
                set intg_upd_status = %s
                where date(pay_date) between '%s' and '%s'
                """.formatted(status, fromDate, toDate);
        executeSql(sql);
    }

    private void syncOPDReceive(String fromDate, String toDate, String status) {
        String sql = """
                update opd_patient_bill_payment
                set intg_upd_status = %s
                where date(pay_date) between '%s' and '%s'
                """.formatted(status, fromDate, toDate);
        executeSql(sql);
    }

    private void syncExpense(String fromDate, String toDate, String status) {
        String sql = """
                update gen_expense
                set intg_upd_status = %s
                where date(exp_date) between '%s' and '%s'
                """.formatted(status, fromDate, toDate);
        executeSql(sql);
    }


    public ResultSet getResult(String sql) {
        return jdbcTemplate.execute((ConnectionCallback<ResultSet>) con -> {
            Statement stmt = con.createStatement();
            return stmt.executeQuery(sql);
        });
    }

    @Transactional
    public void executeSql(String... sql) {
        for (String s : sql) {
            jdbcTemplate.execute(s);
        }
    }
}
