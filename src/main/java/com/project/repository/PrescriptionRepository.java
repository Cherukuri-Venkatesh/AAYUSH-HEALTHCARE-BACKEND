package com.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.project.entity.Prescription;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatient_Id(Long patientId);

    List<Prescription> findByDoctor_IdAndPatient_Id(Long doctorId, Long patientId);

    Prescription findByAppointment_Id(Long appointmentId);

    @Transactional
    void deleteByPatient_Id(Long patientId);

    Optional<Prescription> findByFilePath(String filePath);

    Optional<Prescription> findByFilePathEndingWith(String suffix);
}