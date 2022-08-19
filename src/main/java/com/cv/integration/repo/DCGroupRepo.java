package com.cv.integration.repo;

import com.cv.integration.entity.DCGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DCGroupRepo extends JpaRepository<DCGroup, Integer> {
    @Transactional
    @Modifying
    @Query("update DCGroup o set o.intgUpdStatus = :status,o.accountCode=:coaCode where o.groupId = :groupId")
    void updateDCGroup(@Param("groupId") Integer groupId, @Param("status") String status, @Param("coaCode") String coaCode);

    @Query("select o from DCGroup o where o.intgUpdStatus is null")
    List<DCGroup> unUploadDCGroup();
}
