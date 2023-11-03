package com.cv.integration.listener;

import com.cv.integration.common.Util1;
import com.cv.integration.entity.*;
import com.cv.integration.model.*;
import com.cv.integration.repo.*;
import com.cv.integration.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
@PropertySource("file:config/application.properties")
public class HMSIntegration {
    @Autowired
    private final AccountSettingRepo accountSetting;
    @Autowired
    private final SaleHisRepo saleHisRepo;
    @Autowired
    private final PurHisRepo purHisRepo;
    @Autowired
    private final ReturnInRepo returnInRepo;
    @Autowired
    private final ReturnOutRepo returnOutRepo;
    @Autowired
    private final OPDHisRepo opdHisRepo;
    @Autowired
    private final OTHisRepo otHisRepo;
    @Autowired
    private final OTHisDetailRepo otHisDetailRepo;
    @Autowired
    private final DCHisRepo dcHisRepo;
    @Autowired
    private final DCHisDetailRepo dcHisDetailRepo;
    @Autowired
    private final OPDHisDetailRepo opdHisDetailRepo;
    @Autowired
    private final OPDReceiveRepo opdReceiveRepo;
    @Autowired
    private final TraderRepo traderRepo;

    @Autowired
    private final GenExpenseRepo genExpenseRepo;
    @Autowired
    private final PaymentHisRepo paymentHisRepo;
    @Autowired
    private final DCDoctorFeeRepo dcDoctorFeeRepo;
    @Autowired
    private final OTDoctorFeeRepo otDoctorFeeRepo;
    @Autowired
    private final TraderOpeningRepo traderOpeningRepo;
    @Autowired
    private final OTServiceRepo otServiceRepo;
    @Autowired
    private final DCServiceRepo dcServiceRepo;
    @Autowired
    private final String packageId;
    @Autowired
    private OPDCategoryRepo opdCategoryRepo;
    @Autowired
    private DCGroupRepo dcGroupRepo;
    @Autowired
    private ReportService reportService;
    @Autowired
    private final Map<String, AccountSetting> hmAccSetting;
    @Autowired
    private final String dcDepositId;
    @Autowired
    private final String dcDiscountId;
    @Autowired
    private final String dcPaidId;
    @Autowired
    private final String dcRefundId;
    @Autowired
    private final String otDepositId;
    @Autowired
    private final String otDiscountId;
    @Autowired
    private final String otPaidId;
    @Autowired
    private final String otRefundId;
    @Autowired
    private OTGroupRepo otGroupRepo;
    @Value("${upload.trader}")
    private String uploadTrader;
    @Value("${upload.sale}")
    private String uploadSale;
    @Value("${upload.purchase}")
    private String uploadPurchase;
    @Value("${upload.retin}")
    private String uploadReturnIn;
    @Value("${upload.retout}")
    private String uploadReturnOut;
    @Value("${upload.opd}")
    private String uploadOPD;
    @Value("${upload.ot}")
    private String uploadOT;
    @Value("${upload.dc}")
    private String uploadDC;
    @Value("${upload.payment}")
    private String uploadPayment;
    @Value("${upload.expense}")
    private String uploadExpense;
    @Value("${upload.bill}")
    private String uploadOPDBill;
    @Value("${account.compcode}")
    private String compCode;
    @Value("${account.department}")
    private String deptCode;
    @Value("${account.inpatient.code}")
    private String inPatientCode;
    @Value("${account.outpatient.code}")
    private String outPatientCode;
    @Value("${app.type}")
    private String appType;
    private final String ACK = "ACK";
    private final String APP_NAME = "CM";
    private final Integer MAC_ID = 99;
    private final String FOC = "FOC";
    private final String ERR = "ERR";
    @Autowired
    private WebClient accountApi;
    @Autowired
    private UserRepo userRepo;

    private void sendAccount(List<Gl> glList) {
        if (!glList.isEmpty()) {
            Response response = accountApi.post().uri("/account/saveGlList")
                    .body(Mono.just(glList), List.class)
                    .retrieve()
                    .bodyToMono(Response.class)
                    //.retryWhen(Retry.backoff(3, Duration.ofSeconds(10))
                    //.maxBackoff(Duration.ofMinutes(1))) // Retry 3 times with a backoff of 10 seconds
                    //.timeout(Duration.ofSeconds(1)) // Timeout after 5 minutes
                    .onErrorResume(throwable -> {
                        // Handle the error case when the server is down
                        log.error("sendAccount: " + throwable.getMessage());
                        String code = glList.get(0).getRefNo();
                        String tranSource = glList.get(0).getTranSource();
                        update(tranSource, code, null);
                        return Mono.empty(); // Return an empty Mono or a default response
                    }).block();
            if (response != null) {
                String code = response.getVouNo();
                String tranSource = response.getTranSource();
                update(tranSource, code, ACK);
            }
        }
    }


    private void update(String tranSource, String code, String status) {
        switch (tranSource) {
            case "SALE" -> updateSale(code, status);
            case "PURCHASE" -> updatePurchase(code, status);
            case "RETURN_IN" -> updateReturnIn(code, status);
            case "RETURN_OUT" -> updateReturnOut(code, status);
            case "OPD" -> updateOPD(code, status);
            case "OT" -> updateOT(code, status);
            case "DC" -> updateDC(code, status);
            case "BILL_PAYMENT" -> updateOPDReceive(Integer.parseInt(code), status);
            case "EXPENSE", "UNPAID" -> updateExpense(Integer.parseInt(code), status);
            case "PAYMENT" -> updatePayment(Integer.parseInt(code), status);
            case "OPENING" -> updateOpening(code, status);
            case "COA_OPD" -> updateOPDCOA(code, status);
            case "COA_OT" -> updateOTCOA(code, status);
            case "COA_DC" -> updateDCCOA(code, status);
        }
    }

    public Mono<String> saveCOA(ChartOfAccount coa) {
        return accountApi.post().uri("/account/processCOA")
                .body(Mono.just(coa), ChartOfAccount.class)
                .retrieve()
                .bodyToMono(String.class);
    }

    public void saveOpening(COAOpening op) {
        op = accountApi.post().uri("/account/saveOpening").body(Mono.just(op), COAOpening.class).retrieve().bodyToMono(COAOpening.class).block();
        assert op != null;
        updateOpening(op.getTraderCode(), ACK);
    }


    private void deleteGl(String tranSource, String vouNo,
                          String srcAcc, Integer chargeType) {
        if (chargeType == 2) {// mean FOC
            Gl gl = new Gl();
            GlKey key = new GlKey();
            key.setCompCode(compCode);
            gl.setKey(key);
            gl.setTranSource(tranSource);
            gl.setRefNo(vouNo);
            gl.setSrcAccCode(srcAcc);
            if (srcAcc != null) {
                accountApi.post().uri("/account/deleteGlByAccount")
                        .body(Mono.just(gl), Gl.class).retrieve()
                        .bodyToMono(String.class).subscribe((t) -> update(tranSource, vouNo, ACK), (e) -> log.info(e.getMessage()));
            } else {
                accountApi.post().uri("/account/deleteGlByVoucher")
                        .body(Mono.just(gl), Gl.class)
                        .retrieve().bodyToMono(String.class)
                        .subscribe((t) -> update(tranSource, vouNo, ACK), (e) -> log.info(e.getMessage()));
            }
        }

    }


    private void updateOPDCOA(String code, String status) {
        if (code != null) {
            code = code.replaceAll("^\"|\"$", ""); // Remove surrounding quotes
            String[] split = code.split(",");
            Integer groupId = Util1.getInteger(split[0]);
            String coaCode = split[1];
            opdCategoryRepo.updateOPDCategory(groupId, status, coaCode);
            log.info(String.format("updateOPDCOA: %s", code));
        }

    }

    private void updateOTCOA(String code, String status) {
        if (code != null) {
            code = code.replaceAll("^\"|\"$", ""); // Remove surrounding quotes
            String[] split = code.split(",");
            Integer groupId = Util1.getInteger(split[0]);
            String coaCode = split[1];
            otGroupRepo.updateOTGroup(groupId, status, coaCode);
            log.info(String.format("updateOTCOA: %s", code));
        }

    }

    private void updateDCCOA(String code, String status) {
        if (code != null) {
            code = code.replaceAll("^\"|\"$", ""); // Remove surrounding quotes
            String[] split = code.split(",");
            Integer groupId = Util1.getInteger(split[0]);
            String coaCode = split[1];
            dcGroupRepo.updateDCGroup(groupId, status, coaCode);
            log.info(String.format("updateDCCOA: %s", code));
        }
    }

    private void updateOpening(String code, String status) {
        traderOpeningRepo.updateOpening(code, status);
        log.info(String.format("updateOpening: %s", code));
    }

    public void sendTraderOpening(TraderOpening op) {
        Trader trader = op.getKey().getTrader();
        String type = trader.getTraderType();
        double opAmt = op.getAmount();
        COAOpening opening = new COAOpening();
        OpeningKey key = new OpeningKey();
        key.setCompCode(compCode);
        opening.setKey(key);
        opening.setOpDate(op.getKey().getOpDate());
        opening.setTraderCode(op.getKey().getTrader().getTraderCode());
        opening.setCurCode(op.getKey().getCurCode());
        opening.setDeptCode(deptCode);
        if (type.equals("C")) {
            if (opAmt > 0) {
                opening.setCrAmt(0.0);
                opening.setDrAmt(opAmt);
            } else {
                opening.setCrAmt(opAmt * -1);
                opening.setDrAmt(0.0);
            }
        } else {
            if (opAmt > 0) {
                opening.setDrAmt(0.0);
                opening.setCrAmt(opAmt);
            } else {
                opening.setDrAmt(opAmt * -1);
                opening.setCrAmt(0.0);
            }
        }
        opening.setCreatedDate(LocalDateTime.now());
        saveOpening(opening);
    }

    public void sendTrader(Trader t) {
        if (Util1.getBoolean(uploadTrader)) {
            if (t.isActive()) {
                String traderType = t.getTraderType();
                String code = t.getTraderCode();
                AccTrader accTrader = new AccTrader();
                TraderKey key = new TraderKey();
                key.setCode(code);
                key.setCompCode(compCode);
                accTrader.setKey(key);
                String userCode = t.getUserCode();
                accTrader.setTraderName(t.getTraderName());
                accTrader.setUserCode(Util1.isNull(userCode, code));
                accTrader.setActive(true);
                accTrader.setAppName(APP_NAME);
                accTrader.setMacId(MAC_ID);
                accTrader.setCreatedBy(APP_NAME);
                accTrader.setTraderType(traderType);
                accTrader.setAccount(traderType.equals("C") ? getCustomerAcc() : getSupplierAcc());
                try {
                    Mono<AccTrader> result = accountApi.post().uri("/account/saveTrader").body(Mono.just(accTrader), AccTrader.class).retrieve().bodyToMono(AccTrader.class).doOnError((e) -> log.error(e.getMessage()));
                    AccTrader trader = result.block();
                    assert trader != null;
                    updateTrader(trader);
                } catch (Exception e) {
                    log.error("sendTrader : " + e.getMessage());
                }
            }

        }
    }

    private String getCustomerAcc() {
        return userRepo.getProperty("debtor.account", compCode);
    }

    private String getSupplierAcc() {
        return userRepo.getProperty("creditor.account", compCode);
    }


    private void updateTrader(AccTrader accTrader) {
        String traderCode = accTrader.getKey().getCode();
        Optional<Trader> optionalTrader = traderRepo.findById(traderCode);
        if (optionalTrader.isPresent()) {
            Trader t = optionalTrader.get();
            t.setIntgUpdStatus("ACK");
            t.setAccount(accTrader.getAccount());
            traderRepo.save(t);
            log.info(String.format("updateTrader: %s", traderCode));
        } else {
            log.error(String.format("Trader with code %s not found in the repository.", traderCode));
        }
    }


    public void sendSaleVoucherToAccount(SaleHis sh) {
        if (Util1.getBoolean(uploadSale)) {
            String tranSource = "SALE";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (setting != null) {
                Integer vouStatusId = sh.getVouStatusId();
                String vouNo = sh.getVouNo();
                if (vouStatusId == 1) {
                    String srcAcc = setting.getSourceAcc();
                    String ipdSrc = setting.getIpdSource();
                    String payAcc = setting.getPayAcc();
                    String disAcc = setting.getDiscountAcc();
                    String balAcc = setting.getBalanceAcc();
                    String deptCode = setting.getDeptCode();
                    LocalDateTime vouDate = sh.getVouDate();
                    String traderCode = null;
                    String reference = null;
                    String accCodeByLoc = sh.getLocation().getAccCode();
                    String deptCodeByLoc = sh.getLocation().getDeptCode();
                    String traderByLoc = sh.getLocation().getTraderCode();
                    boolean admission = !Util1.isNullOrEmpty(sh.getAdmissionNo());
                    srcAcc = admission ? Util1.isNull(ipdSrc, srcAcc) : srcAcc;
                    srcAcc = Util1.isNull(accCodeByLoc, srcAcc);
                    deptCode = Util1.isNull(deptCodeByLoc, deptCode);
                    String patientType = Util1.isNullOrEmpty(sh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                    Patient p = sh.getPatient();
                    Trader trader = sh.getTrader();
                    if (!Objects.isNull(p)) {
                        String patientNo = p.getPatientNo();
                        String patientName = p.getPatientName();
                        traderCode = Util1.isNullOrEmpty(sh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                        reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                        TraderGroup g = p.getGroup();
                        if (g != null) {
                            traderCode = Util1.isNull(g.getTraderCode(), traderCode);
                            balAcc = Util1.isNull(g.getAccountId(), balAcc);
                        }
                    } else if (trader != null) {
                        traderCode = trader.getTraderCode();
                        balAcc = Util1.isNull(trader.getAccount(), balAcc);
                    } else if (appType.equals("H")) {
                        if (Util1.isNullOrEmpty(traderByLoc)) {
                            reference = String.format("%s : %s : (%s)", "-", Util1.isNull(sh.getName(), "-"), patientType);
                            traderCode = Util1.isNullOrEmpty(sh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                        }
                    }
                    String curCode = sh.getCurrency().getAccCurCode();
                    boolean deleted = sh.isDeleted();
                    double vouTotalAmt = Util1.getDouble(sh.getVouTotal());
                    double vouPaidAmt = Util1.getDouble(sh.getVouPaid());
                    double vouDisAmt = Util1.getDouble(sh.getVouDiscount());
                    double vouBalAmt = Util1.getDouble(sh.getVouBalance());
                    List<Gl> listGl = new ArrayList<>();
                    //income
                    if (vouBalAmt > 0) {
                        Gl gl = new Gl();
                        GlKey key = new GlKey();
                        key.setDeptId(1);
                        key.setCompCode(compCode);
                        gl.setKey(key);
                        gl.setGlDate(vouDate);
                        gl.setDescription("Sale Voucher Total");
                        gl.setSrcAccCode(srcAcc);
                        gl.setDeptCode(deptCode);
                        gl.setAccCode(balAcc);
                        gl.setTraderCode(traderCode);
                        gl.setCrAmt(vouTotalAmt);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setCreatedDate(LocalDateTime.now());
                        gl.setCreatedBy(APP_NAME);
                        gl.setTranSource(tranSource);
                        gl.setRefNo(vouNo);
                        gl.setDeleted(deleted);
                        gl.setMacId(MAC_ID);
                        listGl.add(gl);
                    }
                    //discount
                    if (vouDisAmt > 0) {
                        Gl gl = new Gl();
                        GlKey key = new GlKey();
                        key.setDeptId(1);
                        key.setCompCode(compCode);
                        gl.setKey(key);
                        if (vouPaidAmt > 0) {
                            gl.setSrcAccCode(payAcc);
                            gl.setCash(true);
                        } else {
                            gl.setSrcAccCode(balAcc);
                            gl.setTraderCode(traderCode);
                        }
                        gl.setGlDate(vouDate);
                        gl.setDescription("Sale Voucher Discount");
                        gl.setAccCode(disAcc);
                        gl.setCrAmt(vouDisAmt);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(deptCode);
                        gl.setCreatedDate(LocalDateTime.now());
                        gl.setCreatedBy(APP_NAME);
                        gl.setTranSource(tranSource);
                        gl.setRefNo(vouNo);
                        gl.setDeleted(deleted);
                        gl.setMacId(MAC_ID);
                        listGl.add(gl);
                    }
                    //payment
                    if (vouPaidAmt > 0) {
                        Gl gl = new Gl();
                        GlKey key = new GlKey();
                        key.setDeptId(1);
                        key.setCompCode(compCode);
                        gl.setKey(key);
                        gl.setGlDate(vouDate);
                        gl.setDescription("Sale Voucher Paid");
                        gl.setSrcAccCode(payAcc);
                        gl.setAccCode(srcAcc);
                        gl.setDrAmt(vouTotalAmt);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(deptCode);
                        gl.setCreatedDate(LocalDateTime.now());
                        gl.setCreatedBy(APP_NAME);
                        gl.setTranSource(tranSource);
                        gl.setRefNo(vouNo);
                        gl.setDeleted(deleted);
                        gl.setMacId(MAC_ID);
                        gl.setCash(true);
                        listGl.add(gl);
                    }

                    if (!listGl.isEmpty()) {
                        sendAccount(listGl);
                    } else {
                        deleteGl(tranSource, vouNo, null, 2);
                        saleHisRepo.updateSale(vouNo, FOC);
                        log.info("FOC");
                    }
                }

            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }

    public void updateSale(String vouNo, String status) {
        saleHisRepo.updateSale(vouNo, status);
        log.info(String.format("updateSale :%s", vouNo));
    }

    public void sendPurchaseVoucherToAccount(PurHis ph) {
        if (Util1.getBoolean(uploadPurchase)) {
            String tranSource = "PURCHASE";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                String vouNo = ph.getVouNo();
                String srcAcc = setting.getSourceAcc();
                String payAcc = setting.getPayAcc();
                String balAcc = setting.getBalanceAcc();
                String deptCode = setting.getDeptCode();
                LocalDateTime vouDate = ph.getVouDate();
                String curCode = ph.getCurrency().getAccCurCode();
                boolean deleted = ph.isDeleted();
                double vouPaidAmt = Util1.getDouble(ph.getVouPaid());
                double vouBalAmt = Util1.getDouble(ph.getVouBalance());
                Trader trader = ph.getTrader();
                String traderCode = trader.getTraderCode();
                balAcc = Util1.isNull(trader.getAccount(), balAcc);
                String reference = ph.getRemark();
                String deptCodeByLoc = ph.getLocation().getDeptCode();
                String accByLoc = ph.getLocation().getPurAccount();
                deptCode = Util1.isNull(deptCodeByLoc, deptCode);
                srcAcc = Util1.isNull(accByLoc, srcAcc);
                List<Gl> listGl = new ArrayList<>();
                //income
                if (vouBalAmt > 0) {
                    Gl gl = new Gl();
                    GlKey key = new GlKey();
                    key.setDeptId(1);
                    key.setCompCode(compCode);
                    gl.setKey(key);
                    gl.setGlDate(vouDate);
                    gl.setDescription("Purchase Voucher Total");
                    gl.setSrcAccCode(srcAcc);
                    gl.setAccCode(balAcc);
                    gl.setTraderCode(traderCode);
                    gl.setDrAmt(vouBalAmt);
                    gl.setCurCode(curCode);
                    gl.setReference(reference);
                    gl.setDeptCode(deptCode);
                    gl.setCreatedDate(LocalDateTime.now());
                    gl.setCreatedBy(APP_NAME);
                    gl.setTranSource(tranSource);
                    gl.setRefNo(vouNo);
                    gl.setDeleted(deleted);
                    gl.setMacId(MAC_ID);
                    listGl.add(gl);
                }
                //payment
                if (vouPaidAmt > 0) {
                    Gl gl = new Gl();
                    GlKey key = new GlKey();
                    key.setDeptId(1);
                    key.setCompCode(compCode);
                    gl.setKey(key);
                    gl.setGlDate(vouDate);
                    gl.setDescription("Purchase Voucher Paid");
                    gl.setSrcAccCode(payAcc);
                    gl.setAccCode(srcAcc);
                    gl.setCrAmt(vouPaidAmt);
                    gl.setCurCode(curCode);
                    gl.setReference(trader.getTraderName());
                    gl.setDeptCode(deptCode);
                    gl.setCreatedDate(LocalDateTime.now());
                    gl.setCreatedBy(APP_NAME);
                    gl.setTranSource(tranSource);
                    gl.setRefNo(vouNo);
                    gl.setDeleted(deleted);
                    gl.setMacId(MAC_ID);
                    gl.setCash(true);
                    listGl.add(gl);
                }
                if (!listGl.isEmpty()) {
                    sendAccount(listGl);
                } else {
                    deleteGl(tranSource, vouNo, null, 2);
                    purHisRepo.updatePurchase(vouNo, ACK);
                }
            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }

    public void updatePurchase(String vouNo, String status) {
        purHisRepo.updatePurchase(vouNo, status);
        log.info(String.format("updatePurchase %s", vouNo));
    }

    public void sendReturnInVoucherToAccount(RetInHis ri) {
        if (Util1.getBoolean(uploadReturnIn)) {
            String tranSource = "RETURN_IN";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                String vouNo = ri.getVouNo();
                String srcAcc = setting.getSourceAcc();
                String payAcc = setting.getPayAcc();
                String balAcc = setting.getBalanceAcc();
                String deptCode = setting.getDeptCode();
                LocalDateTime vouDate = ri.getVouDate();
                String traderCode = null;
                String reference = null;
                Patient p = ri.getPatient();
                Trader trader = ri.getTrader();
                if (!Objects.isNull(p)) {
                    String patientNo = p.getPatientNo();
                    String patientName = p.getPatientName();
                    String patientType = Util1.isNullOrEmpty(ri.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                    traderCode = Util1.isNullOrEmpty(ri.getAdmissionNo()) ? outPatientCode : inPatientCode;
                    reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                    TraderGroup g = p.getGroup();
                    if (g != null) {
                        traderCode = Util1.isNull(g.getTraderCode(), traderCode);
                        balAcc = Util1.isNull(g.getAccountId(), balAcc);
                    }
                } else if (trader != null) {
                    traderCode = trader.getTraderCode();
                    balAcc = Util1.isNull(trader.getAccount(), balAcc);
                } else if (appType.equals("H")) {
                    String patientType = Util1.isNullOrEmpty(ri.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                    reference = String.format("%s : %s : (%s)", "-", "-", patientType);
                    traderCode = Util1.isNullOrEmpty(ri.getAdmissionNo()) ? outPatientCode : inPatientCode;
                }
                String curCode = ri.getCurrency().getAccCurCode();
                boolean deleted = ri.isDeleted();
                double vouTotalAmt = Util1.getDouble(ri.getVouTotal());
                double vouPaidAmt = Util1.getDouble(ri.getVouPaid());
                double vouBalAmt = Util1.getDouble(ri.getVouBalance());
                String deptCodeByLoc = ri.getLocation().getDeptCode();
                String accByLoc = ri.getLocation().getAccCode();
                String traderByLoc = ri.getLocation().getTraderCode();
                deptCode = Util1.isNull(deptCodeByLoc, deptCode);
                traderCode = Util1.isNull(traderByLoc, traderCode);
                srcAcc = Util1.isNull(accByLoc, srcAcc);
                List<Gl> listGl = new ArrayList<>();
                //income
                if (vouBalAmt > 0) {
                    Gl gl = new Gl();
                    GlKey key = new GlKey();
                    key.setDeptId(1);
                    key.setCompCode(compCode);
                    gl.setKey(key);
                    gl.setGlDate(vouDate);
                    gl.setDescription("Return In Voucher Total");
                    gl.setSrcAccCode(srcAcc);
                    gl.setAccCode(balAcc);
                    gl.setTraderCode(traderCode);
                    gl.setDrAmt(vouTotalAmt);
                    gl.setCurCode(curCode);
                    gl.setReference(reference);
                    gl.setDeptCode(deptCode);
                    gl.setCreatedDate(LocalDateTime.now());
                    gl.setCreatedBy(APP_NAME);
                    gl.setTranSource(tranSource);
                    gl.setRefNo(vouNo);
                    gl.setDeleted(deleted);
                    gl.setMacId(MAC_ID);
                    listGl.add(gl);
                }
                //payment
                if (vouPaidAmt > 0) {
                    Gl gl = new Gl();
                    GlKey key = new GlKey();
                    key.setDeptId(1);
                    key.setCompCode(compCode);
                    gl.setKey(key);
                    gl.setGlDate(vouDate);
                    gl.setDescription("Return In Voucher Paid");
                    gl.setSrcAccCode(payAcc);
                    gl.setAccCode(srcAcc);
                    gl.setCrAmt(vouPaidAmt);
                    gl.setCurCode(curCode);
                    gl.setReference(reference);
                    gl.setDeptCode(deptCode);
                    gl.setCreatedDate(LocalDateTime.now());
                    gl.setCreatedBy(APP_NAME);
                    gl.setTranSource(tranSource);
                    gl.setRefNo(vouNo);
                    gl.setDeleted(deleted);
                    gl.setMacId(MAC_ID);
                    gl.setCash(true);
                    listGl.add(gl);
                }
                if (!listGl.isEmpty()) sendAccount(listGl);

            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }

    public void updateReturnIn(String vouNo, String status) {
        returnInRepo.updateReturnIn(vouNo, status);
        log.info(String.format("updateReturnIn: %s", vouNo));
    }

    public void sendReturnOutVoucherToAccount(RetOutHis ro) {
        if (Util1.getBoolean(uploadReturnOut)) {
            String tranSource = "RETURN_OUT";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                String vouNo = ro.getVouNo();
                String srcAcc = setting.getSourceAcc();
                String payAcc = setting.getPayAcc();
                String balAcc = setting.getBalanceAcc();
                String deptCode = setting.getDeptCode();
                LocalDateTime vouDate = ro.getVouDate();
                String curCode = ro.getCurrency().getAccCurCode();
                boolean deleted = ro.isDeleted();
                double vouTotalAmt = Util1.getDouble(ro.getVouTotal());
                double vouPaidAmt = Util1.getDouble(ro.getVouPaid());
                double vouBalAmt = Util1.getDouble(ro.getVouBalance());
                Trader trader = ro.getTrader();
                String traderCode = trader.getTraderCode();
                balAcc = Util1.isNull(trader.getAccount(), balAcc);
                String reference = ro.getRemark();
                String deptCodeByLoc = ro.getLocation().getDeptCode();
                String accByLoc = ro.getLocation().getAccCode();
                deptCode = Util1.isNull(deptCodeByLoc, deptCode);
                srcAcc = Util1.isNull(accByLoc, srcAcc);
                List<Gl> listGl = new ArrayList<>();
                //income
                if (vouBalAmt > 0) {
                    Gl gl = new Gl();
                    GlKey key = new GlKey();
                    key.setDeptId(1);
                    key.setCompCode(compCode);
                    gl.setKey(key);
                    gl.setGlDate(vouDate);
                    gl.setDescription("Return Out Voucher Total");
                    gl.setSrcAccCode(srcAcc);
                    gl.setAccCode(balAcc);
                    gl.setTraderCode(traderCode);
                    gl.setCrAmt(vouTotalAmt);
                    gl.setCurCode(curCode);
                    gl.setReference(reference);
                    gl.setDeptCode(deptCode);
                    gl.setCreatedDate(LocalDateTime.now());
                    gl.setCreatedBy(APP_NAME);
                    gl.setTranSource(tranSource);
                    gl.setRefNo(vouNo);
                    gl.setDeleted(deleted);
                    gl.setMacId(MAC_ID);
                    listGl.add(gl);
                }
                //payment
                if (vouPaidAmt > 0) {
                    Gl gl = new Gl();
                    GlKey key = new GlKey();
                    key.setDeptId(1);
                    key.setCompCode(compCode);
                    gl.setKey(key);
                    gl.setGlDate(vouDate);
                    gl.setDescription("Return Out Voucher Paid");
                    gl.setSrcAccCode(payAcc);
                    gl.setAccCode(srcAcc);
                    gl.setDrAmt(vouPaidAmt);
                    gl.setCurCode(curCode);
                    gl.setReference(reference);
                    gl.setDeptCode(deptCode);
                    gl.setCreatedDate(LocalDateTime.now());
                    gl.setCreatedBy(APP_NAME);
                    gl.setTranSource(tranSource);
                    gl.setRefNo(vouNo);
                    gl.setDeleted(deleted);
                    gl.setMacId(MAC_ID);
                    gl.setCash(true);
                    listGl.add(gl);
                }
                if (!listGl.isEmpty()) sendAccount(listGl);

            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }

    public void updateReturnOut(String vouNo, String status) {
        returnOutRepo.updateReturnOut(vouNo, status);
        log.info(String.format("updateReturnOut: %s", vouNo));
    }

    public void sendOPDVoucherToAccount(OPDHis oh) {
        if (Util1.getBoolean(uploadOPD)) {
            String tranSource = "OPD";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                String vouNo = oh.getVouNo();
                String srcAcc = setting.getSourceAcc();
                String payAcc = setting.getPayAcc();
                String disAcc = setting.getDiscountAcc();
                String balAcc = setting.getBalanceAcc();
                String mainDept = setting.getDeptCode();
                LocalDateTime vouDate = oh.getVouDate();
                String traderCode = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                String reference;
                String patientNo = null;
                String doctorId = oh.getDoctorId();
                Patient p = oh.getPatient();
                if (!Objects.isNull(p)) {
                    patientNo = p.getPatientNo();
                    String patientName = p.getPatientName();
                    reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                    TraderGroup g = p.getGroup();
                    if (g != null) {
                        traderCode = Util1.isNull(g.getTraderCode(), traderCode);
                        balAcc = Util1.isNull(g.getAccountId(), balAcc);
                    }
                } else {
                    reference = String.format("%s : %s : (%s)", "-", Util1.isNull(oh.getPatientName(), "-"), patientType);
                }
                String curCode = oh.getCurrency().getAccCurCode();
                boolean deleted = oh.isDeleted();
                double vouDisAmt = Util1.getDouble(oh.getVouDiscount());
                double vouPaid = Util1.getDouble(oh.getVouPaid());
                double vouTotal = Util1.getDouble(oh.getVouTotal());
                boolean admission = !Util1.isNullOrEmpty(oh.getAdmissionNo());
                Integer paymentId = oh.getPaymentId();
                List<Gl> listGl = new ArrayList<>();
                List<OPDHisDetail> listOPD = opdHisDetailRepo.search(vouNo);
                if (!listOPD.isEmpty()) {
                    for (OPDHisDetail op : listOPD) {
                        OPDCategory cat = op.getService().getCategory();
                        Integer chargeType = op.getChargeType();
                        String serviceId = String.valueOf(op.getService().getServiceId());
                        String serviceName = op.getService().getServiceName();
                        if (op.getRefer() != null) {
                            serviceName = String.format("%s : %s", serviceName, op.getRefer().getDoctorName());
                        }
                        //account
                        String opdAcc = cat.getOpdAcc();
                        String ipdAcc = cat.getIpdAcc();
                        String hDepCode = cat.getGroup().getDeptCode();
                        String deptCode = Util1.isNull(cat.getDeptCode(), hDepCode);
                        String moAcc = cat.getMoFeeAcc();
                        String staffAcc = cat.getStaffFeeAcc();
                        String techAcc = cat.getTechFeeAcc();
                        String referAcc = cat.getReferFeeAcc();
                        String readerAcc = cat.getReadFeeAcc();
                        String payableAcc = cat.getPayableAcc();
                        //percent
                        boolean percent = op.getService().isPercent();
                        //amount
                        double qty = Util1.getDouble(op.getQty());
                        double amount = Util1.getDouble(op.getAmount());
                        //income
                        String tmp = admission ? Util1.isNull(ipdAcc, opdAcc) : opdAcc;
                        srcAcc = Util1.isNull(tmp, srcAcc);
                        if (amount == 0) {
                            op.setMoFeeAmt(amount);
                            op.setStaffFeeAmt(amount);
                            op.setTechFeeAmt(amount);
                            op.setReferFeeAmt(amount);
                            op.setReadFeeAmt(amount);
                        }
                        if (amount != 0) {
                            Gl gl = new Gl();
                            GlKey key = new GlKey();
                            key.setDeptId(1);
                            key.setCompCode(compCode);
                            gl.setKey(key);
                            gl.setDescription(serviceName);
                            //cash
                            gl.setAccCode(srcAcc);
                            if (amount > 0) {
                                gl.setDrAmt(amount);
                            } else {
                                gl.setDescription("Return : " + serviceName);
                                gl.setCrAmt(amount * -1);
                            }
                            if (paymentId == 1 && vouTotal == vouPaid) {
                                gl.setSrcAccCode(payAcc);
                                gl.setCash(true);
                            } else {
                                //credit
                                gl.setTraderCode(traderCode);
                                gl.setSrcAccCode(balAcc);
                            }
                            gl.setGlDate(vouDate);
                            gl.setRefNo(vouNo);
                            gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                            gl.setMacId(MAC_ID);
                            gl.setCreatedBy(APP_NAME);
                            gl.setCurCode(curCode);
                            gl.setRefNo(vouNo);
                            gl.setCreatedDate(LocalDateTime.now());
                            gl.setTranSource(tranSource);
                            gl.setReference(reference);
                            gl.setDeleted(deleted);
                            gl.setServiceId(serviceId);
                            gl.setDoctorId(doctorId);
                            gl.setPatientNo(patientNo);
                            listGl.add(gl);
                            //payable
                            if (!Util1.isNullOrEmpty(payableAcc) && amount > 0) {
                                String[] accounts = payableAcc.split(",");
                                gl = new Gl();
                                key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(amount);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(serviceId);
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            }
                        } else {
                            deleteGl(tranSource, vouNo, srcAcc, chargeType);
                        }
                        //mo payable
                        if (!Util1.isNullOrEmpty(moAcc)) {
                            double moFeeAmt = percent ? amount * Util1.getDouble(op.getMoFeeAmt()) / 100 : Util1.getDouble(op.getMoFeeAmt()) * qty;
                            String[] accounts = moAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (moFeeAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                gl.setDescription(serviceName);
                                if (moFeeAmt > 0) {
                                    gl.setCrAmt(moFeeAmt);
                                } else {
                                    gl.setDrAmt(moFeeAmt * -1);
                                    gl.setDescription("Return : " + serviceName);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(serviceId);
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                        //staff payable
                        if (!Util1.isNullOrEmpty(staffAcc)) {
                            double staffAmt = percent ? Util1.getDouble(op.getStaffFeeAmt()) / 100 : Util1.getDouble(op.getStaffFeeAmt()) * qty;
                            String[] accounts = staffAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (staffAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                gl.setDescription(serviceName);
                                if (staffAmt > 0) {
                                    gl.setCrAmt(staffAmt);
                                } else {
                                    gl.setDrAmt(staffAmt * -1);
                                    gl.setDescription("Return : " + serviceName);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(serviceId);
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                        //tech payable
                        if (!Util1.isNullOrEmpty(techAcc)) {
                            double techAmt = percent ? amount * Util1.getDouble(op.getTechFeeAmt()) / 100 : Util1.getDouble(op.getTechFeeAmt()) * qty;
                            String[] accounts = techAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (techAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                gl.setDescription(serviceName);
                                if (techAmt > 0) {
                                    gl.setCrAmt(techAmt);
                                } else {
                                    gl.setDrAmt(techAmt * -1);
                                    gl.setDescription("Return : " + serviceName);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(serviceId);
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                        //refer payable
                        if (!Util1.isNullOrEmpty(referAcc)) {
                            double referAmt = percent ? amount * Util1.getDouble(op.getReferFeeAmt()) / 100 : Util1.getDouble(op.getReferFeeAmt()) * qty;
                            String[] accounts = referAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (referAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setDescription(serviceName);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                if (referAmt > 0) {
                                    gl.setCrAmt(referAmt);
                                } else {
                                    gl.setDescription("Return : " + serviceName);
                                    gl.setDrAmt(referAmt * -1);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(serviceId);
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                        //reader payable
                        if (!Util1.isNullOrEmpty(readerAcc)) {
                            double readerAmt = percent ? amount * Util1.getDouble(op.getReadFeeAmt()) / 100 : Util1.getDouble(op.getReadFeeAmt()) * qty;
                            String[] accounts = readerAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (readerAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                gl.setDescription(serviceName);
                                if (readerAmt > 0) {
                                    gl.setCrAmt(readerAmt);
                                } else {
                                    gl.setDescription("Return : " + serviceName);
                                    gl.setDrAmt(readerAmt * -1);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(serviceId);
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                    }
                    //discount
                    if (vouDisAmt > 0) {
                        Gl gl = new Gl();
                        GlKey key = new GlKey();
                        key.setDeptId(1);
                        key.setCompCode(compCode);
                        gl.setKey(key);
                        if (paymentId == 1 && vouTotal == vouPaid) {
                            gl.setSrcAccCode(payAcc);
                            gl.setAccCode(disAcc);
                            gl.setCash(true);
                        } else {
                            gl.setSrcAccCode(balAcc);
                            gl.setAccCode(disAcc);
                            gl.setTraderCode(traderCode);
                        }
                        gl.setGlDate(vouDate);
                        gl.setDescription("OPD Voucher Discount");
                        gl.setCrAmt(vouDisAmt);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(mainDept);
                        gl.setCreatedDate(LocalDateTime.now());
                        gl.setCreatedBy(APP_NAME);
                        gl.setTranSource(tranSource);
                        gl.setRefNo(vouNo);
                        gl.setDeleted(deleted);
                        gl.setMacId(MAC_ID);
                        gl.setDoctorId(doctorId);
                        gl.setPatientNo(patientNo);
                        listGl.add(gl);
                    }
                    //paid
                    if (vouPaid != vouTotal) {
                        Gl gl = new Gl();
                        GlKey key = new GlKey();
                        key.setDeptId(1);
                        key.setCompCode(compCode);
                        gl.setKey(key);
                        gl.setSrcAccCode(payAcc);
                        gl.setAccCode(balAcc);
                        gl.setCash(true);
                        gl.setGlDate(vouDate);
                        gl.setDescription("OPD Partial Paid");
                        gl.setDrAmt(vouPaid);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(mainDept);
                        gl.setCreatedDate(LocalDateTime.now());
                        gl.setCreatedBy(APP_NAME);
                        gl.setTranSource(tranSource);
                        gl.setRefNo(vouNo);
                        gl.setDeleted(deleted);
                        gl.setMacId(MAC_ID);
                        gl.setDoctorId(doctorId);
                        gl.setPatientNo(patientNo);
                        listGl.add(gl);
                    }
                    if (!listGl.isEmpty()) {
                        sendAccount(listGl);
                    } else {
                        deleteGl(tranSource, vouNo, null, 2);
                        opdHisRepo.updateOPD(vouNo, FOC);
                    }
                } else {
                    deleteGl(tranSource, vouNo, null, 2);
                    opdHisRepo.updateOPD(vouNo, ERR);
                }


            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }


    public void updateOPD(String vouNo, String status) {
        opdHisRepo.updateOPD(vouNo, status);
        log.info(String.format("updateOPD: %s", vouNo));
    }

    public void sendOTVoucherToAccount(OTHis oh) {
        if (Util1.getBoolean(uploadOT)) {
            String tranSource = "OT";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                String vouNo = oh.getVouNo();
                String srcAcc = setting.getSourceAcc();
                String payAcc = setting.getPayAcc();
                String disAcc = setting.getDiscountAcc();
                String balAcc = setting.getBalanceAcc();
                String mainDept = setting.getDeptCode();
                LocalDateTime vouDate = oh.getVouDate();
                String traderCode = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                String reference;
                String patientNo = null;
                String doctorId = oh.getDoctorId();
                Patient p = oh.getPatient();
                if (!Objects.isNull(p)) {
                    patientNo = p.getPatientNo();
                    String patientName = p.getPatientName();
                    String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                    reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                    TraderGroup g = p.getGroup();
                    if (g != null) {
                        traderCode = Util1.isNull(g.getTraderCode(), traderCode);
                        balAcc = Util1.isNull(g.getAccountId(), balAcc);
                    }
                } else {
                    String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                    reference = String.format("%s : %s : (%s)", "-", "-", patientType);
                }
                String curCode = oh.getCurrency().getAccCurCode();
                boolean deleted = oh.isDeleted();
                boolean admission = !Util1.isNullOrEmpty(oh.getAdmissionNo());
                double paymentId = oh.getPaymentId();
                List<OTHisDetail> listOT = otHisDetailRepo.search(vouNo);
                List<Gl> listGl = new ArrayList<>();

                if (!listOT.isEmpty()) {
                    for (OTHisDetail ot : listOT) {
                        StringBuilder doctorName = new StringBuilder();
                        List<OTDoctorFee> doctors = otDoctorFeeRepo.search(ot.getId());
                        if (!doctors.isEmpty()) {
                            for (OTDoctorFee d : doctors) {
                                doctorName.append(" : ").append(d.getDoctor().getDoctorName());
                            }
                        }
                        Integer chargeType = ot.getChargeType();
                        OTGroup group = ot.getService().getOtGroup();
                        Integer serviceId = ot.getService().getServiceId();
                        String serviceName = ot.getService().getServiceName();
                        if (!doctorName.isEmpty()) {
                            serviceName = String.format("%s%s", serviceName, doctorName);
                        }
                        //account
                        String opdAcc = group.getOpdAcc();
                        String ipdAcc = group.getIpdAcc();
                        String deptCode = group.getDeptCode();
                        String moAcc = group.getMoFeeAcc();
                        String staffAcc = group.getStaffFeeAcc();
                        String nurseAcc = group.getNurseFeeAcc();
                        String payableAcc = group.getPayableAcc();
                        //amount
                        double qty = Util1.getDouble(ot.getQty());
                        double amount = Util1.getDouble(ot.getAmount());
                        if (amount == 0) {
                            ot.setStaffFeeAmt(amount);
                            ot.setMoFeeAmt(amount);
                            ot.setNurseFeeAmt(amount);
                        }
                        //discount
                        if (serviceId == Util1.getInteger(otDiscountId)) {
                            Gl gl = new Gl();
                            GlKey key = new GlKey();
                            key.setDeptId(1);
                            key.setCompCode(compCode);
                            gl.setKey(key);
                            //cash
                            if (paymentId == 1) {
                                gl.setSrcAccCode(payAcc);
                                gl.setCash(true);
                            } else {
                                //credit
                                gl.setSrcAccCode(balAcc);
                                gl.setTraderCode(traderCode);
                            }
                            gl.setGlDate(vouDate);
                            gl.setAccCode(Util1.isNull(opdAcc, disAcc));
                            gl.setCrAmt(amount);
                            gl.setRefNo(vouNo);
                            gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                            gl.setMacId(MAC_ID);
                            gl.setCreatedBy(APP_NAME);
                            gl.setCurCode(curCode);
                            gl.setRefNo(vouNo);
                            gl.setDescription(serviceName);
                            gl.setCreatedDate(LocalDateTime.now());
                            gl.setTranSource(tranSource);
                            gl.setReference(reference);
                            gl.setDeleted(deleted);
                            gl.setServiceId(String.valueOf(serviceId));
                            gl.setDoctorId(doctorId);
                            gl.setPatientNo(patientNo);
                            listGl.add(gl);
                        } else if (serviceId == Util1.getInteger(otPaidId) || serviceId == Util1.getInteger(otDepositId)) {
                            //paid or deposit
                            //credit
                            if (paymentId == 2) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(opdAcc, payAcc));
                                gl.setAccCode(balAcc);
                                gl.setDrAmt(amount);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                gl.setCash(true);
                                gl.setServiceId(String.valueOf(serviceId));
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            }
                        } else if (serviceId == Util1.getInteger(otRefundId)) {
                            //refund
                            Gl gl = new Gl();
                            GlKey key = new GlKey();
                            key.setDeptId(1);
                            key.setCompCode(compCode);
                            gl.setKey(key);
                            gl.setGlDate(vouDate);
                            gl.setSrcAccCode(payAcc);
                            if (!Util1.isNullOrEmpty(opdAcc)) {
                                gl.setAccCode(opdAcc);
                            } else {
                                gl.setAccCode(balAcc);
                                gl.setTraderCode(traderCode);
                            }
                            gl.setCrAmt(amount);
                            gl.setRefNo(vouNo);
                            gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                            gl.setMacId(MAC_ID);
                            gl.setCreatedBy(APP_NAME);
                            gl.setCurCode(curCode);
                            gl.setRefNo(vouNo);
                            gl.setDescription(serviceName);
                            gl.setCreatedDate(LocalDateTime.now());
                            gl.setTranSource(tranSource);
                            gl.setReference(reference);
                            gl.setTraderCode(traderCode);
                            gl.setDeleted(deleted);
                            gl.setCash(true);
                            gl.setServiceId(String.valueOf(serviceId));
                            gl.setDoctorId(doctorId);
                            gl.setPatientNo(patientNo);
                            listGl.add(gl);
                        } else {
                            //income
                            String tmp = admission ? Util1.isNull(ipdAcc, opdAcc) : opdAcc;
                            srcAcc = Util1.isNull(tmp, srcAcc);
                            if (amount != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setAccCode(srcAcc);
                                if (paymentId == 1) {
                                    gl.setSrcAccCode(payAcc);
                                    gl.setCash(true);
                                } else {
                                    //credit
                                    gl.setTraderCode(traderCode);
                                    gl.setSrcAccCode(balAcc);
                                }
                                if (amount > 0) {
                                    gl.setDrAmt(amount);
                                } else {
                                    gl.setCrAmt(amount * -1);
                                }
                                gl.setGlDate(vouDate);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(String.valueOf(serviceId));
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                                //payable
                                if (!Util1.isNullOrEmpty(payableAcc) && amount > 0) {
                                    String[] accounts = payableAcc.split(",");
                                    gl = new Gl();
                                    key = new GlKey();
                                    key.setDeptId(1);
                                    key.setCompCode(compCode);
                                    gl.setKey(key);
                                    gl.setGlDate(vouDate);
                                    gl.setSrcAccCode(accounts[0]);
                                    gl.setAccCode(accounts[1]);
                                    gl.setCrAmt(amount);
                                    gl.setRefNo(vouNo);
                                    gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                    gl.setMacId(MAC_ID);
                                    gl.setCreatedBy(APP_NAME);
                                    gl.setCurCode(curCode);
                                    gl.setRefNo(vouNo);
                                    gl.setDescription(serviceName);
                                    gl.setCreatedDate(LocalDateTime.now());
                                    gl.setTranSource(tranSource);
                                    gl.setReference(reference);
                                    gl.setDeleted(deleted);
                                    gl.setServiceId(String.valueOf(serviceId));
                                    gl.setDoctorId(doctorId);
                                    gl.setPatientNo(patientNo);
                                    listGl.add(gl);
                                }
                            } else {
                                deleteGl(tranSource, vouNo, srcAcc, chargeType);
                            }
                        }
                        //mo payable
                        if (!Util1.isNullOrEmpty(moAcc)) {
                            double moFeeAmt = Util1.getDouble(ot.getMoFeeAmt()) * qty;
                            String[] accounts = moAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (moFeeAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                gl.setDescription(serviceName);
                                if (moFeeAmt > 0) {
                                    gl.setCrAmt(moFeeAmt);
                                } else {
                                    gl.setDrAmt(moFeeAmt * -1);
                                    gl.setDescription("Return : " + serviceName);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(String.valueOf(serviceId));
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                        //staff payable
                        if (!Util1.isNullOrEmpty(staffAcc)) {
                            double staffAmt = Util1.getDouble(ot.getStaffFeeAmt()) * qty;
                            String[] accounts = staffAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (staffAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                gl.setDescription(serviceName);
                                if (staffAmt > 0) {
                                    gl.setCrAmt(staffAmt);
                                } else {
                                    gl.setDrAmt(staffAmt * -1);
                                    gl.setDescription("Return : " + serviceName);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(String.valueOf(serviceId));
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                        //nurse payable
                        if (!Util1.isNullOrEmpty(nurseAcc)) {
                            double nurseAmt = Util1.getDouble(ot.getNurseFeeAmt()) * qty;
                            String[] accounts = nurseAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (nurseAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                gl.setDescription(serviceName);
                                if (nurseAmt > 0) {
                                    gl.setCrAmt(nurseAmt);
                                } else {
                                    gl.setDrAmt(nurseAmt * -1);
                                    gl.setDescription("Return : " + serviceName);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(String.valueOf(serviceId));
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                    }//loop
                    if (!listGl.isEmpty()) {
                        sendAccount(listGl);
                    } else {
                        deleteGl(tranSource, vouNo, null, 2);
                        otHisRepo.updateOT(vouNo, FOC);
                    }
                } else {
                    otHisRepo.updateOT(vouNo, ERR);
                }

            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }


    public void updateOT(String vouNo, String status) {
        otHisRepo.updateOT(vouNo, status);
        log.info(String.format("updateOT: %s", vouNo));
    }

    public void sendDCVoucherToAccount(DCHis oh) {
        if (Util1.getBoolean(uploadDC)) {
            String tranSource = "DC";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                String vouNo = oh.getVouNo();
                String srcAcc = setting.getSourceAcc();
                String payAcc = setting.getPayAcc();
                String disAcc = setting.getDiscountAcc();
                String balAcc = setting.getBalanceAcc();
                String mainDept = setting.getDeptCode();
                LocalDateTime vouDate = oh.getVouDate();
                String traderCode = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                String reference;
                String patientNo = null;
                String doctorId = oh.getDoctorId();
                Patient p = oh.getPatient();
                if (!Objects.isNull(p)) {
                    patientNo = p.getPatientNo();
                    String patientName = p.getPatientName();
                    String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                    reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                    TraderGroup g = p.getGroup();
                    if (g != null) {
                        traderCode = Util1.isNull(g.getTraderCode(), traderCode);
                        balAcc = Util1.isNull(g.getAccountId(), balAcc);
                    }
                } else {
                    String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                    reference = String.format("%s : %s : (%s)", "-", "-", patientType);
                }
                String curCode = oh.getCurrency().getAccCurCode();
                boolean deleted = oh.isDeleted();
                Integer paymentId = oh.getPaymentId();
                List<DCHisDetail> listDC = dcHisDetailRepo.search(vouNo);
                if (!listDC.isEmpty()) {
                    List<Gl> listGl = new ArrayList<>();
                    StringBuilder doctorName = new StringBuilder();
                    for (DCHisDetail dc : listDC) {
                        List<DCDoctorFee> doctors = dcDoctorFeeRepo.search(dc.getId());
                        if (!doctors.isEmpty()) {
                            for (DCDoctorFee d : doctors) {
                                doctorName.append(" : ").append(d.getDoctor().getDoctorName());
                            }
                        }
                        Integer chargeType = dc.getChargeType();
                        DCGroup group = dc.getService().getDcGroup();
                        String serviceName = dc.getService().getServiceName();
                        Integer serviceId = dc.getService().getServiceId();
                        if (!doctorName.isEmpty()) {
                            serviceName = String.format("%s%s", serviceName, doctorName);
                        }
                        //account
                        String accountCode = group.getAccountCode();
                        String deptCode = group.getDeptCode();
                        String moAcc = group.getMoFeeAcc();
                        String techAcc = group.getTechFeeAcc();
                        String nurseAcc = group.getNurseFeeAcc();
                        String payableAcc = group.getPayableAcc();
                        //amount
                        double qty = Util1.getDouble(dc.getQty());
                        double amount = Util1.getDouble(dc.getAmount());
                        if (amount == 0) {
                            dc.setTechFeeAmt(amount);
                            dc.setMoFeeAmt(amount);
                            dc.setNurseFeeAmt(amount);
                        }
                        //discount
                        if (serviceId == Util1.getInteger(dcDiscountId)) {
                            Gl gl = new Gl();
                            GlKey key = new GlKey();
                            key.setDeptId(1);
                            key.setCompCode(compCode);
                            gl.setKey(key);
                            //cash
                            if (paymentId == 1) {
                                gl.setSrcAccCode(payAcc);
                                gl.setCash(true);
                            } else {
                                //credit
                                gl.setSrcAccCode(balAcc);
                                gl.setTraderCode(traderCode);
                            }
                            gl.setGlDate(vouDate);
                            gl.setAccCode(Util1.isNull(accountCode, disAcc));
                            gl.setCrAmt(amount);
                            gl.setRefNo(vouNo);
                            gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                            gl.setMacId(MAC_ID);
                            gl.setCreatedBy(APP_NAME);
                            gl.setCurCode(curCode);
                            gl.setRefNo(vouNo);
                            gl.setDescription(serviceName);
                            gl.setCreatedDate(LocalDateTime.now());
                            gl.setTranSource(tranSource);
                            gl.setReference(reference);
                            gl.setDeleted(deleted);
                            gl.setServiceId(String.valueOf(serviceId));
                            gl.setDoctorId(doctorId);
                            gl.setPatientNo(patientNo);
                            listGl.add(gl);
                        } else if (serviceId == Util1.getInteger(dcPaidId) || serviceId == Util1.getInteger(dcDepositId)) {
                            //paid or deposit
                            //credit
                            Gl gl = new Gl();
                            GlKey key = new GlKey();
                            key.setDeptId(1);
                            key.setCompCode(compCode);
                            gl.setKey(key);
                            gl.setGlDate(vouDate);
                            gl.setSrcAccCode(Util1.isNull(accountCode, payAcc));
                            gl.setAccCode(balAcc);
                            gl.setDrAmt(amount);
                            gl.setRefNo(vouNo);
                            gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                            gl.setMacId(MAC_ID);
                            gl.setCreatedBy(APP_NAME);
                            gl.setCurCode(curCode);
                            gl.setRefNo(vouNo);
                            gl.setDescription(serviceName);
                            gl.setCreatedDate(LocalDateTime.now());
                            gl.setTranSource(tranSource);
                            gl.setReference(reference);
                            gl.setTraderCode(traderCode);
                            gl.setDeleted(deleted);
                            gl.setCash(true);
                            gl.setServiceId(String.valueOf(serviceId));
                            gl.setDoctorId(doctorId);
                            gl.setPatientNo(patientNo);
                            listGl.add(gl);

                        } else if (serviceId == Util1.getInteger(dcRefundId)) {
                            //refund
                            Gl gl = new Gl();
                            GlKey key = new GlKey();
                            key.setDeptId(1);
                            key.setCompCode(compCode);
                            gl.setKey(key);
                            gl.setGlDate(vouDate);
                            gl.setSrcAccCode(payAcc);
                            if (!Util1.isNullOrEmpty(accountCode)) {
                                gl.setAccCode(accountCode);
                            } else {
                                gl.setAccCode(balAcc);
                                gl.setTraderCode(traderCode);
                            }
                            gl.setCrAmt(amount);
                            gl.setRefNo(vouNo);
                            gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                            gl.setMacId(MAC_ID);
                            gl.setCreatedBy(APP_NAME);
                            gl.setCurCode(curCode);
                            gl.setRefNo(vouNo);
                            gl.setDescription(serviceName);
                            gl.setCreatedDate(LocalDateTime.now());
                            gl.setTranSource(tranSource);
                            gl.setReference(reference);
                            gl.setTraderCode(traderCode);
                            gl.setDeleted(deleted);
                            gl.setCash(true);
                            gl.setServiceId(String.valueOf(serviceId));
                            gl.setDoctorId(doctorId);
                            gl.setPatientNo(patientNo);
                            listGl.add(gl);
                        } else if (serviceId == Util1.getInteger(packageId)) {
                            //refund
                            Gl gl = new Gl();
                            GlKey key = new GlKey();
                            key.setDeptId(1);
                            key.setCompCode(compCode);
                            gl.setKey(key);
                            gl.setGlDate(vouDate);
                            gl.setSrcAccCode(accountCode);
                            gl.setAccCode(balAcc);
                            if (amount < 0) {
                                gl.setDrAmt(amount * -1);
                            } else {
                                gl.setCrAmt(amount);
                            }
                            gl.setRefNo(vouNo);
                            gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                            gl.setMacId(MAC_ID);
                            gl.setCreatedBy(APP_NAME);
                            gl.setCurCode(curCode);
                            gl.setDescription(serviceName);
                            gl.setCreatedDate(LocalDateTime.now());
                            gl.setTranSource(tranSource);
                            gl.setReference(reference);
                            gl.setTraderCode(traderCode);
                            gl.setDeleted(deleted);
                            gl.setServiceId(String.valueOf(serviceId));
                            gl.setDoctorId(doctorId);
                            gl.setPatientNo(patientNo);
                            listGl.add(gl);
                        } else {
                            //income
                            srcAcc = Util1.isNull(accountCode, srcAcc);
                            if (amount != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setAccCode(srcAcc);
                                if (paymentId == 1) {
                                    gl.setSrcAccCode(payAcc);
                                    gl.setCash(true);
                                } else {
                                    //credit
                                    gl.setTraderCode(traderCode);
                                    gl.setSrcAccCode(balAcc);
                                }
                                if (amount > 0) {
                                    gl.setDrAmt(amount);
                                } else {
                                    gl.setCrAmt(amount * -1);
                                }

                                gl.setGlDate(vouDate);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(String.valueOf(serviceId));
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                                //payable
                                if (!Util1.isNullOrEmpty(payableAcc) && amount > 0) {
                                    String[] accounts = payableAcc.split(",");
                                    gl = new Gl();
                                    key = new GlKey();
                                    key.setDeptId(1);
                                    key.setCompCode(compCode);
                                    gl.setKey(key);
                                    gl.setGlDate(vouDate);
                                    gl.setSrcAccCode(accounts[0]);
                                    gl.setAccCode(accounts[1]);
                                    gl.setCrAmt(amount);
                                    gl.setRefNo(vouNo);
                                    gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                    gl.setMacId(MAC_ID);
                                    gl.setCreatedBy(APP_NAME);
                                    gl.setCurCode(curCode);
                                    gl.setRefNo(vouNo);
                                    gl.setDescription(serviceName);
                                    gl.setCreatedDate(LocalDateTime.now());
                                    gl.setTranSource(tranSource);
                                    gl.setReference(reference);
                                    gl.setDeleted(deleted);
                                    gl.setServiceId(String.valueOf(serviceId));
                                    gl.setDoctorId(doctorId);
                                    gl.setPatientNo(patientNo);
                                    listGl.add(gl);
                                }
                            } else {
                                deleteGl(tranSource, vouNo, srcAcc, chargeType);
                            }
                        }
                        //mo payable
                        if (!Util1.isNullOrEmpty(moAcc)) {
                            double moFeeAmt = Util1.getDouble(dc.getMoFeeAmt()) * qty;
                            String[] accounts = moAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (moFeeAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                gl.setDescription(serviceName);
                                if (moFeeAmt > 0) {
                                    gl.setCrAmt(moFeeAmt);
                                } else {
                                    gl.setDrAmt(moFeeAmt * -1);
                                    gl.setDescription("Return : " + serviceName);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(String.valueOf(serviceId));
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                        //tech payable
                        if (!Util1.isNullOrEmpty(techAcc)) {
                            double techAmt = Util1.getDouble(dc.getTechFeeAmt()) * qty;
                            String[] accounts = techAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (techAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                gl.setDescription(serviceName);
                                if (techAmt > 0) {
                                    gl.setCrAmt(techAmt);
                                } else {
                                    gl.setDrAmt(techAmt * -1);
                                    gl.setDescription("Return : " + serviceName);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(String.valueOf(serviceId));
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                        //nurse payable
                        if (!Util1.isNullOrEmpty(nurseAcc)) {
                            double nurseAmt = Util1.getDouble(dc.getNurseFeeAmt()) * qty;
                            String[] accounts = nurseAcc.split(",");
                            String sAcc = accounts[0];
                            String acc = accounts[1];
                            if (nurseAmt != 0) {
                                Gl gl = new Gl();
                                GlKey key = new GlKey();
                                key.setDeptId(1);
                                key.setCompCode(compCode);
                                gl.setKey(key);
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(sAcc);
                                gl.setAccCode(acc);
                                gl.setDescription(serviceName);
                                if (nurseAmt > 0) {
                                    gl.setCrAmt(nurseAmt);
                                } else {
                                    gl.setDrAmt(nurseAmt * -1);
                                    gl.setDescription("Return : " + serviceName);
                                }
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setCreatedDate(LocalDateTime.now());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setDeleted(deleted);
                                gl.setServiceId(String.valueOf(serviceId));
                                gl.setDoctorId(doctorId);
                                gl.setPatientNo(patientNo);
                                listGl.add(gl);
                            } else {
                                deleteGl(tranSource, vouNo, sAcc, chargeType);
                            }
                        }
                    }
                    if (!listGl.isEmpty()) {
                        sendAccount(listGl);
                        log.info(String.format("sendDCVoucherToAccount: %s", vouNo));
                    } else {
                        deleteGl(tranSource, vouNo, null, 2);
                        dcHisRepo.updateDC(vouNo, FOC);
                    }
                } else {
                    dcHisRepo.updateDC(vouNo, ERR);
                }

            }
        }
    }

    public void updateDC(String vouNo, String status) {
        dcHisRepo.updateDC(vouNo, status);
        log.info(String.format("updateDC: %s", vouNo));
    }

    public void sendOPDReceiveToAccount(OPDReceive receive) {
        if (Util1.getBoolean(uploadOPDBill)) {
            Integer id = receive.getBillId();
            LocalDateTime vouDate = receive.getPayDate();
            double payAmt = Util1.getDouble(receive.getPayAmt());
            double discount = Util1.getDouble(receive.getDiscount());
            String regNo = receive.getPatient().getPatientNo();
            String curCode = receive.getCurrency().getAccCurCode();
            boolean deleted = receive.isDeleted();
            String tranSource;
            String deptCode;
            String cashAcc;
            String balAcc;
            String disAcc;
            String traderCode;
            String description;
            if (reportService.isAdmission(Util1.toDateStr(vouDate), regNo, id)) {
                tranSource = "DC";
                AccountSetting ac = hmAccSetting.get(tranSource);
                deptCode = ac.getDeptCode();
                cashAcc = ac.getPayAcc();
                balAcc = ac.getBalanceAcc();
                traderCode = inPatientCode;
                disAcc = ac.getDiscountAcc();
                description = "Inpatient Bill";
            } else {
                tranSource = "OPD";
                AccountSetting ac = hmAccSetting.get(tranSource);
                deptCode = ac.getDeptCode();
                cashAcc = ac.getPayAcc();
                balAcc = ac.getBalanceAcc();
                traderCode = outPatientCode;
                disAcc = ac.getDiscountAcc();
                description = "Outpatient Bill";
            }
            String reference = null;
            Patient p = receive.getPatient();
            if (!Objects.isNull(p)) {
                String patientNo = receive.getPatient().getPatientNo();
                String patientName = receive.getPatient().getPatientName();
                reference = String.format("%s : %s", patientNo, patientName);
                TraderGroup g = p.getGroup();
                if (g != null) {
                    traderCode = Util1.isNull(g.getTraderCode(), traderCode);
                    balAcc = Util1.isNull(g.getAccountId(), balAcc);
                }
            }
            List<Gl> list = new ArrayList<>();
            if (payAmt != 0) {
                Gl gl = new Gl();
                GlKey key = new GlKey();
                key.setDeptId(1);
                key.setCompCode(compCode);
                gl.setKey(key);
                gl.setGlDate(vouDate);
                if (payAmt > 0) {
                    gl.setDrAmt(payAmt);
                    gl.setDescription(String.format("%s %s", description, "Received"));
                } else {
                    gl.setCrAmt(payAmt * -1);
                    gl.setDescription(String.format("%s %s", description, "Refund"));
                }
                gl.setSrcAccCode(cashAcc);
                gl.setAccCode(balAcc);
                gl.setRefNo(String.valueOf(id));
                gl.setDeptCode(deptCode);
                gl.setMacId(MAC_ID);
                gl.setCreatedBy(APP_NAME);
                gl.setCurCode(curCode);
                gl.setCreatedDate(LocalDateTime.now());
                gl.setTranSource("BILL_PAYMENT");
                gl.setReference(reference);
                gl.setTraderCode(traderCode);
                gl.setDeleted(deleted);
                gl.setCash(true);
                gl.setPatientNo(regNo);
                list.add(gl);
            }
            if (discount != 0) {
                Gl gl = new Gl();
                GlKey key = new GlKey();
                key.setDeptId(1);
                key.setCompCode(compCode);
                gl.setKey(key);
                gl.setGlDate(vouDate);
                gl.setDescription(String.format("%s %s", description, "Discount"));
                gl.setSrcAccCode(disAcc);
                gl.setAccCode(balAcc);
                gl.setDrAmt(discount);
                gl.setRefNo(String.valueOf(id));
                gl.setDeptCode(deptCode);
                gl.setMacId(MAC_ID);
                gl.setCreatedBy(APP_NAME);
                gl.setCurCode(curCode);
                gl.setCreatedDate(LocalDateTime.now());
                gl.setTranSource("BILL_PAYMENT");
                gl.setReference(reference);
                gl.setTraderCode(traderCode);
                gl.setDeleted(deleted);
                gl.setCash(true);
                gl.setPatientNo(regNo);
                list.add(gl);
            }
            if (!list.isEmpty()) {
                sendAccount(list);
                log.info(String.format("sendOPDReceiveToAccount: %s", id));
            }


        }
    }


    private void updateOPDReceive(Integer id, String status) {
        opdReceiveRepo.updateOPD(id, status);
        log.info(String.format("updateOPDPayment: %s", id));
    }

    public void sendGeneralExpenseToAcc(GenExpense ge) {
        if (Util1.getBoolean(uploadExpense)) {
            String tranSource = ge.isUnpaid() ? "UNPAID" : "EXPENSE";
            String geneId = ge.getGenId().toString();
            if (ge.isDeleted()) {
                deleteGl(tranSource, geneId, null, 2);
            } else {
                List<Gl> listGl = new ArrayList<>();
                String description = ge.getDescription();
                LocalDateTime expDate = ge.getExpDate();
                String remark = ge.getRemark();
                String srcAcc = ge.getSrcAcc();
                String account = ge.getAccount();
                String depCode = ge.getDeptCode();
                String curCode = ge.getCurrency() == null ? "MMK" : ge.getCurrency().getAccCurCode();
                String expenseId = ge.getGenId().toString();
                double expAmt = Util1.getDouble(ge.getExpAmt());
                boolean deleted = ge.isDeleted();
                if (expAmt != 0) {
                    Gl gl = new Gl();
                    GlKey key = new GlKey();
                    key.setDeptId(1);
                    key.setCompCode(compCode);
                    gl.setKey(key);
                    gl.setGlDate(expDate);
                    gl.setSrcAccCode(srcAcc);
                    gl.setAccCode(account);
                    gl.setDescription(description);
                    if (expAmt > 0) {
                        gl.setCrAmt(expAmt);
                    } else {
                        gl.setDescription("Return : " + description);
                        gl.setDrAmt(expAmt * -1);
                    }
                    gl.setRefNo(expenseId);
                    gl.setDeptCode(depCode);
                    gl.setMacId(MAC_ID);
                    gl.setCreatedBy(APP_NAME);
                    gl.setCurCode(curCode);
                    gl.setCreatedDate(LocalDateTime.now());
                    gl.setTranSource(tranSource);
                    gl.setReference(remark);
                    gl.setDeleted(deleted);
                    gl.setCash(true);
                    listGl.add(gl);
                }
                if (!listGl.isEmpty()) {
                    sendAccount(listGl);
                    log.info(String.format("sendGeneralExpenseToAcc: %s", expenseId));
                }
            }
        }
    }

    private void updateExpense(Integer id, String status) {
        genExpenseRepo.updateExpense(id, status);
        log.info(String.format("updateExpense %s", id));
    }

    public void sendPaymentToAcc(PaymentHis pay) {
        if (Util1.getBoolean(uploadPayment)) {
            String tranSource = "PAYMENT";
            AccountSetting as = hmAccSetting.get(tranSource);
            if (!Objects.isNull(as)) {
                String supBalAcc = "-";
                String cusBalAcc = "-";
                String supDisAcc = "-";
                String cusDisAcc = "-";
                Optional<AccountSetting> purSetting = accountSetting.findById("PURCHASE");
                if (purSetting.isPresent()) {
                    supBalAcc = purSetting.get().getBalanceAcc();
                    supDisAcc = purSetting.get().getDiscountAcc();
                }
                Optional<AccountSetting> saleSetting = accountSetting.findById("SALE");
                if (saleSetting.isPresent()) {
                    cusBalAcc = saleSetting.get().getBalanceAcc();
                    cusDisAcc = saleSetting.get().getDiscountAcc();
                }
                String payAcc = pay.getPaymentType() == null ? as.getPayAcc() : pay.getPaymentType().getAccount();
                String payAccOut = as.getPayAccOut();
                String mainDept = as.getDeptCode();
                if (pay.getLocation() != null) {
                    String deptCodeByLoc = pay.getLocation().getDeptCode();
                    mainDept = Util1.isNull(deptCodeByLoc, mainDept);
                }
                Integer payId = pay.getPayId();
                LocalDateTime payDate = pay.getPayDate();
                double discount = Util1.getDouble(pay.getDiscount());
                double payAmt = Util1.getDouble(pay.getPayAmt());
                boolean deleted = pay.isDeleted();
                String curCode = pay.getCurrency().getAccCurCode();
                String traderCode = pay.getTrader().getTraderCode();
                String type = pay.getTrader().getTraderType();
                String traderType = type.equals("C") ? "Customer" : "Supplier";
                String balAcc = pay.getTrader().getTraderType().equals("C") ? cusBalAcc : supBalAcc;
                String reference = pay.getRemark();
                String description = String.format("%s (%s)", traderType, type.equals("C") ? "Receive" : "Payment");
                List<Gl> listGl = new ArrayList<>();
                if (payAmt != 0) {
                    Gl gl = new Gl();
                    GlKey key = new GlKey();
                    key.setDeptId(1);
                    key.setCompCode(compCode);
                    gl.setKey(key);
                    gl.setGlDate(payDate);
                    gl.setAccCode(balAcc);
                    if (payAmt > 0) {
                        if (pay.getTrader().getTraderType().equals("C")) {
                            gl.setSrcAccCode(payAcc);
                            gl.setDrAmt(payAmt);
                        } else {
                            gl.setSrcAccCode(Util1.isNull(payAccOut, payAcc));
                            gl.setCrAmt(payAmt);
                        }
                    } else {
                        if (pay.getTrader().getTraderType().equals("C")) {
                            gl.setSrcAccCode(payAcc);
                            gl.setCrAmt(payAmt);
                        } else {
                            gl.setSrcAccCode(Util1.isNull(payAccOut, payAcc));
                            gl.setDrAmt(payAmt);
                        }
                    }
                    gl.setRefNo(String.valueOf(payId));
                    gl.setDeptCode(mainDept);
                    gl.setMacId(MAC_ID);
                    gl.setCreatedBy(APP_NAME);
                    gl.setCurCode(curCode);
                    gl.setDescription(description);
                    gl.setCreatedDate(LocalDateTime.now());
                    gl.setTranSource(tranSource);
                    gl.setReference(reference);
                    gl.setTraderCode(traderCode);
                    gl.setDeleted(deleted);
                    gl.setCash(true);
                    listGl.add(gl);
                } else {
                    updatePayment(payId, ERR);
                }
                if (discount > 0) {
                    Gl gl = new Gl();
                    GlKey key = new GlKey();
                    key.setDeptId(1);
                    key.setCompCode(compCode);
                    gl.setKey(key);
                    gl.setGlDate(payDate);
                    if (pay.getTrader().getTraderType().equals("C")) {
                        gl.setSrcAccCode(cusDisAcc);
                        gl.setAccCode(cusBalAcc);
                        gl.setDrAmt(discount);
                    } else {
                        gl.setSrcAccCode(supDisAcc);
                        gl.setAccCode(balAcc);
                        gl.setCrAmt(discount);
                    }
                    gl.setRefNo(String.valueOf(payId));
                    gl.setDeptCode(mainDept);
                    gl.setMacId(MAC_ID);
                    gl.setCreatedBy(APP_NAME);
                    gl.setCurCode(curCode);
                    gl.setDescription("Payment Discount Received");
                    gl.setCreatedDate(LocalDateTime.now());
                    gl.setTranSource(tranSource);
                    gl.setReference(reference);
                    gl.setTraderCode(traderCode);
                    gl.setDeleted(deleted);
                    gl.setCash(true);
                    listGl.add(gl);
                }
                if (!listGl.isEmpty()) sendAccount(listGl);

            }
        }
    }

    private void updatePayment(Integer id, String status) {
        paymentHisRepo.updatePayment(id, status);
        log.info(String.format("updatePayment: %s", id));
    }


    public void sendOPDGroup(Object obj) {
        if (obj instanceof OPDCategory opd) {
            String coaParent = opd.getGroup().getAccount();
            if (coaParent != null) {
                ChartOfAccount coa = new ChartOfAccount();
                COAKey key = new COAKey();
                key.setCoaCode(opd.getOpdAcc());
                key.setCompCode(compCode);
                coa.setKey(key);
                coa.setCoaLevel(3);
                coa.setCoaParent(coaParent);
                coa.setCoaNameEng(opd.getCatName());
                coa.setActive(true);
                coa.setCreatedBy(APP_NAME);
                coa.setCreatedDate(LocalDateTime.now());
                coa.setMacId(MAC_ID);
                coa.setOption("USR");
                coa.setMigCode(String.valueOf(opd.getCatId()));
                saveCOA(coa).doOnSuccess(s -> {
                    updateOPDCOA(s, ACK);
                    log.info(String.format("sendOPDGroup: %s", opd.getCatName()));
                }).block();
            } else {
                log.info("coa parent is not assigned at opd");
            }
        }
    }

    private boolean isValidOTGroup(Integer groupId) {
        if (!otServiceRepo.searchGroup(groupId, Util1.getInteger(otDepositId)).isEmpty()) {
            return false;
        }
        if (!otServiceRepo.searchGroup(groupId, Util1.getInteger(otPaidId)).isEmpty()) {
            return false;
        }
        if (!otServiceRepo.searchGroup(groupId, Util1.getInteger(otRefundId)).isEmpty()) {
            return false;
        }
        return otServiceRepo.searchGroup(groupId, Util1.getInteger(otDiscountId)).isEmpty();
    }

    public void sendOTGroup(Object obj) {
        if (obj instanceof OTGroup ot) {
            Integer groupId = ot.getGroupId();
            String coaParent = hmAccSetting.get("OT_INCOME_GROUP").getSourceAcc();
            if (coaParent != null) {
                if (isValidOTGroup(groupId)) {
                    ChartOfAccount coa = new ChartOfAccount();
                    COAKey key = new COAKey();
                    key.setCoaCode(ot.getIpdAcc());
                    key.setCompCode(compCode);
                    coa.setKey(key);
                    coa.setCoaLevel(3);
                    coa.setCoaParent(coaParent);
                    coa.setCoaNameEng(ot.getGroupName());
                    coa.setActive(true);
                    coa.setCreatedBy(APP_NAME);
                    coa.setCreatedDate(LocalDateTime.now());
                    coa.setMacId(MAC_ID);
                    coa.setOption("USR");
                    coa.setMigCode(String.valueOf(groupId));
                    saveCOA(coa).doOnSuccess(s -> {
                        updateOTCOA(s, ACK);
                        log.info(String.format("sendOTGroup: %s", ot.getGroupName()));
                    }).block();

                }
            } else {
                log.info("coa parent is not assigned at ot.");
            }
        }
    }

    private boolean isValidDCGroup(Integer groupId) {
        if (!dcServiceRepo.searchGroup(groupId, Util1.getInteger(dcDepositId)).isEmpty()) {
            return false;
        }
        if (!dcServiceRepo.searchGroup(groupId, Util1.getInteger(dcDiscountId)).isEmpty()) {
            return false;
        }
        if (!dcServiceRepo.searchGroup(groupId, Util1.getInteger(dcRefundId)).isEmpty()) {
            return false;
        }
        return dcServiceRepo.searchGroup(groupId, Util1.getInteger(dcPaidId)).isEmpty();
    }

    public void sendDCGroup(Object obj) {
        if (obj instanceof DCGroup dc) {
            Integer groupId = dc.getGroupId();
            String coaParent = hmAccSetting.get("DC_INCOME_GROUP").getSourceAcc();
            if (coaParent != null) {
                if (isValidDCGroup(groupId)) {
                    ChartOfAccount coa = new ChartOfAccount();
                    COAKey key = new COAKey();
                    key.setCoaCode(dc.getAccountCode());
                    key.setCompCode(compCode);
                    coa.setKey(key);
                    coa.setCoaLevel(3);
                    coa.setCoaParent(coaParent);
                    coa.setCoaNameEng(dc.getGroupName());
                    coa.setActive(true);
                    coa.setCreatedBy(APP_NAME);
                    coa.setCreatedDate(LocalDateTime.now());
                    coa.setMacId(MAC_ID);
                    coa.setOption("USR");
                    coa.setMigCode(String.valueOf(dc.getGroupId()));
                    saveCOA(coa).doOnSuccess(s -> updateDCCOA(s, ACK)).block();
                    log.info(String.format("sendDCGroup: %s", dc.getGroupName()));
                }
            } else {
                log.info("coa parent is not assigned at dc.");
            }
        }
    }
}
