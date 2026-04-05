package com.project.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One appointment → one prescription
    @OneToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    // Doctor who uploaded - SET NULL when doctor is deleted
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Admindoctor doctor;

    // Patient who owns prescription
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private String filePath;

    private LocalDateTime uploadedAt;
}