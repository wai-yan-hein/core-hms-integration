package com.cv.integration.repo;

import com.cv.integration.entity.OTHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface OTHisRepo extends JpaRepository<OTHis, String> {
    @Query("select o from OTHis o where o.intgUpdStatus is null and date(o.vouDate) >= :vou_date")
    List<OTHis> unUploadVoucher(@Param("vou_date") Date syncDate);
    @Transactional
    @Modifying
    @Query("update OTHis o set o.intgUpdStatus = :status where o.vouNo = :vouNo")
    void updateOT(@Param("vouNo") String vouNo, @Param("status") String status);

}
