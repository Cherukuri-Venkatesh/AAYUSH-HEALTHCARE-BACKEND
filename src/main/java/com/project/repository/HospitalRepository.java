package com.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.entity.Hospital;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    boolean existsByName(String name);

    @Query("SELECT COUNT(h) > 0 FROM Hospital h WHERE LOWER(TRIM(h.name)) = LOWER(TRIM(:name))")
    boolean existsByNormalizedName(@Param("name") String name);

    @Query("SELECT COUNT(h) > 0 FROM Hospital h WHERE LOWER(TRIM(h.name)) = LOWER(TRIM(:name)) AND h.id <> :id")
    boolean existsByNormalizedNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("SELECT COUNT(h) > 0 FROM Hospital h WHERE LOWER(TRIM(h.email)) = LOWER(TRIM(:email))")
    boolean existsByNormalizedEmail(@Param("email") String email);

    @Query("SELECT COUNT(h) > 0 FROM Hospital h WHERE LOWER(TRIM(h.email)) = LOWER(TRIM(:email)) AND h.id <> :id")
    boolean existsByNormalizedEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    Hospital findByName(String name);

    // Count doctors in hospital
    @Query("SELECT COUNT(d) FROM Hospital h JOIN h.doctors d WHERE h.id = :hospitalId")
    long countDoctorsByHospitalId(Long hospitalId);

    // Hospitals with minimum doctors
    @Query("SELECT h FROM Hospital h WHERE SIZE(h.doctors) >= :minDoctors")
    List<Hospital> findHospitalsWithMinimumDoctors(int minDoctors);
}
