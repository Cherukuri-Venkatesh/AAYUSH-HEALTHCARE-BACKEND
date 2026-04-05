package com.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.project.entity.Admindoctor;

@Repository
public interface AdminRepository extends JpaRepository<Admindoctor, Long> {
	
	Optional<Admindoctor> findByEmail(String email);

    Optional<Admindoctor> findByEmailIgnoreCase(String email);

    // Find by specialization
    List<Admindoctor> findBySpecialization(String specialization);

    // Find by name
    Admindoctor findByName(String name);

    // AND condition
    List<Admindoctor> findBySpecializationAndConsultingFeesGreaterThan(String specialization, Integer fees);

    // OR condition
    List<Admindoctor> findBySpecializationOrDegree(String specialization, String degree);

    // Between fees
    List<Admindoctor> findByConsultingFeesBetween(Integer min, Integer max);

    // Like search (search doctor name)
    List<Admindoctor> findByNameContaining(String keyword);

    // Greater than
    List<Admindoctor> findByConsultingFeesGreaterThan(Integer fees);

    // Count
    long countBySpecialization(String specialization);

    // Exists
    boolean existsByEmail(String email);

    // Delete by email
    @Transactional
    void deleteByEmail(String email);

    // JPQL Custom Query
    @Query("SELECT d FROM Admindoctor d ORDER BY d.consultingFees ASC")
    List<Admindoctor> sortByFeesAsc();
    boolean existsByWhatsappNumber(String whatsappNumber);
}