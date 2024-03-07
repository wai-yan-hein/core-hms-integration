package com.cv.integration.repo;

import com.cv.integration.entity.RetInHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface ReturnInRepo extends JpaRepository<RetInHis, String> {
    @Query("select o from RetInHis o where o.intgUpdStatus is null and date(o.vouDate) >= :vou_date")
    List<RetInHis> unUploadVoucher(@Param("vou_date") Date syncDate);
    @Query("select o from RetInHis o where o.vouPaid<>0 and o.intgUpdStatus is null and date(o.vouDate) >= :vou_date")
    List<RetInHis> unUploadVoucherCash(@Param("vou_date") Date syncDate);

    @Transactional
    @Modifying
    @Query("update RetInHis o set o.intgUpdStatus = :status where o.vouNo = :vouNo")
    void updateReturnIn(@Param("vouNo") String vouNo, @Param("status") String status);

}
