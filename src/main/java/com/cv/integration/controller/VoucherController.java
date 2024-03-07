package com.cv.integration.controller;

import com.cv.integration.entity.*;
import com.cv.integration.listener.AutoUpload;
import com.cv.integration.listener.HMSIntegration;
import com.cv.integration.model.SyncModel;
import com.cv.integration.repo.*;
import com.cv.integration.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class VoucherController {
    private final HMSIntegration integration;
    private final SaleHisRepo saleHisRepo;
    private final PurHisRepo purHisRepo;
    private final ReturnInRepo returnInRepo;
    private final ReturnOutRepo returnOutRepo;
    private final OPDHisRepo opdHisRepo;
    private final OTHisRepo otHisRepo;
    private final DCHisRepo dcHisRepo;
    private final PaymentHisRepo paymentHisRepo;
    private final GenExpenseRepo expenseRepo;
    private final OPDReceiveRepo opdReceiveRepo;
    private final OPDCategoryRepo opdCategoryRepo;
    private final DCGroupRepo dcGroupRepo;
    private final ReportService reportService;
    private final OTGroupRepo otGroupRepo;
    private final AutoUpload autoUpload;

    @GetMapping("/apiTest")
    private ResponseEntity<?> test() {
        log.info("/apiTest");
        return ResponseEntity.status(HttpStatus.FOUND).body("OK");
    }

    @PostMapping("/sale1")
    private ResponseEntity<?> saleVoucher1(@RequestParam(name = "vouNo") String vouNo) {
        log.info("/sale : " + vouNo);
        return ResponseEntity.status(HttpStatus.FOUND).body("OK");
    }

    @PostMapping("/sale")
    private Mono<?> saleVoucher(@RequestParam String vouNo) {
        log.info("/sale : " + vouNo);
        Optional<SaleHis> option = saleHisRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendSaleVoucherToAccount(option.get());
            return Mono.just("sent.");
        }
        return Mono.just("sale voucher not found : " + vouNo);
    }

    @PostMapping("/purchase")
    private ResponseEntity<?> purchaseVoucher(@RequestParam String vouNo) {
        log.info("/Purchase : " + vouNo);
        Optional<PurHis> option = purHisRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendPurchaseVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/returnIn")
    private ResponseEntity<?> retInVoucher(@RequestParam String vouNo) {
        log.info("/returnIn : " + vouNo);
        Optional<RetInHis> option = returnInRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendReturnInVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/returnOut")
    private ResponseEntity<?> retOutVoucher(@RequestParam String vouNo) {
        log.info("/returnOut : " + vouNo);
        Optional<RetOutHis> option = returnOutRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendReturnOutVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/opd")
    private ResponseEntity<?> opdVoucher(@RequestParam String vouNo) {
        log.info("/opd : " + vouNo);
        Optional<OPDHis> option = opdHisRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendOPDVoucherToAccount(option.get());

            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/dc")
    private ResponseEntity<?> dcVoucher(@RequestParam String vouNo) {
        log.info("/dc : " + vouNo);
        Optional<DCHis> option = dcHisRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendDCVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/ot")
    private ResponseEntity<?> otVoucher(@RequestParam String vouNo) {
        log.info("/ot : " + vouNo);
        Optional<OTHis> option = otHisRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendOTVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/payment")
    private ResponseEntity<?> paymentVoucher(@RequestParam String payId) {
        log.info("/payment : " + payId);
        Optional<PaymentHis> option = paymentHisRepo.findById(Integer.parseInt(payId));
        if (option.isPresent()) {
            integration.sendPaymentToAcc(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/expense")
    private ResponseEntity<?> expenseVoucher(@RequestParam String expId) {
        log.info("/expense : " + expId);
        Optional<GenExpense> option = expenseRepo.findById(Integer.parseInt(expId));
        if (option.isPresent()) {
            integration.sendGeneralExpenseToAcc(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/opdReceive")
    private ResponseEntity<?> opdReceive(@RequestParam String id) {
        log.info("/opdReceive : " + id);
        Optional<OPDReceive> option = opdReceiveRepo.findById(Integer.parseInt(id));
        if (option.isPresent()) {
            integration.sendOPDReceiveToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/opdCategory")
    private ResponseEntity<?> opdCategory(@RequestParam String id) {
        log.info("/opdCategory : " + id);
        Optional<OPDCategory> option = opdCategoryRepo.findById(Integer.parseInt(id));
        if (option.isPresent()) {
            integration.sendOPDGroup(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/otGroup")
    private ResponseEntity<?> otGroup(@RequestParam String id) {
        log.info("/otGroup : " + id);
        Optional<OTGroup> option = otGroupRepo.findById(Integer.parseInt(id));
        if (option.isPresent()) {
            integration.sendOTGroup(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/dcGroup")
    private ResponseEntity<?> dcGroup(@RequestParam String id) {
        log.info("/dcGroup : " + id);
        Optional<DCGroup> option = dcGroupRepo.findById(Integer.parseInt(id));
        if (option.isPresent()) {
            integration.sendDCGroup(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @GetMapping("/getSaleList")
    private ResponseEntity<?> getSaleList(@RequestParam String fromDate, @RequestParam String toDate) {
        return ResponseEntity.ok(reportService.getSaleList(fromDate, toDate));
    }
    @GetMapping("/getPurchaseList")
    private ResponseEntity<?> getPurchaseList(@RequestParam String fromDate, @RequestParam String toDate) {
        return ResponseEntity.ok(reportService.getPurchaseList(fromDate, toDate));
    }
    @GetMapping("/getReturnInList")
    private ResponseEntity<?> getReturnInList(@RequestParam String fromDate, @RequestParam String toDate) {
        return ResponseEntity.ok(reportService.getReturnInList(fromDate, toDate));
    }
    @GetMapping("/getReturnOutList")
    private ResponseEntity<?> getReturnOutList(@RequestParam String fromDate, @RequestParam String toDate) {
        return ResponseEntity.ok(reportService.getReturnOutList(fromDate, toDate));
    }
    @GetMapping("/getOPDList")
    private ResponseEntity<?> getOPDList(@RequestParam String fromDate, @RequestParam String toDate) {
        return ResponseEntity.ok(reportService.getOPDList(fromDate, toDate));
    }
    @GetMapping("/getOTList")
    private ResponseEntity<?> getOTList(@RequestParam String fromDate, @RequestParam String toDate) {
        return ResponseEntity.ok(reportService.getOTList(fromDate, toDate));
    }
    @GetMapping("/getDCList")
    private ResponseEntity<?> getDCList(@RequestParam String fromDate, @RequestParam String toDate) {
        return ResponseEntity.ok(reportService.getDCList(fromDate, toDate));
    }
    @GetMapping("/getPaymentList")
    private ResponseEntity<?> getPaymentList(@RequestParam String fromDate, @RequestParam String toDate) {
        return ResponseEntity.ok(reportService.getPaymentList(fromDate, toDate));
    }
    @PostMapping("/syncVoucher")
    public Mono<Boolean> syncVoucher(@RequestBody List<SyncModel> list) {
        boolean sync =reportService.syncData(list);
        if(sync){
            autoUpload.autoUpload();
        }
        return Mono.just(true);
    }
}
