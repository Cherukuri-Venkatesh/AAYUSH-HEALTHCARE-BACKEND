package com.project.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.dto.AppointmentDTO;
import com.project.entity.Appointment;
import com.project.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/book")
    public AppointmentDTO bookAppointment(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam Integer age,
            @RequestParam Double weight,
            @RequestParam String issue,
            @RequestParam String paymentType) {

        return appointmentService.bookAppointment(
                patientId,
                doctorId,
                LocalDate.parse(date),
                LocalTime.parse(time),
                age,
                weight,
                issue,
                paymentType
        );
    }

    // ======================
    // PATIENT
    // ======================

    @GetMapping("/patient/{patientId}")
    public List<AppointmentDTO> getPatientAppointments(@PathVariable Long patientId) {
        return appointmentService.getAppointmentsByPatient(patientId);
    }

    @GetMapping("/patient/upcoming/{patientId}")
    public List<Appointment> patientUpcoming(@PathVariable Long patientId) {
        return appointmentService.getPatientUpcomingAppointments(patientId);
    }

    @GetMapping("/patient/history/{patientId}")
    public List<Appointment> patientHistory(@PathVariable Long patientId) {
        return appointmentService.getPatientHistory(patientId);
    }

    // ======================
    // DOCTOR
    // ======================

    @GetMapping("/doctor/{doctorId}")
    public List<AppointmentDTO> getDoctorAppointments(@PathVariable Long doctorId) {
        return appointmentService.getAppointmentsByDoctor(doctorId);
    }

    @GetMapping("/doctor/upcoming/{doctorId}")
    public List<Appointment> doctorUpcoming(@PathVariable Long doctorId) {
        return appointmentService.getDoctorUpcomingAppointments(doctorId);
    }

    @GetMapping("/doctor/today/{doctorId}")
    public List<Appointment> doctorToday(@PathVariable Long doctorId) {
        return appointmentService.getDoctorTodayAppointments(doctorId);
    }

    @GetMapping("/doctor/past/{doctorId}")
    public List<Appointment> doctorPast(@PathVariable Long doctorId) {
        return appointmentService.getDoctorPastAppointments(doctorId);
    }

    @GetMapping("/doctor/cancelled/{doctorId}")
    public List<Appointment> doctorCancelled(@PathVariable Long doctorId) {
        return appointmentService.getDoctorCancelledAppointments(doctorId);
    }

    // ======================
    // STATUS
    // ======================

    @GetMapping("/all")
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @PutMapping("/status/{appointmentId}")
    public String updateStatus(
            @PathVariable Long appointmentId,
            @RequestParam String status) {

        appointmentService.updateStatus(appointmentId, status);

        return "Appointment Status Updated Successfully";
    }

    @PutMapping("/cancel")
    public String cancelAppointment(
            @RequestParam Long appointmentId,
            @RequestParam Long patientId) {

        appointmentService.cancelAppointment(appointmentId, patientId);

        return "Appointment Cancelled Successfully";
    }

    @PutMapping("/doctor/cancel")
    public String doctorCancel(
            @RequestParam Long appointmentId,
            @RequestParam Long doctorId) {

        appointmentService.doctorCancelAppointment(appointmentId, doctorId);

        return "Doctor Cancelled Appointment Successfully";
    }

    // ======================
    // ANALYTICS
    // ======================

    @GetMapping("/doctor/analytics/{doctorId}")
    public Map<String, Object> doctorAnalytics(@PathVariable Long doctorId) {
        return appointmentService.getDoctorAnalytics(doctorId);
    }
}