package com.cv.integration.repo;

import com.cv.integration.entity.OTHisDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OTHisDetailRepo extends JpaRepository<OTHisDetail, String> {
    @Query("select o from OTHisDetail o where o.vouNo = :vouNo")
    List<OTHisDetail> search(@Param("vouNo") String vouNo);
}
