package com.cv.integration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Service
@Transactional
@Slf4j
public class LabCostUpdateServiceImpl implements LabCostUpdateService{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String getTodayDate(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(Calendar.getInstance().getTime());
    }

    private void insertStockFilterCodeMed(String medId) {
        try {
            String strSQLDelete = "delete from tmp_stock_filter where user_id = '999'";
            jdbcTemplate.execute(strSQLDelete);
            String strSQL = "insert into tmp_stock_filter select m.location_id, m.med_id, "
                    + " ifnull(meod.op_date, '1900-01-01'),'999' from v_med_loc m left join "
                    + "(select location_id, med_id, max(op_date) op_date from med_op_date "
                    + " where op_date < '" + getTodayDate() + "'";

            strSQL = strSQL + " group by location_id, med_id) meod on m.med_id = meod.med_id "
                    + " and m.location_id = meod.location_id where (m.active = true or m.active = false) "
                    + "and m.calc_stock = true and m.med_id in (" + medId + ")";
            jdbcTemplate.execute(strSQL);
        }catch (Exception ex){
            log.error("insertStockFilterCodeMed : " + ex.getMessage());
        }
    }

    private void calculateCostBalance() {
        try{
            jdbcTemplate.execute("delete from tmp_costing_detail where user_id = '999'");
            jdbcTemplate.execute("delete from tmp_stock_costing where user_id = '999'");
            String strMethod = "AVG";
            String strSql1 = "insert into tmp_stock_costing(med_id, user_id, bal_qty, qty_str, tran_option)\n" +
                    "    select A.med_id, prm_user_id, sum(A.ttl_qty),\n" +
                    "           get_qty_in_str(ifnull(sum(A.ttl_qty),0), B.unit_smallest, B.unit_str),\n" +
                    "\t\t   prm_tran_option\n" +
                    "      from (\n" +
                    "            select vso.med_id, sum(ifnull(vso.op_smallest_qty,0)) ttl_qty\n" +
                    "              from v_stock_op vso, tmp_stock_filter tsf\n" +
                    "             where vso.location = tsf.location_id and vso.med_id = tsf.med_id\n" +
                    "               and vso.op_date = tsf.op_date and tsf.user_id = prm_user_id\n" +
                    "             group by vso.med_id\n" +
                    "             union all\n" +
                    "            select vs.med_id, sum((ifnull(vs.sale_smallest_qty, 0)+ifnull(vs.foc_smallest_qty,0))*-1) ttl_qty\n" +
                    "              from v_sale vs, tmp_stock_filter tsf\n" +
                    "             where vs.location_id = tsf.location_id and vs.med_id = tsf.med_id\n" +
                    "               and date(vs.sale_date) >= tsf.op_date and date(vs.sale_date) <= prm_stock_date\n" +
                    "               and vs.deleted = false and vs.vou_status = 1 and tsf.user_id = prm_user_id\n" +
                    "             group by vs.med_id\n" +
                    "             union all\n" +
                    "            select vp.med_id, sum(ifnull(vp.pur_smallest_qty,0)+ifnull(vp.pur_foc_smallest_qty,0)) ttl_qty\n" +
                    "              from v_purchase vp, tmp_stock_filter tsf\n" +
                    "             where vp.location = tsf.location_id and vp.med_id = tsf.med_id\n" +
                    "               and date(vp.pur_date) >= tsf.op_date and date(vp.pur_date) <= prm_stock_date\n" +
                    "               and vp.deleted = false and vp.vou_status = 1 and tsf.user_id = prm_user_id\n" +
                    "             group by vp.med_id\n" +
                    "             union all\n" +
                    "            select vri.med_id, sum(ifnull(vri.ret_in_smallest_qty,0)) ttl_qty\n" +
                    "              from v_return_in vri, tmp_stock_filter tsf\n" +
                    "             where vri.location = tsf.location_id and vri.med_id = tsf.med_id\n" +
                    "               and date(vri.ret_in_date) >= tsf.op_date and tsf.user_id = prm_user_id\n" +
                    "               and date(vri.ret_in_date) <= prm_stock_date and vri.deleted = false\n" +
                    "             group by vri.med_id\n" +
                    "             union all\n" +
                    "            select vro.med_id, sum(ifnull(ret_out_smallest_qty,0)*-1) ttl_qty\n" +
                    "              from v_return_out vro, tmp_stock_filter tsf\n" +
                    "             where vro.location = tsf.location_id and vro.med_id = tsf.med_id\n" +
                    "               and date(vro.ret_out_date) >= tsf.op_date and tsf.user_id = prm_user_id\n" +
                    "               and date(vro.ret_out_date) <= prm_stock_date and vro.deleted = false\n" +
                    "             group by vro.med_id\n" +
                    "             union all\n" +
                    "            select va.med_id, sum(if(va.adj_type = '-',(ifnull(va.adj_smallest_qty,0)*-1),\n" +
                    "                        ifnull(va.adj_smallest_qty,0))) ttl_qty\n" +
                    "              from v_adj va, tmp_stock_filter tsf\n" +
                    "             where va.location = tsf.location_id and va.med_id = tsf.med_id\n" +
                    "               and date(va.adj_date) >= tsf.op_date and tsf.user_id = prm_user_id\n" +
                    "               and date(va.adj_date) <= prm_stock_date and va.deleted = false\n" +
                    "             group by va.med_id\n" +
                    "             union all\n" +
                    "            select vt.med_id, sum(ifnull(tran_smallest_qty,0)*-1) ttl_qty\n" +
                    "              from v_transfer vt, tmp_stock_filter tsf\n" +
                    "             where vt.from_location = tsf.location_id and vt.med_id = tsf.med_id\n" +
                    "               and date(vt.tran_date) >= tsf.op_date and tsf.user_id = prm_user_id\n" +
                    "               and date(vt.tran_date) <= prm_stock_date and vt.deleted = false\n" +
                    "             group by vt.med_id\n" +
                    "             union all\n" +
                    "            select vt.med_id, sum(ifnull(tran_smallest_qty,0)) ttl_qty\n" +
                    "              from v_transfer vt, tmp_stock_filter tsf\n" +
                    "             where vt.to_location = tsf.location_id and vt.med_id = tsf.med_id\n" +
                    "               and date(vt.tran_date) >= tsf.op_date and tsf.user_id = prm_user_id\n" +
                    "               and date(vt.tran_date) <= prm_stock_date and vt.deleted = false\n" +
                    "             group by vt.med_id\n" +
                    "             union all\n" +
                    "            select vsi.med_id, sum(ifnull(smallest_qty,0)*-1) ttl_qty\n" +
                    "              from v_stock_issue vsi, tmp_stock_filter tsf\n" +
                    "             where vsi.location_id = tsf.location_id and vsi.med_id = tsf.med_id\n" +
                    "               and date(vsi.issue_date) >= tsf.op_date and tsf.user_id = prm_user_id\n" +
                    "               and date(vsi.issue_date) <= prm_stock_date and vsi.deleted = false\n" +
                    "             group by vsi.med_id\n" +
                    "             union all\n" +
                    "            select vsr.rec_med_id med_id, sum(ifnull(smallest_qty,0)) ttl_qty\n" +
                    "              from v_stock_receive vsr, tmp_stock_filter tsf\n" +
                    "             where vsr.location_id = tsf.location_id and vsr.rec_med_id = tsf.med_id\n" +
                    "               and date(vsr.receive_date) >= tsf.op_date and tsf.user_id = prm_user_id\n" +
                    "               and date(vsr.receive_date) <= prm_stock_date and vsr.deleted = false\n" +
                    "             group by vsr.rec_med_id\n" +
                    "             union all\n" +
                    "            select vd.med_id, sum(ifnull(dmg_smallest_qty,0)*-1) ttl_qty\n" +
                    "              from v_damage vd, tmp_stock_filter tsf\n" +
                    "             where vd.location = tsf.location_id and vd.med_id = tsf.med_id\n" +
                    "               and date(vd.dmg_date) >= tsf.op_date and tsf.user_id = prm_user_id\n" +
                    "               and date(vd.dmg_date) <= prm_stock_date and vd.deleted = false\n" +
                    "             group by vd.med_id\n" +
                    "             union all \n" +
                    "            select mu.med_id, sum(ifnull(mu.qty_smallest,0)*-1) ttl_qty\n" +
                    "              from med_usaged mu, tmp_stock_filter tsf \n" +
                    "             where mu.location_id = tsf.location_id and mu.med_id = tsf.med_id\n" +
                    "               and date(mu.created_date) between tsf.op_date and prm_stock_date and tsf.user_id = prm_user_id\n" +
                    "             group by mu.med_id\n" +
                    "             union all\n" +
                    "            select a.med_id, (sum(a.ttl)*-1) as ttl_qty\n" +
                    "              from (select vsi.med_id, sum(ifnull(vsi.smallest_qty,0))*-1 as ttl\n" +
                    "                      from v_stock_issue vsi, tmp_stock_filter tsf\n" +
                    "                     where vsi.issue_opt = 'Borrow' and vsi.deleted = false\n" +
                    "                       and vsi.med_id = tsf.med_id and vsi.location_id = tsf.location_id\n" +
                    "                       and tsf.user_id = prm_user_id and vsi.issue_date <= prm_stock_date\n" +
                    "                     group by med_id\n" +
                    "                     union all\n" +
                    "                    select vsr.rec_med_id as med_id, sum(ifnull(vsr.smallest_qty,0)) as ttl\n" +
                    "                      from v_stock_receive vsr, tmp_stock_filter tsf\n" +
                    "                     where vsr.rec_option = 'Borrow' and vsr.deleted = false\n" +
                    "                       and vsr.rec_med_id = tsf.med_id and vsr.location_id = tsf.location_id\n" +
                    "                       and tsf.user_id = prm_user_id and vsr.receive_date <= prm_stock_date\n" +
                    "                     group by vsr.rec_med_id) a\n" +
                    "         group by a.med_id\n" +
                    "             ) A,\n" +
                    "            v_med_unit_smallest_rel B\n" +
                    "     where A.med_id = B.med_id\n" +
                    "     group by A.med_id"
                            .replace("prm_stock_date", "'" + getTodayDate() + "'")
                            .replace("prm_tran_option", "'Opening'")
                            .replace("prm_user_id", "'999'");
            jdbcTemplate.execute(strSql1);
        }catch (Exception ex){
            log.error("calculateMed : " + ex.getMessage());
        }
    }

    private void insertCostDetail(){

    }

    @Override
    public void updateLabCost(){

        try {
            ResultSet rs = jdbcTemplate.execute((ConnectionCallback<ResultSet>) con -> {
                Statement stmt = con.createStatement();
                String strSql = "SELECT vou_type, vou_no, service_id, med_id\n" +
                        "  FROM med_usaged mu \n" +
                        " WHERE mu.status is null";
                return stmt.executeQuery(strSql);
            });

            if(rs != null){
                String vouTypes = "";
                String vouNos = "";
                String serviceIds = "";
                String medIds = "";
                while (rs.next()){
                    if(vouTypes.isEmpty()){
                        vouTypes = "'" + rs.getString("vou_type") + "'";
                    } else {
                        if(!vouTypes.contains(rs.getString("vou_type"))) {
                            vouTypes = vouTypes.concat(",'").concat(rs.getString("vou_type")).concat("'");
                        }
                    }

                    if(vouNos.isEmpty()){
                        vouNos = "'" + rs.getString("vou_no") + "'";
                    } else {
                        if(!vouNos.contains(rs.getString("vou_no"))) {
                            vouNos = vouNos.concat(",'").concat(rs.getString("vou_no")).concat("'");
                        }
                    }

                    if(serviceIds.isEmpty()){
                        serviceIds = rs.getString("service_id");
                    } else {
                        if(!serviceIds.contains(rs.getString("service_id"))) {
                            serviceIds = serviceIds.concat(",").concat(rs.getString("service_id"));
                        }
                    }

                    if(medIds.isEmpty()){
                        medIds = "'" + rs.getString("med_id") + "'";
                    } else {
                        if(!medIds.contains(rs.getString("med_id"))) {
                            medIds = medIds.concat(",'").concat(rs.getString("med_id")).concat("'");
                        }
                    }
                }

                log.info("vouTypes : " + vouTypes);
                log.info("vouNos : " + vouNos);
                log.info("serviceIds : " + serviceIds);
                log.info("medIds : " + medIds);

                if(!medIds.isEmpty()){
                    insertStockFilterCodeMed(medIds);
                    calculateCostBalance();
                }
            }
        }catch (Exception ex){
            log.error("updateLabCost : " + ex.getMessage());
        }
    }
}
