package com.cv.integration.listener;

import com.cv.integration.common.Util1;
import com.cv.integration.entity.*;
import com.cv.integration.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
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
    private final TraderOpeningRepo traderOpeningRepo;
    private final HMSIntegration listener;
    private final SaleHisRepo saleHisRepo;
    private final PurHisRepo purHisRepo;
    private final ReturnInRepo returnInRepo;
    private final ReturnOutRepo returnOutRepo;
    private final OPDHisRepo opdHisRepo;
    private final OTHisRepo otHisRepo;
    private final DCHisRepo dcHisRepo;
    private final TraderRepo traderRepo;
    private final PaymentHisRepo paymentHis;
    private final GenExpenseRepo genExpenseRepo;
    private final OPDReceiveRepo opdReceiveRepo;
    private final OPDCategoryRepo opdGroupRepo;
    private final OTGroupRepo otGroupRepo;
    private final DCGroupRepo dcGroupRepo;
    private final Environment environment;
    private final TaskExecutor taskExecutor;
    private boolean syncing = false;

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void autoUpload() {
        if (!syncing) {
            taskExecutor.execute(() -> {
                log.info("autoUpload: Start");
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
                syncing = false;
                log.info("autoUpload: End");

            });
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
            List<OTHis> vouchers =otHisRepo.unUploadVoucher(Util1.toDate(syncDate));
            if (!vouchers.isEmpty()) {
                log.info(String.format("uploadOTVoucher: %s", vouchers.size()));
                vouchers.forEach(listener::sendOTVoucherToAccount);
            }
        }

    }


    private void uploadDCVoucher() {
        if (Util1.getBoolean(uploadDC)) {
            List<DCHis> vouchers =dcHisRepo.unUploadVoucher(Util1.toDate(syncDate));
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
                for (PaymentHis vou : vouchers) {
                    listener.sendPaymentToAcc(vou);
                }
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
