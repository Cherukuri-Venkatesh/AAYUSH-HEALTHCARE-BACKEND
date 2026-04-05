package com.project.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.project.dto.AppointmentDTO;
import com.project.dto.BookAppointmentRequest;
import com.project.dto.DoctorDTO;
import com.project.dto.LabReportDTO;
import com.project.dto.PatientDTO;
import com.project.dto.PatientProfileUpdateRequest;
import com.project.dto.PasswordUpdateRequest;
import com.project.dto.RescheduleAppointmentRequest;
import com.project.dto.PrescriptionDTO;
import com.project.entity.LabReport;
import com.project.entity.Prescription;
import com.project.service.AppointmentService;
import com.project.service.LabReportService;
import com.project.service.PatientService;
import com.project.service.PrescriptionService;
import com.project.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
@Validated
public class PatientApiController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private LabReportService labReportService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/me")
    public PatientDTO getMyProfile() {
        return patientService.getCurrentPatientProfile();
    }

    @PutMapping("/me")
    public PatientDTO updateMyProfile(@RequestBody PatientProfileUpdateRequest request) {
        return patientService.updateCurrentPatientProfile(request);
    }

    @PutMapping("/password")
    public String updateMyPassword(@RequestBody PasswordUpdateRequest request) {
        patientService.updateCurrentPatientPassword(request.getNewPassword());
        return "Password updated successfully";
    }

    @PostMapping("/appointments/book")
    public AppointmentDTO bookAppointment(@Valid @RequestBody BookAppointmentRequest request) {
        return appointmentService.bookAppointmentForCurrentPatient(
                request.getDoctorId(),
                LocalDate.parse(request.getDate()),
                LocalTime.parse(request.getTime()),
                request.getAge(),
                request.getWeight(),
                request.getIssue(),
                request.getPaymentType().toUpperCase()
        );
    }

    @GetMapping("/appointments")
    public List<AppointmentDTO> getMyAppointments() {
        return appointmentService.getCurrentPatientAppointments();
    }

    @GetMapping("/doctors")
    public List<DoctorDTO> getDoctorsForPatient() {
        return adminService.getAllDoctors();
    }

    @GetMapping("/doctors/top")
    public List<DoctorDTO> getTopDoctorsForPatient() {
        return adminService.sortDoctorsByFees();
    }

    @PutMapping("/appointments/{appointmentId}/cancel")
    public String cancelMyAppointment(@PathVariable Long appointmentId) {
        appointmentService.cancelCurrentPatientAppointment(appointmentId);
        return "Appointment cancelled successfully";
    }

    @PutMapping("/appointments/{appointmentId}/reschedule")
    public AppointmentDTO rescheduleMyAppointment(
            @PathVariable Long appointmentId,
            @RequestBody RescheduleAppointmentRequest request) {
        return appointmentService.rescheduleCurrentPatientAppointment(
                appointmentId,
                LocalDate.parse(request.getDate()),
                LocalTime.parse(request.getTime())
        );
    }

    @GetMapping("/prescriptions")
    public List<PrescriptionDTO> getMyPrescriptions() {
        return prescriptionService.getPatientPrescriptions(null);
    }

    @GetMapping("/reports")
    public List<LabReportDTO> getMyReports() {
        return labReportService.getCurrentPatientReports();
    }

    @GetMapping("/download-report/{id}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) throws IOException {
        LabReport report = labReportService.getCurrentPatientReportById(id);
        return buildFileDownloadResponse(report.getReportPath());
    }

    @GetMapping("/download-prescription/{id}")
    public ResponseEntity<byte[]> downloadPrescription(@PathVariable Long id) throws IOException {
        Prescription prescription = prescriptionService.getPrescriptionByIdForCurrentUser(id);
        return buildFileDownloadResponse(prescription.getFilePath());
    }

    private ResponseEntity<byte[]> buildFileDownloadResponse(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("File not found");
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = fis.readAllBytes();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        }
    }
}
