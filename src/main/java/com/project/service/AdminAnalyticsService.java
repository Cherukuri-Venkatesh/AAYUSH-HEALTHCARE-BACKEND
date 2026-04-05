package com.project.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.repository.AdminRepository;
import com.project.repository.AppointmentRepository;
import com.project.repository.HospitalRepository;
import com.project.repository.PatientRepository;

@Service
public class AdminAnalyticsService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AdminRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Map<String,Object> getAdminDashboard(){

        Map<String,Object> map = new HashMap<>();

        map.put("totalPatients",patientRepository.count());

        map.put("totalHospitals",hospitalRepository.count());

        map.put("totalDoctors",doctorRepository.count());

        map.put("totalAppointments",appointmentRepository.count());

        map.put("completedAppointments",
                appointmentRepository.countByStatus("COMPLETED"));

        map.put("cancelledAppointments",
                appointmentRepository.countByStatus("CANCELLED"));

        Integer revenue =
                appointmentRepository.getTotalSystemEarnings();

        if(revenue == null) revenue = 0;

        map.put("totalRevenue",revenue);

        return map;
    }
}