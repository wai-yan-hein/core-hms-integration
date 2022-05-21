package com.cv.integration.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "doctor")
public class Doctor implements java.io.Serializable {
    @Id
    @Column(name = "doctor_id")
    private String doctorId;
    @Column(name = "doctor_name")
    private String doctorName;
    @ManyToOne
    @JoinColumn(name = "dr_type")
    private DoctorType drType;
    @Column(name = "active")
    private boolean active;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Doctor doctor = (Doctor) o;
        return doctorId != null && Objects.equals(doctorId, doctor.doctorId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
