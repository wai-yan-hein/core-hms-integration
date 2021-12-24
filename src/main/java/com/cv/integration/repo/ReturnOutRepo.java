package com.cv.integration.repo;

import com.cv.integration.entity.RetOutHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ReturnOutRepo extends JpaRepository<RetOutHis, String> {
    @Query("select o from RetOutHis o where o.intgUpdStatus is null and date(o.vouDate) >= :vou_date")
    List<RetOutHis> unUploadVoucher(@Param("vou_date") Date syncDate);
}
