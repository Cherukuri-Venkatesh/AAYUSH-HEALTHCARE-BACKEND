package com.project.service;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.project.dto.LabReportDTO;
import com.project.entity.Admindoctor;
import com.project.entity.Appointment;
import com.project.entity.LabReport;
import com.project.entity.Patient;
import com.project.repository.AdmindoctorRepository;
import com.project.repository.AppointmentRepository;
import com.project.repository.LabReportRepository;
import com.project.repository.PatientRepository;
import com.project.security.AuthContextService;

@Service
public class LabReportService {

    @Autowired
    private LabReportRepository labReportRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AdmindoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AuthContextService authContextService;

    public LabReportDTO uploadReport(
            Long appointmentId,
            Long doctorId,
            Long patientId,
            MultipartFile file) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Long effectiveDoctorId = authContextService.resolveDoctorId(doctorId);

        if (!appointment.getDoctor().getId().equals(effectiveDoctorId)) {
            throw new RuntimeException("Unauthorized");
        }

        Long effectivePatientId = appointment.getPatient().getId();

        if (patientId != null && !patientId.equals(effectivePatientId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!"COMPLETED".equals(appointment.getStatus())) {
            throw new RuntimeException("Upload allowed only after completion");
        }

        Admindoctor doctor = doctorRepository.findById(effectiveDoctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Patient patient = patientRepository.findById(effectivePatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        String filePath = fileStorageService.saveLabReport(file);

        LabReport report = new LabReport();
        report.setAppointment(appointment);
        report.setDoctor(doctor);
        report.setPatient(patient);
        report.setReportPath(filePath);
        report.setUploadedAt(LocalDateTime.now());

        labReportRepository.save(report);

        appointment.setLabReportPath(filePath);
        appointmentRepository.save(appointment);

        LabReportDTO dto = new LabReportDTO();
        dto.setId(report.getId());
        dto.setAppointmentId(appointmentId);
        dto.setDoctorId(effectiveDoctorId);
        dto.setDoctorName(doctor.getName());
        dto.setPatientId(effectivePatientId);
        dto.setFilePath(filePath);
        dto.setUploadedAt(report.getUploadedAt() == null ? null : report.getUploadedAt().toString());

        return dto;
    }

    public List<LabReportDTO> getCurrentPatientReports() {
        Long patientId = authContextService.getCurrentPatientId();

        return labReportRepository.findByPatient_Id(patientId)
                .stream()
                .map(report -> {
                    LabReportDTO dto = new LabReportDTO();
                    dto.setId(report.getId());
                    dto.setAppointmentId(report.getAppointment().getId());
                    dto.setDoctorId(report.getDoctor().getId());
                    dto.setDoctorName(report.getDoctor().getName());
                    dto.setPatientId(report.getPatient().getId());
                    dto.setFilePath(report.getReportPath());
                    dto.setUploadedAt(report.getUploadedAt() == null ? null : report.getUploadedAt().toString());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public LabReport getCurrentPatientReportById(Long reportId) {
        Long patientId = authContextService.getCurrentPatientId();

        LabReport report = labReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Lab report not found"));

        if (!report.getPatient().getId().equals(patientId)) {
            throw new RuntimeException("Unauthorized");
        }

        return report;
    }
}