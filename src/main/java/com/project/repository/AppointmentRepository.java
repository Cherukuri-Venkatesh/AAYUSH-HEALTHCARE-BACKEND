package com.project.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.project.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ================================
    // PATIENT QUERIES
    // ================================

    List<Appointment> findByPatient_Id(Long patientId);

        @Transactional
        void deleteByPatient_Id(Long patientId);

    List<Appointment> findByPatient_IdAndStatus(Long patientId, String status);

    List<Appointment> findByPatient_IdAndAppointmentDateAfter(Long patientId, LocalDate date);

    List<Appointment> findByPatient_IdAndAppointmentDateBefore(Long patientId, LocalDate date);


    // ================================
    // DOCTOR QUERIES
    // ================================

    List<Appointment> findByDoctor_Id(Long doctorId);

        @Transactional
        void deleteByDoctor_Id(Long doctorId);

    List<Appointment> findByDoctor_IdAndStatus(Long doctorId, String status);

    List<Appointment> findByDoctor_IdAndAppointmentDate(Long doctorId, LocalDate date);

    List<Appointment> findByDoctor_IdAndAppointmentDateAfter(Long doctorId, LocalDate date);

    List<Appointment> findByDoctor_IdAndAppointmentDateBefore(Long doctorId, LocalDate date);

        List<Appointment> findByDoctor_IdAndPatient_Id(Long doctorId, Long patientId);


    // ================================
    // BOOKING VALIDATION
    // ================================

    boolean existsByDoctor_IdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String status
    );

    boolean existsByDoctor_IdAndAppointmentDateAndAppointmentTimeAndStatusNotAndIdNot(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String status,
            Long id
    );

    boolean existsByDoctor_IdAndAppointmentDateAndStatusNot(
            Long doctorId,
            LocalDate appointmentDate,
            String status
    );

    List<Appointment> findByDoctor_IdAndAppointmentDateAndStatusNot(
            Long doctorId,
            LocalDate appointmentDate,
            String status
    );

    boolean existsByDoctor_IdAndPatient_Id(Long doctorId, Long patientId);


    // ================================
    // ANALYTICS
    // ================================

    long countByDoctor_Id(Long doctorId);

    long countByStatus(String status);


    // ================================
    // REVENUE QUERIES
    // ================================

    @Query("SELECT SUM(a.consultingFees) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.status = 'COMPLETED'")
    Integer getDoctorTotalEarnings(@Param("doctorId") Long doctorId);

    @Query("SELECT SUM(a.consultingFees) FROM Appointment a WHERE a.status = 'COMPLETED'")
    Integer getTotalSystemEarnings();
    
 // today's appointments
    long countByDoctor_IdAndAppointmentDate(Long doctorId, LocalDate date);

    // today's revenue
    @Query("SELECT SUM(a.consultingFees) FROM Appointment a WHERE a.doctor.id=:doctorId AND a.appointmentDate=:date AND a.status='COMPLETED'")
    Integer getDoctorTodayRevenue(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // completed appointments
    long countByDoctor_IdAndStatus(Long doctorId, String status);
    
    @Query("SELECT a.status, COUNT(a) FROM Appointment a WHERE a.doctor.id=:doctorId GROUP BY a.status")
    List<Object[]> getDoctorStatusDistribution(@Param("doctorId") Long doctorId);
    
    @Query("SELECT DAYNAME(a.appointmentDate), COUNT(a) FROM Appointment a WHERE a.doctor.id=:doctorId GROUP BY DAYNAME(a.appointmentDate)")
    List<Object[]> getWeeklyConsultations(@Param("doctorId") Long doctorId);
    
    @Query("SELECT MONTH(a.appointmentDate), SUM(a.consultingFees) FROM Appointment a WHERE a.doctor.id=:doctorId AND a.status='COMPLETED' GROUP BY MONTH(a.appointmentDate)")
    List<Object[]> getMonthlyRevenue(@Param("doctorId") Long doctorId);
    
    @Query("SELECT a.issue, COUNT(a) FROM Appointment a GROUP BY a.issue ORDER BY COUNT(a) DESC")
    List<Object[]> getDiseaseTrends();
    
    @Query("""
    		SELECT DAYNAME(a.appointmentDate), COUNT(a)
    		FROM Appointment a
    		WHERE a.doctor.id = :doctorId
    		AND a.status = 'COMPLETED'
    		GROUP BY DAYNAME(a.appointmentDate)
    		""")
    		List<Object[]> getWeeklyCompletedAppointments(@Param("doctorId") Long doctorId);

}