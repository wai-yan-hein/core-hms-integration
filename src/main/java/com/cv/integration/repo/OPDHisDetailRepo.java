package com.cv.integration.repo;

import com.cv.integration.entity.OPDHisDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OPDHisDetailRepo extends JpaRepository<OPDHisDetail, String> {
    @Query("select o from OPDHisDetail o where o.vouNo = :vouNo")
    List<OPDHisDetail> search(@Param("vouNo") String vouNo);
}
