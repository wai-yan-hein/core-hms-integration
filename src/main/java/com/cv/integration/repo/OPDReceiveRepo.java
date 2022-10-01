package com.cv.integration.repo;

import com.cv.integration.entity.OPDHis;
import com.cv.integration.entity.OPDReceive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface OPDReceiveRepo extends JpaRepository<OPDReceive, Integer> {
    @Transactional
    @Modifying
    @Query("update OPDReceive o set o.intgUpdStatus = :status where o.billId = :billId")
    void updateOPD(@Param("billId") Integer vouNo, @Param("status") String status);

    @Query("select o from OPDReceive o where o.intgUpdStatus is null and date(o.payDate) >= :vou_date")
    List<OPDReceive> unUploadVoucher(@Param("vou_date") Date syncDate);

}
