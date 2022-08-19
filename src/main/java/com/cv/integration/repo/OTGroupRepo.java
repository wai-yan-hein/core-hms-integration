package com.cv.integration.repo;

import com.cv.integration.entity.OTGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OTGroupRepo extends JpaRepository<OTGroup, Integer> {
    @Transactional
    @Modifying
    @Query("update OTGroup o set o.intgUpdStatus = :status,o.opdAcc=:coaCode where o.groupId = :groupId")
    void updateOTGroup(@Param("groupId") Integer groupId, @Param("status") String status, @Param("coaCode") String coaCode);

    @Query("select o from OTGroup o where o.intgUpdStatus is null")
    List<OTGroup> unUploadOTGroup();
}
