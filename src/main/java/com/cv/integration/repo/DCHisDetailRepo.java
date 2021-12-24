package com.cv.integration.repo;

import com.cv.integration.entity.DCHisDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DCHisDetailRepo extends JpaRepository<DCHisDetail, String> {
    @Query("select o from DCHisDetail o where o.vouNo = :vouNo")
    List<DCHisDetail> search(@Param("vouNo") String vouNo);
}
