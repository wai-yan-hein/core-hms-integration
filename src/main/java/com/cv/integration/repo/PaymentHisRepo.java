package com.cv.integration.repo;

import com.cv.integration.entity.PaymentHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface PaymentHisRepo extends JpaRepository<PaymentHis, Integer> {
    @Query("select o from PaymentHis o where o.intgUpdStatus is null and date(o.payDate) >= :vou_date")
    List<PaymentHis> unUploadVoucher(@Param("vou_date") Date syncDate);

    @Transactional
    @Modifying
    @Query("update PaymentHis o set o.intgUpdStatus = :status where o.payId = :payId")
    void updatePayment(@Param("payId") Integer vouNo, @Param("status") String status);
}
