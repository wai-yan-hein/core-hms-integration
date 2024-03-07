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
                "           prm_tran_option\n" +
                "      from (\n" +
                "            select vso.med_id, sum(ifnull(vso.op_smallest_qty,0)) ttl_qty\n" +
                "              from v_stock_op vso, tmp_stock_filter tsf\n" +
                "             where vso.location = tsf.location_id and vso.med_id = tsf.med_id\n" +
                "               and vso.op_date = tsf.op_date and tsf.user_id = prm_user_id\n" +
                "             group by vso.med_id\n" +
                "             union all\n" +
                "            select vs.med_id, sum((ifnull(vs.sale_smallest_qty, 0)+ifnull(vs.foc_smallest_qty,0))*-1) ttl_qty\n" +
                "              from v_sale1 vs, tmp_stock_filter tsf\n" +
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
                "     group by A.med_id";
            strSql1 = strSql1
                .replace("prm_stock_date", "'" + getTodayDate() + "'")
                .replace("prm_tran_option", "'Opening'")
                .replace("prm_user_id", "'999'");
            //log.info("calculateCostBalance : sql : " + strSql1);
            jdbcTemplate.execute(strSql1);
        }catch (Exception ex){
            log.error("calculateCostBalance : " + ex.getMessage());
        }
    }

    private void insertCostDetail(){
        String userId = "999";
        String costFor = "Opening";
        String costDate = getTodayDate();
        String strDelete = "delete from tmp_costing_detail where cost_for = '" + costFor + "' and user_id = '" + userId + "'";

        try{
            jdbcTemplate.execute(strDelete);
            ResultSet rs = jdbcTemplate.execute((ConnectionCallback<? extends ResultSet>) con -> {
                Statement stmt = con.createStatement();
                String strSql = "select tsc.med_id item_id, bal_qty ttl_stock, cost_price.tran_date, cost_price.tran_option, \n"
                        + "             cost_price.ttl_qty, cost_price.smallest_cost, cost_price, item_unit\n"
                        + "        from tmp_stock_costing tsc, \n"
                        + "             (select 'Purchase' tran_option, vpur.med_id item_id, pur_date tran_date, \n"
                        + "                     sum(pur_smallest_qty+ifnull(pur_foc_smallest_qty,0)) ttl_qty, pur_unit_cost cost_price, \n"
                        + "                     (pur_unit_cost/vm.smallest_qty) smallest_cost, vpur.pur_unit item_unit\n"
                        + "                from v_purchase vpur, (select med_id, min(op_date) op_date\n"
                        + "								            from tmp_stock_filter where user_id = prm_user_id\n"
                        + "                                        group by med_id) tsf,\n"
                        + "				 v_medicine vm\n"
                        + "        where vpur.med_id = tsf.med_id and deleted = false and date(pur_date) >= op_date\n"
                        + "			 and vpur.med_id = vm.med_id and vpur.pur_unit = vm.item_unit\n"
                        + "			 and date(pur_date) <= prm_cost_date and vm.active = true\n"
                        + "        group by vpur.med_id, pur_date, pur_unit_cost, vpur.pur_unit\n"
                        + "        union all\n"
                        + "       select 'Opening' tran_option, vso.med_id item_id, vso.op_date tran_date, \n"
                        + "				 sum(vso.op_smallest_qty) ttl_qty, vso.cost_price, \n"
                        + "				 (vso.cost_price/vm.smallest_qty) smallest_cost, vso.item_unit\n"
                        + "         from v_stock_op vso, tmp_stock_filter tsf, v_medicine vm\n"
                        + "		   where vso.med_id = tsf.med_id and vso.location = tsf.location_id\n"
                        + "          and vso.med_id = vm.med_id and vso.item_unit = vm.item_unit\n"
                        + "          and vso.op_date = tsf.op_date and tsf.user_id = prm_user_id\n"
                        + "			 and vm.active = true\n"
                        + "        group by vso.med_id, vso.op_date, vso.cost_price, vso.item_unit) cost_price\n"
                        + "   where tsc.med_id = cost_price.item_id and tsc.user_id = prm_user_id and tsc.tran_option = prm_cost_for\n"
                        + "   order by item_id, cost_price.tran_date desc, cost_price desc";
                strSql = strSql
                        .replace("prm_user_id", "'" + userId + "'")
                        .replace("prm_cost_for", "'" +costFor + "'")
                        .replace("prm_cost_date", "'" +costDate + "'");
                return stmt.executeQuery(strSql);
            });

            if(rs != null){
                String prvItemId = "-";
                String itemId;
                Double totalStock;
                String tranDate;
                String tranOption;
                Double ttlQty;
                Double smallestCost;
                Double unitCost;
                String unit;
                Double leftStock = 0.0;
                Double prvTtlStock = 0.0;
                String prvTranDate = "-";
                Double prvTtlQty = 0.0;
                Double prvCost = 0.0;
                Double prvLeftStock = 0.0;
                Double prvSmallestCost = 0.0;
                String prvTranOption = "-";
                Double costQty;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                while (rs.next()) {
                    itemId = rs.getString("item_id");
                    if (itemId.equals("101094")) {
                        log.info("Error Tran : " + itemId);
                    }
                    totalStock = rs.getDouble("ttl_stock");
                    tranDate = formatter.format(rs.getDate("tran_date"));
                    tranOption = rs.getString("tran_option");
                    ttlQty = rs.getDouble("ttl_qty");
                    smallestCost = rs.getDouble("smallest_cost");
                    unitCost = rs.getDouble("cost_price");
                    unit = rs.getString("item_unit");

                    if (!prvItemId.equals(itemId)) {
                        if (leftStock > 0.0) {
                            String tmpSql = "insert into tmp_costing_detail(item_id, ttl_stock, tran_date, tran_option, ttl_qty, \n"
                                    + "					cost_price, cost_qty, smallest_cost, user_id, cost_for, item_unit)\n"
                                    + "		values('" + prvItemId + "', " + prvTtlStock + ", '" + prvTranDate
                                    + "', 'ERR', " + prvTtlQty + ",\n"
                                    + prvCost + ", " + prvLeftStock + ", " + prvSmallestCost + ", '" + userId + "',\n'"
                                    + costFor + "', '" + unit + "')";
                            jdbcTemplate.execute(tmpSql);
                        }

                        prvItemId = itemId;
                        prvTtlStock = totalStock;
                        prvTranDate = tranDate;
                        prvTranOption = tranOption;
                        prvTtlQty = ttlQty;
                        prvSmallestCost = smallestCost;
                        prvCost = unitCost;
                        leftStock = totalStock;
                    }

                    if (leftStock > 0) {
                        if (leftStock >= ttlQty) {
                            costQty = ttlQty;
                            leftStock = leftStock - ttlQty;
                        } else {
                            costQty = leftStock;
                            leftStock = 0.0;
                        }

                        if (costQty > 0) {
                            String tmpSql1 = "insert into tmp_costing_detail(item_id, ttl_stock, tran_date, tran_option, ttl_qty, \n"
                                    + "					cost_price, cost_qty, smallest_cost, user_id, cost_for, item_unit)\n"
                                    + "	  values('" + itemId + "', " + totalStock + ", '" + tranDate + "', '" + tranOption + "', " + ttlQty + ",\n"
                                    + unitCost + ", " + costQty + ", " + smallestCost + ", '" + userId + "',\n'"
                                    + costFor + "' , '" + unit + "')";
                            jdbcTemplate.execute(tmpSql1);
                        }
                    }
                }
            }

            String sqlUpdate1 = "update tmp_costing_detail tcd, (\n" +
                "select user_id, item_id, sum(ttl_qty) ttl_qty, sum(ttl_qty*smallest_cost) ttl_amt, \n" +
                "       (sum(ttl_qty*smallest_cost)/if(sum(ttl_qty)=0,1,sum(ttl_qty))) as avg_cost\n" +
                "  from tmp_costing_detail\n" +
                " where user_id = prm_user_id and cost_for = prm_cost_for\n" +
                " group by user_id,item_id) avgc\n" +
                "   set tcd.smallest_cost = avgc.avg_cost\n" +
                " where tcd.item_id = avgc.item_id and tcd.user_id = avgc.user_id and tcd.user_id = prm_user_id";
            sqlUpdate1 = sqlUpdate1
                .replace("prm_user_id", "'" + userId + "'")
                .replace("prm_cost_for", "'" + costFor + "'");
            jdbcTemplate.execute(sqlUpdate1);

            String sqlUpdate2 = "update tmp_stock_costing tsc, \n" +
                "(select user_id, item_id, sum(cost_qty*smallest_cost) ttl_cost\n" +
                "  from tmp_costing_detail\n" +
                " where user_id = prm_user_id and cost_for = prm_cost_for\n" +
                " group by user_id,item_id) cd\n" +
                "   set total_cost = cd.ttl_cost\n" +
                " where tsc.med_id = cd.item_id and tsc.user_id = cd.user_id\n" +
                "   and tsc.user_id = prm_user_id and tran_option = prm_cost_for";
            sqlUpdate2 = sqlUpdate2
                .replace("prm_user_id", "'" + userId + "'")
                .replace("prm_cost_for", "'" + costFor + "'");
            jdbcTemplate.execute(sqlUpdate2);
        }catch (Exception ex){
            log.error("insertCostDetail : " + ex.getMessage());
        }
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
                    insertCostDetail();

                    //Update to med usage
                    String updScript = "update med_usaged mu join (select med_id, bal_qty, total_cost, if(ifnull(bal_qty,0) <= 0,0,ifnull(total_cost,0)/bal_qty) as small_cost \n" +
                            "from tmp_stock_costing where user_id = '999' and ifnull(bal_qty,0)>=0) ct on mu.med_id = ct.med_id\n" +
                            "set mu.smallest_cost = small_cost, mu.total_cost = ifnull(mu.qty_smallest,0)*ct.small_cost, status = 'ACK' \n";
                    String strFilter = "";
                    if(!vouTypes.isEmpty()){
                        if(strFilter.isEmpty()){
                            strFilter = "mu.vou_type in (" + vouTypes + ")\n";
                        } else {
                            strFilter = strFilter + " and mu.vou_type in (" + vouTypes + ")\n";
                        }
                    }

                    if(!vouNos.isEmpty()){
                        if(strFilter.isEmpty()){
                            strFilter = "mu.vou_no in (" + vouNos + ")\n";
                        } else {
                            strFilter = strFilter + " and mu.vou_no in (" + vouNos + ")\n";
                        }
                    }

                    if(!serviceIds.isEmpty()){
                        if(strFilter.isEmpty()){
                            strFilter = "mu.service_id in (" + serviceIds + ")\n";
                        } else {
                            strFilter = strFilter + " and mu.service_id in (" + serviceIds + ")\n";
                        }
                    }

                    if(!medIds.isEmpty()){
                        if(strFilter.isEmpty()){
                            strFilter = "mu.med_id in (" + medIds + ")\n";
                        } else {
                            strFilter = strFilter + " and mu.med_id in (" + medIds + ")\n";
                        }
                    }

                    if(!strFilter.isEmpty()){
                        log.info("updateLabCost : updateSql : " + updScript + " where " + strFilter);
                        jdbcTemplate.execute(updScript + " where " + strFilter);
                    }
                }
            }
        }catch (Exception ex){
            log.error("updateLabCost : " + ex.getMessage());
        }
    }
}
