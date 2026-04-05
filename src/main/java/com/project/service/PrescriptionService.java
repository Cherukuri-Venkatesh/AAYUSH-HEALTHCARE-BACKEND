package com.project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.project.dto.PrescriptionDTO;
import com.project.entity.Admindoctor;
import com.project.entity.Appointment;
import com.project.entity.Patient;
import com.project.entity.Prescription;
import com.project.repository.AdmindoctorRepository;
import com.project.repository.AppointmentRepository;
import com.project.repository.PatientRepository;
import com.project.repository.PrescriptionRepository;
import com.project.security.AuthContextService;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

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

    public PrescriptionDTO uploadPrescription(
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

        String filePath = fileStorageService.savePrescription(file);

        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setDoctor(doctor);
        prescription.setPatient(patient);
        prescription.setFilePath(filePath);
        prescription.setUploadedAt(LocalDateTime.now());

        prescriptionRepository.save(prescription);

        appointment.setPrescriptionPath(filePath);
        appointmentRepository.save(appointment);

        return mapToDTO(prescription);
    }

    public PrescriptionDTO getPrescriptionByAppointment(Long appointmentId) {

        Prescription prescription =
                prescriptionRepository.findByAppointment_Id(appointmentId);

        if (prescription == null) {
            throw new RuntimeException("Prescription not found");
        }

        if (authContextService.isPatient()) {
            Long patientId = authContextService.getCurrentPatientId();
            if (!prescription.getPatient().getId().equals(patientId)) {
                throw new RuntimeException("Unauthorized");
            }
        }

        if (authContextService.isDoctor()) {
            Long doctorId = authContextService.getCurrentDoctorId();
            if (!prescription.getDoctor().getId().equals(doctorId)) {
                throw new RuntimeException("Unauthorized");
            }
        }

        return mapToDTO(prescription);
    }

    public List<PrescriptionDTO> getPatientPrescriptions(Long patientId) {

        if (authContextService.isPatient()) {
            Long effectivePatientId = authContextService.getCurrentPatientId();

            return prescriptionRepository.findByPatient_Id(effectivePatientId)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        if (authContextService.isDoctor()) {
            Long doctorId = authContextService.getCurrentDoctorId();
            return prescriptionRepository.findByDoctor_IdAndPatient_Id(doctorId, patientId)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        return prescriptionRepository.findByPatient_Id(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Prescription getPrescriptionByIdForCurrentUser(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        if (authContextService.isAdmin()) {
            return prescription;
        }

        if (authContextService.isDoctor()) {
            Long doctorId = authContextService.getCurrentDoctorId();
            if (!prescription.getDoctor().getId().equals(doctorId)) {
                throw new RuntimeException("Unauthorized");
            }
            return prescription;
        }

        if (authContextService.isPatient()) {
            Long patientId = authContextService.getCurrentPatientId();
            if (!prescription.getPatient().getId().equals(patientId)) {
                throw new RuntimeException("Unauthorized");
            }
            return prescription;
        }

        throw new RuntimeException("Unauthorized");
    }

    private PrescriptionDTO mapToDTO(Prescription p) {

        PrescriptionDTO dto = new PrescriptionDTO();

        dto.setId(p.getId());
        dto.setAppointmentId(p.getAppointment().getId());
        dto.setDoctorId(p.getDoctor().getId());
        dto.setDoctorName(p.getDoctor().getName());
        dto.setPatientId(p.getPatient().getId());
        dto.setFilePath(p.getFilePath());
        dto.setUploadedAt(p.getUploadedAt() == null ? null : p.getUploadedAt().toString());

        return dto;
    }
}