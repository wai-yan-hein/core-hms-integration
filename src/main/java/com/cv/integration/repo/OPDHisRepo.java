package com.cv.integration.repo;

import com.cv.integration.entity.OPDHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface OPDHisRepo extends JpaRepository<OPDHis, String> {
    @Query("select o from OPDHis o where o.intgUpdStatus is null and date(o.vouDate) >= :vou_date")
    List<OPDHis> unUploadVoucher(@Param("vou_date") Date syncDate);
}
