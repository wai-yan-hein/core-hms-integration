package com.cv.integration.listener;

import com.cv.integration.common.Util1;
import com.cv.integration.entity.*;
import com.cv.integration.model.AccTrader;
import com.cv.integration.model.Gl;
import com.cv.integration.repo.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;
import java.text.DateFormat;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
@PropertySource("file:config/application.properties")
public class InventoryMessageListener {
    private static final String LISTEN_QUEUE = "INVENTORY";
    private static final String ACC_QUEUE = "ACCOUNT_QUEUE";
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setDateFormat(DateFormat.FULL, DateFormat.FULL)
            .create();

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
    private final DoctorRepo doctorRepo;
    @Autowired
    private final GenExpenseRepo genExpenseRepo;
    @Autowired
    private final PaymentHisRepo paymentHisRepo;
    @Autowired
    private final JmsTemplate jmsTemplate;
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
    @Value("${account.compcode}")
    private String compCode;
    @Value("${account.inpatient.code}")
    private String inPatientCode;
    @Value("${account.outpatient.code}")
    private String outPatientCode;
    @Value("${app.type}")
    private String appType;
    @Value("${customer.account}")
    private String customerAcc;
    @Value("${supplier.account}")
    private String supplierAcc;
    @Value("${upload.doctor}")
    private String uploadDoctor;
    private final String ACK = "ACK";
    private final String APP_NAME = "CM";
    private final Integer MAC_ID = 99;
    private final String FOC = "FOC";
    private final String ERR = "ERR";


    private void sendMessage(String entity, String option, String data) {
        MessageCreator mc = (Session session) -> {
            MapMessage mm = session.createMapMessage();
            mm.setString("SENDER_QUEUE", LISTEN_QUEUE);
            mm.setString("ENTITY", entity);
            mm.setString("OPTION", option);
            mm.setString("DATA", data);
            return mm;
        };
        jmsTemplate.send(ACC_QUEUE, mc);
    }

    private void deleteGl(String tranSource, String vouNo) {
        MessageCreator mc = (Session session) -> {
            MapMessage mm = session.createMapMessage();
            mm.setString("SENDER_QUEUE", LISTEN_QUEUE);
            mm.setString("ENTITY", "GL_DEL");
            mm.setString("TRAN_SOURCE", tranSource);
            mm.setString("VOU_NO", vouNo);
            return mm;
        };
        jmsTemplate.send(ACC_QUEUE, mc);

    }

    @JmsListener(destination = LISTEN_QUEUE)
    public void receivedMessage(final MapMessage message) throws JMSException {
        String entity = Util1.isNull(message.getString("entity"), message.getString("ENTITY"));
        String code = Util1.isNull(message.getString("VOUCHER-NO"), message.getString("CODE"));
        int vouNo = message.getInt("vouNo");
        log.info(String.format("receiveMessage: %s", entity));
        switch (entity) {
            case "TRADER" -> sendTrader(code);
            case "SALE" -> sendSaleVoucherToAccount(code);
            case "PURCHASE" -> sendPurchaseVoucherToAccount(code);
            case "RETURNIN" -> sendReturnInVoucherToAccount(code);
            case "RETURNOUT" -> sendReturnOutVoucherToAccount(code);
            case "OPD" -> sendOPDVoucherToAccount(code);
            case "OT" -> sendOTVoucherToAccount(code);
            case "DC" -> sendDCVoucherToAccount(code);
            case "OPD_RECEIVE" -> sendOPDReceiveToAccount(Integer.parseInt(code));
            case "EXPENSE" -> sendGeneralExpenseToAcc(code);
            case "PAYMENT" -> sendPaymentToAcc(vouNo);
            case "ACK_TRADER" -> updateTrader(code);
            case "ACK_SALE" -> updateSale(code);
            case "ACK_PURCHASE" -> updatePurchase(code);
            case "ACK_RETURN_IN" -> updateReturnIn(code);
            case "ACK_RETURN_OUT" -> updateReturnOut(code);
            case "ACK_OPD" -> updateOPD(code);
            case "ACK_OT" -> updateOT(code);
            case "ACK_DC" -> updateDC(code);
            case "ACK_OPD_RECEIVE" -> updateOPDReceive(Integer.parseInt(code));
            case "ACK_EXPENSE" -> updateExpense(Integer.parseInt(code));
            case "ACK_PAYMENT" -> updatePayment(Integer.parseInt(code));
            default -> log.error("Unexpected value: " + message.getString("ENTITY"));
        }
    }

    public void sendTrader(String traderCode) {
        if (Util1.getBoolean(uploadTrader)) {
            Optional<Trader> trader = traderRepo.findById(traderCode);
            if (trader.isPresent()) {
                Trader c = trader.get();
                if (c.isActive()) {
                    String traderType = c.getTraderType();
                    AccTrader accTrader = new AccTrader();
                    accTrader.setTraderCode(c.getTraderCode());
                    accTrader.setTraderName(c.getTraderName());
                    accTrader.setUserCode(c.getTraderCode());
                    accTrader.setActive(true);
                    accTrader.setCompCode(compCode);
                    accTrader.setAppName(APP_NAME);
                    accTrader.setMacId(MAC_ID);
                    accTrader.setDiscriminator(traderType);
                    String data = gson.toJson(accTrader);
                    sendMessage("TRADER", "TRADER", data);
                }
            }
        }
    }

    public void sendDoctor(String doctorId) {
        if (Util1.getBoolean(uploadDoctor)) {
            Optional<Doctor> doctor = doctorRepo.findById(doctorId);
            if (doctor.isPresent()) {
                Doctor d = doctor.get();
                if (d.isActive()) {
                    AccTrader accTrader = new AccTrader();
                    DoctorType drType = d.getDrType();
                    accTrader.setTraderCode(d.getDoctorId());
                    accTrader.setTraderName(d.getDoctorName());
                    accTrader.setActive(true);
                    accTrader.setCompCode(compCode);
                    accTrader.setAppName(APP_NAME);
                    accTrader.setMacId(MAC_ID);
                    accTrader.setDiscriminator("D");
                    accTrader.setAccount(drType.getAccount());
                    String data = gson.toJson(accTrader);
                    sendMessage("TRADER", "TRADER", data);
                }
            }
        }
    }

    private void updateTrader(String traderCode) {
        Optional<Trader> trader = traderRepo.findById(traderCode);
        if (trader.isPresent()) {
            Trader t = trader.get();
            t.setIntgUpdStatus(ACK);
            traderRepo.save(t);
            log.info(String.format("updateTrader: %s", traderCode));
        }
    }

    public void sendSaleVoucherToAccount(String vouNo) {
        if (Util1.getBoolean(uploadSale)) {
            String tranSource = "SALE";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (setting != null) {
                Optional<SaleHis> saleHis = saleHisRepo.findById(vouNo);
                if (saleHis.isPresent()) {
                    SaleHis sh = saleHis.get();
                    Integer vouStatusId = sh.getVouStatusId();
                    if (vouStatusId == 1) {
                        String srcAcc = setting.getSourceAcc();
                        String payAcc = setting.getPayAcc();
                        String disAcc = setting.getDiscountAcc();
                        String balAcc = setting.getBalanceAcc();
                        String deptCode = setting.getDeptCode();
                        String ipdSrc = setting.getIpdSource();
                        Date vouDate = Util1.toMySqlDate(sh.getVouDate());
                        String traderCode = null;
                        String reference = null;
                        String accCodeByLoc = sh.getLocation().getAccCode();
                        String deptCodeByLoc = sh.getLocation().getDeptCode();
                        String traderByLoc = sh.getLocation().getTraderCode();
                        String patientType = Util1.isNullOrEmpty(sh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                        if (!Objects.isNull(sh.getPatient())) {
                            String patientNo = sh.getPatient().getPatientNo();
                            String patientName = sh.getPatient().getPatientName();
                            traderCode = Util1.isNullOrEmpty(sh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                            reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                        } else if (appType.equals("H")) {
                            if (Util1.isNullOrEmpty(traderByLoc)) {
                                reference = String.format("%s : %s : (%s)", "-", Util1.isNull(sh.getName(), "-"), patientType);
                                traderCode = Util1.isNullOrEmpty(sh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                            }
                        } else {
                            String traderName = sh.getTrader().getTraderName();
                            String traderType = sh.getTrader().getTraderType().equals("C") ? "Customer" : "Supplier";
                            traderCode = sh.getTrader().getTraderCode();
                            reference = String.format("%s : %s : (%s)", traderCode, traderName, traderType);
                        }
                        String curCode = sh.getCurrency().getAccCurCode();
                        boolean deleted = sh.isDeleted();
                        double vouTotalAmt = Util1.getDouble(sh.getVouTotal());
                        double vouPaidAmt = Util1.getDouble(sh.getVouPaid());
                        double vouDisAmt = Util1.getDouble(sh.getVouDiscount());
                        boolean admission = !Util1.isNullOrEmpty(sh.getAdmissionNo());
                        List<Gl> listGl = new ArrayList<>();
                        //income
                        if (vouTotalAmt > 0) {
                            Gl gl = new Gl();
                            gl.setGlDate(vouDate);
                            gl.setDescription("Sale Voucher Balance");
                            if (!Util1.isNullOrEmpty(accCodeByLoc)) {
                                gl.setSrcAccCode(accCodeByLoc);
                                deptCode = Util1.isNull(deptCodeByLoc, deptCode);
                                traderCode = Util1.isNull(traderByLoc, traderCode);
                            } else {
                                gl.setSrcAccCode(admission ? Util1.isNull(ipdSrc, srcAcc) : srcAcc);
                            }
                            gl.setDeptCode(deptCode);
                            gl.setAccCode(balAcc);
                            gl.setTraderCode(traderCode);
                            gl.setCrAmt(vouTotalAmt);
                            gl.setDrAmt(0.0);
                            gl.setCurCode(curCode);
                            gl.setReference(reference);
                            gl.setCompCode(compCode);
                            gl.setCreatedDate(Util1.getTodayDate());
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
                            gl.setGlDate(vouDate);
                            gl.setDescription("Sale Voucher Discount");
                            gl.setSrcAccCode(disAcc);
                            gl.setAccCode(balAcc);
                            gl.setTraderCode(traderCode);
                            gl.setDrAmt(vouDisAmt);
                            gl.setCrAmt(0.0);
                            gl.setCurCode(curCode);
                            gl.setReference(reference);
                            gl.setDeptCode(deptCode);
                            gl.setCompCode(compCode);
                            gl.setCreatedDate(Util1.getTodayDate());
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
                            gl.setGlDate(vouDate);
                            gl.setDescription("Sale Voucher Paid");
                            gl.setSrcAccCode(payAcc);
                            gl.setAccCode(balAcc);
                            gl.setTraderCode(traderCode);
                            gl.setDrAmt(vouPaidAmt);
                            gl.setCrAmt(0.0);
                            gl.setCurCode(curCode);
                            gl.setReference(reference);
                            gl.setDeptCode(deptCode);
                            gl.setCompCode(compCode);
                            gl.setCreatedDate(Util1.getTodayDate());
                            gl.setCreatedBy(APP_NAME);
                            gl.setTranSource(tranSource);
                            gl.setRefNo(vouNo);
                            gl.setDeleted(deleted);
                            gl.setMacId(MAC_ID);
                            gl.setCash(true);
                            listGl.add(gl);
                        }
                        if (!listGl.isEmpty()) {
                            sendMessage("GL_LIST", tranSource, gson.toJson(listGl));
                            log.info(String.format("sendSaleVoucherToAccount: %s", vouNo));
                        } else {
                            deleteGl(tranSource, vouNo);
                            sh.setIntgUpdStatus(FOC);
                            saleHisRepo.save(sh);
                        }
                    }

                } else {
                    log.info(String.format("sendSaleVoucherToAccount: %s not found.", vouNo));
                }
            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }

    private void updateSale(String vouNo) {
        Optional<SaleHis> saleHis = saleHisRepo.findById(vouNo);
        if (saleHis.isPresent()) {
            SaleHis sh = saleHis.get();
            sh.setIntgUpdStatus(ACK);
            saleHisRepo.save(sh);
            log.info(String.format("updateSale %s", vouNo));
        }
    }

    public void sendPurchaseVoucherToAccount(String vouNo) {
        if (Util1.getBoolean(uploadPurchase)) {
            String tranSource = "PURCHASE";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                Optional<PurHis> purHis = purHisRepo.findById(vouNo);
                if (purHis.isPresent()) {
                    PurHis ph = purHis.get();
                    String srcAcc = setting.getSourceAcc();
                    String payAcc = setting.getPayAcc();
                    String disAcc = setting.getDiscountAcc();
                    String balAcc = setting.getBalanceAcc();
                    String deptCode = setting.getDeptCode();
                    Date vouDate = Util1.toMySqlDate(ph.getVouDate());
                    String traderCode = ph.getTrader().getTraderCode();
                    String curCode = ph.getCurrency().getAccCurCode();
                    boolean deleted = ph.isDeleted();
                    double vouTotalAmt = Util1.getDouble(ph.getVouTotal());
                    double vouPaidAmt = Util1.getDouble(ph.getVouPaid());
                    double vouDisAmt = Util1.getDouble(ph.getVouDiscount());
                    String traderName = ph.getTrader().getTraderName();
                    String traderType = ph.getTrader().getTraderType().equals("C") ? "Customer" : "Supplier";
                    String reference = String.format("%s : %s : (%s)", traderCode, traderName, traderType);
                    String deptCodeByLoc = ph.getLocation().getDeptCode();
                    deptCode = Util1.isNull(deptCodeByLoc, deptCode);
                    List<Gl> listGl = new ArrayList<>();
                    //income
                    if (vouTotalAmt > 0) {
                        Gl gl = new Gl();
                        gl.setGlDate(vouDate);
                        gl.setDescription("Purchase Voucher Balance");
                        gl.setSrcAccCode(srcAcc);
                        gl.setAccCode(balAcc);
                        gl.setTraderCode(traderCode);
                        gl.setDrAmt(vouTotalAmt);
                        gl.setCrAmt(0.0);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(deptCode);
                        gl.setCompCode(compCode);
                        gl.setCreatedDate(Util1.getTodayDate());
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
                        gl.setGlDate(vouDate);
                        gl.setDescription("Purchase Voucher Discount");
                        gl.setSrcAccCode(disAcc);
                        gl.setAccCode(balAcc);
                        gl.setTraderCode(traderCode);
                        gl.setCrAmt(vouDisAmt);
                        gl.setDrAmt(0.0);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(deptCode);
                        gl.setCompCode(compCode);
                        gl.setCreatedDate(Util1.getTodayDate());
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
                        gl.setGlDate(vouDate);
                        gl.setDescription("Purchase Voucher Paid");
                        gl.setSrcAccCode(payAcc);
                        gl.setAccCode(balAcc);
                        gl.setTraderCode(traderCode);
                        gl.setCrAmt(vouPaidAmt);
                        gl.setDrAmt(0.0);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(deptCode);
                        gl.setCompCode(compCode);
                        gl.setCreatedDate(Util1.getTodayDate());
                        gl.setCreatedBy(APP_NAME);
                        gl.setTranSource(tranSource);
                        gl.setRefNo(vouNo);
                        gl.setDeleted(deleted);
                        gl.setMacId(MAC_ID);
                        gl.setCash(true);
                        listGl.add(gl);
                    }
                    if (!listGl.isEmpty()) {
                        sendMessage("GL_LIST", tranSource, gson.toJson(listGl));
                    } else {
                        deleteGl(tranSource, vouNo);
                        ph.setIntgUpdStatus(FOC);
                        purHisRepo.save(ph);
                    }
                } else {
                    log.info(String.format("sendPurchaseVoucherToAccount: %s not found.", vouNo));
                }
            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }

    private void updatePurchase(String vouNo) {
        Optional<PurHis> purHis = purHisRepo.findById(vouNo);
        if (purHis.isPresent()) {
            PurHis ph = purHis.get();
            ph.setIntgUpdStatus(ACK);
            purHisRepo.save(ph);
            log.info(String.format("updatePurchase %s", vouNo));
        }
    }

    public void sendReturnInVoucherToAccount(String vouNo) {
        if (Util1.getBoolean(uploadReturnIn)) {
            String tranSource = "RETURN_IN";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                Optional<RetInHis> retInHis = returnInRepo.findById(vouNo);
                if (retInHis.isPresent()) {
                    RetInHis ri = retInHis.get();
                    String srcAcc = setting.getSourceAcc();
                    String payAcc = setting.getPayAcc();
                    String balAcc = setting.getBalanceAcc();
                    String deptCode = setting.getDeptCode();
                    Date vouDate = Util1.toMySqlDate(ri.getVouDate());
                    String traderCode;
                    String reference;
                    if (!Objects.isNull(ri.getPatient())) {
                        String patientNo = ri.getPatient().getPatientNo();
                        String patientName = ri.getPatient().getPatientName();
                        String patientType = Util1.isNullOrEmpty(ri.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                        traderCode = Util1.isNullOrEmpty(ri.getAdmissionNo()) ? outPatientCode : inPatientCode;
                        reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                    } else if (appType.equals("H")) {
                        String patientType = Util1.isNullOrEmpty(ri.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                        reference = String.format("%s : %s : (%s)", "-", "-", patientType);
                        traderCode = Util1.isNullOrEmpty(ri.getAdmissionNo()) ? outPatientCode : inPatientCode;
                    } else {
                        String traderName = ri.getTrader().getTraderName();
                        String traderType = ri.getTrader().getTraderType().equals("C") ? "Customer" : "Supplier";
                        traderCode = ri.getTrader().getTraderCode();
                        reference = String.format("%s : %s : (%s)", traderCode, traderName, traderType);
                    }
                    String curCode = ri.getCurrency().getAccCurCode();
                    boolean deleted = ri.isDeleted();
                    double vouTotalAmt = Util1.getDouble(ri.getVouTotal());
                    double vouPaidAmt = Util1.getDouble(ri.getVouPaid());
                    String deptCodeByLoc = ri.getLocation().getDeptCode();
                    String traderByLoc = ri.getLocation().getTraderCode();
                    deptCode = Util1.isNull(deptCodeByLoc, deptCode);
                    traderCode = Util1.isNull(traderByLoc, traderCode);
                    List<Gl> listGl = new ArrayList<>();
                    //income
                    if (vouTotalAmt > 0) {
                        Gl gl = new Gl();
                        gl.setGlDate(vouDate);
                        gl.setDescription("Return In Voucher Balance");
                        gl.setSrcAccCode(srcAcc);
                        gl.setAccCode(balAcc);
                        gl.setTraderCode(traderCode);
                        gl.setDrAmt(vouTotalAmt);
                        gl.setCrAmt(0.0);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(deptCode);
                        gl.setCompCode(compCode);
                        gl.setCreatedDate(Util1.getTodayDate());
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
                        gl.setGlDate(vouDate);
                        gl.setDescription("Return In Voucher Paid");
                        gl.setSrcAccCode(payAcc);
                        gl.setAccCode(balAcc);
                        gl.setTraderCode(traderCode);
                        gl.setCrAmt(vouPaidAmt);
                        gl.setDrAmt(0.0);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(deptCode);
                        gl.setCompCode(compCode);
                        gl.setCreatedDate(Util1.getTodayDate());
                        gl.setCreatedBy(APP_NAME);
                        gl.setTranSource(tranSource);
                        gl.setRefNo(vouNo);
                        gl.setDeleted(deleted);
                        gl.setMacId(MAC_ID);
                        gl.setCash(true);
                        listGl.add(gl);
                    }
                    if (!listGl.isEmpty()) sendMessage("GL_LIST", tranSource, gson.toJson(listGl));
                } else {
                    log.info(String.format("sendReturnInVoucherToAccount: %s not found.", vouNo));
                }
            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }

    private void updateReturnIn(String vouNo) {
        Optional<RetInHis> retInHis = returnInRepo.findById(vouNo);
        if (retInHis.isPresent()) {
            RetInHis ri = retInHis.get();
            ri.setIntgUpdStatus(ACK);
            returnInRepo.save(ri);
            log.info(String.format("updateReturnIn %s", vouNo));
        }
    }

    public void sendReturnOutVoucherToAccount(String vouNo) {
        if (Util1.getBoolean(uploadReturnOut)) {
            String tranSource = "RETURN_OUT";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                Optional<RetOutHis> retOutHis = returnOutRepo.findById(vouNo);
                if (retOutHis.isPresent()) {
                    RetOutHis ro = retOutHis.get();
                    String srcAcc = setting.getSourceAcc();
                    String payAcc = setting.getPayAcc();
                    String balAcc = setting.getBalanceAcc();
                    String deptCode = setting.getDeptCode();
                    Date vouDate = Util1.toMySqlDate(ro.getVouDate());
                    String traderCode = ro.getTrader().getTraderCode();
                    String curCode = ro.getCurrency().getAccCurCode();
                    boolean deleted = ro.isDeleted();
                    double vouTotalAmt = Util1.getDouble(ro.getVouTotal());
                    double vouPaidAmt = Util1.getDouble(ro.getVouPaid());
                    String traderName = ro.getTrader().getTraderName();
                    String traderType = ro.getTrader().getTraderType().equals("C") ? "Customer" : "Supplier";
                    String reference = String.format("%s : %s : (%s)", traderCode, traderName, traderType);
                    String deptCodeByLoc = ro.getLocation().getDeptCode();
                    deptCode = Util1.isNull(deptCodeByLoc, deptCode);
                    List<Gl> listGl = new ArrayList<>();
                    //income
                    if (vouTotalAmt > 0) {
                        Gl gl = new Gl();
                        gl.setGlDate(vouDate);
                        gl.setDescription("Return Out Voucher Balance");
                        gl.setSrcAccCode(srcAcc);
                        gl.setAccCode(balAcc);
                        gl.setTraderCode(traderCode);
                        gl.setCrAmt(Util1.getDouble(ro.getVouTotal()));
                        gl.setDrAmt(0.0);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(deptCode);
                        gl.setCompCode(compCode);
                        gl.setCreatedDate(Util1.getTodayDate());
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
                        gl.setGlDate(vouDate);
                        gl.setDescription("Return Out Voucher Paid");
                        gl.setSrcAccCode(payAcc);
                        gl.setAccCode(balAcc);
                        gl.setTraderCode(traderCode);
                        gl.setDrAmt(vouPaidAmt);
                        gl.setCrAmt(0.0);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(deptCode);
                        gl.setCompCode(compCode);
                        gl.setCreatedDate(Util1.getTodayDate());
                        gl.setCreatedBy(APP_NAME);
                        gl.setTranSource(tranSource);
                        gl.setRefNo(vouNo);
                        gl.setDeleted(deleted);
                        gl.setMacId(MAC_ID);
                        gl.setCash(true);
                        listGl.add(gl);
                    }
                    if (!listGl.isEmpty()) sendMessage("GL_LIST", tranSource, gson.toJson(listGl));

                } else {
                    log.info(String.format("sendReturnOutVoucherToAccount: %s not found.", vouNo));
                }
            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }

    private void updateReturnOut(String vouNo) {
        Optional<RetOutHis> retOutHis = returnOutRepo.findById(vouNo);
        if (retOutHis.isPresent()) {
            RetOutHis ro = retOutHis.get();
            ro.setIntgUpdStatus(ACK);
            returnOutRepo.save(ro);
            log.info(String.format("updateReturnOut %s", vouNo));
        }
    }

    public void sendOPDVoucherToAccount(String vouNo) {
        if (Util1.getBoolean(uploadOPD)) {
            String tranSource = "OPD";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                Optional<OPDHis> opdHis = opdHisRepo.findById(vouNo);
                if (opdHis.isPresent()) {
                    OPDHis oh = opdHis.get();
                    String srcAcc = setting.getSourceAcc();
                    String payAcc = setting.getPayAcc();
                    String disAcc = setting.getDiscountAcc();
                    String balAcc = setting.getBalanceAcc();
                    String mainDept = setting.getDeptCode();
                    Date vouDate = Util1.toMySqlDate(oh.getVouDate());
                    String traderCode = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                    String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                    String reference;
                    if (!Objects.isNull(oh.getPatient())) {
                        String patientNo = oh.getPatient().getPatientNo();
                        String patientName = oh.getPatient().getPatientName();
                        reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                    } else {
                        reference = String.format("%s : %s : (%s)", "-", Util1.isNull(oh.getPatientName(), "-"), patientType);
                    }
                    String curCode = oh.getCurrency().getAccCurCode();
                    boolean deleted = oh.isDeleted();
                    double vouPaidAmt = Util1.getDouble(oh.getVouPaid());
                    double vouDisAmt = Util1.getDouble(oh.getVouDiscount());
                    boolean admission = !Util1.isNullOrEmpty(oh.getAdmissionNo());
                    List<OPDHisDetail> listOPD = opdHisDetailRepo.search(vouNo);
                    if (!listOPD.isEmpty()) {
                        List<Gl> listGl = new ArrayList<>();
                        for (OPDHisDetail op : listOPD) {
                            OPDCategory cat = op.getService().getCategory();
                            String serviceName = op.getService().getServiceName();
                            //account
                            String opdAcc = cat.getOpdAcc();
                            String ipdAcc = cat.getIpdAcc();
                            String deptCode = cat.getDeptCode();
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
                            double moFeeAmt = percent ?
                                    qty * amount * Util1.getDouble(op.getMoFeeAmt()) / 100
                                    : Util1.getDouble(op.getMoFeeAmt()) * qty;
                            double staffAmt = percent ?
                                    qty * amount * Util1.getDouble(op.getStaffFeeAmt()) / 100 :
                                    Util1.getDouble(op.getStaffFeeAmt()) * qty;
                            double techAmt = percent ?
                                    qty * amount * Util1.getDouble(op.getTechFeeAmt()) / 100 :
                                    Util1.getDouble(op.getTechFeeAmt()) * qty;
                            double referAmt = percent ?
                                    qty * amount * Util1.getDouble(op.getReferFeeAmt()) / 100 :
                                    Util1.getDouble(op.getReferFeeAmt()) * qty;
                            double readerAmt = percent ?
                                    qty * amount * Util1.getDouble(op.getReadFeeAmt()) / 100 :
                                    Util1.getDouble(op.getReadFeeAmt()) * qty;
                            //income
                            if (amount > 0) {
                                String tmp = admission ? Util1.isNull(ipdAcc, opdAcc) : opdAcc;
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(tmp, srcAcc));
                                gl.setAccCode(balAcc);
                                gl.setCrAmt(amount);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                                //payable
                                if (!Util1.isNullOrEmpty(payableAcc)) {
                                    String[] accounts = payableAcc.split(",");
                                    gl = new Gl();
                                    gl.setGlDate(vouDate);
                                    gl.setSrcAccCode(accounts[0]);
                                    gl.setAccCode(accounts[1]);
                                    gl.setCrAmt(amount);
                                    gl.setRefNo(vouNo);
                                    gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                    gl.setCompCode(compCode);
                                    gl.setMacId(MAC_ID);
                                    gl.setCreatedBy(APP_NAME);
                                    gl.setCurCode(curCode);
                                    gl.setRefNo(vouNo);
                                    gl.setDescription(serviceName);
                                    gl.setCreatedDate(Util1.getTodayDate());
                                    gl.setTranSource(tranSource);
                                    gl.setReference(reference);
                                    //gl.setTraderCode(traderCode);
                                    gl.setDeleted(deleted);
                                    listGl.add(gl);
                                }
                            }
                            //mo payable
                            if (moFeeAmt > 0 && !Util1.isNullOrEmpty(moAcc)) {
                                String[] accounts = moAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(moFeeAmt);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                            //staff payable
                            if (staffAmt > 0 && !Util1.isNullOrEmpty(staffAcc)) {
                                String[] accounts = staffAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(staffAmt);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                            //tech payable
                            if (techAmt > 0 && !Util1.isNullOrEmpty(techAcc)) {
                                String[] accounts = techAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(techAmt);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                            //refer payable
                            if (referAmt > 0 && !Util1.isNullOrEmpty(referAcc)) {
                                String[] accounts = referAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(referAmt);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                            //refer payable
                            if (readerAmt > 0 && !Util1.isNullOrEmpty(readerAcc)) {
                                String[] accounts = readerAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(readerAmt);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                        }
                        //discount
                        if (vouDisAmt > 0) {
                            Gl gl = new Gl();
                            gl.setGlDate(vouDate);
                            gl.setDescription("OPD Voucher Discount");
                            gl.setSrcAccCode(disAcc);
                            gl.setAccCode(balAcc);
                            gl.setTraderCode(traderCode);
                            gl.setDrAmt(vouDisAmt);
                            gl.setCrAmt(0.0);
                            gl.setCurCode(curCode);
                            gl.setReference(reference);
                            gl.setDeptCode(mainDept);
                            gl.setCompCode(compCode);
                            gl.setCreatedDate(Util1.getTodayDate());
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
                            gl.setGlDate(vouDate);
                            gl.setDescription("OPD Voucher Paid");
                            gl.setSrcAccCode(payAcc);
                            gl.setAccCode(balAcc);
                            gl.setTraderCode(traderCode);
                            gl.setDrAmt(vouPaidAmt);
                            gl.setCrAmt(0.0);
                            gl.setCurCode(curCode);
                            gl.setReference(reference);
                            gl.setDeptCode(mainDept);
                            gl.setCompCode(compCode);
                            gl.setCreatedDate(Util1.getTodayDate());
                            gl.setCreatedBy(APP_NAME);
                            gl.setTranSource(tranSource);
                            gl.setRefNo(vouNo);
                            gl.setDeleted(deleted);
                            gl.setMacId(MAC_ID);
                            gl.setCash(true);
                            listGl.add(gl);
                        }
                        if (!listGl.isEmpty()) {
                            sendMessage("GL_LIST", tranSource, gson.toJson(listGl));
                            log.info(String.format("sendOPDVoucherToAccount: %s", vouNo));
                        } else {
                            deleteGl(tranSource, vouNo);
                            oh.setIntgUpdStatus(FOC);
                            opdHisRepo.save(oh);
                        }
                    } else {
                        oh.setIntgUpdStatus(ERR);
                        opdHisRepo.save(oh);
                    }
                } else {
                    log.info(String.format("sendOPDVoucherToAccount: %s not found.", vouNo));
                }
            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
        }
    }

    private boolean opdCF(Integer id) {
        return false;
    }

    public void sendOPDVoucherByDoctor(String vouNo) {
        if (Util1.getBoolean(uploadOPD)) {
            String tranSource = "OPD";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                Optional<OPDHis> opdHis = opdHisRepo.findById(vouNo);
                if (opdHis.isPresent()) {
                    OPDHis oh = opdHis.get();
                    String srcAcc = setting.getSourceAcc();
                    String payAcc = setting.getPayAcc();
                    String disAcc = setting.getDiscountAcc();
                    String balAcc = setting.getBalanceAcc();
                    String mainDept = setting.getDeptCode();
                    Date vouDate = oh.getVouDate();
                    boolean admission = !Util1.isNullOrEmpty(oh.getAdmissionNo());
                    String traderCode = admission ? inPatientCode : outPatientCode;
                    String reference;
                    if (!Objects.isNull(oh.getPatient())) {
                        String patientNo = oh.getPatient().getPatientNo();
                        String patientName = oh.getPatient().getPatientName();
                        String patientType = admission ? "Inpatient" : "Outpatient";
                        reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                    } else {
                        String patientType = admission ? "Inpatient" : "Outpatient";
                        reference = String.format("%s : %s : (%s)", "-", "-", patientType);
                    }
                    String curCode = oh.getCurrency().getAccCurCode();
                    boolean deleted = oh.isDeleted();
                    double vouPaidAmt = Util1.getDouble(oh.getVouPaid());
                    double vouDisAmt = Util1.getDouble(oh.getVouDiscount());
                    List<Gl> listGl = new ArrayList<>();
                    if (vouDisAmt > 0) {
                        Gl gl = new Gl();
                        gl.setGlDate(vouDate);
                        gl.setDescription("OPD Voucher Discount");
                        gl.setSrcAccCode(disAcc);
                        gl.setAccCode(balAcc);
                        gl.setTraderCode(traderCode);
                        gl.setDrAmt(vouDisAmt);
                        gl.setCrAmt(0.0);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(mainDept);
                        gl.setCompCode(compCode);
                        gl.setCreatedDate(Util1.getTodayDate());
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
                        gl.setGlDate(vouDate);
                        gl.setDescription("OPD Voucher Paid");
                        gl.setSrcAccCode(payAcc);
                        gl.setAccCode(balAcc);
                        gl.setTraderCode(traderCode);
                        gl.setDrAmt(vouPaidAmt);
                        gl.setCrAmt(0.0);
                        gl.setCurCode(curCode);
                        gl.setReference(reference);
                        gl.setDeptCode(mainDept);
                        gl.setCompCode(compCode);
                        gl.setCreatedDate(Util1.getTodayDate());
                        gl.setCreatedBy(APP_NAME);
                        gl.setTranSource(tranSource);
                        gl.setRefNo(vouNo);
                        gl.setDeleted(deleted);
                        gl.setMacId(MAC_ID);
                        gl.setCash(true);
                        listGl.add(gl);
                    }
                    List<OPDHisDetail> listOPD = opdHisDetailRepo.search(vouNo);
                    if (!listOPD.isEmpty()) {
                        for (OPDHisDetail op : listOPD) {
                            OPDCategory category = op.getService().getCategory();
                            String serviceName = op.getService().getServiceName();
                            String groupAcc = category.getGroup().getAccount();
                            String groupDept = category.getGroup().getDeptCode();
                            String catId = category.getCatId().toString();
                            String catName = category.getCatName();
                            //amount
                            double amount = Util1.getDouble(op.getAmount());
                            //income
                            if (amount > 0) {
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setMigId(catId);
                                gl.setMigName(catName);
                                gl.setCoaParent(groupAcc);
                                gl.setAccCode(balAcc);
                                gl.setCrAmt(amount);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(groupDept, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }

                        }
                        if (!listGl.isEmpty()) sendMessage("GL_NEW", tranSource, gson.toJson(listGl));
                    }
                } else {
                    log.info(String.format("sendOPDVoucherToAccount: %s not found.", vouNo));
                }
            } else {
                log.error(String.format("%s Setting not assigned", tranSource));
            }
            log.info(String.format("sendOPDVoucherToAccount: %s", vouNo));
        }
    }

    private void updateOPD(String vouNo) {
        Optional<OPDHis> opdHis = opdHisRepo.findById(vouNo);
        if (opdHis.isPresent()) {
            OPDHis ph = opdHis.get();
            ph.setIntgUpdStatus(ACK);
            opdHisRepo.save(ph);
            log.info(String.format("updateOPD %s", vouNo));
        }
    }

    public void sendOTVoucherByDoctor(String vouNo) {
        if (Util1.getBoolean(uploadOT)) {
            String tranSource = "OT";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                Optional<OTHis> otHis = otHisRepo.findById(vouNo);
                if (otHis.isPresent()) {
                    OTHis oh = otHis.get();
                    String srcAcc = setting.getSourceAcc();
                    String payAcc = setting.getPayAcc();
                    String disAcc = setting.getDiscountAcc();
                    String balAcc = setting.getBalanceAcc();
                    String mainDept = setting.getDeptCode();
                    Date vouDate = oh.getVouDate();
                    String traderCode = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                    String reference;
                    if (!Objects.isNull(oh.getPatient())) {
                        String patientNo = oh.getPatient().getPatientNo();
                        String patientName = oh.getPatient().getPatientName();
                        String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                        reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                    } else {
                        String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                        reference = String.format("%s : %s : (%s)", "-", "-", patientType);
                    }
                    String curCode = oh.getCurrency().getAccCurCode();
                    boolean deleted = oh.isDeleted();
                    boolean admission = !Util1.isNullOrEmpty(oh.getAdmissionNo());

                    List<OTHisDetail> listOT = otHisDetailRepo.search(vouNo);
                    if (!listOT.isEmpty()) {
                        List<Gl> listGl = new ArrayList<>();
                        for (OTHisDetail ot : listOT) {
                            OTGroup group = ot.getService().getOtGroup();
                            String serviceName = ot.getService().getServiceName();
                            Integer serviceId = ot.getService().getServiceId();
                            //account
                            String opdAcc = group.getOpdAcc();
                            String ipdAcc = group.getIpdAcc();
                            String deptCode = group.getDeptCode();
                            String moAcc = group.getMoFeeAcc();
                            String staffAcc = group.getStaffFeeAcc();
                            String nurseAcc = group.getNurseFeeAcc();
                            String payableAcc = group.getPayableAcc();
                            //amount
                            double amount = Util1.getDouble(ot.getAmount());
                            double moFeeAmt = Util1.getDouble(ot.getMoFeeAmt());
                            double staffAmt = Util1.getDouble(ot.getStaffFeeAmt());
                            double nurseAmt = Util1.getDouble(ot.getNurseFeeAmt());
                            //discount
                            if (serviceId == Util1.getInteger(otDiscountId)) {
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(opdAcc, disAcc));
                                gl.setAccCode(balAcc);
                                gl.setDrAmt(amount);
                                gl.setCrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            } else if (serviceId == Util1.getInteger(otPaidId) || serviceId == Util1.getInteger(otDepositId)) {
                                //paid or deposit
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(opdAcc, payAcc));
                                gl.setAccCode(balAcc);
                                gl.setDrAmt(amount);
                                gl.setCrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                gl.setCash(true);
                                listGl.add(gl);
                            } else if (serviceId == Util1.getInteger(otRefundId)) {
                                //refund
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(opdAcc, payAcc));
                                gl.setAccCode(balAcc);
                                gl.setCrAmt(amount);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                gl.setCash(true);
                                listGl.add(gl);
                            } else {
                                //income
                                if (amount > 0) {
                                    String tmp = admission ? Util1.isNull(ipdAcc, opdAcc) : opdAcc;
                                    Gl gl = new Gl();
                                    gl.setGlDate(vouDate);
                                    gl.setSrcAccCode(Util1.isNull(tmp, srcAcc));
                                    gl.setAccCode(balAcc);
                                    gl.setCrAmt(amount);
                                    gl.setDrAmt(0.0);
                                    gl.setRefNo(vouNo);
                                    gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                    gl.setCompCode(compCode);
                                    gl.setMacId(MAC_ID);
                                    gl.setCreatedBy(APP_NAME);
                                    gl.setCurCode(curCode);
                                    gl.setRefNo(vouNo);
                                    gl.setDescription(serviceName);
                                    gl.setCreatedDate(Util1.getTodayDate());
                                    gl.setTranSource(tranSource);
                                    gl.setReference(reference);
                                    gl.setTraderCode(traderCode);
                                    gl.setDeleted(deleted);
                                    listGl.add(gl);
                                    //payable
                                    if (!Util1.isNullOrEmpty(payableAcc)) {
                                        String[] accounts = payableAcc.split(",");
                                        gl = new Gl();
                                        gl.setGlDate(vouDate);
                                        gl.setSrcAccCode(accounts[0]);
                                        gl.setAccCode(accounts[1]);
                                        gl.setCrAmt(amount);
                                        gl.setDrAmt(0.0);
                                        gl.setRefNo(vouNo);
                                        gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                        gl.setCompCode(compCode);
                                        gl.setMacId(MAC_ID);
                                        gl.setCreatedBy(APP_NAME);
                                        gl.setCurCode(curCode);
                                        gl.setRefNo(vouNo);
                                        gl.setDescription(serviceName);
                                        gl.setCreatedDate(Util1.getTodayDate());
                                        gl.setTranSource(tranSource);
                                        gl.setReference(reference);
                                        gl.setTraderCode(traderCode);
                                        gl.setDeleted(deleted);
                                        listGl.add(gl);
                                    }
                                }
                            }
                            //mo payable
                            if (moFeeAmt > 0 && !Util1.isNullOrEmpty(moAcc)) {
                                String[] accounts = moAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(moFeeAmt);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                            //staff payable
                            if (staffAmt > 0 && !Util1.isNullOrEmpty(staffAcc)) {
                                String[] accounts = staffAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(staffAmt);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                            //nurse payable
                            if (nurseAmt > 0 && !Util1.isNullOrEmpty(nurseAcc)) {
                                String[] accounts = nurseAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(nurseAmt);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                        }
                        if (!listGl.isEmpty()) sendMessage("GL_LIST", tranSource, gson.toJson(listGl));
                    } else {
                        log.info(String.format("sendOTVoucherToAccount: %s not found.", vouNo));
                    }
                } else {
                    log.error(String.format("%s Setting not assigned", tranSource));
                }
                log.info(String.format("sendOTVoucherToAccount: %s", vouNo));
            }
        }
    }

    public void sendOTVoucherToAccount(String vouNo) {
        if (Util1.getBoolean(uploadOT)) {
            String tranSource = "OT";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                Optional<OTHis> otHis = otHisRepo.findById(vouNo);
                if (otHis.isPresent()) {
                    OTHis oh = otHis.get();
                    String srcAcc = setting.getSourceAcc();
                    String payAcc = setting.getPayAcc();
                    String disAcc = setting.getDiscountAcc();
                    String balAcc = setting.getBalanceAcc();
                    String mainDept = setting.getDeptCode();
                    Date vouDate = Util1.toMySqlDate(oh.getVouDate());
                    String traderCode = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                    String reference;
                    if (!Objects.isNull(oh.getPatient())) {
                        String patientNo = oh.getPatient().getPatientNo();
                        String patientName = oh.getPatient().getPatientName();
                        String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                        reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                    } else {
                        String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                        reference = String.format("%s : %s : (%s)", "-", "-", patientType);
                    }
                    String curCode = oh.getCurrency().getAccCurCode();
                    boolean deleted = oh.isDeleted();
                    boolean admission = !Util1.isNullOrEmpty(oh.getAdmissionNo());

                    List<OTHisDetail> listOT = otHisDetailRepo.search(vouNo);
                    if (!listOT.isEmpty()) {
                        List<Gl> listGl = new ArrayList<>();
                        for (OTHisDetail ot : listOT) {
                            OTGroup group = ot.getService().getOtGroup();
                            String serviceName = ot.getService().getServiceName();
                            Integer serviceId = ot.getService().getServiceId();
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
                            double moFeeAmt = Util1.getDouble(ot.getMoFeeAmt()) * qty;
                            double staffAmt = Util1.getDouble(ot.getStaffFeeAmt()) * qty;
                            double nurseAmt = Util1.getDouble(ot.getNurseFeeAmt()) * qty;
                            //discount
                            if (serviceId == Util1.getInteger(otDiscountId)) {
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(opdAcc, disAcc));
                                gl.setAccCode(balAcc);
                                gl.setDrAmt(amount);
                                gl.setCrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            } else if (serviceId == Util1.getInteger(otPaidId) || serviceId == Util1.getInteger(otDepositId)) {
                                //paid or deposit
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(opdAcc, payAcc));
                                gl.setAccCode(balAcc);
                                gl.setDrAmt(amount);
                                gl.setCrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                gl.setCash(true);
                                listGl.add(gl);
                            } else if (serviceId == Util1.getInteger(otRefundId)) {
                                //refund
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(opdAcc, payAcc));
                                gl.setAccCode(balAcc);
                                gl.setCrAmt(amount);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                gl.setCash(true);
                                listGl.add(gl);
                            } else {
                                //income
                                if (amount > 0) {
                                    String tmp = admission ? Util1.isNull(ipdAcc, opdAcc) : opdAcc;
                                    Gl gl = new Gl();
                                    gl.setGlDate(vouDate);
                                    gl.setSrcAccCode(Util1.isNull(tmp, srcAcc));
                                    gl.setAccCode(balAcc);
                                    gl.setCrAmt(amount);
                                    gl.setDrAmt(0.0);
                                    gl.setRefNo(vouNo);
                                    gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                    gl.setCompCode(compCode);
                                    gl.setMacId(MAC_ID);
                                    gl.setCreatedBy(APP_NAME);
                                    gl.setCurCode(curCode);
                                    gl.setRefNo(vouNo);
                                    gl.setDescription(serviceName);
                                    gl.setCreatedDate(Util1.getTodayDate());
                                    gl.setTranSource(tranSource);
                                    gl.setReference(reference);
                                    gl.setTraderCode(traderCode);
                                    gl.setDeleted(deleted);
                                    listGl.add(gl);
                                    //payable
                                    if (!Util1.isNullOrEmpty(payableAcc)) {
                                        String[] accounts = payableAcc.split(",");
                                        gl = new Gl();
                                        gl.setGlDate(vouDate);
                                        gl.setSrcAccCode(accounts[0]);
                                        gl.setAccCode(accounts[1]);
                                        gl.setCrAmt(amount);
                                        gl.setDrAmt(0.0);
                                        gl.setRefNo(vouNo);
                                        gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                        gl.setCompCode(compCode);
                                        gl.setMacId(MAC_ID);
                                        gl.setCreatedBy(APP_NAME);
                                        gl.setCurCode(curCode);
                                        gl.setRefNo(vouNo);
                                        gl.setDescription(serviceName);
                                        gl.setCreatedDate(Util1.getTodayDate());
                                        gl.setTranSource(tranSource);
                                        gl.setReference(reference);
                                        //gl.setTraderCode(traderCode);
                                        gl.setDeleted(deleted);
                                        listGl.add(gl);
                                    }
                                }
                            }
                            //mo payable
                            if (moFeeAmt > 0 && !Util1.isNullOrEmpty(moAcc)) {
                                String[] accounts = moAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(moFeeAmt);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                            //staff payable
                            if (staffAmt > 0 && !Util1.isNullOrEmpty(staffAcc)) {
                                String[] accounts = staffAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(staffAmt);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                            //nurse payable
                            if (nurseAmt > 0 && !Util1.isNullOrEmpty(nurseAcc)) {
                                String[] accounts = nurseAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(nurseAmt);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                        }
                        if (!listGl.isEmpty()) {
                            sendMessage("GL_LIST", tranSource, gson.toJson(listGl));
                            log.info(String.format("sendOTVoucherToAccount: %s", vouNo));
                        } else {
                            deleteGl(tranSource, vouNo);
                            oh.setIntgUpdStatus(FOC);
                            otHisRepo.save(oh);
                        }
                    } else {
                        oh.setIntgUpdStatus(ERR);
                        otHisRepo.save(oh);
                    }
                } else {
                    log.error(String.format("%s Setting not assigned", tranSource));
                }
            }
        }
    }

    private void updateOT(String vouNo) {
        Optional<OTHis> otHis = otHisRepo.findById(vouNo);
        if (otHis.isPresent()) {
            OTHis ot = otHis.get();
            ot.setIntgUpdStatus(ACK);
            otHisRepo.save(ot);
            log.info(String.format("updateOT %s", vouNo));
        }
    }

    public void sendDCVoucherToAccount(String vouNo) {
        if (Util1.getBoolean(uploadDC)) {
            String tranSource = "DC";
            AccountSetting setting = hmAccSetting.get(tranSource);
            if (!Objects.isNull(setting)) {
                Optional<DCHis> dcHis = dcHisRepo.findById(vouNo);
                if (dcHis.isPresent()) {
                    DCHis oh = dcHis.get();
                    String srcAcc = setting.getSourceAcc();
                    String payAcc = setting.getPayAcc();
                    String disAcc = setting.getDiscountAcc();
                    String balAcc = setting.getBalanceAcc();
                    String mainDept = setting.getDeptCode();
                    Date vouDate = Util1.toMySqlDate(oh.getVouDate());
                    String traderCode = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? outPatientCode : inPatientCode;
                    String reference;
                    if (!Objects.isNull(oh.getPatient())) {
                        String patientNo = oh.getPatient().getPatientNo();
                        String patientName = oh.getPatient().getPatientName();
                        String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                        reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                    } else {
                        String patientType = Util1.isNullOrEmpty(oh.getAdmissionNo()) ? "Outpatient" : "Inpatient";
                        reference = String.format("%s : %s : (%s)", "-", "-", patientType);
                    }
                    String curCode = oh.getCurrency().getAccCurCode();
                    boolean deleted = oh.isDeleted();
                    List<DCHisDetail> listDC = dcHisDetailRepo.search(vouNo);
                    if (!listDC.isEmpty()) {
                        List<Gl> listGl = new ArrayList<>();
                        for (DCHisDetail dc : listDC) {
                            DCGroup group = dc.getService().getDcGroup();
                            String serviceName = dc.getService().getServiceName();
                            Integer serviceId = dc.getService().getServiceId();
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
                            double moFeeAmt = Util1.getDouble(dc.getMoFeeAmt()) * qty;
                            double techAmt = Util1.getDouble(dc.getTechFeeAmt()) * qty;
                            double nurseAmt = Util1.getDouble(dc.getNurseFeeAmt()) * qty;
                            //discount
                            if (serviceId == Util1.getInteger(dcDiscountId)) {
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(accountCode, disAcc));
                                gl.setAccCode(balAcc);
                                gl.setDrAmt(amount);
                                gl.setCrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            } else if (serviceId == Util1.getInteger(dcPaidId) || serviceId == Util1.getInteger(dcDepositId)) {
                                //paid or deposit
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(accountCode, payAcc));
                                gl.setAccCode(balAcc);
                                gl.setDrAmt(amount);
                                gl.setCrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                gl.setCash(true);
                                listGl.add(gl);
                            } else if (serviceId == Util1.getInteger(dcRefundId)) {
                                //refund
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(Util1.isNull(accountCode, payAcc));
                                gl.setAccCode(balAcc);
                                gl.setCrAmt(amount);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                gl.setCash(true);
                                listGl.add(gl);
                            } else {
                                //income
                                if (amount > 0) {
                                    Gl gl = new Gl();
                                    gl.setGlDate(vouDate);
                                    gl.setSrcAccCode(Util1.isNull(accountCode, srcAcc));
                                    gl.setAccCode(balAcc);
                                    gl.setCrAmt(amount);
                                    gl.setDrAmt(0.0);
                                    gl.setRefNo(vouNo);
                                    gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                    gl.setCompCode(compCode);
                                    gl.setMacId(MAC_ID);
                                    gl.setCreatedBy(APP_NAME);
                                    gl.setCurCode(curCode);
                                    gl.setRefNo(vouNo);
                                    gl.setDescription(serviceName);
                                    gl.setCreatedDate(Util1.getTodayDate());
                                    gl.setTranSource(tranSource);
                                    gl.setReference(reference);
                                    gl.setTraderCode(traderCode);
                                    gl.setDeleted(deleted);
                                    listGl.add(gl);
                                    //payable
                                    if (!Util1.isNullOrEmpty(payableAcc)) {
                                        String[] accounts = payableAcc.split(",");
                                        gl = new Gl();
                                        gl.setGlDate(vouDate);
                                        gl.setSrcAccCode(accounts[0]);
                                        gl.setAccCode(accounts[1]);
                                        gl.setCrAmt(amount);
                                        gl.setDrAmt(0.0);
                                        gl.setRefNo(vouNo);
                                        gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                        gl.setCompCode(compCode);
                                        gl.setMacId(MAC_ID);
                                        gl.setCreatedBy(APP_NAME);
                                        gl.setCurCode(curCode);
                                        gl.setRefNo(vouNo);
                                        gl.setDescription(serviceName);
                                        gl.setCreatedDate(Util1.getTodayDate());
                                        gl.setTranSource(tranSource);
                                        gl.setReference(reference);
                                        //gl.setTraderCode(traderCode);
                                        gl.setDeleted(deleted);
                                        listGl.add(gl);
                                    }
                                }
                            }
                            //mo payable
                            if (moFeeAmt > 0 && !Util1.isNullOrEmpty(moAcc)) {
                                String[] accounts = moAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(moFeeAmt);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                            //tech payable
                            if (techAmt > 0 && !Util1.isNullOrEmpty(techAcc)) {
                                String[] accounts = techAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(techAmt);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                            //nurse payable
                            if (nurseAmt > 0 && !Util1.isNullOrEmpty(nurseAcc)) {
                                String[] accounts = nurseAcc.split(",");
                                Gl gl = new Gl();
                                gl.setGlDate(vouDate);
                                gl.setSrcAccCode(accounts[0]);
                                gl.setAccCode(accounts[1]);
                                gl.setCrAmt(nurseAmt);
                                gl.setDrAmt(0.0);
                                gl.setRefNo(vouNo);
                                gl.setDeptCode(Util1.isNull(deptCode, mainDept));
                                gl.setCompCode(compCode);
                                gl.setMacId(MAC_ID);
                                gl.setCreatedBy(APP_NAME);
                                gl.setCurCode(curCode);
                                gl.setRefNo(vouNo);
                                gl.setDescription(serviceName);
                                gl.setCreatedDate(Util1.getTodayDate());
                                gl.setTranSource(tranSource);
                                gl.setReference(reference);
                                //gl.setTraderCode(traderCode);
                                gl.setDeleted(deleted);
                                listGl.add(gl);
                            }
                        }
                        if (!listGl.isEmpty()) {
                            sendMessage("GL_LIST", tranSource, gson.toJson(listGl));
                            log.info(String.format("sendDCVoucherToAccount: %s", vouNo));
                        } else {
                            deleteGl(tranSource, vouNo);
                            oh.setIntgUpdStatus(FOC);
                            dcHisRepo.save(oh);
                        }
                    } else {
                        oh.setIntgUpdStatus(ERR);
                        dcHisRepo.save(oh);
                    }
                } else {
                    log.error(String.format("%s Setting not assigned", tranSource));
                }
            }
        }
    }

    private void updateDC(String vouNo) {
        Optional<DCHis> otHis = dcHisRepo.findById(vouNo);
        if (otHis.isPresent()) {
            DCHis dc = otHis.get();
            dc.setIntgUpdStatus(ACK);
            dcHisRepo.save(dc);
            log.info(String.format("updateDC %s", vouNo));
        }
    }

    private void sendOPDReceiveToAccount(Integer id) {
        String tranSource = "OPD_RECEIVE";
        AccountSetting as = hmAccSetting.get(tranSource);
        if (!Objects.isNull(as)) {
            Optional<OPDReceive> opdReceive = opdReceiveRepo.findById(id);
            if (opdReceive.isPresent()) {
                OPDReceive receive = opdReceive.get();
                String payAcc = as.getPayAcc();
                String balAcc = as.getBalanceAcc();
                String mainDept = as.getDeptCode();
                String patientType = "Outpatient";
                Date payDate = receive.getPayDate();
                double payAmt = Util1.getDouble(receive.getPayAmt());
                String curCode = receive.getCurCode();
                String reference;
                if (!Objects.isNull(receive.getPatient())) {
                    String patientNo = receive.getPatient().getPatientNo();
                    String patientName = receive.getPatient().getPatientName();
                    reference = String.format("%s : %s : (%s)", patientNo, patientName, patientType);
                } else {
                    reference = String.format("%s : %s : (%s)", "-", "-", patientType);
                }
                if (payAmt > 0) {
                    Gl gl = new Gl();
                    gl.setGlDate(payDate);
                    gl.setSrcAccCode(payAcc);
                    gl.setAccCode(balAcc);
                    gl.setDrAmt(payAmt);
                    gl.setCrAmt(0.0);
                    gl.setRefNo(String.valueOf(id));
                    gl.setDeptCode(mainDept);
                    gl.setCompCode(compCode);
                    gl.setMacId(MAC_ID);
                    gl.setCreatedBy(APP_NAME);
                    gl.setCurCode(curCode);
                    gl.setDescription("OPD Bill Receive");
                    gl.setCreatedDate(Util1.getTodayDate());
                    gl.setTranSource(tranSource);
                    gl.setReference(reference);
                    gl.setTraderCode(outPatientCode);
                    gl.setDeleted(false);
                    sendMessage("GL", tranSource, gson.toJson(gl));
                }
            }
        }
    }

    private void updateOPDReceive(Integer id) {
        Optional<OPDReceive> opdReceive = opdReceiveRepo.findById(id);
        if (opdReceive.isPresent()) {
            OPDReceive opd = opdReceive.get();
            opd.setIntgUpdStatus(ACK);
            opdReceiveRepo.save(opd);
            log.info(String.format("updateOPDReceive %s", id));
        }
    }

    public void sendGeneralExpenseToAcc(String vouNo) {
        if (Util1.getBoolean(uploadExpense)) {
            String tranSource = "EXPENSE";
            String tmpVouNo = vouNo.substring(vouNo.indexOf("-") + 1);
            String expOption = vouNo.replace("-" + tmpVouNo, "");
            List<GenExpense> listExp = genExpenseRepo.search(tmpVouNo, expOption);
            if (!listExp.isEmpty()) {
                List<Gl> listGl = new ArrayList<>();
                for (GenExpense ge : listExp) {
                    String description = ge.getDescription();
                    Date expDate = ge.getExpDate();
                    String remark = ge.getRemark();
                    String srcAcc = ge.getSrcAcc();
                    String account = ge.getAccount();
                    String depCode = ge.getDeptCode();
                    String curCode = ge.getCurrency().getAccCurCode();
                    String expenseId = ge.getGenId().toString();
                    double expAmt = ge.getExpAmt();
                    boolean deleted = ge.isDeleted();
                    if (Util1.getDouble(expAmt) > 0) {
                        Gl gl = new Gl();
                        gl.setGlDate(expDate);
                        gl.setSrcAccCode(srcAcc);
                        gl.setAccCode(account);
                        gl.setCrAmt(expAmt);
                        gl.setDrAmt(0.0);
                        gl.setRefNo(expenseId);
                        gl.setDeptCode(depCode);
                        gl.setCompCode(compCode);
                        gl.setMacId(MAC_ID);
                        gl.setCreatedBy(APP_NAME);
                        gl.setCurCode(curCode);
                        gl.setDescription(description);
                        gl.setCreatedDate(Util1.getTodayDate());
                        gl.setTranSource(tranSource);
                        gl.setReference(remark);
                        gl.setDeleted(deleted);
                        gl.setCash(true);
                        listGl.add(gl);
                    }
                    if (!listGl.isEmpty()) sendMessage("GL_LIST", tranSource, gson.toJson(listGl));
                }
            }
        }
    }

    private void updateExpense(Integer id) {
        Optional<GenExpense> genExpense = genExpenseRepo.findById(id);
        if (genExpense.isPresent()) {
            GenExpense ge = genExpense.get();
            ge.setIntgUpdStatus(ACK);
            genExpenseRepo.save(ge);
            log.info(String.format("updateExpense %s", id));
        }
    }

    public void sendPaymentToAcc(Integer payId) {
        if (Util1.getBoolean(uploadPayment)) {
            String tranSource = "PAYMENT";
            AccountSetting as = hmAccSetting.get(tranSource);
            if (!Objects.isNull(as)) {
                Optional<PaymentHis> payment = paymentHisRepo.findById(payId);
                if (payment.isPresent()) {
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
                    PaymentHis pay = payment.get();
                    String payAcc = pay.getPaymentType() == null ? as.getPayAcc() : pay.getPaymentType().getAccount();
                    String payAccOut = as.getPayAccOut();
                    String mainDept = as.getDeptCode();
                    Date payDate = Util1.toMySqlDate(pay.getPayDate());
                    double payAmt = Util1.getDouble(pay.getPayAmt());
                    double discount = Util1.getDouble(pay.getDiscount());
                    boolean deleted = pay.isDeleted();
                    String curCode = pay.getCurrency().getAccCurCode();
                    String traderCode = pay.getTrader().getTraderCode();
                    String traderName = pay.getTrader().getTraderName();
                    String traderType = pay.getTrader().getTraderType().equals("C") ? "Customer" : "Supplier";
                    String balAcc = pay.getTrader().getTraderType().equals("C") ? cusBalAcc : supBalAcc;
                    String reference = String.format("%s : %s : (%s)", traderCode, traderName, traderType);
                    String description = String.format("%s (%s)", traderType, "Payment");
                    List<Gl> listGl = new ArrayList<>();
                    if (payAmt > 0) {
                        Gl gl = new Gl();
                        gl.setGlDate(payDate);
                        gl.setAccCode(balAcc);
                        if (pay.getTrader().getTraderType().equals("C")) {
                            gl.setSrcAccCode(payAcc);
                            gl.setDrAmt(payAmt);
                            gl.setCrAmt(0.0);
                        } else {
                            gl.setSrcAccCode(Util1.isNull(payAccOut, payAcc));
                            gl.setDrAmt(0.0);
                            gl.setCrAmt(payAmt);
                        }
                        gl.setRefNo(String.valueOf(payId));
                        gl.setDeptCode(mainDept);
                        gl.setCompCode(compCode);
                        gl.setMacId(MAC_ID);
                        gl.setCreatedBy(APP_NAME);
                        gl.setCurCode(curCode);
                        gl.setDescription(description);
                        gl.setCreatedDate(Util1.getTodayDate());
                        gl.setTranSource(tranSource);
                        gl.setReference(reference);
                        gl.setTraderCode(traderCode);
                        gl.setDeleted(deleted);
                        gl.setCash(true);
                        listGl.add(gl);
                    }
                    if (discount > 0) {
                        Gl gl = new Gl();
                        gl.setGlDate(payDate);
                        if (pay.getTrader().getTraderType().equals("C")) {
                            gl.setSrcAccCode(cusDisAcc);
                            gl.setAccCode(cusBalAcc);
                            gl.setCrAmt(discount);
                            gl.setDrAmt(0.0);
                        } else {
                            gl.setSrcAccCode(supDisAcc);
                            gl.setAccCode(balAcc);
                            gl.setDrAmt(discount);
                            gl.setCrAmt(0.0);
                        }
                        gl.setSrcAccCode(payAcc);
                        gl.setAccCode(balAcc);
                        gl.setRefNo(String.valueOf(payId));
                        gl.setDeptCode(mainDept);
                        gl.setCompCode(compCode);
                        gl.setMacId(MAC_ID);
                        gl.setCreatedBy(APP_NAME);
                        gl.setCurCode(curCode);
                        gl.setDescription(description);
                        gl.setCreatedDate(Util1.getTodayDate());
                        gl.setTranSource(tranSource);
                        gl.setReference(reference);
                        gl.setTraderCode(traderCode);
                        gl.setDeleted(deleted);
                        gl.setCash(true);
                        listGl.add(gl);
                    }
                    if (!listGl.isEmpty()) sendMessage("GL_LIST", tranSource, gson.toJson(listGl));
                }
            }
        }
    }

    private void updatePayment(Integer id) {
        Optional<PaymentHis> payHis = paymentHisRepo.findById(id);
        if (payHis.isPresent()) {
            PaymentHis ph = payHis.get();
            ph.setIntgUpdStatus(ACK);
            paymentHisRepo.save(ph);
            log.info(String.format("updatePayment %s", id));
        }
    }
}
