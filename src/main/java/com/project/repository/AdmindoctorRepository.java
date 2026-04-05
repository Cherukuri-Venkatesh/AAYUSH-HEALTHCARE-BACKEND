package com.project.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.entity.Admindoctor;

public interface AdmindoctorRepository extends JpaRepository<Admindoctor, Long> {

    Optional<Admindoctor> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByWhatsappNumber(String whatsappNumber);
}