package com.project.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admindoctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    private String specialization;

    private Integer consultingFees;

    private String degree;

    private String experience;

    private String addressLine1;

    // ✅ STORE hospital name
    private String hospitalName;

    @Column(length = 1000)
    private String aboutDoctor;

    @Column(unique = true)
    private String whatsappNumber;
    
    private String role;

    // ✅ RELATION (hospitalId)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    @JsonBackReference
    private Hospital hospital;

    // Keep appointments when doctor is deleted (patients need history)
    // Use only PERSIST and MERGE, not REMOVE/DELETE
    @OneToMany(mappedBy = "doctor", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Appointment> appointments;

    // Delete schedules when doctor is deleted (doctor-specific work schedules)
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<DoctorSchedule> schedules;
}