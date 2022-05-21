package com.cv.integration.repo;

import com.cv.integration.entity.SaleHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.EntityManagerFactory;
import java.util.Date;
import java.util.List;

public interface SaleHisRepo extends JpaRepository<SaleHis, String> {
    @Query("select o from SaleHis o where o.intgUpdStatus is null and date(o.vouDate) >= :vou_date")
    List<SaleHis> unUploadVoucher(@Param("vou_date") Date synDate);

    @Query("select o from SaleHis o where o.vouNo = :vouNo")
    List<SaleHis> getSaleVoucher(@Param("vouNo") String vouNo);

}
