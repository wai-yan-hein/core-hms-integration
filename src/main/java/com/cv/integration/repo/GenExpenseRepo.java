package com.cv.integration.repo;

import com.cv.integration.entity.GenExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface GenExpenseRepo extends JpaRepository<GenExpense, Integer> {
    @Query("select o from GenExpense o where o.vouNo = :vouNo and o.expOption = :option")
    List<GenExpense> search(@Param("vouNo") String vouNo, @Param("option") String option);

    @Query("select o from GenExpense o where o.intgUpdStatus is null and date(o.expDate) >= :vou_date")
    List<GenExpense> unUploadVoucher(@Param("vou_date") Date syncDate);
}
