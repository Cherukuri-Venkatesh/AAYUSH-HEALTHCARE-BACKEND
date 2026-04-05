package com.project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.dto.AppointmentHistoryDTO;
import com.project.dto.PatientHistoryDTO;
import com.project.entity.Appointment;
import com.project.entity.Patient;
import com.project.repository.AppointmentRepository;
import com.project.repository.PatientRepository;
import com.project.security.AuthContextService;

@Service
public class PatientHistoryService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

        @Autowired
        private AuthContextService authContextService;

    public PatientHistoryDTO getPatientHistory(Long patientId) {

                Long effectivePatientId = patientId;
                Long effectiveDoctorId = null;

                if (authContextService.isPatient()) {
                        effectivePatientId = authContextService.getCurrentPatientId();
                } else if (authContextService.isDoctor()) {
                        effectiveDoctorId = authContextService.getCurrentDoctorId();
                        boolean hasRelationship = appointmentRepository.existsByDoctor_IdAndPatient_Id(effectiveDoctorId, patientId);
                        if (!hasRelationship) {
                                throw new RuntimeException("Unauthorized");
                        }
                }

                Patient patient = patientRepository.findById(effectivePatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Appointment> appointments;

        if (effectiveDoctorId != null) {
                appointments = appointmentRepository.findByDoctor_IdAndPatient_Id(effectiveDoctorId, effectivePatientId);
        } else {
                appointments = appointmentRepository.findByPatient_Id(effectivePatientId);
        }

        List<AppointmentHistoryDTO> appointmentHistory =
                appointments.stream()
                        .map(a -> AppointmentHistoryDTO.builder()
                                .appointmentId(a.getId())
                                .doctorId(a.getDoctor().getId())
                                .doctorName(a.getDoctor().getName())
                                .issue(a.getIssue())
                                .appointmentDate(a.getAppointmentDate())
                                .appointmentTime(a.getAppointmentTime())
                                .consultingFees(a.getConsultingFees())
                                .paymentType(a.getPaymentType() == null ? null : a.getPaymentType().name())
                                .age(a.getAge())
                                .weight(a.getWeight())
                                .status(a.getStatus())
                                .prescriptionPath(a.getPrescriptionPath())
                                .labReportPath(a.getLabReportPath())
                                .build())
                        .collect(Collectors.toList());

        return PatientHistoryDTO.builder()
                .patientId(patient.getId())
                .patientName(patient.getName())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .appointments(appointmentHistory)
                .build();
    }
}