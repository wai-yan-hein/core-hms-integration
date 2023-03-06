package com.cv.integration.controller;

import com.cv.integration.common.Util1;
import com.cv.integration.common.Voucher;
import com.cv.integration.service.ReportService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@AllArgsConstructor
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/get-sale")
    public ResponseEntity<byte[]> getSaleVoucher(@RequestParam String vouNo) throws SQLException, IOException {
        List<Voucher> values = reportService.getSaleVoucher(vouNo);
        String exportPath = String.format("temp%s%s.json", File.separator, "sale");
        return ResponseEntity.ok(Util1.writeJsonFile(values, exportPath));
    }

    @PostMapping("/get-purchase")
    public ResponseEntity<List<Voucher>> getPurchaseVoucher(@RequestParam String vouNo) throws SQLException {
        return ResponseEntity.ok(reportService.getPurchaseVoucher(vouNo));
    }

    @PostMapping("/get-returnIn")
    public ResponseEntity<List<Voucher>> getReturnInVoucher(@RequestParam String vouNo) throws SQLException {
        return ResponseEntity.ok(reportService.getReturnInVoucher(vouNo));
    }

    @PostMapping("/get-returnOut")
    public ResponseEntity<List<Voucher>> getReturnOutVoucher(@RequestParam String vouNo) throws SQLException {
        return ResponseEntity.ok(reportService.getReturnOutVoucher(vouNo));
    }

    @PostMapping("/get-opd")
    public ResponseEntity<List<Voucher>> getOPDVoucher(@RequestParam String vouNo) throws SQLException {
        return ResponseEntity.ok(reportService.getOPDVoucher(vouNo));
    }

    @PostMapping("/get-ot")
    public ResponseEntity<List<Voucher>> getOTVoucher(@RequestParam String vouNo) throws SQLException {
        return ResponseEntity.ok(reportService.getOTVoucher(vouNo));
    }

    @PostMapping("/get-dc")
    public ResponseEntity<List<Voucher>> getDCVoucher(@RequestParam String vouNo) throws SQLException {
        return ResponseEntity.ok(reportService.getDCVoucher(vouNo));
    }

}
