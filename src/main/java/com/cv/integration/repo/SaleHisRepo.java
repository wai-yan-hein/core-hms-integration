package com.cv.integration.repo;

import com.cv.integration.entity.SaleHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface SaleHisRepo extends JpaRepository<SaleHis, String> {
    @Query("select o from SaleHis o where o.intgUpdStatus is null and date(o.vouDate) >= :vou_date")
    List<SaleHis> unUploadVoucher(@Param("vou_date") Date synDate);

    @Transactional
    @Modifying
    @Query("update SaleHis o set o.intgUpdStatus = :status where o.vouNo = :vouNo")
    void updateSale(@Param("vouNo") String vouNo, @Param("status") String status);

}
