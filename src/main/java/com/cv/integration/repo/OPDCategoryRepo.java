package com.cv.integration.repo;

import com.cv.integration.entity.OPDCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OPDCategoryRepo extends JpaRepository<OPDCategory, Integer> {
    @Transactional
    @Modifying
    @Query("update OPDCategory o set o.intgUpdStatus = :status,o.opdAcc=:coaCode where o.catId = :catId")
    void updateOPDCategory(@Param("catId") Integer catId, @Param("status") String status, @Param("coaCode") String coaCode);

    @Query("select o from OPDCategory o where o.intgUpdStatus is null")
    List<OPDCategory> unUploadOPDCategory();
}
