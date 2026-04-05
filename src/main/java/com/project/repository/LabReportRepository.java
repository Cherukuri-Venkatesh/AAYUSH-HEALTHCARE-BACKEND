package com.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.project.entity.LabReport;
public interface LabReportRepository extends JpaRepository<LabReport, Long> {

    List<LabReport> findByPatient_Id(Long patientId);

    List<LabReport> findByDoctor_Id(Long doctorId);

    LabReport findByAppointment_Id(Long appointmentId);

    @Transactional
    void deleteByPatient_Id(Long patientId);

    Optional<LabReport> findByReportPath(String reportPath);

    Optional<LabReport> findByReportPathEndingWith(String suffix);
}