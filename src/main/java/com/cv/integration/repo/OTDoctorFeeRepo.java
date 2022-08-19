package com.cv.integration.repo;

import com.cv.integration.entity.OTDoctorFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OTDoctorFeeRepo extends JpaRepository<OTDoctorFee, String> {
    @Query("select o from OTDoctorFee o where o.otDetailId = :otDetailId")
    List<OTDoctorFee> search(@Param("otDetailId") String otDetailId);
}
