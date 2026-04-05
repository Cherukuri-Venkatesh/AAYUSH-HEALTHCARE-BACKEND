package com.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.dto.PrescriptionDTO;
import com.project.service.PrescriptionService;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping("/upload")
    public PrescriptionDTO upload(
            @RequestParam Long appointmentId,
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam MultipartFile file) {

        return prescriptionService.uploadPrescription(
                appointmentId, doctorId, patientId, file);
    }

    @GetMapping("/appointment/{appointmentId}")
    public PrescriptionDTO getByAppointment(@PathVariable Long appointmentId) {

        return prescriptionService.getPrescriptionByAppointment(appointmentId);
    }

    @GetMapping("/patient/{patientId}")
    public List<PrescriptionDTO> getPatientPrescriptions(@PathVariable Long patientId) {

        return prescriptionService.getPatientPrescriptions(patientId);
    }
}