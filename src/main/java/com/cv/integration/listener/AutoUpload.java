package com.cv.integration.listener;

import com.cv.integration.common.Util1;
import com.cv.integration.entity.*;
import com.cv.integration.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@PropertySource("file:config/application.properties")
public class AutoUpload {
    @Value("${sync.date}")
    private String syncDate;
    @Value("${upload.trader}")
    private String uploadTrader;
    @Value("${upload.doctor}")
    private String uploadDoctor;
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
    @Autowired
    private final InventoryMessageListener listener;
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
    private final DCHisRepo dcHisRepo;
    @Autowired
    private final TraderRepo traderRepo;
    @Autowired
    private final DoctorRepo doctorRepo;
    @Autowired
    private final PaymentHisRepo paymentHis;
    @Autowired
    private final GenExpenseRepo genExpenseRepo;
    private boolean syncing = false;

    @Scheduled(fixedRate = 10 * 60 * 1000)
    private void autoUpload() {
        if (!syncing) {
            log.info("autoUpload: Start");
            syncing = true;
            uploadTrader();
            uploadDoctor();
            uploadSaleVoucher();
            uploadPurchaseVoucher();
            uploadReturnInVoucher();
            uploadPayment();
            uploadReturnOutVoucher();
            uploadOPDVoucher();
            uploadOTVoucher();
            uploadDCVoucher();
            uploadExpense();
            syncing = false;
            log.info("autoUpload: End");
        }
    }

    private void uploadTrader() {
        if (Util1.getBoolean(uploadTrader)) {
            List<Trader> traders = traderRepo.unUploadTrader();
            if (!traders.isEmpty()) {
                log.info(String.format("uploadTrader: %s", traders.size()));
                traders.forEach(t -> listener.sendTrader(t.getTraderCode()));
            }
        }
    }

    private void uploadDoctor() {
        if (Util1.getBoolean(uploadDoctor)) {
            List<Doctor> doctors = doctorRepo.unUploadDoctor();
            if (!doctors.isEmpty()) {
                log.info(String.format("uploadDoctor: %s", doctors.size()));
                doctors.forEach(t -> listener.sendDoctor(t.getDoctorId()));
            }
        }
    }

    private void uploadSaleVoucher() {
        if (Util1.getBoolean(uploadSale)) {
            List<SaleHis> vouchers = saleHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadSaleVoucher: %s", vouchers.size()));
                vouchers.forEach(vou -> listener.sendSaleVoucherToAccount(vou.getVouNo()));
            }
        }
    }

    private void uploadPurchaseVoucher() {
        if (Util1.getBoolean(uploadPurchase)) {
            List<PurHis> vouchers = purHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadPurchaseVoucher: %s", vouchers.size()));
                vouchers.forEach(vou -> listener.sendPurchaseVoucherToAccount(vou.getVouNo()));
            }
        }
    }

    private void uploadReturnInVoucher() {
        if (Util1.getBoolean(uploadReturnIn)) {
            List<RetInHis> vouchers = returnInRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadReturnInVoucher: %s", vouchers.size()));
                vouchers.forEach(vou -> listener.sendReturnInVoucherToAccount(vou.getVouNo()));
            }
        }
    }

    private void uploadReturnOutVoucher() {
        if (Util1.getBoolean(uploadReturnOut)) {
            List<RetOutHis> vouchers = returnOutRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadReturnOutVoucher: %s", vouchers.size()));
                vouchers.forEach(vou -> listener.sendReturnOutVoucherToAccount(vou.getVouNo()));
            }
        }
    }

    private void uploadOPDVoucher() {
        if (Util1.getBoolean(uploadOPD)) {
            List<OPDHis> vouchers = opdHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadOPDVoucher: %s", vouchers.size()));
                vouchers.forEach(vou -> listener.sendOPDVoucherToAccount(vou.getVouNo()));
            }
        }
    }

    private void uploadOTVoucher() {
        if (Util1.getBoolean(uploadOT)) {
            List<OTHis> vouchers = otHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadOTVoucher: %s", vouchers.size()));
                vouchers.forEach(vou -> listener.sendOTVoucherToAccount(vou.getVouNo()));
            }
        }
    }

    private void uploadDCVoucher() {
        if (Util1.getBoolean(uploadDC)) {
            List<DCHis> vouchers = dcHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadDCVoucher: %s", vouchers.size()));
                vouchers.forEach(vou -> listener.sendDCVoucherToAccount(vou.getVouNo()));
            }
        }
    }

    private void uploadPayment() {
        if (Util1.getBoolean(uploadPayment)) {
            List<PaymentHis> vouchers = paymentHis.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadPayment: %s", vouchers.size()));
                vouchers.forEach(vou -> listener.sendPaymentToAcc(vou.getPayId()));
            }
        }
    }

    private void uploadExpense() {
        if (Util1.getBoolean(uploadExpense)) {
            List<GenExpense> vouchers = genExpenseRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadExpense: %s", vouchers.size()));
                vouchers.forEach(vou -> listener.sendGeneralExpenseToAcc(vou.getExpOption() + "-" + vou.getVouNo()));
            }
        }
    }

}
