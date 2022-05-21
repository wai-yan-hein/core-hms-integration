package com.cv.integration.repo;

import com.cv.integration.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DoctorRepo extends JpaRepository<Doctor, String> {
    @Query("select o from Doctor o where o.active = true and o.intgUpdStatus is null")
    List<Doctor> unUploadDoctor();
}
