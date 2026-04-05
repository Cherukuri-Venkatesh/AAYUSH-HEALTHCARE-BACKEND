package com.project.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.service.DoctorAnalyticsService;

@RestController
@RequestMapping("/api/doctor/analytics")
@CrossOrigin(origins = "*")
public class DoctorAnalyticsController {

    @Autowired
    private DoctorAnalyticsService service;

    @GetMapping("/{doctorId}")
    public Map<String,Object> getAnalytics(@PathVariable Long doctorId){
        return service.getDoctorDashboard(doctorId);
    }
    
    @GetMapping("/status/{doctorId}")
    public Map<String,Long> getStatusChart(@PathVariable Long doctorId){
        return service.getStatusDistribution(doctorId);
    }
    
    @GetMapping("/weekly-consultations/{doctorId}")
    public List<Object[]> weeklyConsultations(@PathVariable Long doctorId){
        return service.getWeeklyConsultations(doctorId);
    }
    
    @GetMapping("/monthly-revenue/{doctorId}")
    public List<Object[]> monthlyRevenue(@PathVariable Long doctorId){
        return service.getMonthlyRevenue(doctorId);
    }
    
    @GetMapping("/patient-demographics")
    public List<Object[]> demographics(){
        return service.getPatientDemographics();
    }
    
    @GetMapping("/disease-trends")
    public List<Object[]> diseaseTrends(){
        return service.getDiseaseTrends();
    }
    
    @GetMapping("/utilization-heatmap/{doctorId}")
    public Map<String,Integer> heatmap(@PathVariable Long doctorId){
        return service.getUtilizationHeatMap(doctorId);
    }
}