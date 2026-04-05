package com.project.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.service.AdminAnalyticsService;

@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = "*")
public class AdminAnalyticsController {

    @Autowired
    private AdminAnalyticsService service;

    @GetMapping
    public Map<String,Object> dashboard(){
        return service.getAdminDashboard();
    }
}