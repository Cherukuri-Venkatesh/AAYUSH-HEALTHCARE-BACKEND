package com.project.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many Appointments → One Patient
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    // Many Appointments → One Doctor
    // SET NULL when doctor is deleted - keep appointment history for patient records
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Admindoctor doctor;

    private Integer age;

    private Double weight;

    @Column(length = 1000)
    private String issue;

    private LocalDate appointmentDate;

    private LocalTime appointmentTime;

    private String status;

    private Integer consultingFees;

    private String prescriptionPath;

    private String labReportPath;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
}