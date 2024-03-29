package com.cv.integration.repo;

import com.cv.integration.entity.DCDoctorFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DCDoctorFeeRepo extends JpaRepository<DCDoctorFee, String> {
    @Query("select o from DCDoctorFee o where o.dcDetailId = :dcDetailId")
    List<DCDoctorFee> search(@Param("dcDetailId") String dcDetailId);
}
