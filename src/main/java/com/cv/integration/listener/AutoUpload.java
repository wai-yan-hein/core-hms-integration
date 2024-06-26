package com.cv.integration.listener;

import com.cv.integration.common.Util1;
import com.cv.integration.entity.*;
import com.cv.integration.repo.*;
import com.cv.integration.service.LabCostUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
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
    @Autowired
    private final TraderOpeningRepo traderOpeningRepo;
    @Autowired
    private final HMSIntegration listener;
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
    private final PaymentHisRepo paymentHis;
    @Autowired
    private final GenExpenseRepo genExpenseRepo;
    @Autowired
    private final OPDReceiveRepo opdReceiveRepo;
    @Autowired
    private final OPDCategoryRepo opdGroupRepo;
    @Autowired
    private final OTGroupRepo otGroupRepo;
    @Autowired
    private final DCGroupRepo dcGroupRepo;
    @Autowired
    private Environment environment;
    private boolean syncing = false;
    @Autowired
    private LabCostUpdateService lcuService;

    @Scheduled(fixedRate = 10 * 60 * 1000)
    //@Scheduled(fixedRate = 1 * 60 * 1000)
    public void autoUpload() {
        if (!syncing) {
            //log.info("autoUpload: Start");
            syncing = true;
            uploadOPDSetup();
            uploadOTSetup();
            uploadDCSetup();
            uploadTrader();
            uploadTraderOpening();
            uploadSaleVoucher();
            uploadPurchaseVoucher();
            uploadReturnInVoucher();
            uploadPayment();
            uploadReturnOutVoucher();
            uploadOPDVoucher();
            uploadOTVoucher();
            uploadDCVoucher();
            uploadExpense();
            uploadOPDReceive();
            //log.info("updateLabCost : Start");
            lcuService.updateLabCost();
            //log.info("updateLabCost : End");
            syncing = false;
            //log.info("autoUpload: End");
        }
    }

    private void uploadOPDSetup() {
        if (Util1.getBoolean(environment.getProperty("upload.opd.setup"))) {
            List<OPDCategory> categories = opdGroupRepo.unUploadOPDCategory();
            for (OPDCategory c : categories) {
                listener.sendOPDGroup(c);
            }
        }
    }

    private void uploadOTSetup() {
        if (Util1.getBoolean(environment.getProperty("upload.ot.setup"))) {
            List<OTGroup> otGroups = otGroupRepo.unUploadOTGroup();
            for (OTGroup g : otGroups) {
                listener.sendOTGroup(g);
            }
        }
    }

    private void uploadDCSetup() {
        if (Util1.getBoolean(environment.getProperty("upload.dc.setup"))) {
            List<DCGroup> dcGroups = dcGroupRepo.unUploadDCGroup();
            for (DCGroup g : dcGroups) {
                listener.sendDCGroup(g);
            }
        }
    }

    private void uploadTraderOpening() {
        List<TraderOpening> listOP = traderOpeningRepo.unUploadVoucher(Util1.toDate(syncDate));
        if (!listOP.isEmpty()) {
            log.info(String.format("uploadTraderOpening: %s", listOP.size()));
            for (TraderOpening op : listOP) {
                listener.sendTraderOpening(op);
            }
        }
    }

    private void uploadTrader() {
        if (Util1.getBoolean(uploadTrader)) {
            List<Trader> traders = traderRepo.unUploadTrader();
            if (!traders.isEmpty()) {
                log.info(String.format("uploadTrader: %s", traders.size()));
                traders.forEach(listener::sendTrader);
            }
        }
    }


    private void uploadSaleVoucher() {
        if (Util1.getBoolean(uploadSale)) {
            List<SaleHis> vouchers = saleHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadSaleVoucher: %s", vouchers.size()));
                vouchers.forEach(listener::sendSaleVoucherToAccount);
            }
        }
    }

    private void uploadPurchaseVoucher() {
        if (Util1.getBoolean(uploadPurchase)) {
            List<PurHis> vouchers = purHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadPurchaseVoucher: %s", vouchers.size()));
                vouchers.forEach(listener::sendPurchaseVoucherToAccount);
            }
        }
    }

    private void uploadReturnInVoucher() {
        if (Util1.getBoolean(uploadReturnIn)) {
            List<RetInHis> vouchers = returnInRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadReturnInVoucher: %s", vouchers.size()));
                vouchers.forEach(listener::sendReturnInVoucherToAccount);

            }
        }
    }

    private void uploadReturnOutVoucher() {
        if (Util1.getBoolean(uploadReturnOut)) {
            List<RetOutHis> vouchers = returnOutRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadReturnOutVoucher: %s", vouchers.size()));
                vouchers.forEach(listener::sendReturnOutVoucherToAccount);

            }
        }
    }

    private void uploadOPDVoucher() {
        if (Util1.getBoolean(uploadOPD)) {
            List<OPDHis> vouchers = opdHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadOPDVoucher: %s", vouchers.size()));
                vouchers.forEach(listener::sendOPDVoucherToAccount);
            }
        }
    }

    private void uploadOTVoucher() {
        if (Util1.getBoolean(uploadOT)) {
            List<OTHis> vouchers = otHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadOTVoucher: %s", vouchers.size()));
                vouchers.forEach(listener::sendOTVoucherToAccount);

            }
        }
    }

    private void uploadDCVoucher() {
        if (Util1.getBoolean(uploadDC)) {
            List<DCHis> vouchers = dcHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadDCVoucher: %s", vouchers.size()));
                vouchers.forEach(listener::sendDCVoucherToAccount);

            }
        }
    }

    private void uploadPayment() {
        if (Util1.getBoolean(uploadPayment)) {
            List<PaymentHis> vouchers = paymentHis.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadPayment: %s", vouchers.size()));
                vouchers.forEach(listener::sendPaymentToAcc);
            }
        }
    }

    private void uploadExpense() {
        if (Util1.getBoolean(uploadExpense)) {
            List<GenExpense> vouchers = genExpenseRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadExpense: %s", vouchers.size()));
                vouchers.forEach(listener::sendGeneralExpenseToAcc);
            }
        }
    }

    private void uploadOPDReceive() {
        if (Util1.getBoolean(uploadOPDBill)) {
            List<OPDReceive> vouchers = opdReceiveRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadOPDReceive: %s", vouchers.size()));
                vouchers.forEach(listener::sendOPDReceiveToAccount);
            }

        }
    }

}
