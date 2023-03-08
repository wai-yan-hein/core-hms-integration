package com.cv.integration.controller;

import com.cv.integration.entity.*;
import com.cv.integration.listener.HMSIntegration;
import com.cv.integration.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
public class VoucherController {
    @Autowired
    private HMSIntegration integration;
    @Autowired
    private SaleHisRepo saleHisRepo;
    @Autowired
    private PurHisRepo purHisRepo;
    @Autowired
    private ReturnInRepo returnInRepo;
    @Autowired
    private ReturnOutRepo returnOutRepo;
    @Autowired
    private OPDHisRepo opdHisRepo;
    @Autowired
    private OTHisRepo otHisRepo;
    @Autowired
    private DCHisRepo dcHisRepo;
    @Autowired
    private PaymentHisRepo paymentHisRepo;
    @Autowired
    private GenExpenseRepo expenseRepo;
    @Autowired
    private OPDReceiveRepo opdReceiveRepo;
    @Autowired
    private OPDCategoryRepo opdCategoryRepo;
    @Autowired
    private OTGroupRepo otGroupRepo;
    @Autowired
    private DCGroupRepo dcGroupRepo;

    @PostMapping("/sale")
    private ResponseEntity<?> saleVoucher(@RequestParam String vouNo) {
        Optional<SaleHis> option = saleHisRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendSaleVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/purchase")
    private ResponseEntity<?> purchaseVoucher(@RequestParam String vouNo) {
        Optional<PurHis> option = purHisRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendPurchaseVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/returnIn")
    private ResponseEntity<?> retInVoucher(@RequestParam String vouNo) {
        Optional<RetInHis> option = returnInRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendReturnInVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/returnOut")
    private ResponseEntity<?> retOutVoucher(@RequestParam String vouNo) {
        Optional<RetOutHis> option = returnOutRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendReturnOutVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/opd")
    private ResponseEntity<?> opdVoucher(@RequestParam String vouNo) {
        Optional<OPDHis> option = opdHisRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendOPDVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/dc")
    private ResponseEntity<?> dcVoucher(@RequestParam String vouNo) {
        Optional<DCHis> option = dcHisRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendDCVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/ot")
    private ResponseEntity<?> otVoucher(@RequestParam String vouNo) {
        Optional<OTHis> option = otHisRepo.findById(vouNo);
        if (option.isPresent()) {
            integration.sendOTVoucherToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/payment")
    private ResponseEntity<?> paymentVoucher(@RequestParam String payId) {
        Optional<PaymentHis> option = paymentHisRepo.findById(Integer.parseInt(payId));
        if (option.isPresent()) {
            integration.sendPaymentToAcc(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/expense")
    private ResponseEntity<?> expenseVoucher(@RequestParam String expId) {
        Optional<GenExpense> option = expenseRepo.findById(Integer.parseInt(expId));
        if (option.isPresent()) {
            integration.sendGeneralExpenseToAcc(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/opdReceive")
    private ResponseEntity<?> opdReceive(@RequestParam String id) {
        Optional<OPDReceive> option = opdReceiveRepo.findById(Integer.parseInt(id));
        if (option.isPresent()) {
            integration.sendOPDReceiveToAccount(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/opdCategory")
    private ResponseEntity<?> opdCategory(@RequestParam String id) {
        Optional<OPDCategory> option = opdCategoryRepo.findById(Integer.parseInt(id));
        if (option.isPresent()) {
            integration.sendOPDGroup(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/otGroup")
    private ResponseEntity<?> otGroup(@RequestParam String id) {
        Optional<OTGroup> option = otGroupRepo.findById(Integer.parseInt(id));
        if (option.isPresent()) {
            integration.sendOTGroup(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }

    @PostMapping("/dcGroup")
    private ResponseEntity<?> dcGroup(@RequestParam String id) {
        Optional<DCGroup> option = dcGroupRepo.findById(Integer.parseInt(id));
        if (option.isPresent()) {
            integration.sendDCGroup(option.get());
            return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher Not found.");
    }
}
