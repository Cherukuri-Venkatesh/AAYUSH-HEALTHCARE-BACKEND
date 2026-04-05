package com.project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.dto.AppointmentDTO;
import com.project.dto.DoctorPatientDTO;
import com.project.entity.*;
import com.project.repository.*;
import com.project.security.AuthContextService;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AdminRepository doctorRepository;

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

        @Autowired
        private AuthContextService authContextService;

    // ================================
    // BOOK APPOINTMENT
    // ================================

    public AppointmentDTO bookAppointment(Long patientId, Long doctorId,
                                          LocalDate date, LocalTime time,
                                          Integer age, Double weight,
                                          String issue, String paymentType) {

                Long effectivePatientId = authContextService.resolvePatientId(patientId);

        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(6);

        if (date.isBefore(today) || date.isAfter(maxDate)) {
            throw new RuntimeException("Booking allowed only for next 7 days");
        }

        List<DoctorSchedule> schedules =
                scheduleRepository.findByDoctor_IdAndScheduleDate(doctorId, date);

        if (schedules.isEmpty()) {
            throw new RuntimeException("Doctor not available on this date");
        }

        boolean withinTime = false;

        for (DoctorSchedule schedule : schedules) {
            if (!time.isBefore(schedule.getStartTime()) &&
                time.isBefore(schedule.getEndTime())) {
                withinTime = true;
                break;
            }
        }

        if (!withinTime) {
            throw new RuntimeException("Appointment time outside doctor's working hours");
        }

        boolean alreadyBooked =
                appointmentRepository.existsByDoctor_IdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        doctorId, date, time, "CANCELLED"
                );

        if (alreadyBooked) {
            throw new RuntimeException("Doctor already has appointment at this time");
        }

        Patient patient = patientRepository.findById(effectivePatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Admindoctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Appointment appointment = new Appointment();

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setAge(age);
        appointment.setWeight(weight);
        appointment.setIssue(issue);
        appointment.setStatus("BOOKED");
        appointment.setPaymentType(PaymentType.valueOf(paymentType));
        appointment.setConsultingFees(doctor.getConsultingFees());

        Appointment saved = appointmentRepository.save(appointment);

        return convertToDTO(saved);
    }

        public AppointmentDTO bookAppointmentForCurrentPatient(Long doctorId,
                                                                                                                   LocalDate date,
                                                                                                                   LocalTime time,
                                                                                                                   Integer age,
                                                                                                                   Double weight,
                                                                                                                   String issue,
                                                                                                                   String paymentType) {
                Long patientId = authContextService.getCurrentPatientId();
                return bookAppointment(patientId, doctorId, date, time, age, weight, issue, paymentType);
        }

    // ================================
    // PATIENT APPOINTMENTS
    // ================================

    public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
        Long effectivePatientId = authContextService.resolvePatientId(patientId);

        return appointmentRepository.findByPatient_Id(effectivePatientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Appointment> getPatientUpcomingAppointments(Long patientId) {

        Long effectivePatientId = authContextService.resolvePatientId(patientId);

        return appointmentRepository
                .findByPatient_IdAndAppointmentDateAfter(
                        effectivePatientId,
                        LocalDate.now()
                );
    }

    public List<Appointment> getPatientHistory(Long patientId) {

        Long effectivePatientId = authContextService.resolvePatientId(patientId);

        return appointmentRepository
                .findByPatient_IdAndAppointmentDateBefore(
                        effectivePatientId,
                        LocalDate.now()
                );
    }

    // ================================
    // DOCTOR APPOINTMENTS
    // ================================

    public List<AppointmentDTO> getAppointmentsByDoctor(Long doctorId) {
        Long effectiveDoctorId = authContextService.resolveDoctorId(doctorId);

        return appointmentRepository.findByDoctor_Id(effectiveDoctorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

        public List<AppointmentDTO> getAllAppointments() {
                return appointmentRepository.findAll()
                                .stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

    public List<AppointmentDTO> getCurrentDoctorAppointments() {
        Long doctorId = authContextService.getCurrentDoctorId();
        return getAppointmentsByDoctor(doctorId);
    }

    public List<DoctorPatientDTO> getCurrentDoctorPatients() {
        Long doctorId = authContextService.getCurrentDoctorId();

        return appointmentRepository.findByDoctor_Id(doctorId)
                .stream()
                // Patient Management should show only patients who have at least one booked-lifecycle appointment
                .filter(appointment -> appointment.getPatient() != null)
                .filter(appointment -> isBookedLifecycleStatus(appointment.getStatus()))
                .sorted(Comparator
                        .comparing(Appointment::getAppointmentDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Appointment::getAppointmentTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toMap(
                        appointment -> appointment.getPatient().getId(),
                        appointment -> appointment,
                        (first, second) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .map(this::convertToDoctorPatientDTO)
                .collect(Collectors.toList());
    }

    private boolean isBookedLifecycleStatus(String status) {
        return Set.of("BOOKED", "COMPLETED", "CANCELLED").contains(status);
    }

    public List<Appointment> getDoctorTodayAppointments(Long doctorId) {

        Long effectiveDoctorId = authContextService.resolveDoctorId(doctorId);

        return appointmentRepository
                .findByDoctor_IdAndAppointmentDate(
                        effectiveDoctorId,
                        LocalDate.now()
                );
    }

    public List<Appointment> getDoctorUpcomingAppointments(Long doctorId) {

        Long effectiveDoctorId = authContextService.resolveDoctorId(doctorId);

        return appointmentRepository
                .findByDoctor_IdAndAppointmentDateAfter(
                        effectiveDoctorId,
                        LocalDate.now()
                );
    }

    public List<Appointment> getDoctorPastAppointments(Long doctorId) {

        Long effectiveDoctorId = authContextService.resolveDoctorId(doctorId);

        return appointmentRepository
                .findByDoctor_IdAndAppointmentDateBefore(
                        effectiveDoctorId,
                        LocalDate.now()
                );
    }

    public List<Appointment> getDoctorCancelledAppointments(Long doctorId) {

        Long effectiveDoctorId = authContextService.resolveDoctorId(doctorId);

        return appointmentRepository
                .findByDoctor_IdAndStatus(
                        effectiveDoctorId,
                        "CANCELLED"
                );
    }

    // ================================
    // UPDATE STATUS
    // ================================

    public void updateStatus(Long id, String status) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

                if (authContextService.isDoctor()) {
                        Long doctorId = authContextService.getCurrentDoctorId();
                        if (!appointment.getDoctor().getId().equals(doctorId)) {
                                throw new RuntimeException("Unauthorized");
                        }

                        if ("COMPLETED".equalsIgnoreCase(status)) {
                                LocalDateTime slotDateTime = LocalDateTime.of(
                                                appointment.getAppointmentDate(),
                                                appointment.getAppointmentTime());

                                if (LocalDateTime.now().isBefore(slotDateTime)) {
                                        throw new RuntimeException("Doctor can mark appointment as completed only after slot time");
                                }
                        }
                }

        appointment.setStatus(status);

        appointmentRepository.save(appointment);
    }

    // ================================
    // CANCEL APPOINTMENTS
    // ================================

    public void cancelAppointment(Long appointmentId, Long patientId) {

                Long effectivePatientId = authContextService.resolvePatientId(patientId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

                if (!appointment.getPatient().getId().equals(effectivePatientId)) {
                        throw new RuntimeException("Unauthorized");
        }

        appointment.setStatus("CANCELLED");

        appointmentRepository.save(appointment);
    }

        public void cancelCurrentPatientAppointment(Long appointmentId) {
                Long patientId = authContextService.getCurrentPatientId();
                cancelAppointment(appointmentId, patientId);
        }

                public AppointmentDTO rescheduleCurrentPatientAppointment(Long appointmentId, LocalDate date, LocalTime time) {
                        Long patientId = authContextService.getCurrentPatientId();

                        Appointment appointment = appointmentRepository.findById(appointmentId)
                                        .orElseThrow(() -> new RuntimeException("Appointment not found"));

                        if (!appointment.getPatient().getId().equals(patientId)) {
                                throw new RuntimeException("Unauthorized");
                        }

                        if (!"BOOKED".equalsIgnoreCase(appointment.getStatus())) {
                                throw new RuntimeException("Only booked appointments can be rescheduled");
                        }

                        LocalDateTime currentSlot = LocalDateTime.of(
                                        appointment.getAppointmentDate(),
                                        appointment.getAppointmentTime());
                        if (LocalDateTime.now().isAfter(currentSlot.minusHours(24))) {
                                throw new RuntimeException("Reschedule is allowed only until 24 hours before the appointment slot");
                        }

                        LocalDate today = LocalDate.now();
                        LocalDate maxDate = today.plusDays(6);
                        if (date.isBefore(today) || date.isAfter(maxDate)) {
                                throw new RuntimeException("Reschedule allowed only for next 7 days");
                        }

                        Long doctorId = appointment.getDoctor().getId();
                        List<DoctorSchedule> schedules = scheduleRepository.findByDoctor_IdAndScheduleDate(doctorId, date);
                        if (schedules.isEmpty()) {
                                throw new RuntimeException("Doctor not available on selected date");
                        }

                        boolean withinTime = schedules.stream().anyMatch(schedule ->
                                        !time.isBefore(schedule.getStartTime()) && time.isBefore(schedule.getEndTime()));
                        if (!withinTime) {
                                throw new RuntimeException("Selected time is outside doctor's schedule");
                        }

                        boolean slotTaken = appointmentRepository
                                        .existsByDoctor_IdAndAppointmentDateAndAppointmentTimeAndStatusNotAndIdNot(
                                                        doctorId,
                                                        date,
                                                        time,
                                                        "CANCELLED",
                                                        appointmentId);

                        if (slotTaken) {
                                throw new RuntimeException("Selected slot is no longer available");
                        }

                        appointment.setAppointmentDate(date);
                        appointment.setAppointmentTime(time);

                        Appointment saved = appointmentRepository.save(appointment);
                        return convertToDTO(saved);
                }

        public List<AppointmentDTO> getCurrentPatientAppointments() {
                Long patientId = authContextService.getCurrentPatientId();
                return getAppointmentsByPatient(patientId);
        }

    public void doctorCancelAppointment(Long appointmentId, Long doctorId) {

                Long effectiveDoctorId = authContextService.resolveDoctorId(doctorId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

                if (!appointment.getDoctor().getId().equals(effectiveDoctorId)) {
                        throw new RuntimeException("Unauthorized");
        }

        appointment.setStatus("CANCELLED");

        appointmentRepository.save(appointment);
    }

    // ================================
    // DOCTOR ANALYTICS
    // ================================

    public Map<String, Object> getDoctorAnalytics(Long doctorId) {

        Long effectiveDoctorId = authContextService.resolveDoctorId(doctorId);

        Map<String, Object> analytics = new HashMap<>();

        long total = appointmentRepository.countByDoctor_Id(effectiveDoctorId);
        long completed = appointmentRepository.countByDoctor_IdAndStatus(effectiveDoctorId, "COMPLETED");
        long cancelled = appointmentRepository.countByDoctor_IdAndStatus(effectiveDoctorId, "CANCELLED");

        Integer earnings = appointmentRepository.getDoctorTotalEarnings(effectiveDoctorId);
        if (earnings == null) earnings = 0;

        List<Appointment> today =
                appointmentRepository.findByDoctor_IdAndAppointmentDate(
                        effectiveDoctorId,
                        LocalDate.now()
                );

        analytics.put("totalAppointments", total);
        analytics.put("completedAppointments", completed);
        analytics.put("cancelledAppointments", cancelled);
        analytics.put("totalEarnings", earnings);
        analytics.put("todayAppointments", today.size());

        return analytics;
    }

    // ================================
    // DTO CONVERTER
    // ================================

    private AppointmentDTO convertToDTO(Appointment appointment) {

        AppointmentDTO dto = new AppointmentDTO();

        dto.setId(appointment.getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientName(appointment.getPatient().getName());
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setDoctorName(appointment.getDoctor().getName());
                dto.setDoctorSpecialization(appointment.getDoctor().getSpecialization());
                dto.setHospitalName(appointment.getDoctor().getHospitalName());
        dto.setAge(appointment.getAge());
        dto.setWeight(appointment.getWeight());
        dto.setIssue(appointment.getIssue());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setConsultingFees(appointment.getConsultingFees());
                dto.setPaymentType(appointment.getPaymentType() == null ? null : appointment.getPaymentType().name());
        dto.setPrescriptionPath(appointment.getPrescriptionPath());
        dto.setLabReportPath(appointment.getLabReportPath());

        return dto;
    }

        private DoctorPatientDTO convertToDoctorPatientDTO(Appointment appointment) {
                DoctorPatientDTO dto = new DoctorPatientDTO();
                dto.setPatientId(appointment.getPatient().getId());
                dto.setName(appointment.getPatient().getName());
                dto.setDoctorId(appointment.getDoctor().getId());
                dto.setPhone(appointment.getPatient().getPhone());
                dto.setAge(appointment.getAge());
                dto.setWeight(appointment.getWeight());
                dto.setIssue(appointment.getIssue());
                dto.setAppointmentDate(appointment.getAppointmentDate() == null ? null : appointment.getAppointmentDate().toString());
                dto.setAppointmentTime(appointment.getAppointmentTime() == null ? null : appointment.getAppointmentTime().toString());
                dto.setConsultingFees(appointment.getConsultingFees());
                dto.setPaymentType(appointment.getPaymentType() == null ? null : appointment.getPaymentType().name());
                dto.setPrescriptionPath(appointment.getPrescriptionPath());
                dto.setLabReportPath(appointment.getLabReportPath());
                dto.setStatus(appointment.getStatus());
                dto.setAppointmentId(appointment.getId());
                return dto;
        }
}