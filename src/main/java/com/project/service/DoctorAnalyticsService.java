package com.project.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.repository.AppointmentRepository;
import com.project.repository.DoctorScheduleRepository;
import com.project.repository.PatientRepository;

@Service
public class DoctorAnalyticsService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    @Autowired
    private PatientRepository patientRepository;

    public Map<String,Object> getDoctorDashboard(Long doctorId){

        Map<String,Object> map = new HashMap<>();

        LocalDate today = LocalDate.now();

        long todaysAppointments =
                appointmentRepository.countByDoctor_IdAndAppointmentDate(doctorId,today);

        Integer todaysRevenue =
                appointmentRepository.getDoctorTodayRevenue(doctorId,today);

        if(todaysRevenue == null) todaysRevenue = 0;

        long completed =
                appointmentRepository.countByDoctor_IdAndStatus(doctorId,"COMPLETED");

        long total =
                appointmentRepository.countByDoctor_Id(doctorId);

        long pending =
                appointmentRepository.countByDoctor_IdAndStatus(doctorId,"BOOKED");

        Integer totalRevenue =
                appointmentRepository.getDoctorTotalEarnings(doctorId);

        if(totalRevenue == null) totalRevenue = 0;

        map.put("todaysAppointments",todaysAppointments);
        map.put("todaysRevenue",todaysRevenue);
        map.put("completedAppointments",completed);
        map.put("totalAppointments",total);
        map.put("pendingConsultations",pending);
        map.put("totalRevenue",totalRevenue);

        return map;
    }

    public Map<String, Long> getStatusDistribution(Long doctorId){

        List<Object[]> result =
                appointmentRepository.getDoctorStatusDistribution(doctorId);

        Map<String, Long> map = new HashMap<>();

        for(Object[] row : result){
            map.put((String)row[0], (Long)row[1]);
        }

        return map;
    }

    public List<Object[]> getWeeklyConsultations(Long doctorId){
        return appointmentRepository.getWeeklyConsultations(doctorId);
    }

    public List<Object[]> getMonthlyRevenue(Long doctorId){
        return appointmentRepository.getMonthlyRevenue(doctorId);
    }

    public List<Object[]> getPatientDemographics(){
        return patientRepository.getGenderDistribution();
    }

    public List<Object[]> getDiseaseTrends(){
        return appointmentRepository.getDiseaseTrends();
    }

    public Map<String,Integer> getUtilizationHeatMap(Long doctorId){

        List<Object[]> completed =
                appointmentRepository.getWeeklyCompletedAppointments(doctorId);

        List<Object[]> schedules =
                scheduleRepository.getWeeklySchedules(doctorId);

        Map<String,Integer> completedMap = new HashMap<>();
        Map<String,Integer> scheduleMap = new HashMap<>();

        for(Object[] row : completed){
            completedMap.put((String)row[0], ((Long)row[1]).intValue());
        }

        for(Object[] row : schedules){
            scheduleMap.put((String)row[0], ((Long)row[1]).intValue());
        }

        Map<String,Integer> heatmap = new HashMap<>();

        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};

        for(String day : days){

            int booked = completedMap.getOrDefault(day,0);
            int total = scheduleMap.getOrDefault(day,0);

            int utilization = 0;

            if(total > 0){
                utilization = (booked * 100) / total;
            }

            heatmap.put(day,utilization);
        }

        return heatmap;
    }
}