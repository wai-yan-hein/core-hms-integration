package com.cv.integration.repo;

import com.cv.integration.entity.GenExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface GenExpenseRepo extends JpaRepository<GenExpense, Integer> {
    @Transactional
    @Modifying
    @Query("update GenExpense o set o.intgUpdStatus = :status where o.genId = :genId")
    void updateExpense(@Param("genId") Integer vouNo, @Param("status") String status);

    @Query("select o from GenExpense o where o.intgUpdStatus is null and date(o.expDate) >= :vou_date")
    List<GenExpense> unUploadVoucher(@Param("vou_date") Date syncDate);
}
